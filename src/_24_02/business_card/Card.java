package _24_02.business_card;

import com.krab.lazy.LazyGuiSettings;
import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PGraphics;
import com.krab.lazy.LazyGui;
import processing.core.PVector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Card extends PApplet {
    LazyGui gui;
    PGraphics pg;
    List<PGraphics> cards = new ArrayList<PGraphics>();

    public static void main(String[] args) {
        PApplet.main(java.lang.invoke.MethodHandles.lookup().lookupClass());
    }

    @Override
    public void settings() {
//        size(1600, 800, P2D);
        fullScreen(P2D, 3);
    }

    @Override
    public void setup() {
        gui = new LazyGui(this, new LazyGuiSettings().setCustomGuiDataFolder("..\\gui_data"));
        colorMode(HSB, 1, 1, 1, 1);
        pg = createGraphics(width, height, P2D);
        pg.beginDraw();
        pg.colorMode(HSB, 1, 1, 1, 1);
        pg.endDraw();

        cards.add(createGraphics(900, 500, P2D));
        cards.add(createGraphics(900, 500, P2D));
    }

    @Override
    public void draw() {
        pg.beginDraw();
        drawBackground();
        for (int i = 0; i < cards.size(); i++) {
            String cardName = i == 0 ? "front" : "back";
            PGraphics card = cards.get(i);
            gui.pushFolder("card " + cardName);
            if (gui.toggle("enabled", true)) {
                PVector pos = gui.plotXY("position");
                PVector size = gui.plotXY("size", 900, 500);
                int cardWidth = (int) size.x;
                int cardHeight = (int) size.y;
                if (cardWidth != card.width || cardHeight != card.height) {
                    card = createGraphics(cardWidth, cardHeight, P2D);
                    println("recreated card at " + cardWidth + "x" + cardHeight + " pixels.");
                }
                float scale = gui.slider("scale", 1);
                card.beginDraw();
                card.clear();
                card.colorMode(HSB, 1, 1, 1, 1);
                card.background(gui.colorPicker("background", color(0.5f, 0, 0)).hex);
                drawTexts(card);
                drawLines(card);
                card.endDraw();
                pg.pushMatrix();
                pg.translate(pos.x, pos.y);
                pg.scale(scale);
                pg.image(card, 0, 0);
                pg.popMatrix();
            }
            gui.popFolder();
        }
        pg.endDraw();
        image(pg, 0, 0);
    }

    private void drawLines(PGraphics card) {
        gui.pushFolder("lines");
        int lineCount = gui.sliderInt("line count", 0);
        if (gui.button("add line")) {
            lineCount++;
        }
        gui.sliderSet("line count", lineCount);
        int maxLines = 50;
        for (int i = 0; i < maxLines; i++) {
            gui.pushFolder("line " + i);
            if (i >= lineCount) {
                gui.hideCurrentFolder();
                gui.popFolder();
                continue;
            } else {
                gui.showCurrentFolder();
            }
            card.pushMatrix();
            styleLine(card);
            transform(card);
            PVector a = gui.plotXY("a", 0.0f, 0);
            PVector b = gui.plotXY("b", 0.2f, 0);
            card.line(a.x * card.width, a.y * card.height, b.x * card.width, b.y * card.height);
            card.popMatrix();
            gui.popFolder();
        }
        gui.popFolder();
    }

    private void drawTexts(PGraphics card) {
        gui.pushFolder("texts");
        int textCount = gui.sliderInt("text count", 0);
        if (gui.button("add text")) {
            textCount++;
        }
        gui.sliderSet("text count", textCount);
        int maxTexts = 50;
        for (int i = 0; i < maxTexts; i++) {
            gui.pushFolder("text " + i);
            if (i >= textCount) {
                gui.hideCurrentFolder();
                gui.popFolder();
                continue;
            } else {
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

    // Change position
    void transform(PGraphics canvas) {
        PVector pos = gui.plotXY("pos", 0.2f);
        canvas.translate(pos.x * canvas.width, pos.y * canvas.height);
    }

    // Change drawing style
    void styleLine(PGraphics canvas) {
        gui.pushFolder("style");
        canvas.strokeWeight(gui.slider("weight", 4));
        canvas.stroke(gui.colorPicker("stroke", color(1)).hex);
        gui.popFolder();
    }

    // font() related fields
    HashMap<String, PFont> fontCache = new HashMap<String, PFont>();
    HashMap<String, Integer> xAligns;
    HashMap<String, Integer> yAligns;

    // Select from lazily created, cached fonts.
    void font(PGraphics canvas) {
        gui.pushFolder("font");
        canvas.fill(gui.colorPicker("fill", color(1)).hex);
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
            if (fontCache.size() > 50) {
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

