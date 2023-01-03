package _23_01.images;

import lazy.LazyGui;
import lazy.ShaderReloader;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PVector;
import processing.opengl.PShader;

import java.util.HashMap;
import java.util.Map;

import static processing.core.PApplet.println;
import static processing.core.PApplet.radians;
import static processing.core.PConstants.*;

public class ImageUtils {
    static Map<Integer, PGraphics> canvases = new HashMap<Integer, PGraphics>();
    static boolean isInitializing = true;

    // TODO layer shaders alone on clear full sketch size canvases without images

    static void updateDrawImages(PApplet app, LazyGui gui, PGraphics pg){
        gui.pushFolder("image list");
        int count = gui.sliderInt("count", 0);
        if(gui.button("add image")){
            gui.sliderSet("count", count + 1);
        }
        for (int i = 0; i < count; i++) {
            updateCanvas(app, gui, pg, i);
        }
        gui.popFolder();
        isInitializing = false;
    }

    private static void updateCanvas(PApplet app, LazyGui gui, PGraphics pg, int canvasIndex) {
        gui.pushFolder("image " + canvasIndex);
        String imagePath = gui.textInput("img path");

        if(gui.button("loadImage") || (imagePath.length() > 0 && isInitializing)){
            try{
                PImage img = app.loadImage(imagePath);
                PGraphics imageCanvas = app.createGraphics(img.width, img.height, P2D);
                imageCanvas.beginDraw();
                imageCanvas.image(img, 0, 0);
                imageCanvas.endDraw();
                canvases.put(canvasIndex, imageCanvas);
                println("loaded image " + canvasIndex + ": " + imagePath);
                gui.toggleSet("display", true);
            }catch(Exception ex){
                println(ex);
            }
        }

        PGraphics canvas = canvases.get(canvasIndex);
        if(canvas == null){
            gui.popFolder();
            return;
        }


        String shaderPath = gui.textInput("shader path");
        if(gui.toggle("apply shader", true)){
            PShader shader = ShaderReloader.getShader(shaderPath);
            if(shader != null){
                shader.set("time", radians(app.frameCount));
            }
            ShaderReloader.shader(shaderPath, pg);
        }

        if(gui.toggle("display")){
            boolean cornerMode = gui.toggle("center\\/corner mode");
            PVector pos = PVector.add(gui.plotXY("pos"), new PVector(pg.width/2f, pg.height/2f));
            if(cornerMode){
                pg.imageMode(CORNER);
            }else{
                pg.imageMode(CENTER);
            }
            pg.pushMatrix();

            pg.translate(pos.x, pos.y);
            pg.rotate(gui.slider("rot"));
            pg.scale(gui.slider("scale", 1));
            pg.image(canvas, 0, 0);
            pg.popMatrix();
        }
        pg.resetShader();
        gui.popFolder();
    }
}
