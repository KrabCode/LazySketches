package _25_07;

import com.krab.lazy.LazyGui;
import processing.core.PApplet;
import processing.core.PVector;
import java.util.ArrayList;

public class Fishes  extends PApplet {

    PVector target, cameraOffset;
    float centerToCornerDistance, farAwayDist, alignRadius, alignWeight, centralizeRadius, centralizeWeight, avoidRadius, avoidWeight, headingChangeMax, spdLimit, globalDrag;
    float minFishSize = 10;
    float gaussFishSize = 20;
    int fishCount = 50;

    int dustCount = 100;

    Fish player;
    ArrayList<Fish> allFish = new ArrayList<Fish>();
    ArrayList<Fish> fishToRemove = new ArrayList<Fish>();
    private boolean targetActive = false;
    private float t;
    LazyGui gui;

    private ArrayList<Dust> dusts = new ArrayList<Dust>();
    private ArrayList<Dust> dustsToRemove = new ArrayList<Dust>();

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
        player.spd = new PVector(1, 0);
        allFish.add(player);
        target = new PVector(player.pos.x, player.pos.y);
    }

    public void draw() {
        t = radians(frameCount);
        centerToCornerDistance = dist(0, 0, width * .5f, height * .5f);
        farAwayDist = centerToCornerDistance * 1.5f;
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
        while( dusts.size() < dustCount) {
            // base location is the cameraOffset
            PVector pos = new PVector(
                    -cameraOffset.x + random(-farAwayDist, farAwayDist),
                    -cameraOffset.y + random(-farAwayDist, farAwayDist)
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
        headingChangeMax = gui.slider("heading change max", 0.1f, 0, 1);
        spdLimit = gui.slider("spd limit", 10, 1, 100);
        globalDrag = gui.slider("global drag", 0.9f, 0, 1f);
        alignRadius =   gui.slider("align radius", 200, 10, 400);
        alignWeight =   gui.slider("align weight",  .5f, 0, 1);
        avoidRadius =   gui.slider("avoid radius",  10, 10, 200);
        avoidWeight =   gui.slider("avoid weight",  0.5f, 0, 1);
        centralizeRadius = gui.slider("centralize radius", 100, 10, 400);
        centralizeWeight = gui.slider("centralize weight", .5f, 0, 1);
        fishCount = gui.sliderInt("fish count", 50, 1, 200);
        minFishSize = gui.slider("min fish size", 10, 1, 50);
        gaussFishSize = gui.slider("gauss fish size", 20, 1, 50);
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
        for(Fish f : allFish){
            if(f == player){
                stroke(gui.colorPicker("stroke (p)", color(255, 255, 255, 150)).hex);
                strokeWeight(gui.slider("weight (p)", 1));
            }else{
                stroke(gui.colorPicker("stroke (f)", color(255, 255, 255, 150)).hex);
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
        if (dist(player.pos.x, player.pos.y, target.x, target.y) < 3) {
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
        PVector alignableAvgSpd = new PVector();
        PVector friendAvoidance = new PVector();
        Fish() {
            if (player == null) {
                pos = new PVector(width * .5f, height * .5f);
                radius = 15;
            } else {
                pos = randomPositionOffscreenInFrontOfPlayer();
                radius = minFishSize + abs(randomGaussian() * gaussFishSize);
            }
        }

        void update() {
            acc.mult(0);
            centralizeAvgPos.mult(0);
            alignableAvgSpd.mult(0);
            friendAvoidance.mult(0);
            int alignableFishCount = 0;
            int centralizableFishCount = 0;

            for (Fish f : allFish) {
                if (this.equals(f)) {
                    continue;
                }
                float d = dist(f.pos.x, f.pos.y, pos.x, pos.y);
                if (d < alignRadius) {
                    alignableAvgSpd.add(f.spd);
                    alignableFishCount++;
                }
                if (d < avoidRadius + radius + f.radius) {
                    friendAvoidance.add(PVector.sub(pos, f.pos).normalize());
                }
                if (d < centralizeRadius) {
                    centralizeAvgPos.add(f.pos);
                    centralizableFishCount++;
                }
            }
            if (centralizableFishCount > 0) {
                centralizeAvgPos.div(centralizableFishCount);
            }
            if (alignableFishCount > 0) {
                alignableAvgSpd.div(alignableFishCount);
            }
            PVector toLocalCenter = PVector.sub(centralizeAvgPos, pos).normalize();
            acc.add(friendAvoidance.mult(avoidWeight));
            acc.add(toLocalCenter.mult(centralizeWeight));
            acc.add(alignableAvgSpd.normalize().mult(alignWeight));


            float origHeading = spd.heading();
            spd.add(acc);
            spd.limit(spdLimit);
            spd.mult(globalDrag);
            float idealHeading = spd.heading();
            // only allow gradual changes in heading
            float headingDiff = idealHeading - origHeading;
            if(abs(headingDiff) > headingChangeMax) {
                headingDiff = sign(headingDiff) * headingChangeMax;
            }
            spd.setHeading(origHeading);
            spd.rotate(headingDiff);
            pos.add(spd);

        }

        private void drawFish(){

            pushMatrix();
            translate(pos.x, pos.y);
            rotate(spd.heading());
            float distanceToPlayer = dist(pos.x, pos.y, player.pos.x, player.pos.y);
            if (distanceToPlayer > farAwayDist && isBehindPlayer(pos)) {
                this.toRemove = true;
            }

            beginShape(TRIANGLE_STRIP);
            int vertexCount = 12;
            for (float i = -vertexCount * .25f; i < vertexCount; i++) {
                boolean tail = i < 0;
                float iN = map(i, 0, vertexCount - 1, 0, 1);
                float x = map(iN, 0, 1, -radius * 2, radius);
                float y0 = radius * .5f * sin(iN * 3.f) + radius * .1f * sin(iN * 2 - t * 25) * (tail ? 2 : 1);
                float y1 = -radius * .5f * sin(iN * 3.f) + radius * .1f * sin(iN * 2 - t * 25) * (tail ? 2 : 1);
                vertex(x, y0);
                vertex(x, y1);
            }
            endShape();
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
            float distance = random(centerToCornerDistance, farAwayDist);
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

    class Dust{
        PVector pos;
        float radius;
        float lifeTimeTotal, lifeTime;
        boolean toRemove = false;

        Dust(PVector pos) {
            this.pos = pos;
            this.radius = random(2, 5);
            this.lifeTimeTotal = random(30, 100);
            lifeTime = lifeTimeTotal;
        }

        void update() {
            lifeTime -= 1;
            if(lifeTime <= 0){
                toRemove = true;
            }
        }

        void draw() {
            float alpha;
            if (lifeTime > 66) { // First third of the lifetime
                alpha = map(lifeTime, lifeTimeTotal, lifeTimeTotal * 0.66f, 0, 255);
            } else if (lifeTime < 33) { // Last third of the lifetime
                alpha = map(lifeTime, lifeTimeTotal * 0.33f, 0, 255, 0);
            } else { // Middle third of the lifetime
                alpha = 255;
            }
            fill(128, alpha);
            noStroke();
            ellipse(pos.x, pos.y, radius * 2, radius * 2);
        }
    }
}
