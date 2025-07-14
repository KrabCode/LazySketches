package _25_07;

import com.krab.lazy.ColorPoint;
import com.krab.lazy.LazyGui;
import com.krab.lazy.PickerColor;
import processing.core.*;

import java.util.ArrayList;
import java.util.List;

public class Boids extends PApplet {
    LazyGui gui;
    PGraphics pg;
    List<Boid> boids = new ArrayList<>();
    Boid player;
    int boidCount = 100;
    float boidRadius = 2;
    private int trailDepth = 60; // how many frames to keep trail
    float camX = 0, camY = 0;
    float camLerp = 0.1f;
    List<Dust> dusts = new ArrayList<>();
    int dustRate = 10; // particles per frame
    private float maxSpeed = 3;
    private float minSpeed = 1;
    private float alignRange, cohRange, sepRange;
    private float alignWeight, cohWeight, sepWeight;

    public static void main(String[] args) {
        PApplet.main(java.lang.invoke.MethodHandles.lookup().lookupClass());
    }

    @Override
    public void settings() {
//        size(800, 800, P2D);
        fullScreen(P2D);
        smooth(8);
    }

    @Override
    public void setup() {
        gui = new LazyGui(this);
        pg = createGraphics(width, height, P2D);
        frameRate(144);
        boids.clear();
        player = new Boid(width/2f, height/2f, true);
        boids.add(0, player);
        for (int i = 1; i < boidCount / 2f; i++) {
            boids.add(new Boid(random(width), random(height), false));
        }
    }

    @Override
    public void draw() {
        pg.beginDraw();
        drawBackground();
        globalSliders();

        // Camera follows player
        camX = lerp(camX, player.pos.x - width/2f, camLerp);
        camY = lerp(camY, player.pos.y - height/2f, camLerp);
        pg.pushMatrix();
        pg.translate(-camX, -camY);

        updateDrawDust();
        updateBoidPopulation();
        for (Boid b : boids) {
            b.update();
            b.display();
        }
        pg.popMatrix();
        pg.endDraw();
        image(pg, 0, 0);
    }

    private void globalSliders() {
        gui.pushFolder("global");
        boidCount = gui.sliderInt("boid count", boidCount, 1, 500);
        boidRadius = gui.slider("boid radius", boidRadius, 1, 10);
        camLerp = gui.slider("camera lerp", camLerp, 0, 1);
        dustRate = gui.sliderInt("dust rate", dustRate, 0, 100);
        trailDepth = gui.sliderInt("trail length", trailDepth, 1, 200);
        maxSpeed = gui.slider("max speed", maxSpeed);
        minSpeed = gui.slider("min speed", minSpeed);
        gui.popFolder();

        gui.pushFolder("forces");
        alignWeight = gui.slider("align weight", 0.5f);
        alignRange = gui.slider("align range", 50);
        cohWeight = gui.slider("cohesion weight", 0.5f);
        cohRange = gui.slider("cohesion range", 50);
        sepWeight = gui.slider("separation weight", 0.5f);
        sepRange = gui.slider("separation range", 25);
        gui.popFolder();
    }

    private void updateBoidPopulation() {
        // Unified spawn/despawn distance logic
        float centerToCorner = dist(0, 0, width/2f, height/2f);
        float spawnDistMin = gui.slider("global/spawn dist min", 1.5f);
        float spawnDistMax = gui.slider("global/spawn dist max", 2.0f);
        float despawnDist = gui.slider("global/despawn dist", 1.6f);
        float spread = radians(gui.slider("global/spawn spread °", 60));
        float playerAngle = player.spd.heading();
        PVector playerDir = PVector.fromAngle(playerAngle);
        float despawnRadius = despawnDist * centerToCorner;
        // Despawn boids outside despawnRadius behind player
        for (int i = boids.size() - 1; i >= 1; i--) { // skip player at index 0
            Boid b = boids.get(i);
            PVector toBoid = PVector.sub(b.pos, player.pos);
            float forwardDist = toBoid.dot(playerDir);
            float distFromPlayer = toBoid.mag();
            if (forwardDist < 0 && distFromPlayer > despawnRadius) {
                boids.remove(i);
            }
        }
        // Spawn new boids in direction of player travel to maintain count
        while (boids.size() < boidCount) {
            float angle = player.spd.heading();
            float spawnDist = random(spawnDistMin, spawnDistMax) * centerToCorner;
            float spawnAngle = angle + random(-spread / 2, spread / 2);
            float x = player.pos.x + cos(spawnAngle) * spawnDist;
            float y = player.pos.y + sin(spawnAngle) * spawnDist;
            boids.add(new Boid(x, y, false));
        }
    }

