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
    public void setup() {
        Utils.initSurface(this, false);
        gui = new LazyGui(this);
        pg = createGraphics(width, height, P2D);
        pg.beginDraw();
        pg.background(0);
        pg.endDraw();
    }

    @Override
    public void draw() {
        pg.beginDraw();
        drawBackground();
        pg.endDraw();
        Utils.shaderMove(pg, gui);
        image(pg, 0, 0);
        Utils.record(this, gui);
    }

    private void drawBackground() {
        if(gui.toggle("bg/draw gradient")){
            pg.image(gui.gradient("bg/gradient"), 0, 0);
        }else{
            pg.blendMode(gui.radio("bg/blend mode", new String[]{"blend", "sub"}).equals("blend") ? BLEND : SUBTRACT);
            pg.fill(gui.colorPicker("bg/color").hex);
            pg.noStroke();
            pg.rect(0, 0, width, height);
        }
        if(gui.toggle("bg/draw point")){
            pg.strokeWeight(gui.slider("bg/point/weight", 15));
            pg.stroke(gui.colorPicker("stroke color").hex);
            pg.point(width/2f, height/2f);
        }
    }
}
