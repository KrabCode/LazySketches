package _22_10;

import _0_utils.Utils;
import lazy.LazyGui;
import lazy.ShaderReloader;
import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PGraphics;
import processing.opengl.PShader;

public class FlatFlame extends PApplet {
    private PGraphics pg;
    LazyGui gui;
    String alphabet = "abcdefghijklmnopqrstuvwxyz".toUpperCase();
    private float t;
    PFont comicSans;

    public static void main(String[] args) {
        PApplet.main(java.lang.invoke.MethodHandles.lookup().lookupClass());
    }

    public void settings() {
        size(1000, 1000, P2D);
    }

    public void setup() {
        pg = createGraphics(width, height, P2D);
        gui = new LazyGui(this);

        comicSans = createFont("Comic Sans MS", 128);
        colorMode(RGB,1,1,1,1);
    }

    public void draw() {
        pg.beginDraw();
        int colorCount = gui.sliderInt("colors/count", 4, 2, alphabet.length());
        float[] colorData = new float[colorCount * 4];
        float[] colorStops = new float[colorCount];
        String shaderPath = "_22_10/flatFlame.glsl";
        PShader shader = ShaderReloader.getShader(shaderPath);
        for (int i = 0; i < colorCount; i++) {
            String letter = "" + alphabet.charAt(i);
            float norm = norm(i, 0, colorCount-1);
            colorStops[i] = gui.slider("colors/stops/"+i, norm);
            int hex = gui.colorPicker("colors/" + letter, color(norm)).hex;
            int j = i * 4 - 1;
            colorData[++j] = red(hex);
            colorData[++j] = green(hex);
            colorData[++j] = blue(hex);
            colorData[++j] = alpha(hex);
        }
        t += radians(gui.slider("time speed"));
        shader.set("colorCount", colorCount);
        shader.set("colorsRGBA", colorData);
        shader.set("colorStops", colorStops);
        shader.set("time", t);
        float offX =  gui.slider("noise/offset x");
        float offY =  gui.slider("noise/offset y");
        gui.sliderSet("noise/offset x", offX + gui.slider("noise/delta x"));
        gui.sliderSet("noise/offset y", offY + gui.slider("noise/delta y"));
        shader.set("noiseOffset", offX, offY);
        shader.set("baseValue", gui.slider("noise/base val", 0));
        shader.set("baseAmp", gui.slider("noise/base amp", 0.5f));
        shader.set("baseFreq", gui.slider("noise/base frq", 0.5f));
        shader.set("fbmFreqMult", gui.slider("noise/mult frq", 0.1f));
        shader.set("fbmAmpMult", gui.slider("noise/mult amp", 2f));
        shader.set("octaves", gui.sliderInt("noise/octaves", 4));
        shader.set("distSmoothStart", gui.slider("noise/dist smooth start", 0.1f));
        shader.set("distSmoothEnd", gui.slider("noise/dist smooth end", 0.5f));
        ShaderReloader.filter(shaderPath, pg);

        pg.fill(gui.colorPicker("text/color").hex);
        pg.noStroke();
        pg.textAlign(CENTER,CENTER);
        pg.textFont(comicSans);
        pg.textSize(gui.slider("text/size", 32));
        pg.text("arson is fun", gui.slider("text/x"), gui.slider("text/y"));
        pg.endDraw();
        image(pg, 0, 0, width, height);
        Utils.record(this, gui);
    }
}
