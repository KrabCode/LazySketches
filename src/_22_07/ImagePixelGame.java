package _22_07;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.opengl.PShader;
import toolbox.Gui;
import toolbox.ShaderReloader;
import toolbox.windows.nodes.colorPicker.Color;

public class ImagePixelGame extends PApplet {
    Gui gui;
    PGraphics pg;
    String shaderPath = "_22_07/imagePixels.glsl";
    float time = 0;

    public static void main(String[] args) {
        PApplet.main(java.lang.invoke.MethodHandles.lookup().lookupClass());
    }

    @Override
    public void settings() {
//        size(521, 768, P2D);
        fullScreen(P2D);
    }

    @Override
    public void setup() {
        gui = new Gui(this);
        pg = createGraphics(width, height, P2D);
        colorMode(HSB,1,1,1,1);
//        surface.setLocation(2560, 0);
    }

    @Override
    public void draw() {
        pg.beginDraw();
//        pg.clear();
        pg.blendMode(ADD);
        pg.image(gui.imagePicker("image"), 0, 0);
        pg.blendMode(BLEND);
        PShader shader = ShaderReloader.getShader(shaderPath);
        shader.set("time", time += radians(gui.slider("time speed", 1)));
        shader.set("strength", gui.slider("shader/strength", 1));
        Color targetColor = gui.colorPicker("shader/target", color(1));
        shader.set("targetColorRGB", red(targetColor.hex), green(targetColor.hex), blue(targetColor.hex));
        shader.set("targetSmoothstepLow", gui.slider("shader/smooth low", 0));
        shader.set("targetSmoothstepHigh", gui.slider("shader/smooth high", 1));
        shader.set("mouse", (float) mouseX,  (float) height-mouseY);
        ShaderReloader.filter(shaderPath, pg);
        pg.endDraw();
        clear();
        image(pg, 0, 0);
        gui.themePicker();
        gui.draw();
        if(gui.button("save image")){
            pg.save("out/screenshots/ImagePixelGame_"+frameCount+".png");
        }
    }

}