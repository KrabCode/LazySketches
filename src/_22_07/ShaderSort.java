package _22_07;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.opengl.PShader;
import toolbox.LazyGui;
import toolbox.ShaderReloader;

public class ShaderSort extends PApplet {

    PGraphics pg;
    private LazyGui gui;

    public static void main(String[] args) {
        PApplet.main(java.lang.invoke.MethodHandles.lookup().lookupClass());
    }

    @Override
    public void settings() {
        size(800,800, P2D);
    }

    @Override
    public void setup() {
        gui = new LazyGui(this);
        pg = createGraphics(width, height, P2D);
    }

    @Override
    public void draw() {
        pg.beginDraw();
        if(frameCount == 1 || gui.toggle("redraw image")){
            drawBackground();
        }
        String shaderPath = "_22_07/sort.glsl";
        PShader shader = ShaderReloader.getShader(shaderPath);
        shader.set("speed", gui.slider("speed", 1, 0, 1));
        ShaderReloader.filter(shaderPath, pg);
        pg.endDraw();
        clear();
        image(pg, 0, 0);

    }

    private void drawBackground() {
        pg.image(gui.imagePicker("image"), 0, 0);
    }
}