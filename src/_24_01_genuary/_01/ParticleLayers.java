package _24_01_genuary._01;

import _0_utils.Utils;
import com.krab.lazy.PickerColor;
import processing.core.*;
import com.krab.lazy.LazyGui;

import java.util.ArrayList;

// Particles, lots of them.
// https://genuary2024.github.io/

public class ParticleLayers extends PApplet {
    LazyGui gui;
    PGraphics pg;
    ArrayList<Layer> layers = new ArrayList<>();
    private PImage img;

    public static void main(String[] args) {
        PApplet.main(java.lang.invoke.MethodHandles.lookup().lookupClass());
    }

    @Override
    public void settings() {
//        size(1080, 1080, P2D);
        fullScreen(P3D);
    }

    @Override
    public void setup() {
        gui = new LazyGui(this);
        colorMode(HSB, 1, 1, 1, 1);
        pg = createGraphics(width, height, P3D);
        pg.beginDraw();
        pg.colorMode(HSB, 1, 1, 1, 1);
        pg.endDraw();
        frameRate(60);
    }

    @Override
    public void draw() {
        pg.beginDraw();
        drawBackground();
        drawLayers();
        drawDrosteBox();
        pg.endDraw();
        image(pg, 0, 0);
        Utils.record(this, gui);
        gui.draw();
    }

    private void drawDrosteBox() {
        gui.pushFolder("droste box");
        PVector getPos = gui.plotXY("get pos", 0);
        PVector getSize = gui.plotXY("get size", 1000);
        float boxZ = gui.slider("box z", 0);
        if(gui.toggle("show box")){
            pg.push();
            pg.stroke(1);
            pg.noFill();
            pg.rectMode(CORNER);
            pg.translate(0, 0, boxZ);
            pg.rect(getPos.x, getPos.y, getSize.x, getSize.y);
            pg.pop();
        }
        pg.stroke(gui.colorPicker("stroke").hex);
        pg.strokeWeight(gui.slider("stroke weight", 1));
        float boxSize = gui.slider("box size", 1);
        PVector boxRot = gui.plotXYZ("box rot", 0);
        boxRot.add(gui.plotXYZ("box rot speed", 0));
        gui.plotSet("box rot", boxRot);
        PVector boxPos = gui.plotXYZ("box pos", 0);
        pg.pushMatrix();
        pg.translate(width * .5f, height * .5f);
        pg.translate(boxPos.x, boxPos.y, boxPos.z);
        pg.rotateX(boxRot.x);
        pg.rotateY(boxRot.y);
        pg.rotateZ(boxRot.z);
        pg.scale(gui.slider("box scale", 1));
        if(img != null){
            texturedCube(img, boxSize);
        }
        pg.popMatrix();
        img = pg.get(floor(getPos.x), floor(getPos.y), floor(getSize.x), floor(getSize.y));
        pg.noTexture();
        gui.popFolder();
    }

    // texturedCube() source:
    // https://forum.processing.org/one/topic/box-multitextures.html
    void texturedCube(PImage tex, float size) {
        pg.beginShape(QUADS);
        pg.texture(tex);
        pg.textureMode(NORMAL);

        // Given one texture and six faces, we can easily set up the uv coordinates
        // such that four of the faces tile "perfectly" along either u or v, but the other
        // two faces cannot be so aligned.  This code tiles "along" u, "around" the X/Z faces
        // and fudges the Y faces - the Y faces are arbitrarily aligned such that a
        // rotation along the X axis will put the "top" of either texture at the "top"
        // of the screen, but is not otherwised aligned with the X/Z faces. (This
        // just affects what type of symmetry is required if you need seamless
        // tiling all the way around the cube)

        // +Z "front" face
        pg.vertex(-size, -size, +size, 0, 0);
        pg.vertex(+size, -size, +size, 1, 0);
        pg.vertex(+size, +size, +size, 1, 1);
        pg.vertex(-size, +size, +size, 0, 1);

        // -Z "back" face
        pg.vertex(+size, -size, -size, 0, 0);
        pg.vertex(-size, -size, -size, 1, 0);
        pg.vertex(-size, +size, -size, 1, 1);
        pg.vertex(+size, +size, -size, 0, 1);

        // +Y "bottom" face
        pg.vertex(-size, +size, +size, 0, 0);
        pg.vertex(+size, +size, +size, 1, 0);
        pg.vertex(+size, +size, -size, 1, 1);
        pg.vertex(-size, +size, -size, 0, 1);

        // -Y "top" face
        pg.vertex(-size, -size, -size, 0, 0);
        pg.vertex(+size, -size, -size, 1, 0);
        pg.vertex(+size, -size, +size, 1, 1);
        pg.vertex(-size, -size, +size, 0, 1);

        // +X "right" face
        pg.vertex(+size, -size, +size, 0, 0);
        pg.vertex(+size, -size, -size, 1, 0);
        pg.vertex(+size, +size, -size, 1, 1);
        pg.vertex(+size, +size, +size, 0, 1);

        // -X "left" face
        pg.vertex(-size, -size, -size, 0, 0);
        pg.vertex(-size, -size, +size, 1, 0);
        pg.vertex(-size, +size, +size, 1, 1);
        pg.vertex(-size, +size, -size, 0, 1);

        pg.endShape();
    }
    
    private void drawBackground() {
        pg.fill(gui.colorPicker("background").hex);
        pg.noStroke();
        pg.rectMode(CORNER);
        pg.rect(0, 0, width, height);
    }

    private void drawLayers() {
        gui.pushFolder("layers");
        pg.pushMatrix();
        pg.translate(width * .5f, height * .5f);
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
            if (reset) {
                layers.get(layerIndex).particles.clear();
            }
            pg.popMatrix();
        }
        gui.popFolder();
        pg.popMatrix();
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
                if (ps.size() > pCount) {
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
            if (frameCount >= p.born + lifeDuration - fadeOutDuration) {
                lifeFade = 1 - constrain(norm(frameCount, p.born + lifeDuration - fadeOutDuration, p.born + lifeDuration), 0, 1);
            }
            if (frameCount >= p.born + lifeDuration) {
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

