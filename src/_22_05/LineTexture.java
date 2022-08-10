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
        if (gui.button("capture") || key == 's') {
            saveFrame("out/LineTexture/####.jpg");
        }

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

        PVector result = new PVector();
        switch (sideIndex) {
            case 0: { // top
                result = new PVector(lerp(x, x + w, random(1)), y);
                break;
            }
            case 1: { // right
                result = new PVector(x + w, lerp(y, y + h, random(1)));
                break;
            }
            case 2: { // bottom
                result = new PVector(lerp(x, x + w, random(1)), y + h);
                break;
            }
            case 3: { // left
                result = new PVector(x, lerp(y, y + h, random(1)));
                break;
            }
        }

        return result;
    }

    private void drawRectangles() {
        float sizeX = width;
        float sizeY = height;
        pg.scale(gui.slider("scale", 1));
        pg.stroke(gui.colorPicker("rect/stroke").hex);
        pg.fill(gui.colorPicker("rect/fill").hex);
        pg.strokeWeight(gui.slider("rect/weight", 1.99f));
        pg.rect(0, 0, sizeX, sizeY);
        drawLineTexture(-sizeX / 2, -sizeY / 2, sizeX, sizeY);

    }

private void drawBackground(){
        pg.fill(gui.colorPicker("background").hex);
        pg.noStroke();
        pg.rectMode(CORNER);
        pg.rect(0,0,width,height);
        }
        }
