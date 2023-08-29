package _23_08;

import com.krab.lazy.LazyGui;
import com.krab.lazy.LazyGuiSettings;
import com.krab.lazy.ShaderReloader;
import processing.core.PApplet;
import processing.core.PGraphics;
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
        colorMode(HSB,1,1,1,1);
        updateCanvas = createGraphics(width, height, P2D);
        renderCanvas = createGraphics(width, height, P2D);
        drawSeedTexture(updateCanvas);
    }

    @Override
    public void draw() {
        drawSeedTexture(updateCanvas);
        updateSimulation(updateCanvas);
        renderSimulation(updateCanvas, renderCanvas);
    }

    private void drawSeedTexture(PGraphics pg) {
        gui.pushFolder("seed");
        if(gui.button("regen once") || gui.toggle("regen always") || frameCount == 0){
            if(frameCount == 0){
                // dummy draw to appease a bug spirit
                pg.beginDraw();
                pg.endDraw();
            }
            pg.beginDraw();
            String seedShaderPath = "_23_08/RD/seed.glsl";
            PShader seedShader = ShaderReloader.getShader(seedShaderPath);
            seedShader.set("n", gui.slider("amp", 1));
            ShaderReloader.filter(seedShaderPath, pg);
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
        updateShader.set("f", gui.slider("f", 0.055f));
        updateShader.set("k", gui.slider("k", 0.062f));
        updateShader.set("t", gui.slider("t", 0.1f));
        ShaderReloader.filter(updateShaderPath, pg);
        pg.endDraw();
        gui.popFolder();
    }

    private void renderSimulation(PGraphics sourceCanvas, PGraphics targetCanvas) {
        gui.pushFolder("display");
        targetCanvas.beginDraw();
        String displayShaderPath = "_23_08/RD/display.glsl";
        PShader displayShader = ShaderReloader.getShader(displayShaderPath);
        displayShader.set("img",  sourceCanvas);
        displayShader.set("displayRedAsWhite",  gui.toggle("A\\/B = white"));
        ShaderReloader.filter(displayShaderPath, targetCanvas);
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
