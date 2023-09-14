package _23_09;

import com.krab.lazy.Input;
import com.krab.lazy.LazyGui;
import com.krab.lazy.LazyGuiSettings;
import com.krab.lazy.ShaderReloader;
import processing.core.PApplet;
import processing.core.PImage;
import processing.opengl.PGraphicsOpenGL;

public class FreeBlur extends PApplet{
    PImage img;
    LazyGui gui;

    public static void main(String[] args) {
        PApplet.main(java.lang.invoke.MethodHandles.lookup().lookupClass());
    }

    @Override
    public void settings() {
        size(600,600, P3D);
    }

    @Override
    public void setup() {
        gui = new LazyGui(this, new LazyGuiSettings().setLoadLatestSaveOnStartup(false));
        img = loadImage("https://picsum.photos/id/866/600/600.jpg");
        colorMode(HSB, 1, 1, 1, 1);
        frameRate(144);
        clear();
        image(img, 0, 0);
    }

    @Override
    public void draw() {
        if(Input.getChar(' ').pressed){
            image(img, 0, 0);
        }

        ((PGraphicsOpenGL)g).textureSampling(gui.sliderInt("sampling", 3));
        String shaderPath = "_23_09\\FreeBlur\\filter.glsl";
        ShaderReloader.getShader(shaderPath).set("frame", frameCount);
        ShaderReloader.filter(shaderPath, g);
    }
}
