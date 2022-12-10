package _22_12.Noise;

import _0_utils.Utils;
import _22_03.PostFxAdapter;
import lazy.PickerColor;
import lazy.ShaderReloader;
import processing.core.PApplet;
import processing.core.PGraphics;
import lazy.LazyGui;
import processing.opengl.PShader;

public class Noise extends PApplet {
    LazyGui gui;
    PGraphics pg;

    public static void main(String[] args) {
        PApplet.main(java.lang.invoke.MethodHandles.lookup().lookupClass());
    }

    @Override
    public void settings() {
        size(1080, 1080, P2D);
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
        drawNoiseShader();
        pg.endDraw();
        Utils.shaderMove(pg,gui);
        PostFxAdapter.apply(this, gui, pg);
        image(pg, 0, 0);
        Utils.record(this, gui);
        gui.draw();
    }


    private void drawNoiseShader() {
        gui.pushFolder("noise shader");
        float t = gui.slider("time");
        gui.sliderAdd("time", radians(gui.slider("time +", 1)));
        String noiseShaderPath = "_22_12/Noise/noise.glsl";
        PShader noiseShader = ShaderReloader.getShader(noiseShaderPath);
        noiseShader.set("time", t);
        PickerColor a = gui.colorPicker("a", color(0));
        PickerColor b = gui.colorPicker("b", color(1));
        noiseShader.set("colorA", red(a.hex), green(a.hex), blue(a.hex), alpha(a.hex));
        noiseShader.set("colorB", red(b.hex), green(b.hex), blue(b.hex), alpha(b.hex));
        ShaderReloader.filter(noiseShaderPath, pg);
        gui.popFolder();
    }

}

