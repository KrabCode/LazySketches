package _23_03;

import _0_utils.Utils;
import processing.core.PApplet;
import processing.core.PGraphics;
import lazy.LazyGui;
import processing.core.PVector;

public class GuiShowcase extends PApplet {
    LazyGui gui;
    PGraphics pg;

    public static void main(String[] args) {
        PApplet.main(java.lang.invoke.MethodHandles.lookup().lookupClass());
    }

    @Override
    public void settings() {
        size(1200, 1200, P2D);
    }

    @Override
    public void setup() {
        gui = new LazyGui(this);
        pg = createGraphics(width, height, P2D);
        colorMode(HSB, 1, 1, 1, 1);
    }

    @Override
    public void draw() {
        pg.beginDraw();
        drawBackground();
        drawRectangles();
        pg.endDraw();
        image(pg, 0, 0);
        gui.draw();
        Utils.updateGetFrameRateAverage(this, gui, 60);
        Utils.record(this, gui);
    }

    private void drawBackground() {
        pg.fill(gui.colorPicker("background").hex);
        pg.noStroke();
        pg.rectMode(CORNER);
        pg.rect(0, 0, width, height);
    }

    void drawRectangles() {
        gui.pushFolder("rects");
        int maxRectCount = 20;
        int rectCount = gui.sliderInt("count", 10, 0, maxRectCount);
        PVector basePos = gui.plotXY("base pos", 500);
        PVector baseSize = gui.plotXY("base size", 8);
        PVector iPos = gui.plotXY("i pos", 22, 0);
        PVector iSize = gui.plotXY("i size", 5);
        pg.rectMode(gui.radio("rect mode", new String[]{"corner", "center"}).equals("corner") ? CORNER : CENTER);
        for (int i = 0; i < maxRectCount; i++) {
            // make a dynamic list of rects each with its own folder
            gui.pushFolder("#" + i);

            if (i < rectCount) {
                // show the current folder in case it was hidden
                gui.showCurrentFolder();
            } else {
                // this rect is over the rectCount limit, so we hide its folder and skip drawing it
                gui.hideCurrentFolder();
                // shouldn't forget to pop out of the folder before 'continue'
                gui.popFolder();
                continue;
            }
            PVector pos = gui.plotXY("pos").add(basePos.x + i * iPos.x, basePos.y + i * iPos.y);
            PVector size = gui.plotXY("size", 0).add(baseSize.x + iSize.x * i, baseSize.y + iSize.y * i);
            pg.fill(gui.colorPicker("fill", color(1)).hex);
            pg.noStroke();
            pg.rect(pos.x, pos.y, size.x, size.y);
            gui.popFolder();
        }
        gui.popFolder();
    }
}

