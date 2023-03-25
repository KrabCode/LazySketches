package _23_01.lsystem;

import _0_utils.Utils;
import com.krab.lazy.PickerColor;
import processing.core.PApplet;
import processing.core.PGraphics;
import com.krab.lazy.LazyGui;
import processing.core.PVector;

import java.util.ArrayList;

public class LSystems extends PApplet {
    ArrayList<LSystem> systems = new ArrayList<>();
    LazyGui gui;
    PGraphics pg;

    public static void main(String[] args) {
        PApplet.main(java.lang.invoke.MethodHandles.lookup().lookupClass());
    }

    @Override
    public void settings() {
//        size(1080, 1080, P2D);
        fullScreen(P2D);
    }

    @Override
    public void setup() {
        gui = new LazyGui(this);
        pg = createGraphics(width, height, P2D);
        colorMode(HSB, 1, 1, 1, 1);
        frameRate(144);
    }

    @Override
    public void draw() {
        pg.beginDraw();
        drawBackground();
        drawLSystems();
        pg.endDraw();
        image(pg, 0, 0);
        gui.draw();
        Utils.record(this, gui);
    }

    private void drawTutorial() {
        gui.pushFolder("tutorial");

        gui.text("overview",
                "An L-system or Lindenmayer system is a parallel rewriting system and a type of formal grammar.\n" +
                "An L-system consists of an alphabet of symbols that can be used to make strings,\n" +
                "a collection of production rules that expand each symbol into some larger string\n" +
                " of symbols, an initial \"axiom\" string from which to begin construction,\n" +
                "and a mechanism for translating the generated strings into geometric structures.");
        gui.text("history",
                "L-systems were introduced and developed in 1968 by Aristid Lindenmayer, \n" +
                        "a Hungarian theoretical biologist and botanist at the University of Utrecht.\n" +
                        "Lindenmayer used L-systems to describe the behaviour of plant cells and to model\n" +
                        "the growth processes of plant development. L-systems have also been used to model\n" +
                        "the morphology of a variety of organisms and can be used to generate self-similar fractals.");
        gui.text("mechanics",
                "an \"axiom\" is a string that acts as the first generation\n" +
                    "the \"rules\" are applied on the current generation to produce a new generation\n" +
                    "the final result is a list of instructions of how to draw the recursive tree");
        gui.text("rules", "a rule does a \"find + replace\" on the current string\n" +
                "the part being searched for is called the \"matcher\" in this sketch\n" +
                "and its new value is called the \"replacement\" \n");
        gui.text("drawing instructions",
                    "F : draw and go forward a fixed length\n" +
                        "G : go forward a fixed length but do not draw\n" +
                        "+ : turn right at (angle)\n" +
                        "- : turn left at (angle)\n" +
                        "[ : save position and angle\n" +
                        "] : restore position and angle\n" +
                        "| : draw and go forward by a length based on current ["
        );
        gui.pushFolder("example fractal flower");
        gui.text("axiom", "X");
        gui.text("rule 1 matcher", "F");
        gui.text("rule 1 replacement", "FF");
        gui.text("rule 2 matcher", "X");
        gui.text("rule 2 replacement", "F+[[X]-X]-F[-FX]+X");
        gui.text("angle (degrees)", "25");
        gui.popFolder();

        gui.pushFolder("example koch curve");
        gui.text("axiom", "F");
        gui.text("rule 1 matcher", "F");
        gui.text("rule 1 replacement", "F+F−F−F+F");
        gui.text("angle (degrees)", "90");
        gui.popFolder();

        gui.popFolder();
    }

    private void drawLSystems() {
        gui.pushFolder("systems");
        drawTutorial();
        int systemCount = gui.sliderInt("count", 1);
        if(gui.button("add system")){
            gui.sliderSet("count", systemCount + 1);
        }
        for (int i = 0; i < systemCount; i++) {
            if(systems.size() <= i){
                systems.add(new LSystem());
            }
            gui.pushFolder("system " + (i+1));
            systems.get(i).update(i + 1);
            gui.popFolder();
        }
        gui.popFolder();
    }

    private void drawBackground() {
        pg.fill(gui.colorPicker("background", color(0.15f)).hex);
        pg.noStroke();
        pg.rectMode(CORNER);
        pg.rect(0, 0, width, height);
    }

