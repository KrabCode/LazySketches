package _23_07;

import _0_utils.Utils;
import com.krab.lazy.LazyGui;
import com.krab.lazy.LazyGuiSettings;
import com.krab.lazy.utils.ArrayListBuilder;
import processing.core.PApplet;
import processing.core.PGraphics;

import java.util.ArrayList;

public class Concentric extends PApplet {
    private static final String RAY_CIRCLE = "rays";
    private static final String CIRCLE = "circle";
    private static final String SINE_CIRCLE = "sine";
    ArrayList<String> typeOptions = new ArrayListBuilder<String>()
            .add(RAY_CIRCLE)
            .add(SINE_CIRCLE)
            .build();
    LazyGui gui;
    PGraphics pg;

    public static void main(String[] args) {
        PApplet.main(java.lang.invoke.MethodHandles.lookup().lookupClass());
    }

    @Override
    public void settings() {
//        size(800, 800, P2D);
        fullScreen(P2D);
    }

    @Override
    public void setup() {
        gui = new LazyGui(this, new LazyGuiSettings().setLoadLatestSaveOnStartup(false));
        pg = createGraphics(width, height, P3D);
    }

    @Override
    public void draw() {
        pg.beginDraw();
        drawBackground();
        pg.translate(width / 2f, height / 2f);
        drawShapes();
        pg.endDraw();
        image(pg, 0, 0);
        Utils.record(this, gui);
    }

    private void drawBackground() {
        pg.fill(gui.colorPicker("background", color(36)).hex);
        pg.noStroke();
        pg.rectMode(CORNER);
        pg.rect(0, 0, width, height);
    }

    int maximumShapeCountSoFar = 1;

    private void drawShapes() {
        gui.pushFolder("shapes");
        int shapeCount = gui.sliderInt("count", 1, 1, 1000);
        maximumShapeCountSoFar = max(shapeCount, maximumShapeCountSoFar);
        float runningRotate = 0;
        for (int i = 0; i < maximumShapeCountSoFar; i++) {
            gui.pushFolder(String.valueOf(i));
            if (i >= shapeCount) {
                gui.hideCurrentFolder();
                gui.popFolder();
                continue;
            } else {
                gui.showCurrentFolder();
            }
            gui.pushFolder("style");
            int strokeClr = gui.colorPicker("stroke", color(255)).hex;
            int fillClr = gui.colorPicker("fill", color(0, 0)).hex;
            float weight = gui.slider("weight", 2);
            pg.strokeWeight(weight);
            pg.stroke(strokeClr);
            pg.fill(fillClr);
            gui.popFolder();
            gui.pushFolder("rotate");
            float rotate = gui.slider("position");
            float rotateDelta = radians(gui.slider("change", 1));
            gui.sliderAdd("position", rotateDelta);
            boolean inheritsRotation = gui.toggle("inherit", true);
            gui.popFolder();
            float radius = gui.slider("radius", 30 + i * 30);
            String type = gui.radio("type", typeOptions);
            if (inheritsRotation) {
                runningRotate += rotate;
                rotate = runningRotate;
            }
            drawCircleTemplate(type, radius, rotate);
            drawSine(type, radius, rotate);
            drawRayCircle(type, radius, rotate);
            gui.popFolder();
        }
        gui.popFolder();
    }

    private void drawCircleTemplate(String type, float radius, float rotation) {
        gui.pushFolder(CIRCLE);
        gui.showCurrentFolder();
        if (!type.equals(CIRCLE)) {
            gui.hideCurrentFolder();
            gui.popFolder();
            return;
        }
        // only ask for GUI values here
        pg.ellipse(0, 0, radius * 2, radius * 2);
        gui.popFolder();
    }

    private void drawRayCircle(String type, float radius, float rotation) {
        gui.pushFolder(RAY_CIRCLE);
        gui.showCurrentFolder();
        if (!type.equals(RAY_CIRCLE)) {
            gui.hideCurrentFolder();
            gui.popFolder();
            return;
        }
        pg.pushMatrix();
        pg.rotate(rotation);
        int rayCount = gui.sliderInt("ray count", 12);
        float length = gui.slider("length", 10);
        for (int i = 0; i < rayCount; i++) {
            pg.pushMatrix();
            float localAngle = map(i, 0, rayCount, 0, TAU);
            pg.rotate(localAngle);
            pg.line(0, radius, 0, radius + length);
            pg.popMatrix();
        }
        pg.popMatrix();
        gui.popFolder();
    }

    private void drawSine(String type, float radius, float rotation) {
        gui.pushFolder(SINE_CIRCLE);
        gui.showCurrentFolder();
        if (!type.equals(SINE_CIRCLE)) {
            gui.hideCurrentFolder();
            gui.popFolder();
            return;
        }
        float freq = gui.slider("frequency", 10);
        float amp = gui.slider("amplitude", 20);
        int detail = gui.sliderInt("detail", 360);
        pg.pushMatrix();
        pg.rotate(rotation);

        pg.beginShape();
        for (int vertex = 0; vertex < detail; vertex++) {
            float localAngle = map(vertex, 0, detail, 0, TAU);
            float localRadius = radius + amp * sin((localAngle + rotation) * freq);
            float x = localRadius * cos(localAngle);
            float y = localRadius * sin(localAngle);
            pg.vertex(x, y);
        }
        pg.endShape(CLOSE);
        gui.popFolder();
        pg.popMatrix();
    }
}
