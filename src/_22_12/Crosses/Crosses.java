package _22_12.Crosses;

import _0_utils.Shapes;
import _0_utils.Utils;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;
import lazy.LazyGui;
import processing.core.PVector;

public class Crosses extends PApplet {
    LazyGui gui;
    PGraphics pg;

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
        frameRate(144);
    }

    @Override
    public void draw() {
        pg.beginDraw();
        Shapes.drawBackground("background", gui, pg);
        drawCrosses();
        pg.endDraw();
        image(pg, 0, 0);
        gui.draw();
        Utils.record(this, gui);
    }

    private void drawCrosses() {
        pg.pushMatrix();
        pg.rectMode(CENTER);
        gui.pushFolder("crosses");
        pg.translate(width/2f, height/2f);
        int count = gui.sliderInt("x count", 12, 0, Integer.MAX_VALUE);
        float sideShort = gui.slider("rect size", 30);
        float sideLong = sideShort * 3;
        PVector fieldPos = gui.plotXY("field pos");
        pg.translate(fieldPos.x, fieldPos.y);
        pg.stroke(gui.colorPicker("stroke").hex);
        pg.strokeWeight(gui.slider("weight", 1.75f));
        pg.fill(gui.colorPicker("fill").hex);
        PVector step = gui.plotXY("step", sideShort*2, sideShort);
        PVector stepDown = step.copy().rotate(-HALF_PI);
        PVector colPos = new PVector();
        for (int xi = 0; xi < count; xi++) {
            PVector rowPos = colPos.copy();
            for(int yi = 0; yi < count; yi++){
                pg.pushMatrix();
                pg.translate(rowPos.x, rowPos.y);
                pg.rect(0,0,sideLong, sideShort);
                pg.rect(0,0,sideShort, sideLong);
                pg.popMatrix();
                rowPos.add(stepDown);
            }
            colPos.add(step);
        }
        gui.popFolder();
        pg.popMatrix();
    }

}

