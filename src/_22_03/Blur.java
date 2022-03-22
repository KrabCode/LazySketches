package _22_03;

import processing.core.PApplet;
import processing.core.PGraphics;
import toolbox.Gui;
import toolbox.ShaderReloader;

import java.util.ArrayList;

public class Blur extends PApplet {
    Gui gui;
    PGraphics canvas;
    ArrayList<PGraphics> graphics = new ArrayList<>();

    public static void main(String[] args) {
        PApplet.main(java.lang.invoke.MethodHandles.lookup().lookupClass());
    }

    @Override
    public void settings() {
        size(1080, 1080, P2D);
    }

    @Override
    public void setup() {
        gui = new Gui(this);
        canvas = createGraphics(width, height, P2D);
    }

    @Override
    public void draw() {
        canvas.beginDraw();
        canvas.background(0);
        canvas.image(gui.imagePicker("image", "https://www.industrialempathy.com/img/remote/ZiClJf-1920w.jpg"), 0, 0);
        canvas.endDraw();
        int passes = gui.sliderInt("blur/passes", 4, 0, 100);
        int w = width;
        int h = height;
        if (passes > 0) {
            for (int i = 0; i < passes; i++) {
                if (i >= graphics.size()) {
                    graphics.add(createGraphics(w, h, P2D));
                    println(i, w, h);
                }
                w /= 2;
                h /= 2;
                PGraphics pg = graphics.get(i);
                pg.beginDraw();
                pg.clear();
                PGraphics lastGraphics = canvas;
                if (i > 0) {
                    lastGraphics = graphics.get(i - 1);
                }
                pg.image(lastGraphics, 0, 0, pg.width, pg.height);
                String blurShaderPath = "_22_03/blur.glsl";
                ShaderReloader.getShader(blurShaderPath).set("distance", gui.slider("blur/distance", 1));
                ShaderReloader.filter(blurShaderPath, pg);
                pg.endDraw();
            }
            image(graphics.get(passes - 1), 0, 0, width, height);
        } else {
            image(canvas, 0, 0);
        }

        gui.draw();
    }
}