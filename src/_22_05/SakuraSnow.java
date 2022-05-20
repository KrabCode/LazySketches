package _22_05;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;
import toolbox.Gui;
import toolbox.windows.nodes.colorPicker.Color;

import java.util.ArrayList;

public class SakuraSnow extends PApplet {
    Gui gui;
    PGraphics pg;
    ArrayList<Leaf> leaves = new ArrayList<>();
    ArrayList<Leaf> leavesToRemove = new ArrayList<>();


    public static void main(String[] args) {
        PApplet.main(java.lang.invoke.MethodHandles.lookup().lookupClass());
    }

    @Override
    public void settings() {
        size(1080,1080,P2D);
    }

    @Override
    public void setup() {
        gui = new Gui(this);
        pg = createGraphics(width, height, P2D);

    }

    @Override
    public void draw() {
        pg.colorMode(HSB,1,1,1,1);
        pg.beginDraw();
        drawMainBackground();
        pg.clear();
        updateLeaves();
        pg.endDraw();
        pushMatrix();
        translate(width/2f, height/2f);
        imageMode(CENTER);
        int mirrorCount = gui.sliderInt("mirrors", 1);
        for (int mirrorIndex = 0; mirrorIndex < mirrorCount; mirrorIndex++) {
            pushMatrix();
            rotate(TAU*norm(mirrorIndex, 0, mirrorCount));

            image(pg, 0, 0);
            popMatrix();
        }
        popMatrix();
        gui.themePicker();
        gui.draw();
    }

    private void updateLeaves() {
        int leafCount = gui.sliderInt("leaf/spawn/count", 100);
        if(leaves.size() < leafCount){
            int leavesSpawnedPerFrame = gui.sliderInt("leaf/spawn/speed", 1);
            for (int i = 0; i < leavesSpawnedPerFrame; i++) {
                leaves.add(new Leaf());
            }
        }
        pg.rectMode(CENTER);
        for(Leaf leaf : leaves){
            leaf.update();
            if(leaf.isGarbage()){
                leavesToRemove.add(leaf);
            }
        }
        leaves.removeAll(leavesToRemove);
        leavesToRemove.clear();
    }

    private void drawMainBackground() {
        fill(gui.colorPicker("main background", color(0)).hex);
        noStroke();
        rectMode(CORNER);
        rect(0,0,width,height);
    }

    class Leaf{
        PVector pos = new PVector(), spd = new PVector();
        float timePos;
        int frameBorn = frameCount;
        float randomConstant = random(1);
        float sizeModifier = randomGaussian() * gui.slider("leaf/draw/size variation", 1);
        private int lifeLength;

        Leaf(){
            float spawnRangeX = randomGaussian() * gui.slider("leaf/spawn/range x");
            float spawnRangeY = randomGaussian() *  gui.slider("leaf/spawn/range y");
            pos.x = gui.slider("leaf/spawn/x", width/2f) + spawnRangeX;
            pos.y =  gui.slider("leaf/spawn/y", height/2f) + spawnRangeY;

        }

        void update(){
            PVector acc = new PVector(gui.slider("leaf/move/acc x"), gui.slider("leaf/move/acc y"));
            timePos += radians(gui.slider("leaf/move/noise/time", 1));
            float freq = gui.slider("leaf/move/noise/freq", 0.1f);
            PVector noise = new PVector(
                    noise(pos.x * freq + randomConstant, pos.y* freq  + randomConstant, timePos),
                    noise(pos.x * freq +430.95f, pos.y* freq -1740.125f, timePos+320.5f)
            );
            noise.sub(0.5f, 0.5f);
            noise.mult(gui.slider("leaf/move/noise/power", 1));
            acc.add(noise);
            spd.mult(gui.slider("leaf/move/drag", .98f));
            spd.add(acc);
            pos.add(spd);
            Color baseFill = gui.colorPicker("leaf/draw/base fill", color(255));
            float fade = constrain(norm(frameCount, frameBorn, frameBorn + gui.slider("leaf/draw/fade in time", 60)), 0, 1);
            float fadeOutDuration = gui.slider("leaf/draw/fade out time", 60);
            if(frameCount >= frameBorn + lifeLength - fadeOutDuration){
                fade =  1 - constrain(norm(frameCount, frameBorn + lifeLength - fadeOutDuration, frameBorn + lifeLength), 0, 1);
            }
            pg.pushMatrix();
            pg.translate(pos.x, pos.y);
            pg.rotate(spd.heading());
            pg.fill(baseFill.hue, baseFill.saturation, baseFill.brightness, lerp(0, baseFill.alpha, fade));
            pg.noStroke();
            float size = gui.slider("leaf/draw/size", 2) + sizeModifier;
            if(gui.stringPicker("leaf/shape", new String[]{"rectangle", "circle"}).equals("rectangle")){
                pg.rect(0,0,size,size);
            }else{
                pg.ellipse(0,0,size,size);
            }
            pg.popMatrix();
        }

        public boolean isGarbage() {
            lifeLength = gui.sliderInt("leaf/spawn/lifetime", 120);
            return frameBorn + lifeLength < frameCount;
        }
    }
}

