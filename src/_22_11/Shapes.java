package _22_11;

import _0_utils.Utils;
import lazy.LazyGui;
import lazy.PickerColor;
import lazy.ShaderReloader;
import processing.core.PApplet;
import processing.core.PGraphics;


public class Shapes extends PApplet {
    LazyGui gui;
    PGraphics pg;

    public static void main(String[] args) {
        PApplet.main(java.lang.invoke.MethodHandles.lookup().lookupClass());
    }

    @Override
    public void settings() {
        fullScreen(P3D);
    }

    @Override
    public void setup() {
        gui = new LazyGui(this);
        pg = createGraphics(width, height, P3D);
    }

    @Override
    public void draw() {
        pg.beginDraw();
        pg.ortho();
        drawBackground();
        drawGrid("grid/");
        drawShapes("shapes/");
        drawSinewaves("sines/");
        drawString("string/");
        pg.endDraw();
        image(pg, 0, 0);
        Utils.record(this, gui);
    }

    private void drawString(String path) {
        pg.pushMatrix();
        String content = gui.stringInput( path + "content", "カニサラダとても寒い");
        int count = content.length();
        float time = gui.slider( path + "time");
        float speed = radians(gui.slider( path + "speed"));
        gui.sliderSet( path + "time", time + speed);
        float x = width / 2f + gui.slider( path + "x");
        float y = height / 2f + gui.slider( path + "y");
        float w = gui.slider( path + "width", 300);
        float h = gui.slider( path + "text size", 256);
        if(gui.toggle( path + "display rect")){
            pg.stroke(0xFFFFFFFF);
            pg.strokeWeight(4);
            pg.rectMode(CORNER);
            pg.rect(x,y,w,h);
        }
        pg.textSize(h);
        pg.translate(0, 0, gui.slider( path + "z"));
        float sx = gui.slider( path + "shadow off x");
        float sy = gui.slider( path + "shadow off y");
        for (int i = 0; i < count; i++) {
            pg.pushMatrix();
            float iNorm = norm(i, 0, count);
            String letter = getLetter(content, i);
            float xOff = ((iNorm + time) % 1f) * w;
            pg.fill(gui.colorPicker( path + "shadow fill").hex);
            pg.text(letter, x + xOff + sx, y + sy);
            pg.fill(gui.colorPicker( path + "normal fill").hex);
            pg.text(letter, x + xOff, y);
            pg.popMatrix();
        }
        pg.popMatrix();
    }

    private String getLetter(String content, int letterIndex) {
        return String.valueOf(content.charAt(letterIndex % content.length()));
    }

    private void drawSinewaves(String path) {
        int count = gui.sliderInt(path + "count", 1);
        if (gui.button(path + "add new")) {
            gui.sliderAdd(path + "count", 1);
        }
        for (int i = 0; i < count; i++) {
            String iPath = path + "sines[" + i + "]/";
            pg.pushMatrix();
            int detail = gui.sliderInt( iPath + "detail", 100);
            float freq = gui.slider( iPath + "freq", 0.01f);
            float sineTime = gui.slider( iPath + "time", 0);
            float sineTimeDelta = radians(gui.slider( iPath + "time +", 1));
            gui.sliderSet( iPath + "time", sineTime + sineTimeDelta);
            pg.translate(width / 2f + gui.slider( iPath + "x"), height / 2f + +gui.slider( iPath + "y"));
            float w = gui.slider( iPath + "width", 400);
            float h = gui.slider( iPath + "height", 200);
            pg.noFill();
            pg.stroke(gui.colorPicker( iPath + "stroke", color(255)).hex);
            pg.strokeWeight(gui.slider( iPath + "weight", 5));
            float z = gui.slider( iPath + "pos z");
            pg.beginShape();
            for (int j = 0; j < detail; j++) {
                float norm = norm(j, 0, detail - 1);
                float x = -w / 2f + w * norm;
                float y = h * sin(norm * freq * TAU + sineTime);

                pg.vertex(x, y, z);
            }
            pg.endShape();
            pg.popMatrix();
        }
    }

