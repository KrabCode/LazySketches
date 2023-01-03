package _22_12.RadialLines;

import _0_utils.Utils;
import lazy.LazyGui;
import processing.core.PApplet;
import processing.core.PGraphics;

public class RadialLines extends PApplet {
    LazyGui gui;
    PGraphics pg;
    float t;

    public static void main(String[] args) {
        PApplet.main(java.lang.invoke.MethodHandles.lookup().lookupClass());
    }

    @Override
    public void settings() {
//        size(1000, 1000, P2D);
        fullScreen(P2D);
        smooth(16);
    }

    @Override
    public void setup() {
        gui = new LazyGui(this);
        pg = createGraphics(width, height, P2D);
        pg.smooth(8);
    }

    @Override
    public void draw() {
        t += radians(gui.slider("time +", 30));
        pg.beginDraw();
        drawBackground();
        pg.translate(width / 2f, height / 2f);
        drawCircle(pg);
        drawLines(pg);
        pg.endDraw();
        image(pg, 0, 0);
        Utils.record(this, gui);
    }

    private void drawCircle(PGraphics pg) {
        gui.pushFolder("circle");
        pg.fill(gui.colorPicker("fill", color(50)).hex);
        pg.stroke(gui.colorPicker("stroke", color(255)).hex);
        pg.strokeWeight(gui.slider("weight", 1.99f));
        float diam = gui.slider("diameter", 250);
        pg.ellipse(0, 0, diam, diam);
        gui.popFolder();
    }

    private void drawLines(PGraphics pg) {
        gui.pushFolder("lines");
        int lineCount = gui.sliderInt("group count", 4);
        for (int lineIndex = 0; lineIndex < lineCount; lineIndex++) {
            gui.pushFolder("group " + lineIndex);
            if(!gui.toggle("active")){
                gui.popFolder();
                continue;
            }
            pg.pushMatrix();
            float groupRotOffset = gui.slider("rot offset") * HALF_PI;
            pg.rotate(groupRotOffset);
            int count = gui.sliderInt("count", 4);
            float length = gui.slider("length", 300) * 0.5f;
            pg.stroke(gui.colorPicker("stroke", color(255)).hex);
            pg.strokeWeight(gui.slider("weight", 1.99f));
            float segLength = gui.slider("segment length", 20, 1, 1000);
            float gapLength = gui.slider("gap length", 5, 1, 1000);
            float timeSpeed = gui.slider("time +", 1);
            for (int i = 0; i < count; i++) {
                pg.pushMatrix();
                float theta = map(i, 0, count, 0, PI);
                pg.rotate(theta);
                if (segLength >= length) {
                    pg.line(-length, 0, length, 0);
                } else {
                    float dir = (i % 2 == 0 ? -1 : 1) * timeSpeed;
                    float timeOffset = map(i,0, count, 0,  (segLength + gapLength));
                    timeOffset += dir * (t % (segLength + gapLength));
                    float x = -length * 2 + timeOffset;
                    while (x < length) {
                        float x0 = constrain(x, -length, length);
                        float x1 = constrain(x + segLength, -length, length);
                        if(x0 != x1){
                            pg.line(x0, 0, x1, 0);
                        }
                        x += segLength + gapLength;
                    }
                }
                pg.popMatrix();
            }
            pg.popMatrix();
            gui.popFolder();
        }
        gui.popFolder();
    }

    private void drawBackground() {
        pg.fill(gui.colorPicker("background").hex);
        pg.noStroke();
        pg.rectMode(CORNER);
        pg.rect(0, 0, width, height);
    }
}
