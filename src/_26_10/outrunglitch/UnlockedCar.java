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

// second scene: the Favorit sitting still on red felt, like the reward screen after unlocking a car
public class UnlockedCar extends PApplet {
    private static final float FIXED_DT = 1f / 60f;
    // one full camera orbit in RECORD_FRAMES frames; AUTO_RECORD starts recording on the first frame
    private static final int RECORD_FRAMES = 600;
    private static final boolean AUTO_RECORD = Boolean.getBoolean("autoRecord");
    private static final String NAME = "ŠMELO FAVORIT";

    LazyGui gui;
    private PGraphics bg;
    private PShape car;
    private PShape wheel;
    private PShape lamps;
    private PGraphics glowBuf, reflBuf;
    private PShader phong;
    private PFont cardFont, statFont;
    private float spin;

    private final PVector eye = new PVector();
    private final PVector fwd = new PVector();
    private final PVector right = new PVector();
    private final PVector down = new PVector();
    private float fov, carYaw;
    private PVector felt = new PVector();
    private PVector neonA = new PVector(), neonB = new PVector();
    private float underglow, underSize, underCore, haze, neonPool;

    public static void main(String[] args) {
        PApplet.main(java.lang.invoke.MethodHandles.lookup().lookupClass());
    }

    public void settings() {
        size(1920, 1080, P3D);
        smooth(4);
    }

    public void setup() {
        frameRate(60);
        gui = new LazyGui(this, new LazyGuiSettings().setLoadLatestSaveOnStartup(false));
        bg = createGraphics(width, height, P2D);
        car = FavoritModel.build(this, true, true);
        wheel = FavoritModel.buildWheel(this);
        lamps = FavoritModel.buildLamps(this);
        glowBuf = createGraphics(width, height, P3D);
        reflBuf = createGraphics(width, height, P3D);
        phong = loadShader(Utils.dataPath("car_phong.frag.glsl"), Utils.dataPath("car_phong.vert.glsl"));
        cardFont = createFont(Utils.dataPath("fonts/Orbitron-VF.ttf"), 128, true);
        statFont = createFont(Utils.dataPath("fonts/BebasNeue-Regular.ttf"), 128, true);
    }

    public void draw() {
        colorMode(RGB, 255);
        neonControls();
        updateCamera();
        updateCarYaw();
        floorBackground();

        camera();
        perspective();
        hint(DISABLE_DEPTH_TEST);
        image(bg, 0, 0);
        drawReflection();
        drawCar();
        drawLightBloom();
        camera();
        perspective();
        drawFlash();
        postPass();
        drawUnlockCard();
        Utils.record(this, gui, RECORD_FRAMES * Integer.getInteger("orbits", 1), AUTO_RECORD);
    }

    private PVector colorVec(String name, int defaultColor) {
        int c = gui.colorPicker(name, defaultColor).hex;
        return new PVector(red(c) / 255f, green(c) / 255f, blue(c) / 255f);
    }

    private PVector hsbVec(float[] hsb) {
        int c = java.awt.Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
        return new PVector((c >> 16 & 255) / 255f, (c >> 8 & 255) / 255f, (c & 255) / 255f);
    }

    private PVector gradientVec(String path, float position) {
        int c = gui.gradientColorAt(path, position).hex;
        return new PVector(red(c) / 255f, green(c) / 255f, blue(c) / 255f);
    }

    private void neonControls() {
        gui.pushFolder("neon");
        String palette = gui.radio("palette", OutrunGlitch.PALETTE_NAMES, "vapor");
        gui.gradient("custom gradient", new int[]{color(255, 120, 30), color(255, 20, 90), color(130, 40, 200)});
        boolean swap = gui.toggle("swap colours", false);
        int preset = java.util.Arrays.asList(OutrunGlitch.PALETTE_NAMES).indexOf(palette);
        PVector first, second;
        if (preset < OutrunGlitch.PALETTES.length) {
            first = hsbVec(OutrunGlitch.PALETTES[preset][0]);
            second = hsbVec(OutrunGlitch.PALETTES[preset][2]);
        } else {
            first = gradientVec("custom gradient", 0f);
            second = gradientVec("custom gradient", 1f);
        }
        neonA = swap ? second : first;
        neonB = swap ? first : second;
        underglow = gui.slider("underglow", 0.1f);
        underSize = gui.slider("underglow size", 3.3f);
        underCore = gui.slider("underglow core", 44f);
        haze = gui.slider("haze", 0.5f);
        neonPool = gui.slider("floor pools", 0.6f);
        gui.popFolder();
    }

