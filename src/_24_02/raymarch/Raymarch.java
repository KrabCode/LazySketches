package _24_02.raymarch;

import _0_utils.Utils;
import com.krab.lazy.LazyGui;
import com.krab.lazy.LazyGuiSettings;
import com.krab.lazy.ShaderReloader;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.opengl.PShader;

public class Raymarch extends PApplet {
    LazyGui gui;
    PGraphics pg;

    public static void main(String[] args) {
        PApplet.main(java.lang.invoke.MethodHandles.lookup().lookupClass());
    }

    @Override
    public void settings() {
        fullScreen(P2D, 2);
    }

    @Override
    public void setup() {
//        surface.setSize(w, h);
//        surface.setLocation(2560-w, 0);
//        surface.setAlwaysOnTop(true);
        gui = new LazyGui(this, new LazyGuiSettings()
                .setCustomGuiDataFolder("..\\gui_data")
                .setStartGuiHidden(true)
        );
        pg = createGraphics(width, height, P2D);
        colorMode(HSB, 1, 1, 1, 1);
    }

    @Override
    public void draw() {
        pg.beginDraw();
        drawBackground();
        drawFilterShader();
        drawDarkFrame();
        pg.endDraw();
        image(pg, 0, 0);
        Utils.record(this, gui);
    }

    private void drawBackground() {
        pg.fill(gui.colorPicker("background").hex);
        pg.noStroke();
        pg.rectMode(CORNER);
        pg.rect(0, 0, width, height);
    }

    private void drawFilterShader() {
        gui.pushFolder("shader");
        String shaderPath = "_24_02\\raymarch.glsl";
        PShader shader = ShaderReloader.getShader(shaderPath);
        float time = gui.slider("time", 0);
        gui.sliderSet("time", time + radians(gui.slider("time ++", 0.1f)));
        shader.set("time", time);
        ShaderReloader.filter(shaderPath, pg);
        gui.popFolder();
    }

    private void drawDarkFrame() {
        gui.pushFolder("frame");
        float weight = gui.slider("frame weight", 10);
        pg.strokeWeight(weight * 2);
        pg.stroke(gui.colorPicker("frame color", color(0.1f)).hex);
        pg.noFill();
        pg.rectMode(CENTER);
        pg.rect(width / 2f, height / 2f, width, height);
        gui.popFolder();
    }
}
