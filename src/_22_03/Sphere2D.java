package _22_03;

import processing.core.PApplet;
import processing.core.PGraphics;
import toolbox.Gui;

public class Sphere2D extends PApplet {

    Gui gui;
    PGraphics pg;

    public static void main(String[] args) {
        PApplet.main(java.lang.invoke.MethodHandles.lookup().lookupClass());
    }

    @Override
    public void settings() {
        size(1080, 1080, P2D);
    }

    @Override
    public void setup() {
        gui = new Gui(this);
        pg = createGraphics(width, height, P2D);
        colorMode(HSB,1,1,1,1);
    }

    @Override
    public void draw() {
        pg.beginDraw();
        drawBackground(pg);
        drawSphere(pg);
        pg.endDraw();
        image(pg, 0, 0);
        gui.palettePicker();
        gui.draw();

    }

    @Override
    public void keyPressed() {
        if (key == 's') {
            saveFrame("out/screenshot_####.jpg");
        }
    }

    private void drawBackground(PGraphics pg) {
        pg.fill(gui.colorPicker("background").hex);
        pg.noStroke();
        pg.rectMode(CORNER);
        pg.rect(0, 0, width, height);
    }

    private void drawSphere(PGraphics pg) {
        float circleX = gui.slider("circle/x");
        float circleY = gui.slider("circle/y");
        float radius = gui.slider("circle/radius", 350);
        float diameter = radius * 2;
        pg.strokeWeight(gui.slider("circle/weight", 1));
        pg.stroke(gui.colorPicker("circle/stroke").hex);
        pg.fill(gui.colorPicker("circle/fill", color(0, 0)).hex);
        pg.translate(pg.width / 2f, pg.height / 2f);
        pg.circle(circleX, circleY, diameter);
        if (gui.toggle("points/do gauss", true)) {
            float pointsOffsetX = gui.slider("points/x");
            float pointsOffsetY = gui.slider("points/y");
            float gaussRange = gui.slider("points/gauss range", 200);
            int pointCount = gui.sliderInt("points/count", 1000);
            pg.stroke(gui.colorPicker("points/stroke", color(1)).hex);
            pg.strokeWeight(gui.slider("points/weight", 1));
            for (int i = 0; i < pointCount; i++) {
                float gaussX = randomGaussian() * gaussRange;
                float gaussY = randomGaussian() * gaussRange;
                pg.point(pointsOffsetX + gaussX, pointsOffsetY + gaussY);
            }
        }
    }
}

