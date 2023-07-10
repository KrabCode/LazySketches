package _23_05.LitGridGui;

import _0_utils.Utils;
import com.krab.lazy.LazyGui;
import processing.core.PApplet;

public class LitGridGui extends PApplet {
    LazyGui gui;

    public static void main(String[] args) {
        PApplet.main(java.lang.invoke.MethodHandles.lookup().lookupClass());
    }

    @Override
    public void settings() {
//        size(1080,1080,P2D);
        fullScreen(P2D);
    }

    float t = 0;
    float freq = 15; // of the lit sinewave
    int num = 20; // column and row count

    @Override
    public void setup() {
        gui = new LazyGui(this);
    }

    @Override
    public void draw() {
        // read mouse input+
        // enforce odd num for nice centering
        gui.pushFolder("grid");
        num = gui.sliderInt("rows", 20)*2+1;
        freq = gui.slider("freq", 10);

        // w means cell size
        float w = width / (float) num;
        t += radians(gui.slider("time speed", 1));

        // draw the grid
        background(gui.colorPicker("background", color(36)).hex);
        stroke(gui.colorPicker("stroke").hex);
        strokeWeight(gui.slider("weight", 2));
        if(gui.toggle("noStroke()", true)){
            noStroke();
        }
        pushMatrix();
        translate(0, height/2f-width/2f);
        float marginX = gui.slider("margin x", 1);
        float marginY = gui.slider("margin y", 1);
        for (int xi = floor(-num*(marginX-1)); xi < num*marginX; xi++) {
            for (int yi = floor(-num*(marginY-1)); yi < num*marginY; yi++) {
                float x = xi*w;
                float y = yi*w;
                float n = wave(xi, yi, num/2f, num/2f, t);
                fill(gui.gradientColorAt("wave", n).hex);
                rect(x, y, w, w);
            }
        }
        popMatrix();
        gui.popFolder();
        Utils.record(this, gui);
    }

    float wave(int xi, int yi, float centerX, float centerY, float t) {
        float d = dist(xi, yi, centerX, centerY);
        // a sinewave with some frequency
        // grows from the center with time
        // and flips the lit switch at 0
        return 0.5f + 0.5f * sin(-t+d*freq);
    }
}
