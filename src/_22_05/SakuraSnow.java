package _22_05;

import _22_03.PostFxAdapter;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;
import toolbox.Gui;
import toolbox.global.Utils;
import toolbox.windows.nodes.colorPicker.Color;

import java.util.ArrayList;

public class SakuraSnow extends PApplet {
    Gui gui;
    PGraphics pg;
    ArrayList<Leaf> leaves = new ArrayList<>();
    ArrayList<Leaf> leavesToRemove = new ArrayList<>();
    int lifeDuration;

    public static void main(String[] args) {
        PApplet.main(java.lang.invoke.MethodHandles.lookup().lookupClass());
    }

    @Override
    public void settings() {
//        size(1000,1000,P2D);
        fullScreen(P2D);
    }

    @Override
    public void setup() {
        gui = new Gui(this);
        pg = createGraphics(width, height, P2D);

    }

    int recFrame = 0;
    boolean isRec = false;

    @Override
    public void draw() {
        pg.colorMode(HSB,1,1,1,1);
        pg.beginDraw();
        pg.pushStyle();
        pg.blendMode(BLEND);
        drawMainBackground();
        pg.popStyle();
        pg.translate(width/2f, height/2f);
        updateLeaves();
        pg.endDraw();
        PostFxAdapter.apply(this, gui, pg);
        imageMode(CORNER);
        image(pg, 0, 0, width, height);
        int recSize = gui.sliderInt("record/size", 600);
        if(gui.toggle("record/show rectangle")){
            rectMode(CORNER);
            noFill();
            stroke(255);
            rect(width/2f - recSize / 2f, height / 2f - recSize / 2f, recSize, recSize);
        }
        if(gui.toggle("record/active", false)){
            isRec = true;
            pg.get(width/2 - recSize / 2, height / 2 - recSize / 2, recSize, recSize).save("/out/sakura/" + recFrame++ + ".jpg");
        }else{
            isRec = false;
            recFrame = 1;
        }
        if(gui.toggle("record/show rectangle")){
            rectMode(CORNER);
            noFill();
            stroke(255);
            rect(width/2f - recSize / 2f, height / 2f - recSize / 2f, recSize, recSize);
        }
        imageMode(CORNER);
        image(pg, 0, 0, width, height);

        gui.draw();
    }

    private void updateLeaves() {
        lifeDuration = gui.sliderInt("leaf/spawn/lifetime", 120, 1, Integer.MAX_VALUE);
        int leafCount = gui.sliderInt("leaf/spawn/count", 100);
        int particlesPerFrame = min(1, leafCount / lifeDuration);

        if(leaves.size() < leafCount){
            for (int i = 0; i < particlesPerFrame; i++) {
                leaves.add(new Leaf());
            }
        }
        pg.rectMode(CENTER);
        if("blend".equals(gui.stringPicker("leaf/blend mode", new String[]{"blend", "add"}))){
            pg.blendMode(BLEND);
        }else{
            pg.blendMode(ADD);
        }
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
        pg.fill(gui.colorPicker("main background", color(0)).hex);
        pg.noStroke();
        pg.rectMode(CORNER);
        pg.rect(0,0,width,height);
    }

    class Leaf{
        PVector pos = new PVector(), spd = new PVector();
        float timePos;
        int frameBorn = frameCount;
        float sizeModifier = randomGaussian();
        float hueModifier = randomGaussian();
        float satModifier = randomGaussian();
        float brModifier = randomGaussian();
        private int lifeLength;

        Leaf(){
            float x = gui.slider("leaf/spawn/x", 0);
            float y = gui.slider("leaf/spawn/y", 0);
            float range = gui.slider("leaf/spawn/range");
            float spawnRangeX = randomGaussian() * range;
            float spawnRangeY = randomGaussian() * range;
            pos.x = x + spawnRangeX;
            pos.y = y + spawnRangeY;

        }

        void update(){
            PVector acc = new PVector(gui.slider("leaf/move/acc x"), gui.slider("leaf/move/acc y"));
            timePos += radians(gui.slider("leaf/move/noise time", 1));
            float freq = gui.slider("leaf/move/noise freq", 0.1f);
            PVector noise = new PVector(
                    noise(pos.x * freq, pos.y* freq, timePos),
                    noise(pos.x * freq +430.95f, pos.y* freq -1740.125f, timePos+320.5f)
            );
            noise.sub(0.5f, 0.5f);
            noise.mult(gui.slider("leaf/move/noise power", 1));
            acc.add(noise);
            spd.mult(gui.slider("leaf/move/drag", .98f));
            spd.add(acc);
            pos.add(spd);
            Color baseFill = gui.colorPicker("leaf/base fill", color(255));
            float fade = constrain(norm(frameCount, frameBorn, frameBorn + gui.slider("leaf/fade in time", 60)), 0, 1);
            float fadeOutDuration = gui.slider("leaf/fade out time", 60);
            if(frameCount >= frameBorn + lifeDuration - fadeOutDuration){
                fade =  1 - constrain(norm(frameCount, frameBorn + lifeDuration - fadeOutDuration, frameBorn + lifeDuration), 0, 1);
            }
            int mirrorCount = gui.sliderInt("mirrors", 1);
            for (int mirrorIndex = 0; mirrorIndex < mirrorCount; mirrorIndex++) {
                pg.pushMatrix();
                pg.rotate(TAU * norm(mirrorIndex, 0, mirrorCount));
                pg.translate(pos.x, pos.y);
                pg.rotate(spd.heading());
                pg.fill(Utils.hueModulo(baseFill.hue + hueModifier  * gui.slider("leaf/hue variation", 0)),
                        constrain(baseFill.saturation + satModifier  * gui.slider("leaf/sat variation", 0), 0, 1),
                        constrain(baseFill.brightness + brModifier * gui.slider("leaf/br variation", 0), 0, 1),
                        lerp(0, baseFill.alpha, fade));
                pg.noStroke();
                float size = gui.slider("leaf/size", 2) + sizeModifier  * gui.slider("leaf/size variation", 1);
                if (gui.stringPicker("leaf/shape", new String[]{"rectangle", "circle"}).equals("rectangle")) {
                    pg.rect(0, 0, size, size);
                } else {
                    pg.ellipse(0, 0, size, size);
                }
                pg.popMatrix();
            }
        }

        public boolean isGarbage() {
            return frameBorn + lifeDuration < frameCount;
        }
    }
}

