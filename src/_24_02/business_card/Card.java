package _24_02.business_card;

import com.krab.lazy.LazyGuiSettings;
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
        size(1600, 800, P2D);
    }

    @Override
    public void setup() {
        gui = new LazyGui(this, new LazyGuiSettings().setCustomGuiDataFolder("..\\gui_data"));
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
        gui.pushFolder("card front");
        if(gui.toggle("enabled", true)){
            drawCard(cardFront);
        }
        gui.popFolder();

        gui.pushFolder("card back");
        if(gui.toggle("enabled", false)){
            drawCard(cardBack);
        }
        gui.popFolder();

        pg.endDraw();
        image(pg, 0, 0);
    }

    private void drawCard(PGraphics card) {
        int cardWidth = gui.sliderInt("w", 900);
        int cardHeight = gui.sliderInt("h", 550);
        if(cardWidth != card.width || cardHeight != card.height){
            card = createGraphics(cardWidth, cardHeight, P2D);
            card.beginDraw();
            card.colorMode(HSB, 1, 1, 1, 1);
            card.endDraw();
        }
        card.beginDraw();
        card.clear();
        card.background(gui.colorPicker("background", color(0.5f,0,0)).hex);
        PVector pos = gui.plotXY("position");
        drawTexts(card);
        card.endDraw();
        pg.image(card, pos.x, pos.y);
    }

    private void drawTexts(PGraphics card) {
        gui.pushFolder("texts");
        int textCount = gui.sliderInt("text count", 1);
        if(gui.button("add text")){
            textCount++;
        }
        gui.sliderSet("text count", textCount);
        int maxTexts = 50;
        for(int i = 0; i < maxTexts; i++){
            gui.pushFolder("text " + i);
            if(i >= textCount){
                gui.hideCurrentFolder();
                gui.popFolder();
                continue;
            }else{
                gui.showCurrentFolder();
            }
            card.pushMatrix();
            String value = gui.text("label", "text " + i);
            font(card);
            transform(card);
            card.text(value, 0, 0);
            card.popMatrix();
            gui.popFolder();
        }
        gui.popFolder();
    }

    private void drawBackground() {
        pg.fill(gui.colorPicker("background").hex);
        pg.noStroke();
        pg.rectMode(CORNER);
        pg.rect(0, 0, width, height);
    }


    // Change position and rotation without creating a new gui folder.
    void transform(PGraphics canvas) {
        PVector pos = gui.plotXY("pos", 0.2f);
        canvas.translate(pos.x * canvas.width, pos.y * canvas.height);
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
    void font(PGraphics canvas) {
        gui.pushFolder("font");
        canvas.fill(gui.colorPicker("fill", color(0)).hex);
        int size = max(1, floor(gui.slider("size", 0.1f) * canvas.height));
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
        canvas.textAlign(xAligns.get(xAlignSelection), yAligns.get(yAlignSelection));
        String fontName = gui.text("font name", "Arial").trim();
        if (gui.button("list fonts")) {
            String[] fonts = PFont.list();
            for (String font : fonts) {
                println(font + "       "); // some spaces to avoid copying newlines from the console
            }
        }
        String fontKey = fontName + " | size: " + size;
        if (!fontCache.containsKey(fontKey)) {
            PFont loadedFont = createFont(fontName, size);
            if(fontCache.size() > 50){
                // trying to avoid running out of memory
                fontCache.clear();
            }
            fontCache.put(fontKey, loadedFont);
            println("Loaded font: " + fontKey);
        }
        PFont cachedFont = fontCache.get(fontKey);
        canvas.textFont(cachedFont);
        gui.popFolder();
    }

}

