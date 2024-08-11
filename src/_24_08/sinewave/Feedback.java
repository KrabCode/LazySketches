package _24_08.sinewave;

import _0_utils.Utils;
import com.krab.lazy.LazyGui;
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
//        size(1200, 800, P2D);
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
        // change the current GUI folder to go into the "scene" folder
        gui.pushFolder("scene");
        pg.beginDraw();
        drawCanvasOnItselfRecursively();
        drawBackground();
        drawForegroundShape();
        pg.endDraw();
        image(pg, 0, 0);
        // go one level up from the current folder
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
        if(frameCount % gui.sliderInt("frameSkip", 1) != 0) {
            gui.popFolder();
            return;
        }
        // the controls are ordered on screen by which gets called first
        // so it can be better to ask for all the values before any if-statement branching
        // because this way you can enforce any given ordering of them in the GUI
        // and avoid control elements appearing suddenly at runtime at unexpected places
        int solidBackgroundColor = gui.colorPicker("solid", color(0xFF050705)).hex;
        PGraphics gradient = gui.gradient("gradient");
        boolean useGradient = gui.toggle("solid\\/gradient"); // here '\\' escapes the '/' path separator
        if (useGradient) {
            pg.imageMode(CORNER);
            pg.image(gradient, 0, 0);
        } else {
            if (gui.toggle("subtract", true)) {
                pg.blendMode(SUBTRACT); // when fading out - subtract gets rid of traces that low alpha background doesn't
            }
            pg.noStroke();
            pg.fill(solidBackgroundColor);
            pg.rectMode(CORNER);
            pg.rect(0, 0, width, height);
        }
        pg.blendMode(BLEND); // reset blend mode to default
        gui.popFolder();
    }

    void drawForegroundShape() {
        // go into a new "shape" folder nested inside the current folder
        gui.pushFolder("foreground");

        // get various values from the GUI using a unique path and an optional default value parameter
        PVector pos = gui.plotXY("position");
        PVector size = gui.plotXY("size", 50, 125);
        float rotationAngle = gui.slider("rotation");

        // enforce a minimum and maximum value on sliders with the min/max parameters (-10, 10) here
        float rotateDelta = gui.slider("rotation ++", -0.22f, -10, 10);

        // change GUI values from code
        gui.sliderSet("rotation", rotationAngle + rotateDelta);
        pg.fill(gui.colorPicker("fill", color(0x6B9BC1FF)).hex);
        gui.colorPickerHueAdd("fill", radians(gui.slider("fill hue ++", 0.08f)));

        // plug GUI values directly into where they get consumed
        pg.stroke(gui.colorPicker("stroke", 0x1EFFFFFF).hex);
        pg.strokeWeight(gui.slider("stroke weight", 1));
        if (gui.toggle("no stroke", true)) {
            pg.noStroke();
        }

        pg.pushMatrix();
        pg.translate(width/2f, height/2f);
        pg.translate(pos.x, pos.y);
        pg.rotate(radians(rotationAngle));

        // pick one string from an array using gui.radio()
        String selectedShape = gui.radio("shape type", new String[]{"ellipse", "rectangle"});
        boolean shouldDrawEllipse = selectedShape.equals("ellipse");
        if (shouldDrawEllipse) {
            pg.ellipse(0, 0, size.x, size.y);
        } else {
            pg.rectMode(CENTER);
            pg.rect(0, 0, size.x, size.y);
        }
        // go up one level back into the "scene" folder
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
