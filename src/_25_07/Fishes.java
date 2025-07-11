package _25_07;

import com.krab.lazy.LazyGui;
import processing.core.PApplet;
import processing.core.PVector;

import java.util.ArrayList;

public class Fishes extends PApplet {

    private int fishColorA = color(255, 0, 0);
    private int fishColorB = color(0, 0, 255);
    PVector target, cameraOffset;
    float centerToCornerDistance;
    float farSpawnDistance;
    float alignRadius = 200;
    float alignWeight = 0.5f;
    float centralizeRadius = 283;
    float centralizeWeight = 0.5f;
    float avoidRadius = 30;
    float avoidWeight = 5f;
    float minSpeed = 6;
    float maxSpeed = 16;
    float globalDrag = 0.95f;
    float randomMag = 0.5f;
    float minFishSize = 10;
    float maxFishSize = 20;
    int fishCount = 50;
    private final float animationSpeedIdle = radians(0.5f); // Speed of the fish animation
    private final float animationSpeedFast = radians(1f); // Speed of the fish animation
    int dustCount = 300;

    Fish player;
    ArrayList<Fish> allFish = new ArrayList<Fish>();
    ArrayList<Fish> fishToRemove = new ArrayList<Fish>();
    private boolean targetActive = false;
    LazyGui gui;

    private final ArrayList<Dust> dusts = new ArrayList<Dust>();
    private final ArrayList<Dust> dustsToRemove = new ArrayList<Dust>();
    private float accSmoothing = 0.1f; // Smoothing factor for acceleration
    private float dustMinLife = 30; // Lifetime of dust particles
    private float dustMaxLife = 100; // Lifetime of dust particles
    private float dustMinSize = 2; // Minimum size of dust particles
    private float dustMaxSize = 5; // Maximum size of dust particles


    public static void main(String[] args) {
        PApplet.main(java.lang.invoke.MethodHandles.lookup().lookupClass());
    }

    public void settings() {
        fullScreen(P3D);
        orientation(PORTRAIT);
    }

    public void setup() {
        gui = new LazyGui(this);
        cameraOffset = new PVector(width * .5f, height * .5f);
        player = new Fish();
        player.pos = new PVector(0, 0);
        player.spd = new PVector(minSpeed, 0);
        allFish.add(player);
        target = new PVector(player.pos.x, player.pos.y);
    }

    public void draw() {
        centerToCornerDistance = dist(0, 0, width * .5f, height * .5f);
        farSpawnDistance = centerToCornerDistance * 1.5f;
        background(0);
        pushMatrix();
        translate(cameraOffset.x, cameraOffset.y);
        updateMouseTarget();
        movePlayerTowardsTarget();
        moveCameraTowardsPlayer();
        spawnNewDust();
        drawDust();
        drawAllFish();
        spawnNewFish();
        popMatrix();
        drawSliders();
    }

    private void spawnNewDust() {
        while (dusts.size() < dustCount) {
            // base location is the cameraOffset
            PVector pos = new PVector(
                    -cameraOffset.x + random(-farSpawnDistance, farSpawnDistance),
                    -cameraOffset.y + random(-farSpawnDistance, farSpawnDistance)
            );
            dusts.add(new Dust(pos));
        }
    }

    private void drawDust() {
        for (Dust d : dusts) {
            d.update();
            if (d.toRemove) {
                dustsToRemove.add(d);
            } else {
                d.draw();
            }
        }
        dusts.removeAll(dustsToRemove);
        dustsToRemove.clear();
    }

    private void spawnNewFish() {
        while (allFish.size() < fishCount) {
            allFish.add(new Fish());
        }
    }

