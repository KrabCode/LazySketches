package _23_01.layers;

import com.krab.lazy.LazyGui;
import com.krab.lazy.ShaderReloader;
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

public class LayerUtils {
    public static Map<Integer, PImage> images = new HashMap<Integer, PImage>();
    public static Map<Integer, PGraphics> canvases = new HashMap<Integer, PGraphics>();
    public static boolean isInitializing = true;

    static void updateDrawLayers(PApplet app, LazyGui gui, PGraphics pg){
        gui.pushFolder("image list");
        int count = gui.sliderInt("count", 0);
        if(gui.button("add layer")){
            gui.sliderSet("count", count + 1);
        }
        for (int i = 0; i < count; i++) {
            updateDrawLayer(app, gui, pg, i);
        }
        gui.popFolder();
        isInitializing = false;
    }

    // TODO crop

    private static void updateDrawLayer(PApplet app, LazyGui gui, PGraphics pg, int canvasIndex) {
        gui.pushFolder("layer " + canvasIndex);
        int canvasWidth = gui.sliderInt("width", app.width, 256, Integer.MAX_VALUE);
        int canvasHeight = gui.sliderInt("height", app.height,256, Integer.MAX_VALUE);
        String imagePath = gui.text("img path");
        if(gui.button("load image") || (imagePath.length() > 0 && isInitializing)){
            try{
                PImage img = app.loadImage(imagePath);
                images.put(canvasIndex, img);
                PGraphics imageCanvas = app.createGraphics(img.width, img.height, P2D);
                gui.sliderSet("width", imageCanvas.width);
                gui.sliderSet("height", imageCanvas.height);
                canvases.put(canvasIndex, imageCanvas);
            }catch(Exception ex){
                println(ex);
            }
        }

        PGraphics canvas = canvases.get(canvasIndex);
        if(gui.button("delete image") || canvas == null){
            PGraphics imageCanvas = app.createGraphics(canvasWidth, canvasHeight, P2D);
            imageCanvas.beginDraw();
            imageCanvas.clear();
            imageCanvas.endDraw();
            canvases.put(canvasIndex, imageCanvas);
            images.remove(canvasIndex);
            canvas = imageCanvas;
        }

        PImage img = images.get(canvasIndex);
        String shaderPath = gui.text("shader path");
        if(gui.toggle("apply shader", true) && shaderPath.length() > 0){
            // add two modes - self feedback (which we have now) and main canvas passthrough as the texture uniform
            PShader shader = ShaderReloader.getShader(shaderPath);
            if(shader != null){
                shader.set("time", radians(app.frameCount));
                if(img != null){
                    shader.set("image", img);
                }
            }
            ShaderReloader.filter(shaderPath, canvas);
        }else{
            if(img != null){
                canvas.beginDraw();
                canvas.image(img, 0, 0);
                canvas.endDraw();
            }
        }

        boolean cornerMode = gui.toggle("center\\/corner mode");
        if(gui.toggle("show canvas", true)){
            PVector pos = PVector.add(gui.plotXY("pos"), new PVector(pg.width/2f, pg.height/2f));
            if(cornerMode){
                pg.imageMode(CORNER);
            }else{
                pg.imageMode(CENTER);
            }
            pg.pushMatrix();
            pg.pushStyle();
            pg.translate(pos.x, pos.y);
            pg.rotate(PI*gui.slider("rotation * π"));
            pg.scale(gui.slider("scale", 1));
            float alpha = gui.slider("alpha", 1, 0, 1);
            pg.colorMode(HSB,1,1,1,1);
            pg.tint(1, alpha);
            pg.image(canvas, 0, 0);
            pg.popMatrix();
            pg.popStyle();
        }
        pg.resetShader();
        gui.popFolder();
    }
}
