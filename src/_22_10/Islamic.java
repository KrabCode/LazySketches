package _22_10;

import lazy.LazyGui;
import lazy.Utils;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PVector;
import processing.video.Movie;

public class Islamic extends PApplet {

    private PGraphics tex;
    private int imageCount = 6;
    private PVector off = new PVector();
    private float rotationTime;

    private int hexCountX;
    private int hexCountY;
    private float equilateralTriangleHeight = sqrt(3) / 2;

    private int recStarted = -1;
    private int recLength;
    private float timeMultiplier = 3;

    Movie movie;
    LazyGui gui;
    private String sketchInstanceId;
    private int saveIndex;
    private float size = -1;
    private float hexWidth;
    private float hexHeight;
    private float xStep;
    private float yStep;

    public static void main(String[] args) {
        PApplet.main(java.lang.invoke.MethodHandles.lookup().lookupClass());
    }

    public void settings() {
//        size(800, 800, P2D);
        fullScreen(P2D);
    }

    public void setup() {
        gui = new LazyGui(this);
        updateSizes();
        movie = new Movie(this, "C:\\Users\\Krab\\Documents\\GitHub\\LazySketches\\resources_big\\videos\\sunset_tree.mp4");
        movie.loop();
    }

    public void updateSizes() {
        size = gui.slider("hex/size", 100);
        resetTexture();
        hexWidth = size * 2;
        hexHeight = sqrt(3) * size;
        xStep = hexWidth * (3 / 4f);
        yStep = hexHeight;
        hexCountX = gui.sliderInt("hex/count x", 10, 0, Integer.MAX_VALUE);
        hexCountY = gui.sliderInt("hex/count y", 21, 0, Integer.MAX_VALUE);
    }

    public void resetTexture(){
        tex = createGraphics(floor(size), floor(size), P2D);
    }

    public void draw() {
        rotationTime += radians(gui.slider("texture/rot speed"));
        updateSizes();
        movieControl();
        drawBackground();
        updateTexture();
        drawHexagonGrid();
        record();
        off.x = gui.slider("texture/offset x");
        off.y = gui.slider("texture/offset y");
        if (gui.toggle("texture/debug triangle")) {
            drawTextureForDebugging();
        }
        if (gui.toggle("movie/debug movie")) {
            image(movie, gui.slider("movie/debug x"), gui.slider("movie/debug y"));
        }
        if(gui.button("texture/reset")){
            resetTexture();
        }
    }

    public void movieEvent(Movie m) {
        m.read();
    }

    private void drawHexagonGrid() {
        pushMatrix();
        translate(gui.slider("hex/offset x"), gui.slider("hex/offset y"));
        for (float xi = -hexCountX / 2f; xi <= hexCountX / 2f; xi++) {
            for (float yi = -hexCountY / 2f; yi <= hexCountY / 2f; yi++) {
                float x = width / 2f + xi * xStep;
                float y = height / 2f + yi * yStep - yStep / 2f;
                if (xi % 2 == 0) {
                    y += yStep / 2f;
                }
                pushMatrix();
                translate(x, y);
                drawHexagon();
                popMatrix();
            }
        }
        popMatrix();
    }

    private void drawHexagon() {
        int cornerCount = 6;
        for (int triangleIndex = 0; triangleIndex <= cornerCount; triangleIndex++) {
            float angle0 = map(triangleIndex, 0, cornerCount, 0, TWO_PI);
            float angle1 = map(triangleIndex + 1, 0, cornerCount, 0, TWO_PI);
            float x0 = size * cos(angle0);
            float y0 = size * sin(angle0);
            float x1 = size * cos(angle1);
            float y1 = size * sin(angle1);
            beginShape();
            noStroke();
            if (gui.toggle("hex/debug stroke")) {
                strokeWeight(gui.slider("hex/debug weight", 2));
                stroke(gui.colorPicker("hex/debug color", color(255)).hex);
            }
            textureMode(NORMAL);
            texture(tex);
            vertex(0, 0, 0.5f, 1 - equilateralTriangleHeight);
            if (triangleIndex % 2 == 0) { // mirror the texture every second triangle
                vertex(x0, y0, 0, 1);
                vertex(x1, y1, 1, 1);
            } else {
                vertex(x0, y0, 1, 1);
                vertex(x1, y1, 0, 1);
            }
            endShape();
        }
    }

    private void updateTexture() {
        float w = tex.width;
        float h = tex.height;
        tex.beginDraw();
        tex.colorMode(HSB, 1, 1, 1, 1);
        tex.background(0);
        tex.imageMode(CENTER);
        tex.translate(w / 2, h / 2);
        tex.rotate(rotationTime);
        tex.tint(1, 1);
        tex.image(movie, off.x, off.y);
        tex.endDraw();
    }

    private void drawTextureForDebugging() {
        pushMatrix();
        translate(gui.slider("texture/triangle x"), gui.slider("texture/triangle y"));
        rectMode(CENTER);
        noStroke();
        fill(0);
        rect(0, 0, tex.width * 1.2f, tex.height * 1.2f);
        imageMode(CENTER);
        image(tex, 0, 0);
        noFill();
        strokeWeight(3);
        stroke(255, 0, 0);
        triangle(0, -(tex.height * equilateralTriangleHeight) / 2, -tex.width / 2f, tex.height / 2f, tex.width / 2f, tex.height / 2f);
        popMatrix();
    }


    private void movieControl() {
        if (gui.button("movie/play")) {
            movie.play();
        }
        if (gui.button("movie/loop")) {
            movie.loop();
        }
        if (gui.button("movie/stop")) {
            movie.stop();
        }
    }

    private void drawBackground() {
        fill(gui.colorPicker("background").hex);
        noStroke();
        rectMode(CORNER);
        rect(0, 0, width, height);
    }


    private void record() {
        recLength = gui.sliderInt("rec/frames", 600);
        if (gui.button("rec/start")) {
            recStarted = frameCount;
        }
        if (gui.button("rec/stop")) {
            sketchInstanceId = Utils.generateRandomShortId();
            recStarted = -1;
        }
        int recordRectPosX = gui.sliderInt("rec/rect pos x");
        int recordRectPosY = gui.sliderInt("rec/rect pos y");
        int recordRectSizeX = gui.sliderInt("rec/rect size x", width);
        int recordRectSizeY = gui.sliderInt("rec/rect size y", height);
        if (recStarted != -1 && frameCount < recStarted + recLength) {
            println("rec " + saveIndex + " / " + recLength);
            PImage cutout = get(recordRectPosX, recordRectPosY, recordRectSizeX, recordRectSizeY);
            cutout.save("out/recorded images/PixelSorting_" + sketchInstanceId + "/" + saveIndex++ + ".png");
        }
        if (gui.toggle("rec/show rect")) {
            stroke(255);
            noFill();
            rect(recordRectPosX, recordRectPosY, recordRectSizeX, recordRectSizeY);
        }
    }

}