    private void drawSliders() {
        fishCount = gui.sliderInt("fish count", 50);
        minFishSize = gui.slider("min fish size", 10);
        maxFishSize = gui.slider("max fish size", 20);

        gui.pushFolder("flocking");
        alignRadius = gui.slider("align radius", alignRadius);
        avoidRadius = gui.slider("avoid radius", avoidRadius);
        centralizeRadius = gui.slider("centralize radius", centralizeRadius);
        alignWeight = gui.slider("align weight", alignWeight);
        avoidWeight = gui.slider("avoid weight", avoidWeight);
        centralizeWeight = gui.slider("centralize weight", centralizeWeight);
        maxSpeed = gui.slider("max speed", maxSpeed);
        minSpeed = gui.slider("min speed", minSpeed);
        globalDrag = gui.slider("global drag", globalDrag);
        randomMag = gui.slider("random mag", randomMag, 0, 20);
        accSmoothing = gui.slider("acc smoothing", 0.1f, 0, 1);
        gui.popFolder();

        gui.pushFolder("dust");
        dustCount = gui.sliderInt("dust count", 300);
        dustMinLife = gui.slider("dust min life", 30, 0, 500);
        dustMaxLife = gui.slider("dust max life", 100, 0, 500);
        dustMinSize = gui.slider("dust min size", 2, 0, 10);
        dustMaxSize = gui.slider("dust max size", 5, 0, 20);
        gui.popFolder();
    }

    private void drawAllFish() {
        noFill();
        for (Fish f : allFish) {
            f.update();
            if (f != player && f.toRemove) {
                fishToRemove.add(f);
            }
        }
        allFish.removeAll(fishToRemove);
        fishToRemove.clear();

        gui.pushFolder("visuals");
        fishColorA = gui.colorPicker("fish color A", fishColorA).hex;
        fishColorB = gui.colorPicker("fish color B", fishColorB).hex;
        for (Fish f : allFish) {
            if (f == player) {
                stroke(gui.colorPicker("stroke (p)", color(255, 255, 255, 150)).hex);
                strokeWeight(gui.slider("weight (p)", 1));
            } else {
                stroke(lerpColor(fishColorA, fishColorB, f.colorRand));
                strokeWeight(gui.slider("weight (f)", 1));
            }
            f.drawFish();
        }
        gui.popFolder();
    }

    private void updateMouseTarget() {
        if (mousePressed && gui.isMouseOutsideGui()) {
            target.x = mouseX - cameraOffset.x;
            target.y = mouseY - cameraOffset.y;
            targetActive = true;
        }
        if (dist(player.pos.x, player.pos.y, target.x, target.y) < 10) {
            targetActive = false;
        }
    }

    private float distanceFromPlayerToTarget() {
        return (target.x == player.pos.x && target.y == player.pos.y) ? 0 : dist(player.pos.x, player.pos.y, target.x, target.y);
    }

    private void movePlayerTowardsTarget() {
        if (!targetActive) {
            return;
        }
        player.spd.x = lerp(player.pos.x, target.x, .1f);
        player.spd.y = lerp(player.pos.y, target.y, .1f);
        player.spd.sub(player.pos);
        player.spd.limit(10);
        player.spd.mult(.9f);
        player.pos.add(player.spd);
    }

    private void moveCameraTowardsPlayer() {
        // PVector playerCoordinateOnScreen = new PVector(modelX(player.pos.x, player.pos.y, player.pos.z), modelY(player.pos.x, player.pos.y, player.pos.z), 0);
        cameraOffset.x = lerp(cameraOffset.x, width * .5f - player.pos.x, .1f);
        cameraOffset.y = lerp(cameraOffset.y, height * .5f - player.pos.y, .1f);
    }

    boolean isPointInRect(float px, float py, float rx, float ry, float rw, float rh) {
        return px >= rx && px <= rx + rw && py >= ry && py <= ry + rh;
    }

    class Fish {
        PVector pos;
        PVector spd = new PVector();
        PVector acc = new PVector();
        float radius;
        boolean toRemove = false;
        PVector centralizeAvgPos = new PVector();
        PVector fishAvoidance = new PVector();
        PVector alignableAvgSpd = new PVector();
        float animationTime = 0;
        float colorRand;

