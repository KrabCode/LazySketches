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
    static final String[] PALETTE_NAMES = {"neon night", "sunset drive", "miami", "vapor", "gradient"};
    // HSB triples (blob A, B, C) per palette; neighbouring hues so the blends stay rich instead of going grey
    static final float[][][] PALETTES = {
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
    private static final String[] TITLE_FONT_NAMES = {"Orbitron", "Impact", "Anton", "Bebas Neue"};
    private PFont[] titleFonts;
    private PFont tipFont;
    private float ui = 1;

    private PVector blobA = new PVector();
    private PVector blobB = new PVector();
    private PVector blobC = new PVector();
    private float busRoadY = 0.22f;
    private float roadScroll;
    private float cityScroll;
    // every frame advances the animation by the same step, so a slow recording still plays back at the right speed
    private static final float FIXED_DT = 1f / 60f;
    // shader oscillations are written in units of this many frames (6 seconds); time itself never wraps
    private static final float EFFECT_FRAMES = 360f;
    private float bobPhase;
    private float breath;
    private float breathDepth;
    private float hueDrift;

    private float loopPhase;
    private float loopFrame;
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
    private float reflAmount, shadowAmount, bloomAmount, aoAmount;
    private PGraphics glowBuf, reflBuf;
    private PShape lamps;
    private final PVector[] headPos = {new PVector(), new PVector()};
    private final PVector[] headDir = {new PVector(), new PVector()};
    private final PVector reflSky = new PVector();

    public static void main(String[] args) {
        PApplet.main(java.lang.invoke.MethodHandles.lookup().lookupClass());
    }

    public void settings() {
        size(1920, 1080, P3D);
        smooth(4);
    }

    public void setup() {
        frameRate(60);
        ui = height / 720f;
        gui = new LazyGui(this, new LazyGuiSettings().setLoadLatestSaveOnStartup(false));
        colorMode(HSB, 1, 1, 1, 1);
        bg = createGraphics(width, height, P2D);
        car = FavoritModel.build(this);
        wheel = FavoritModel.buildWheel(this);
        phong = loadShader(Utils.dataPath("car_phong.frag.glsl"), Utils.dataPath("car_phong.vert.glsl"));
        glowBuf = createGraphics(width, height, P3D);
        reflBuf = createGraphics(width, height, P3D);
        lamps = FavoritModel.buildLamps(this);
        titleFonts = new PFont[]{
                createFont(Utils.dataPath("fonts/Orbitron-VF.ttf"), 128, true),
                createFont("Impact", 128, true),
                createFont(Utils.dataPath("fonts/Anton-Regular.ttf"), 128, true),
                createFont(Utils.dataPath("fonts/BebasNeue-Regular.ttf"), 128, true)};
        tipFont = createFont("Arial Bold", 64, true);
    }

    public void draw() {
        float t = millis() / 1000f;
        frameDt = FIXED_DT;
        loopFrame = frameCount;
        loopPhase = frameCount / EFFECT_FRAMES;
        updateCar(t);

        gui.pushFolder("breathing");
        breath = 0.5f + 0.5f * sin(TWO_PI * frameCount * FIXED_DT / gui.slider("period (s)", 6f));
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
        drawHud();
        Utils.record(this, gui, 3600);
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
        float dt = FIXED_DT;
        float scrollRate = gui.slider("speed", 1.2f);
        roadScroll += dt * scrollRate;
        cityScroll += dt * scrollRate;
        groundSpeedPx = scrollRate * 1.6f * height;
        shader.set("roadScroll", roadScroll);
        shader.set("cityScroll", cityScroll);
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
        cityControls(shader);
        roadShading(shader);

        bg.beginDraw();
        bg.endDraw();
        ShaderReloader.filter(path, bg);
    }

    // the car lights and shades the asphalt itself
    private void roadShading(PShader shader) {
        gui.pushFolder("road");
        shader.set("carPos", carX, 1f - carRoadY);
        shader.set("carSize", carSize);
        shader.set("carDir", carFlip ? -1f : 1f);
        shader.set("carShadow", gui.slider("road shadow", 0.7f));
        shader.set("headWash", gui.slider("headlight wash", 1.5f));
        shader.set("washStart", gui.slider("wash start", 0.7f));
        shader.set("washLen", gui.slider("wash falloff", 1.2f));
        shader.set("washWidth", gui.slider("wash width", 0.1f));
        shader.set("washY", gui.slider("wash y", 0f));
        shader.set("tailWash", gui.slider("tail light wash", 6.1f));
        shader.set("tailColor", colorVec("tail wash color", color(0f, 0.882f, 0.24f)));
        shader.set("tailStart", gui.slider("tail wash start", 0.57f));
        shader.set("tailLen", gui.slider("tail wash falloff", 5f));
        shader.set("tailWidth", gui.slider("tail wash width", 0.054f));
        shader.set("tailSpread", gui.slider("tail wash spread", 7.6f));
        shader.set("tailY", gui.slider("tail wash y", 0.012f));
        shader.set("tailAngle", radians(gui.slider("tail wash angle", 15f)));
        shader.set("tailPivotY", gui.slider("tail wash pivot", -0.75f) * carSize * width / (float) height / FavoritModel.LENGTH);
        gui.popFolder();
    }

    private void cityControls(PShader shader) {
        gui.pushFolder("city");
        shader.set("skyDim", gui.slider("sky brightness", 0.4f));
        shader.set("cityParallax", gui.slider("parallax", 2.5f));
        shader.set("hazeAmt", gui.slider("depth haze", 1f));
        shader.set("widthVar", gui.slider("width variation", 0.22f));
        shader.set("tierChance", gui.slider("stepped tops", 0.5f));
        PVector win = colorVec("window color", color(0.09f, 0.5f, 1f));
        shader.set("winColor", win);
        shader.set("winBright", gui.slider("window brightness", 0.9f));
        shader.set("winDensity", gui.slider("window density", 0.4f));
        shader.set("winFlicker", gui.slider("window flicker", 0.1f));
        gui.pushFolder("far layer");
        shader.set("farH", gui.slider("min height", 0.10f), gui.slider("height variation", 0.20f));
        gui.popFolder();
        gui.pushFolder("middle layer");
        shader.set("midH", gui.slider("min height", 0.05f), gui.slider("height variation", 0.22f));
        gui.popFolder();
        gui.pushFolder("near layer");
        shader.set("nearH", gui.slider("min height", 0.03f), gui.slider("height variation", 0.14f));
        gui.popFolder();
        gui.popFolder();
    }

    private void updateCar(float t) {
        float carDt = FIXED_DT;

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
        float bob = gui.slider("bob", 0.4f);
        float bobSpeed = gui.slider("bob speed", 2f);
        float wheelSpin = gui.slider("wheel spin", 3f);
        gui.popFolder();

        // suspension wiggle: small rotations on every axis
        bobPhase += carDt * bobSpeed;
        float wiggle = bob * 0.01f;
        rotX += wiggle * 0.6f * sin(bobPhase * 7.7f + 1f);
        yaw += wiggle * 0.5f * sin(bobPhase * 6.1f + 2f);
        rotZ += wiggle * (sin(bobPhase * 11f) + 0.5f * sin(bobPhase * 5.3f));

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
        addFreewayLights(carX);
        float wheelAngle = roadScroll * 1.6f * height / (FavoritModel.WHEEL_RADIUS * s) * carWheelSpin;
        drawShadow(s);
        drawRoadReflection(s, wheelAngle);
        hint(ENABLE_DEPTH_TEST);
        ortho();
        specular(0xFF8C8C8C);
        shininess(18);
        pushMatrix();
        translate(width * carX, height * carRoadY, 0);
        rotateZ(carRotZ);
        rotateX(carRotX);
        rotateY((carFlip ? -HALF_PI : HALF_PI) + carYaw);
        scale(s, -s, s);
        drawCarShapes(wheelAngle);
        exhaustPos.set(screenX(-0.45f, 0.23f, -1.96f), screenY(-0.45f, 0.23f, -1.96f));
        for (int i = 0; i < 2; i++) {
            float lampX = i == 0 ? -0.5f : 0.5f;
            headPos[i].set(screenX(lampX, 0.66f, 1.92f), screenY(lampX, 0.66f, 1.92f));
            headDir[i].set(screenX(lampX, 0.66f, 3.92f) - headPos[i].x, screenY(lampX, 0.66f, 3.92f) - headPos[i].y);
        }
        popMatrix();
        noLights();
        specular(0xFF000000);
        shininess(1);
        perspective();
        hint(DISABLE_DEPTH_TEST);
        drawLightBeams(s);
        drawLightBloom(s);
        colorMode(HSB, 1, 1, 1, 1);
    }

    // headlight cones ahead of the car
    private void drawLightBeams(float s) {
        gui.pushFolder("light beams");
        float beam = gui.slider("headlight beam", 0.6f);
        float beamLen = gui.slider("beam length", 0.15f);
        float beamWidth = gui.slider("beam width", 4f);
        float tilt = tan(radians(gui.slider("beam tilt down", -1f)));
        gui.popFolder();

        if (headDir[0].mag() < 1) return;
        noStroke();
        blendMode(ADD);
        if (beam > 0) {
            float len = width * beamLen;
            for (int i = 0; i < 2; i++) {
                PVector dir = headDir[i].copy().normalize();
                dir.y += tilt * abs(dir.x);
                dir.normalize();
                PVector perp = new PVector(-dir.y, dir.x);
                beamQuad(headPos[i], dir, perp, len, s * 0.05f * beamWidth, len * 0.1f * beamWidth, 255, 235, 190, beam * 45);
                beamQuad(headPos[i], dir, perp, len * 0.8f, s * 0.03f * beamWidth, len * 0.04f * beamWidth, 255, 245, 215, beam * 70);
            }
        }
        blendMode(BLEND);
    }

    // quad that starts at origin with the given alpha and fades to nothing at its far end
    private void beamQuad(PVector o, PVector dir, PVector perp, float len, float w0, float w1, float r, float g, float b, float a) {
        PVector end = PVector.add(o, PVector.mult(dir, len));
        beginShape(QUADS);
        fill(r, g, b, a);
        vertex(o.x + perp.x * w0, o.y + perp.y * w0);
        fill(r, g, b, 0);
        vertex(end.x + perp.x * w1, end.y + perp.y * w1);
        vertex(end.x - perp.x * w1, end.y - perp.y * w1);
        fill(r, g, b, a);
        vertex(o.x - perp.x * w0, o.y - perp.y * w0);
        endShape();
    }

    // only the lamp lenses go into a mask, which is blurred and added over the scene
    private void drawLightBloom(float s) {
        if (bloomAmount <= 0) return;
        glowBuf.beginDraw();
        glowBuf.background(0);
        glowBuf.hint(ENABLE_DEPTH_TEST);
        glowBuf.ortho();
        glowBuf.pushMatrix();
        glowBuf.translate(width * carX, height * carRoadY, 0);
        glowBuf.rotateZ(carRotZ);
        glowBuf.rotateX(carRotX);
        glowBuf.rotateY((carFlip ? -HALF_PI : HALF_PI) + carYaw);
        glowBuf.scale(s, -s, s);
        car.disableStyle();
        glowBuf.noStroke();
        glowBuf.fill(0);
        glowBuf.shape(car);
        car.enableStyle();
        glowBuf.shape(lamps);
        glowBuf.popMatrix();
        glowBuf.endDraw();

        String path = Utils.dataPath("bloom.glsl");
        PShader blur = ShaderReloader.getShader(path);
        blur.set("resolution", (float) width, (float) height);
        blur.set("bloom", bloomAmount);
        ShaderReloader.filter(path, glowBuf);

        blendMode(ADD);
        image(glowBuf, 0, 0);
        blendMode(BLEND);
    }

    // the car mirrored under the ground line, blurred and faded like wet asphalt
    private void drawRoadReflection(float s, float wheelAngle) {
        gui.pushFolder("road");
        float opacity = gui.slider("reflection", 1.2f);
        float fade = gui.slider("reflection fade", 0.4f);
        float blur = gui.slider("reflection blur", 4f);
        float lampBoost = gui.slider("reflection lights", 4f);
        gui.popFolder();
        if (opacity <= 0) return;

        reflBuf.beginDraw();
        reflBuf.background(0);
        reflBuf.hint(ENABLE_DEPTH_TEST);
        reflBuf.ortho();
        reflBuf.pushMatrix();
        reflBuf.translate(width * carX, height * carRoadY, 0);
        reflBuf.rotateZ(carRotZ);
        reflBuf.rotateX(carRotX);
        reflBuf.rotateY((carFlip ? -HALF_PI : HALF_PI) + carYaw);
        reflBuf.scale(s, s, s);
        reflBuf.noStroke();
        reflBuf.shape(car);
        for (float sign : new float[]{-1, 1}) {
            for (float axle : new float[]{FavoritModel.REAR_AXLE, FavoritModel.FRONT_AXLE}) {
                reflBuf.pushMatrix();
                reflBuf.translate(sign * FavoritModel.WHEEL_X, FavoritModel.WHEEL_RADIUS, axle);
                reflBuf.rotateX(wheelAngle);
                if (sign < 0) reflBuf.rotateY(PI);
                reflBuf.shape(wheel);
                reflBuf.popMatrix();
            }
        }
        reflBuf.popMatrix();
        reflBuf.endDraw();

        String path = Utils.dataPath("road_reflect.glsl");
        PShader sh = ShaderReloader.getShader(path);
        sh.set("resolution", (float) width, (float) height);
        sh.set("groundY", 1f - carRoadY);
        sh.set("opacity", opacity);
        sh.set("fade", fade);
        sh.set("blur", blur);
        sh.set("lampBoost", lampBoost);
        float peak = max(max(blobB.x, blobB.y), max(blobB.z, 0.001f));
        sh.set("tint", blobB.x / peak, blobB.y / peak, blobB.z / peak);
        ShaderReloader.filter(path, reflBuf);

        blendMode(ADD);
        image(reflBuf, 0, 0);
        blendMode(BLEND);
    }

    // soft dark ellipse on the asphalt under the car
    private void drawShadow(float s) {
        if (shadowAmount <= 0) return;
        float len = FavoritModel.LENGTH * s * 0.95f;
        noStroke();
        for (int k = 0; k < 8; k++) {
            float f = 1f - 0.09f * k;
            fill(0, 0, 0, shadowAmount * 36);
            ellipse(width * carX, height * carRoadY + len * 0.02f, len * f, len * 0.13f * f);
        }
    }

    // soft smoke puffs from the tailpipe, left behind in the world so they stream back with the road
    private void drawExhaust() {
        gui.pushFolder("exhaust");
        boolean enabled = gui.toggle("enabled", true);
        float amount = gui.slider("puffs per second", 45f);
        float sizeMul = gui.slider("size", 2.5f);
        float opacity = gui.slider("opacity", 0.5f);
        gui.popFolder();

        boolean carOnScreen = exhaustPos.x > -40 && exhaustPos.x < width + 40;
        if (enabled && carOnScreen) {
            spawnCarry += amount * frameDt;
            while (spawnCarry >= 1f) {
                spawnCarry -= 1f;
                Puff pf = new Puff();
                pf.x = exhaustPos.x + random(-3, 3) * ui;
                pf.y = exhaustPos.y + random(-3, 3) * ui;
                pf.vx = -groundSpeedPx * random(0.6f, 0.95f) + random(-20, 20) * ui;
                pf.vy = random(-45, -10) * ui;
                pf.life = random(1.0f, 1.8f);
                pf.size = random(8, 16) * ui * sizeMul * carSize / 0.42f;
                puffs.add(pf);
            }
        }

        colorMode(RGB, 255);
        noStroke();
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
            float grey = 78;
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
            phong.set("reflAmount", reflAmount);
            phong.set("aoAmount", aoAmount);
            phong.set("skyTop", reflSky.x, reflSky.y, reflSky.z);
            phong.set("skyHorizon", blobB.x, blobB.y, blobB.z);
        }
        specular(0xFF4A4A4A);
        shininess(12);
        shape(car);
        // metal: tight bright highlights on the wheels
        if (usePhong) {
            phong.set("specColor", 1f, 1f, 1f);
            phong.set("specShininess", 45f);
            phong.set("reflAmount", 0f);
            phong.set("aoAmount", 0f);
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
        float frontAmount = gui.slider("front light", 0.4f);
        reflAmount = gui.slider("sky reflection", 0.6f);
        shadowAmount = gui.slider("shadow", 0.6f);
        bloomAmount = gui.slider("light bloom", 0.6f);
        aoAmount = gui.slider("lower body dark", 0.5f);
        gui.popFolder();

        float aspect = width / (float) height;
        PVector sky = skyColorAt(carX, aspect);
        reflSky.set(sky);
        lightSpecular(200, 200, 215);
        ambientLight(255 * ambient, 255 * ambient, 255 * ambient);
        directionalLight(255 * sky.x * skyAmount, 255 * sky.y * skyAmount, 255 * sky.z * skyAmount, 0, 1, -0.3f);

        // key light from behind the camera, tinted by the sky, so the visible side of the car is lit
        float tintR = 0.6f * sky.x + 0.4f;
        float tintG = 0.6f * sky.y + 0.4f;
        float tintB = 0.6f * sky.z + 0.4f;
        directionalLight(255 * tintR * frontAmount, 255 * tintG * frontAmount, 255 * tintB * frontAmount, 0.6f, 0.8f, -0.6f);

        float lampPeriod = 1.5f;
        float lampY = height * (1f - (busRoadY + 0.10f + 0.035f + 0.22f - 0.012f));
        float first = ((0.63f - roadScroll * 0.6f) % lampPeriod + lampPeriod) % lampPeriod;
        lightFalloff(0.35f, 0, 6e-6f / (ui * ui));
        for (float a = first - lampPeriod; a < aspect + lampPeriod; a += lampPeriod) {
            float lampX = a / aspect * width;
            if (abs(lampX - width * carX) > width * 0.9f) continue;
            pointLight(255 * lampAmount, 215 * lampAmount, 150 * lampAmount, lampX, lampY, 400 * ui);
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
        String fontName = gui.radio("font", TITLE_FONT_NAMES, "Bebas Neue");
        float size = gui.slider("size", 0.15f) * height;
        float y = gui.slider("y", 0.2f) * height;
        float embolden = gui.slider("embolden", 0f) * size;
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

        PFont font = titleFonts[java.util.Arrays.asList(TITLE_FONT_NAMES).indexOf(fontName)];
        textFont(font, size);
        textAlign(LEFT, CENTER);
        float total = titleWidth(spacing);
        // wide fonts shrink to fit instead of running off the frame
        float maxWidth = width * 0.85f;
        if (total > maxWidth) {
            float k = maxWidth / total;
            size *= k;
            spacing *= k;
            embolden *= k;
            offset *= k;
            textFont(font, size);
            total = titleWidth(spacing);
        }
        float x0 = (width - total) / 2f;

        if (offset > 0) {
            fill(accent);
            drawEmboldened(x0 + offset, y + offset, spacing, embolden);
        }
        fill(main);
        drawEmboldened(x0, y, spacing, embolden);
    }

    private float titleWidth(float spacing) {
        float total = spacing * (TITLE.length() - 1);
        for (int i = 0; i < TITLE.length(); i++) total += textWidth(TITLE.charAt(i));
        return total;
    }

    // thin fonts get thicker by drawing the text a few times in a small circle
    private void drawEmboldened(float x, float y, float spacing, float radius) {
        if (radius > 0.01f) {
            for (int k = 0; k < 8; k++) {
                float a = TWO_PI * k / 8f;
                drawSpaced(x + cos(a) * radius, y + sin(a) * radius, spacing);
            }
        }
        drawSpaced(x, y, spacing);
    }

    // drawn after the post pass so the glitch never touches it
    private void drawHud() {
        drawWantedStars();
        drawTooltip();
    }

    // wanted level in the top right
    private void drawWantedStars() {
        gui.pushFolder("hud stars");
        boolean enabled = gui.toggle("enabled", true);
        int level = gui.sliderInt("level", 3, 0, 5);
        float size = gui.slider("size", 0.032f);
        float x = gui.slider("x", 0.95f);
        float y = gui.slider("y", 0.07f);
        gui.popFolder();
        if (!enabled) return;

        float r = size * height;
        float gap = r * 2.5f;
        colorMode(RGB, 255);
        for (int i = 0; i < 5; i++) {
            float cx = width * x - (4 - i) * gap;
            boolean filled = i < level;
            strokeWeight(r * 0.12f);
            stroke(0, 0, 0, 200);
            if (filled) {
                fill(255, 205, 0);
            } else {
                fill(0, 0, 0, 90);
            }
            beginShape();
            for (int k = 0; k < 10; k++) {
                float a = -HALF_PI + k * PI / 5f;
                float rad = k % 2 == 0 ? r : r * 0.42f;
                vertex(cx + cos(a) * rad, height * y + sin(a) * rad);
            }
            endShape(CLOSE);
        }
        noStroke();
        colorMode(HSB, 1, 1, 1, 1);
    }

    // GTA style help text anchored by its bottom right corner: dark box, button icon and a highlighted keyword
    private void drawTooltip() {
        gui.pushFolder("hud tooltip");
        boolean enabled = gui.toggle("enabled", true);
        float size = gui.slider("size", 0.034f) * height;
        float x = gui.slider("x", 0.98f) * width;
        float y = gui.slider("y", 0.96f) * height;
        int highlight = gui.colorPicker("highlight", color(0.13f, 1f, 1f)).hex;
        gui.popFolder();
        if (!enabled) return;

        colorMode(RGB, 255);
        textFont(tipFont, size);
        textAlign(LEFT, CENTER);
        String before = "Press ";
        String between = " to go ";
        String keyword = "ALL IN";
        float icon = size * 1.25f;
        float pad = size * 0.55f;
        float contentW = textWidth(before) + icon + textWidth(between) + textWidth(keyword);
        float boxH = icon + pad * 1.2f;
        x -= contentW + pad * 2;
        y -= boxH;

        noStroke();
        fill(0, 0, 0, 175);
        rect(x, y, contentW + pad * 2, boxH);

        float cy = y + boxH / 2f;
        float cx = x + pad;
        fill(255);
        text(before, cx, cy - size * 0.08f);
        cx += textWidth(before);

        // PlayStation cross: dark disc, thin light ring, light blue cross
        fill(28, 30, 38);
        stroke(205, 210, 222);
        strokeWeight(size * 0.06f);
        ellipse(cx + icon / 2f, cy, icon, icon);
        float arm = icon * 0.2f;
        float mx = cx + icon / 2f;
        stroke(124, 178, 232);
        strokeWeight(size * 0.13f);
        strokeCap(ROUND);
        line(mx - arm, cy - arm, mx + arm, cy + arm);
        line(mx - arm, cy + arm, mx + arm, cy - arm);
        strokeCap(PROJECT);
        noStroke();
        cx += icon;

        fill(255);
        text(between, cx, cy - size * 0.08f);
        cx += textWidth(between);

        fill(highlight);
        text(keyword, cx, cy - size * 0.08f);
        colorMode(HSB, 1, 1, 1, 1);
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
        shader.set("chroma", gui.slider("chromatic aberration", 0.02f));
        shader.set("burstChroma", gui.slider("burst chroma", 0f));
        shader.set("scanlines", gui.slider("scanlines", 0.12f));
        shader.set("vignette", gui.slider("vignette", 0.8f));
        shader.set("grain", gui.slider("grain", 0.2f));
        gui.popFolder();
        gui.pushFolder("glitch");
        shader.set("glitchAmount", gui.slider("amount", 0.6f));
        shader.set("glitchRate", gui.slider("bursts per second", 3f) * EFFECT_FRAMES * FIXED_DT);
        shader.set("tear", gui.slider("tear", 0f));
        shader.set("lineJitter", gui.slider("line jitter", 0.01f));
        shader.set("tracking", gui.slider("tracking band", 5f));
        shader.set("ghost", gui.slider("ghost", 0.3f));
        gui.popFolder();
        ShaderReloader.filter(path, g);
    }
}
