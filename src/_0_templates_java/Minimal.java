package _0_templates_java;

import processing.core.PApplet;

public class Minimal extends PApplet {

    public static void main(String[] args) {
        PApplet.main(java.lang.invoke.MethodHandles.lookup().lookupClass());
    }

    @Override
    public void settings() {
        size(800,800);
    }

    @Override
    public void setup() {

    }

    @Override
    public void draw() {
        background(255);
        fill(0);
        textSize(38);
        textAlign(CENTER);
        text("Intellij Processing 4 template", width * 0.5F, height * 0.5F);
    }
}
