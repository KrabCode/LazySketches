package _23_10;

import _0_utils.Utils;
import com.krab.lazy.LazyGui;
import com.krab.lazy.ShaderReloader;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.opengl.PShader;

public class BitwiseShader extends PApplet {
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
        gui = new LazyGui(this);
        pg = createGraphics(width, height, P2D);
        colorMode(HSB, 1, 1, 1, 1);
        frameRate(144);
    }

    @Override
    public void draw() {
        pg.beginDraw();
        String fragPath = "/_23_10/bitwise.glsl";
        ShaderReloader.getShader(fragPath).set("time", t);
        t += radians(gui.slider("time", 1));
        ShaderReloader.filter(fragPath);
        pg.endDraw();
        image(pg, 0, 0);
        Utils.record(this, gui);
    }
}

