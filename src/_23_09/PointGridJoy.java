package _23_09;

import _0_utils.OpenSimplexNoise;
import _0_utils.Utils;
import com.krab.lazy.LazyGui;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;

public class PointGridJoy extends PApplet {
    LazyGui gui;
    PGraphics pg;
    OpenSimplexNoise noise = new OpenSimplexNoise();

    public static void main(String[] args) {
        PApplet.main(java.lang.invoke.MethodHandles.lookup().lookupClass());
    }

    @Override
    public void settings() {
        size(800, 800, P2D);
        smooth(4);
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
        drawPoints();
        pg.endDraw();
        image(pg, 0, 0);
        Utils.record(this, gui);
    }

    private void drawPoints() {
        gui.pushFolder("point grid");
        float step = gui.slider("step", 20);
        PVector offset = gui.plotXY("offset", step / 2f);
        PVector margin = gui.plotXY("margin");
        pg.noStroke();
        pg.fill(gui.colorPicker("color", color(1)).hex);
        float radius = gui.slider("weight", 1.9f);
        float timeLinear = radians(frameCount) * (gui.slider("time speed", 1));
        pg.translate(width/2f, height/2f);
        for (float x = -height/2f -margin.x + offset.x; x < width/2f + margin.x; x += step) {
            for (float y = -height/2f -margin.y + offset.y; y < height/2f + margin.y; y += step) {
                float dist = dist(x,y,0,0);
                float rotateSpeed = gui.slider("rotate amp") + sin(radians(dist * gui.slider("rotate freq")));
                pg.pushMatrix();
                pg.rotate(rotateSpeed * timeLinear);
                pg.ellipse(x, y, radius, radius);
                pg.popMatrix();
            }
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
