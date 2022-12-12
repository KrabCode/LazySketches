package _22_12.Crosses;

import _0_utils.Utils;
import processing.core.PApplet;
import processing.core.PGraphics;
import lazy.LazyGui;
import processing.core.PVector;

public class Crosses extends PApplet {
    LazyGui gui;
    PGraphics pg;
    private float rotateFrames;

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
        frameRate(60);
    }

    @Override
    public void draw() {
        pg.beginDraw();
        rotateFrames = gui.sliderInt("crosses/rot frames", 360);
        boolean aTurn = frameCount % rotateFrames <= rotateFrames / 2;
        gui.pushFolder("colors");
        int colorA = gui.colorPicker("a", color(0xFF0F0F0F)).hex;
        int colorB = gui.colorPicker("b", color(0xFFB0B0B0)).hex;
        gui.popFolder();
        if(aTurn){
            pg.background(colorA);
            drawCrosses(true, colorB);
        }else{
            pg.background(colorB);
            drawCrosses(false, colorA);
        }
        pg.endDraw();
        image(pg, 0, 0);
        gui.draw();
        Utils.record(this, gui);
    }

    private void drawCrosses(boolean offset, int fillColor) {
        pg.pushMatrix();
        pg.rectMode(CENTER);
        gui.pushFolder("crosses");
        pg.translate(width/2f, height/2f);
        int count = gui.sliderInt("count", 20, 0, Integer.MAX_VALUE);
        float sideShort = gui.slider("rect size", 10);
        PVector stepRight = gui.plotXY("step", 1, 1);
        float sideLong = sideShort * 3;
        PVector fieldPos = gui.plotXY("field pos");
        pg.noStroke();
        pg.fill(fillColor);
        gui.popFolder();
        stepRight.x *= sideShort*4;
        stepRight.y *= sideShort*2;
        PVector stepDown = stepRight.copy().rotate(-HALF_PI);
        PVector colPos = new PVector();
        fieldPos.add(PVector.mult(stepRight, -count/2f));
        fieldPos.add(PVector.mult(stepDown, -count/2f));
        if(offset){
            fieldPos.add(PVector.div(stepDown, 2));
            fieldPos.add(PVector.div(stepRight, 2));
        }


        pg.translate(fieldPos.x, fieldPos.y);
        for (int xi = 0; xi < count; xi++) {
            PVector rowPos = colPos.copy();
            for(int yi = 0; yi < count; yi++){
                float rotation;
                if(offset){
                    rotation = constrain(norm(frameCount % rotateFrames, 0, rotateFrames/2), 0, 1);
                }else{
                    rotation = constrain(norm(frameCount % rotateFrames, rotateFrames/2, rotateFrames), 0, 1);
                }
                rotation = easeInOutQuad(rotation) * HALF_PI;
                pg.pushMatrix();
                pg.translate(rowPos.x, rowPos.y);
                pg.rotate(rotation);
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

    float easeInOutQuad(float x) {
        return x < 0.5f ? 2 * x * x : 1 - pow(-2 * x + 2, 2) / 2;

    }
}

