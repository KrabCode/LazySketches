package _24_07.raymarch;

import _0_utils.Utils;
import com.krab.lazy.LazyGui;
import com.krab.lazy.LazyGuiSettings;
import com.krab.lazy.ShaderReloader;
import processing.core.PApplet;
import processing.core.PGraphics;

public class Raymarch extends PApplet {
    private PGraphics pg;
    LazyGui gui;

    public static void main(String[] args) {
        PApplet.main(java.lang.invoke.MethodHandles.lookup().lookupClass());
    }

    public void settings() {
        fullScreen(P2D);
    }

    public void setup() {
        gui = new LazyGui(this, new LazyGuiSettings().setStartGuiHidden(true));
//        ShaderReloader.setApplet(this);
        surface.setSize(800, 640);
        surface.setLocation(2*1920-width-20, 20);
        surface.setAlwaysOnTop(true);
        pg = createGraphics(width, height, P2D);
        pg.colorMode(HSB, 1, 1, 1, 1);
        colorMode(HSB, 1, 1, 1, 1);
    }

    public void draw() {
        pg.beginDraw();
        raymarchPass(pg);
        pg.endDraw();
        image(pg, 0, 0);
        postProcess(g);
        Utils.record(this, gui);
        gui.textSet("frameRate", nf(frameRate, 2, 2));
    }

    private void postProcess(PGraphics pg) {
        String shaderPath = Utils.dataPath("post.glsl");
        ShaderReloader.filter(shaderPath, pg);
    }

    private void raymarchPass(PGraphics pg) {
        String shaderPath = Utils.dataPath("raymarch.glsl");
        ShaderReloader.getShader(shaderPath).set("time", millis() / 1000f);
        ShaderReloader.filter(shaderPath, pg);
    }
}