    private void updateDrawDust() {
        for (int i = 0; i < dustRate; i++) {
            dusts.add(new Dust(player.pos.x + random(-width, width), player.pos.y + random(-height, height)));
        }
        for (int i = dusts.size() - 1; i >= 0; i--) {
            Dust d = dusts.get(i);
            d.update();
            d.display(pg);
            if (d.isDead()) dusts.remove(i);
        }
    }

    private void drawBackground() {
        pg.fill(gui.colorPicker("global/background").hex);
        pg.noStroke();
        pg.rectMode(CORNER);
        pg.rect(0, 0, width, height);
    }

    class Boid {
        private int clr;
        PVector pos, spd;
        boolean debug;
        ArrayList<PVector> trail = new ArrayList<>();
        private final int alignClr = color(0, 200, 255, 200);
        private final int cohClr = color(0, 255, 0, 200);
        private final int sepClr = color(255, 0, 0, 200);


        Boid(float x, float y, boolean debug) {
            pos = new PVector(x, y);
            float angle = random(TWO_PI);
            spd = PVector.fromAngle(angle).mult(maxSpeed);
            this.debug = debug;
            clr = gui.gradientColorAt("visual/boid color", random(1)).hex;
            if(debug){
                clr = 0xFFFFFFFF;
            }
            if(clr == 0) {
                clr = 0xFFFFFFFF;
            }
        }

        void update() {
            flock();
            pos.add(spd);
            // Add current position to trail
            trail.add(pos.copy());
            while (trail.size() > trailDepth) {
                trail.remove(0);
            }
        }

        void display() {
            drawTrail();
            drawBoid();
        }

        private void drawBoid() {
            float arrowLen = boidRadius * 6;
            float arrowWidth = boidRadius * 2.5f;
            float baseCut = boidRadius * 1.2f;
            float angle = spd.heading();
            drawArrow(pos, angle, arrowLen, arrowWidth, baseCut);
        }

        private void drawTrail() {
            if (trail.size() > 1) {
                pg.noFill();
                pg.stroke(clr);
                pg.strokeWeight(gui.slider("global/trailWeight", 1));
                pg.beginShape();
                for (PVector p : trail) {
                    pg.vertex(p.x, p.y);
                }
                pg.vertex(pos.x, pos.y);
                pg.endShape();
            }
        }

        private void drawArrow(PVector pos, float angle, float arrowLen, float arrowWidth, float baseCut) {
            pg.pushMatrix();
            pg.translate(pos.x, pos.y);
            pg.rotate(angle);
            pg.noStroke();
            pg.fill(clr);
            pg.beginShape();
            // Tip
            pg.vertex(arrowLen/2, 0);
            // Right base
            pg.vertex(-arrowLen/2 + baseCut, arrowWidth/2);
            // Center cut
            pg.vertex(-arrowLen/2, 0);
            // Left base
            pg.vertex(-arrowLen/2 + baseCut, -arrowWidth/2);
            pg.endShape(CLOSE);
            pg.popMatrix();
        }


