package _23_09;

import _0_utils.Utils;
import com.krab.lazy.LazyGui;
import com.krab.lazy.ShaderReloader;
import processing.core.PApplet;
import processing.core.PGraphics;

public class TiledShader extends PApplet {
    LazyGui gui;
    PGraphics pg;
    private float t = 0;

    public static void main(String[] args) {
        PApplet.main(java.lang.invoke.MethodHandles.lookup().lookupClass());
    }

    @Override
    public void settings() {
        size(800,800,P2D);
    }

    @Override
    public void setup() {
        surface.setAlwaysOnTop(true);
        gui = new LazyGui(this);
        pg = createGraphics(width, height, P2D);
    }

    @Override
    public void draw() {
        pg.beginDraw();
        t += radians(gui.slider("time +=", 1));
        String shaderPath = "_23_09/TiledShader/tiles.glsl";
        ShaderReloader.getShader(shaderPath).set("time", t);
        ShaderReloader.filter(shaderPath, pg);
        pg.endDraw();
        image(pg, 0, 0);
        Utils.record(this, gui);
    }
}
