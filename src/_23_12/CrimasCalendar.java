package _23_12;

import com.krab.lazy.LazyGui;
import com.krab.lazy.PickerColor;
import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PGraphics;
import processing.core.PVector;

import java.util.ArrayList;

public class CrimasCalendar extends PApplet{
    LazyGui gui;
    PGraphics pg;
    ArrayList<String> dates = new ArrayList<String>();
    PFont font;
    private int currentFontSize = -1;

    public static void main(String[] args) {
        PApplet.main(java.lang.invoke.MethodHandles.lookup().lookupClass());
    }

    @Override
    public void settings() {
//        size(1200,800,P2D);
        fullScreen(P2D);
        smooth(8);
    }

    @Override
    public void setup() {
        gui = new LazyGui(this);
        pg = createGraphics(width, height, P2D);
        pg.colorMode(HSB,1,1,1,1);
        frameRate(144);
        dates.add("3");
        dates.add("10");
        dates.add("17");
        dates.add("24");
    }

    @Override
    public void draw() {
        pg.beginDraw();
        drawBackground();
        drawRectangles();
        pg.endDraw();
        image(pg, 0, 0);
    }

    private void drawRectangles() {
        gui.pushFolder("rects");
        pg.pushMatrix();
        pg.translate(width/2f, height/2f);
        float rectSpan = gui.slider("rect span", 800);
        int length = dates.size();
        gui.pushFolder("rect style");
        int rectFillDefault = gui.colorPicker("fill default", 0.1f).hex;
        int rectFillSelected = gui.colorPicker("fill selected", 0.3f).hex;
        int rectStrokeDefault = gui.colorPicker("stroke default", 0.8f).hex;
        int rectStrokeSelected = gui.colorPicker("stroke selected", 1).hex;
        PVector rectPos = gui.plotXY("pos");
        PVector rectSize = gui.plotXY("size", 150);
        float rectRound = gui.slider("rounding", 10);
        float rectWeight = gui.slider("weight", 2);
        gui.popFolder();
        for (int i = 0; i < length; i++) {
            pg.pushMatrix();
            gui.pushFolder("checked state");
            boolean isSelected = gui.toggle(dates.get(i));
            gui.popFolder();
            float x = map(i, 0, length-1, - rectSpan/2, rectSpan/2);
            pg.translate(x, 0);
            pg.fill(isSelected ? rectFillSelected : rectFillDefault);
            pg.stroke(isSelected ? rectStrokeSelected : rectStrokeDefault);
            pg.strokeWeight(rectWeight);
            pg.rectMode(CENTER);
            pg.rect(rectPos.x, rectPos.y, rectSize.x, rectSize.y, rectRound);
            if(isSelected){
                drawLineAnimation();
            }
            drawText(i);
            pg.popMatrix();
        }
        pg.popMatrix();
        gui.popFolder();
    }

    private void drawText(int dateIndex) {
        gui.pushFolder("text");
        updateFont();
        alignText();
        pg.fill(gui.colorPicker("color", 1).hex);
        pg.textFont(font);
        PVector textPos = gui.plotXY("pos", 0, -50);
        pg.text(dates.get(dateIndex), textPos.x, textPos.y);
        gui.popFolder();
    }

    private void alignText() {
        int textAlignX = switch(gui.radio("align x", new String[]{"LEFT", "CENTER", "RIGHT"})){
            case "LEFT" -> LEFT;
            case "RIGHT" -> RIGHT;
            default -> CENTER;
        };
        int textAlignY = switch(gui.radio("align y", new String[]{"TOP", "CENTER", "BOTTOM"})){
            case "TOP" -> TOP;
            case "BOTTOM" -> BOTTOM;
            default -> CENTER;
        };
        pg.textAlign(textAlignX, textAlignY);
    }

    private void updateFont() {
        int intendedFontSize = gui.sliderInt("font size", 20, 4, 1000);
        if(currentFontSize != intendedFontSize){
            font = createFont("JetBrainsMono-Regular.ttf", intendedFontSize);
            currentFontSize = intendedFontSize;
        }
    }

    private void drawLineAnimation() {
        gui.pushFolder("line animation");
        float t = radians(frameCount) * gui.slider("speed", 1);
        float startY = gui.slider("y", 0);
        float height = -gui.slider("height", 120);
        float detail = gui.sliderInt("detail", 10);
        pg.noFill();
        pg.strokeWeight(gui.slider("stroke weight", 2));
        pg.stroke(gui.colorPicker("stroke", 1).hex);
        float freq = gui.slider("freq", 0.1f);
        float amp = gui.slider("amp", 10);
        int phaseOffsetCount = gui.sliderInt("phase offset count", 3);
        for(int phaseOffsetIndex = 0; phaseOffsetIndex < phaseOffsetCount; phaseOffsetIndex++){
            float phaseOffset = map(phaseOffsetIndex, 0, phaseOffsetCount, 0, TWO_PI);
            float tinyYoffset = gui.slider("tiny y offset", 1) * sin(phaseOffset);
            pg.beginShape();
            for(float i = 0; i < detail; i++) {
                float y = map(i, 0, detail-1, startY, startY + height) + tinyYoffset;
                float x = sin(y * freq + t + phaseOffset) * amp;
                pg.vertex(x, y);
            }
            pg.endShape();
        }
        gui.popFolder();
    }

    private void drawBackground() {
        pg.fill(gui.colorPicker("background").hex);
        pg.noStroke();
        pg.rectMode(CORNER);
        pg.rect(0,0,width,height);
    }
}