        void debugVisuals(PVector speedVector,
                          PVector alignForce, float alignRange,
                          PVector cohForce, float cohRange,
                          PVector sepForce, float sepRange) {
            gui.pushFolder("debug visuals");
            boolean showAlign = gui.toggle("show align circle");
            boolean showCoh = gui.toggle("show cohesion circle");
            boolean showSep = gui.toggle("show separation circle");
            boolean showVectors = gui.toggle("show force vectors");
            boolean showSpeed = gui.toggle("show speed vector");
            float circleStrokeWeight = gui.slider("circle stroke weight", 1);
            float vectorStrokeWeight = gui.slider("vector stroke weight", 1);
            float vectorScale = gui.slider("vector scale", 80);
            float speedScale = gui.slider("speed scale", 20);
            // Draw debug circles
            pg.strokeWeight(circleStrokeWeight);
            gui.popFolder();

            pg.noFill();
            if (showAlign) {
                pg.stroke(alignClr);
                pg.ellipse(pos.x, pos.y, alignRange * 2, alignRange * 2);
            }
            if (showCoh) {
                pg.stroke(cohClr);
                pg.ellipse(pos.x, pos.y, cohRange * 2, cohRange * 2);
            }
            if (showSep) {
                pg.stroke(sepClr);
                pg.ellipse(pos.x, pos.y, sepRange * 2, sepRange * 2);
            }
            pg.strokeWeight(vectorStrokeWeight);
            // Draw force vectors
            if (showVectors) {
                // align: blue, coh: green, sep: red
                pg.stroke(alignClr);
                drawVector(pos, alignForce, vectorScale);
                pg.stroke(cohClr);
                drawVector(pos, cohForce, vectorScale);
                pg.stroke(sepClr);
                drawVector(pos, sepForce, vectorScale);
            }
            if(showSpeed) {
                pg.stroke(255, 0, 255);
                drawVector(pos, speedVector, speedScale);
            }
        }

        void drawVector(PVector base, PVector vec, float scale) {
            pg.pushMatrix();
            pg.translate(base.x, base.y);
            pg.line(0, 0, vec.x * scale, vec.y * scale);
            pg.popMatrix();
        }

        private void flock() {
            PVector align = new PVector();
            PVector coh = new PVector();
            PVector sep = new PVector();
            int alignCount = 0, cohCount = 0, sepCount = 0;

            for (Boid other : boids) {
                if (other == this) continue;
                float d = PVector.dist(pos, other.pos);
                // Alignment
                if (d < alignRange) {
                    align.add(other.spd);
                    alignCount++;
                }
                // Cohesion
                if (d < cohRange) {
                    coh.add(other.pos);
                    cohCount++;
                }
                // Separation
                if (d < sepRange) {
                    PVector diff = PVector.sub(pos, other.pos);
                    if (d > 0) diff.div(d); // Weight by distance
                    sep.add(diff);
                    sepCount++;
                }
            }
            if (alignCount > 0) {
                align.div(alignCount);
                align.setMag(maxSpeed);
                align.sub(spd);
                align.limit(0.1f);
            }
            if (cohCount > 0) {
                coh.div(cohCount);
                coh.sub(pos);
                coh.setMag(maxSpeed);
                coh.sub(spd);
                coh.limit(0.1f);
            }
            if (sepCount > 0) {
                sep.div(sepCount);
                sep.setMag(maxSpeed);
                sep.sub(spd);
                sep.limit(0.15f);
            }
            spd.add(PVector.mult(align, alignWeight));
            spd.add(PVector.mult(coh, cohWeight));
            spd.add(PVector.mult(sep, sepWeight));
            spd.limit(maxSpeed);
            if(spd.mag() < minSpeed) {
                spd.setMag(minSpeed); // Prevent zero speed
            }
            if(debug){
                debugVisuals(spd, align, alignRange, coh, cohRange, sep, sepRange);
            }
        }
    }

    class Dust {
        PVector pos;
        float alpha;
        int life, maxLife;
        float radius;
        Dust(float x, float y) {
            pos = new PVector(x, y);
            maxLife = (int)random(60, 120); // longer lived
            life = 0;
            alpha = 255;
            radius = random(0.5f, 1.2f); // smaller
        }
        void update() {
            float t = (float)life / maxLife;
            float fadeInUntil = 0.33f;
            float fadeOutFrom = 0.66f;
            float end = 1;
            if (t < fadeInUntil) {
                alpha = map(t, 0, fadeInUntil, 0, 255);
            } else if (t < fadeOutFrom) {
                alpha = 255;
            } else {
                alpha = map(t, fadeOutFrom, end, 255, 0);
            }
            life++;
        }
        void display(PGraphics pg) {
            pg.noStroke();
            pg.fill(255, 255, 255, alpha);
            pg.ellipse(pos.x, pos.y, radius * 2, radius * 2);
        }
        boolean isDead() {
            return life > maxLife;
        }
    }
}
