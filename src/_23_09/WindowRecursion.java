package _23_09;

import _0_utils.Utils;
import com.krab.lazy.LazyGui;
import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;

public class WindowRecursion extends PApplet {
    LazyGui gui;
    PImage seedImage;

    public static void main(String[] args) {
        PApplet.main(java.lang.invoke.MethodHandles.lookup().lookupClass());
    }

    @Override
    public void settings() {
        size(800, 800, P2D);
    }

    @Override
    public void setup() {
        gui = new LazyGui(this);
        resetCanvas();
    }

    private void resetCanvas() {
        background(36f);
        if (seedImage == null) {
            seedImage = loadImage("https://picsum.photos/id/237/800/800.jpg");
        }
        imageMode(CORNER);
        image(seedImage, 0, 0);
    }

    @Override
    public void draw() {
        if (gui.button("reset canvas")) {
            resetCanvas();
        }
        Utils.record(this, gui);
        PVector rectPos = gui.plotXY("input center", width * 0.5f, height * 0.5f);
        PVector rectSize = gui.plotXY("input size", 780);
        PImage rect = get(
                floor(rectPos.x - rectSize.x * 0.5f),
                floor(rectPos.y - rectSize.y * 0.5f),
                floor(rectSize.x),
                floor(rectSize.y)
        );
        translate(width * 0.5f, height * 0.5f);
        PVector pos = gui.plotXY("out center");
        translate(pos.x, pos.y);
        rotate(gui.slider("out rotate", 0f));
        scale(gui.slider("out scale", 1));
        imageMode(CENTER);
        image(rect, 0, 0);
    }
}
