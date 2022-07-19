package _22_07;

import processing.core.PApplet;
import processing.core.PGraphics;
import toolbox.Gui;

public class LineSpiral extends PApplet {
    Gui gui;
    PGraphics pg;
    float rotateTime;
    int frame = 1;

    public static void main(String[] args) {
        PApplet.main(java.lang.invoke.MethodHandles.lookup().lookupClass());
    }

    @Override
    public void settings() {
        size(800, 800, P2D);
    }

    @Override
    public void setup() {
        gui = new Gui(this);
        pg = createGraphics(width, height, P2D);
    }

    @Override
    public void draw() {
        pg.beginDraw();
        pg.blendMode(BLEND);
        drawBackground();
        if(gui.stringPicker("blend mode", new String[]{"add", "blend"}).equals("add")){
            pg.blendMode(ADD);
        }
        drawSpiral();
        pg.endDraw();
        image(pg, 0, 0);
        if(gui.toggle("saving 2", false)){
            pg.save("out/spirals 2/" + frame++ + ".jpg");
        }
        gui.themePicker();
        gui.draw();
    }

    private void drawBackground() {
//        pg.fill(gui.colorPicker("background").hex);
//        pg.noStroke();
//        pg.rectMode(CORNER);
//        pg.rect(0, 0, width, height);
        pg.image(gui.gradient("bg"), 0, 0);
    }

    private void drawSpiral() {
        rotateTime += radians(gui.slider("lines/rotate time"));
        float rotateNorm = gui.slider("lines/rotate norm");
        int lineCount = gui.sliderInt("lines/count", 10);
        float lineLengthBase = gui.slider("lines/length base", 2);
        float lineLengthNorm = gui.slider("lines/length norm", 5);
        float distanceStep = gui.slider("lines/distance", 1);
        float angleStep = gui.slider("lines/angle", radians(1));
        float distance = 0;
        float angle = 0;
        float centerX = gui.slider("lines/center x", width / 2f);
        float centerY = gui.slider("lines/center y", height / 2f);
        pg.stroke(gui.colorPicker("lines/stroke").hex);
        float weightBase = gui.slider("lines/weight base", 2);
        float weightNorm = gui.slider("lines/weight norm", 1);
        for (int i = 0; i < lineCount; i++) {
            float norm = norm(i, 0, lineCount - 1);
            distance += distanceStep;
            angle += angleStep;
            pg.pushMatrix();
            pg.translate(centerX + distance * cos(angle), centerY + distance * sin(angle));
            pg.rotate(rotateNorm * norm + rotateTime);
            pg.strokeWeight(weightBase + weightNorm * norm);
            float lineLength = lineLengthBase + lineLengthNorm * norm;
            pg.line(-lineLength, 0, lineLength, 0);
            pg.popMatrix();
        }
    }
}