package _23_07;

import _0_utils.Utils;
import _22_03.PostFxAdapter;
import com.krab.lazy.LazyGui;
import com.krab.lazy.LazyGuiSettings;
import com.krab.lazy.utils.ArrayListBuilder;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;

import java.util.ArrayList;

public class Concentric extends PApplet {
    private static int maximumShapeCountSoFar = 1;
    private static final String RAYS = "rays";
    private static final String CIRCLE = "circle";
    private static final String SINE_CIRCLE = "sine";
    private static final String TRIANGLE_STRIP_ARC = "strip";
    private static final String SPIROGRAPH = "spiro";
    ArrayList<String> typeOptions = new ArrayListBuilder<String>()
            .add(RAYS)
            .add(SINE_CIRCLE)
            .add(TRIANGLE_STRIP_ARC)
            .add(SPIROGRAPH)
            .build();
    LazyGui gui;
    PGraphics pg;


    public static void main(String[] args) {
        PApplet.main(java.lang.invoke.MethodHandles.lookup().lookupClass());
    }

    @Override
    public void settings() {
//        size(800, 800, P2D);
        fullScreen(P3D);
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

    private void drawShapes() {
        gui.pushFolder("shapes");
        PVector pos = gui.plotXY("position");
        pg.translate(pos.x, pos.y);
        pg.scale(gui.slider("scale", 1));
        int shapeCount = gui.sliderInt("count", typeOptions.size(), 1, 1000);
        if (gui.button("add shape")) {
            gui.sliderIntSet("count", shapeCount + 1);
        }
        maximumShapeCountSoFar = max(shapeCount, maximumShapeCountSoFar);
        for (int i = 0; i <= maximumShapeCountSoFar; i++) {
            gui.pushFolder(String.valueOf(i));
            if (i >= shapeCount) {
                gui.hideCurrentFolder();
                gui.popFolder();
                continue;
            } else {
                gui.showCurrentFolder();
            }

            gui.text("");
            float radius = gui.slider("radius", 10 + i * 50);
            float z = gui.slider("z pos", 0);
            float rotateAngle = gui.slider("rotate");
            float rotateDelta = radians(gui.slider("rotate +", 1));
            pg.strokeWeight(gui.slider("weight", 2));
            pg.stroke(gui.colorPicker("stroke", color(255)).hex);
            pg.fill(gui.colorPicker("fill", color(0, 0)).hex);
            String type = gui.radio("type", typeOptions, typeOptions.get(i % typeOptions.size()));
            String name = i + "-" + type;
            gui.textSet("", name);
            gui.sliderAdd("rotate", rotateDelta);
            pg.pushMatrix();
            pg.translate(0,0, z);
            drawCircleTemplate(type, radius, rotateAngle);
            drawSineCircle(type, radius, rotateAngle);
            drawRayCircle(type, radius, rotateAngle);
            drawTriangleStripArc(type, radius, rotateAngle);
            drawSpirographs(type, radius, rotateAngle);
            pg.popMatrix();
            gui.popFolder();
        }
        gui.popFolder();
    }

    @SuppressWarnings("unused")
    private void drawCircleTemplate(String type, float radius, float rotation) {
        gui.pushFolder(CIRCLE + " params");
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
        gui.pushFolder(RAYS + " params");
        gui.showCurrentFolder();
        if (!type.equals(RAYS)) {
            gui.hideCurrentFolder();
            gui.popFolder();
            return;
        }
        pg.pushMatrix();
        pg.rotate(rotation);
        int rayCount = gui.sliderInt("ray count", 12);
        float length = gui.slider("length", 10);
        float lengthHalf = length * 0.5f;
        for (int i = 0; i < rayCount; i++) {
            pg.pushMatrix();
            float localAngle = map(i, 0, rayCount, 0, TAU);
            pg.rotate(localAngle);
            pg.line(0, radius - lengthHalf,
                    0, radius + lengthHalf);
            pg.popMatrix();
        }
        pg.popMatrix();
        gui.popFolder();
    }

    private void drawSineCircle(String type, float radius, float rotation) {
        gui.pushFolder(SINE_CIRCLE + " params");
        gui.showCurrentFolder();
        if (!type.equals(SINE_CIRCLE)) {
            gui.hideCurrentFolder();
            gui.popFolder();
            return;
        }
        boolean rotateWhole = gui.toggle("rotate whole");
        float freq = gui.slider("frequency", 10);
        float amp = gui.slider("amplitude", 0.25f) * radius;
        int detail = gui.sliderInt("detail", 360, 1, 1000);
        pg.pushMatrix();
        if (rotateWhole) {
            pg.rotate(rotation);
        }

        pg.beginShape();
        for (int vertex = 0; vertex < detail; vertex++) {
            float localAngle = map(vertex, 0, detail, 0, TAU);
            float waveRotation = rotateWhole ? 0 : rotation;
            float localRadius = radius + amp * sin((localAngle - waveRotation) * freq);
            float x = localRadius * cos(localAngle);
            float y = localRadius * sin(localAngle);
            pg.vertex(x, y);
        }
        pg.endShape(CLOSE);
        gui.popFolder();
        pg.popMatrix();
    }


    private void drawTriangleStripArc(String type, float radius, float rotation) {
        gui.pushFolder(TRIANGLE_STRIP_ARC + " params");
        gui.showCurrentFolder();
        if (!type.equals(TRIANGLE_STRIP_ARC)) {
            gui.hideCurrentFolder();
            gui.popFolder();
            return;
        }
        // only ask for GUI values here
        float extent = gui.slider("angle extent", 0.25f);
        if (extent <= 0) {
            gui.popFolder();
            return;
        }
        pg.pushMatrix();
        pg.rotate(rotation);
        pg.beginShape(TRIANGLE_STRIP);
        int detail = gui.sliderInt("detail", 360, 1, 1000);
        float thickness = gui.slider("height", radius * 0.25f);
        float thicknessHalf = thickness * 0.5f;
        for (int i = 0; i < detail; i++) {
            float innerRadius = radius - thicknessHalf;
            float outerRadius = radius + thicknessHalf;
            float angle = map(i, 0, detail - 1, 0, TAU);
            if (angle > extent * TAU) {
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

    private void drawSpirographs(String type, float radius, float rotateAngle) {
        gui.pushFolder(SPIROGRAPH + " params");
        gui.showCurrentFolder();
        if (!type.equals(SPIROGRAPH)) {
            gui.hideCurrentFolder();
            gui.popFolder();
            return;
        }
        // only ask for GUI values here
        pg.pushStyle();
        pg.noFill();
        float radiusSmall = radius / gui.sliderInt("small circles", 3);
        float holeDistance = gui.slider("hole distance", 15);
        int detail = gui.sliderInt("detail", 500);
        boolean shouldDrawGradually = gui.toggle("draw gradually");
        float gradualMaxAngle = radians(gui.slider("draw length", 0.9f) * 360);
        if (shouldDrawGradually) {
            pg.beginShape();
            for (int i = 0; i < detail; i++) {
                float bigAngle = map(i, 0, detail-1, 0, TAU);
                if (bigAngle > gradualMaxAngle) {
                    break;
                }
                float finalAngle = rotateAngle + bigAngle;
                PVector p = getSpirographPoint(radius, radiusSmall, finalAngle, holeDistance);
                pg.vertex(p.x, p.y);
            }
            pg.endShape();
        } else {
            pg.rotate(rotateAngle);
            pg.beginShape();
            for (int i = 0; i < detail; i++) {
                float bigAngle = map(i, 0, detail-1, 0, TAU);
                PVector p = getSpirographPoint(radius, radiusSmall, bigAngle, holeDistance);
                pg.vertex(p.x, p.y);
            }
            pg.endShape(CLOSE);
        }
        pg.popStyle();
        gui.popFolder();
    }

    PVector getSpirographPoint(float radiusBig, float radiusSmall, float bigAngle, float holeDistance) {
        float smallCenterRadius = radiusBig - radiusSmall;
        float smallCenterX = smallCenterRadius * cos(bigAngle);
        float smallCenterY = smallCenterRadius * sin(bigAngle);
        float smallRotation = - (smallCenterRadius / radiusSmall) * bigAngle;
        float holeX = smallCenterX + holeDistance * cos(smallRotation);
        float holeY = smallCenterY + holeDistance * sin(smallRotation);
        return new PVector(holeX, holeY);
    }
}
