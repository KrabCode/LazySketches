package _23_09;

import _22_03.PostFxAdapter;
import com.krab.lazy.LazyGui;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;

import java.util.ArrayList;

// TODO unfinished
public class Slime extends PApplet {
    LazyGui gui;
    PGraphics pg;
    ArrayList<Actor> actors = new ArrayList<>();
    float wHalf, hHalf;

    public static void main(String[] args) {
        PApplet.main(java.lang.invoke.MethodHandles.lookup().lookupClass());
    }

    @Override
    public void settings() {
        size(800,800,P2D);
    }

    @Override
    public void setup() {
        wHalf = width/2f;
        hHalf = height/2f;
        gui = new LazyGui(this);
        pg = createGraphics(width, height, P2D);
        pg.beginDraw(); pg.endDraw();
        pg.beginDraw(); pg.background(0); pg.endDraw();
        frameRate(144);
    }

    @Override
    public void draw() {
        pg.beginDraw();
        gui.pushFolder("subtract");
        if(frameCount % gui.sliderInt("delay", 2, 1, 100) == 0){
            pg.blendMode(SUBTRACT);
            pg.noStroke();
            pg.fill(gui.slider("value", 1));
            pg.rectMode(CORNER);
            pg.rect(0,0,width,height);
        }
        gui.popFolder();
        pg.blendMode(BLEND);
        int actorCount = gui.sliderInt("actor count");
        while(actors.size() < actorCount){
            actors.add(new Actor());
        }
        while(actors.size() > actorCount){
            actors.remove(actors.size()-1);
        }
        gui.pushFolder("actors");
        pg.pushMatrix();
        pg.translate(wHalf, hHalf);
        pg.loadPixels();
        for(Actor a : actors){
            a.update(pg);
        }
        pg.popMatrix();
        gui.popFolder();
        // actors move randomly initially, fan out to edges, bounce from them
        // actors leave a trail that blurs over time (shader)
        // actors steer towards areas of higher trail concentration
        pg.endDraw();
        PostFxAdapter.apply(this, gui, pg);
        image(pg, 0, 0);
    }

    float getPixelBrightness(PVector p){
        int clr = pg.get(floor(p.x+wHalf), floor(p.y+hHalf));
        return brightness(clr);
    }

    class Actor{
        PVector pos = new PVector();
        PVector spd = new PVector(1, 0).rotate(random(TAU*2));


        public void update(PGraphics pg) {
            lookAhead();
            pos.add(spd);
            bounce();
            pg.stroke(gui.colorPicker("stroke").hex);
            pg.noFill();
            pg.strokeWeight(gui.slider("weight", 1.5f));
            pg.point(pos.x, pos.y);
        }

        private void bounce() {
            if((pos.x > wHalf && spd.x > 0) || pos.x < -wHalf && spd.x < 0){
                spd.x *= -1;
            }
            if((pos.y > hHalf && spd.y > 0) || pos.y < -hHalf && spd.y < 0){
                spd.y *= -1;
            }
        }

        private void lookAhead() {
            float heading = spd.heading();
            float checkRange = gui.slider("check range", 0.2f);
            float checkDist = gui.slider("check dist", 5);
            float steerSharpness = gui.slider("steering", 0.1f);
            PVector aheadVector = PVector.fromAngle(heading).mult(checkDist);
            PVector ahead = PVector.add(pos, aheadVector.copy());
            PVector aheadLeft = PVector.add(pos, aheadVector.copy().rotate(checkRange));
            PVector aheadRight = PVector.add(pos, aheadVector.copy().rotate(-checkRange));
            float brAhead = getPixelBrightness(ahead);
            float brAheadLeft = getPixelBrightness(aheadLeft);
            float brAheadRight = getPixelBrightness(aheadRight);
            if(brAhead > brAheadLeft && brAhead > brAheadRight){
                // do nothing
            }else if(brAheadLeft < brAheadRight){
                spd.rotate(-steerSharpness);
            }else if(brAheadLeft > brAheadRight){
                spd.rotate(steerSharpness);
            }
        }
    }
}
