package _23_03.InteriorDesigner;

import _0_utils.Utils;
import processing.core.PApplet;
import processing.core.PGraphics;
import lazy.LazyGui;
import processing.core.PVector;

public class InteroriDesign extends PApplet {
    LazyGui gui;
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
        gui = new LazyGui(this);
        pg = createGraphics(width, height, P2D);
        colorMode(HSB, 1, 1, 1, 1);
        frameRate(144);
        gui.toggleSet("options/saves/autosave on exit", false);
    }

    @Override
    public void draw() {
        pg.beginDraw();
        drawBackground();
        drawRectangles();
        pg.endDraw();
        image(pg, 0, 0);
        gui.draw();
        Utils.record(this, gui);
    }

    private void drawBackground() {
        pg.fill(gui.colorPicker("background", color(0.1f)).hex);
        pg.noStroke();
        pg.rectMode(CORNER);
        pg.rect(0, 0, width, height);
    }

    private void drawRectangles() {
        gui.pushFolder("rectangles");
        int count = gui.sliderInt("count", 1);
        int maxCount = gui.sliderInt("maxCount", 100);
        gui.sliderSet("maxCount", max(maxCount, count));
        String template = gui.radio("template", new String[]{
           "square",
           "long rect"
        });
        if(gui.button("+1")){
            gui.sliderSet("count", count + 1);
        }
        for (int i = 0; i < count; i++) {
            gui.pushFolder("#" + i);
            PVector pos = gui.plotXY("pos", width/2f, height/2f);
            PVector size;
            if(template.equals("square")){
                size = gui.plotXY("size", 300, 300);
            }else{
                size = gui.plotXY("size", 400, 200);
            }
            pg.strokeWeight(gui.slider("weight", 1.9f));
            pg.stroke(gui.colorPicker("stroke", 0.7f).hex);
            pg.fill(gui.colorPicker("fill", 0.3f).hex);
            pg.rect(pos.x, pos.y, size.x, size.y);
            pg.textAlign(LEFT, TOP);
            pg.textFont(gui.getMainFont());
            PVector textPos = gui.plotXY("text pos", 30, 10);
            pg.fill(gui.colorPicker("text fill", color(0.5f)).hex);
            pg.text(gui.text("label", "" + i), pos.x + textPos.x, pos.y + textPos.y);
            gui.popFolder();
        }
        gui.popFolder();
    }
}

