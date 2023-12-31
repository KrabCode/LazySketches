package _23_12;

import _0_utils.Utils;
import com.krab.lazy.ShaderReloader;
import processing.core.PApplet;
import processing.core.PGraphics;
import com.krab.lazy.LazyGui;
import processing.core.PVector;

public class TextMask extends PApplet {
    LazyGui gui;
    PGraphics bg;
    PGraphics fg;
    private float backgroundTime;

    public static void main(String[] args) {
        PApplet.main(java.lang.invoke.MethodHandles.lookup().lookupClass());
    }

    @Override
    public void settings() {
        size(1080, 1080, P2D);
        smooth(8);
    }

    @Override
    public void setup() {
        gui = new LazyGui(this);
        colorMode(HSB, 1, 1, 1, 1);
        bg = createGraphics(width, height, P2D);
        bg.beginDraw();
        bg.colorMode(HSB, 1, 1, 1, 1);
        bg.endDraw();
        fg = createGraphics(width, height);
        fg.beginDraw();
        fg.colorMode(HSB, 1, 1, 1, 1);
        fg.endDraw();
        frameRate(60);
    }

    @Override
    public void draw() {
        bg.beginDraw();
        drawBackground(bg);
        bg.endDraw();
        image(bg, 0, 0);

        fg.beginDraw();
        drawTextMask(fg);
        fg.endDraw();
        image(fg, 0, 0);

        Utils.record(this, gui);
    }

    private void drawBackground(PGraphics pg) {
        String shaderPath = "_23_12/text_mask.frag";
        backgroundTime += radians(1) * gui.slider("bg time +=", 1);
        ShaderReloader.getShader(shaderPath).set("time", backgroundTime);
        ShaderReloader.filter(shaderPath, pg);
    }

    private void drawTextMask(PGraphics pg) {
        gui.pushFolder("text mask");
        pg.blendMode(BLEND);
        pg.background(gui.colorPicker("background", color(0.1f)).hex);
        pg.blendMode(REPLACE);
        pg.pushMatrix();
        {
            PVector textPos = gui.plotXY("pos");
            int fontSize = gui.sliderInt("size", 32);
            pg.textFont(Utils.getFont(this,
                gui.text("font", "Comic Sans MS"),
                    fontSize
            ));
            pg.translate(width / 2f, height / 2f);
            pg.translate(textPos.x, textPos.y);
            pg.textAlign(CENTER, CENTER);
            pg.fill(gui.colorPicker("text color", color(0.9f)).hex);
            pg.text(gui.text("content", "hello"), 0, 0);
        }
        pg.popMatrix();
        gui.popFolder();

    }
}

