package _26_10.outrunglitch;

import _0_utils.Utils;
import com.krab.lazy.LazyGui;
import com.krab.lazy.LazyGuiSettings;
import com.krab.lazy.ShaderReloader;
import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PGraphics;
import processing.core.PShape;
import processing.core.PVector;
import processing.opengl.PShader;

import java.util.ArrayList;
import java.util.Iterator;

public class OutrunGlitch extends PApplet {
    private static final String[] PALETTE_NAMES = {"neon night", "sunset drive", "miami", "vapor", "gradient"};
    // HSB triples (blob A, B, C) per palette; neighbouring hues so the blends stay rich instead of going grey
    private static final float[][][] PALETTES = {
            {{0.88f, 0.85f, 1.0f}, {0.68f, 0.80f, 0.85f}, {0.52f, 0.80f, 0.95f}},
            {{0.08f, 0.85f, 1.0f}, {0.995f, 0.90f, 1.0f}, {0.82f, 0.80f, 0.60f}},
            {{0.93f, 0.75f, 1.0f}, {0.04f, 0.80f, 1.0f}, {0.50f, 0.80f, 0.80f}},
            {{0.50f, 0.50f, 1.0f}, {0.85f, 0.50f, 1.0f}, {0.62f, 0.60f, 0.90f}}};
    private static final String TITLE = "šmelo in progress";

    LazyGui gui;
    private PGraphics bg;
    private PShape car;
    private PShape wheel;
    private PShader phong;
    private boolean usePhong = true;
    private PFont titleFont;

    private PVector blobA = new PVector();
    private PVector blobB = new PVector();
    private PVector blobC = new PVector();
    private float busRoadY = 0.22f;
    private float driveBoost;
    private float roadScroll;
    private int lastMillis;
    private int lastCarMillis;
    private float bobPhase;
    private float breath;
    private float breathDepth;
    private float hueDrift;

    private float loopPhase;
    private float loopFrame;
    private float boostSmooth;
    private float carX, carSize, carRoadY, carRotX, carYaw, carRotZ, carWheelSpin;
    private boolean carFlip;

    private static class Puff {
        float x, y, vx, vy, age, life, size;
    }

    private final ArrayList<Puff> puffs = new ArrayList<>();
    private final PVector exhaustPos = new PVector(-1000, -1000);
    private float spawnCarry;
    private float groundSpeedPx;
    private float frameDt;

    public static void main(String[] args) {
        PApplet.main(java.lang.invoke.MethodHandles.lookup().lookupClass());
    }

    public void settings() {
        size(1280, 720, P3D);
        smooth(4);
    }

    public void setup() {
        frameRate(60);
        gui = new LazyGui(this, new LazyGuiSettings().setLoadLatestSaveOnStartup(false));
        colorMode(HSB, 1, 1, 1, 1);
        bg = createGraphics(width, height, P2D);
        car = FavoritModel.build(this);
        wheel = FavoritModel.buildWheel(this);
        phong = loadShader(Utils.dataPath("car_phong.frag.glsl"), Utils.dataPath("car_phong.vert.glsl"));
        titleFont = createFont("Impact", 128, true);
    }

    public void draw() {
        float t = millis() / 1000f;
        frameDt = min(1f / max(frameRate, 1f), 0.1f);
        updateCar(t);

        gui.pushFolder("breathing");
        breath = 0.5f + 0.5f * sin(TWO_PI * loopPhase * gui.sliderInt("cycles per loop", 2));
        breathDepth = gui.slider("depth", 0.6f);
        hueDrift = gui.slider("hue drift", 0.15f);
        gui.popFolder();

        freewayBackground(t);

        hint(DISABLE_DEPTH_TEST);
        image(bg, 0, 0);
        drawExhaust();
        drawCar();
        drawTitle();
        postPass();
        Utils.record(this, gui);
    }

    private PVector colorVec(String name, int defaultColor) {
        int c = gui.colorPicker(name, defaultColor).hex;
        return new PVector(red(c), green(c), blue(c));
    }

