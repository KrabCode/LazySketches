package _22_05;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;
import toolbox.Gui;

public class LineTexture extends PApplet {
    Gui gui;
    PGraphics pg;

    public static void main(String[] args) {
        PApplet.main(java.lang.invoke.MethodHandles.lookup().lookupClass());
    }

    @Override
    public void settings() {
        size(1080, 1080, P2D);
    }

    @Override
    public void setup() {
        gui = new Gui(this);
        pg = createGraphics(width, height, P2D);
    }

    @Override
    public void draw() {
        if (gui.toggle("update")) {
            pg.beginDraw();
            drawBackground();
            pg.translate(width / 2f, height / 2f);
            pg.rectMode(CENTER);
            drawRectangles();
            pg.endDraw();
        }
        image(pg, 0, 0);
        if(gui.button("capture") || key == 's'){
            saveFrame("out/LineTexture/####.jpg");
        }
        gui.themePicker();
        gui.draw();

    }

    private void drawLineTexture(float x, float y, float w, float h) {
        int maxSideIndex = 4;
        pg.stroke(gui.colorPicker("texture/stroke").hex, color(255, 20));
        pg.strokeWeight(gui.slider("texture/weight", 1));
        int linesPerSide = gui.sliderInt("texture/count", 30);
        for (int sideIndex = 0; sideIndex < maxSideIndex; sideIndex++) {
            for (int lineIndex = 0; lineIndex < linesPerSide; lineIndex++) {
                PVector me = getRandomPosOnRectSide(x, y, w, h, sideIndex);
                int randomOtherSideIndex = findRandomOtherIndex(sideIndex, maxSideIndex);
                PVector other = getRandomPosOnRectSide(x, y, w, h, randomOtherSideIndex);
                pg.line(me.x, me.y, other.x, other.y);
            }
        }
    }

    private int findRandomOtherIndex(int index, int maxIndex) {
        int randomOtherIndex = index;
        while (randomOtherIndex == index) {
            randomOtherIndex = floor(random(maxIndex));
        }
        return randomOtherIndex;
    }

    private PVector getRandomPosOnRectSide(float x, float y, float w, float h, int sideIndex) {
        switch(sideIndex){
            case 0:{ // top
                return new PVector(lerp(x,x+w,random(1)), y);
            }
            case 1:{ // right
                return new PVector(x+w, lerp(y, y+h, random(1)));
            }
            case 2:{ // bottom
                return new PVector(lerp(x,x+w,random(1)), y+h);
            }
            case 3:{ // left
                return new PVector(x, lerp(y, y+h, random(1)));
            }
        }
        return new PVector();
    }

    private void drawRectangles() {
        int xCount = gui.sliderInt("rect/count x", 10);
        int yCount = gui.sliderInt("rect/count y", 10);
        float sizeX = width / (float) xCount;
        float sizeY = height / (float) yCount;
        pg.scale(gui.slider("scale", 1));
        for (int xi = 0; xi < xCount; xi++) {
            for (int yi = 0; yi < xCount; yi++) {
                float x = map(xi, 0, xCount - 1, -width / 2f, width / 2f);
                float y = map(yi, 0, yCount - 1, -height / 2f, height / 2f);
                pg.stroke(gui.colorPicker("rect/stroke").hex);
                pg.fill(gui.colorPicker("rect/fill").hex);
                pg.strokeWeight(gui.slider("rect/weight", 1.99f));
                pg.rect(x, y, sizeX, sizeY);
                drawLineTexture(x-sizeX/2, y-sizeY/2, sizeX, sizeY);
            }
        }
    }

    private void drawBackground() {
        pg.fill(gui.colorPicker("background").hex);
        pg.noStroke();
        pg.rectMode(CORNER);
        pg.rect(0, 0, width, height);
    }
}
