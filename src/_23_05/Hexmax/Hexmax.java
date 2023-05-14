package _23_05.Hexmax;

import _0_utils.Utils;
import com.krab.lazy.LazyGui;
import processing.core.PApplet;
import processing.core.PGraphics;


public class Hexmax extends PApplet {
    LazyGui gui;
    PGraphics pg;

    public static void main(String[] args) {
        PApplet.main(java.lang.invoke.MethodHandles.lookup().lookupClass());
    }

    @Override
    public void settings() {
//        fullScreen(P2D);
        size(600, 600, P2D);
    }

    @Override
    public void setup() {
        Utils.setupSurface(this, surface);
        gui = new LazyGui(this);
        pg = createGraphics(width, height, P2D);
        pg.smooth(8);
        colorMode(HSB, 1, 1, 1, 1);
        frameRate(144);
    }

    @Override
    public void draw() {
        pg.beginDraw();
        pg.hint(DISABLE_DEPTH_TEST);
        drawBackground();
        pg.translate(width/2f, height/2f);
        drawHex();
        pg.endDraw();
        image(pg, 0, 0);
        gui.draw();
//        Utils.record(this, gui);
    }

    private void drawBackground() {
        pg.fill(gui.colorPicker("background").hex);
        pg.noStroke();
        pg.rectMode(CORNER);
        pg.rect(0, 0, width, height);
    }

    private void drawHex() {
        gui.pushFolder("hex");
        drawQuadStripHexes();
        gui.popFolder();
    }

    private void drawQuadStripHexes() {
        gui.pushFolder("quad strip");
        float t = gui.slider("time", 0);
        gui.sliderSet("time", t + radians(gui.slider("time +")));
        int hexCorners = 6;
        for (int cornerIndex = 0; cornerIndex < hexCorners; cornerIndex++) {
            pg.pushMatrix();
            float theta = map(cornerIndex, 0, hexCorners, 0, TAU);
            pg.rotate(theta);
            drawQuadStripHex(t);
            pg.popMatrix();
        }
        gui.popFolder();
    }

    private void drawQuadStripHex(float t) {
        float totalHeight = gui.slider("total height", 200);
        int quadCount = gui.sliderInt("quad count", 8);
        float quadPos = gui.slider("quad r", 50);
        float angleStep = TAU / 12f;
        float quadHeight = gui.slider("quad height", totalHeight / quadCount);
        pg.beginShape(QUADS);
        pg.stroke(gui.colorPicker("stroke", color(255)).hex);
        pg.strokeWeight(gui.slider("weight", 1.5f));
        for (int quadIndex = 0; quadIndex < quadCount; quadIndex++) {
            float quadNorm = norm(quadIndex, 0, quadCount);
            float r0 = quadPos + quadNorm * totalHeight;
            float r1 = quadPos + quadNorm * totalHeight + quadHeight;
            pg.fill(gui.gradientColorAt("gradient",(quadNorm + t) % 1f).hex);
            pg.vertex(r0 * cos(-angleStep),r0 * sin(-angleStep));
            pg.vertex(r1 * cos(-angleStep),r1 * sin(-angleStep));
            pg.vertex(r1 * cos(+angleStep),r1 * sin(+angleStep));
            pg.vertex(r0 * cos(+angleStep),r0 * sin(+angleStep));
        }
        pg.endShape();
    }
}

