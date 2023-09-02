package _23_09;

import _0_utils.Utils;
import com.krab.lazy.LazyGui;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PVector;

import java.awt.*;

public class ScreenshotRecursion extends PApplet {
    LazyGui gui;
    PGraphics pg;
    java.awt.Robot robot;

    public static void main(String[] args) {
        PApplet.main(java.lang.invoke.MethodHandles.lookup().lookupClass());
    }

    @Override
    public void settings() {
        float sizeMult = 1.f;
        int w = floor(displayWidth * sizeMult);
        int h = floor(displayHeight * sizeMult);
        size(w, h,P2D);
    }

    @Override
    public void setup() {
        gui = new LazyGui(this);
        pg = createGraphics(width, height, P2D);
        frameRate(144);
        try {
            robot = new Robot();
        } catch (AWTException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void draw() {
        pg.beginDraw();
        drawBackground();
        PImage screen = new PImage(robot.createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize())));
        PVector pos = gui.plotXYZ("pos");
        pg.translate(width/2f, height/2f);
        pg.translate(pos.x, pos.y);
        pg.rotate(gui.slider("rotate"));
        pg.scale(gui.slider("scale", 1));
        pg.imageMode(CENTER);
        pg.image(screen, 0, 0);
        pg.endDraw();
        image(pg, 0, 0);
        Utils.record(this, gui);
    }

    private void drawBackground() {
        pg.fill(gui.colorPicker("background").hex);
        pg.noStroke();
        pg.rectMode(CORNER);
        pg.rect(0,0,width,height);
    }
}
