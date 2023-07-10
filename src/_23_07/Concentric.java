package _23_07;

import _0_utils.Utils;
import com.krab.lazy.LazyGui;
import processing.core.PApplet;
import processing.core.PGraphics;

public class Concentric extends PApplet{
    LazyGui gui;
    PGraphics pg;

    public static void main(String[] args) {
        PApplet.main(java.lang.invoke.MethodHandles.lookup().lookupClass());
    }

    @Override
    public void settings() {
        size(800,800,P2D);
    }

    @Override
    public void setup() {
        gui = new LazyGui(this);
        pg = createGraphics(width, height, P3D);
    }

    @Override
    public void draw() {
        pg.beginDraw();
        drawBackground();
        drawConcentric();
        pg.endDraw();
        image(pg, 0, 0);
        Utils.record(this, gui);
    }

    private void drawBackground() {
        pg.fill(gui.colorPicker("background", color(36)).hex);
        pg.noStroke();
        pg.rectMode(CORNER);
        pg.rect(0,0,width,height);
    }

    private void drawConcentric() {
        pg.translate(width/2f, height/2f);
        drawLines();
    }

    private void drawLines() {
        gui.pushFolder("ray lines");
        int count = gui.sliderInt("count", 1);
        float runningRotate = 0;
        for (int i = 0; i < count; i++) {
            gui.pushFolder("ray " + i);
            float lineStart = gui.slider("start", i * 30);
            float lineLength = gui.slider("length", 10);
            int rayCount =  gui.sliderInt("ray count", 8);
            float rotate = gui.slider("rotate");
            float rotateDelta = radians(gui.slider("rotate +", 1));
            gui.sliderAdd("rotate", rotateDelta);
            int strokeClr = gui.colorPicker("stroke", color(255)).hex;
            float weight = gui.slider("weight", 2);
            boolean rotatesIndependently = gui.toggle("independent");
            if(!rotatesIndependently){
                runningRotate += rotate;
                rotate = runningRotate;
            }
            for(int ray = 0; ray < rayCount; ray++){
                float angle = map(ray, 0, rayCount, 0, TAU);
                pg.pushMatrix();
                pg.strokeWeight(weight);
                pg.stroke(strokeClr);
                pg.rotate(angle + rotate);
                pg.line(lineStart, 0, lineStart + lineLength, 0);
                pg.popMatrix();
            }
            gui.popFolder();
        }
        gui.popFolder();
    }
}
