package _24_06.standalone;

import _0_utils.Utils;
import com.krab.lazy.LazyGui;
import com.krab.lazy.ShaderReloader;
import processing.core.PApplet;
import processing.core.PGraphics;

public class Standalone extends PApplet {

    LazyGui gui;
    PGraphics pg;

    public static void main(String[] args) {
        PApplet.main(java.lang.invoke.MethodHandles.lookup().lookupClass());
    }

    public void settings() {
        size(800, 800, P2D);
    }

    public void setup() {
        gui = new LazyGui(this);
        colorMode(HSB, 1, 1, 1, 1);
        pg = createGraphics(width, height, P2D);
        pg.colorMode(HSB, 1, 1, 1, 1);
    }

    public void draw() {
        pg.beginDraw();
        pg.background(0);
        float time = gui.slider("time");
        gui.sliderSet("time", time + gui.slider("time ++"));
        String shaderPath = Utils.dataPath("test.glsl");
        ShaderReloader.getShader(shaderPath).set("time", time);
        ShaderReloader.filter(shaderPath, pg);
        pg.endDraw();
        clear();
        image(pg, 0, 0);
    }
}