    // palette colour pushed to full brightness so it still pops as text
    private int neon(PVector rgb) {
        float peak = max(max(rgb.x, rgb.y), max(rgb.z, 0.001f));
        colorMode(RGB, 1, 1, 1, 1);
        int c = color(rgb.x / peak, rgb.y / peak, rgb.z / peak);
        colorMode(HSB, 1, 1, 1, 1);
        return c;
    }

    private PVector gradientVec(String path, float position) {
        int c = gui.gradientColorAt(path, position).hex;
        return new PVector(red(c), green(c), blue(c));
    }

    private PVector hsbVec(float[] hsb) {
        int c = color(hsb[0], hsb[1], hsb[2]);
        return new PVector(red(c), green(c), blue(c));
    }

    private void freewayBackground(float t) {
        String path = Utils.dataPath("bg_freeway.glsl");
        PShader shader = ShaderReloader.getShader(path);
        shader.set("resolution", (float) bg.width, (float) bg.height);
        shader.set("loopPhase", loopPhase);
        shader.set("loopFrame", loopFrame);
        shader.set("breath", breath);
        shader.set("breathDepth", breathDepth);
        shader.set("hueDrift", hueDrift);

        gui.pushFolder("freeway");
        float dt = (millis() - lastMillis) / 1000f;
        lastMillis = millis();
        float boostAmount = gui.slider("speed boost", 1.5f);
        boostSmooth += (driveBoost - boostSmooth) * (1f - exp(-dt / 0.35f));
        float scrollRate = gui.slider("speed", 1.2f) * (1f + boostSmooth * boostAmount);
        roadScroll += dt * scrollRate;
        groundSpeedPx = scrollRate * 1.6f * height;
        shader.set("roadScroll", roadScroll);
        shader.set("cityParallax", gui.slider("city parallax", 2.5f));
        shader.set("boost", boostSmooth);
        shader.set("streaks", gui.slider("streaks", 1f));
        busRoadY = 1f - gui.slider("car road y", 0.78f);
        shader.set("roadY", busRoadY);
        String palette = gui.radio("palette", PALETTE_NAMES, "sunset drive");
        gui.gradient("custom gradient", new int[]{color(0.08f, 0.85f, 1f), color(0.995f, 0.9f, 1f), color(0.82f, 0.8f, 0.6f)});
        int preset = java.util.Arrays.asList(PALETTE_NAMES).indexOf(palette);
        if (preset < PALETTES.length) {
            blobA = hsbVec(PALETTES[preset][0]);
            blobB = hsbVec(PALETTES[preset][1]);
            blobC = hsbVec(PALETTES[preset][2]);
        } else {
            blobA = gradientVec("custom gradient", 0f);
            blobB = gradientVec("custom gradient", 0.5f);
            blobC = gradientVec("custom gradient", 1f);
        }
        shader.set("colA", blobA);
        shader.set("colB", blobB);
        shader.set("colC", blobC);
        gui.popFolder();

        bg.beginDraw();
        bg.endDraw();
        ShaderReloader.filter(path, bg);
    }

