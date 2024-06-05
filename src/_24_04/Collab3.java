package _24_04;

import _0_utils.Utils;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import com.krab.lazy.LazyGui;
import com.krab.lazy.ShaderReloader;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.opengl.PShader;

public class Collab3 extends PApplet {
    LazyGui gui;
    PGraphics pg;

    public static void main(String[] args) {
        PApplet.main(java.lang.invoke.MethodHandles.lookup().lookupClass());
    }

    @Override
    public void settings() {
        // size(1080, 1080, P2D);
        fullScreen(P2D);
    }

    @Override
    public void setup() {
        gui = new LazyGui(this);
        pg = createGraphics(width, height, P2D);
        colorMode(HSB, 1, 1, 1, 1);
        println("LazyGui: " + gui.getVersion());
    }

    @Override
    public void draw() {
        pg.beginDraw();
        drawTriangleFanBackground();
        gui.pushFolder("shader");
        String shaderPath = "collab_move_filter.glsl";
        Utils.shaderMove(pg, gui, shaderPath);
        gui.popFolder();
        pg.endDraw();
        PShader shader = ShaderReloader.getShader("collab_post_pass.glsl");
        shader.set("tex0", pg);
        ShaderReloader.filter("collab_post_pass.glsl");
        //image(pg, 0, 0);

        Utils.record(this, gui);
    }

    private void drawTriangleFanBackground() {
        gui.pushFolder("background");
        pg.translate(mouseX, mouseY);
        pg.rotate(gui.slider("rotation"));
        pg.stroke(gui.colorPicker("stroke").hex);
        pg.strokeWeight(gui.slider("weight", 1.5f));

        pg.beginShape(TRIANGLE_FAN);
        PGraphics fillTexture = gui.gradient("texture");
        pg.textureMode(NORMAL);
        pg.texture(fillTexture);
        pg.vertex(0, 0, 0.5f, 0);
        int vertexCount = gui.sliderInt("vertices", 6, 3, 10000);
        float radius = gui.slider("radius", 250);
        for (int i = 0; i <= vertexCount; i++) {
            float theta = map(i, 0, vertexCount, 0, TAU);
            float x = radius * cos(theta);
            float y = radius * sin(theta);
            pg.vertex(x, y, 0.5f, 1);
        }
        pg.endShape();

        gui.popFolder();
    }

}