        Fish() {
            if (player == null) {
                pos = new PVector(width * .5f, height * .5f);
                radius = 20;
            } else {
                pos = randomPositionOffscreenInFrontOfPlayer();
                radius = random(minFishSize, maxFishSize);
                colorRand = random(1);
            }
        }

        void update() {
            acc.mult(0);
            centralizeAvgPos.mult(0);
            alignableAvgSpd.mult(0);
            fishAvoidance.mult(0);
            int alignableFishCount = 0;
            int centralizableFishCount = 0;
            gui.pushFolder("debug");
            if (gui.toggle("avoid circle")) {
                pushStyle();
                stroke(255, 0, 0, 100);
                noFill();
                ellipse(pos.x, pos.y, avoidRadius * 2, avoidRadius * 2);
                popStyle();
            }
            if (gui.toggle("align circle")) {
                pushStyle();
                stroke(0, 255, 0, 100);
                noFill();
                ellipse(pos.x, pos.y, alignRadius * 2, alignRadius * 2);
                popStyle();
            }
            if (gui.toggle("centralize circle")) {
                pushStyle();
                stroke(0, 0, 255, 100);
                noFill();
                ellipse(pos.x, pos.y, centralizeRadius * 2, centralizeRadius * 2);
                popStyle();
            }
            gui.popFolder();
            for (Fish f : allFish) {
                if (this.equals(f)) {
                    continue;
                }
                float d = dist(f.pos.x, f.pos.y, pos.x, pos.y);
                if (d < alignRadius) {
                    alignableAvgSpd.add(f.spd);
                    alignableFishCount++;
                }
                if (d < avoidRadius) {
//                    friendAvoidance.add(PVector.sub(pos, f.pos).normalize());
                    float avoidDist = 1 - norm(d, 0, avoidRadius);
                    float avoidCloserMore = 1 / max(avoidDist, 0.01f); // Prevent division by zero
                    PVector fishAvoidVector = PVector.sub(pos, f.pos).normalize().mult(avoidCloserMore);
                    fishAvoidance.add(fishAvoidVector);
                }
                if (d < centralizeRadius) {
                    centralizeAvgPos.add(f.pos); // Prevent division by zero
                    centralizableFishCount++;
                }
            }
            if (centralizableFishCount > 0) {
                centralizeAvgPos.div(centralizableFishCount);
            }
            if (alignableFishCount > 0) {
                alignableAvgSpd.div(alignableFishCount);
            }

            PVector centralize = PVector.sub(centralizeAvgPos, pos).normalize().mult(centralizeWeight);
            PVector avoid = fishAvoidance.normalize().mult(avoidWeight);
            PVector align = PVector.sub(alignableAvgSpd, spd).normalize().mult(alignWeight);
            gui.pushFolder("debug");
            pushStyle();
            pushMatrix();
            strokeWeight(2);
            translate(pos.x, pos.y);
            if (gui.toggle("avoid vector")) {
                stroke(255, 0, 0, 150);
                line(0, 0, avoid.x * avoidRadius, avoid.y * avoidRadius);
            }
            if (gui.toggle("align vector")) {
                stroke(0, 255, 0, 150);
                line(0, 0, align.x * alignRadius, align.y * alignRadius);
            }
            if (gui.toggle("centralize vector")) {
                stroke(0, 0, 255, 150);
                line(0, 0, centralize.x * centralizeRadius, centralize.y * centralizeRadius);
            }
            popMatrix();
            popStyle();
            gui.popFolder();
            acc.add(centralize);
            acc.add(avoid);
            acc.add(align);
            acc.add(PVector.random2D().mult(randomMag));
            spd.lerp(acc, accSmoothing); // Smoothly apply acceleration to speed
            spd.mult(globalDrag);
            spd.limit(maxSpeed);
            if (spd.mag() < minSpeed) {
                spd.setMag(minSpeed);
            }
            pos.add(spd);
        }

        private boolean isPlayer() {
            return this == player;
        }

