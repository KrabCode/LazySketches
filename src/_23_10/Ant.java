package _23_10;

import _0_utils.Utils;
import com.krab.lazy.*;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;

@SuppressWarnings("DuplicatedCode")
public class Ant extends PApplet {
    LazyGui gui;
    PGraphics pg;
    float t;
    int antCount = 4;
    PImage[] ants = new PImage[antCount];

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
        for (int i = 0; i < antCount; i++) {
            ants[i] = loadImage("_23_10/ant/ant0" + (i+1) + ".png");
        }
        gui = new LazyGui(this);
        pg = createGraphics(width, height, P2D);
        pg.imageMode(CENTER);
        colorMode(HSB, 1, 1, 1, 1);
        frameRate(144);
    }

    @Override
    public void draw() {
        pg.beginDraw();
        drawBackground();
        pg.translate(width/2f, height/2f);
        float animationSpeed = radians(frameCount) / TAU * gui.slider("speed");
        int currentAntIndex = floor(animationSpeed % antCount);
        pg.translate(gui.plotXY("pos").x, gui.plotXY("pos").y);
        pg.rotate(gui.slider("rotate"));
        pg.scale(gui.slider("scale", 1));
        pg.tint(gui.colorPicker("tint", color(255)).hex);
        pg.image(ants[currentAntIndex], 0, 0);
        gui.pushFolder("shader");
        String shaderPath = gui.text("path", "_23_10/ant/hue.glsl");
        t += radians(gui.slider("shader time +", 1));
        ShaderReloader.getShader(shaderPath).set("time", t);
        ShaderReloader.filter(shaderPath, pg);
        gui.popFolder();
        pg.endDraw();
        image(pg, 0, 0);
        Utils.record(this, gui);
    }

    private void drawBackground() {
        pg.fill(gui.colorPicker("background").hex);
        pg.noStroke();
        pg.rectMode(CORNER);
        pg.rect(0, 0, width, height);
    }
}

