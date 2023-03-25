package _22_11;

import com.krab.lazy.LazyGui;
import processing.core.PApplet;
import processing.core.PGraphics;

@SuppressWarnings("DuplicatedCode")
public class Aperture extends PApplet {

    PGraphics pg;
    LazyGui gui;

    public static void main(String[] args) {
        PApplet.main(java.lang.invoke.MethodHandles.lookup().lookupClass());
    }

    public void settings() {
        fullScreen(P2D);
    }

    public void setup() {
        gui = new LazyGui(this);
        pg = createGraphics(width, height, P2D);
        pg.smooth(16);
    }

    public void draw() {
        pg.beginDraw();
        pg.background(gui.colorPicker("background", color(255*0.15f)).hex);
        pg.translate(width/2f, height/2f);
        drawLines();
        pg.endDraw();
        image(pg, 0, 0);
    }

    private void drawLines() {
        String p = "lines/";
        pg.strokeWeight(gui.slider(p + "weight", 1.99f));
        int detail = gui.sliderInt(p + "count", 24);
        pg.noFill();
        float thetaOffsetA = gui.slider(p + "angle a");
        float thetaOffsetB = gui.slider(p + "angle b");
        gui.sliderSet(p + "angle a", thetaOffsetA + radians(gui.slider(p + "angle a +")));
        gui.sliderSet(p + "angle b", thetaOffsetB + radians(gui.slider(p + "angle b +")));
        pg.beginShape(LINES);
        for (int i = 0; i < detail; i++) {
            float theta = map(i, 0, detail, 0, TAU);
            float thetaA = theta + thetaOffsetA;
            float thetaB = theta + thetaOffsetB;
            float radiusA = gui.slider(p + "radius a", 100);
            float radiusB = gui.slider(p + "radius b", 250);
            float aX = radiusA * cos(thetaA);
            float aY = radiusA * sin(thetaA);
            float bX = radiusB * cos(thetaB);
            float bY = radiusB * sin(thetaB);
            pg.stroke(gui.colorPicker(p + "stroke a", color(255)).hex);
            pg.vertex(aX, aY);
            pg.stroke(gui.colorPicker(p + "stroke b", color(255)).hex);
            pg.vertex(bX, bY);
        }
        pg.endShape();
    }
}
