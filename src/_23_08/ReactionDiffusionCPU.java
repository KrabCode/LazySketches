package _23_08;

import com.krab.lazy.LazyGui;
import javafx.scene.control.Cell;
import processing.core.PApplet;
import processing.core.PGraphics;

public class ReactionDiffusionCPU extends PApplet {

    Cell[][] canvas;
    LazyGui gui;
    PGraphics pg;

    float diffA, diffB, feed, kill, time;

    int scaleMult = 3;
    int w;
    int h;

    public static void main(String[] args) {
        PApplet.main(java.lang.invoke.MethodHandles.lookup().lookupClass());
    }

    @Override
    public void settings() {
        size(800, 800, P2D);
    }

    @Override
    public void setup() {
        w = width / scaleMult;
        h = height / scaleMult;
        gui = new LazyGui(this);
        pg = createGraphics(w, h, P2D);
        pg.beginDraw();
        pg.clear();
        pg.endDraw();
        pg.colorMode(RGB, 1, 1, 1, 1);
        colorMode(RGB, 1, 1, 1, 1);
        canvas = new Cell[w][h];
    }

    @Override
    public void draw() {
        if(frameCount == 1 || gui.button("reset")){
            float initDiameter = gui.slider("init diam", 60);
            initCanvas(initDiameter);
        }
        updateParams();
        updateCanvas();
        displayCanvasOnPGraphics(pg);
        background(gui.colorPicker("background").hex);
        imageMode(CENTER);
        translate(width/2f, height/2f);
        scale(gui.slider("scale", scaleMult));
        image(pg, 0, 0);
    }

    private void updateParams() {
        gui.pushFolder("update");
        diffA = gui.slider("diff A", 1);
        diffB = gui.slider("diff B", 0.5f);
        feed = gui.slider("feed", 0.0545f);
        kill = gui.slider("kill", 0.062f);
        time = gui.slider("time", 1);
        gui.popFolder();
    }

    private void updateCanvas() {
        gui.pushFolder("update");
        if (!gui.button("update once") && !gui.toggle("keep updating")) {
            gui.popFolder();
            return;
        }
        Cell[][] writeOnly = new Cell[w][h];
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                writeOnly[x][y] = getUpdatedCell(x,y);
            }
        }
        canvas = writeOnly;

        gui.popFolder();
    }

    private Cell getUpdatedCell(int x, int y) {
        Cell readOnly = canvas[x][y];
        float localA = readOnly.a;
        float localB = readOnly.b;
        float neighbourAverageA = - localA +
                getValueEdgeAwareA(x+1,y) * 0.2f +
                getValueEdgeAwareA(x-1,y) * 0.2f +
                getValueEdgeAwareA(x,y+1) * 0.2f +
                getValueEdgeAwareA(x,y-1) * 0.2f +
                getValueEdgeAwareA(x+1,y+1) * 0.05f +
                getValueEdgeAwareA(x+1,y-1) * 0.05f +
                getValueEdgeAwareA(x-1,y+1) * 0.05f +
                getValueEdgeAwareA(x-1,y-1) * 0.05f;
        float neighbourAverageB = - localB +
                getValueEdgeAwareB(x+1,y) * 0.2f +
                getValueEdgeAwareB(x-1,y) * 0.2f +
                getValueEdgeAwareB(x,y+1) * 0.2f +
                getValueEdgeAwareB(x,y-1) * 0.2f +
                getValueEdgeAwareB(x+1,y+1) * 0.05f +
                getValueEdgeAwareB(x+1,y-1) * 0.05f +
                getValueEdgeAwareB(x-1,y+1) * 0.05f +
                getValueEdgeAwareB(x-1,y-1) * 0.05f;
        float ABB = localA * localB * localB;
        Cell writeOnly = new Cell();
        writeOnly.a = localA + (diffA * neighbourAverageA - ABB + feed * (1 - localA)) * time;
        writeOnly.b = localB + (diffB * neighbourAverageB + ABB - (kill + feed) * localB) * time;
        writeOnly.constrain();
        return writeOnly;
    }

    private float getValueEdgeAwareA(int x, int y){
        if(isBeyondEdge(x,y)){
            return 0;
        }
        return canvas[x][y].a;
    }

    private float getValueEdgeAwareB(int x, int y){
        if(isBeyondEdge(x,y)){
            return 0;
        }
        return canvas[x][y].b;
    }

    boolean isBeyondEdge(int x, int y){
        return x < 0 || x >= w || y < 0 || y >= h;
    }

    private void displayCanvasOnPGraphics(PGraphics pg) {
        if (frameCount % gui.sliderInt("frame skip", 60) != 0) {
            return;
        }
        pg.beginDraw();
        pg.loadPixels();
        float exp = gui.slider("exp", 1);
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                int i = x + y * w;
                Cell cell = canvas[x][y];
                pg.pixels[i] = color(pow(cell.b, exp));
            }
        }
        pg.updatePixels();
        pg.endDraw();
    }

    private void drawBackground() {
        pg.fill(gui.colorPicker("background").hex);
        pg.noStroke();
        pg.rectMode(CORNER);
        pg.rect(0, 0, width, height);
    }

    private void initCanvas(float size) {
        float rectX = w / 2f - size / 2;
        float rectY = h / 2f - size / 2;
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                Cell cell = new Cell();
//                if (dist(x, y, w / 2f, h / 2f) < size) {
//                    cell.b = 1.f;
//                }
                if(pointRect(x,y,rectX,rectY,size,size)){
                    cell.b = 1.f;
                }
                canvas[x][y] = cell;
            }
        }
    }
    // POINT/RECTANGLE
    boolean pointRect(float px, float py, float rx, float ry, float rw, float rh) {
        return px >= rx &&
                px <= rx + rw &&
                py >= ry &&
                py <= ry + rh;
    }

    static class Cell {
        float a = 1;
        float b = 0;

        public Cell(){

        }

        public Cell(int a, int b) {
            this.a = a;
            this.b = b;
        }

        public void constrain() {
            if(a < 0){
                a = 0;
            }
            if(a > 1){
                a = 1;
            }
            if(b < 0){
                b = 0;
            }
            if(b > 1){
                b = 1;
            }

        }
    }
}
