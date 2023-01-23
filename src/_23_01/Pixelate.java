package _23_01;

import _0_utils.Utils;
import lazy.LazyGui;
import lazy.ShaderReloader;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.opengl.PShader;

public class Pixelate extends PApplet {
    LazyGui gui;
    PGraphics pg;
    String oldPath = "";
    PImage img;

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
        colorMode(RGB,1,1,1,1);
    }

    @Override
    public void draw() {
        drawBackground();
        refreshInputImage();
        pg.beginDraw();
        pg.colorMode(RGB,1,1,1,1);
        updateOutputImage();
        pg.endDraw();
        if(gui.button("output/save image")){
            String path = "out/" + Utils.generateRandomShortId() +".png";
            pg.save(path);
            println("saved " + path);
        }
        translate(width/2f, height/2f);
        imageMode(CENTER);
        scale(gui.slider("output/display scale", 1));
        image(pg, 0, 0);
    }

    private void refreshInputImage() {
        gui.pushFolder("input");
        String imagePath = gui.text("image path", "https://i.pinimg.com/originals/cc/d4/c7/ccd4c7adf7f3f4cdc5a2bdcd7f1d49ae.jpg");
        if(gui.button("load image") || frameCount == 1){
            try{
                img = loadImage(imagePath);
            }catch(Exception ex){
                println(ex);
            }
        }
        gui.popFolder();
    }

    private void updateOutputImage() {
        gui.pushFolder("output");
        if(img != null){
            if(!gui.toggle("view input\\/output", true)){
                pg.image(img, 0, 0);
            }else{
                pixelate();
            }
        }
        gui.popFolder();
    }

    private void pixelate() {
        pg.strokeWeight(gui.slider("stroke weight", 2));
        pg.stroke(gui.colorPicker("stroke").hex);
        if(gui.toggle("no stroke", true)){
            pg.noStroke();
        }
        img.loadPixels();
        int pixelSize = gui.sliderInt("pixel size", 20, 1, width);
        for (int xi = 0; xi < img.width; xi+= pixelSize) {
            for (int yi = 0; yi < img.height; yi+= pixelSize) {
                int avg = getAverageColor(xi, yi, pixelSize, pixelSize);
                pg.fill(avg);
                pg.rect(xi,yi,pixelSize,pixelSize);
            }
        }
    }

    private int getAverageColor(int x, int y, int w, int h) {
        float sumR = 0;
        float sumG = 0;
        float sumB = 0;
        float count = 0;
        for (int xi = x; xi < min(x+w, img.width); xi++) {
            for (int yi = y; yi < min(y+h, img.height); yi++) {
                int pixelColor = img.pixels[xi + yi * img.width];
                sumR += red(pixelColor);
                sumG += green(pixelColor);
                sumB += blue(pixelColor);
                count++;
            }
        }
        return color(sumR/count, sumG/count, sumB/count);
    }


    private void drawBackground() {
        fill(gui.colorPicker("background", 0xFF303030).hex);
        noStroke();
        rectMode(CORNER);
        rect(0, 0, width, height);
    }
}
