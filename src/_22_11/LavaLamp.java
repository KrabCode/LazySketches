package _22_11;

import lazy.LazyGui;
import lazy.PickerColor;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;

import java.util.ArrayList;

import static _0_utils.Utils.hueModulo;
import static _0_utils.Utils.record;

@SuppressWarnings("DuplicatedCode")
public class LavaLamp extends PApplet {
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
        record(this, gui);
    }

    private void drawBackground() {
        String folder = "background/";
        boolean subtract = gui.toggle(folder + "subtract");
        pg.blendMode(subtract ? SUBTRACT : BLEND);
        pg.noStroke();
        pg.fill(gui.colorPicker(folder + "color", color(0.15f)).hex);
        pg.rect(0, 0, width, height);
        pg.blendMode(BLEND);
    }

    private void drawParticles() {
        String folder = "particles/";
        int count = gui.sliderInt(folder + "count", 200);
        if (gui.button(folder + "reset") || count != particles.size()) {
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

    class Particle {
        PVector pos, spd;
        float hueGauss = randomGaussian();
        float satGauss = randomGaussian();
        float sizeGauss = randomGaussian();
        private float radius = 10;

        Particle() {
            pos = new PVector(random(width), random(height));
            spd = PVector.random2D().setMag(random(1));
        }

        void update() {
            String folder = "particles/update/";
            PVector acc = new PVector();
            float heatNoiseTime = gui.slider(folder + "heat noise time", 1);
            float heatNoiseFreq = gui.slider(folder + "heat noise freq", 0.1f);
            float heatNoiseMag = gui.slider(folder + "heat noise mag", 1);
            float heatNoise = noise(pos.x * heatNoiseFreq, radians(frameCount) * heatNoiseTime) * heatNoiseMag;
            float linearColdZoneY = gui.slider(folder + "cold boundary", 0);
            float linearHeightHeat = constrain(norm(pos.y, linearColdZoneY, height), 0, 1) * gui.slider(folder + "heat linear mag", 1);
            float forceHeatY = - (linearHeightHeat + heatNoise);
            acc.y += forceHeatY;
            float gravityY = gui.slider(folder + "gravity", 1);
            acc.y += gravityY;
            float mass = radius * gui.slider(folder + "mass", 1);
            acc.div(mass);
            PVector repulsion = getRepulsion(folder).mult(gui.slider(folder + "repulsion", 1));
            acc.add(repulsion);
            spd.add(acc);
            float drag = gui.slider(folder + "drag", 0.98f);
            spd.mult(drag);
            pos.add(spd);
            if (pos.x > width + radius) {
                pos.x = -radius;
            }
            if (pos.x < -radius) {
                pos.x = width + radius;
            }
            pos.y = constrain(pos.y, 0, height);
        }

        private PVector getRepulsion(String folder) {
            PVector result = new PVector();
            for (Particle p : particles) {
                if (p == this) {
                    continue;
                }
                PVector toThis = PVector.sub(this.pos, p.pos);
                float mag = toThis.mag();
                toThis.normalize();
                toThis.mult(gui.slider(folder + "repulsion dist", 0.1f) / mag);
                result.add(toThis);
            }
            return result;
        }

        void display() {
            String folder = "particles/display/";
            PickerColor pColor = gui.colorPicker(folder + "color", color(0f, 0.5f, 1f));
            float hueGaussRange = gui.slider(folder + "hue gauss", 0.05f);
            float hue = hueModulo(pColor.hue + hueGauss * hueGaussRange);
            float sat = pColor.saturation + satGauss * gui.slider(folder + "sat gauss", 0);
            pg.fill(hue, constrain(sat, 0, 1), pColor.brightness, pColor.alpha);
            pg.noStroke();
            float sizeMinimum = gui.slider(folder + "size minimum", 5);
            float sizeBase = gui.slider(folder + "size", 10);
            float sizeGaussRange = gui.slider(folder + "size range", 20);
            radius = max(sizeMinimum, sizeBase + sizeGauss * sizeGaussRange);
            pg.ellipse(pos.x, pos.y, radius * 2, radius * 2);
        }
    }
}
