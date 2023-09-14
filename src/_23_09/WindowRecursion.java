package _23_09;

import _0_utils.Utils;
import com.krab.lazy.LazyGui;
import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;

import java.awt.*;

public class WindowRecursion extends PApplet {
    LazyGui gui;
    PImage seedImage;
    private Robot robot;

    public static void main(String[] args) {
        PApplet.main(java.lang.invoke.MethodHandles.lookup().lookupClass());
    }

    @Override
    public void settings() {
        size(1200, 1200, P2D);
    }

    @Override
    public void setup() {
        try {
            robot = new Robot();
        } catch (AWTException e) {
            throw new RuntimeException(e);
        }
        gui = new LazyGui(this);
        colorMode(RGB,255,255,255,100);
        getNewImage();

    }

    @Override
    public void draw() {
        Utils.record(this, gui);
        if (gui.button("reset canvas")) {
            resetCanvas();
        }
        if(gui.button("new image")){
            getNewImage();
        }
        PImage snapshot;
        if(gui.toggle("get()\\/screenshot", false)){
            gui.pushFolder("screenshot");
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            snapshot = new PImage(robot.createScreenCapture(new Rectangle(
                    gui.sliderInt("x"),
                    gui.sliderInt("y"),
                    gui.sliderInt("w", width),
                    gui.sliderInt("h", height)
            )));
            gui.popFolder();
        }else{
            PVector rectPos = gui.plotXY("get() center");
            PVector rectSize = gui.plotXY("get() size", 780);
            snapshot = get(
                    floor(width * 0.5f + rectPos.x - rectSize.x * 0.5f),
                    floor(height * 0.5f + rectPos.y - rectSize.y * 0.5f),
                    floor(rectSize.x),
                    floor(rectSize.y)
            );
        }
        rectMode(CENTER);
        translate(width * 0.5f, height * 0.5f);
        PVector pos = gui.plotXY("out center");
        translate(pos.x, pos.y);
        pushMatrix();
        rotate(gui.slider("out rotate", 0f));
        scale(gui.slider("out scale", 1));
        imageMode(CENTER);
        image(snapshot, 0, 0);
        popMatrix();
        if(gui.toggle("center debug")){
            stroke(255);
            strokeWeight(3);
            line(0,-height,0,+height);
            line(-width, 0, width, 0);
        }
    }

    private void getNewImage() {
        seedImage = loadImage("https://picsum.photos/"+width+"/"+height+".jpg");
        resetCanvas();
    }

    private void resetCanvas() {
        background(36f);
        imageMode(CORNER);
        image(seedImage, 0, 0);
    }

}
