package _0_templates_java;

import _0_utils.Utils;
import lazy.LazyGui;
import lazy.ShaderReloader;
import processing.core.PApplet;
import processing.core.PGraphics;

public class TemplateFragShader extends PApplet {
    LazyGui gui;
    PGraphics pg;
    float t;

    public static void main(String[] args) {
        PApplet.main(java.lang.invoke.MethodHandles.lookup().lookupClass());
    }

    @Override
    public void settings() {
        size(1080, 1080, P2D);
    }

    @Override
    public void setup() {
        gui = new LazyGui(this);
        pg = createGraphics(width, height, P2D);
        colorMode(HSB, 1, 1, 1, 1);
        frameRate(144);
    }

    @Override
    public void draw() {
        pg.beginDraw();
        drawBackground();
        gui.pushFolder("shader");
        String shaderPath = gui.textInput("path", "_0_templates_glsl\\template.glsl");
        t += radians(gui.slider("shader time +", 1));
        ShaderReloader.getShader(shaderPath).set("time", t);
        ShaderReloader.filter(shaderPath, pg);
        gui.popFolder();
        pg.endDraw();
        image(pg, 0, 0);
        gui.draw();
        Utils.record(this, gui);
    }

    private void drawBackground() {
        pg.fill(gui.colorPicker("background").hex);
        pg.noStroke();
        pg.rectMode(CORNER);
        pg.rect(0, 0, width, height);
    }
}

