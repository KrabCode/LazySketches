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
        int count = gui.sliderInt("count", 20, 0, Integer.MAX_VALUE);
        float sideShort = gui.slider("rect size", 10);
        float sideLong = sideShort * 3;
        PVector fieldPos = gui.plotXY("field pos");
        pg.stroke(gui.colorPicker("stroke").hex);
        pg.strokeWeight(gui.slider("weight", 1.75f));
        pg.fill(gui.colorPicker("fill", color(0xFFB0B0B0)).hex);
        PVector stepRight = gui.plotXY("step", 1, 1);
        stepRight.x *= sideShort*4;
        stepRight.y *= sideShort*2;
        PVector stepDown = stepRight.copy().rotate(-HALF_PI);
        PVector colPos = new PVector();
        fieldPos.add(PVector.mult(stepRight, -count/2f));
        fieldPos.add(PVector.mult(stepDown, -count/2f));
        pg.translate(fieldPos.x, fieldPos.y);
        for (int xi = 0; xi < count; xi++) {
            PVector rowPos = colPos.copy();
            for(int yi = 0; yi < count; yi++){
                pg.pushMatrix();
                pg.translate(rowPos.x, rowPos.y);
                pg.beginShape();
                pg.vertex(sideShort, sideLong);
                pg.vertex(sideShort, sideShort);
                pg.vertex(sideLong, sideShort);
                pg.vertex(sideLong, -sideShort);
                pg.vertex(sideShort, -sideShort);
                pg.vertex(sideShort, -sideLong);
                pg.vertex(-sideShort, -sideLong);
                pg.vertex(-sideShort, -sideShort);
                pg.vertex(-sideLong, -sideShort);
                pg.vertex(-sideLong, sideShort);
                pg.vertex(-sideShort, sideShort);
                pg.vertex(-sideShort, sideLong);
                pg.endShape(CLOSE);
                pg.popMatrix();
                rowPos.add(stepDown);
            }
            colPos.add(stepRight);
        }
        gui.popFolder();
        pg.popMatrix();
    }

}

