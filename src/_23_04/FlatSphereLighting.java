package _23_04;

import _0_utils.Utils;
import com.krab.lazy.LazyGui;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;


public class FlatSphereLighting extends PApplet {
    LazyGui gui;
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
        gui = new LazyGui(this);
        pg = createGraphics(width, height, P2D);
        colorMode(HSB, 1, 1, 1, 1);
        frameRate(144);
    }

    @Override
    public void draw() {
        pg.beginDraw();
        drawBackground();
        drawForeground();
        pg.endDraw();
        image(pg, 0, 0);
        gui.draw();
        Utils.record(this, gui);
    }

    private void drawForeground() {
        PVector planetPos = gui.plotXY("planet pos", width / 2f, height / 2f);
        float planetRadius = gui.slider("planet radius", 200);
        PVector lightPos = gui.plotXY("light pos (relative)", 0, 0);
        float lightReach = gui.slider("light reach", 400);
        int concentricCircleCount = gui.sliderInt("circle count", 10);
        int vertexCountPerCircle = gui.sliderInt("circle detail", 360);
        pg.pushMatrix();
        pg.translate(planetPos.x, planetPos.y);
        pg.noStroke();
        pg.fill(gui.colorPicker("planet color", color(1)).hex);
        pg.ellipse(0, 0, planetRadius*2, planetRadius*2);
        pg.noFill();
        pg.strokeWeight(gui.slider("circle stroke weight"));
        int circleLit = gui.colorPicker("circle lit", color(0)).hex;
        int circleDark = gui.colorPicker("circle dark", color(0)).hex;
        for (int circleIndex = 0; circleIndex < concentricCircleCount; circleIndex++) {
            float circleIndexNorm = norm(circleIndex, 0, concentricCircleCount - 1);
            float circleRadius = circleIndexNorm * planetRadius;
            pg.beginShape();
            for (int vertexIndex = 0; vertexIndex < vertexCountPerCircle; vertexIndex++) {
                float vertexTheta = map(vertexIndex, 0, vertexCountPerCircle, 0, TAU);
                float x = circleRadius * cos(vertexTheta);
                float y = circleRadius * sin(vertexTheta);
                float distanceFromVertexToLight = dist(x, y, lightPos.x, lightPos.y);
                float darknessNorm = constrain(norm(distanceFromVertexToLight, 0, lightReach), 0, 1);
                pg.stroke(lerpColor(circleLit, circleDark, darknessNorm));
                pg.vertex(x, y);
            }
            pg.endShape(CLOSE);
        }
        pg.popMatrix();
    }

    private void drawBackground() {
        pg.fill(gui.colorPicker("background").hex);
        pg.noStroke();
        pg.rectMode(CORNER);
        pg.rect(0, 0, width, height);
    }
}

