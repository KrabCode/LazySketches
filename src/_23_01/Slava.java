package _23_01;

import _0_utils.Utils;
import lazy.ShaderReloader;
import processing.core.PApplet;
import processing.core.PGraphics;
import lazy.LazyGui;
import processing.core.PImage;
import processing.opengl.PShader;

public class Slava extends PApplet {
    LazyGui gui;
    PGraphics pg;
    PImage[] images = new PImage[16];
    private int currentFrameIndex;
    private int lastFrameIndex;

    public static void main(String[] args) {
        PApplet.main(java.lang.invoke.MethodHandles.lookup().lookupClass());
    }

    @Override
    public void settings() {
        size(1300, 1300, P2D);
    }

    @Override
    public void setup() {
        gui = new LazyGui(this);
        pg = createGraphics(256, 256, P2D);
        colorMode(HSB, 1, 1, 1, 1);

        loadImages();
    }

    private void loadImages() {
        String path = "C:\\Users\\Krab\\Documents\\GitHub\\LazySketches\\resources_big\\videos\\leaf\\leaf_frames\\frame_";
        for (int i = 0; i < 16; i++) {
            images[i] = loadImage(path + nf(i, 2, 0) + "_delay-0.09s.gif");
        }
    }

    int savedFrames = 0;

    @Override
    public void draw() {
        pg.beginDraw();
        pg.clear();
        animateLeaf();
        pg.endDraw();
        if(gui.toggle("leaf/save") && savedFrames < 16){
            if(lastFrameIndex != currentFrameIndex){
                pg.save("rec\\" + savedFrames++ + ".png");
            }
        }else{
            gui.toggleSet("leaf/save", false);
            savedFrames = 0;
        }
        lastFrameIndex = currentFrameIndex;
        drawBackground();
        imageMode(CENTER);
        image(pg, width/2f, height/2f);
        gui.draw();
        Utils.updateGetFrameRateAverage(this, gui, 60);
    }

    private void animateLeaf() {
        gui.pushFolder("leaf");
        pg.pushMatrix();
        pg.imageMode(CORNER);
        int animationDuration = gui.sliderInt("total frames", 60);
        float currentFrameNorm = norm(frameCount%animationDuration, 0, animationDuration);
        currentFrameIndex = floor(currentFrameNorm * images.length);
        PImage currentImage = images[currentFrameIndex];
        pg.image(currentImage, 0, 0);
        String shaderPath = "_23_01\\hueShift\\hueShift.glsl";
        PShader shader = ShaderReloader.getShader(shaderPath);
        shader.set("time", currentFrameNorm);
        shader.set("hueShiftAmount", gui.slider("hue shift"));
        shader.set("satShiftAmount", gui.slider("sat shift"));
        shader.set("brShiftAmount", gui.slider("br shift"));
        ShaderReloader.filter(shaderPath, pg);
        pg.popMatrix();
        gui.popFolder();
    }

    private void drawBackground() {
        fill(gui.colorPicker("background").hex);
        noStroke();
        rectMode(CORNER);
        rect(0, 0, width, height);
    }
}