    private void drawShapes(String path) {
        int count = gui.sliderInt(path + "count", 1);
        if (gui.button(path + "add new")) {
            gui.sliderAdd(path + "count", 1);
        }
        for (int i = 0; i < count; i++) {
            pg.pushMatrix();
            pg.pushStyle();
            String iPath = path + "shapes[" + i + "]/";
            boolean isRect = gui.toggle( iPath + "rect | ellipse", false);
            boolean center = gui.toggle( iPath + "corner | center", true);
            boolean cutout = gui.toggle( iPath + "normal | cutout", false);
            if (center) {
                pg.rectMode(CENTER);
                pg.ellipseMode(CENTER);
            } else {
                pg.rectMode(CORNER);
                pg.ellipseMode(CORNER);
            }
            pg.stroke(gui.colorPicker( iPath + "stroke", 0xFF000000).hex);
            if (gui.toggle( iPath + "no stroke")) {
                pg.noStroke();
            }
            pg.strokeWeight(gui.slider( iPath + "weight", 1));
            pg.fill(gui.colorPicker( iPath + "fill", 0xFF202020).hex);
            if (gui.toggle( iPath + "no fill")) {
                pg.noFill();
            }
            float x = width / 2f + gui.slider( iPath + "pos x");
            float y = height / 2f + gui.slider( iPath + "pos y");
            float z = gui.slider( iPath + "pos z", 1);
            float w = gui.slider( iPath + "size x", 100);
            float h = gui.slider( iPath + "size y", 100);
            pg.translate(x, y, z);
            pg.rotate(gui.slider( iPath + "rotate"));
            gui.sliderAdd( iPath + "rotate", radians(gui.slider( iPath + "rotate +")));
            pg.translate(gui.slider( iPath + "pos x 2"), gui.slider( iPath + "pos y 2"));
            pg.scale(gui.slider( iPath + "scale x", 1), gui.slider( iPath + "scale y", 1));
            if (!cutout) {
                if (isRect) {
                    pg.rect(0, 0, w, h);
                } else {
                    pg.ellipse(0, 0, w, h);
                }
            } else {
                pg.beginShape();
                float edgeSize = 3;
                pg.vertex(-width * edgeSize, -height * edgeSize);
                pg.vertex(width * edgeSize, -height * edgeSize);
                pg.vertex(width * edgeSize, height * edgeSize);
                pg.vertex(-width * edgeSize, height * edgeSize);
                pg.beginContour();
                if (isRect) {
                    pg.vertex(-w / 2, -h / 2);
                    pg.vertex(-w / 2, h / 2);
                    pg.vertex(w / 2, h / 2);
                    pg.vertex(w / 2, -h / 2);
                } else {
                    int detail = 360;
                    for (int j = 0; j <= detail; j++) {
                        float theta = -map(j, 0, detail, 0, TAU);
                        pg.vertex(w * cos(theta), h * sin(theta));
                    }
                }
                pg.endContour();
                pg.endShape(CLOSE);
            }
            pg.popStyle();
            pg.popMatrix();
        }
    }

    float gridPosX, gridPosY;

    private void drawGrid(String path) {
        String fragPath = "_22_11/grid.glsl";
        PickerColor fg = gui.colorPicker(path + "fg", 0xFFFFFFFF);
        int w = pg.width;
        int h = pg.height;
        float z = gui.slider(path + "pos z");
        ShaderReloader.shader(fragPath, pg);
        pg.pushMatrix();
        pg.pushStyle();
        pg.strokeWeight(gui.slider(path + "point weight", 3));
        pg.translate(w / 2f, h / 2f, z);
        int step = gui.sliderInt(path + "step", 20);
        float speedX = gui.slider(path + "speed x");
        float speedY = gui.slider(path + "speed y");
        gridPosX += speedX * step / 360f;
        gridPosY += speedY * step / 360f;
        if (abs(gridPosX) > step) {
            gridPosX %= step;
        }
        if (abs(gridPosY) > step) {
            gridPosY %= step;
        }
        pg.rotate(gui.slider(path + "rotate"));
        pg.translate(gridPosX, gridPosY);
        pg.beginShape(POINTS);
        pg.stroke(fg.hex);
        int overshoot = gui.sliderInt(path + "overshoot", 1);
        for (int x = -w * overshoot; x <= w * overshoot; x += step) {
            for (int y = -h * overshoot; y <= h * overshoot; y += step) {
                pg.vertex(x, y);
            }
        }
        pg.endShape();
        pg.popStyle();
        pg.popMatrix();
        pg.resetShader();
    }

    private void drawBackground() {
        pg.fill(gui.colorPicker("background", color(0xFF303030)).hex);
        pg.noStroke();
        pg.rectMode(CORNER);
        pg.rect(0, 0, width, height);
    }
}
