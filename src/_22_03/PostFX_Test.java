package _22_03;

import ch.bildspur.postfx.builder.PostFX;
import ch.bildspur.postfx.builder.PostFXBuilder;
import processing.core.PApplet;
import processing.core.PGraphics;
import toolbox.Gui;

public class PostFX_Test extends PApplet {
    Gui gui;
    PGraphics pg;
    String testImagePath = "https://upload.wikimedia.org/wikipedia/en/7/7d/Lenna_%28test_image%29.png";
    PostFX fx;

    public static void main(String[] args) {
        PApplet.main(java.lang.invoke.MethodHandles.lookup().lookupClass());
    }

    @Override
    public void settings() {
        size(512 * 2, 512 * 2, P2D);
    }

    @Override
    public void setup() {
        gui = new Gui(this);
        pg = createGraphics(width, height, P2D);
        fx = new PostFX(this);
    }

    @Override
    public void draw() {
        pg.beginDraw();
        drawBackground();
        pg.image(gui.imagePicker("image", testImagePath), 0, 0);
        pg.endDraw();
        if (gui.toggle("fx/enabled")) {
            PostFXBuilder fxBuilder = fx.render(pg);
            if (gui.toggle("fx/blur/enabled")) {
                fxBuilder.blur(gui.sliderInt("fx/blur/size"), gui.slider("fx/blur/sigma"));
            }
            if (gui.toggle("fx/glitch/enabled")) {
                fxBuilder.binaryGlitch(gui.slider("fx/glitch/strength"));
            }
            if (gui.toggle("fx/noise/enabled")) {
                fxBuilder.noise(gui.slider("fx/noise/amount"),
                        gui.slider("fx/noise/speed"));
            }
            if (gui.toggle("fx/bright pass/enabled")) {
                fxBuilder.brightPass(gui.slider("fx/bright pass/threshold"));
            }
            if (gui.toggle("fx/bloom/enabled")) {
                fxBuilder.bloom(gui.slider("fx/bloom/threshold"),
                        gui.sliderInt("fx/bloom/blur size"),
                        gui.slider("fx/bloom/sigma"));
            }
            if (gui.toggle("fx/rgb split/enabled")) {
                fxBuilder.rgbSplit(gui.slider("fx/rgb split/delta"));
            }
            if (gui.toggle("fx/chromab/enabled")) {
                fxBuilder.chromaticAberration();
            }
            fxBuilder.compose(pg);
        }
        image(pg, 0, 0);
        gui.palettePicker();
        gui.draw();

    }

    @Override
    public void keyPressed() {
        if(key == 'k'){
            save("screenshots/frame.png");
        }
    }

    private void drawBackground() {
        pg.fill(gui.colorPicker("background", color(0,0)).hex);
        pg.noStroke();
        pg.rectMode(CORNER);
        pg.rect(0, 0, width, height);
    }
}