    private void updateCamera() {
        gui.pushFolder("camera");
        fov = radians(gui.slider("fov", 30f));
        float h = gui.slider("height", 2.4f);
        float dist = gui.slider("distance", 7.5f);
        float target = gui.slider("target height", 1f);
        float orbitStart = gui.slider("orbit start deg", 0f);
        float orbitSpeed = gui.slider("orbit deg per s", 360f / (RECORD_FRAMES / 60f));
        gui.popFolder();
        float a = radians(orbitStart + orbitSpeed * frameCount * FIXED_DT);
        eye.set(dist * sin(a), -h, dist * cos(a));
        PVector center = new PVector(0, -target, 0);
        fwd.set(PVector.sub(center, eye)).normalize();
        right.set(fwd.cross(new PVector(0, 1, 0))).normalize();
        down.set(right.cross(fwd));
    }

    private void updateCarYaw() {
        gui.pushFolder("car");
        float baseYaw = radians(gui.slider("yaw", -35f));
        spin += radians(gui.slider("turntable deg per s", 0f)) * FIXED_DT;
        gui.popFolder();
        carYaw = baseYaw + spin;
    }

    private void floorBackground() {
        String path = Utils.dataPath("showroom_floor.glsl");
        PShader shader = ShaderReloader.getShader(path);
        shader.set("resolution", (float) bg.width, (float) bg.height);
        shader.set("eye", eye.x, eye.y, eye.z);
        shader.set("camFwd", fwd.x, fwd.y, fwd.z);
        shader.set("camRight", right.x, right.y, right.z);
        shader.set("camDown", down.x, down.y, down.z);
        shader.set("tanHalf", tan(fov / 2f));

        gui.pushFolder("floor");
        felt = colorVec("felt", color(30, 6, 26));
        shader.set("feltColor", felt.x, felt.y, felt.z);
        shader.set("spotStrength", gui.slider("spot strength", 1.4f));
        shader.set("spotRadius", gui.slider("spot radius", 5f));
        shader.set("spotPos", gui.slider("spot x", 0f), gui.slider("spot z", 0.5f));
        shader.set("ambientFloor", gui.slider("ambient", 0.25f));
        shader.set("fogDist", gui.slider("fog distance", 14f));
        shader.set("grain", gui.slider("felt grain", 0.35f));
        shader.set("grainScale", gui.slider("felt grain scale", 220f));
        shader.set("grainFade", gui.slider("felt grain focus fade", 0.12f));
        shader.set("nap", gui.slider("felt nap", 0.12f));
        gui.popFolder();

        gui.pushFolder("lights");
        shader.set("headWash", gui.slider("headlight wash", 1.0f));
        shader.set("headLen", gui.slider("wash length", 100f));
        shader.set("headWidth", gui.slider("wash width", 0.3f));
        shader.set("tailWash", gui.slider("tail light wash", 0.5f));
        gui.popFolder();

        shader.set("neonA", neonA.x, neonA.y, neonA.z);
        shader.set("neonB", neonB.x, neonB.y, neonB.z);
        shader.set("underglow", underglow);
        shader.set("underSize", underSize);
        shader.set("underCore", underCore);
        shader.set("haze", haze);
        shader.set("neonPool", neonPool);

        shader.set("carYaw", carYaw);
        shader.set("carHalf", 0.85f, 1.95f);

        bg.beginDraw();
        bg.endDraw();
        ShaderReloader.filter(path, bg);
    }

