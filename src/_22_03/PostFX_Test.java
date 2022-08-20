package _22_03;

import ch.bildspur.postfx.builder.PostFX;
import ch.bildspur.postfx.builder.PostFXBuilder;
import processing.core.PApplet;
import processing.core.PGraphics;
import lazy.LazyGui;
import processing.core.PImage;

public class PostFX_Test extends PApplet {

    // TODO expand and move GUI-PostFX adapter into global static class

    LazyGui gui;
    PGraphics pg;
    PImage testImage;
    String testImagePath = "https://upload.wikimedia.org/wikipedia/en/7/7d/Lenna_%28test_image%29.png";
    PostFX fx;

    public static void main(String[] args) {
        PApplet.main(java.lang.invoke.MethodHandles.lookup().lookupClass());
    }

    @Override
    public void settings() {
//        size(512 * 2, 512 * 2, P2D);/
        fullScreen(P2D);
        testImage = loadImage(testImagePath);
    }

    @Override
    public void setup() {
        gui = new LazyGui(this);
        pg = createGraphics(width, height, P2D);
        fx = new PostFX(this);
    }

    @Override
    public void draw() {
        pg.beginDraw();
        drawBackground();
        pg.image(testImage, 0, 0);
        pg.endDraw();
        PostFxAdapter.apply(this, gui, pg);
        image(pg, 0, 0);


    }

    @Override
    public void keyPressed() {
        if (key == 'k') {
            save("screenshots/frame.png");
        }
    }

    private void drawBackground() {
        pg.fill(gui.colorPicker("background", color(0, 0)).hex);
        pg.noStroke();
        pg.rectMode(CORNER);
        pg.rect(0, 0, width, height);
    }
}
