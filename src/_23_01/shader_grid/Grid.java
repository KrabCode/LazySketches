package _23_01.shader_grid;

import _0_utils.Utils;
import com.krab.lazy.ShaderReloader;
import processing.core.PApplet;
import processing.core.PGraphics;
import com.krab.lazy.LazyGui;

public class Grid extends PApplet {
    LazyGui gui;
    PGraphics pg;
    float time = 0;

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
        gui.pushFolder("grid");
        String path = "_23_01\\grid\\grid.glsl";
        time += radians(gui.slider("time ++", 1));
        ShaderReloader.getShader(path).set("time", time);
        ShaderReloader.filter(path, pg);
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