    private void drawCar() {
        gui.pushFolder("car lighting");
        float key = gui.slider("key light", 1.0f);
        float rim = gui.slider("rim light", 0.8f);
        float bounce = gui.slider("floor bounce", 0.5f);
        float ambient = gui.slider("ambient", 0.15f);
        float reflAmount = gui.slider("felt reflection", 0.35f);
        float aoAmount = gui.slider("lower body dark", 0.4f);
        gui.popFolder();

        hint(ENABLE_DEPTH_TEST);
        perspective(fov, width / (float) height, 1f, 60f);
        camera(eye.x, eye.y, eye.z, eye.x + fwd.x, eye.y + fwd.y, eye.z + fwd.z, 0, 1, 0);
        ambientLight(255 * ambient, 255 * ambient, 255 * ambient);
        lightSpecular(220, 220, 230);
        directionalLight(255 * key * lerp(1f, neonA.x, 0.35f), 255 * key * lerp(0.96f, neonA.y, 0.35f), 255 * key * lerp(0.92f, neonA.z, 0.35f), 0.5f, 0.7f, -0.7f);
        directionalLight(255 * rim * neonB.x, 255 * rim * neonB.y, 255 * rim * neonB.z, -0.6f, 0.3f, 0.8f);
        directionalLight(255 * bounce * neonA.x, 255 * bounce * neonA.y, 255 * bounce * neonA.z, 0, -1, 0);

        pushMatrix();
        rotateY(carYaw);
        scale(1, -1, 1);
        shader(phong);
        phong.set("specColor", 0.25f, 0.25f, 0.28f);
        phong.set("specShininess", 12f);
        phong.set("reflAmount", reflAmount);
        phong.set("aoAmount", aoAmount);
        phong.set("flatGlass", 1f);
        phong.set("skyTop", neonB.x * 0.5f, neonB.y * 0.5f, neonB.z * 0.5f);
        phong.set("skyHorizon", neonA.x, neonA.y, neonA.z);
        specular(0xFF4A4A4A);
        shininess(12);
        shape(car);
        phong.set("specColor", 0.35f, 0.35f, 0.38f);
        phong.set("specShininess", 20f);
        phong.set("reflAmount", 0f);
        phong.set("aoAmount", 0f);
        specular(0xFF666666);
        shininess(20);
        for (float sign : new float[]{-1, 1}) {
            for (float axle : new float[]{FavoritModel.REAR_AXLE, FavoritModel.FRONT_AXLE}) {
                pushMatrix();
                translate(sign * FavoritModel.WHEEL_X, FavoritModel.WHEEL_RADIUS, axle);
                if (sign < 0) rotateY(PI);
                shape(wheel);
                popMatrix();
            }
        }
        resetShader();
        popMatrix();
        noLights();
        specular(0xFF000000);
        shininess(1);
        drawBeams();
        hint(DISABLE_DEPTH_TEST);
    }

    // headlight shafts: soft additive triangles that turn to face the camera and fade out ahead of the car
    private void drawBeams() {
        gui.pushFolder("lights");
        float strength = gui.slider("beam strength", 0.5f);
        float length = gui.slider("beam length", 4.5f);
        float width = gui.slider("beam width", 1.2f);
        float tilt = gui.slider("beam tilt", 0.08f);
        gui.popFolder();
        if (strength <= 0) return;
        PVector f = new PVector(sin(carYaw), 0, cos(carYaw));
        PVector r = new PVector(cos(carYaw), 0, -sin(carYaw));
        PVector d = new PVector(f.x, tilt, f.z).normalize();
        hint(ENABLE_DEPTH_TEST);
        hint(DISABLE_DEPTH_MASK);
        blendMode(ADD);
        noStroke();
        for (float side : new float[]{-0.575f, 0.575f}) {
            PVector apex = new PVector(side * r.x + 1.92f * f.x, -0.66f, side * r.z + 1.92f * f.z);
            PVector mid = PVector.add(apex, PVector.mult(d, length * 0.5f));
            PVector view = PVector.sub(mid, eye).normalize();
            PVector perp = d.cross(view).normalize();
            // the shaft starts as wide as the lens, seen from this side: 0.43 m across, 0.15 m tall
            float nearHalf = sqrt(sq(perp.dot(r) * 0.215f) + sq(perp.y * 0.075f));
            PVector tip = PVector.add(apex, PVector.mult(d, length));
            for (int layer = 0; layer < 3; layer++) {
                float w = width * (0.35f + 0.35f * layer) / 2f;
                PVector a0 = PVector.add(apex, PVector.mult(perp, nearHalf));
                PVector b0 = PVector.sub(apex, PVector.mult(perp, nearHalf));
                PVector a1 = PVector.add(tip, PVector.mult(perp, w));
                PVector b1 = PVector.sub(tip, PVector.mult(perp, w));
                beginShape(TRIANGLES);
                fill(255, 235, 200, 255 * strength * 0.22f);
                vertex(a0.x, a0.y, a0.z);
                vertex(b0.x, b0.y, b0.z);
                fill(255, 235, 200, 0);
                vertex(a1.x, a1.y, a1.z);
                fill(255, 235, 200, 255 * strength * 0.22f);
                vertex(b0.x, b0.y, b0.z);
                fill(255, 235, 200, 0);
                vertex(a1.x, a1.y, a1.z);
                vertex(b1.x, b1.y, b1.z);
                endShape();
            }
        }
        blendMode(BLEND);
        hint(ENABLE_DEPTH_MASK);
    }

