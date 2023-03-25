package _22_08;

import _22_03.PostFxAdapter;
import processing.core.PApplet;
import processing.core.PGraphics;
import com.krab.lazy.LazyGui;

public class DoubleOrbitMandala extends PApplet {
    LazyGui gui;
    PGraphics pg;

    public static void main(String[] args) {
        PApplet.main(java.lang.invoke.MethodHandles.lookup().lookupClass());
    }

    @Override
    public void settings() {
//        size(1080, 1080, P2D);
        fullScreen(P2D);
    }

    @Override
    public void setup() {
        gui = new LazyGui(this);
        pg = createGraphics(width, height, P2D);
    }

    @Override
    public void draw() {
        pg.beginDraw();
        drawBackground();
        pg.translate(width*0.5f, height*0.5f);
        drawOrbitMandala();
        pg.endDraw();
        PostFxAdapter.apply(this, gui, pg);
        image(pg, 0, 0);
    }

    private void drawBackground() {
        pg.blendMode(SUBTRACT);
        pg.noStroke();
        pg.fill(gui.colorPicker("subtract").hex);
        pg.rect(0,0,width,height);

    }

    float thetaA, thetaB;

    private void drawOrbitMandala() {
        String path = "mandala";
        float speedA = radians(gui.slider(path +  "/a speed", -13.200005f));
        float speedB = radians(gui.slider(path +  "/b speed",  0.9199681f));
        float speedAB = gui.slider("/ab speed", 1);
        speedA *= speedAB * TAU;
        speedB *= speedAB* TAU;
        float radiusA = gui.slider(path + "/a radius", 327.3601f);
        float radiusB = gui.slider(path + "/b radius",  1566.3936f);
        pg.blendMode(gui.radio(path + "/blendMode", new String[]{"blend", "add"}).equals("blend") ? BLEND : ADD);
        pg.stroke(gui.colorPicker(path +  "/stroke", color(255)).hex);
        pg.strokeWeight(gui.slider(path + "/weight", 1.5f));
        int linesPerFrame = gui.sliderInt(path + "/lines per frame", 3);
        for (int i = 0; i < linesPerFrame; i++) {
            thetaA = (thetaA + speedA) % TAU;
            thetaB = (thetaB + speedB) % TAU;
            float ax = radiusA * cos(thetaA);
            float ay = radiusA * sin(thetaA);
            float bx = radiusB * cos(thetaB);
            float by = radiusB * sin(thetaB);
            pg.line(ax, ay, bx, by);
        }

    }
}

