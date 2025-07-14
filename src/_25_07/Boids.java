package _25_07;

import com.krab.lazy.LazyGui;
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
    private float alignRange, cohRange, sepRange;
    private float alignWeight, cohWeight, sepWeight;

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
        pg = createGraphics(width, height, P2D);
        frameRate(60);
        boids.clear();
        player = new Boid(width/2f, height/2f, true);
        boids.add(player); // Add player first
        for (int i = 1; i < boidCount; i++) {
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
            b.display(pg);
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
        // despawn boids that are too far behind the player
        float margin = 200;
        float playerAngle = player.spd.heading();
        PVector playerDir = PVector.fromAngle(playerAngle);
        for (int i = boids.size() - 1; i >= 1; i--) { // skip player at index 0
            Boid b = boids.get(i);
            // Vector from player to boid
            PVector toBoid = PVector.sub(b.pos, player.pos);
            // Project onto player direction
            float forwardDist = toBoid.dot(playerDir);
            float sideDist = toBoid.mag();
            // Only remove if behind the player and off screen
            if (forwardDist < -margin &&
                    (abs(b.pos.x - player.pos.x) > width / 2f + margin ||
                            abs(b.pos.y - player.pos.y) > height / 2f + margin)) {
                boids.remove(i);
            }
        }
        // Spawn new boids in direction of player travel to maintain count
        while (boids.size() < boidCount) {
            float angle = player.spd.heading();
            float dist = max(width, height) * 0.6f;
            float spread = radians(60);
            float spawnAngle = angle + random(-spread / 2, spread / 2);
            float spawnDist = dist + random(-50, 50);
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
        pg.fill(gui.colorPicker("background").hex);
        pg.noStroke();
        pg.rectMode(CORNER);
        pg.rect(0, 0, width, height);
    }

    class Boid {
        PVector pos, spd;
        boolean debug;
        ArrayList<PVector> trail = new ArrayList<>();


        Boid(float x, float y, boolean debug) {
            pos = new PVector(x, y);
            float angle = random(TWO_PI);
            spd = PVector.fromAngle(angle).mult(maxSpeed);
            this.debug = debug;
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

        void display(PGraphics pg) {
            // Draw trail
            if (trail.size() > 1) {
                pg.noFill();
                pg.stroke(255, 80);
                pg.strokeWeight(1);
                pg.beginShape();
                for (int i = 0; i < trail.size(); i++) {
                    PVector p = trail.get(i);
                    pg.curveVertex(p.x, p.y);
                }
                pg.endShape();
            }
            // Draw boid
            pg.fill(255);
            pg.noStroke();
            pg.ellipse(pos.x, pos.y, boidRadius * 2, boidRadius * 2);
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
                pg.stroke(0, 200, 255, 200);
                pg.ellipse(pos.x, pos.y, alignRange * 2, alignRange * 2);
            }
            if (showCoh) {
                pg.stroke(0, 255, 0, 200);
                pg.ellipse(pos.x, pos.y, cohRange * 2, cohRange * 2);
            }
            if (showSep) {
                pg.stroke(255, 0, 0, 200);
                pg.ellipse(pos.x, pos.y, sepRange * 2, sepRange * 2);
            }
            pg.strokeWeight(vectorStrokeWeight);
            // Draw force vectors
            if (showVectors) {
                // align: blue, coh: green, sep: red
                pg.stroke(0, 200, 255);
                drawVector(pos, alignForce, vectorScale);
                pg.stroke(0, 255, 0);
                drawVector(pos, cohForce, vectorScale);
                pg.stroke(255, 0, 0);
                drawVector(pos, sepForce, vectorScale);
            }
            if(showSpeed) {
                pg.stroke(255);
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