    // the car mirrored under the floor, smeared and faded, tinted by the neon colours
    private void drawReflection() {
        gui.pushFolder("reflection");
        float opacity = gui.slider("amount", 0.6f);
        float fade = gui.slider("fade", 0.3f);
        float blur = gui.slider("blur", 4f);
        float lampBoost = gui.slider("lamp boost", 3f);
        gui.popFolder();
        if (opacity <= 0) return;
        reflBuf.beginDraw();
        reflBuf.background(0);
        reflBuf.hint(ENABLE_DEPTH_TEST);
        reflBuf.perspective(fov, width / (float) height, 1f, 60f);
        reflBuf.camera(eye.x, eye.y, eye.z, eye.x + fwd.x, eye.y + fwd.y, eye.z + fwd.z, 0, 1, 0);
        float contactY = reflBuf.screenY(0, 0, 0);
        reflBuf.pushMatrix();
        reflBuf.rotateY(carYaw);
        reflBuf.shape(car);
        for (float sign : new float[]{-1, 1}) {
            for (float axle : new float[]{FavoritModel.REAR_AXLE, FavoritModel.FRONT_AXLE}) {
                reflBuf.pushMatrix();
                reflBuf.translate(sign * FavoritModel.WHEEL_X, FavoritModel.WHEEL_RADIUS, axle);
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
        sh.set("groundY", 1f - contactY / height);
        sh.set("opacity", opacity);
        sh.set("fade", fade);
        sh.set("blur", blur);
        sh.set("lampBoost", lampBoost);
        sh.set("tint", neonB.x, neonB.y, neonB.z);
        ShaderReloader.filter(path, reflBuf);

        camera();
        perspective();
        blendMode(ADD);
        image(reflBuf, 0, 0);
        blendMode(BLEND);
    }

    // lamp lenses blurred into a glow and added over the car
    private void drawLightBloom() {
        gui.pushFolder("lights");
        float bloom = gui.slider("lamp glow", 0.8f);
        gui.popFolder();
        if (bloom <= 0) return;
        glowBuf.beginDraw();
        glowBuf.background(0);
        glowBuf.hint(ENABLE_DEPTH_TEST);
        glowBuf.perspective(fov, width / (float) height, 1f, 60f);
        glowBuf.camera(eye.x, eye.y, eye.z, eye.x + fwd.x, eye.y + fwd.y, eye.z + fwd.z, 0, 1, 0);
        glowBuf.pushMatrix();
        glowBuf.rotateY(carYaw);
        glowBuf.scale(1, -1, 1);
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
        blur.set("bloom", bloom);
        ShaderReloader.filter(path, glowBuf);

        camera();
        perspective();
        blendMode(ADD);
        image(glowBuf, 0, 0);
        blendMode(BLEND);
    }

    private void drawFlash() {
        gui.pushFolder("unlock card");
        float flash = gui.slider("flash", 0.8f);
        float flashLen = gui.slider("flash length (s)", 0.6f);
        gui.popFolder();

        float f = flash * exp(-frameCount * FIXED_DT / max(flashLen, 0.01f) * 3f);
        if (f > 0.003f) {
            noStroke();
            fill(255, 255, 255, 255 * min(f, 1f));
            rect(0, 0, width, height);
        }
    }

    // drawn after the post pass so the text stays clean
    private void drawUnlockCard() {
        gui.pushFolder("unlock card");
        boolean enabled = gui.toggle("enabled", true);
        float slideLen = gui.slider("slide in (s)", 0.7f);
        float size = gui.slider("text size", 1f);
        float x = gui.slider("x", 0.06f);
        float y = gui.slider("y", 0.14f);
        gui.popFolder();

        float t = frameCount * FIXED_DT;
        noStroke();
        if (!enabled) return;
        float k = constrain(t / max(slideLen, 0.01f), 0, 1);
        k = 1f - pow(1f - k, 3f);
        float ui = height / 720f * size;
        float left = width * x - (1f - k) * 120 * ui;
        textFont(cardFont);
        textAlign(LEFT, BASELINE);
        fill(255, 255, 255, 255 * k);
        textSize(20 * ui);
        text("NEW CAR UNLOCKED", left, height * y);
        textSize(54 * ui);
        text(NAME, left, height * y + 62 * ui);
        fill(255, 60, 60, 255 * k);
        rect(left, height * y + 78 * ui, 160 * ui * k, 4 * ui);
        drawStats(ui, t, slideLen);
    }

    private void drawStats(float ui, float t, float slideLen) {
        gui.pushFolder("unlock card");
        boolean enabled = gui.toggle("stats/enabled", true);
        float right = width * gui.slider("stats/x", 0.94f);
        float top = height * gui.slider("stats/y", 0.14f);
        float spacing = gui.slider("stats/line spacing", 1.38f);
        float panelOpacity = gui.slider("stats/panel opacity", 0.484f);
        float panelPad = gui.slider("stats/panel padding", 17f);
        gui.popFolder();
        if (!enabled) return;
        String[] labels = {"VPIP", "PFR", "WTSD", "BB/100", "HANDS"};
        String[] values = {"82%", "25%", "58%", "-37.4", "13'583"};
        float[] gauge = {0.82f, 0.25f, 0.58f, 0.37f, -1f};
        float rowH = 46 * ui * spacing;
        float colW = 250 * ui;
        float kp = constrain(t / max(slideLen, 0.01f), 0, 1);
        kp = 1f - pow(1f - kp, 3f);
        float pad = panelPad * ui;
        float panelRight = right + pad + (1f - kp) * 120 * ui;
        float panelTop = top - 36 * ui - pad;
        float panelW = colW + 2 * pad;
        float lastRowBelow = gauge[labels.length - 1] < 0 ? 0 : 14 * ui;
        float panelH = (labels.length - 1) * rowH + 36 * ui + lastRowBelow + 2 * pad;
        float radius = 18 * ui;
        noStroke();
        for (int g = 6; g >= 1; g--) {
            float grow = g * 5 * ui;
            fill(0, 0, 0, 255 * panelOpacity * 0.06f * kp);
            rect(panelRight - panelW - grow, panelTop - grow, panelW + 2 * grow, panelH + 2 * grow, radius + grow);
        }
        fill(8, 4, 14, 255 * panelOpacity * kp);
        rect(panelRight - panelW, panelTop, panelW, panelH, radius);
        textFont(statFont);
        for (int i = 0; i < labels.length; i++) {
            float k = constrain((t - 0.15f - i * 0.1f) / max(slideLen, 0.01f), 0, 1);
            k = 1f - pow(1f - k, 3f);
            float rowRight = right + (1f - k) * 120 * ui;
            float baseline = top + i * rowH;
            fill(255, 255, 255, 255 * k);
            textAlign(LEFT, BASELINE);
            textSize(36 * ui);
            text(labels[i], rowRight - colW, baseline);
            textAlign(RIGHT, BASELINE);
            textSize(36 * ui);
            text(values[i], rowRight, baseline);
            if (gauge[i] < 0) continue;
            fill(255, 255, 255, 50 * k);
            rect(rowRight - colW, baseline + 8 * ui, colW, 6 * ui);
            fill(255, 60, 60, 255 * k);
            rect(rowRight - colW, baseline + 8 * ui, colW * gauge[i] * k, 6 * ui);
        }
    }

    private void postPass() {
        String path = Utils.dataPath("post.glsl");
        PShader shader = ShaderReloader.getShader(path);
        shader.set("resolution", (float) width, (float) height);
        shader.set("loopPhase", frameCount / 360f);
        shader.set("loopFrame", (float) frameCount);
        gui.pushFolder("post");
        shader.set("wobble", gui.slider("wobble", 0f));
        shader.set("chroma", gui.slider("chromatic aberration", 0.01f));
        shader.set("burstChroma", gui.slider("burst chroma", 0f));
        shader.set("scanlines", gui.slider("scanlines", 0.06f));
        shader.set("vignette", gui.slider("vignette", 0.9f));
        shader.set("grain", gui.slider("grain", 0.12f));
        gui.popFolder();
        gui.pushFolder("glitch");
        shader.set("glitchAmount", gui.slider("amount", 0f));
        shader.set("glitchRate", gui.slider("bursts per second", 0.5f) * 360f * FIXED_DT);
        shader.set("tear", gui.slider("tear", 0f));
        shader.set("lineJitter", gui.slider("line jitter", 0f));
        shader.set("tracking", gui.slider("tracking band", 0f));
        shader.set("ghost", gui.slider("ghost", 0f));
        gui.popFolder();
        ShaderReloader.filter(path, g);
    }
}
