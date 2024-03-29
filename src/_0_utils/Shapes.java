package _0_utils;

import com.krab.lazy.LazyGui;
import com.krab.lazy.PickerColor;
import com.krab.lazy.ShaderReloader;
import processing.core.*;

import java.util.HashMap;
import java.util.Map;

import static com.krab.lazy.stores.GlobalReferences.app;

@SuppressWarnings("SuspiciousNameCombination")
public class Shapes extends PApplet {
    private static float maxShapeCount = 10;
    static int maxCountPyramids = 10;

    LazyGui gui;
    PGraphics pg;
    private static float gridPosX, gridPosY;

    private static int backgroundFrameCount;

    static Map<String, PFont> fontsByPath = new HashMap<>();

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
        pg.colorMode(HSB, 1, 1, 1, 1);
    }

    @Override
    public void draw() {
        pg.beginDraw();
        pg.ortho();
        drawBackground("bg/", gui, pg);
        drawGrid("grid/", gui, pg);
        drawShapes("shapes/", gui, pg);
        drawPyramids("pyramids/", gui, pg);
        drawSinewaves("sines/", gui, pg);
        drawMovingString("string/", gui, pg);
        pg.endDraw();
        image(pg, 0, 0);
        Utils.record(this, gui);
    }

    public static void drawMovingString(String path, LazyGui gui, PGraphics pg) {
        pg.pushMatrix();
        String content = gui.text(path + "content", "...");
        int count = content.length();
        float time = gui.slider(path + "time");
        float speed = radians(gui.slider(path + "speed"));
        gui.sliderSet(path + "time", time + speed);
        float x = pg.width / 2f + gui.slider(path + "x");
        float y = pg.height / 2f + gui.slider(path + "y");
        float w = gui.slider(path + "width", 300);
        float h = gui.slider(path + "text size", 256);
        if (gui.toggle(path + "display rect")) {
            pg.stroke(0xFFFFFFFF);
            pg.strokeWeight(4);
            pg.rectMode(CORNER);
            pg.rect(x, y, w, h);
        }
        pg.textSize(h);
        pg.translate(0, 0, gui.slider(path + "z"));
        float sx = gui.slider(path + "shadow off x");
        float sy = gui.slider(path + "shadow off y");
        for (int i = 0; i < count; i++) {
            pg.pushMatrix();
            float iNorm = norm(i, 0, count);
            String letter = getLetter(content, i);
            float xOff = ((iNorm + time) % 1f) * w;
            pg.fill(gui.colorPicker(path + "shadow fill").hex);
            pg.text(letter, x + xOff + sx, y + sy);
            pg.fill(gui.colorPicker(path + "normal fill").hex);
            pg.text(letter, x + xOff, y);
            pg.popMatrix();
        }
        pg.popMatrix();
    }

    public static void drawSimpleText(String path, LazyGui gui, PGraphics pg) {
        gui.pushFolder(path);
        String content = gui.text("content");
        PVector pos = gui.plotXY("pos");
        float rotation = TAU * gui.slider("rotation * tau");
        float x = pg.width / 2f + pos.x;
        float y = pg.height / 2f + pos.y;
        float textSize = gui.slider("text size", 256);
        String fontPath = gui.text("font path");
        String fontPathWithSize = fontPath + "|" + floor(textSize);
        if (gui.button("update font")) {
            PFont font = app.createFont(fontPath, textSize);
            fontsByPath.put(fontPathWithSize, font);
        }
        if (fontsByPath.containsKey(fontPathWithSize)) {
            pg.textFont(fontsByPath.get(fontPathWithSize));
        }
        pg.textSize(textSize);
        pg.textAlign(CENTER, CENTER);
        pg.pushMatrix();
        pg.translate(x, y);
        pg.rotate(rotation);
        PVector shadowOffset = gui.plotXY("shadow offset");
        pg.fill(gui.colorPicker("shadow fill", 0xFF000000).hex);
        pg.text(content, shadowOffset.x, shadowOffset.y);
        pg.fill(gui.colorPicker("normal fill", 0xFFFFFFFF).hex);
        pg.text(content, 0, 0);
        pg.popMatrix();
        gui.popFolder();
    }

    private static String getLetter(String content, int letterIndex) {
        return String.valueOf(content.charAt(letterIndex % content.length()));
    }

    public static void drawPyramids(String path, LazyGui gui, PGraphics pg) {
        gui.pushFolder(path);
        pg.pushStyle();
        pg.pushMatrix();
        pg.translate(pg.width / 2f, pg.height / 2f);
        gui.pushFolder("Δ global transform");
        translate(gui, pg);
        rotateXYZ(gui, pg);
        gui.popFolder();
        pg.rectMode(CENTER);
        int count = gui.sliderInt("count", 1);
        maxCountPyramids = max(maxCountPyramids, count);
        for (int i = 0; i < maxCountPyramids; i++) {
            gui.pushFolder("pyramids[" + i + "]");
            if (i >= count) {
                gui.hideCurrentFolder();
                gui.popFolder();
                continue;
            } else {
                gui.showCurrentFolder();
            }
            pg.pushStyle();
            pg.pushMatrix();
            float topHeight = gui.slider("top ■ height", 80);
            float footSize = gui.slider("bot ■ size", 100);
            float cutoffSize = gui.slider("top ■ size", 30);
            pg.stroke(gui.colorPicker("stroke", pg.color(255)).hex);
            pg.strokeWeight(gui.slider("weight", 5));
            pg.fill(gui.colorPicker("fill").hex);
            boolean triangleFanMode = gui.toggle("triangle fan | quads");
            gui.pushFolder("Δ[" + i + "].transform");
            pg.translate(0, topHeight, 0);
            translate(gui, pg);
            pg.rotateX(HALF_PI);
            rotateXYZ(gui, pg);
            pg.scale(gui.slider("scale x", 1), gui.slider("scale y", 1));

            if (triangleFanMode) {
                pg.beginShape(PConstants.TRIANGLE_FAN);
                pg.vertex(0, 0, topHeight);
                pg.vertex(footSize, -footSize);
                pg.vertex(-footSize, -footSize);
                pg.vertex(-footSize, footSize);
                pg.vertex(footSize, footSize);
                pg.vertex(footSize, -footSize);
                pg.endShape();
                gui.popFolder();
            } else {
                pg.beginShape(PConstants.QUAD_STRIP);
                pg.vertex(cutoffSize, -cutoffSize, topHeight);
                pg.vertex(footSize, -footSize, 0);

                pg.vertex(-cutoffSize, -cutoffSize, topHeight);
                pg.vertex(-footSize, -footSize, 0);

                pg.vertex(-cutoffSize, cutoffSize, topHeight);
                pg.vertex(-footSize, footSize, 0);

                pg.vertex(cutoffSize, cutoffSize, topHeight);
                pg.vertex(footSize, footSize, 0);

                pg.vertex(cutoffSize, -cutoffSize, topHeight);
                pg.vertex(footSize, -footSize, 0);
                pg.endShape();

                gui.popFolder();
            }

            gui.pushFolder("bot ■[" + i + "].transform");
            translate(gui, pg);
            rotateXYZ(gui, pg);
            pg.rect(0, 0, footSize * 2, footSize * 2);
            gui.popFolder();

            if (!triangleFanMode) {

                gui.pushFolder("top ■[" + i + "].transform");
                pg.translate(0, 0, topHeight);
                translate(gui, pg);
                rotateXYZ(gui, pg);
                pg.rect(0, 0, cutoffSize * 2, cutoffSize * 2);
                gui.popFolder();
            }

            pg.popStyle();
            pg.popMatrix();
            gui.popFolder();
        }
        gui.popFolder();
        pg.popStyle();
        pg.popMatrix();
    }

    private static void translate(LazyGui gui, PGraphics pg) {
        pg.translate(gui.slider("pos x"), gui.slider("pos y"), gui.slider("pos z"));
    }

    private static void rotateXYZ(LazyGui gui, PGraphics pg) {
        pg.rotateX(gui.slider("rot x"));
        gui.sliderAdd("rot x", radians(gui.slider("rot x +")));
        pg.rotateY(gui.slider("rot y"));
        gui.sliderAdd("rot y", radians(gui.slider("rot y +")));
        pg.rotateZ(gui.slider("rot z"));
        gui.sliderAdd("rot z", radians(gui.slider("rot z +")));
    }

    public static void drawSinewaves(String path, LazyGui gui, PGraphics pg) {
        gui.pushFolder(path);
        int count = gui.sliderInt("count", 1);
        if (gui.button("add new")) {
            gui.sliderAdd("count", 1);
        }
        for (int i = 0; i < count; i++) {
            gui.pushFolder("sines[" + i + "]/");
            pg.pushMatrix();
            int detail = gui.sliderInt("detail", 100);
            float freq = gui.slider("freq", 0.01f);
            float sineTime = gui.slider("time", 0);
            float sineTimeDelta = radians(gui.slider("time +", 1));
            gui.sliderSet("time", sineTime + sineTimeDelta);
            PVector pos = gui.plotXY("pos");
            pg.translate(pg.width / 2f + pos.x, pg.height / 2f + pos.y);
            PVector size = gui.plotXY("size", 400, 100);
            pg.rotate(gui.slider("rotate"));
            float w = size.x;
            float h = size.y;
            pg.noFill();
            pg.stroke(gui.colorPicker("stroke", pg.color(255)).hex);
            pg.strokeWeight(gui.slider("weight", 5, 0, app.height));
//            float z = gui.slider("pos z");
            pg.beginShape();
            for (int j = 0; j < detail; j++) {
                float norm = norm(j, 0, detail - 1);
                float x = -w / 2f + w * norm;
                float y = h * sin(norm * freq * TAU + sineTime);

                pg.vertex(x, y); // , z);
            }
            pg.endShape();
            pg.popMatrix();
            gui.popFolder();
        }
        gui.popFolder();
    }

    public static void drawShapes(String path, LazyGui gui, PGraphics pg) {
        if (!path.endsWith("/")) {
            path += "/";
        }
        int count = gui.sliderInt(path + "count", 1);
        if (gui.button(path + "add new")) {
            gui.sliderAdd(path + "count", 1);
        }

        maxShapeCount = max(count, maxShapeCount);
        for (int i = 0; i < maxShapeCount; i++) {
            String iPath = path + "shapes[" + i + "]/";
            gui.pushFolder(iPath);
            if (i >= count) {
                gui.hideCurrentFolder();
                gui.popFolder();
                continue;
            } else {
                gui.showCurrentFolder();
            }
            pg.pushMatrix();
            pg.pushStyle();
            boolean isRect = gui.toggle("rect | ellipse", false);
            boolean center = gui.toggle("corner | center", true);
            boolean cutout = gui.toggle("normal | cutout", false);
            if (center) {
                pg.rectMode(CENTER);
                pg.ellipseMode(CENTER);
            } else {
                pg.rectMode(CORNER);
                pg.ellipseMode(CORNER);
            }
            pg.stroke(gui.colorPicker("stroke", 0xFF000000).hex);
            if (gui.toggle("no stroke")) {
                pg.noStroke();
            }
            pg.strokeWeight(gui.slider("weight", 1));
            pg.fill(gui.colorPicker("fill", 0xFF202020).hex);
            if (gui.toggle("no fill")) {
                pg.noFill();
            }
            float x = pg.width / 2f + gui.slider("pos x");
            float y = pg.height / 2f + gui.slider("pos y");
            float z = gui.slider("pos z", 1);
            float w = gui.slider("size x", 100);
            float h = gui.slider("size y", 100);
            pg.translate(x, y, z);
            pg.rotate(gui.slider("rotate"));
            gui.sliderAdd("rotate", radians(gui.slider("rotate +")));
            pg.translate(gui.slider("pos x 2"), gui.slider("pos y 2"));
            pg.scale(gui.slider("scale x", 1), gui.slider("scale y", 1));
            if (!cutout) {
                if (isRect) {
                    pg.rect(0, 0, w, h);
                } else {
                    pg.ellipse(0, 0, w, h);
                }
            } else {
                pg.beginShape();
                float edgeSize = 3;
                pg.vertex(-pg.width * edgeSize, -pg.height * edgeSize);
                pg.vertex(pg.width * edgeSize, -pg.height * edgeSize);
                pg.vertex(pg.width * edgeSize, pg.height * edgeSize);
                pg.vertex(-pg.width * edgeSize, pg.height * edgeSize);
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
            gui.popFolder();
        }
    }

    public static void drawGrid(String path, LazyGui gui, PGraphics pg) {
        gui.pushFolder(path);
        String fragPath = "_22_11/grid.glsl";
        PickerColor fg = gui.colorPicker("fg", 0xFFFFFFFF);
        int w = pg.width;
        int h = pg.height;
        float z = gui.slider("pos z");
        ShaderReloader.shader(fragPath, pg);
        pg.pushMatrix();
        pg.pushStyle();
        pg.strokeWeight(gui.slider("point weight", 3));
        pg.translate(w / 2f, h / 2f, z);
        int step = gui.sliderInt("step", 20);
        float speedX = gui.slider("speed x");
        float speedY = gui.slider("speed y");
        gridPosX += speedX * step / 360f;
        gridPosY += speedY * step / 360f;
        if (abs(gridPosX) > step) {
            gridPosX %= step;
        }
        if (abs(gridPosY) > step) {
            gridPosY %= step;
        }
        pg.rotate(gui.slider("rotate"));
        pg.translate(gridPosX, gridPosY);
        pg.beginShape(POINTS);
        pg.stroke(fg.hex);
        int overshoot = gui.sliderInt("overshoot", 1);
        for (int x = -w * overshoot; x <= w * overshoot; x += step) {
            for (int y = -h * overshoot; y <= h * overshoot; y += step) {
                pg.vertex(x, y);
            }
        }
        pg.endShape();
        pg.popStyle();
        pg.popMatrix();
        pg.resetShader();
        gui.popFolder();
    }

    public static void drawBackground(String path, LazyGui gui, PGraphics pg) {
        gui.pushFolder(path);
        PickerColor bg = gui.colorPicker("solid background", pg.color(0xFF303030));
        pg.pushStyle();
        pg.noStroke();
        pg.rectMode(CORNER);
        if (gui.toggle("nice fade", false)) {
            int subEveryNFrames = gui.sliderInt("subtract skip frames", 3);
            PickerColor sub = gui.colorPicker("nice subtract", pg.color(0xFF010101));
            PickerColor min = gui.colorPicker("nice minimum", pg.color(0xFF232323));
            if (subEveryNFrames != 0 && backgroundFrameCount++ % subEveryNFrames == 0) {
                pg.blendMode(PConstants.SUBTRACT);
                pg.fill(sub.hex);
                pg.rect(0, 0, pg.width, pg.height);
            }
            pg.blendMode(PConstants.LIGHTEST);
            pg.fill(min.hex);
        } else {
            pg.fill(bg.hex);
        }
        pg.rect(0, 0, pg.width, pg.height);
        pg.popStyle();
        gui.popFolder();
    }
}
