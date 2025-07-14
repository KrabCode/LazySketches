package _25_07;

import _0_utils.Utils;
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
    private float maxSpeed = 3;
    private float minSpeed = 1;
    private float alignRange, cohRange, sepRange;
    private float alignWeight, cohWeight, sepWeight;
    PVector debugTarget = null;
    int debugTargetFrames = 0;

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
        frameRate(60);
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
        for (int i = boids.size() - 1; i >= 0; i--) {
            Boid b = boids.get(i);
            b.update();
            b.drawTrail();
        }
        for (int i = boids.size() - 1; i >= 0; i--) {
            Boid b = boids.get(i);
            b.drawBoid();
        }
        // Draw debug rectangle if enabled
        if (gui.toggle("debug/show target rect") && debugTarget != null && debugTargetFrames > 0) {
            pg.noFill();
            pg.stroke(255, 0, 0);
            float size = 40;
            pg.rect(debugTarget.x - size/2, debugTarget.y - size/2, size, size);
        }
        debugTargetFrames--;
        if (debugTargetFrames <= 0) {
            debugTarget = null; // Clear after showing for a while
        }
        pg.popMatrix();
        pg.endDraw();
        image(pg, 0, 0);
        gui.draw();
        Utils.record(this, gui);
    }

    @Override
    public void mousePressed() {
        if(gui.isMouseOverGui()){
            return;
        }
        // Convert mouse to world coordinates
        float worldX = mouseX + camX;
        float worldY = mouseY + camY;
        PVector target = new PVector(worldX, worldY);
        // Find closest boids (excluding player)
        ArrayList<Boid> candidates = new ArrayList<>(boids);
        candidates.remove(player);
        // Only consider boids within a reasonable distance (screen diagonal)
        float maxDist = dist(0, 0, width, height);
        ArrayList<Boid> closeBoids = new ArrayList<>();
        for (Boid b : candidates) {
            if (b.pos.dist(player.pos) < maxDist) {
                closeBoids.add(b);
            }
        }
        // Sort by distance to player
        closeBoids.sort((a, b) -> Float.compare(a.pos.dist(player.pos), b.pos.dist(player.pos)));
        int nFriends = gui.sliderInt("mouse/friend count", 2, 1, 20);
        // Pick up to nFriends closest
        ArrayList<Boid> chosen = new ArrayList<>();
        for (int i = 0; i < min(nFriends, closeBoids.size()); i++) {
            chosen.add(closeBoids.get(i));
        }
        // Set override target for player and friends
        player.setOverrideTarget(target.copy());
        for (Boid b : chosen) {
            b.setOverrideTarget(target.copy());
        }
        // For debug rectangle
        debugTarget = target.copy();
        debugTargetFrames = 60; // show for 1 second
    }

    private void globalSliders() {
        gui.pushFolder("global");
        boidCount = gui.sliderInt("boid count", boidCount, 1, 500);
        boidRadius = gui.slider("boid radius", boidRadius, 1, 100);
        camLerp = gui.slider("camera lerp", camLerp, 0, 1);
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
        int dustRate = gui.sliderInt("dust/dust rate", 10, 0, 100);
        float dustLimit = gui.slider("dust/dust limit", 1000, 100, 5000);
        boolean snapToGrid = gui.toggle("dust/snap to grid");
        float gridSize = gui.slider("dust/grid size", boidRadius * 2, 1, 100);
        for (int i = 0; i < dustRate; i++) {
            if( dusts.size() > dustLimit) {
                // Limit dust count to prevent performance issues
                break;
            }
            PVector dustPos = new PVector(
                    player.pos.x + random(-width, width),
                    player.pos.y + random(-height, height)
            );
            if (snapToGrid) {
                // Snap to grid based on boid radius
                dustPos.x = round(dustPos.x / gridSize) * gridSize;
                dustPos.y = round(dustPos.y / gridSize) * gridSize;
            }
            dusts.add(new Dust(dustPos));
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
        private float clrPos;
        PVector pos, spd;
        boolean debug;
        ArrayList<PVector> trail = new ArrayList<>();
        private final int alignClr = color(0, 200, 255, 200);
        private final int cohClr = color(0, 255, 0, 200);
        private final int sepClr = color(255, 0, 0, 200);
        PVector overrideTarget = null;
        boolean overrideActive = false;


        Boid(float x, float y, boolean debug) {
            pos = new PVector(x, y);
            float angle = random(TWO_PI);
            spd = PVector.fromAngle(angle).mult(maxSpeed);
            this.debug = debug;
            clrPos = random(1);
            if(debug){
                clrPos = 0;
            }
        }

        void setOverrideTarget(PVector t) {
            overrideTarget = t;
            overrideActive = true;
        }

        void update() {
            clr = gui.gradientColorAt("visual/boid color", clrPos).hex;
            if (overrideActive && overrideTarget != null) {
                // Move toward target using steering acceleration
                PVector toTarget = PVector.sub(overrideTarget, pos);
                float dist = toTarget.mag();
                float speed = maxSpeed;
                if (dist < boidRadius * 4) {
                    overrideActive = false;
                    overrideTarget = null;
                } else {
                    // Steering: desired velocity - current velocity
                    toTarget.setMag(speed);
                    PVector steer = PVector.sub(toTarget, spd);
                    float steerStrength = gui.slider("mouse/steer weight",0.01f); // how quickly to turn
                    steer.limit(speed * steerStrength);
                    spd.add(steer);
                    spd.limit(maxSpeed * 2); // allow faster speeds when steering
                }
            } else {
                flock();
            }
            pos.add(spd);
            // Add current position to trail
            trail.add(pos.copy());
            while (trail.size() > trailDepth) {
                trail.remove(0);
            }
        }

        private void drawBoid() {
            float arrowLen = boidRadius * 6;
            float arrowWidth = boidRadius * 2.5f;
            float baseCut = boidRadius * 1.2f;
            float angle = spd.heading();
            drawArrow(pos, angle, arrowLen, arrowWidth, baseCut);
        }
        void drawArrow(PVector pos, float angle, float len, float width, float baseCut) {
            pg.pushMatrix();
            pg.translate(pos.x, pos.y);
            pg.rotate(angle);
            pg.fill(clr);
            pg.noStroke();
            pg.beginShape();
            // Tip
            pg.vertex(len, 0);
            // Right base
            pg.vertex(-baseCut, width / 2);
            // Tail
            pg.vertex(-len / 2, 0);
            // Left base
            pg.vertex(-baseCut, -width / 2);
            pg.endShape(PConstants.CLOSE);
            pg.popMatrix();
        }

        private void drawTrail() {
            if (trail.size() > 1) {
                // Ribbon parameters from GUI
                gui.pushFolder("ribbons");
                float widthScale = gui.slider("width", boidRadius * 1.2f, 0.1f, 100);
                float minWidth = gui.slider("min width", 0.1f, 0.01f, 10);
                float headTaper = gui.slider("head taper", 0.4f, 0, 1);
                float alpha = gui.slider("alpha", 120, 0, 255);
                gui.popFolder();
                pg.noStroke();
                pg.fill(clr, (int)alpha);
                pg.beginShape(PConstants.TRIANGLE_STRIP);
                for (int i = 0; i < trail.size(); i++) {
                    PVector p = trail.get(i);
                    // Direction for width
                    PVector dir;
                    if (i == 0) {
                        dir = PVector.sub(trail.get(i+1), p);
                    } else if (i == trail.size() - 1) {
                        dir = PVector.sub(p, trail.get(i-1));
                    } else {
                        dir = PVector.sub(trail.get(i+1), trail.get(i-1));
                    }
                    dir.normalize();
                    float t = 1f - (i / (float)(trail.size()-1));
                    // Taper both head and tail
                    float w = lerp(minWidth, widthScale, smoothstep(headTaper, 1.0f, t));
                    // Perpendicular
                    float px = -dir.y * w;
                    float py = dir.x * w;
                    // Left
                    pg.vertex(p.x + px, p.y + py);
                    // Right
                    pg.vertex(p.x - px, p.y - py);
                }
                pg.endShape();
            }
        }
        // Helper for smooth taper
        private float smoothstep(float edge0, float edge1, float x) {
            x = constrain((x - edge0) / (edge1 - edge0), 0, 1);
            return x * x * (3 - 2 * x);
        }


        void debugVisuals(PVector speedVector,
                          PVector alignForce, float alignRange,
                          PVector cohForce, float cohRange,
                          PVector sepForce, float sepRange) {
            gui.pushFolder("debug");
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

            // --- Noise force ---
            float noiseAmp = gui.slider("noise/amp", 0.2f);
            float noiseFreq = gui.slider("noise/freq", 0.01f);
            float nx = noiseFreq * pos.x;
            float ny = noiseFreq * pos.y;
            float nt = noiseFreq * frameCount * 0.5f;
            float angle = noise(nx, ny, nt) * TWO_PI * 2;
            PVector noiseForce = PVector.fromAngle(angle).mult(noiseAmp);
            spd.add(noiseForce);
            // --- End noise force ---

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
        float minRadius = boidRadius * 0.5f;
        float maxRadius = boidRadius * 1.5f;
        float radiusPos;
        float minStartLife = 60;
        float maxStartLife = 120;
        private final float randomRotation = random(TAU);

        Dust(PVector pos) {
            this.pos = pos.copy();
            maxLife = (int)random(minStartLife, maxStartLife);
            life = 0;
            alpha = 255;
            radiusPos = random(1);
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
            gui.pushFolder("dust");

            minStartLife = gui.slider("min start life", 60);
            maxStartLife = gui.slider("max start life", 120);
            minRadius = gui.slider("min radius", boidRadius * 0.5f);
            maxRadius = gui.slider("max radius", boidRadius * 1.5f);

            float radius = lerp(minRadius, maxRadius, radiusPos);
            pg.noStroke();
            pg.fill(gui.colorPicker("color").hex, alpha);
            pg.pushMatrix();
            pg.translate(pos.x, pos.y);
            if(gui.toggle("rotate auto")) {
                pg.rotate(radians(frameCount) * gui.slider("rotation speed", 0.1f));
            }
            pg.rotate(gui.slider("rotation pos", 0));
            if(gui.toggle("rotate random")) {
                pg.rotate(randomRotation);
            }
            pg.scale(gui.slider("scale", 1));
            gui.popFolder();
            pg.rectMode(PConstants.CENTER);
            pg.rect(0, 0, radius, radius);
            pg.popMatrix();
        }
        boolean isDead() {
            return life > maxLife;
        }
    }
}
