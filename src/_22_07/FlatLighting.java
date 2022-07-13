package _22_07;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.opengl.PShader;
import toolbox.Gui;
import toolbox.ShaderReloader;
import toolbox.windows.nodes.colorPicker.Color;

public class FlatLighting extends PApplet {
    Gui gui;
    PGraphics canvas;
    PGraphics canvasLit;
    String lightShaderPath = "_22_07/light.glsl";

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
        canvasLit = createGraphics(width, height, P2D);
    }

    @Override
    public void draw() {
        canvas.beginDraw();
        if (frameCount == 1 || gui.toggle("background/active", false)) {
            drawBackground();
        }
        updateBrush();
        canvas.endDraw();

        boolean displayShader = gui.toggle("shader/display");

        canvasLit.beginDraw();
        PShader lightShader = ShaderReloader.getShader(lightShaderPath);
        lightShader.set("canvas", canvas);
        lightShader.set("strength", gui.slider("shader/strength", 1));
        lightShader.set("height", gui.slider("shader/height", 0.5f));
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
        gui.themePicker();
        gui.draw();
    }

    private void updateBrush() {
        canvas.noStroke();
        Color colorA = gui.colorPicker("brush/color A", color(0));
        Color colorB = gui.colorPicker("brush/color B", color(255));
        int detail = gui.sliderInt("brush/detail", 16);
        float brushRadius = gui.slider("brush/weight", 5);
        if (!mousePressed) {
            return;
        }
        float distance = dist(pmouseX, pmouseY, mouseX, mouseY);
        for (int i = 0; i < distance; i++) {
            float percent = norm(i, 0, distance);
            float x = lerp(pmouseX, mouseX, percent);
            float y = lerp(pmouseY, mouseY, percent);

            canvas.beginShape(TRIANGLE_FAN);
            canvas.fill(colorA.hex);
            canvas.vertex(x, y);
            canvas.fill(colorB.hex);
            for (int cornerIndex = 0; cornerIndex < detail; cornerIndex++) {
                float theta = map(cornerIndex, 0, detail - 1, 0, TAU);
                canvas.vertex(x + brushRadius * cos(theta), y + brushRadius * sin(theta));
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
