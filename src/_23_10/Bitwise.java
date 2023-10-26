package _23_10;

import com.krab.lazy.LazyGui;
import processing.core.PApplet;
import processing.core.PGraphics;

public class Bitwise extends PApplet {
    LazyGui gui;
    PGraphics pg;

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
        pg = createGraphics(width, height, P2D);
        colorMode(HSB, 1, 1, 1, 1);
        frameRate(144);
    }

    @Override
    public void draw() {
        pg.beginDraw();
        drawBackground();
        pg.fill(gui.colorPicker("foreground", color(200)).hex);
        pg.noStroke();
        // draw the grid
        float canvasSizeHalf = max(width, height)*0.5f;
        int num = gui.sliderInt("cols\\/rows", 256);
        float rectSize = width / (float) num + 1;
        pg.translate(width/2f, height/2f);
        for (int xi = 0; xi < num; xi++) {
            for (int yi = 0; yi < num; yi++) {
                float x = map(xi, 0, num - 1, -canvasSizeHalf, canvasSizeHalf);
                float y = map(yi, 0, num - 1, -canvasSizeHalf, canvasSizeHalf);
                if(isLit(xi,yi,num)){
                    pg.rect(x, y, rectSize, rectSize);
                }
            }
        }
        pg.endDraw();
        image(pg, 0, 0);
    }

    boolean isLit(int xi, int yi, int max) {
        int modulo = gui.sliderInt("%", 1, 1, 200);
//        int d = floor(dist(xi, yi, max/2f, max/2f));
        int condition = gui.sliderInt("condition", 1);
        return (xi ^ yi) % modulo > condition;
    }

    private void drawBackground() {
        pg.fill(gui.colorPicker("background").hex);
        pg.noStroke();
        pg.rectMode(CORNER);
        pg.rect(0, 0, width, height);
    }
}

