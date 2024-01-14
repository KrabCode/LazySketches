package _24_01;

import _0_utils.TexturedShapes;
import _0_utils.Utils;
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
       // size(1000, 1000, P3D);
        fullScreen(P3D, 1);
        smooth(8);
    }

    @Override
    public void setup() {
        gui = new LazyGui(this);
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
            TexturedShapes.box(pg, img, boxSize);
        }
        pg.popMatrix();
        PVector getPos = gui.plotXY("get() pos", width/2f, height/2f);
        PVector getSize = gui.plotXY("get() size", 1000);
        img = pg.get(
                floor(getPos.x-getSize.x*0.5f),
                floor(getPos.y-getSize.y*0.5f),
                floor(getSize.x),
                floor(getSize.y)
        );
        if(gui.toggle("show get rect")){
            pg.push();
            pg.stroke(gui.colorPicker("get rect stroke").hex);
            pg.strokeWeight(gui.slider("get rect weight", 1));
            pg.noFill();
            pg.rectMode(CENTER);
            pg.translate(0, 0);
            pg.rect(getPos.x, getPos.y, getSize.x, getSize.y);
            pg.pop();
        }
        pg.noTexture();
        gui.popFolder();
    }

}

