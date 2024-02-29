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
    float t;

    public static void main(String[] args) {
        PApplet.main(java.lang.invoke.MethodHandles.lookup().lookupClass());
    }

    @Override
    public void settings() {
        fullScreen(P2D);
    }

    @Override
    public void setup() {
        int padding = 0;
        surface.setSize(600,480);
        surface.setLocation(1920*2-600-padding, padding);
        surface.setAlwaysOnTop(true);
        gui = new LazyGui(this, new LazyGuiSettings()
                .setCustomGuiDataFolder("..\\gui_data")
                .setStartGuiHidden(true)
        );
        pg = createGraphics(width, height, P2D);
        colorMode(HSB, 1, 1, 1, 1);
//        frameRate(60);
    }

    @Override
    public void draw() {
        pg.beginDraw();
        drawBackground();
        drawFilterShader();
        drawDarkFrame();
        pg.endDraw();
        image(pg, 0, 0);
        gui.draw();
        Utils.record(this, gui);
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

    private void drawFilterShader() {
        gui.pushFolder("shader");
        String shaderPath = "_24_02\\raymarch.glsl";
        PShader shader = ShaderReloader.getShader(shaderPath);
        t += radians(gui.slider("shader time +", 1));
        shader.set("time", t);
        ShaderReloader.filter(shaderPath, pg);
        gui.popFolder();
    }

    private void drawBackground() {
        pg.fill(gui.colorPicker("background").hex);
        pg.noStroke();
        pg.rectMode(CORNER);
        pg.rect(0, 0, width, height);
    }
}
