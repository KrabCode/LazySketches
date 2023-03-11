package _23_03;

import lazy.LazyGui;
import lazy.ShaderReloader;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PVector;
import processing.opengl.PShader;

public class Fx extends PApplet {
    LazyGui gui;
    PGraphics pg;
    PImage img;
    String barrelShaderPath = "filters/sableralph/barrelBlurChroma.glsl";
    String gaussBlurShaderPath = "filters/sableralph/gaussianBlur.glsl";

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
    }

    @Override
    public void draw() {
        pg.beginDraw();
        drawBackground();
        drawImage();
        drawFx();
        pg.endDraw();
        image(pg, 0, 0);
    }

    private void drawFx() {
        gui.pushFolder("fx");
        gui.pushFolder("barrel blur");
        if(gui.toggle("active")){
            ShaderReloader.getShader(barrelShaderPath).set("sketchSize", (float) width, (float) height);
            ShaderReloader.getShader(barrelShaderPath).set("barrelPower", gui.slider("barrel power", 2.2f));
            ShaderReloader.filter(barrelShaderPath, pg);
        }
        gui.popFolder();

        gui.pushFolder("gaussian blur");
        if(gui.toggle("active")){

            PShader gaussianBlur = ShaderReloader.getShader(gaussBlurShaderPath);

            // Control the values with the mouse
            gaussianBlur.set("strength", gui.slider("strength", 7, 0.1f, 9.0f));
            gaussianBlur.set("kernelSize", gui.sliderInt("kernel size", 16, 3, 32));

            // Vertical pass
            gaussianBlur.set("horizontalPass", 0);
            ShaderReloader.filter(gaussBlurShaderPath, pg);

            // Horizontal pass
            gaussianBlur.set("horizontalPass", 1);
            ShaderReloader.filter(gaussBlurShaderPath, pg);
        }
        gui.popFolder();
        gui.popFolder();
    }

    private void drawImage() {
        gui.pushFolder("image");
        pg.translate(pg.width/2f, pg.height/2f);
        String imagePath = gui.text("image path", "https://pbs.twimg.com/media/E2e8LHOX0AMkXG8.jpg");
        if(frameCount == 1 || gui.button("load image")){
            try{
                img = loadImage(imagePath);
            }catch(Exception ex){
                img = null;
                println(ex.getMessage(), ex);
            }
        }
        float imageScale = gui.slider("image scale", 1);
        PVector imagePos = gui.plotXY("image pos");
        if(img != null){
            pg.imageMode(CENTER);
            pg.translate(imagePos.x, imagePos.y);
            pg.scale(imageScale);
            pg.image(img, 0, 0);
        }
        gui.popFolder();

    }

    private void drawBackground() {
        pg.fill(gui.colorPicker("background").hex);
        pg.noStroke();
        pg.rectMode(CORNER);
        pg.rect(0, 0, width, height);
    }
}