    private void updateCar(float t) {
        float carDt = (millis() - lastCarMillis) / 1000f;
        lastCarMillis = millis();

        gui.pushFolder("car");
        float size = gui.slider("size", 0.42f);
        float roadY = gui.slider("road y", 0.78f);
        float x = gui.slider("x", 0.5f);
        boolean flip = gui.toggle("flip direction", false);
        PVector rotate = gui.plotXYZ("rotate", -0.07f, -0.07f, 0f).copy();
        PVector rotateSpeed = gui.plotXYZ("rotate speed", 0f);
        if (rotateSpeed.magSq() > 0) {
            rotate.add(PVector.mult(rotateSpeed, carDt));
            gui.plotSet("rotate", rotate);
        }
        float rotX = rotate.x;
        float yaw = rotate.y;
        float rotZ = rotate.z;
        float bob = gui.slider("bob", 0.7f);
        float bobSpeed = gui.slider("bob speed", 2f);
        float wheelSpin = gui.slider("wheel spin", 1f);
        boolean loop = gui.toggle("drive loop", true);
        int loopFrames = max(gui.sliderInt("loop frames", 360), 1);
        float catchShare = gui.slider("overtake share", 0.3f);
        float holdShare = gui.slider("cruise share", 0.25f);
        float emptyShare = gui.slider("empty road share", 0.2f);
        loopFrame = frameCount % loopFrames;
        loopPhase = loopFrame / loopFrames;
        gui.popFolder();

        if (loop) {
            // the car overtakes from the left, settles next to the camera, then speeds off to the right
            float phase = (frameCount % loopFrames) / (float) loopFrames;
            float reach = 0.5f + size * 0.6f + 0.1f;
            float offLeft = x - reach;
            float offRight = x + reach;
            float driveShare = max(1f - catchShare - holdShare - emptyShare, 0.01f);
            driveBoost = 0;
            if (phase < catchShare) {
                float p = phase / catchShare;
                driveBoost = 0.6f * pow(1 - p, 2);
                x = lerp(offLeft, x, 1 - pow(1 - p, 3));
            } else if (phase < catchShare + holdShare) {
                float p = (phase - catchShare) / holdShare;
                x += sin(p * TWO_PI) * 0.01f;
            } else if (phase < catchShare + holdShare + driveShare) {
                float p = (phase - catchShare - holdShare) / driveShare;
                driveBoost = p * p;
                x = lerp(x, offRight, p * p * p);
            } else {
                // the car is gone, the road stays empty for a while so the smoke can clear
                x = offRight;
            }
        }

        // suspension wiggle: small rotations on every axis, plus the nose lifting when the car accelerates
        bobPhase += carDt * bobSpeed;
        float wiggle = bob * 0.01f;
        rotX += wiggle * 0.6f * sin(bobPhase * 7.7f + 1f);
        yaw += wiggle * 0.5f * sin(bobPhase * 6.1f + 2f);
        rotZ += wiggle * (sin(bobPhase * 11f) + 0.5f * sin(bobPhase * 5.3f)) + driveBoost * 0.03f;

        carX = x;
        carSize = size;
        carRoadY = roadY;
        carRotX = rotX;
        carYaw = yaw;
        carRotZ = rotZ;
        carFlip = flip;
        carWheelSpin = wheelSpin;
    }

    private void drawCar() {
        float s = width * carSize / FavoritModel.LENGTH;
        colorMode(RGB, 255);
        hint(ENABLE_DEPTH_TEST);
        ortho();
        addFreewayLights(carX);
        specular(0xFF8C8C8C);
        shininess(18);
        pushMatrix();
        translate(width * carX, height * carRoadY, 0);
        rotateZ(carRotZ);
        rotateX(carRotX);
        rotateY((carFlip ? -HALF_PI : HALF_PI) + carYaw);
        scale(s, -s, s);
        drawCarShapes(roadScroll * 1.6f * height / (FavoritModel.WHEEL_RADIUS * s) * carWheelSpin);
        exhaustPos.set(screenX(-0.45f, 0.23f, -2.03f), screenY(-0.45f, 0.23f, -2.03f));
        popMatrix();
        noLights();
        specular(0xFF000000);
        shininess(1);
        perspective();
        hint(DISABLE_DEPTH_TEST);
        colorMode(HSB, 1, 1, 1, 1);
    }

