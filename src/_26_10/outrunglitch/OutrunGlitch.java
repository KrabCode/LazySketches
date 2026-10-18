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
    private float cityScroll;
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
    private float reflAmount, shadowAmount, bloomAmount, aoAmount;
    private PGraphics glowBuf;
    private PShape lamps;
    private final PVector[] headPos = {new PVector(), new PVector()};
    private final PVector[] headDir = {new PVector(), new PVector()};
    private final PVector reflSky = new PVector();

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
        glowBuf = createGraphics(width, height, P3D);
        lamps = FavoritModel.buildLamps(this);
        titleFont = createFont("Impact", 128, true);
    }

    public void draw() {
        float t = millis() / 1000f;
        frameDt = min(1f / max(frameRate, 1f), 0.1f);
        updateCar(t);

        gui.pushFolder("breathing");
        breath = 0.5f + 0.5f * sin(TWO_PI * loopPhase * gui.sliderInt("cycles per loop", 1));
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
        drawWantedStars();
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
        float boostAmount = gui.slider("speed boost", 0f);
        boostSmooth += (driveBoost - boostSmooth) * (1f - exp(-dt / 0.35f));
        float scrollRate = gui.slider("speed", 1.2f) * (1f + boostSmooth * boostAmount);
        roadScroll += dt * scrollRate;
        float baseSpeed = gui.slider("speed", 1.2f);
        cityScroll += dt * baseSpeed * (1f + boostSmooth * boostAmount * gui.slider("city boost share", 0.2f));
        groundSpeedPx = scrollRate * 1.6f * height;
        shader.set("roadScroll", roadScroll);
        shader.set("cityScroll", cityScroll);
        shader.set("skyDim", gui.slider("sky brightness", 0.55f));
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
        float bob = gui.slider("bob", 0.4f);
        float bobSpeed = gui.slider("bob speed", 2f);
        float wheelSpin = gui.slider("wheel spin", 3f);
        boolean loop = gui.toggle("drive loop", false);
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
        addFreewayLights(carX);
        drawShadow(s);
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
        drawCarShapes(roadScroll * 1.6f * height / (FavoritModel.WHEEL_RADIUS * s) * carWheelSpin);
        exhaustPos.set(screenX(-0.45f, 0.23f, -2.03f), screenY(-0.45f, 0.23f, -2.03f));
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

    // wanted level in the top right
    private void drawWantedStars() {
        gui.pushFolder("wanted stars");
        boolean enabled = gui.toggle("enabled", true);
        int level = gui.sliderInt("level", 5, 0, 5);
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
        shader.set("scanlines", gui.slider("scanlines", 0.12f));
        shader.set("vignette", gui.slider("vignette", 0.8f));
        shader.set("grain", gui.slider("grain", 0.08f));
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
