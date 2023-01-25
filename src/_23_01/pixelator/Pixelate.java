package _23_01.pixelator;

import _0_utils.Utils;
import lazy.LazyGui;
import lazy.PickerColor;
import lazy.stores.FontStore;
import processing.core.*;
import processing.opengl.PShader;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Pixelate extends PApplet {
    LazyGui gui;
    PGraphics pg;
    PImage img;
    Map<Integer, PickerColor> palettes = new HashMap<Integer, PickerColor>();
    Map<Integer, PFont> fonts = new HashMap<>();
    PShader colorShiftShader;

    public static void main(String[] args) {
        PApplet.main(java.lang.invoke.MethodHandles.lookup().lookupClass());
    }

    @Override
    public void settings() {
//        size(800, 800, P2D);
        fullScreen(P2D);
    }

    @Override
    public void setup() {
        gui = new LazyGui(this);
        gui.toggleSet("options/saves/autosave on exit", true);
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
        if (img != null) {
            pg.image(img, 0, 0);
        }
        updateOutputImage();
        pg.endDraw();
        updateExport();

        imageMode(CENTER);
        image(pg, width/2f, height/2f);

//        Utils.record(this, gui);
    }

    private void refreshInputImage() {
        gui.pushFolder("input");
        String imagePath = gui.text("image path", "https://i.pinimg.com/originals/cc/d4/c7/ccd4c7adf7f3f4cdc5a2bdcd7f1d49ae.jpg");
        if (gui.button("load image") || frameCount == 1) {
            try {
                img = loadImage(imagePath);
                pg = createGraphics(img.width, img.height, P2D);
            } catch (Exception ex) {
                println(ex);
            }
        }
        gui.popFolder();
    }

    private void updateOutputImage() {
        gui.pushFolder("output");
        if (img != null) {
            gui.pushFolder("color shift");
            if(gui.toggle("active")){
                hueShift();
            }
            gui.popFolder();
            gui.pushFolder("pixelate");
            if (gui.toggle("pixelate active", true)) {
                pixelate();
            }
            gui.popFolder();
        }
        gui.popFolder();
    }

    private void updateExport() {
        gui.pushFolder("export");
        String saveImageFolder = "export/" + gui.text("folder name", "output");
        if(gui.button("open folder")){
            try {
                Desktop.getDesktop().browse(new File(saveImageFolder).toURI());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        if (gui.button("export .png")) {
            String randomId = UUID.randomUUID().toString().substring(0, 6);
            String path = saveImageFolder + "/" + randomId + ".png";
            pg.save(path);
            println("saved " + path);
        }
        gui.popFolder();
    }

    private void hueShift() {
        if(colorShiftShader == null){
            colorShiftShader = loadShader("_23_01/pixelator/hueShift.glsl");
        }
        float hueTime = gui.slider("animate hue");
        float hue =  gui.slider("hue");
        gui.sliderSet("hue", hue + radians(hueTime));
        colorShiftShader.set("hueShiftAmount", hue);
        colorShiftShader.set("satShiftAmount", gui.slider("saturation"));
        colorShiftShader.set("brShiftAmount", gui.slider("brightness"));
        pg.filter(colorShiftShader);
    }

    private void pixelate() {
        gui.pushFolder("grid");
        if (gui.toggle("active", true)) {
            pg.strokeWeight(gui.slider("stroke weight", 1));
            pg.stroke(gui.colorPicker("stroke").hex);
        } else {
            pg.noStroke();
        }
        gui.popFolder();

        gui.pushFolder("limited palette");
            boolean limitedPalette = gui.toggle("active", false);
            gui.pushFolder("text");
            boolean showNumber = gui.toggle("active", true);
            int fontSize = gui.sliderInt("text size", 10);
            int fontColor = gui.colorPicker("text fill", 0xFF000000).hex;
            PVector textOffset = gui.plotXY("text pos");
        gui.popFolder();

        int colorCount = gui.sliderInt("color count", 4);
        for (int i = 0; i < colorCount; i++) {
            palettes.put(i, gui.colorPicker("color " + i, color(norm(i, 0, colorCount - 1))));
        }
        gui.popFolder();

        pg.loadPixels();
        int pixelSize = gui.sliderInt("pixel size", 20, 2, width);
        int offsetX = gui.sliderInt("offset x");
        int offsetY = gui.sliderInt("offset y");

        for (int xi = -pixelSize * 2 + offsetX; xi < pg.width + pixelSize * 2; xi += pixelSize) {
            for (int yi = -pixelSize * 2 + offsetY; yi < pg.height + pixelSize * 2; yi += pixelSize) {
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
                    pg.textFont(getFontAtSize(fontSize));
                    pg.fill(fontColor);
                    pg.text("" + colorMatchIndex, textOffset.x + xi + pixelSize / 2f, textOffset.y + yi + pixelSize / 2f);
                }
            }
        }
    }

    private PFont getFontAtSize(int fontSize) {
        if(!fonts.containsKey(fontSize)){
            fonts.put(fontSize, createFont("JetBrainsMono-Regular.ttf", fontSize));
        }
        return fonts.get(fontSize);
    }

    private int getAverageColor(int x, int y, int w, int h) {
        float sumR = 0;
        float sumG = 0;
        float sumB = 0;
        float count = 0;
        for (int xi = max(x, 0); xi < min(x + w, pg.width); xi++) {
            for (int yi = max(y, 0); yi < min(y + h, pg.height); yi++) {
                int pixelColor = pg.pixels[xi + yi * pg.width];
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
        int closestColor = 0xFFFF0000; // red debug color
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