    // soft smoke puffs from the tailpipe, left behind in the world so they stream back with the road
    private void drawExhaust() {
        gui.pushFolder("exhaust");
        boolean enabled = gui.toggle("enabled", true);
        float amount = gui.slider("puffs per second", 45f);
        float sizeMul = gui.slider("size", 2.5f);
        float opacity = gui.slider("opacity", 0.5f);
        float accelSmoke = gui.slider("accel smoke", 3f);
        gui.popFolder();

        boolean carOnScreen = exhaustPos.x > -40 && exhaustPos.x < width + 40;
        if (enabled && carOnScreen) {
            spawnCarry += amount * (1f + driveBoost * accelSmoke) * frameDt;
            while (spawnCarry >= 1f) {
                spawnCarry -= 1f;
                Puff pf = new Puff();
                pf.x = exhaustPos.x + random(-3, 3);
                pf.y = exhaustPos.y + random(-3, 3);
                pf.vx = -groundSpeedPx * random(0.6f, 0.95f) + random(-20, 20);
                pf.vy = random(-45, -10);
                pf.life = random(1.0f, 1.8f);
                pf.size = random(8, 16) * sizeMul * carSize / 0.42f;
                puffs.add(pf);
            }
        }

        colorMode(RGB, 255);
        noStroke();
        float dark = constrain(driveBoost, 0, 1);
        for (Iterator<Puff> it = puffs.iterator(); it.hasNext(); ) {
            Puff pf = it.next();
            pf.age += frameDt;
            if (pf.age >= pf.life) {
                it.remove();
                continue;
            }
            pf.x += pf.vx * frameDt;
            pf.y += pf.vy * frameDt;
            float life01 = pf.age / pf.life;
            float diameter = pf.size * (1f + 3.5f * life01);
            float alpha = opacity * 255 * (1f - life01) * (1f - life01);
            float grey = lerp(78, 26, dark);
            for (int k = 0; k < 3; k++) {
                fill(grey + blobA.x * 40, grey + blobA.y * 40, grey + blobA.z * 40, alpha * (0.3f + 0.2f * k));
                ellipse(pf.x, pf.y, diameter * (1f - 0.28f * k), diameter * (1f - 0.28f * k));
            }
        }
        colorMode(HSB, 1, 1, 1, 1);
    }

    private void drawCarShapes(float wheelAngle) {
        if (usePhong) {
            shader(phong);
            phong.set("specColor", 0.25f, 0.25f, 0.28f);
            phong.set("specShininess", 12f);
        }
        specular(0xFF4A4A4A);
        shininess(12);
        shape(car);
        // metal: tight bright highlights on the wheels
        if (usePhong) {
            phong.set("specColor", 1f, 1f, 1f);
            phong.set("specShininess", 45f);
        }
        specular(0xFFFFFFFF);
        shininess(45);
        for (float sign : new float[]{-1, 1}) {
            for (float axle : new float[]{FavoritModel.REAR_AXLE, FavoritModel.FRONT_AXLE}) {
                pushMatrix();
                translate(sign * FavoritModel.WHEEL_X, FavoritModel.WHEEL_RADIUS, axle);
                rotateX(wheelAngle);
                if (sign < 0) rotateY(PI);
                shape(wheel);
                popMatrix();
            }
        }
        if (usePhong) resetShader();
    }

    // sky colour from above plus a point light for every street lamp near the car
    private void addFreewayLights(float carX) {
        gui.pushFolder("car lighting");
        float ambient = gui.slider("ambient", 0f);
        float skyAmount = gui.slider("sky light", 0.5f);
        float lampAmount = gui.slider("lamp light", 1.2f);
        usePhong = gui.toggle("phong per pixel", true);
        float frontAmount = gui.slider("front light", 1f);
        gui.popFolder();

        float aspect = width / (float) height;
        PVector sky = skyColorAt(carX, aspect);
        lightSpecular(200, 200, 215);
        ambientLight(255 * ambient, 255 * ambient, 255 * ambient);
        directionalLight(255 * sky.x * skyAmount, 255 * sky.y * skyAmount, 255 * sky.z * skyAmount, 0, 1, -0.3f);

        // key light from behind the camera, tinted by the sky, so the visible side of the car is lit
        float tintR = 0.6f * sky.x + 0.4f;
        float tintG = 0.6f * sky.y + 0.4f;
        float tintB = 0.6f * sky.z + 0.4f;
        directionalLight(255 * tintR * frontAmount, 255 * tintG * frontAmount, 255 * tintB * frontAmount, 0.25f, 0.45f, -1f);

        float lampPeriod = 1.5f;
        float lampY = height * (1f - (busRoadY + 0.10f + 0.035f + 0.22f - 0.012f));
        float first = ((0.63f - roadScroll * 0.6f) % lampPeriod + lampPeriod) % lampPeriod;
        lightFalloff(0.35f, 0, 6e-6f);
        for (float a = first - lampPeriod; a < aspect + lampPeriod; a += lampPeriod) {
            float lampX = a / aspect * width;
            if (abs(lampX - width * carX) > width * 0.9f) continue;
            pointLight(255 * lampAmount, 215 * lampAmount, 150 * lampAmount, lampX, lampY, 400);
        }
    }

