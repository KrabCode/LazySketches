package _23_09;

import _0_utils.Utils;
import com.krab.lazy.LazyGui;
import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;

public class WindowRecursion extends PApplet {
    LazyGui gui;
    PImage seedImage;

    public static void main(String[] args) {
        PApplet.main(java.lang.invoke.MethodHandles.lookup().lookupClass());
    }

    @Override
    public void settings() {
        size(1200, 1200, P2D);
    }

    @Override
    public void setup() {
        gui = new LazyGui(this);
        colorMode(RGB,255,255,255,100);
        getNewImage();

    }

    @Override
    public void draw() {
        Utils.record(this, gui);
        if (gui.button("reset canvas")) {
            resetCanvas();
        }
        if(gui.button("new image")){
            getNewImage();
        }
        PVector rectPos = gui.plotXY("input center");
        PVector rectSize = gui.plotXY("input size", 780);
        PImage rect = get(
                floor(width * 0.5f + rectPos.x - rectSize.x * 0.5f),
                floor(height * 0.5f + rectPos.y - rectSize.y * 0.5f),
                floor(rectSize.x),
                floor(rectSize.y)
        );
        rectMode(CENTER);
        translate(width * 0.5f, height * 0.5f);
        PVector pos = gui.plotXY("out center");
        translate(pos.x, pos.y);
        pushMatrix();
        rotate(gui.slider("out rotate", 0f));
        scale(gui.slider("out scale", 1));
        imageMode(CENTER);
        image(rect, 0, 0);
        popMatrix();
        if(gui.toggle("input debug")){
            noFill();
            stroke(255);
            strokeWeight(3);
            rect(rectPos.x, rectPos.y, rectSize.x, rectSize.y);
            circle(rectPos.x, rectPos.y, max(rectSize.x, rectSize.y));
        }
        if(gui.toggle("center debug")){
            stroke(255);
            strokeWeight(3);
            line(0,-height,0,+height);
            line(-width, 0, width, 0);
        }
    }

    private void getNewImage() {
        seedImage = loadImage("https://picsum.photos/"+width+"/"+height+".jpg");
        resetCanvas();
    }

    private void resetCanvas() {
        background(36f);
        imageMode(CORNER);
        image(seedImage, 0, 0);
    }

}
