package _23_02.FractalFlame;

import lazy.LazyGui;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PVector;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * The algorithm:
 * Have three non-linear functions that return a new pos based on old pos.
 * Pick random point, iterate 'totalIterations' times, pick a random function for each iteration.
 * Start adding low brightness points to the canvas after 'invisibleIterations'.
 * Blue means that the point hit it at all, red means the number of iters when that point was reached.
 * That creates the histogram.
 * Then render the histogram using logarithmic brightness and iteration count in a shader based filter.
 */

public class FractalFlame extends PApplet {
    private LazyGui gui;
    PGraphics pg;
    ArrayList<PVector> points = new ArrayList<PVector>();

    public static void main(String[] args) {
        PApplet.main(java.lang.invoke.MethodHandles.lookup().lookupClass());
    }

    public void settings() {
        fullScreen(P2D);
    }

    public void setup() {
        gui = new LazyGui(this);
        pg = createGraphics(width, height, P2D);
    }

    public void draw() {
        clear();
        if(gui.toggle("points/update", true)){
            pg.beginDraw();
            if(gui.toggle("background/active", true)){
                pg.blendMode(PConstants.SUBTRACT);
                pg.fill(gui.colorPicker("background/color (sub)", 0xFF000000).hex);
                pg.rect(0,0,width,height);
            }
            pg.blendMode(ADD);
            pg.translate(width/2f, height/2f);
            updatePoints();
            pg.endDraw();
        }
        image(pg, 0, 0);
    }

    private void updatePoints() {
        gui.pushFolder("points");
        int pointCount = gui.sliderInt("count", 100);
        if(gui.button("points.clear()")){
            points.clear();
        }
        int spawnPerFrame = gui.sliderInt("spawn per frame");
        int countToRemove = min(points.size()-1, spawnPerFrame);
        for (int i = 0; i < countToRemove; i++) {
            points.remove(0);
        }
        int itersPerFrame = gui.sliderInt("iters per frame", 5);
        float range = gui.slider("spawn range", 300);

        float lerpAmt = gui.slider("lerp amt", 0.1f);
        pg.stroke(gui.colorPicker("color (add)", 0xFFFFFFFF).hex);
        pg.strokeWeight(1.99f);
        for (int pointIndex = 0; pointIndex < pointCount; pointIndex++) {
            if(pointIndex > points.size() - 1){
                points.add(new PVector(random(-range, range), random(-range, range)));
            }
            PVector p = points.get(pointIndex);
            float x = p.x;
            float y = p.y;
            int functionCount = 3;
            for (int iter = 0; iter < itersPerFrame; iter++) {
                int randomFunctionIndex = floor(random(functionCount));
                if(randomFunctionIndex == 0){
                    x = lerp(x, 0, lerpAmt);
                    y = lerp(y, 0, lerpAmt);
                }
                if(randomFunctionIndex == 1){
                    x = x + sin(y*0.05f);
                    y = y + cos(x*0.05f);
                }
                pg.point(x, y);
            }
            p.x = x;
            p.y = y;
        }
        gui.popFolder();
    }

}
