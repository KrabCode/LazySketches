package _22_09;

import _0_utils.Utils;
import jdk.nashorn.internal.objects.Global;
import com.krab.lazy.LazyGui;
import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PGraphics;
import processing.core.PImage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AsciiArt extends PApplet {

    // make an array of available characters
    // get a monospaced font
    // print each char to its own canvas and count the total brightness
    // sort characters by total brightness
    // convert a photo into ascii art
    // animate each character using 2D noise, change chars to adjacent brightness chars in waves

    private PGraphics pg;
    private LazyGui gui;

    int boxWidth = 50;
    int boxHeight = 50;
    float currentFontSize = 30;

    PFont font;

    char[] availableChars = "!\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~".toCharArray();
    List<PGraphics> charCanvases = new ArrayList<PGraphics>();

    PImage photo;
    PGraphics asciiPhoto;

    float noiseX, noiseY, t;
    private int recLength = 600, recStarted = -1, saveIndex;
    String sketchInstanceId = Utils.generateRandomShortId();


    public static void main(String[] args) {
        PApplet.main(java.lang.invoke.MethodHandles.lookup().lookupClass());
    }

    public void settings() {
        size(1000, 1000, P2D);
    }

    public void setup() {
        pg = createGraphics(width, height, P2D);
        gui = new LazyGui(this);
        font = createFont(currentFontSize);
        photo = loadImage("C:\\Users\\jakub Rak\\Desktop\\cat.JPG");
        asciiPhoto = createGraphics(photo.width, photo.height, P2D);
    }

    public void draw() {
        pg.beginDraw();
        pg.background(gui.colorPicker("background/color").hex);
        updateDrawAsciiResources();
        updateDrawPhoto();
        pg.endDraw();
        image(pg, 0, 0, width, height);
        record();
    }

    private void updateDrawPhoto() {
        // draw image
        pg.pushMatrix();
        pg.pushStyle();
        pg.imageMode(CENTER);
        pg.translate(gui.slider("photo/x", width / 2f), gui.slider("photo/y", height / 2f));
        pg.rotate(gui.slider("photo/rotate"));
        pg.scale(gui.slider("photo/scale", 1));
        pg.tint(gui.colorPicker("photo/original tint", color(255)).hex);
        if(gui.toggle("photo/display original")){
            pg.image(photo, 0, 0);
        }
        pg.tint(gui.colorPicker("photo/filtered tint", color(255)).hex);
        if(gui.toggle("photo/display filtered")){
            updateAsciiPhotoFilter();
            pg.image(asciiPhoto, 0, 0);
        }
        pg.popStyle();
        pg.popMatrix();

    }

    private void updateAsciiPhotoFilter() {
        asciiPhoto.beginDraw();
        asciiPhoto.clear();
        replaceCanvasContentsWithAscii();
        asciiPhoto.endDraw();
    }

    private void replaceCanvasContentsWithAscii() {
        int brMin = gui.sliderInt("filter/br min", 0);
        int brMax = gui.sliderInt("filter/br max", 255);
        boolean tintToAvgColor = gui.toggle("filter/tint to avg");
        t += radians(gui.slider("filter/noise speed"));
        float noiseMag = gui.slider("filter/noise mag", 1);
        float noiseFreq = gui.slider("filter/noise freq", 0.01f);
        noiseX += gui.slider("filter/noiseMoveX", 0);
        noiseY += gui.slider("filter/noiseMoveY", 0);
        for (int x = 0; x < width; x += boxWidth) {
            for (int y = 0; y < height; y += boxHeight) {
                int avgColor = getAverageColorHere(x,y,boxWidth,boxHeight);
                float avgBr = brightness(avgColor);
                float normBr = norm(avgBr, brMin, brMax);
                normBr += noiseMag * (-1+2*noise((noiseX+x)*noiseFreq,(noiseY+y)*noiseFreq, t));
                float normBrConstrained = constrain(normBr, 0, 1);
                PGraphics charCanvas = charCanvases.get(floor(normBrConstrained * (charCanvases.size() - 1)));
                if(tintToAvgColor){
                    asciiPhoto.tint(avgColor);
                }else{
                    asciiPhoto.tint(255);
                }
                asciiPhoto.image(charCanvas, x, y);
            }
        }
    }

    private int getAverageColorHere(int x, int y, int w, int h) {
        PImage img = photo.get(x,y,w,h);
        img.loadPixels();
        float r = 0;
        float g = 0;
        float b = 0;
        for (int i = 0; i < img.pixels.length; i++) {
            int c = img.pixels[i];
            r += red(c);
            g += green(c);
            b += blue(c);
        }
        r /= img.pixels.length;
        g /= img.pixels.length;
        b /= img.pixels.length;
        return color(r,g,b);
    }

    private void updateDrawAsciiResources() {
        float intendedFontSize = gui.slider("ascii/font size", currentFontSize);
        if(currentFontSize != intendedFontSize){
            font = createFont(intendedFontSize);
            currentFontSize = intendedFontSize;
        }
        boxWidth = gui.sliderInt("ascii/box width", boxWidth, 2, Integer.MAX_VALUE);
        boxHeight = gui.sliderInt("ascii/box height", boxHeight, 2, Integer.MAX_VALUE);
        if(gui.toggle("ascii/keep refreshing") || frameCount == 1){
            populateCharCanvases();
        }
        if(gui.toggle("ascii/debug view")){
            for (int i = 0; i < charCanvases.size(); i++) {
                int x = (i * boxWidth) % width;
                int y = ((i * boxWidth) / width) * boxHeight;
                pg.image(charCanvases.get(i), x, y);
            }
        }
    }

    private PFont createFont(float fontSize) {
        return createFont("C:\\Projects\\LazySketches\\libs\\JetBrainsMono-2.242\\fonts\\ttf\\JetBrainsMono-Bold.ttf", fontSize);
    }

    private void populateCharCanvases() {
        charCanvases.clear();
        for (char currentChar : availableChars) {
            PGraphics canvas = createGraphics(boxWidth, boxHeight);
            canvas.beginDraw();
            canvas.background(gui.colorPicker("ascii/background", color(0)).hex);
            canvas.fill(gui.colorPicker("ascii/foreground", color(255)).hex);
            canvas.textAlign(CENTER, CENTER);
            canvas.translate(canvas.width / 2f, canvas.height / 2f);
            canvas.textFont(font);
            canvas.text(currentChar + "", gui.slider("ascii/x offset"), gui.slider("ascii/y offset"));
            canvas.endDraw();
            charCanvases.add(canvas);
        }
        charCanvases.sort(new Comparator<PGraphics>() {
            public int compare(PGraphics o1, PGraphics o2) {
                return Integer.compare(getBrightnessSum(o1), getBrightnessSum(o2));
            }

            private int getBrightnessSum(PGraphics canvas) {
                int total = 0;
                canvas.loadPixels();
                for (int i = 0; i < canvas.pixels.length; i++) {
                    total += brightness(canvas.pixels[i]);
                }
                return total;
            }
        });
    }


    private void record() {
        recLength = gui.sliderInt("rec/frames", recLength);
        if(gui.button("rec/start")){
            recStarted = frameCount;
        }
        if(gui.button("rec/stop")){
            sketchInstanceId = Utils.generateRandomShortId();
            recStarted = -1;
        }
        int recordRectPosX = gui.sliderInt("rec/rect pos x");
        int recordRectPosY = gui.sliderInt("rec/rect pos y");
        int recordRectSizeX = gui.sliderInt("rec/rect size x", width);
        int recordRectSizeY = gui.sliderInt("rec/rect size y", height);
        String className = java.lang.invoke.MethodHandles.lookup().lookupClass().getSimpleName();
        if(recStarted != -1 && frameCount < recStarted + recLength){
            println("rec " + saveIndex + " / " + recLength);
            PImage cutout = pg.get(recordRectPosX, recordRectPosY, recordRectSizeX, recordRectSizeY);
            cutout.save("out/recorded images/"+className+"_" + sketchInstanceId + "/" + saveIndex++ + ".png");
        }
        if(gui.toggle("rec/show rect")){
            stroke(255);
            noFill();
            rect(recordRectPosX, recordRectPosY, recordRectSizeX, recordRectSizeY);
        }
    }

}
