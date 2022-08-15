package _22_07;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;
import lazy.LazyGui;

import java.util.ArrayList;

public class ParallaxLayers extends PApplet {
    LazyGui gui;
    PGraphics pg;
    float time;

    ArrayList<Layer> layers = new ArrayList<Layer>();

    public static void main(String[] args) {
        PApplet.main(java.lang.invoke.MethodHandles.lookup().lookupClass());
    }

    @Override
    public void settings() {
        size(1080, 1080, P2D);
    }

    @Override
    public void setup() {
        gui = new LazyGui(this);
        pg = createGraphics(width, height, P2D);
    }

    @Override
    public void draw() {
        pg.beginDraw();
        time += radians(gui.slider("time", 1));
        drawBackground();
        pg.translate(width / 2f, height / 2f);
        drawLayers();
        pg.endDraw();
        image(pg, 0, 0);
    }

    private void drawLayers() {
        int layerCount = gui.sliderInt("layers/count", 5);
        if (layerCount != layers.size()) {
            layers = generateLayers(layerCount);
        }
        for (Layer layer : layers) {
            layer.draw();
        }
    }

    private ArrayList<Layer> generateLayers(int count) {
        ArrayList<Layer> result = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            result.add(new Layer(i, norm(i, 0, count - 1)));
        }
        return result;
    }

    private void drawBackground() {
        pg.fill(gui.colorPicker("background").hex);
        pg.noStroke();
        pg.rectMode(CORNER);
        pg.rect(0, 0, width, height);
    }

    class Layer {
        int index;
        float norm;
        ArrayList<Point> points = new ArrayList<Point>();

        public Layer(int index, float norm) {
            this.norm = norm;
            this.index = index;
        }

        public void draw() {
            float randomRadius = gui.slider("points/random radius", 1000);
            int count = gui.sliderInt("points/per layer", 16);
            if(gui.button("points/clear()")){
                points.clear();
            }
            while (points.size() < count) {
                points.add(new Point(new PVector(
                        randomGaussian() * randomRadius,
                        randomGaussian() * randomRadius
                )));
            }
            while (points.size() > count) {
                points.remove(0);
            }
            for (Point p : points) {
                PVector offPos = getOffsetPoint(p);
                float radius = gui.slider("points/base radius") + gui.slider("points/norm radius") * norm;
                pg.noStroke();
                pg.fill(lerpColor(
                        gui.colorPicker("points/color 0").hex,
                        gui.colorPicker("points/color 1").hex,
                        norm
                ));
                pg.ellipse(offPos.x, offPos.y, radius * 2, radius * 2);
            }
        }

        private PVector getOffsetPoint(Point p) {
            float pNorm = norm(points.indexOf(p), 0, points.size()-1);
            float noiseFreq = gui.slider("points/base freq") + gui.slider("points/norm freq") * norm;
            float noiseAmp = gui.slider("points/base amp") + gui.slider("points/norm amp") * norm;
            PVector offset = new PVector(
                    -1+2*noise(noiseFreq*pNorm, time),
                    -1+2*noise(noiseFreq*pNorm+743.12f, time-23.12f)
            ).mult(noiseAmp);
            return PVector.add(p.pos, offset);
        }
    }


    class Point{
        PVector pos;

        public Point(PVector pos) {
            this.pos = pos;
        }
    }
}