        private void drawFish() {

            pushMatrix();
            translate(pos.x, pos.y);
            rotate(spd.heading());
            float distanceToPlayer = dist(pos.x, pos.y, player.pos.x, player.pos.y);
            if (distanceToPlayer > farSpawnDistance && isBehindPlayer(pos)) {
                this.toRemove = true;
            }

            beginShape(TRIANGLE_STRIP);
            int vertexCount = 12;
            animationTime += map(spd.mag(), minSpeed, maxSpeed, animationSpeedIdle, animationSpeedFast);
            for (float i = -vertexCount * .25f; i < vertexCount; i++) {
                boolean tail = i < 0;
                float iN = map(i, 0, vertexCount - 1, 0, 1);
                float x = map(iN, 0, 1, -radius * 2, radius);
                float y0 = radius * .5f * sin(iN * 3.f) + radius * .1f * sin(iN * 2 - animationTime * 25) * (tail ? 2 : 1);
                float y1 = -radius * .5f * sin(iN * 3.f) + radius * .1f * sin(iN * 2 - animationTime * 25) * (tail ? 2 : 1);
                vertex(x, y0);
                vertex(x, y1);
            }
            endShape(CLOSE);
            popMatrix();
        }

        private boolean isBehindPlayer(PVector pos) {
            float angleToPlayer = atan2(player.pos.y - pos.y, player.pos.x - pos.x);
            float normalizedAngleToPlayer = normalizeAngle(angleToPlayer, PI);
            float normalizedPlayerHeading = normalizeAngle(player.spd.heading(), PI);
            float angleToPlayerVsHeading = normalizeAngle(normalizedAngleToPlayer - normalizedPlayerHeading, 0);
            return abs(angleToPlayerVsHeading) < HALF_PI;
        }

        private PVector randomPositionOffscreenInFrontOfPlayer() {
            float angle = random(player.spd.heading() - HALF_PI, player.spd.heading() + HALF_PI);
            float distance = random(centerToCornerDistance, farSpawnDistance);
            return new PVector(player.pos.x + distance * cos(angle), player.pos.y + distance * sin(angle));
        }

        public float normalizeAngle(float a, float center) {
            return a - TWO_PI * floor((a + PI - center) / TWO_PI);
        }

        boolean isPointInRect(float px, float py, float rx, float ry, float rw, float rh) {
            return px >= rx && px <= rx + rw && py >= ry && py <= ry + rh;
        }

        boolean isPointInRectCenterMode(float px, float py, float rx, float ry, float rw, float rh) {
            return px >= rx - rw * .5f && px <= rx + rw * .5f && py >= ry - rh * .5f && py <= ry + rh * .5f;
        }

        float sign(float value) {
            return (value < 0) ? -1 : 1;
        }
    }

    class Dust {
        PVector pos;
        float radius;
        float lifeTimeTotal, lifeTime;
        boolean toRemove = false;

        Dust(PVector pos) {
            this.pos = pos;
            this.radius = random(dustMinSize, dustMaxSize);
            this.lifeTimeTotal = random(dustMinLife, dustMaxLife);
            lifeTime = lifeTimeTotal;
        }

        void update() {
            lifeTime -= 1;
            if (lifeTime <= 0) {
                toRemove = true;
            }
        }

        void draw() {
            float alpha;
            if (lifeTime > lifeTimeTotal * 0.66f) { // First third of the lifetime
                alpha = map(lifeTime, lifeTimeTotal, lifeTimeTotal * 0.66f, 0, 255);
            } else if (lifeTime < lifeTimeTotal * 0.33f) { // Last third of the lifetime
                alpha = map(lifeTime, lifeTimeTotal * 0.33f, 0, 255, 0);
            } else { // Middle third of the lifetime
                alpha = 255;
            }
            gui.pushFolder("dust");
            fill(gui.colorPicker("color", 128).hex, alpha);
            noStroke();
            ellipse(pos.x, pos.y, radius * 2, radius * 2);
            gui.popFolder();
        }
    }
}
