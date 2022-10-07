package _22_03;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;
import lazy.LazyGui;

import java.util.ArrayList;
import java.util.UUID;

public class Sphere2D extends PApplet {

    PGraphics pg;
    private ArrayList<PVector> gaussPoints = new ArrayList<>();
    private LazyGui gui;

    public static void main(String[] args) {
        PApplet.main(java.lang.invoke.MethodHandles.lookup().lookupClass());
    }

    @Override
    public void settings() {
        size(1080, 1080, P2D);
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
        pg.blendMode(BLEND);
        drawBackground(pg);
        drawSphere(pg);
        pg.endDraw();
        image(pg, 0, 0);


    }

    @Override
    public void keyPressed() {
        if (key == 's') {
            saveFrame("out/screenshots/" + UUID.randomUUID() + ".png");
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
        if (gui.toggle("points/regenerate", true)) {
            gaussPoints.clear();
            float pointsOffsetX = gui.slider("points/x");
            float pointsOffsetY = gui.slider("points/y");
            float gaussRange = gui.slider("points/gauss range", 200);
            int pointCount = gui.sliderInt("points/count", 1000);
            for (int i = 0; i < pointCount; i++) {
                float gaussX = pointsOffsetX + randomGaussian() * gaussRange;
                float gaussY = pointsOffsetY + randomGaussian() * gaussRange;
                if(gui.toggle("dist culling", true) &&
                        dist(circleX, circleY, gaussX, gaussY) > radius){
                    continue;
                }
                gaussPoints.add(new PVector(gaussX, gaussY));
            }
        }
        if (gui.toggle("points/draw gauss", true)) {
            pg.stroke(gui.colorPicker("points/stroke", color(1)).hex);
            pg.strokeWeight(gui.slider("points/weight", 1));
            if(gui.stringPicker("points/blend mode", new String[]{"blend", "add"}).equals("add")){
                pg.blendMode(ADD);
            }else{
                pg.blendMode(BLEND);
            }
            for(PVector p : gaussPoints){
                pg.point(p.x, p.y);
            }
        }
    }
}

