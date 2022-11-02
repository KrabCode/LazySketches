package _22_11;

import _0_utils.Utils;
import processing.core.PApplet;
import processing.core.PGraphics;
import lazy.LazyGui;

public class Test extends PApplet {
    LazyGui gui;
    PGraphics pg;

    public static void main(String[] args) {
        PApplet.main(java.lang.invoke.MethodHandles.lookup().lookupClass());
    }

    @Override
    public void settings() {
        fullScreen(P2D);
//        size(1080, 1080, P2D);
    }

    @Override
    public void setup() {
        gui = new LazyGui(this);
        pg = createGraphics(width, height, P2D);
    }

    @Override
    public void draw() {
        pg.beginDraw();
        drawBackground();
        pg.endDraw();
        image(pg, 0, 0);
        image(gui.getGuiCanvas(), 0, 0);
        pushMatrix();
        translate(mouseX, mouseY);
        stroke(gui.colorPicker("mouse/stroke").hex);
        fill(gui.colorPicker("mouse/fill").hex);
        strokeWeight(gui.slider("mouse/weight"));
        triangle(0, 0,
                gui.slider("mouse/aX"), gui.slider("mouse/aY"),
                gui.slider("mouse/bX"), gui.slider("mouse/bY")
        );
        popMatrix();
        Utils.record(this, gui);
    }

    private void drawBackground() {
        clear();
        pg.clear();
        pg.fill(gui.colorPicker("background").hex);
        pg.noStroke();
        pg.rectMode(CORNER);
        pg.rect(0, 0, width, height);
    }
}

