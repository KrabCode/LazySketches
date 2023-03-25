package _22_07;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.opengl.PShader;
import com.krab.lazy.LazyGui;
import com.krab.lazy.ShaderReloader;

public class FlatLighting extends PApplet {LazyGui gui;
    PGraphics canvas;
    PGraphics canvasLit;
    String lightShaderPath = "_22_07/light.glsl";

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
        canvas = createGraphics(width, height, P2D);
        canvasLit = createGraphics(width, height, P2D);
    }

    @Override
    public void draw() {
        canvas.beginDraw();
        canvas.colorMode(RGB,1,1,1,1);
        if (frameCount == 1 || gui.toggle("background/active", false)) {
            drawBackground();
        }
        updateBrush();
        canvas.endDraw();

        boolean displayShader = gui.toggle("shader/display");

        canvasLit.beginDraw();
        PShader lightShader = ShaderReloader.getShader(lightShaderPath);
        lightShader.set("canvas", canvas);
        lightShader.set("lightDir",
                gui.slider("shader/light x"),
                gui.slider("shader/light y"),
                gui.slider("shader/light z")
        );
        ShaderReloader.filter(lightShaderPath, canvasLit);
        canvasLit.endDraw();
        if(displayShader){
            image(canvasLit, 0, 0);
        }else{
            image(canvas, 0, 0);
        }

    }

    private void updateBrush() {
        canvas.noStroke();
        int detail = gui.sliderInt("brush/detail", 16);
        float brushRadius = gui.slider("brush/weight", 50);
        float alpha = gui.slider("brush/alpha", 1);
        if (!mousePressed || !gui.isMouseOutsideGui()) {
            return;
        }
        float distance = dist(pmouseX, pmouseY, mouseX, mouseY);
        distance = max(1, distance);
        for (int i = 0; i < distance; i++) {
            float percent = norm(i, 0, distance);
            float x = lerp(pmouseX, mouseX, percent);
            float y = lerp(pmouseY, mouseY, percent);

            canvas.beginShape(TRIANGLE_FAN);
            canvas.fill(0,0,1, alpha);
            canvas.vertex(x, y);
            for (int cornerIndex = 0; cornerIndex < detail; cornerIndex++) {
                float norm = norm(cornerIndex, 0, detail - 1);
                float theta = norm * TAU;
                float offX = cos(theta);
                float offY = sin(theta);

                float red = map(offX, -1, 1, 0, 1);
                float green = map(offY, -1, 1, 0, 1);
                canvas.fill(red, green, 0.5f, alpha);
                canvas.vertex(x + brushRadius * offX, y + brushRadius * offY);
                // TODO extend gradient to match background color with another triangle strip
            }
            canvas.endShape(CLOSE);
        }
    }

    private void drawBackground() {
        canvas.fill(gui.colorPicker("background/color", color(127)).hex);
        canvas.noStroke();
        canvas.rectMode(CORNER);
        canvas.rect(0, 0, width, height);
    }
}
