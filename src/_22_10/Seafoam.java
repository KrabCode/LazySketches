package _22_10;

import _0_utils.Utils;
import lazy.LazyGui;
import processing.core.PApplet;
import processing.core.PGraphics;

public class Seafoam extends PApplet {
    LazyGui gui;
    PGraphics pg;

    public static void main(String[] args) {
        PApplet.main(java.lang.invoke.MethodHandles.lookup().lookupClass());
    }

    @Override
    public void settings() {
        fullScreen(P2D);
    }

    @Override
    public void setup(){
        Utils.initSurface(this, false);
        gui = new LazyGui(this);
        pg = createGraphics(width, height);
    }

    @Override
    public void draw() {
        pg.beginDraw();
        pg.background(gui.colorPicker("bg").hex);
        pg.endDraw();
        image(pg, 0,0);
        Utils.record(this, gui);
    }
}
