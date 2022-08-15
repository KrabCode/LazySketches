package _22_08;

import processing.core.PApplet;
import processing.core.PGraphics;
import lazy.LazyGui;
import processing.core.PImage;

import java.util.Arrays;

// TODO implement something like this here:
// https://github.com/kimasendorf/ASDFPixelSort/blob/master/ASDFPixelSort.pde

public class PixelSorting extends PApplet {
    LazyGui gui;
    PGraphics pg;
    PImage img;

    public static void main(String[] args) {
        PApplet.main(java.lang.invoke.MethodHandles.lookup().lookupClass());
    }

    @Override
    public void settings() {
        size(1080, 1080, P2D);
    }

    @Override
    public void setup() {
        gui = new LazyGui(this);
        pg = createGraphics(width, height, P2D);
        img = loadImage("_22_08/rocks.jpg");
        colorMode(RGB,1,1,1,1);
    }

    @Override
    public void draw() {
        pg.beginDraw();
        pg.translate(pg.width/2f, pg.height/2f);
        pg.tint(gui.colorPicker("background/image tint", color(1)).hex);
        pg.imageMode(CENTER);
        pg.scale(gui.slider("background/image scale", 1));
        pg.image(img, 0,0);
        applyPixelSortEffect();
        pg.endDraw();
        clear();
        image(pg, 0, 0);
        gui.draw();
    }

    private void applyPixelSortEffect() {
        pg.loadPixels();
        // TODO
        pg.updatePixels();
    }

    int getColorAt(int[] pixels, int x, int y){
        return pixels[x + y * width];
    }

    void setColorTo(int[] pixels, int x, int y, int colorToSet){
        pixels[x + y * width] = colorToSet;
    }

}

