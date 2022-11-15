package _22_10;

import _0_utils.Utils;
import lazy.LazyGui;
import lazy.PickerColor;
import lazy.ShaderReloader;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.opengl.PShader;

public class StaticMaskedRect extends PApplet {
    PGraphics pg;
    PGraphics noiseMask;
    PGraphics formCanvas;
    LazyGui gui;
    private float noiseTime;

    public static void main(String[] args) {
        PApplet.main(java.lang.invoke.MethodHandles.lookup().lookupClass());
    }

    public void settings() {
        fullScreen(P2D);
    }

    public void setup() {
        gui = new LazyGui(this);
        pg = createGraphics(width, height, P2D);
        noiseMask = createGraphics(width, height, P2D);
        formCanvas = createGraphics(width, height, P2D);
        colorMode(HSB,1,1,1,1);
    }

    public void draw() {
        pg.beginDraw();
        pg.background(gui.colorPicker("background/color").hex);
        drawNoiseMaskRectangle();
        pg.endDraw();
        image(pg, 0, 0);
        Utils.record(this, gui);
    }

    private void drawCentralCircle(PGraphics pg) {
        if(!gui.toggle("frame/ellipse/visible", true)){
            return;
        }
        pg.pushMatrix();
        pg.translate(gui.slider("frame/ellipse/x", width/2f), gui.slider("frame/ellipse/y", height / 2f));
        pg.noStroke();
        PickerColor innerColor = gui.colorPicker("frame/ellipse/fill");
        pg.fill(innerColor.hex);
        float diam = gui.slider("frame/ellipse/size", 64);
        pg.ellipse(0, 0, diam, diam);
        int detail = gui.sliderInt("frame/ellipse/detail", 360);
        float innerRadius = diam / 2f - 2;
        float outerRadius = gui.slider("frame/ellipse/strip size", 50);
        PickerColor outerColor = gui.colorPicker("frame/ellipse/outer fill");
        pg.beginShape(PConstants.TRIANGLE_STRIP);
        for (int i = 0; i < detail; i++) {
            float theta = map(i, 0, detail-1, 0, TAU);
            pg.fill(innerColor.hex);
            pg.vertex(innerRadius * cos(theta), innerRadius * sin(theta));
            pg.fill(outerColor.hex);
            pg.vertex(outerRadius * cos(theta), outerRadius * sin(theta));
        }
        pg.endShape();
        pg.popMatrix();
    }

    private void drawNoiseMaskRectangle() {
        String modeMaskedForm = "masked form";
        String modeFormDebug = "form debug";
        String modeNoiseDebug = "noise debug";
        String mode = gui.stringPicker("frame/mode", new String[]{ modeMaskedForm, modeNoiseDebug, modeFormDebug});

        if(mode.equals(modeNoiseDebug)){
            pg.image(noiseMask, 0, 0);
        }
        if(mode.equals(modeFormDebug) || mode.equals(modeMaskedForm)){
            formCanvas.beginDraw();
            formCanvas.background(gui.colorPicker("frame/background").hex);
            drawCentralCircle(formCanvas);
            drawRect(formCanvas);
            if(mode.equals(modeMaskedForm)){
                formCanvas.mask(noiseMask);
            }
            formCanvas.endDraw();
            pg.image(formCanvas, 0, 0);
        }
    }

    private void drawRect(PGraphics pg) {
        pg.noFill();
        pg.strokeWeight(gui.slider("frame/line/weight", 5));
        pg.stroke(gui.colorPicker("frame/line/stroke", color(0)).hex);
        pg.rectMode(CENTER);
        float size = gui.slider("frame/line/size", 200);
        pg.rect(width / 2f, height / 2f, size, size);
    }

    private void regenerateNoiseMask(String pathSuffix) {
        String path = "frame/noise" + pathSuffix + "/";
        noiseMask.beginDraw();
        noiseTime += radians(gui.slider(path + "time speed"));
        String noiseShaderPath = "_22_10/StaticMaskedRect_noise.glsl";
        PShader shader = ShaderReloader.getShader(noiseShaderPath);
        shader.set("time", noiseTime);
        shader.set("offset", gui.slider(path + "x", 0), gui.slider(path + "y", 0));
        shader.set("alpha", gui.slider(path + "alpha"));
        shader.set("baseValue", gui.slider(path + "amp const", 0));
        shader.set("baseAmp",gui.slider(path + "base amp", 1));
        shader.set("baseFreqX", gui.slider(path + "freq x", 0.01f));
        shader.set("baseFreqY", gui.slider(path + "freq y", 0.01f));
        shader.set("fbmFreqMultX", gui.slider(path + "freq mult x", 2));
        shader.set("fbmFreqMultY", gui.slider(path + "freq mult y", 2));
        shader.set("fbmTimeFreqMult", gui.slider(path + "time freq mult"));
        shader.set("fbmAmpMult", gui.slider(path + "amp mult", 0.5f));
        shader.set("pixelate", gui.slider(path + "pixelate", 10, 1, Integer.MAX_VALUE));
        shader.set("inverter", gui.slider(path + "inverter", 1));
        shader.set("octaves", gui.sliderInt(path + "octaves", 1));
        ShaderReloader.filter(noiseShaderPath, noiseMask);
        noiseMask.endDraw();
    }
}
