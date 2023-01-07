package _23_01.recursion;

import _0_utils.Utils;
import processing.core.PApplet;
import processing.core.PGraphics;
import lazy.LazyGui;

public class Recursion extends PApplet {
    LazyGui gui;
    PGraphics pg;

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
        gui.pushFolder("rects");
        drawRecursiveRects(0, width-1);
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

    private void drawRecursiveRects(float y, float w) {
        if(w < 0.1f || w > width * 2){
            return;
        }
        pg.stroke(gui.colorPicker("stroke").hex);
        pg.strokeWeight(gui.slider("weight", 1));
        pg.fill(lerpColor(gui.colorPicker("a").hex, gui.colorPicker("b").hex, norm(w, 0, width)));
        float h = w / 4f;
        pg.rectMode(CORNER);
        pg.rect(pg.width/2f-w/2f, y, w, h);
        float widthChange = gui.slider("change", 0.5f, 0.1f, 0.9f);
        drawRecursiveRects(y + h, w * widthChange);
    }
}

