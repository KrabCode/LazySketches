package _22_12.Backlit;

import _0_utils.Utils;
import lazy.ShaderReloader;
import processing.core.PApplet;
import processing.core.PGraphics;
import lazy.LazyGui;
import processing.opengl.PShader;

import java.util.ArrayList;

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
            float iNormNext = norm(i+1, 0, shaderPasses-1);
            drawFragShader(iNorm, iNormNext);
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
        updateGetFrameRateAverage();
        gui.popFolder();
    }

    ArrayList<Float> frameRateHistory = new ArrayList<>();
    int frameRateTarget = 144;

    public void updateGetFrameRateAverage() {
        int frameRateStackSize = gui.sliderInt("framesToAverage", 256);
        frameRateHistory.add(frameRate);
        if(frameRateHistory.size() > frameRateStackSize) {
            frameRateHistory.remove(0);
        }
        float frameRateAverage = 0;
        if(!frameRateHistory.isEmpty()){
            float sum = 0;
            for (float n : frameRateHistory) {
                sum += n;
            }
            frameRateAverage = sum / frameRateHistory.size();
        }
        gui.sliderSet("frameRate avg", frameRateAverage);
        int frameRateTargetTemp = gui.sliderInt("frameRate target", frameRateTarget);
        if(frameRateTargetTemp != frameRateTarget){
            frameRate(frameRateTarget);
        }
        frameRateTarget = frameRateTargetTemp;
    }


    private void drawFragShader(float iNorm, float iNormNext) {
        gui.pushFolder("shader");
        PShader shader = ShaderReloader.getShader(fragPath);
        float shaderTime = gui.slider("time");
        float timeSpeed = radians(gui.slider("time +", 1));
        gui.sliderSet("time", shaderTime + timeSpeed);
        shader.set("time", shaderTime);
        shader.set("layer", iNorm);
        shader.set("layerNext", iNormNext);
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

