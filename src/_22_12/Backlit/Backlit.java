package _22_12.Backlit;

import _0_utils.Utils;
import lazy.ShaderReloader;
import processing.core.PApplet;
import processing.core.PGraphics;
import lazy.LazyGui;
import processing.opengl.PShader;

public class Backlit extends PApplet {
    LazyGui gui;
    PGraphics pg;
    String fragPath = "_22_12/backlit/backlit.glsl";

    public static void main(String[] args) {
        PApplet.main(java.lang.invoke.MethodHandles.lookup().lookupClass());
    }

    @Override
    public void settings() {
        fullScreen(P2D, 1);
    }

    @Override
    public void setup() {
        gui = new LazyGui(this);
        gui.toggleSet("options/saves/autosave on exit", true);
        pg = createGraphics(width, height, P2D);
        colorMode(HSB,1,1,1,1);
        ShaderReloader.getShader(fragPath);
        frameRate(144);
    }

    @Override
    public void draw() {

        pg.beginDraw();
        drawBackground();
        int shaderPasses = gui.sliderInt("shader/passes", 4, 0, Integer.MAX_VALUE);
        for (int i = 0; i < shaderPasses; i++) {
            float iNorm = norm(i, 0, shaderPasses-1);
            drawFragShader(iNorm);
        }
        drawGuiStats();
        pg.endDraw();
        image(pg, 0, 0);
        gui.draw();
        Utils.record(this, gui);
    }

    private void drawGuiStats() {
        gui.pushFolder("stats");
        gui.sliderIntSet("frameCount", frameCount);
        gui.sliderSet("rad(frameCount)", radians(frameCount));
        Utils.updateGetFrameRateAverage(this, gui);
        gui.popFolder();
    }

    private void drawFragShader(float iNorm) {
        gui.pushFolder("shader");
        PShader shader = ShaderReloader.getShader(fragPath);
        float shaderTime = gui.slider("time");
        float timeSpeed = radians(gui.slider("time +", 1));
        gui.sliderSet("time", shaderTime + timeSpeed);
        shader.set("time", shaderTime);
        shader.set("layer", iNorm);
        shader.set("blurOpaque", gui.slider("blur opaque", 3));
        shader.set("blurShine", gui.slider("blur shine", 40));
        shader.set("layerFreq", gui.slider("layer freq", 0.1f));
        shader.set("layerAmp",  gui.slider("layer amp", 1));
        shader.set("layerPosXY", gui.plotXY("layer pos", 0.1f, -0.15f));
        shader.set("rectPos", gui.plotXY("rect pos", 0, 0));
        shader.set("rectSize", gui.plotXY("rect size", 0.3f, 0.05f));
        int glow = gui.colorPicker("glow fill").hex;
        int rect = gui.colorPicker("rect fill").hex;
        shader.set("glowColor",red(glow),green(glow),blue(glow));
        shader.set("rectColor",red(rect),green(rect),blue(rect));
        ShaderReloader.filter(fragPath, pg);
        gui.popFolder();
    }

    private void drawBackground() {
        pg.fill(gui.colorPicker("background").hex);
        pg.noStroke();
        pg.rectMode(CORNER);
        pg.rect(0, 0, width, height);
    }
}

