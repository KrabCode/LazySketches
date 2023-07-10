package _23_06;

import com.krab.lazy.LazyGui;
import com.krab.lazy.PickerColor;
import com.krab.lazy.ShaderReloader;
import com.krab.lazy.stores.NormColorStore;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;
import processing.opengl.PShader;

public class RothkoStyle extends PApplet{
    LazyGui gui;
    PGraphics pg;
    String shaderPath = "_23_06/rothkoStyle/noiseBlendRect.glsl";
    float t = 0;

    public static void main(String[] args) {
        PApplet.main(java.lang.invoke.MethodHandles.lookup().lookupClass());
    }

    @Override
    public void settings() {
//        size(1080,1080,P2D);
        fullScreen(P2D);
    }

    @Override
    public void setup() {
        gui = new LazyGui(this);
        pg = createGraphics(width, height, P2D);
        frameRate(144);
    }

    @Override
    public void draw() {
        pg.beginDraw();
        t += radians(gui.slider("time", 1));
        drawBackground();
        gui.pushFolder("rects");
        int rectCount = gui.sliderInt("count", 4);
        for(int i = 0; i < rectCount; i++){
            drawNoisyRect(i);
        }
        gui.popFolder();
        pg.endDraw();
        image(pg, 0, 0);
    }

    private void drawNoisyRect(int i) {
        gui.pushFolder("rect #" + (i+1));

        PVector pos = gui.plotXY("pos");
        PVector size = gui.plotXY("size", 200);
        PShader shader = ShaderReloader.getShader(shaderPath);
        int hex = gui.colorPicker("fill").hex;
        shader.set("rectPos", pos.x, pos.y);
        shader.set("rectSize", size.x, size.y);
        shader.set("boxSmoothLow", gui.slider("box smooth low", -0.5f));
        shader.set("boxSmoothHigh", gui.slider("box smooth high", 0.5f));
        shader.set("time", t);
        shader.set("fill", NormColorStore.red(hex), NormColorStore.green(hex), NormColorStore.blue(hex));
        ShaderReloader.shader(shaderPath, pg);
        pg.rect(pos.x, pos.y, size.x, size.y);
        pg.resetShader();
        gui.popFolder();
    }

    private void drawBackground() {
        pg.fill(gui.colorPicker("background").hex);
        pg.noStroke();
        pg.rectMode(CORNER);
        pg.rect(0,0,width,height);
    }
}