    // same moving colour blobs as the freeway background shader
    private PVector skyColorAt(float xFrac, float aspect) {
        float t = millis() / 1000f;
        float k = 3f + 2f * breath * breathDepth;
        float[][] blobs = {
                {0.25f + 0.15f * sin(TWO_PI * loopPhase), 0.75f + 0.10f * cos(TWO_PI * loopPhase)},
                {0.75f + 0.15f * cos(TWO_PI * loopPhase), 0.55f + 0.15f * sin(TWO_PI * loopPhase * 2f)},
                {0.50f + 0.20f * sin(TWO_PI * loopPhase), 0.20f + 0.10f * cos(TWO_PI * loopPhase * 2f)}};
        PVector[] cols = {blobA, blobB, blobC};
        PVector sum = new PVector();
        float wSum = 0.0001f;
        for (int i = 0; i < 3; i++) {
            float dx = xFrac * aspect - blobs[i][0] * aspect;
            float dy = 0.8f - blobs[i][1];
            float w = exp(-(dx * dx + dy * dy) * k);
            sum.add(PVector.mult(cols[i], w));
            wSum += w;
        }
        return sum.div(wSum);
    }

    private void drawTitle() {
        gui.pushFolder("title");
        float size = gui.slider("size", 0.15f) * height;
        float y = gui.slider("y", 0.2f) * height;
        float spacing = gui.slider("letter spacing", 0f) * size;
        int main = gui.colorPicker("color", color(0f, 0f, 1f)).hex;
        boolean paletteAccent = gui.toggle("palette accent", true);
        String accentBlob = gui.radio("accent blob", new String[]{"A", "B", "C"}, "B");
        int accent = gui.colorPicker("accent", color(0.9f, 0.8f, 1f)).hex;
        if (paletteAccent) {
            PVector blob = accentBlob.equals("A") ? blobA : accentBlob.equals("B") ? blobB : blobC;
            accent = neon(blob);
        }
        float offset = gui.slider("accent offset", 6f);
        gui.popFolder();

        textFont(titleFont, size);
        textAlign(LEFT, CENTER);
        float total = spacing * (TITLE.length() - 1);
        for (int i = 0; i < TITLE.length(); i++) total += textWidth(TITLE.charAt(i));
        float x0 = (width - total) / 2f;

        if (offset > 0) {
            fill(accent);
            drawSpaced(x0 + offset, y + offset, spacing);
        }
        fill(main);
        drawSpaced(x0, y, spacing);
    }

    private void drawSpaced(float x, float y, float spacing) {
        for (int i = 0; i < TITLE.length(); i++) {
            char c = TITLE.charAt(i);
            text(c, x, y);
            x += textWidth(c) + spacing;
        }
    }

    private void postPass() {
        String path = Utils.dataPath("post.glsl");
        PShader shader = ShaderReloader.getShader(path);
        shader.set("resolution", (float) width, (float) height);
        shader.set("loopPhase", loopPhase);
        shader.set("loopFrame", loopFrame);
        gui.pushFolder("post");
        shader.set("wobble", gui.slider("wobble", 0.0015f));
        shader.set("chroma", gui.slider("chromatic aberration", 0.04f));
        shader.set("scanlines", gui.slider("scanlines", 0.12f));
        shader.set("vignette", gui.slider("vignette", 0.8f));
        gui.popFolder();
        gui.pushFolder("glitch");
        shader.set("glitchAmount", gui.slider("amount", 0.6f));
        shader.set("glitchRate", gui.sliderInt("bursts per loop", 18));
        shader.set("tear", gui.slider("tear", 0.05f));
        shader.set("lineJitter", gui.slider("line jitter", 0.004f));
        shader.set("tracking", gui.slider("tracking band", 5f));
        shader.set("ghost", gui.slider("ghost", 0.3f));
        gui.popFolder();
        ShaderReloader.filter(path, g);
    }
}
