package _22_03;


import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;
import shaders.GeneralPurposeShaders;
import toolbox.Gui;

import java.util.ArrayList;

public class Test extends PApplet {
    private Gui gui;
    private PGraphics pg;
    private float angleTime;
    private float radiusTime;
    ArrayList<PVector> lastPositions = new ArrayList<>();

    public static void main(String[] args) {
        PApplet.main(java.lang.invoke.MethodHandles.lookup().lookupClass());
    }

    public void settings() {
//        size(800, 800, P2D);
        fullScreen(P2D);
    }

    public void setup() {
        gui = new Gui(this);
        pg = createGraphics(width, height, P2D);
    }

    public void draw() {
        pg.beginDraw();
        pg.noStroke();
        drawScene();
        pg.endDraw();
        clear();
        image(pg, 0, 0);
        gui.draw();
    }


    private void drawScene() {
        pg.pushMatrix();
        pg.translate(width/2f, height/2f);
        GeneralPurposeShaders.applyShaders("shaders", pg);
        int count = gui.sliderInt("flower/count", 10);
        float middleRadius = gui.slider("flower/middle radius", 100);
        float offsetRadius = gui.slider("flower/offset radius", 250);
        pg.strokeWeight(gui.slider("flower/style/weight", 2));
        pg.stroke(gui.colorPicker("flower/style/stroke", color(255)).hex);
        angleTime += radians(gui.slider("flower/angle speed")); ;
        radiusTime += radians(gui.slider("flower/radius speed"));
        for (int i = 0; i < count; i++) {
            float iNorm = norm(i, 0, count);
            float theta = iNorm * TAU * gui.slider("flower/angle", 1) + angleTime;
            float r = (float) (middleRadius + offsetRadius * SimplexNoise.noise(i, radiusTime));
            float x = r * cos(theta);
            float y = r * sin(theta);
            if(lastPositions.size() - 1 < i){
                lastPositions.add(new PVector(x,y));
            }
            PVector lastPos = lastPositions.get(i);
            pg.line(x,y, lastPos.x, lastPos.y);
            lastPositions.get(i).x = x;
            lastPositions.get(i).y = y;
        }
        pg.resetShader();
        GeneralPurposeShaders.applyFilters("filters", pg);
        pg.popMatrix();
    }
}