package _23_01.cyberpunk;

import _0_utils.Utils;
import _22_03.PostFxAdapter;
import com.krab.lazy.PickerColor;
import com.krab.lazy.ShaderReloader;
import processing.core.PApplet;
import processing.core.PGraphics;
import com.krab.lazy.LazyGui;
import processing.core.PImage;
import processing.core.PVector;
import processing.opengl.PShader;

import java.util.ArrayList;
import java.util.HashMap;

public class Cyberpunk extends PApplet {
    LazyGui gui;
    PGraphics pg;
    HashMap<String, PImage> imageMap = new HashMap<String, PImage>();
    ArrayList<PGraphics> graphics = new ArrayList<>();
    private float t;

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
        t += radians(gui.slider("time +", 1));
        drawBackground();
        drawLayers();
        pg.endDraw();
        image(pg, 0, 0);
        Utils.record(this, gui);
    }

    private void drawLayers() {
        gui.pushFolder("layers");
        PVector globalPos = gui.plotXY("global pos");
        PVector globalScale = gui.plotXY("global scale", 0.5f);
        pg.pushMatrix();
        pg.translate(globalPos.x, globalPos.y);
        pg.scale(globalScale.x, globalScale.y);
        int layerCount = gui.sliderInt("layer count", 8);
        for (int i = 0; i < layerCount; i++) {
            gui.pushFolder("layer " + i);
            String imagePath = gui.text("img path", getImagePathByIndex(i));
            PImage img = tryGetImage(imagePath);
            if(img == null || !gui.toggle("active", true)){
                gui.popFolder();
                continue;
            }
            PGraphics gg = getGraphics(i);
            gg.beginDraw();
            gg.clear();
            gg.imageMode(CENTER);
            gg.image(img, gg.width/2f, gg.height/2f);
            String shaderPath = gui.text("shader path");
            if (!"".equals(shaderPath) && gui.toggle("active shader", true)) {
                PShader shader = ShaderReloader.getShader(shaderPath);
                if(shader != null){
                    shader.set("time", t);
                    ShaderReloader.filter(shaderPath, gg);
                }
            }
            if(gui.toggle("shader move")){
                Utils.shaderMove(gg, gui, "C:\\Users\\Krab\\Documents\\GitHub\\LazySketches\\data\\_23_01\\cyberpunk\\shaders\\move.glsl");
            }
            gg.endDraw();
            PostFxAdapter.apply(this, gui, gg);
            PVector pos = gui.plotXY("pos");
            PVector localScale = gui.plotXY("scale", 1);
            pg.pushMatrix();
            pg.translate(pos.x, pos.y);
            pg.scale(localScale.x, localScale.y);
            pg.image(gg, 0, 0);
            pg.popMatrix();
            gui.popFolder();
        }
        pg.popMatrix();
        gui.popFolder();
    }

    private PGraphics getGraphics(int i) {
        if(i >= graphics.size()){
            graphics.add(createGraphics(width*2, height*2, P2D));
        }
        return graphics.get(i);
    }

    private void drawBackground() {
        pg.fill(gui.colorPicker("background").hex);
        pg.noStroke();
        pg.rectMode(CORNER);
        pg.rect(0, 0, width, height);
        String bgPath = "_23_01/cyberpunk/shaders/background.glsl";
        PickerColor clrA = gui.colorPicker("a");
        PickerColor clrB = gui.colorPicker("b");
        ShaderReloader.getShader(bgPath).set("rgbA", red(clrA.hex), green(clrA.hex), blue(clrA.hex));
        ShaderReloader.getShader(bgPath).set("rgbB", red(clrB.hex), green(clrB.hex), blue(clrB.hex));
        ShaderReloader.filter(bgPath, pg);
    }

    PImage tryGetImage(String path){
        if("".equals(path)){
            return null;
        }
        if(!imageMap.containsKey(path)){
            try{
                imageMap.put(path, loadImage(path));
            }catch(Exception ex){
                println(ex);
                if(!imageMap.containsKey(path)){
                    imageMap.put(path, null);
                }
            }
        }
        return imageMap.get(path);

    }

    private String getImagePathByIndex(int i) {
        switch(i){
            case 0: {return "_23_01/cyberpunk/assets/shadow.png"; }
            case 1: {return "_23_01/cyberpunk/assets/8-legs.png"; }
            case 2: {return "_23_01/cyberpunk/assets/7-torso.png"; }
            case 3: {return "_23_01/cyberpunk/assets/5-jacket.png"; }
            case 4: {return "_23_01/cyberpunk/assets/4-head.png"; }
            case 5: {return "_23_01/cyberpunk/assets/3-eye-copy.png"; }
            case 6: {return "_23_01/cyberpunk/assets/2-wire-bridge.png"; }
            case 7: {return "_23_01/cyberpunk/assets/1-hand.png"; }
            default: return "";
        }
    }
}

