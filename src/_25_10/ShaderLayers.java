package _25_10;

import com.krab.lazy.LazyGui;
import com.krab.lazy.ShaderReloader;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.opengl.PShader;

public class ShaderLayers extends PApplet {
    LazyGui gui;
    PGraphics pgInvert;
    PGraphics pgHue;
    PImage img;

    String SHADER_HUE_SHIFT = "_25_10/ShaderLayers/hueShift.glsl";
    PShader shaderHueShift;

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
        pgInvert = createGraphics(400, 400, P2D);
        pgHue = createGraphics(400, 400, P2D);
        shaderHueShift = loadShader(SHADER_HUE_SHIFT);
        img = loadImage("https://picsum.photos/id/237/400/400.jpg");
    }

    @Override
    public void draw() {
        background(gui.colorPicker("background").hex);

        gui.image("source image", img);

        pgInvert.beginDraw();
        pgInvert.image(img, 0, 0);
        pgInvert.filter(INVERT);
        pgInvert.endDraw();
        gui.image("inverted", pgInvert);

        pgHue.beginDraw();
        pgHue.image(img, 0, 0);
        shaderHueShift.set("hueShiftAmount", frameCount * 0.01f);
        pgHue.filter(shaderHueShift);
        pgHue.endDraw();
        gui.image("hue shifted", pgHue);
    }
}

