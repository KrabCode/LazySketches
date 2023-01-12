package _23_01.huesatbrshift;

import _0_utils.Utils;
import _22_03.PostFxAdapter;
import lazy.ShaderReloader;
import processing.core.PApplet;
import processing.core.PGraphics;
import lazy.LazyGui;
import processing.core.PImage;
import processing.opengl.PShader;

public class HueSatBrShiftGif extends PApplet {
    LazyGui gui;
    PGraphics pg;
    PGraphics fg;
    PImage[] images = new PImage[16];
    private int currentFrameIndex;
    private float currentFrameNorm;
    private int lastFrameIndex;
    String oldPath = "..\\resources_big\\videos\\leaf\\frames\\";
    private int frameCountPauseable;

    public static void main(String[] args) {
        PApplet.main(java.lang.invoke.MethodHandles.lookup().lookupClass());
    }

    @Override
    public void settings() {
        size(1300, 1300, P2D);
    }

    int savedFrames = 0;

    @Override
    public void setup() {
        gui = new LazyGui(this);
        fg = createGraphics(width, height, P2D);
        fg.colorMode(HSB, 1, 1, 1, 1);
        colorMode(HSB, 1, 1, 1, 1);
        reloadImages(true);
        pg = createGraphics(images[0].width, images[0].height, P2D);
    }

    private void reloadImages(boolean forceLoad) {
        String newPath = gui.text("animation/source folder", oldPath);
        if(!forceLoad && newPath.equals(oldPath)){
            return;
        }
        oldPath = newPath;
        for (int i = 0; i < 16; i++) {
            images[i] = loadImage(oldPath + i + ".gif");
        }
    }

    @Override
    public void draw() {
        reloadImages(false);
        pg.beginDraw();
        drawBackground(pg);
        animateGif(pg);
        pg.endDraw();
        PostFxAdapter.apply(this, gui, pg);
        saveGif(pg);

        gui.pushFolder("screen output");
        background(0.15f);
        imageMode(CENTER);
        image(pg, width/2f, height/2f);
        Utils.record(this, gui);
        Utils.updateGetFrameRateAverage(this, gui, 60);
        gui.popFolder();
    }

    private void updateHueShiftShader() {
        String shaderPath = "_23_01\\hueShift\\hueShift.glsl";
        PShader shader = ShaderReloader.getShader(shaderPath);
        shader.set("time", currentFrameNorm);
        shader.set("hueShiftAmount", gui.slider("hue shift"));
        shader.set("satShiftAmount", gui.slider("sat shift"));
        shader.set("brShiftAmount", gui.slider("br shift"));
        ShaderReloader.filter(shaderPath, pg);

    }

    private void saveGif(PGraphics pg) {
        if(gui.toggle("animation/save frames") && savedFrames < 16){
            if(lastFrameIndex != currentFrameIndex){
                pg.save("rec\\" + savedFrames++ + ".png");
            }
        }else{
            gui.toggleSet("animation/save frames", false);
            savedFrames = 0;
        }
        lastFrameIndex = currentFrameIndex;
    }

    private void animateGif(PGraphics pg) {
        gui.pushFolder("animation");
        pg.pushMatrix();
        pg.imageMode(CORNER);
        int animationDuration = gui.sliderInt("realtime frames", 60);
        if(!gui.toggle("pause")){
            frameCountPauseable++;
        }
        currentFrameNorm = norm(frameCountPauseable %animationDuration, 0, animationDuration);
        currentFrameIndex = floor(currentFrameNorm * images.length);
        PImage currentImage = images[currentFrameIndex];
        pg.image(currentImage, 0, 0);
        pg.popMatrix();

        updateHueShiftShader();
        gui.popFolder();
    }

    private void drawBackground(PGraphics pg) {
        gui.pushFolder("background");
        if(gui.button("clear")){
            pg.clear();
        }
        if(!gui.toggle("active")){
            gui.popFolder();
            return;
        }
        pg.blendMode(SUBTRACT);
        pg.fill(gui.colorPicker("subtract", color(0.1f)).hex);
        pg.noStroke();
        pg.rectMode(CORNER);
        pg.rect(0, 0, width, height);
        pg.blendMode(LIGHTEST);
        pg.fill(gui.colorPicker("darkest").hex);
        pg.rect(0, 0, width, height);
        pg.blendMode(BLEND);
        gui.popFolder();
    }
}

