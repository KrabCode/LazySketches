package _24_08.feedback;

import _0_utils.Utils;
import com.krab.lazy.LazyGui;
import com.krab.lazy.PickerColor;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;

public class Feedback extends PApplet {

    private LazyGui gui;
    private PGraphics pg;

    public static void main(String[] args) {
        PApplet.main(java.lang.invoke.MethodHandles.lookup().lookupClass());
    }

    @Override
    public void settings() {
        fullScreen(P2D);
        smooth(8);
    }

    @Override
    public void setup() {
        frameRate(144);
        gui = new LazyGui(this);
        pg = createGraphics(width, height, P2D);
    }

    public void draw() {
        gui.pushFolder("scene");
        pg.beginDraw();
        drawCanvasOnItselfRecursively();
        drawBackground();
        drawForegroundShape();
        Utils.shaderMove(pg, gui);
        pg.endDraw();
        image(pg, 0, 0);
        gui.popFolder();
        Utils.record(this, gui);
    }

    void drawCanvasOnItselfRecursively() {
        gui.pushFolder("recursion");
        pg.pushMatrix();
        pg.translate(pg.width * .5f, pg.height * .5f);
        pg.translate(gui.plotXY("pos").x, gui.plotXY("pos").y);
        pg.rotate(gui.slider("rotate", 0.43f));
        float scale = gui.slider("scale", 1.05f);
        pg.scale(scale);
        pg.imageMode(CENTER);
        pg.image(pg, 0, 0);
        pg.popMatrix();
        gui.popFolder();
    }

    void drawBackground() {
        gui.pushFolder("background");
        if (frameCount % max(1, gui.sliderInt("frameSkip", 1, 1, 100)) != 0) {
            gui.popFolder();
            return;
        }


        int solidBackgroundColor = gui.colorPicker("solid", color(0xFF050705)).hex;
        PGraphics gradient = gui.gradient("gradient");
        boolean useGradient = gui.toggle("solid\\/gradient");
        if (useGradient) {
            pg.imageMode(CORNER);
            pg.image(gradient, 0, 0);
        } else {
            if (gui.toggle("subtract", true)) {
                pg.blendMode(SUBTRACT);
            }
            pg.noStroke();
            pg.fill(solidBackgroundColor);
            pg.rectMode(CORNER);
            pg.rect(0, 0, width, height);
        }
        pg.blendMode(BLEND);
        gui.popFolder();
    }

    void drawForegroundShape() {
        gui.pushFolder("foreground");

        PVector pos = gui.plotXY("position");
        PVector size = gui.plotXY("size", 50, 125);
        float rotationAngle = gui.slider("rotation");

        float rotateDelta = gui.slider("rotation ++", -0.22f, -10, 10);

        gui.sliderSet("rotation", rotationAngle + rotateDelta);
        float gradientNorm = gui.slider("gradient norm", 0.5f);
        gui.sliderSet("gradient norm", gradientNorm + gui.slider("gradient speed", 0.01f));
        PickerColor fill = gui.gradientColorAt("gradient", gradientNorm % 1);
        pg.fill(fill.hex);

        pg.stroke(gui.colorPicker("stroke", 0x1EFFFFFF).hex);
        pg.strokeWeight(gui.slider("stroke weight", 1));
        if (gui.toggle("no stroke", true)) {
            pg.noStroke();
        }

        pg.pushMatrix();
        pg.translate(width / 2f, height / 2f);
        pg.translate(pos.x, pos.y);
        pg.rotate(radians(rotationAngle));

        String selectedShape = gui.radio("shape type", new String[]{"ellipse", "rectangle"});
        boolean shouldDrawEllipse = selectedShape.equals("ellipse");
        if (shouldDrawEllipse) {
            pg.ellipse(0, 0, size.x, size.y);
        } else {
            pg.rectMode(CENTER);
            pg.rect(0, 0, size.x, size.y);
        }
        gui.popFolder();

        drawForegroundText();

        pg.popMatrix();
    }


    void drawForegroundText() {
        gui.pushFolder("text");
        String labelText = gui.text("content", "hello");
        PVector pos = gui.plotXY("pos", 30, -120);
        pg.translate(pos.x, pos.y);
        pg.rotate(gui.slider("rotate"));
        Utils.font(this, pg, gui);
        pg.text(labelText, 0, 0);
        gui.popFolder();
    }


}
