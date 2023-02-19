package _23_02.FractalFlame;

import _0_utils.Shapes;
import _0_utils.Utils;
import _22_03.PostFxAdapter;
import lazy.LazyGui;
import lazy.ShaderReloader;
import processing.core.*;
import processing.opengl.PShader;

import java.util.ArrayList;

/**
 * based on: <a href="https://flam3.com/flame_draves.pdf">The Fractal Flame Algorithm</a>
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
    PGraphics pg, fg;
    ArrayList<ArrayList<Point>> points = new ArrayList<>();

    public static void main(String[] args) {
        PApplet.main(java.lang.invoke.MethodHandles.lookup().lookupClass());
    }

    public void settings() {
        fullScreen(P2D);
    }

    public void setup() {
        gui = new LazyGui(this);
        pg = createGraphics(width, height, P2D);
        fg = createGraphics(width, height, P2D);
    }

    public void draw() {
        clear();
        pg.beginDraw();
        if (gui.toggle("update scene", true)) {
            pg.noStroke();
            if (gui.toggle("background/active", true)) {
                pg.blendMode(PConstants.SUBTRACT);
                pg.fill(gui.colorPicker("background/subtract", 0xFF000000).hex);
                pg.rect(0, 0, width, height);
                pg.blendMode(PConstants.ADD);
                pg.fill(gui.colorPicker("background/add", 0xFF000000).hex);
                pg.rect(0, 0, width, height);
            }

            pg.translate(width / 2f, height / 2f);
            gui.pushFolder("point clusters");
            int emitterCount = gui.sliderInt("cluster count");
            for (int i = 0; i < emitterCount; i++) {
                if (i >= points.size()) {
                    points.add(new ArrayList<>());
                }
                gui.pushFolder("cluster " + i);
                updatePoints(points.get(i));
                gui.popFolder();
            }
            gui.popFolder();
            pg.blendMode(BLEND);
        }
        pg.endDraw();

        gui.pushFolder("histogram shader");
        fg.beginDraw();
        String shaderPath = gui.text("shader path", "_23_02/FractalFlame/histogramInterpreter.glsl");

        if (gui.toggle("active")) {
            PShader shader = ShaderReloader.getShader(shaderPath);
            shader.set("time", radians(frameCount));
            shader.set("histogram", pg);
            PGraphics palette = gui.gradient("palette");
            shader.set("palette", palette);
            ShaderReloader.filter(shaderPath, fg);
        }
        gui.popFolder();
        gui.pushFolder("text");
        Shapes.drawSimpleText("text 1/", gui, fg);
        Shapes.drawSimpleText("text 2/", gui, fg);
        gui.popFolder();
        fg.endDraw();
        PostFxAdapter.apply(this, gui, fg);
        image(fg, 0, 0);

        Utils.record(this, gui);
    }

    private void updatePoints(ArrayList<Point> points) {
        int pointCount = gui.sliderInt("point count", 100);
        if (gui.button("points.clear()")) {
            points.clear();
        }
        int spawnPerFrame = gui.sliderInt("spawn per frame");
        int countToRemove = min(points.size() - 1, spawnPerFrame);
        for (int i = 0; i < countToRemove; i++) {
            points.remove(0);
        }
        int itersPerFrame = gui.sliderInt("iters per frame", 5);
        float range = gui.slider("spawn range", 300);
        pg.strokeWeight(gui.slider("stroke weight", 1.99f));
        int invisibleIterCount = gui.sliderInt("invis iters", 0);
        int pointColor = gui.colorPicker("point add", 0xFFFFFFFF).hex;
        ArrayList<Function> functions = rebuildFunctions();
        for (int pointIndex = 0; pointIndex < pointCount; pointIndex++) {
            if (pointIndex > points.size() - 1) {
                points.add(new Point(random(-range, range), random(-range, range)));
            }
            Point p = points.get(pointIndex);
            for (int iter = 0; iter < itersPerFrame; iter++) {
                if(functions.size() > 0){

                    int randomFunctionIndex = floor(random(functions.size()));
                    p = (Point) functions.get(randomFunctionIndex).transform(p);
                }
                if (p.iters++ < invisibleIterCount) {
                    continue;
                }
                pg.blendMode(ADD);
                pg.stroke(pointColor);
                pg.point(p.x, p.y);
            }
        }
    }

    private ArrayList<Function> rebuildFunctions() {
        ArrayList<Function> functions = new ArrayList<>();
        gui.pushFolder("functions");
        gui.pushFolder("lerp");
        if (gui.toggle("active")) {
            PVector lerpCenter = gui.plotXY("pos");
            float lerpAmt = gui.slider("amount", 0.1f);
            int lerpSideCount = gui.sliderInt("sides", 6);
            float shapeRadius = gui.slider("radius", 600);
            float lerpAngleOffset = PI * gui.slider("angle");
            functions.add(p -> {
                if (lerpAmt > 0) {
                    int randomSide = floor(random(lerpSideCount));
                    float angle = lerpAngleOffset + TAU * norm(randomSide, 0, lerpSideCount);
                    float cornerX = lerpCenter.x + shapeRadius * cos(angle);
                    float cornerY = lerpCenter.y + shapeRadius * sin(angle);
                    p.x = lerp(p.x, cornerX, lerpAmt);
                    p.y = lerp(p.y, cornerY, lerpAmt);
                }
                return p;
            });
        }
        gui.popFolder();

        gui.pushFolder("sinusoidal");
        if (gui.toggle("active")) {
            float sinRadius = gui.slider("sin r", 300);
            functions.add(p -> {
                p.x = sin(p.x) * sinRadius;
                p.y = sin(p.y) * sinRadius;
                return p;
            });
        }
        gui.popFolder();

        gui.pushFolder("spherical");
        if (gui.toggle("active")) {
            functions.add(p -> {
                        float r = dist(p.x, p.y, 0, 0);
                        p.x = 1f / pow(r, 2) * p.x;
                        p.y = 1f / pow(r, 2) * p.y;
                        return p;
            });
        }
        gui.popFolder();
        gui.popFolder();
        return functions;
    }

    static class Point extends PVector {
        int iters;

        public Point(float x, float y) {
            super(x, y);
        }
    }

    interface Function {
        PVector transform(PVector p);
    }

}
