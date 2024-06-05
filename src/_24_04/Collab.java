package _24_04;

import com.krab.lazy.ShaderReloader;
import processing.core.PApplet;
import processing.core.PGraphics;
import com.krab.lazy.LazyGui;
import processing.core.PVector;

public class Collab extends PApplet {
    LazyGui gui;
    PGraphics pg;
    private float t;
    private int frame;
    public static void main(String[] args) {
        PApplet.main(java.lang.invoke.MethodHandles.lookup().lookupClass());
    }

    @Override
    public void settings() {
        // size(1080, 1080, P2D);
        fullScreen(P3D);
    }


    PVector ballPos = new PVector();
    PVector ballSpeed = new PVector();

    @Override
    public void setup() {
        gui = new LazyGui(this);
        colorMode(HSB, 1, 1, 1, 1);
        pg = createGraphics(width, height, P2D);
        pg.beginDraw();
        pg.colorMode(HSB, 1, 1, 1, 1);

        pg.endDraw();
        frameRate(60);
        ballPos = new PVector(width / 2f, height / 2f);
        ballSpeed = new PVector(random(-5, 5), random(-5, 5));
    }

    @Override
    public void draw() {
        pg.beginDraw();

        drawBackground();

        gui.pushFolder("shader");
        String shaderPath = gui.text("path", "collab_filter.glsl");
        t += radians(gui.slider("shader time +", 1));
        frame += 1;
        ShaderReloader.getShader(shaderPath).set("ball_pos", ballPos.x, ballPos.y);
        ShaderReloader.getShader(shaderPath).set("time", t);
        ShaderReloader.getShader(shaderPath).set("frame", frame);
        ShaderReloader.filter(shaderPath, pg);
        gui.popFolder();        pg.resetShader();
//        pg.translate(width*0.5f, height/2.0f);
        gui.pushFolder("circle");

        // PVector ballPos = gui.plotXY("pos");
        float diameter = gui.slider("diameter", 30);
        pg.strokeWeight(gui.slider("weight", 2));
        pg.stroke(gui.colorPicker("stroke", color(0)).hex);
        pg.fill(gui.colorPicker("fill", color(1)).hex);
        pg.circle(ballPos.x, ballPos.y, diameter);
        gui.popFolder();
        gui.popFolder();
        pg.endDraw();
        if(frameCount%2==0){
            image(pg, 0, 0);
        }
        ballPos.add(ballSpeed);
        if (ballPos.x - diameter / 2 < 0 || ballPos.x + diameter / 2 > width) ballSpeed.x *= -1;
        if (ballPos.y - diameter / 2 < 0 || ballPos.y + diameter / 2 > height) ballSpeed.y *= -1;
    }

    private void drawBackground() {
        if(gui.toggle("draw bg")){

        pg.fill(gui.colorPicker("background").hex);
        pg.noStroke();
        pg.rectMode(CORNER);
        pg.rect(0, 0, width, height);
    }
    }
}

