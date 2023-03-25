package _22_11;

import com.krab.lazy.LazyGui;
import com.krab.lazy.PickerColor;
import processing.core.PApplet;
import processing.core.PConstants;
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
    float t;

    public static void main(String[] args) {
        PApplet.main(java.lang.invoke.MethodHandles.lookup().lookupClass());
    }

    public void settings() {
//        size(1200, 800, P2D);
        fullScreen(P2D);
    }

    public void setup() {
        gui = new LazyGui(this);
        pg = createGraphics(width, height, P2D);
        pg.smooth(16);
        colorMode(HSB, 1, 1, 1, 1);
    }

    public void draw() {
        t = radians(frameCount);
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
        int count = gui.sliderInt(folder + "count", 100);
        if (frameCount == 1 || gui.button(folder + "reset") || count != particles.size()) {
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
        private float radius = 1;
        private float mass = 1;

        Particle() {
            pos = new PVector(random(width), random(height));
            spd = PVector.random2D().setMag(random(1));
        }

        void update() {
            String folder = "particles/update/";
            PVector acc = new PVector();

            float yNoiseTime = gui.slider(folder + "y noise time", 5);
            float yNoiseFreq = gui.slider(folder + "y noise freq", 0.01f);
            float yNoiseMag = gui.slider(folder + "y noise mag", 0.5f);
            float yNoise = noise(pos.x * yNoiseFreq, t * yNoiseTime) * yNoiseMag;

            float linearHeightMag = gui.slider(folder + "y linear mag", 0.5f);
            float linearHeightNorm = norm(pos.y, 0, height) * linearHeightMag;
            float forceHeatY = - linearHeightNorm - yNoise;
            acc.y += forceHeatY;

            float xNoiseMag = gui.slider(folder + "x noise mag", 0.02f);
            float xNoiseTime = gui.slider(folder + "x noise time", 1);
            float xNoiseFreq = gui.slider(folder + "x noise freq", 0.1f);
            float xNoiseForceNorm = (-1 + 2 * noise(pos.y * xNoiseFreq, t * xNoiseTime)) * xNoiseMag;
            acc.x += xNoiseForceNorm;

            float gravityY = gui.slider(folder + "y gravity", 0.5f);
            acc.y += gravityY;

            PVector center = new PVector(width/2f, height/2f);
            float toCenterMag = gui.slider(folder + "to center mag", 0.01f);
            PVector toCenter = PVector.sub(center, pos).normalize().mult(toCenterMag);
            acc.add(toCenter);

            PVector repulsion = getGravityTowardsOthers(folder);
            acc.add(repulsion);

            spd.add(acc);
            float drag = gui.slider(folder + "drag", 0.94f);
            spd.mult(drag);
            spd.limit(gui.slider(folder + "speed limit", 10));
            pos.add(spd);

            pos.x = constrain(pos.x, radius, width - radius);
            pos.y = constrain(pos.y, radius, height - radius);
        }

        private PVector getGravityTowardsOthers(String folder) {
            mass = radius * gui.slider(folder + "mass", 1);
            PVector result = new PVector();
            for (Particle p : particles) {
                if (p == this) {
                    continue;
                }
                PVector toThis = PVector.sub(this.pos, p.pos);
                float dist = max(0.0001f, toThis.mag());
                toThis.normalize().mult(gui.slider(folder + "repulsion constant", 0.6f));
                toThis.mult( (this.mass * p.mass) / pow(dist, gui.slider(folder + "repulse dist pow", 2)));
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

            pg.noStroke();
            float sizeMinimum = gui.slider(folder + "size minimum", 12);
            float sizeBase = gui.slider(folder + "size", 10);
            float sizeGaussRange = gui.slider(folder + "size range", 20);
            radius = max(sizeMinimum, sizeBase + sizeGauss * sizeGaussRange);

            float taperPercent = gui.slider(folder + "fading edge", 0.8f);
            float diameter =  radius * 2;
            pg.fill(hue, constrain(sat, 0, 1), pColor.brightness, pColor.alpha);
            pg.ellipse(pos.x, pos.y, diameter * taperPercent, diameter * taperPercent);

            pg.beginShape(TRIANGLE_STRIP);
            int taperDetail = gui.sliderInt(folder + "fade detail", 32);
            for (int i = 0; i < taperDetail; i++) {
                float theta = map(i, 0, taperDetail-1, 0, TWO_PI);
                float x0 = radius * taperPercent * cos(theta);
                float y0 = radius * taperPercent * sin(theta);
                float x1 = radius * cos(theta);
                float y1 = radius * sin(theta);
                pg.fill(hue, constrain(sat, 0, 1), pColor.brightness, pColor.alpha);
                pg.vertex(pos.x + x0, pos.y + y0);
                pg.fill(hue, constrain(sat, 0, 1), pColor.brightness, 0);
                pg.vertex(pos.x + x1, pos.y + y1);
            }
            pg.endShape();

        }

    }
}
