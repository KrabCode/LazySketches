package _23_08;

import com.krab.lazy.LazyGui;
import com.krab.lazy.ShaderReloader;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;
import processing.opengl.PShader;

public class ReactionDiffusionCPU extends PApplet {

    Cell[][] canvas;
    LazyGui gui;
    PGraphics pg;

    float diffA, diffB, feed, kill;

    float scaleMult = 0.33f;
    int w;
    int h;
    private float timeA, timeB;

    public static void main(String[] args) {
        PApplet.main(java.lang.invoke.MethodHandles.lookup().lookupClass());
    }

    @Override
    public void settings() {
        size(800, 800, P2D);
    }

    @Override
    public void setup() {
        colorMode(RGB, 1, 1, 1, 1);
        gui = new LazyGui(this);
        initPGraphics();
        frameRate(144);
    }

    @Override
    public void draw() {
        if(frameCount == 1 || gui.button("reset")){
            initPGraphics();
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

    private void initPGraphics() {
        PVector size = gui.plotXY("canvas size", 200);
        if(w != size.x || h != size.y){
            pg = createGraphics(floor(size.x), floor(size.y), P2D);
            pg.beginDraw();
            pg.endDraw();
            pg.colorMode(RGB, 1, 1, 1, 1);
        }
        w = floor(size.x);
        h = floor(size.y);
    }

    private void updateParams() {
        gui.pushFolder("update");
        gui.toggle("active");
        diffA = gui.slider("diff A", 1);
        diffB = gui.slider("diff B", 0.5f);
        feed = gui.slider("feed", 0.061f);
        kill = gui.slider("kill", 0.06264f);
        timeA = gui.slider("time A", 0.5f);
        timeB = gui.slider("time B", 1);
        gui.popFolder();
    }

    private void updateCanvas() {
        gui.pushFolder("update");
        if (!gui.toggle("active")) {
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

        float distanceFromCenter = norm(dist(x,y, w/2f, h/2f), 0, w/2f);
        float angleFromCenter = norm(atan2(y - h/2f, x - w/2f), - PI, PI);
        float angleFromCenterWave = 0.5f+0.5f*sin(3 * atan2(y - h/2f, x - w/2f));
        float n = angleFromCenterWave; // can be any of the above
        float deltaTime = lerp(timeA, timeB, n);

        Cell writeOnly = new Cell();
        writeOnly.a = localA + (diffA * neighbourAverageA - ABB + feed * (1 - localA)) * deltaTime;
        writeOnly.b = localB + (diffB * neighbourAverageB + ABB - (kill + feed) * localB) * deltaTime;
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
        if (frameCount % gui.sliderInt("frame skip", 3, 1, 1000) != 0) {
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
        String displayShaderPath = "_23_08/RD/display_for_CPU.glsl";
        PShader displayShader = ShaderReloader.getShader(displayShaderPath);
        displayShader.set("source",  pg);
        displayShader.set("gradient", gui.gradient("gradient"));
        ShaderReloader.filter(displayShaderPath, pg);
        pg.endDraw();
    }

    private void drawBackground() {
        pg.fill(gui.colorPicker("background").hex);
        pg.noStroke();
        pg.rectMode(CORNER);
        pg.rect(0, 0, width, height);
    }

    private void initCanvas(float size) {
        canvas = new Cell[w][h];
        float rectX = w / 2f - size / 2;
        float rectY = h / 2f - size / 2;
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                Cell cell = new Cell();
//                if (dist(x, y, w / 2f, h / 2f) < size) {
//                    cell.b = 1.f;
//                }
                if(pointRect(x,y,rectX,rectY,size,size)){
                    cell.b = random(1);
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
