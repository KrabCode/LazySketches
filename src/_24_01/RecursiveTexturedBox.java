package _24_01;

import _0_utils.Utils;
import com.krab.lazy.LazyGuiSettings;
import processing.core.PApplet;
import processing.core.PGraphics;
import com.krab.lazy.LazyGui;
import processing.core.PImage;
import processing.core.PVector;

@SuppressWarnings("DuplicatedCode")
public class RecursiveTexturedBox extends PApplet {
    LazyGui gui;
    PGraphics pg;
    private PImage img;

    public static void main(String[] args) {
        PApplet.main(java.lang.invoke.MethodHandles.lookup().lookupClass());
    }

    @Override
    public void settings() {
        size(1000, 1000, P3D);
        smooth(8);
    }

    @Override
    public void setup() {
        gui = new LazyGui(this, new LazyGuiSettings().setLoadLatestSaveOnStartup(false));
        colorMode(HSB, 1, 1, 1, 1);
        pg = createGraphics(width, height, P3D);
        pg.beginDraw();
        pg.colorMode(HSB, 1, 1, 1, 1);
        pg.endDraw();
        frameRate(144);
    }

    @Override
    public void draw() {
        pg.beginDraw();
        drawBackground();
        drawDrosteBox();
        pg.endDraw();
        image(pg, 0, 0);
        Utils.record(this, gui);
    }

    private void drawBackground() {
        int[] defaultGradient = new int[]{color(0.2f), color(0.5f)};
        pg.background(gui.gradient("background", defaultGradient));
    }

    private void drawDrosteBox() {
        gui.pushFolder("droste box");
        pg.stroke(gui.colorPicker("stroke").hex);
        pg.strokeWeight(gui.slider("stroke weight", 1));
        float boxSize = gui.slider("box size", 450);
        PVector boxRot = gui.plotXYZ("box rot", 0);
        PVector boxRotSpd = gui.plotXYZ("box rot speed", 1);
        boxRot.add(radians(boxRotSpd.x), radians(boxRotSpd.y), radians(boxRotSpd.z));
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
        PVector getPos = gui.plotXY("get pos", 0);
        PVector getSize = gui.plotXY("get size", 1000);
        img = pg.get(floor(getPos.x), floor(getPos.y), floor(getSize.x), floor(getSize.y));
        if(gui.toggle("show get")){
            pg.push();
            pg.stroke(1);
            pg.noFill();
            pg.rectMode(CORNER);
            pg.translate(0, 0);
            pg.rect(getPos.x, getPos.y, getSize.x, getSize.y);
            pg.pop();
        }
        pg.noTexture();
        gui.popFolder();
    }

    // texturedCube() source:
    // https://forum.processing.org/one/topic/box-multitextures.html
    void texturedCube(PImage tex, float size) {
        float n = size * 0.5f;
        pg.beginShape(QUADS);
        pg.texture(tex);
        pg.textureMode(NORMAL);

        // +Z "front" face
        pg.vertex(-n, -n, +n, 0, 0);
        pg.vertex(+n, -n, +n, 1, 0);
        pg.vertex(+n, +n, +n, 1, 1);
        pg.vertex(-n, +n, +n, 0, 1);

        // -Z "back" face
        pg.vertex(+n, -n, -n, 0, 0);
        pg.vertex(-n, -n, -n, 1, 0);
        pg.vertex(-n, +n, -n, 1, 1);
        pg.vertex(+n, +n, -n, 0, 1);

        // +Y "bottom" face
        pg.vertex(-n, +n, +n, 0, 0);
        pg.vertex(+n, +n, +n, 1, 0);
        pg.vertex(+n, +n, -n, 1, 1);
        pg.vertex(-n, +n, -n, 0, 1);

        // -Y "top" face
        pg.vertex(-n, -n, -n, 0, 0);
        pg.vertex(+n, -n, -n, 1, 0);
        pg.vertex(+n, -n, +n, 1, 1);
        pg.vertex(-n, -n, +n, 0, 1);

        // +X "right" face
        pg.vertex(+n, -n, +n, 0, 0);
        pg.vertex(+n, -n, -n, 1, 0);
        pg.vertex(+n, +n, -n, 1, 1);
        pg.vertex(+n, +n, +n, 0, 1);

        // -X "left" face
        pg.vertex(-n, -n, -n, 0, 0);
        pg.vertex(-n, -n, +n, 1, 0);
        pg.vertex(-n, +n, +n, 1, 1);
        pg.vertex(-n, +n, -n, 0, 1);

        pg.endShape();
    }
}

