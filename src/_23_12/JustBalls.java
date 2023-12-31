package _23_12;

import _0_utils.Utils;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;
import com.krab.lazy.*;
import processing.core.PVector;

import java.util.ArrayList;

public class JustBalls extends PApplet {
    LazyGui gui;
    PGraphics pg;
    ArrayList<Ball> balls = new ArrayList<>();

    public static void main(String[] args) {
        PApplet.main(java.lang.invoke.MethodHandles.lookup().lookupClass());
    }

    @Override
    public void settings() {
        fullScreen(P2D);
    }

    @Override
    public void setup() {
        gui = new LazyGui(this
                //                ,new LazyGuiSettings().setLoadLatestSaveOnStartup(false)
        );
        pg = createGraphics(width, height, P2D);
        colorMode(HSB, 1, 1, 1, 1);
        frameRate(144);
//        surface.setSize(800, 600);
        int margin = 10;
//        surface.setLocation(displayWidth - width - margin, margin);
//        surface.setAlwaysOnTop(true);
    }

    @Override
    public void draw() {
        pg.beginDraw();
        drawBackground(pg);
        updateBalls(pg);
        pg.endDraw();
        image(pg, 0, 0);
        gui.draw();
    }

    private void drawBackground(PGraphics pg) {
        gui.pushFolder("background");
        if (frameCount % gui.sliderInt("draw every n frames", 1, 1, 100) != 0) {
            gui.popFolder();
            return;
        }
        pg.noStroke();
        pg.rectMode(CORNER);
        if (gui.toggle("darken")) {
            pg.blendMode(PConstants.SUBTRACT);
            pg.fill(gui.colorPicker("sub", color(0.05f)).hex);
            pg.rect(0, 0, width, height);
            pg.blendMode(PConstants.LIGHTEST);
            pg.fill(gui.colorPicker("min", color(0.1f)).hex);
            pg.rect(0, 0, width, height);
        } else {
            pg.blendMode(PConstants.ADD);
            pg.fill(gui.colorPicker("add", color(0.05f)).hex);
            pg.rect(0, 0, width, height);
            pg.blendMode(PConstants.DARKEST);
            pg.fill(gui.colorPicker("max", color(0.9f)).hex);
            pg.rect(0, 0, width, height);
        }
        pg.blendMode(PConstants.BLEND);
        gui.popFolder();
    }

    void updateBalls(PGraphics pg) {
        gui.pushFolder("balls");
        int ballCount = gui.sliderInt("ball count", 100, 1, 1000);
        while (balls.size() < ballCount) {
            balls.add(new Ball());
        }
        while (balls.size() > ballCount) {
            balls.remove(0);
        }
        for (Ball b : balls) {
            b.update();
            b.draw(pg);
        }
        gui.popFolder();
    }

    class Ball {
        PVector prevPos, pos, spd;
        float sizeOff;
        float hueOff;
        float satOff;
        float brOff;
        float diameter;

        Ball() {
            pos = new PVector(random(width), random(height));
            prevPos = pos.copy();
            spd = new PVector(random(-1, 1), random(-1, 1));
            sizeOff = randomGaussian();
            hueOff =  randomGaussian();
            satOff =  randomGaussian();
            brOff =   randomGaussian();
        }

        void update() {
            prevPos = pos.copy();
            pos.add(spd);
            float radius = diameter * 0.5f;
            if (pos.x - radius < 0 && spd.x < 0) {
                spd.x *= -1;
            }
            if (pos.x + radius > width && spd.x > 0) {
                spd.x *= -1;
            }
            if (pos.y - radius < 0 && spd.y < 0) {
                spd.y *= -1;
            }
            if (pos.y + radius > height && spd.y > 0) {
                spd.y *= -1;
            }
        }

        void draw(PGraphics pg) {
            PickerColor base = gui.colorPicker("color");
            PVector colorOffsets = gui.plotXYZ("color offsets", 0, 0, 0);
            pg.noStroke();
            pg.colorMode(HSB, 1, 1, 1, 1);
            pg.fill(
                    Utils.hueModulo(base.hue + hueOff * colorOffsets.x),
                    constrain(base.saturation + satOff * colorOffsets.y, 0, 1),
                    constrain(base.brightness + brOff * colorOffsets.z, 0, 1)
            );
            float baseSize = gui.slider("size", 10, 1, 100);
            diameter = baseSize + sizeOff * gui.slider("size offset", 10, 1, 1000);
            pg.rectMode(CENTER);
            pg.rect(pos.x, pos.y, diameter, diameter);
        }
    }
}

