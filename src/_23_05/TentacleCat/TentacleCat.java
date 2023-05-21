package _23_05.TentacleCat;

import _0_utils.Utils;
import com.krab.lazy.LazyGui;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PVector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TentacleCat extends PApplet {
    PImage drawing;
    Map<Integer, PImage> tentacleSegments = new HashMap<>();
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
        drawing = loadImage("_23_05/TentacleCat/generous_crop.png");
        Utils.setupSurface(this, surface);
        gui = new LazyGui(this);
        pg = createGraphics(width, height, P2D);
        frameRate(144);
    }

    @Override
    public void draw() {
        pg.beginDraw();
        drawBackground();
        pg.scale(gui.slider("scale", 0.52f));
        pg.image(drawing, 0, 0);
        drawTentacles();
        pg.endDraw();
        image(pg, 0, 0);
        Utils.record(this, gui);
    }

    private void drawTentacles() {
        updateSourceImages();
        updateTentacles();
    }

    private void updateTentacles() {
        gui.pushFolder("tentacles");
        float t = radians(frameCount);
        pg.pushMatrix();
        PVector pos = gui.plotXY("base pos", 600, 600);
        pg.translate(pos.x, pos.y);
        pg.blendMode(DARKEST);
        int tentCount = gui.sliderInt("tentacle count",2);
        for (int tentacleIndex = 0; tentacleIndex < tentCount; tentacleIndex++) {
            pg.pushMatrix();
            float tentNorm = norm(tentacleIndex, 0, tentCount-1);
            pg.rotate(gui.slider("tentnorm rotate")*tentNorm);
            pg.translate(gui.plotXY("tent base").x, gui.plotXY("tent base").y);
            pg.translate(gui.plotXY("tentnorm translate").x*tentNorm,
                    gui.plotXY("tentnorm translate").y*tentNorm);
            int count = gui.sliderInt("seg count", 11);
            for (int segIndex = 0; segIndex < count; segIndex++) {
                float norm = norm(segIndex, 0, count - 1);
                float length = gui.slider("base length", 100) + gui.slider("norm length") * norm;
                int tailIndex = tentacleSegments.keySet().size() - 1;
                int imageIndex = floor(norm * tailIndex);
                if (segIndex == count - 1) {
                    imageIndex = tailIndex;
                }
                PImage img = tentacleSegments.get(imageIndex);
                pg.imageMode(CORNER);
                pg.pushMatrix();
                pg.translate(-img.width*0.5f, 0);
                pg.image(img, 0, 0);
                pg.popMatrix();
                pg.rotate(gui.slider("sin mag") *
                        sin(gui.slider("time") * t +
                                norm * (gui.slider("sin freq", 5)))
                );
                pg.translate(0, length);
            }
            pg.popMatrix();
        }
        pg.blendMode(BLEND);
        pg.popMatrix();
        gui.popFolder();
    }

    private void updateSourceImages() {
        gui.pushFolder("source images");
        // update source images first
        int segmentCount = gui.sliderInt("img count", 3);
        for (int i = 0; i < segmentCount; i++) {
            gui.pushFolder("img #" + i);
            PVector pos = gui.plotXY("pos", 1503, 274);
            PVector size = gui.plotXY("size", 181, 114);
            if (gui.toggle("show stroke", true)) {
                pg.stroke(gui.colorPicker("stroke", color(0)).hex);
                pg.noFill();
                pg.rectMode(CORNER);
                pg.rect(pos.x, pos.y, size.x, size.y);
            }

            if (gui.toggle("update") || gui.button("update once") || tentacleSegments.get(i) == null) {
                tentacleSegments.put(i, drawing.get(
                        floor(pos.x),
                        floor(pos.y),
                        floor(size.x),
                        floor(size.y)));
            }
            if (gui.toggle("debug view")) {
                PVector debugPos = gui.plotXY("debug pos", 0, 1200);
                pg.image(tentacleSegments.get(i), debugPos.x, debugPos.y, size.x, size.y);
            }
            gui.popFolder();
        }
        gui.popFolder();
    }

    private void drawBackground() {
        pg.fill(gui.colorPicker("background").hex);
        pg.noStroke();
        pg.rectMode(CORNER);
        pg.rect(0, 0, width, height);
    }
}
