package _22_10;

import _0_utils.Utils;
import lazy.LazyGui;
import lazy.PickerColor;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;

public class StaticMaskedRect extends PApplet {
    PGraphics pg;
    PGraphics noiseMask;
    PGraphics lineCanvas;
    LazyGui gui;

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
        lineCanvas = createGraphics(width, height, P2D);
        colorMode(HSB,1,1,1,1);
    }

    public void draw() {
        pg.beginDraw();
        pg.background(gui.colorPicker("background/color").hex);
        drawNoiseMaskRectangle();
        drawCentralCircle();
        pg.endDraw();
        image(pg, 0, 0);
        Utils.record(this, gui);
    }

    private void drawCentralCircle() {
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
        String mode = gui.stringPicker("frame/mode", new String[]{ "masked line", "noise debug", "line debug"});
        if(gui.button("frame/regen once") || frameCount == 1 ||
                (gui.toggle("frame/regen always") && 0 == frameCount % gui.sliderInt("frame/regen speed", 5, 1, Integer.MAX_VALUE))){
            regenerateNoiseMask();
        }
        if(mode.equals("noise debug")){
            pg.image(noiseMask, 0, 0);
        }
        if(mode.equals("line debug") || mode.equals("masked line")){
            lineCanvas.beginDraw();
            drawLine(lineCanvas);
            if(mode.equals("masked line")){
                lineCanvas.mask(noiseMask);
            }
            lineCanvas.endDraw();
            pg.image(lineCanvas, 0, 0);
        }
    }

    private void drawLine(PGraphics pg) {
        pg.background(gui.colorPicker("frame/line/background").hex);
        pg.noFill();
        pg.strokeWeight(gui.slider("frame/line/weight", 5));
        pg.stroke(gui.colorPicker("frame/line/stroke", color(0)).hex);
        pg.rectMode(CENTER);
        pg.pushMatrix();
        float size = gui.slider("frame/line/size", 200);
        pg.rect(width / 2f, height / 2f, size, size);
        pg.popMatrix();
    }

    private void regenerateNoiseMask() {
        noiseMask.beginDraw();
        float globalFreq = gui.slider("frame/noise/freq", 0.01f);
        float freqMult = gui.slider("frame/noise/freq mult", 2);
        float globalAmp = gui.slider("frame/noise/base amp", 1);
        float ampMult = gui.slider("frame/noise/amp mult", 0.5f);
        float ampConst = gui.slider("frame/noise/amp const", 0);
        float offsetX = gui.slider("frame/noise/x", 0);
        float offsetY = gui.slider("frame/noise/y", 0);
        int octaves = gui.sliderInt("frame/noise/octaves", 1);
        noiseMask.loadPixels();
        int pixelate = gui.sliderInt("frame/noise/pixelate", 10, 1, Integer.MAX_VALUE);
        for (int y = 0; y < noiseMask.height; y+= pixelate) {
            for(int x = 0; x < noiseMask.width; x+= pixelate){
                float sum = 0;
                float amp = globalAmp;
                float freq = globalFreq;
                for (int octaveIndex = 0; octaveIndex < octaves; octaveIndex++) {
                    sum += amp * (-1 + 2 * noise(offsetX + x * freq,offsetY + y * freq));
                    amp *= ampMult;
                    freq *= freqMult;
                }
                sum = 0.5f + 0.5f * sum;
                sum += ampConst;
                sum = constrain(sum, 0, 1);
                for (int xi = x; xi < x + pixelate; xi++) {
                    for (int yi = y; yi < y + pixelate; yi++) {
                        if(xi < noiseMask.width && yi < noiseMask.height){
                            noiseMask.pixels[xi + yi * noiseMask.width] = color(sum);
                        }
                    }
                }
            }
        }
        noiseMask.updatePixels();
        noiseMask.endDraw();
    }
}
