package _0_templates;

import lazy.LazyGui;
import lazy.PickerColor;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;

import java.util.ArrayList;

import static _0_utils.Utils.hueModulo;

public class TemplateParticles extends PApplet {
    LazyGui gui;
    PGraphics pg;
    private final ArrayList<Particle> particles = new ArrayList<>();

    public static void main(String[] args) {
        PApplet.main(java.lang.invoke.MethodHandles.lookup().lookupClass());
    }

    public void settings() {
        size(1200, 800, P2D);
    }

    public void setup() {
        gui = new LazyGui(this);
        pg = createGraphics(width, height, P2D);
        pg.smooth(16);
        colorMode(HSB, 1, 1, 1, 1);
    }

    public void draw() {
        pg.beginDraw();
        pg.colorMode(HSB, 1, 1, 1, 1);
        drawBackground();
        drawParticles();
        pg.endDraw();
        image(pg, 0, 0);
    }

    private void drawBackground() {
        String folder = "bg/";
        boolean subtract = gui.toggle(folder + "subtract");
        pg.blendMode(subtract ? SUBTRACT : BLEND);
        pg.noStroke();
        pg.fill(gui.colorPicker(folder + "color", color(0.15f)).hex);
        pg.rect(0, 0, width, height);
        pg.blendMode(BLEND);
    }

    private void drawParticles() {
        String folder = "particles/";
        int count = gui.sliderInt(folder + "count", 12);
        if (count != particles.size()) {
            particles.clear();
            for (int i = 0; i < count; i++) {
                particles.add(new Particle());
            }
        }
        for (Particle p : particles) {
            p.update();
        }
        for (Particle p : particles) {
            p.display();
        }
    }

    @SuppressWarnings("DuplicatedCode")
    class Particle {
        PVector pos, spd;
        float hueGauss = randomGaussian();
        float sizeGauss = randomGaussian();
        private float radius = 10;

        Particle() {
            pos = new PVector(random(width), random(height));
            spd = new PVector();
        }

        void update() {

        }

        void display() {
            String folder = "particles/display/";
            PickerColor pColor = gui.colorPicker(folder + "color", color(1));
            float hueGaussRange = gui.slider(folder + "hue gauss", 0);
            float hue = hueModulo(pColor.hue + hueGauss * hueGaussRange);
            pg.fill(hue, pColor.saturation, pColor.brightness, pColor.alpha);
            pg.noStroke();
            float sizeBase = gui.slider(folder + "size", 10);
            float sizeGaussRange = gui.slider(folder + "size range", 5);
            radius = sizeBase + sizeGauss * sizeGaussRange;
            pg.ellipse(pos.x, pos.y, radius * 2, radius * 2);
        }
    }
}
