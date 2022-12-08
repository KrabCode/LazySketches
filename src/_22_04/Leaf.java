package _22_04;

import processing.core.PApplet;
import processing.core.PGraphics;
import lazy.LazyGui;

public class Leaf extends PApplet {LazyGui gui;
    PGraphics pg;
    private float shapeSize;
    private int childCount;
    private int generationCount;
    float t;

    public static void main(String[] args) {
        PApplet.main(java.lang.invoke.MethodHandles.lookup().lookupClass());
    }

    @Override
    public void settings() {
        size(1080, 1080, P2D);
    }

    @Override
    public void setup() {
        frameRate(144);
        gui = new LazyGui(this);
        pg = createGraphics(width, height, P2D);
    }

    @Override
    public void draw() {
        t += radians(gui.slider("time speed", 1));
        pg.beginDraw();
        surface.setTitle("fps: " + frameRate);
        drawBackground();
        drawLeaf();
        pg.endDraw();
        image(pg, 0, 0);
//
    }

    @Override
    public void keyPressed() {
        if (key == 'i') {
            pg.save("screenshots/" + frameCount + ".png");
        }
    }

    private void drawBackground() {
        pg.rectMode(CORNER);
        pg.fill(gui.colorPicker("background").hex);
        pg.noStroke();
        pg.rect(0, 0, width, height);
    }

    private void drawLeaf() {
        pg.translate(gui.slider("leaf/x", width / 2f), gui.slider("leaf/y", height / 2f));
        pg.stroke(gui.colorPicker("leaf/stroke").hex);
        pg.fill(gui.colorPicker("leaf/fill").hex);
        pg.rectMode(CENTER);

        shapeSize = gui.slider("leaf/size", 100);
        childCount = gui.sliderInt("fractal/step count", 3);
        generationCount = gui.sliderInt("fractal/gen count", 3);
        drawLeafRecursively(0);
    }

    private void drawLeafRecursively(int gen) {
        if (gen >= generationCount) {
            return;
        }

        for (int i = 0; i <= childCount; i++) {
            pg.pushMatrix();
            pg.rotate(gui.slider("fractal/steps/" + i + "/rotate"));
            float x = gui.slider("fractal/steps/" + i + "/x");
            float y = gui.slider("fractal/steps/" + i + "/y");
            float mag = gui.slider("fractal/steps/"+i+"/mag");
            float spd = t * gui.slider("fractal/steps/"+i+"/speed", 1);
            x += mag*cos(t*spd);
            y += mag*sin(t*spd);
            pg.translate(x, y);
            pg.scale(gui.slider("fractal/steps/" + i + "/scale", 1));
            pg.fill(gui.colorPicker("fractal/steps/" + i + "/color", color(255)).hex);
            if(gui.radio("leaf/shape", new String[]{"rect", "ellipse"}).equals("rect")){

                pg.rect(0, 0, shapeSize, shapeSize);
            }else{
                pg.ellipse(0,0, shapeSize, shapeSize);
            }
            drawLeafRecursively(gen + 1);
            pg.popMatrix();
        }

    }


}
