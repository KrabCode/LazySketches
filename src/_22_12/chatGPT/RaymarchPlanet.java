package _22_12.chatGPT;

import _0_utils.Utils;
import com.krab.lazy.PickerColor;
import com.krab.lazy.ShaderReloader;
import processing.core.PApplet;
import processing.core.PGraphics;
import com.krab.lazy.LazyGui;
import processing.core.PVector;
import processing.opengl.PShader;

public class RaymarchPlanet extends PApplet {
    LazyGui gui;
    PGraphics pg;

    public static void main(String[] args) {
        PApplet.main(java.lang.invoke.MethodHandles.lookup().lookupClass());
    }

    @Override
    public void settings() {
        fullScreen(P2D);
//        size(1400, 1400, P2D);
    }

    @Override
    public void setup() {
        gui = new LazyGui(this);
        pg = createGraphics(min(width, height), min(width, height), P2D);
        colorMode(HSB, 1, 1, 1, 1);
        frameRate(144);
    }

    @Override
    public void draw() {
        pg.beginDraw();
        drawBackground(g);
        applyRaymarchShader();
        pg.endDraw();
        translate(width/2f, height/2f);
        imageMode(CENTER);
        image(pg, 0, 0);
        gui.draw();
        Utils.record(this, gui);
    }

    private void applyRaymarchShader() {
        gui.pushFolder("shader");
        String path = "_22_12/ChatGPT/raymarchPlanet.glsl";
        PShader shader = ShaderReloader.getShader(path);
        float time = gui.slider("time", 0);
        gui.sliderAdd("time", radians(
                gui.slider("time +", 0.5f)
        ));
        shader.set("time", time);
        int atmoClr = gui.colorPicker("atmo", color(.75f,.75f,1.f)).hex;
        int dirtClr = gui.colorPicker("dirt", color(1.0f, 0.5f, 0.3f)).hex;
        shader.set("atmoColor", new PVector(red(atmoClr), green(atmoClr), blue(atmoClr)));
        shader.set("dirtColor", new PVector(red(dirtClr), green(dirtClr), blue(dirtClr)));
        shader.set("atmoDistance", gui.slider("atmo dist", 0.1f));
        shader.set("camPos", gui.plotXYZ("cam pos", 0, 0, -2));
        shader.set("planetRadius", gui.slider("planet radius", 0.5f));
        shader.set("rayStepSize", gui.slider("ray step size", 0.5f));
        shader.set("maxSteps", gui.sliderInt("ray max steps", 64));
        shader.set("maxDist", gui.slider("ray max dist", 5));
        shader.set("surfDist", gui.slider("ray surf dist", 0.01f));
        ShaderReloader.filter(path, pg);
        gui.popFolder();
    }

    private void drawBackground(PGraphics pg) {
        pg.fill(gui.colorPicker("background").hex);
        pg.noStroke();
        pg.rectMode(CORNER);
        pg.rect(0, 0, width, height);
    }
}

