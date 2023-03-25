package _22_12.Shader;

import _0_utils.Utils;
import com.krab.lazy.ShaderReloader;
import processing.core.PApplet;
import processing.core.PGraphics;
import com.krab.lazy.LazyGui;
import processing.opengl.PShader;

public class LogPolar extends PApplet {
    LazyGui gui;
    PGraphics pg;
    private float scalingTime, rotationTime;

    public static void main(String[] args) {
        PApplet.main(java.lang.invoke.MethodHandles.lookup().lookupClass());
    }

    @Override
    public void settings() {
//        size(1080, 1080, P2D);
        fullScreen(P2D);
    }

    @Override
    public void setup() {
        gui = new LazyGui(this);
        pg = createGraphics(width, height, P2D);
        colorMode(HSB, 1, 1, 1, 1);
//        frameRate(144);
    }

    @Override
    public void draw() {
        pg.beginDraw();
        drawBackground();
        drawShader();
        gui.popFolder();
        pg.endDraw();
        image(pg, 0, 0);
        gui.draw();
        Utils.record(this, gui);
    }

    private void drawShader() {
        gui.pushFolder("shader");
        String shaderPath = gui.text("path", "_22_12\\Logpolar\\logpolar_2D.glsl");
        rotationTime += radians(gui.slider("rotation +", 0.01f));
        scalingTime += radians(gui.slider("scale +", 0.01f));
        boolean swapDirections = gui.toggle("swap dirs");
        PShader s = ShaderReloader.getShader(shaderPath);
        if(s != null){
            s.set("rotationTime", rotationTime);
            s.set("swapDirs", swapDirections);
            s.set("scalingTime", scalingTime);
            s.set("copies", gui.sliderInt("copies", 8));
        }

        ShaderReloader.filter(shaderPath, pg);
    }

    private void drawBackground() {
        pg.fill(gui.colorPicker("background").hex);
        pg.noStroke();
        pg.rectMode(CORNER);
        pg.rect(0, 0, width, height);
    }
}

