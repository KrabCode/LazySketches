package _23_08;

import _0_utils.Utils;
import com.krab.lazy.LazyGui;
import com.krab.lazy.LazyGuiSettings;
import com.krab.lazy.PickerColor;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PVector;
import queasycam.QueasyCam;

public class Tunnel extends PApplet {
    LazyGui gui;

    public static void main(String[] args) {
        PApplet.main(java.lang.invoke.MethodHandles.lookup().lookupClass());
    }

    @Override
    public void settings() {
//        size(1080, 1080, P3D);
        fullScreen(P3D);
    }

    @Override
    public void setup() {
        gui = new LazyGui(this, new LazyGuiSettings().setHideBuiltInFolders(true));
        frameRate(144);
    }

    @Override
    public void draw() {
        colorMode(HSB, 1, 1, 1, 1);
        hint(DISABLE_DEPTH_TEST);
        fill(gui.colorPicker("background").hex);
        noStroke();
        rectMode(CORNER);
        rect(0, 0, width, height);
        hint(ENABLE_DEPTH_TEST);
        cameraGui();
        pointLightGui();
        drawTunnel();
        Utils.record(this, gui);
    }

    private void cameraGui() {
        gui.pushFolder("camera");
        PVector pos = gui.plotXYZ("pos");
        translate(pos.x, pos.y, pos.z);
        perspective(gui.slider("fov", PI / 3), (float) width / height,
                gui.slider("near clip", 0.01f),
                gui.slider("far clip", 10000));
        gui.popFolder();
    }

    private void pointLightGui() {
        gui.pushFolder("light");
        PickerColor clr = gui.colorPicker("color");
        PickerColor spec = gui.colorPicker("specular");
        PVector pos = gui.plotXYZ("pos");
        pointLight(clr.hue, clr.saturation, clr.brightness, pos.x, pos.y, pos.z);
        specular(spec.hue, spec.saturation, spec.brightness);
        shininess(gui.slider("shine"));

        gui.popFolder();
    }

    private void drawTunnel() {

        gui.pushFolder("tunnel");
        PVector pos = gui.plotXYZ("pos");
        translate(pos.x, pos.y, pos.z);
        PVector rot = gui.plotXYZ("rot");
        rotateX(rot.x);
        rotateX(rot.y);
        rotateZ(rot.z);
        PVector rotSpd = gui.plotXYZ("rotSpd");
        gui.plotSet("rot", PVector.add(rot, rotSpd));
        strokeWeight(gui.slider("weight", 2));
        stroke(gui.colorPicker("stroke", color(255)).hex);
        fill(gui.colorPicker("fill", color(0)).hex);
        int aDetail = gui.sliderInt("detail A");
        int bDetail = gui.sliderInt("detail B");
        float radiusA = gui.slider("radius");
        float radiusB = radiusA / gui.slider("aspect", 0.4f, 0.0001f, 10f);

        float noiseAmp = gui.slider("noise amp", 1);
        float freqA = gui.slider("noise frq A");
        float freqB = gui.slider("noise frq B");
        for (int aIndex = 0; aIndex <= aDetail; aIndex++) {
            float normACurr = norm(aIndex, 0, aDetail);
            float normAPrev = norm(aIndex + 1, 0, aDetail);
            float thetaACurr = normACurr * TAU;
            float thetaAPrev = normAPrev * TAU;
            pushMatrix();
            beginShape(PConstants.TRIANGLE_STRIP);
            for (int bIndex = 0; bIndex <= bDetail; bIndex++) {
                float normB = norm(bIndex, 0, bDetail);
                float thetaB = normB * TAU;
                float noiseCurr = noiseAmp * noise(-64.648f+cos(thetaACurr) * freqA,73.648f+ sin(thetaB) * freqB);
                float noisePrev = noiseAmp * noise(-64.648f+cos(thetaAPrev) * freqA,73.648f+ sin(thetaB) * freqB);
                PVector p0 = getPointOnTorus(radiusA, radiusB + noiseCurr, thetaACurr, thetaB);
                PVector p1 = getPointOnTorus(radiusA, radiusB + noisePrev, thetaAPrev, thetaB);
                vertex(p0.x, p0.y, p0.z);
                vertex(p1.x, p1.y, p1.z);
            }
            endShape();
            popMatrix();
        }
        gui.popFolder();
    }

    // https://en.wikipedia.org/wiki/Torus#Geometry
    PVector getPointOnTorus(float R, float r, float theta, float phi) {
        return new PVector(
                (R + r * cos(theta)) * cos(phi),
                (R + r * cos(theta)) * sin(phi),
                r * sin(theta)
        );
    }

}
