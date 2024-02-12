package _24_02.business_card;

import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PGraphics;
import com.krab.lazy.LazyGui;
import processing.core.PVector;

import java.util.HashMap;

public class Card extends PApplet {
    LazyGui gui;
    PGraphics pg;
    PGraphics cardFront;
    PGraphics cardBack;

    public static void main(String[] args) {
        PApplet.main(java.lang.invoke.MethodHandles.lookup().lookupClass());
    }

    @Override
    public void settings() {
        size(2000, 1080, P2D);
    }

    @Override
    public void setup() {
        gui = new LazyGui(this);
        colorMode(HSB, 1, 1, 1, 1);
        pg = createGraphics(width, height, P2D);
        pg.beginDraw();
        pg.colorMode(HSB, 1, 1, 1, 1);
        pg.endDraw();

        cardBack = createGraphics(900, 550, P2D);
        cardBack.beginDraw();
        cardBack.colorMode(HSB, 1, 1, 1, 1);
        cardBack.endDraw();

        cardFront = createGraphics(900, 550, P2D);
        cardFront.beginDraw();
        cardFront.colorMode(HSB, 1, 1, 1, 1);
        cardFront.endDraw();
        frameRate(144);
    }

    @Override
    public void draw() {
        pg.beginDraw();
        drawBackground();
        gui.pushFolder("back");
        drawCardFlat(cardBack);
        gui.popFolder();
        gui.pushFolder("front");
        drawCardFlat(cardFront);
        gui.popFolder();
        pg.endDraw();
        image(pg, 0, 0);
    }

    private void drawCardFlat(PGraphics card) {
        card.beginDraw();
        card.clear();
        card.background(gui.colorPicker("background", color(0.5f,0,0)).hex);
        PVector pos = gui.plotXY("position");
        for(int i = 0; i < gui.sliderInt("text count"); i++){
            gui.pushFolder("text " + i);
            card.pushMatrix();
            font(card);
            transform(card);
            card.text(gui.text("text", "Hello, World!"), card.width, card.height);
            card.popMatrix();
            gui.popFolder();
        }
        card.endDraw();
        pg.image(card, pos.x, pos.y);
    }

    private void drawBackground() {
        pg.fill(gui.colorPicker("background").hex);
        pg.noStroke();
        pg.rectMode(CORNER);
        pg.rect(0, 0, width, height);
    }


    // Change position and rotation without creating a new gui folder.
    void transform(PGraphics canvas) {
        gui.pushFolder("transform");
        PVector pos = gui.plotXY("pos");
        canvas.translate(pos.x, pos.y);
        canvas.rotate(gui.slider("rotate"));
        PVector scale = gui.plotXY("scale", 1.00f);
        canvas.scale(scale.x, scale.y);
        gui.popFolder();
    }

    // Change drawing style
    void style(PGraphics canvas) {
        gui.pushFolder("style");
        canvas.strokeWeight(gui.slider("weight", 4));
        canvas.stroke(gui.colorPicker("stroke", color(0)).hex);
        canvas.fill(gui.colorPicker("fill", color(200)).hex);
        String rectMode = gui.radio("rect mode", new String[]{"center", "corner"});
        if("center".equals(rectMode)){
            canvas.rectMode(CENTER);
        }else{
            canvas.rectMode(CORNER);
        }
        gui.popFolder();
    }

    // font() related fields
    HashMap<String, PFont> fontCache = new HashMap<String, PFont>();
    HashMap<String, Integer> xAligns;
    HashMap<String, Integer> yAligns;

    // Select from lazily created, cached fonts.
    void font(PGraphics cardBack) {
        gui.pushFolder("font");
        cardBack.fill(gui.colorPicker("fill", color(0)).hex);
        int size = gui.sliderInt("size", 64, 1, 256);
        if (xAligns == null || yAligns == null) {
            xAligns = new HashMap<String, Integer>();
            xAligns.put("left", LEFT);
            xAligns.put("center", CENTER);
            xAligns.put("right", RIGHT);
            yAligns = new HashMap<String, Integer>();
            yAligns.put("top", TOP);
            yAligns.put("center", CENTER);
            yAligns.put("bottom", BOTTOM);
        }
        String xAlignSelection = gui.radio("align x", xAligns.keySet().toArray(new String[0]), "center");
        String yAlignSelection = gui.radio("align y", yAligns.keySet().toArray(new String[0]), "center");
        cardBack.textAlign(xAligns.get(xAlignSelection), yAligns.get(yAlignSelection));
        String fontName = gui.text("font name", "Arial").trim();
        if (gui.button("list fonts")) {
            String[] fonts = PFont.list();
            for (String font : fonts) {
                println(font + "                 "); // some spaces to avoid copying newlines from the console
            }
        }
        String fontKey = fontName + " | size: " + size;
        if (!fontCache.containsKey(fontKey)) {
            PFont loadedFont = createFont(fontName, size);
            if(fontCache.size() > 50){
                fontCache.clear();
            }
            fontCache.put(fontKey, loadedFont);
            println("Loaded font: " + fontKey);
        }
        PFont cachedFont = fontCache.get(fontKey);
        cardBack.textFont(cachedFont);
        gui.popFolder();
    }

}

