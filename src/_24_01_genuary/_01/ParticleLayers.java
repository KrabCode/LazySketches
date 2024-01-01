package _24_01_genuary._01;

import _0_utils.Utils;
import com.krab.lazy.PickerColor;
import processing.core.PApplet;
import processing.core.PGraphics;
import com.krab.lazy.LazyGui;
import processing.core.PVector;

import java.util.ArrayList;

// Particles, lots of them.
// https://genuary2024.github.io/

public class ParticleLayers extends PApplet {
    LazyGui gui;
    PGraphics pg;
    ArrayList<Layer> layers = new ArrayList<>();

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
        colorMode(HSB, 1, 1, 1, 1);
        pg = createGraphics(width, height, P2D);
        pg.beginDraw();
        pg.colorMode(HSB, 1, 1, 1, 1);
        pg.endDraw();
        frameRate(60);
    }

    @Override
    public void draw() {
        pg.beginDraw();
        drawBackground();
        pg.translate(width * .5f, height * .5f);
        drawLayers();
        pg.endDraw();
        image(pg, 0, 0);
        Utils.record(this, gui);
        gui.draw();
    }

    private void drawBackground() {
        pg.fill(gui.colorPicker("background").hex);
        pg.noStroke();
        pg.rectMode(CORNER);
        pg.rect(0, 0, width, height);
    }

    private void drawLayers() {
        gui.pushFolder("layers");
        int layerCount = gui.sliderInt("count", 3, 1, 100);
        while (layers.size() < layerCount) {
            layers.add(new Layer());
        }
        while (layers.size() > layerCount) {
            layers.remove(layers.size() - 1);
        }
        // global layer variables
        float scaleBase = gui.slider("scale base", 1);
        float scaleLayer = gui.slider("scale layer", 1);
        boolean reset = gui.button("reset");
        for (int layerIndex = 0; layerIndex < layerCount; layerIndex++) {
            // local layer variables
            pg.pushMatrix();
            float layerNorm = norm(layerIndex, 0, layerCount);
            pg.scale(layerNorm * scaleLayer + scaleBase);
            drawLayer(layers.get(layerIndex).particles, layerNorm);
            if(reset){
                layers.get(layerIndex).particles.clear();
            }
            pg.popMatrix();
        }
        gui.popFolder();
    }

    private void drawLayer(ArrayList<P> ps, float layerNorm) {
        int pCount = gui.sliderInt("p count", 100, 0, 10000);
        int pSpawn = gui.sliderInt("p spawn", 10, 0, 1000);
        PVector spawnSize = gui.plotXY("spawn size", 800);
        if (ps.size() < pCount) {
            for (int i = 0; i < pSpawn; i++) {
                ps.add(new P(new PVector(
                        random(-spawnSize.x, spawnSize.x),
                        random(-spawnSize.y, spawnSize.y))
                ));
                if(ps.size() > pCount){
                    break;
                }
            }
        }
        float alphaBase = gui.slider("alpha base", 1);
        float alphaRange = gui.slider("alpha range", -1);
        float layerFade = alphaBase + layerNorm * alphaRange;
        pg.strokeWeight(gui.slider("p weight", 1));
        float angleBase = gui.slider("angle base", 1);
        float angleRange = gui.slider("angle range", 3);
        float drag = gui.slider("drag", 1);
        float acc = gui.slider("speed", 1);
        ArrayList<P> toDestroy = new ArrayList<>();
        PickerColor clr = gui.colorPicker("color");
        for (P p : ps) {
            float noiseFreq = gui.slider("noise freq", 1);
            float layerFreq = gui.slider("noise freq z", 1);
            float angleNoise = Utils.noise(3234.123f + p.pos.x * noiseFreq, 1834.123f + p.pos.y * noiseFreq, layerNorm * layerFreq);
            float angle = angleNoise * angleRange + angleBase;
            float lifeDuration = gui.slider("life duration", 60);
            float lifeFade = constrain(norm(frameCount, p.born, p.born + gui.slider("fade in", 60)), 0, 1);
            float fadeOutDuration = gui.slider("fade out", 60);
            if(frameCount >= p.born + lifeDuration - fadeOutDuration){
                lifeFade =  1 - constrain(norm(frameCount, p.born + lifeDuration - fadeOutDuration, p.born + lifeDuration), 0, 1);
            }
            if(frameCount >= p.born + lifeDuration){
                toDestroy.add(p);
            }
            p.spd.add(new PVector(cos(angle), sin(angle)).mult(acc));
            p.pos.add(p.spd);
            p.spd.mult(drag);
            pg.stroke(clr.hue, clr.saturation, clr.brightness, min(lifeFade, layerFade));
            pg.point(p.pos.x, p.pos.y);
        }
        ps.removeAll(toDestroy);
    }

    class Layer {
        ArrayList<P> particles = new ArrayList<>();
    }

    class P {
        final PVector pos,
                spd = new PVector();
        int born = frameCount;

        P(PVector pos) {
            this.pos = pos;
        }
    }
}

