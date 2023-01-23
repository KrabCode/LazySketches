package _23_01;

import _0_utils.Utils;
import lazy.LazyGui;
import lazy.PickerColor;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PVector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Pixelate extends PApplet {
    LazyGui gui;
    PGraphics pg;
    PImage img;
    Map<Integer, PickerColor> palettes = new HashMap<Integer, PickerColor>();

    public static void main(String[] args) {
        PApplet.main(java.lang.invoke.MethodHandles.lookup().lookupClass());
    }

    @Override
    public void settings() {
        size(800, 800, P2D);
    }

    @Override
    public void setup() {
        gui = new LazyGui(this);
        pg = createGraphics(width, height, P2D);
        pg.beginDraw();
        pg.textSize(128);
        pg.endDraw();
        colorMode(RGB, 1, 1, 1, 1);
    }

    @Override
    public void draw() {
        drawBackground();
        refreshInputImage();
        pg.beginDraw();
        pg.colorMode(RGB, 1, 1, 1, 1);
        updateOutputImage();
        pg.endDraw();
        if (gui.button("output/save image")) {
            String path = "out/" + Utils.generateRandomShortId() + ".png";
            pg.save(path);
            println("saved " + path);
        }
        translate(width / 2f, height / 2f);
        imageMode(CENTER);
        scale(gui.slider("output/display scale", 1));
        image(pg, 0, 0);
    }

    private void refreshInputImage() {
        gui.pushFolder("input");
        String imagePath = gui.text("image path", "https://i.pinimg.com/originals/cc/d4/c7/ccd4c7adf7f3f4cdc5a2bdcd7f1d49ae.jpg");
        if (gui.button("load image") || frameCount == 1) {
            try {
                img = loadImage(imagePath);
            } catch (Exception ex) {
                println(ex);
            }
        }
        gui.popFolder();
    }

    private void updateOutputImage() {
        gui.pushFolder("output");
        if (img != null) {
            if (!gui.toggle("view input\\/output", true)) {
                pg.image(img, 0, 0);
            } else {
                pixelate();
            }
        }
        gui.popFolder();
    }

    private void pixelate() {
        gui.pushFolder("grid");
        pg.strokeWeight(gui.slider("stroke weight", 1));
        pg.stroke(gui.colorPicker("stroke").hex);
        if (gui.toggle("no stroke", true)) {
            pg.noStroke();
        }
        gui.popFolder();
        gui.pushFolder("limited palette");
        boolean limitedPalette = gui.toggle("active", true);
        int colorCount = gui.sliderInt("color count", 4);

        boolean showNumber = gui.toggle("show number", true);
        float fontSize = gui.slider("text size", 10);
        int fontColor = gui.colorPicker("text fill", 0xFF000000).hex;
        PVector textOffset = gui.plotXY("text pos");
        for (int i = 0; i < colorCount; i++) {
            palettes.put(i, gui.colorPicker("color " + i, color(norm(i, 0, colorCount - 1))));
        }
        gui.popFolder();

        img.loadPixels();
        int pixelSize = gui.sliderInt("pixel size", 20, 2, width);
        int offsetX = gui.sliderInt("offset x");
        int offsetY = gui.sliderInt("offset y");

        for (int xi = -pixelSize * 2 + offsetX; xi < img.width + pixelSize * 2; xi += pixelSize) {
            for (int yi = -pixelSize * 2 + offsetY; yi < img.height + pixelSize * 2; yi += pixelSize) {
                int avg = getAverageColor(xi, yi, pixelSize, pixelSize);
                int clr = avg;
                int colorMatchIndex = -1;
                if (limitedPalette) {
                    int[] closestColor = findClosestColorInPalette(avg, colorCount);
                    colorMatchIndex = closestColor[0];
                    clr = closestColor[1];
                }
                pg.fill(clr);
                pg.rect(xi, yi, pixelSize, pixelSize);
                if (limitedPalette && showNumber) {
                    pg.textAlign(CENTER, CENTER);
                    pg.textSize(fontSize);
                    pg.fill(fontColor);
                    pg.text("" + colorMatchIndex, textOffset.x + xi + pixelSize / 2f, textOffset.y + yi + pixelSize / 2f);
                }
            }
        }
    }

    private int getAverageColor(int x, int y, int w, int h) {
        float sumR = 0;
        float sumG = 0;
        float sumB = 0;
        float count = 0;
        for (int xi = max(x, 0); xi < min(x + w, img.width); xi++) {
            for (int yi = max(y, 0); yi < min(y + h, img.height); yi++) {
                int pixelColor = img.pixels[xi + yi * img.width];
                sumR += red(pixelColor);
                sumG += green(pixelColor);
                sumB += blue(pixelColor);
                count++;
            }
        }
        if(count == 0){
            return color(0);
        }
        return color(sumR / count, sumG / count, sumB / count);
    }

    private int[] findClosestColorInPalette(int queryColor, int colorCount) {
        float closestDistance = Float.MAX_VALUE;
        int closestColor = 0xFFFF0000;
        int closestColorIndex = -1;
        for (int i = 0; i < colorCount; i++) {
            PVector queryPoint = new PVector(red(queryColor), green(queryColor), blue(queryColor));
            PickerColor candidate = palettes.get(i);
            PVector matchPoint = new PVector(red(candidate.hex), green(candidate.hex), blue(candidate.hex));
            float distance = PVector.dist(queryPoint, matchPoint);
            if (distance < closestDistance) {
                closestDistance = distance;
                closestColor = candidate.hex;
                closestColorIndex = i;
            }
        }
        return new int[]{closestColorIndex, closestColor};
    }

    private void drawBackground() {
        fill(gui.colorPicker("background", 0xFF303030).hex);
        noStroke();
        rectMode(CORNER);
        rect(0, 0, width, height);
    }
}
