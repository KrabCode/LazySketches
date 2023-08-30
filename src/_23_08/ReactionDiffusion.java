package _23_08;

import _0_utils.Utils;
import _22_03.PostFxAdapter;
import com.krab.lazy.LazyGui;
import com.krab.lazy.LazyGuiSettings;
import com.krab.lazy.ShaderReloader;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;
import processing.opengl.PShader;

public class ReactionDiffusion extends PApplet {
    LazyGui gui;
    PGraphics updateCanvas;
    PGraphics renderCanvas;

    public static void main(String[] args) {
        PApplet.main(java.lang.invoke.MethodHandles.lookup().lookupClass());
    }

    @Override
    public void settings() {
//        size(600,600, P2D);
        fullScreen(P2D);
        noSmooth();
    }

    @Override
    public void setup() {
        gui = new LazyGui(this, new LazyGuiSettings()
//                .setLoadLatestSaveOnStartup(false)
        );
        colorMode(RGB,1,1,1,1);
        int canvasSize = min(width, height);
        updateCanvas = createGraphics(canvasSize, canvasSize, P2D);
        updateCanvas.noSmooth();
        renderCanvas = createGraphics(canvasSize, canvasSize, P2D);
        renderCanvas.noSmooth();
        drawSeedIfNeeded(updateCanvas);
    }

    @Override
    public void draw() {
        drawSeedIfNeeded(updateCanvas);
        updateSimulation(updateCanvas);
        renderSimulation(updateCanvas, renderCanvas);
        Utils.record(this, gui);
    }

    private void drawSeedIfNeeded(PGraphics pg) {
        gui.pushFolder("seed");
        if(gui.button("draw once") || frameCount == 0){
            if(frameCount == 0){
                pg.beginDraw();
                pg.endDraw();
            }
            pg.beginDraw();
            pg.colorMode(RGB,1,1,1,1);
            String[] types = new String[]{
                    "circle",
                    "square"
            };
            float size = gui.slider("size", 60);
            String type = gui.radio("type", types);
            pg.background(1,0,0);
            pg.translate(pg.width/2f, pg.height/2f);
            pg.fill(1,0,1);
            pg.rectMode(CENTER);
            pg.noStroke();
            if(type.equals("circle")){
                pg.circle(0,0, size);
            }else{
                pg.square(0,0,size);
            }
            pg.endDraw();
        }
        gui.popFolder();
    }

    private void updateSimulation(PGraphics pg) {
        gui.pushFolder("update");
        pg.beginDraw();
        String updateShaderPath = "_23_08/RD/update.glsl";
        PShader updateShader = ShaderReloader.getShader(updateShaderPath);
        updateShader.set("dA", gui.slider("dA", 1));
        updateShader.set("dB", gui.slider("dB", 0.5f));
        PVector killFeed = gui.plotXY("kill\\/feed", 0.01f, 0.01f);
        updateShader.set("k", killFeed.x);
        updateShader.set("f", killFeed.y);
        updateShader.set("t", gui.slider("t",1));
        int passCount = gui.sliderInt("passes", 2, 1, 200);
        for (int i = 0; i < passCount; i++) {
            ShaderReloader.filter(updateShaderPath, pg);
        }
        PostFxAdapter.apply(this, gui, pg);
        pg.endDraw();
        gui.popFolder();
    }

    private void renderSimulation(PGraphics sourceCanvas, PGraphics targetCanvas) {
        gui.pushFolder("display");
        targetCanvas.beginDraw();
        String displayShaderPath = "_23_08/RD/display.glsl";
        PShader displayShader = ShaderReloader.getShader(displayShaderPath);
        displayShader.set("source",  sourceCanvas);
        displayShader.set("gradient", gui.gradient("gradient"));
        ShaderReloader.filter(displayShaderPath, targetCanvas);
        PostFxAdapter.apply(this, gui, targetCanvas);
        targetCanvas.endDraw();
        boolean debugSource = gui.toggle("debug canvas", false);
        displayOutputOnMainCanvas(debugSource?sourceCanvas:targetCanvas);
        gui.popFolder();
    }

    private void displayOutputOnMainCanvas(PGraphics src) {
        background(gui.colorPicker("background").hex);
        imageMode(CENTER);
        translate(width/2f, height/2f);
        scale(gui.slider("scale", 1));
        image(src, 0, 0);
    }

}
