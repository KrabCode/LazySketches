package _22_11;

import _0_utils.Shapes;
import _0_utils.Utils;
import processing.core.PApplet;
import processing.core.PGraphics;
import com.krab.lazy.LazyGui;

public class PyramidHead extends PApplet {
    LazyGui gui;
    PGraphics pg;

    public static void main(String[] args) {
        PApplet.main(java.lang.invoke.MethodHandles.lookup().lookupClass());
    }

    @Override
    public void settings() {
//        size(1080, 1080, P3D);
        fullScreen(P3D);
        smooth(16);
    }

    @Override
    public void setup() {
        gui = new LazyGui(this);
        pg = createGraphics(width, height, P3D);
        smooth(16);
    }

    @Override
    public void draw() {
        pg.beginDraw();
        Shapes.drawBackground("bg", gui, pg);
        Shapes.drawGrid("grid", gui, pg);
        Shapes.drawPyramids("pyramids ΔΔΔ", gui, pg);
        pg.endDraw();
        image(pg, 0, 0);
        gui.draw();
        Utils.record(this, gui);
    }

}

