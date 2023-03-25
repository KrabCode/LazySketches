package _23_03.BoxLandscape;

import _0_utils.Utils;
import com.krab.lazy.ShaderReloader;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;
import com.krab.lazy.LazyGui;
import processing.core.PVector;
import processing.opengl.PShader;

public class BoxLandscape extends PApplet {
    LazyGui gui;
    PGraphics pg;
    float time;

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
        pg = createGraphics(width, height, P3D);
        colorMode(HSB, 1, 1, 1, 1);
        frameRate(60);
    }

    @Override
    public void draw() {
        pg.beginDraw();
        if(gui.toggle("clear 3D canvas", true)){
            pg.clear();
        }
        perspective();
        drawBoxes();
        drawFx();
        pg.endDraw();
        clear();
        drawBackground();
        image(pg, 0, 0);
        gui.draw();
        Utils.updateGetFrameRateAverage(this, gui, 60);
        Utils.drawCustomCursor(this, gui);
        Utils.record(this, gui);
    }

    private void drawBoxes() {
        gui.pushFolder("boxes");
        time += radians(gui.slider("time", 1));
        pg.pushMatrix();
        PVector pos = gui.plotXYZ("position");
        pg.translate(pos.x + width / 2f, pos.y + height / 2f, pos.z);
        PVector rot = gui.plotXYZ("rotation");
        pg.rotateX(rot.x);
        pg.rotateY(rot.y);
        pg.rotateZ(rot.z);
        pg.fill(gui.colorPicker("fill", color(1)).hex);
        pg.stroke(gui.colorPicker("stroke", color(0)).hex);
        pg.strokeWeight(gui.slider("weight", 1));
        PVector xzRange = gui.plotXY("xz range", 400);
        float xRange = xzRange.x;
        float zRange = xzRange.y;
        int xCount = gui.sliderInt("x count", 10);
        int zCount = gui.sliderInt("z count", 10);
        float xMargin = gui.slider("x margin", 5);
        float zMargin = gui.slider("z margin", 5);
        PVector xzSize = new PVector((xRange * 2) / xCount - xMargin, (zRange * 2) / zCount - zMargin);
        float ySize = gui.slider("y size", 50);
        for (int xi = 0; xi < xCount; xi++) {
            for (int zi = 0; zi < zCount; zi++) {
                pg.pushMatrix();
                float x = map(xi, 0, xCount, -xRange, xRange);
                float y = 0;
                float z = map(zi, 0, zCount, -zRange, zRange);
                pg.translate(x, y, z);
                float ySizeLocal =  ySize;

                gui.pushFolder("animations");
                int waveCount = gui.sliderInt("wave count", 1);
                for (int i = 0; i < waveCount; i++) {
                    gui.pushFolder("wave #" + i);
                    PVector waveCenter = gui.plotXY("wave center");
                    float d = dist(waveCenter.x, waveCenter.y,x,z);
                    float ySizeFreq = gui.slider("freq", 0.1f);
                    float ySizeAmp = gui.slider("amp", 10);
                    float yTimeFreq = gui.slider("time freq", 1);
                    ySizeLocal += ySizeAmp * sin(d*ySizeFreq + time * yTimeFreq);
                    gui.popFolder();
                }
                pg.box(xzSize.x, ySizeLocal, xzSize.y);
                pg.popMatrix();
                gui.popFolder();
            }
        }
        pg.popMatrix();
        gui.popFolder();
    }


    private void drawFx() {
        String barrelShaderPath = "filters/sableralph/barrelBlurChroma.glsl";
        String gaussBlurShaderPath = "filters/sableralph/gaussianBlur.glsl";
        gui.pushFolder("fx");
        gui.pushFolder("barrel blur");
        if(gui.toggle("active")){
            ShaderReloader.getShader(barrelShaderPath).set("sketchSize", (float) width, (float) height);
            ShaderReloader.getShader(barrelShaderPath).set("barrelPower", gui.slider("barrel power", 2.2f));
            ShaderReloader.filter(barrelShaderPath, pg);
        }
        gui.popFolder();

        gui.pushFolder("gaussian blur");
        if(gui.toggle("active")){

            PShader gaussianBlur = ShaderReloader.getShader(gaussBlurShaderPath);

            // Control the values with the mouse
            gaussianBlur.set("strength", gui.slider("strength", 7, 0.1f, 9.0f));
            gaussianBlur.set("kernelSize", gui.sliderInt("kernel size", 16, 3, 32));

            // Vertical pass
            gaussianBlur.set("horizontalPass", 0);
            ShaderReloader.filter(gaussBlurShaderPath, pg);

            // Horizontal pass
            gaussianBlur.set("horizontalPass", 1);
            ShaderReloader.filter(gaussBlurShaderPath, pg);
        }
        gui.popFolder();
        gui.popFolder();
    }


    private void drawBackground() {
        image(gui.gradient("background"), 0, 0, width, height);
    }

    public void perspective() {
        gui.pushFolder("perspective");
        float cameraFOV = radians(gui.slider("FOV", 60)); // at least for now
        float cameraY = height / 2.0f;
        float cameraZ = cameraY / ((float) Math.tan(cameraFOV / 2.0f));
        float cameraNear = gui.slider("near", cameraZ / 10.0f);
        float cameraFar = gui.slider("far", cameraZ * 10.0f);
        float cameraAspect = (float) width / (float) height;
        perspective(cameraFOV, cameraAspect, cameraNear, cameraFar);
        gui.popFolder();
    }

    public void perspective(float fov, float aspect, float zNear, float zFar) {
        float ymax = zNear * (float) Math.tan(fov / 2);
        float ymin = -ymax;
        float xmin = ymin * aspect;
        float xmax = ymax * aspect;
        pg.frustum(xmin, xmax, ymin, ymax, zNear, zFar);
    }
}

