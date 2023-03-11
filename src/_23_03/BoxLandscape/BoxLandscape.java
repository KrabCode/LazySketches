package _23_03.BoxLandscape;

import _0_utils.Utils;
import processing.core.PApplet;
import processing.core.PGraphics;
import lazy.LazyGui;
import processing.core.PVector;

public class BoxLandscape extends PApplet {
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
        pg = createGraphics(width, height, P3D);
        colorMode(HSB, 1, 1, 1, 1);
        frameRate(144);
    }

    @Override
    public void draw() {
        pg.beginDraw();
        drawBackground();
        perspective();
        drawBoxes();
        pg.endDraw();
        clear();
        image(pg, 0, 0);
        gui.draw();
        Utils.updateGetFrameRateAverage(this, gui, 144);
        Utils.drawCustomCursor(this, gui);
        Utils.record(this, gui);
    }

    private void drawBoxes() {
        gui.pushFolder("boxes");
        pg.pushMatrix();
        PVector pos = gui.plotXYZ("position");
        pg.translate(pos.x+width/2f, pos.y+height/2f, pos.z);
        PVector rot = gui.plotXYZ("rotation");
        pg.rotateX(rot.x);
        pg.rotateY(rot.y);
        pg.rotateZ(rot.z);
        pg.fill(gui.colorPicker("fill", color(1)).hex);
        pg.stroke(gui.colorPicker("stroke", color(0)).hex);
        pg.strokeWeight(gui.slider("weight", 1));
        PVector xzRange = gui.plotXY("xz range", 400);
        float xRange = xzRange.x;
        float zRange = xzRange.y;
        int xCount = gui.sliderInt("x count", 10);
        int zCount = gui.sliderInt("z count", 10);
        float xMargin = gui.slider("x margin", 5);
        float zMargin = gui.slider("z margin", 5);
        PVector xzSize = new PVector((xRange*2) / xCount - xMargin, (zRange*2) / zCount - zMargin);
        float ySize = gui.slider("y size", 50);
        for (int xi = 0; xi < xCount; xi++) {
            for (int zi = 0; zi < zCount; zi++) {
                pg.pushMatrix();
                float x = map(xi, 0, xCount, -xRange, xRange);
                float y = 0;
                float z = map(zi, 0, zCount, -zRange, zRange);
                pg.translate(x,y,z);
                pg.box(xzSize.x, ySize, xzSize.y);
                pg.popMatrix();
            }
        }
        pg.popMatrix();
        gui.popFolder();
    }

    private void drawBackground() {
        pg.background(gui.colorPicker("background").hex);
    }

    public void perspective() {
        gui.pushFolder("perspective");
        float cameraFOV = radians(gui.slider("FOV", 60)); // at least for now
        float cameraY = height / 2.0f;
        float cameraZ = cameraY / ((float) Math.tan(cameraFOV / 2.0f));
        float cameraNear = gui.slider("near", cameraZ / 10.0f);
        float cameraFar = gui.slider("far", cameraZ * 10.0f);
        float cameraAspect = (float) width / (float) height;
        perspective(cameraFOV, cameraAspect, cameraNear, cameraFar);
        gui.popFolder();
    }

    public void perspective(float fov, float aspect, float zNear, float zFar) {
        float ymax = zNear * (float) Math.tan(fov / 2);
        float ymin = -ymax;
        float xmin = ymin * aspect;
        float xmax = ymax * aspect;
        pg.frustum(xmin, xmax, ymin, ymax, zNear, zFar);
    }
}

