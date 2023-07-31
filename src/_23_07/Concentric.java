package _23_07;

import _0_utils.Utils;
import _22_03.PostFxAdapter;
import com.krab.lazy.LazyGui;
import com.krab.lazy.LazyGuiSettings;
import com.krab.lazy.utils.ArrayListBuilder;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;

import java.util.ArrayList;

public class Concentric extends PApplet {
    private static final String RAY_CIRCLE = "rays";
    private static final String CIRCLE = "circle";
    private static final String SINE_CIRCLE = "sine";
    private static final String ARC = "arc";
    ArrayList<String> typeOptions = new ArrayListBuilder<String>()
            .add(RAY_CIRCLE)
            .add(SINE_CIRCLE)
            .add(ARC)
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
        smooth(8);
    }

    @Override
    public void setup() {
        gui = new LazyGui(this, new LazyGuiSettings()
                .setLoadLatestSaveOnStartup(true)
        );
        pg = createGraphics(width, height, P3D);
    }

    @Override
    public void draw() {
        pg.beginDraw();
        drawBackground();
        pg.translate(width / 2f, height / 2f);
        pg.scale(gui.slider("scale", 1));
        drawShapes();
        pg.endDraw();
        PostFxAdapter.apply(this, gui, pg);
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
            float rotateAngle = gui.slider("rotate");
            float rotateDelta = radians(gui.slider("rotate +", 1));
            gui.sliderAdd("rotate", rotateDelta);
            float radius = gui.slider("radius", 30 + i * 30);
            String type = gui.radio("type", typeOptions);
            drawCircleTemplate(type, radius, rotateAngle);
            drawSine(type, radius, rotateAngle);
            drawRayCircle(type, radius, rotateAngle);
            drawArcs(type, radius, rotateAngle);
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
            float localRadius = radius + amp * sin((localAngle) * freq);
            float x = localRadius * cos(localAngle);
            float y = localRadius * sin(localAngle);
            pg.vertex(x, y);
        }
        pg.endShape(CLOSE);
        gui.popFolder();
        pg.popMatrix();
    }


    private void drawArcs(String type, float radius, float rotation) {
        gui.pushFolder(ARC);
        gui.showCurrentFolder();
        if (!type.equals(ARC)) {
            gui.hideCurrentFolder();
            gui.popFolder();
            return;
        }
        // only ask for GUI values here
        float extent = gui.slider("extent", 0.5f);
//        pg.rotate(rotation);
//        pg.arc(0, 0, radius * 2, radius * 2, 0, extent * TAU);
        if (extent <= 0) {
            gui.popFolder();
            return;
        }
        pg.pushMatrix();
        pg.rotate(rotation);
        pg.beginShape(TRIANGLE_STRIP);
        int detail = gui.sliderInt("detail", 360);
        float thickness = gui.slider("thickness", 20);
        float thicknessHalf = thickness * 0.5f;
        for (int i = 0; i < detail; i++) {
            float innerRadius = radius - thicknessHalf;
            float outerRadius = radius + thicknessHalf;
            float angle = map(i, 0, detail-1, 0, TAU);
            if(angle > extent * TAU){
                break;
            }
            float x0 = innerRadius * cos(angle);
            float y0 = innerRadius * sin(angle);
            float x1 = outerRadius * cos(angle);
            float y1 = outerRadius * sin(angle);
            pg.vertex(x0, y0);
            pg.vertex(x1, y1);
        }
        pg.endShape();
        pg.popMatrix();
        gui.popFolder();
    }

}
