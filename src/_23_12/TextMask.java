package _23_12;

import _0_utils.Shapes;
import _0_utils.Utils;
import com.krab.lazy.*;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;

import java.util.HashMap;

public class TextMask extends PApplet {
    private LazyGui gui;
    private PGraphics bg;
    private PGraphics tg;
    private float backgroundTime;
    private HashMap<String, Integer> xAligns;
    private HashMap<String, Integer> yAligns;
    public static void main(String[] args) {
        PApplet.main(java.lang.invoke.MethodHandles.lookup().lookupClass());
    }

    @Override
    public void settings() {
        fullScreen(P3D, 2);
//        size(1080, 1080, P2D);
        smooth(8);
    }

    @Override
    public void setup() {
        gui = new LazyGui(this);
        colorMode(HSB, 1, 1, 1, 1);
        bg = createGraphics(width, height, P3D);
        tg = createGraphics(width, height);
        frameRate(60);
    }

    @Override
    public void draw() {
        bg.beginDraw();
        drawBackground(bg);
        bg.endDraw();
        image(bg, 0, 0);

        PickerColor backgroundColor = gui.colorPicker("background", color(0.1f));
        tg.beginDraw();
        tg.blendMode(BLEND);
        tg.background(backgroundColor.hex);
        tg.blendMode(REPLACE);
        Shapes.drawSinewaves("sines", gui, tg);
        drawTextMask(tg);
        tg.endDraw();
        image(tg, 0, 0);

        Utils.record(this, gui);
    }

    private void drawBackground(PGraphics pg) {
        String shaderPath = "_23_12/text_mask.frag";
        backgroundTime += radians(1) * gui.slider("bg time +=", 1);

        ShaderReloader.getShader(shaderPath).set("time", backgroundTime);
        ShaderReloader.getShader(shaderPath).set("scale", gui.slider("scale", 1));
        ShaderReloader.filter(shaderPath, pg);
    }

    private void drawTextMask(PGraphics pg) {
        if (xAligns == null || yAligns == null) {
            xAligns = new HashMap<>();
            xAligns.put("LEFT", LEFT);
            xAligns.put("CENTER", CENTER);
            xAligns.put("RIGHT", RIGHT);
            yAligns = new HashMap<>();
            yAligns.put("TOP", TOP);
            yAligns.put("CENTER", CENTER);
            yAligns.put("BOTTOM", BOTTOM);
        }

        gui.pushFolder("text mask");
        pg.pushMatrix();
        PVector textPos = gui.plotXY("pos");
        int fontSize = gui.sliderInt("size", 32);
        String fontName = gui.text("font", "Comic Sans MS");
        pg.textFont(Utils.getFont(this, fontName, fontSize));
        pg.translate(width / 2f, height / 2f);
        pg.translate(textPos.x, textPos.y);
        pg.textLeading(fontSize * gui.slider("leading"));
        pg.textAlign(
                xAligns.get(gui.radio("align x", xAligns.keySet().toArray(new String[0]))),
                yAligns.get(gui.radio("align y", yAligns.keySet().toArray(new String[0])))
        );
        pg.fill(gui.colorPicker("text color", color(0.9f)).hex);
        pg.text(gui.text("content", "hello"), 0, 0);
        pg.popMatrix();
        gui.popFolder();
    }
}

