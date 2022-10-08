package _22_10;

import _0_utils.Utils;
import lazy.LazyGui;
import processing.core.PApplet;
import processing.core.PGraphics;

public class Seafoam extends PApplet {
    LazyGui gui;
    PGraphics pg;
    float t = 0;

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
        t += radians(gui.sliderInt("speed", 1));
        pg.noStroke();
        pg.fill(gui.colorPicker("circle/fill").hex);
        float r = gui.slider("circle/radius", 250);
        float s = gui.slider("circle/size", 50);
        pg.translate(width/2f, height/2f);
        pg.ellipse(r*cos(t), r*sin(t), s, s);
        pg.endDraw();
        image(pg, 0,0);
        Utils.record(this, gui);
    }
}
