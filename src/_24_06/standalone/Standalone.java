package _24_06.standalone;

import com.krab.lazy.LazyGui;
import com.krab.lazy.ShaderReloader;
import processing.core.PApplet;

import static _0_utils.Utils.localPath;

public class Standalone extends PApplet {

    LazyGui gui;

    public static void main(String[] args) {
        PApplet.main(java.lang.invoke.MethodHandles.lookup().lookupClass());
    }

    public void settings() {
        size(800, 800, P2D);
    }

    public void setup() {
        gui = new LazyGui(this);
        colorMode(HSB, 1, 1, 1, 1);
    }

    public void draw() {
        background(0);
        String shaderPath = localPath("test.glsl");
        ShaderReloader.getShader(shaderPath).set("time", millis() / 1000f);
        ShaderReloader.filter(shaderPath);
    }

}