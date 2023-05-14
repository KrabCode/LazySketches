package _23_05.FlockingSim;

import _0_utils.Utils;
import processing.core.PApplet;
import com.krab.lazy.*;
import processing.core.PGraphics;
import processing.core.PVector;


public class Flocking extends PApplet {
    LazyGui gui;
    PGraphics pg;
    CameraGrid2D world;
    PVector playerPos;
    PVector playerTarget = new PVector();
    PVector playerSpd = new PVector();
    boolean lerpingTowardsTarget = false;
    float playerHeading;

    public static void main(String[] args) {
        PApplet.main(java.lang.invoke.MethodHandles.lookup().lookupClass());
    }

    @Override
    public void settings() {
        fullScreen(P2D);
        smooth(8);
    }

    @Override
    public void setup() {
        Utils.setupSurface(this, surface);
        gui = new LazyGui(this);
        pg = createGraphics(width, height, P2D);
        pg.smooth(8);
        world = new CameraGrid2D(this);
        frameRate(144);
    }

    @Override
    public void draw() {
        pg.beginDraw();
        pg.colorMode(HSB,1,1,1,1);
        updatePlayer();
        updateFlock();
        drawBackground();
        world.updateCamera(gui, pg, playerPos);
        world.drawGridAroundPlayer(gui, pg);
        pg.translate(playerPos.x, playerPos.y);
        pg.scale(gui.slider("entity zoom", 1));
        drawFlock();
        drawPlayer();
        pg.endDraw();
        image(pg, 0, 0);
    }

    private void updateFlock() {

    }

    private void updatePlayer(){
        gui.pushFolder("player");
        playerPos = gui.plotXY("playerPos");
        float lerpAmt = gui.slider("lerp speed", 0.1f, 0, 1);
        if(Input.getCode(CONTROL).down){
            playerTarget = world.screenPosToWorldPos(mouseX, mouseY);
            lerpingTowardsTarget = true;
            PVector playerPosNext = PVector.lerp(playerPos, playerTarget, lerpAmt);
            playerSpd = PVector.sub(playerPosNext, playerPos);
            playerHeading = playerSpd.heading();
            gui.plotSet("playerPos", playerPosNext);
        }
        if(lerpingTowardsTarget && PVector.dist(playerPos, playerTarget) < 0.01f){
            lerpingTowardsTarget = false;
        }
        gui.popFolder();
    }

    private void drawPlayer() {
        pg.noStroke();
        pg.noFill();
        pg.stroke(1);
        pg.strokeWeight(3);
        pg.ellipse(0, 0, 50,50);
    }

    private void drawFlock() {
        gui.pushFolder("flock");
        gui.pushFolder("spawn");
        if(gui.toggle("debug front")){
            float debugFrontDiam = gui.slider("front diam", 100);
            pg.pushMatrix();
            pg.strokeWeight(1);
            pg.stroke(0.5f,1,1);
            pg.fill(0.5f, 1, 0.5f);
            pg.arc(0, 0, debugFrontDiam, debugFrontDiam, playerHeading - HALF_PI, playerHeading + HALF_PI);
            pg.rotate(playerHeading);
            pg.line(0, debugFrontDiam*0.5f, 0, -debugFrontDiam*0.5f);
            pg.popMatrix();
        }
        if(gui.toggle("debug back")){
            float debugBackDiam = gui.slider("back diam", 150);
            pg.pushMatrix();
            pg.strokeWeight(1);
            pg.stroke(0,1,1);
            pg.fill(0, 1, 0.5f);
            pg.arc(0, 0, debugBackDiam, debugBackDiam, playerHeading + PI - HALF_PI, playerHeading + PI + HALF_PI);
            pg.rotate(playerHeading);
            pg.line(0, debugBackDiam*0.5f, 0, -debugBackDiam*0.5f);
            pg.popMatrix();
        }
        gui.popFolder();
        gui.popFolder();
    }

    private void drawBackground() {
        pg.fill(gui.colorPicker("background").hex);
        pg.noStroke();
        pg.rectMode(CORNER);
        pg.rect(0,0,width,height);
    }
}
