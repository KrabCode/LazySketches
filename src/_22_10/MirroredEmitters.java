package _22_10;

import _0_utils.Utils;
import _22_03.PostFxAdapter;
import lazy.LazyGui;
import lazy.PickerColor;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;

import java.util.ArrayList;


public class MirroredEmitters extends PApplet {
    LazyGui gui;
    PGraphics pg;
    ArrayList<Particle> particles = new ArrayList<>();
    ArrayList<Particle> particlesBin = new ArrayList<Particle>();
    PVector center = new PVector();

    public static void main(String[] args) {
        PApplet.main(java.lang.invoke.MethodHandles.lookup().lookupClass());
    }

    @Override
    public void settings() {
//        size(1080, 1080, P3D);
        fullScreen(P2D);
    }

    @Override
    public void setup() {
        gui = new LazyGui(this);
        pg = createGraphics(width, height, P2D);
        pg.beginDraw();
        pg.background(0);
        pg.endDraw();
        center.x = width / 2f;
        center.y = height / 2f;
    }

    @Override
    public void draw() {
        pg.beginDraw();
        drawBackground();
        pg.translate(width / 2f, height / 2f);
        drawEmitterCircles();
        drawParticles();
        Utils.shaderMove(pg, gui);
        pg.endDraw();
        PostFxAdapter.apply(this, gui, pg);
        clear();
        image(pg, 0, 0);
        Utils.record(this, gui);
    }

    private void drawParticles() {
        for (Particle p : particles) {
            p.update();
        }
        particles.removeAll(particlesBin);
        particlesBin.clear();
    }

    private void drawEmitterCircles() {
        String path = "foreground/circles/";
        chooseBlendMode("foreground");
        int circleCount = gui.sliderInt(path + "count", 1);
        for (int i = 1; i <= circleCount; i++) {
            String pathI = path + i + "/";
            int mirrorCount = gui.sliderInt(pathI + "mirror count", 1);
            float rotationOffset = gui.slider(pathI + "rotate offset");
            float x = gui.slider(pathI + "x");
            float y = gui.slider(pathI + "y");
            float w = gui.slider(pathI + "w", 200);
            float h = gui.slider(pathI + "h", 100);
            boolean showEllipse = gui.toggle(pathI + "ellipse visible", true);
            for (int mirrorIndex = 0; mirrorIndex < mirrorCount; mirrorIndex++) {
                pg.pushMatrix();
                float mirrorAngle = map(mirrorIndex, 0, mirrorCount, 0, TAU);
                pg.rotate(mirrorAngle + rotationOffset);
                PickerColor ellipseColor = gui.colorPicker(pathI + "ellipse stroke", color(255));
                pg.strokeWeight(gui.slider(pathI + "ellipse weight", 1));
                if (showEllipse) {
                    pg.stroke(ellipseColor.hex);
                    pg.noFill();
                    pg.ellipse(x, y, w, h);
                }
                boolean showPoint = gui.toggle(pathI + "point visible", true);
                int strokeHex = gui.colorPicker(pathI + "point stroke", color(255)).hex;
                pg.stroke(strokeHex);
                pg.strokeWeight(gui.slider(pathI + "point weight", 1.99f));
                int speed = gui.sliderInt(pathI + "loop frames", 600, 1, Integer.MAX_VALUE);
                float t = map(frameCount % speed, 0, speed, 0, TAU);
                float px = x + (w / 2f) * sin(t);
                float py = y + (h / 2f) * cos(t);
                if (showPoint) {
                    pg.point(px, py);
                }
                if (gui.toggle(pathI + "spawn particles", true)) {
                    int particleSpawnRate = gui.sliderInt(pathI + "spawn rate", 1);
                    float gaussSpread = gui.slider(pathI + "gauss spread", 5);
                    for (int particleIndex = 0; particleIndex < particleSpawnRate; particleIndex++) {
                        particles.add(new Particle(
                                        pg.screenX(px, py)-center.x + randomGaussian() * gaussSpread,
                                        pg.screenY(px, py)-center.y + randomGaussian() * gaussSpread,
                                        strokeHex
                                )
                        );
                    }
                }
                pg.popMatrix();
            }
        }
    }

    private void drawBackground() {
        chooseBlendMode("background");
        pg.fill(gui.colorPicker("background/color").hex);
        pg.noStroke();
        pg.rectMode(CORNER);
        pg.rect(0, 0, width, height);
        pg.blendMode(BLEND);
    }

    private void chooseBlendMode(String path) {
        switch (gui.radio(path + "/blend mode", new String[]{"blend", "add", "sub"})) {
            case "blend": {
                pg.blendMode(BLEND);
                break;
            }
            case "add": {
                pg.blendMode(ADD);
                break;
            }
            case "sub": {
                pg.blendMode(SUBTRACT);
                break;
            }
            default:
                throw new IllegalStateException("Unexpected blend mode");
        }
    }

    class Particle {
        PVector pos = new PVector();
        PVector spd = new PVector();
        int frameCreated = frameCount;
        int clr;
        float weightRandom = abs(randomGaussian());

        Particle(float x, float y, int clr) {
            this.clr = clr;
            pos.x = x;
            pos.y = y;
        }

        void update() {
            int frameLifetime = gui.sliderInt("particles/lifetime", 60);
            if(frameCount > frameCreated + frameLifetime){
                particlesBin.add(this);
            }
            pg.stroke(clr);
            pg.strokeWeight(gui.slider("particles/weight", 1) + weightRandom * gui.slider("particles/weight random", 1));
            pg.point(pos.x, pos.y);
            PVector awayFromCenter = PVector.div(pos, pos.mag()).normalize();
            pos.add(awayFromCenter.mult(gui.slider("particles/center force", 1)));
            float noiseFreq = gui.slider("particles/noise freq");
            float noiseTime = gui.slider("particles/noise time");
            float t = radians(frameCount);
            float noiseX = -1+2*noise(pos.x*noiseFreq, pos.y*noiseFreq, 3271.2131f + t * noiseTime);
            float noiseY = -1+2*noise(pos.x*noiseFreq, pos.y*noiseFreq, 1327.1243f + t * noiseTime);
            PVector acc = new PVector(noiseX, noiseY).mult(gui.slider("particles/noise strength", 0.1f));
            spd.add(acc);
            spd.mult(gui.slider("particles/drag", 0.95f));
            pos.add(spd);
        }
    }
}