    class LSystem{
        String axiom = "";
        String current = "";
        int generation = 0;
        int maxDepth = 0;
        int maxDepthOverride = 99;

        public void update(int index) {
            gui.pushFolder("system " + index + " rules");
            float angle = radians(gui.slider("angle (deg)", 25));
            axiom = gui.text("axiom", axiom);
            int ruleCount = gui.sliderInt("rule count", 1);
            if(gui.button("add rule")){
                gui.sliderSet("rule count", ruleCount + 1);
            }
            ArrayList<String[]> rules = new ArrayList<>();
            for (int i = 0; i < ruleCount; i++) {
                String matcher = gui.text("rule " + (i+1) + " matcher", "");
                String replacement = gui.text("rule " + (i+1) + " replacement", "");
                String[] rule = new String[]{matcher, replacement};
                rules.add(rule);
            }
            gui.popFolder();

            gui.pushFolder("system " + index + " display");
            if(gui.toggle("active", true)){
                float lengthFixed = gui.slider("F,G step length", 10);
                float lengthMult = gui.slider("| step multiplier", 0.65f);
                char[] path = current.toCharArray();
                pg.pushMatrix();
                PVector pos = gui.plotXY("position");
                pg.translate(width/2f+pos.x, height/2f + pos.y);
                pg.rotate(-HALF_PI + radians(gui.slider("rotation (deg)")));
                pg.strokeCap(gui.toggle("stroke cap square\\/round") ? ROUND : SQUARE);
                float weightAtZero = gui.slider("weight root", 5);
                float weightAtMax = gui.slider("weight max depth", 1);
                PickerColor colorAtZero = gui.colorPicker("color root", color(1,0,1));
                PickerColor colorAtMaxDepth = gui.colorPicker("color max depth", color(1,1,1));
                int depth = 0;
                int turnCount = 1;
                String numberRegex = "[2-9]";
                for(char c : path){
                    float depthNorm = norm(min(depth, maxDepth), 0, maxDepth);
                    pg.stroke(pg.lerpColor(colorAtZero.hex, colorAtMaxDepth.hex, depthNorm));
                    pg.strokeWeight(lerp(weightAtZero, weightAtMax, depthNorm));
                    boolean isNumber = String.valueOf(c).matches(numberRegex);
                    if(isNumber){
                        turnCount = Integer.parseInt(String.valueOf(c));
                    }
                    if(c == 'F'){
                        pg.line(lengthFixed,0,0,0);
                        pg.translate(lengthFixed, 0);
                    }else if(c == 'G'){
                        pg.translate(lengthFixed, 0);
                    }else if(c == '|'){
                        float depthAwareStep = lengthFixed;
                        for (int i = 0; i < depth; i++) {
                            depthAwareStep *= lengthMult;
                        }
                        pg.line(depthAwareStep,0,0,0);
                        pg.translate(depthAwareStep, 0);
                    }else if(c == '-'){
                        for (int i = 0; i < turnCount; i++) {
                            pg.rotate(-angle);
                        }
                    }else if(c == '+'){
                        for (int i = 0; i < turnCount; i++) {
                            pg.rotate(+angle);
                        }
                    }else if(c == '['){
                        pg.pushMatrix();
                        depth++;
                    }else if(c == ']'){
                        pg.popMatrix();
                        depth--;
                    }
                    if(!isNumber){
                        turnCount = 1;
                    }
                    maxDepth = max(depth, maxDepth);
                    maxDepth = min(maxDepthOverride, maxDepth);
                }
                pg.popMatrix();
            }
            gui.popFolder();

            gui.textSet("current string", current);
            gui.textSet("string length", "" + current.length());
            gui.textSet("generation", "" + generation);
            gui.textSet("max depth", "" + maxDepth);
            maxDepthOverride = gui.sliderInt("max depth override", 999);

            if(gui.button("advance generation") || frameCount < 5){
                maxDepth = 0;
                if(current == null || "".equals(current)){
                    current = axiom;
                }
                for (String[] rule : rules) {
                    current = current.replaceAll(rule[0], rule[1]);
                }
                println("current string: " + current);
                generation++;
            }
            if(gui.button("reset to axiom")){
                current = axiom;
                generation = 0;
            }


        }

    }


}

