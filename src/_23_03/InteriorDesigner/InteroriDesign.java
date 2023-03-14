package _23_03.InteriorDesigner;

import _0_utils.Utils;
import processing.core.PApplet;
import processing.core.PGraphics;
import lazy.LazyGui;
import processing.core.PVector;
import org.gicentre.handy.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InteroriDesign extends PApplet {
    LazyGui gui;
    HandyRenderer h;
    PGraphics pg;
    Map<String, HandyRenderer> handyRenderers = new HashMap<>();

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
        h = new HandyRenderer(this);
        h.setOverrideFillColour(true);
        h.setOverrideStrokeColour(true);
        handyRenderers.put("coloured pencil", HandyPresets.createColouredPencil(this));
        handyRenderers.put("pencil", HandyPresets.createPencil(this));
        handyRenderers.put("marker", HandyPresets.createMarker(this));

        colorMode(HSB, 1, 1, 1, 1);
        frameRate(60);
    }

    @Override
    public void draw() {
        drawBackground();
        drawRectangles();
        gui.draw();
        Utils.record(this, gui);
    }

    private void drawBackground() {
        fill(gui.colorPicker("background", color(0.1f)).hex);
        noStroke();
        rectMode(CORNER);
        rect(0, 0, width, height);
    }

    private void drawRectangles() {
        gui.pushFolder("rectangles");
        int count = gui.sliderInt("count", 1);
        int maxCount = max(100, count);
        if(gui.button("+1")){
            gui.sliderSet("count", count + 1);
        }
        boolean areRectsStatic = !gui.toggle("animated");
        PVector globalPos = gui.plotXY("global pos");
        for (int i = 0; i < maxCount; i++) {
            gui.pushFolder("#" + i);
            if(i < count){
                gui.showCurrentFolder();
            }else{
                gui.hideCurrentFolder();
                gui.popFolder();
                continue;
            }
            rectMode(CORNER);
            String label = gui.text("", "" + i);
            PVector pos = gui.plotXY("pos", width/2f, height/2f);
            PVector size = gui.plotXY("size", 100, 100);
            String selectedShape = gui.radio("shape", new String[]{"rect", "circle"});

            gui.pushFolder("style");
            String selectedPreset = gui.radio("style preset", handyRenderers.keySet().toArray(new String[]{}));
            HandyRenderer handyRenderer = handyRenderers.get(selectedPreset);
            if(gui.toggle("use preset")){
                if(areRectsStatic){
                    handyRenderer.setSeed(i);
                }
                if(selectedShape.equals("rect")){
                    handyRenderer.rect(globalPos.x + pos.x, globalPos.y + pos.y, size.x, size.y);
                }else{
                    handyRenderer.ellipse(globalPos.x + pos.x, globalPos.y + pos.y, size.x, size.y);
                }
            }else{
                if(areRectsStatic){
                    h.setSeed(i);
                }
                h.setBackgroundColour(gui.colorPicker("background", 1.9f).hex);
                h.setStrokeColour(gui.colorPicker("stroke", 0.7f).hex);
                h.setFillColour(gui.colorPicker("fill", 0.3f).hex);
                h.setStrokeWeight(gui.slider("stroke weight", 1.9f));
                h.setFillWeight(gui.slider("fill weight", 2.5f));
                h.setFillGap(gui.slider("fill gap", 7));
                h.setBowing(gui.slider("bowing", 1));
                h.setRoughness(gui.slider("roughness", 2));
                h.setHachureAngle(gui.slider("hachure angle", 3));
                h.setHachurePerturbationAngle(gui.slider("hachure angle 2", 1));
                if(selectedShape.equals("rect")){
                    rectMode(CENTER);
                    h.rect(globalPos.x + pos.x, globalPos.y + pos.y, size.x, size.y);
                }else{
                    h.ellipse(globalPos.x + pos.x, globalPos.y + pos.y, size.x, size.y);
                }
            }
            gui.popFolder();
            textAlign(LEFT, CENTER);
            textFont(gui.getMainFont());
            PVector textPos = gui.plotXY("text pos", 30, 50).sub(size.x / 2f, size.y / 2f);

            fill(color(0.15f, 0.75f));
            rectMode(CORNER);
            noStroke();
            rect(globalPos.x + pos.x + textPos.x - 6, globalPos.y + pos.y + textPos.y - 15, textWidth(label)+12, 30);
            fill(color(1));
            text(label, globalPos.x + pos.x + textPos.x, globalPos.y + pos.y + textPos.y);
            gui.popFolder();
        }
        gui.popFolder();
    }

    private boolean isMouseOverRect(int px, int py, PVector pos, PVector size) {
        float rx = pos.x;
        float ry = pos.y;
        float rw = size.x;
        float rh = size.y;
        return (px >= rx &&        // right of the left edge AND
                px <= rx + rw &&   // left of the right edge AND
                py >= ry &&        // below the top AND
                py <= ry + rh);    // above the bottom
    }

}

