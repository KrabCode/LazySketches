package _26_10.outrunglitch;

import processing.core.PApplet;
import processing.core.PShape;
import processing.core.PVector;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Low-poly Skoda Favorit 5-door hatchback. Units are metres: x = lateral, y = up, z = forward, wheels touch y = 0.
class FavoritModel {
    static final float LENGTH = 3.96f;

    static final float FRONT_AXLE = 1.13f;
    static final float REAR_AXLE = -1.32f;
    static final float WHEEL_RADIUS = 0.285f;
    static final float WHEEL_X = 0.735f;
    private static final float BELT = 0.95f;
    private static final float ARCH_RADIUS = 0.37f;

    private static final int RED = rgb(205, 20, 30);
    private static final int GLASS = rgb(30, 38, 56);
    private static final int BLACK = rgb(22, 22, 26);
    private static final int BUMPER = rgb(38, 38, 42);
    private static final int HEADLIGHT = rgb(255, 250, 225);
    private static final int TAILLIGHT = rgb(255, 40, 40);
    private static final int REVERSE = rgb(235, 235, 225);
    private static final int RUBBER = rgb(18, 18, 22);
    private static final int HUBCAP = rgb(150, 154, 162);
    private static final int RIM_RING = rgb(92, 96, 104);
    private static final int PLATE = rgb(230, 230, 218);
    private static final int BADGE = rgb(150, 190, 150);

    private static final PVector BODY_CENTER = v(0, 0.62f, 0);
    private static final PVector CABIN_CENTER = v(0, 1.15f, -0.4f);

    private final Map<Integer, List<PVector[]>> tris = new LinkedHashMap<>();

    static PShape build(PApplet p) {
        return build(p, false);
    }

    // rearSticker puts a fan of 2-3 offsuit on the lower corner of the rear window
    static PShape build(PApplet p, boolean rearSticker) {
        return build(p, rearSticker, false);
    }

    // hoodButterfly puts a white butterfly on the bonnet
    static PShape build(PApplet p, boolean rearSticker, boolean hoodButterfly) {
        FavoritModel m = new FavoritModel();
        m.lowerBody();
        m.cabin();
        m.windows();
        m.sideDetails();
        m.frontEnd();
        m.rearEnd();
        m.roofAndMirrors();
        m.arches();
        if (rearSticker) m.cardSticker();
        if (hoodButterfly) m.butterfly();
        return m.toShape(p);
    }

    // just the headlight and tail light lenses, used as the source for the bloom
    static PShape buildLamps(PApplet p) {
        FavoritModel m = new FavoritModel();
        m.frontEnd();
        m.rearEnd();
        m.tris.keySet().removeIf(c -> c != HEADLIGHT && c != TAILLIGHT);
        return m.toShape(p);
    }

    // one wheel centred on the origin with its hubcap facing +x, so it can be spun around the x axis
    static PShape buildWheel(PApplet p) {
        FavoritModel m = new FavoritModel();
        m.cylinderX(RUBBER, 0, 0, 0, WHEEL_RADIUS, 0.077f, 14);
        m.discX(RIM_RING, 0.084f, 0, 0, 0.23f, 14, 1);
        m.discX(HUBCAP, 0.088f, 0, 0, 0.19f, 14, 1);
        for (int i = 0; i < 5; i++) {
            float a = PApplet.TWO_PI * i / 5f;
            m.discX(BLACK, 0.092f, PApplet.sin(a) * 0.12f, PApplet.cos(a) * 0.12f, 0.022f, 6, 1);
        }
        m.discX(BLACK, 0.092f, 0, 0, 0.035f, 8, 1);
        return m.toShape(p);
    }

    // half width of the body at a given height: full width up to the belt line, then tumblehome into the roof
    private static float hwAt(float y) {
        return y <= BELT ? 0.80f : PApplet.lerp(0.80f, 0.64f, (y - BELT) / 0.45f);
    }

    private void lowerBody() {
        float hw = 0.80f;
        float[][] profile = {{-1.90f, 0.26f}, {1.90f, 0.26f}, {1.90f, 0.74f}, {0.92f, 0.93f}, {-1.78f, 0.95f}, {-1.90f, 0.91f}};
        for (int i = 0; i < profile.length; i++) {
            float[] a = profile[i];
            float[] b = profile[(i + 1) % profile.length];
            int col = i == 0 ? BLACK : RED;
            quad(col, v(-hw, a[1], a[0]), v(hw, a[1], a[0]), v(hw, b[1], b[0]), v(-hw, b[1], b[0]), BODY_CENTER);
        }
        for (float sx : new float[]{-hw, hw}) {
            sideWithArches(sx < 0 ? -1 : 1);
        }
    }

    private void cabin() {
        quad(RED, v(-0.64f, 1.40f, 0.20f), v(0.64f, 1.40f, 0.20f), v(0.64f, 1.39f, -1.42f), v(-0.64f, 1.39f, -1.42f), CABIN_CENTER);

        PVector fbL = v(-0.80f, BELT, 0.92f), fbR = v(0.80f, BELT, 0.92f);
        PVector rbL = v(-0.80f, BELT, -1.78f), rbR = v(0.80f, BELT, -1.78f);
        PVector ftL = v(-0.64f, 1.40f, 0.20f), ftR = v(0.64f, 1.40f, 0.20f);
        PVector rtL = v(-0.64f, 1.39f, -1.42f), rtR = v(0.64f, 1.39f, -1.42f);

        quad(RED, fbL, fbR, ftR, ftL, CABIN_CENTER);
        glassLayer(BLACK, fbL, fbR, ftR, ftL, 0.03f, 0.97f, 0.04f, 0.94f, 0.004f);
        glassLayer(GLASS, fbL, fbR, ftR, ftL, 0.05f, 0.95f, 0.08f, 0.90f, 0.007f);
        // wipers
        glassLayer(BLACK, fbL, fbR, ftR, ftL, 0.10f, 0.48f, 0.10f, 0.125f, 0.010f);
        glassLayer(BLACK, fbL, fbR, ftR, ftL, 0.52f, 0.90f, 0.10f, 0.125f, 0.010f);

        quad(RED, rbL, rbR, rtR, rtL, CABIN_CENTER);
        glassLayer(BLACK, rbL, rbR, rtR, rtL, 0.04f, 0.96f, 0.06f, 0.92f, 0.004f);
        glassLayer(GLASS, rbL, rbR, rtR, rtL, 0.06f, 0.94f, 0.10f, 0.88f, 0.007f);

        for (float sign : new float[]{-1, 1}) {
            sideQuad(RED, sign, 0, new float[][]{{0.92f, BELT}, {-1.78f, BELT}, {-1.42f, 1.39f}, {0.20f, 1.40f}});
        }
    }

    private void windows() {
        float[][] front = {{0.80f, 1.00f}, {-0.10f, 1.00f}, {-0.10f, 1.33f}, {0.30f, 1.33f}};
        float[][] rear = {{-0.22f, 1.00f}, {-1.30f, 1.00f}, {-1.12f, 1.33f}, {-0.22f, 1.33f}};
        float[][] bPillar = {{-0.10f, 0.96f}, {-0.22f, 0.96f}, {-0.22f, 1.38f}, {-0.10f, 1.38f}};
        for (float sign : new float[]{-1, 1}) {
            sideQuad(BLACK, sign, 0.004f, scaled(front, 1.1f));
            sideQuad(BLACK, sign, 0.004f, scaled(rear, 1.1f));
            sideQuad(BLACK, sign, 0.005f, bPillar);
            sideQuad(GLASS, sign, 0.007f, front);
            sideQuad(GLASS, sign, 0.007f, rear);
        }
    }

    private void sideDetails() {
        for (float sign : new float[]{-1, 1}) {
            // rubbing strip along the doors and a black sill
            sideQuad(BLACK, sign, 0.003f, new float[][]{{1.75f, 0.62f}, {-1.75f, 0.62f}, {-1.75f, 0.67f}, {1.75f, 0.67f}});
            sideQuad(BLACK, sign, 0.004f, new float[][]{{1.65f, 0.26f}, {1.50f, 0.26f}, {1.50f, 0.34f}, {1.65f, 0.34f}});
            sideQuad(BLACK, sign, 0.004f, new float[][]{{0.76f, 0.26f}, {-0.95f, 0.26f}, {-0.95f, 0.34f}, {0.76f, 0.34f}});
            // door cut lines
            for (float zl : new float[]{0.86f, -0.16f, -1.14f}) {
                sideQuad(BLACK, sign, 0.002f, new float[][]{{zl + 0.004f, 0.36f}, {zl - 0.004f, 0.36f}, {zl - 0.004f, 0.98f}, {zl + 0.004f, 0.98f}});
            }
            // door handles
            box(BLACK, sign * 0.80f, sign * 0.825f, 0.80f, 0.84f, -0.10f, 0.04f);
            box(BLACK, sign * 0.80f, sign * 0.825f, 0.80f, 0.84f, -1.04f, -0.90f);
            // tail lights wrapping around the rear corner
            sideQuad(TAILLIGHT, sign, 0.003f, new float[][]{{-1.78f, 0.62f}, {-1.90f, 0.62f}, {-1.90f, 0.88f}, {-1.78f, 0.88f}});
        }
    }

    private void frontEnd() {
        // big black wrap-around bumper with a lower air dam
        box(BUMPER, -0.74f, 0.74f, 0.26f, 0.56f, 1.88f, 1.97f);
        for (float sign : new float[]{-1, 1}) {
            // side wrap back to the wheel arch, with a chamfered front corner
            prism(BUMPER, new float[][]{{sign * 0.74f, 1.88f}, {sign * 0.74f, 1.97f}, {sign * 0.82f, 1.89f}, {sign * 0.82f, 1.88f}}, 0.26f, 0.56f);
            box(BUMPER, sign * 0.70f, sign * 0.82f, 0.26f, 0.56f, 1.50f, 1.88f);
        }
        box(BUMPER, -0.70f, 0.70f, 0.23f, 0.31f, 1.84f, 1.94f);
        flatZ(PLATE, -0.26f, 0.26f, 0.38f, 0.47f, 1.974f);
        plateText("NEZDRZUJ", 0, 0.425f, 1.974f, 1, 0.46f, 0.055f);
        flatZ(BLACK, -0.30f, 0.30f, 0.50f, 0.54f, 1.974f);

        for (float sign : new float[]{-1, 1}) {
            float x0 = Math.min(sign * 0.36f, sign * 0.79f), x1 = Math.max(sign * 0.36f, sign * 0.79f);
            flatZ(HEADLIGHT, x0, x1, 0.585f, 0.735f, 1.903f);
        }
        flatZ(BLACK, -0.30f, 0.30f, 0.585f, 0.665f, 1.903f);
        flatZ(BUMPER, -0.28f, 0.28f, 0.612f, 0.620f, 1.906f);
        flatZ(BUMPER, -0.28f, 0.28f, 0.635f, 0.643f, 1.906f);
        discZ(BADGE, 0f, 0.705f, 1.903f, 0.035f, 10, 1f);
    }

    private void rearEnd() {
        box(BUMPER, -0.82f, 0.82f, 0.30f, 0.56f, -1.99f, -1.88f);
        cylinderZ(rgb(70, 70, 78), -0.45f, 0.23f, -1.96f, -1.88f, 0.035f, 10);
        discZ(BLACK, -0.45f, 0.23f, -1.962f, 0.022f, 10, -1f);
        for (float sign : new float[]{-1, 1}) {
            float x0 = Math.min(sign * 0.55f, sign * 0.795f), x1 = Math.max(sign * 0.55f, sign * 0.795f);
            flatZ(TAILLIGHT, x0, x1, 0.60f, 0.90f, -1.905f);
            float ix0 = Math.min(sign * 0.55f, sign * 0.64f), ix1 = Math.max(sign * 0.55f, sign * 0.64f);
            flatZ(REVERSE, ix0, ix1, 0.62f, 0.68f, -1.908f);
        }
        flatZ(PLATE, -0.26f, 0.26f, 0.38f, 0.47f, -1.992f);
        plateText("NEZDRZUJ", 0, 0.425f, -1.992f, -1, 0.46f, 0.055f);
    }

    private void roofAndMirrors() {
        for (float sign : new float[]{-1, 1}) {
            box(BLACK, sign * 0.55f, sign * 0.60f, 1.40f, 1.445f, -1.25f, 0.0f);
            box(BLACK, sign * 0.80f, sign * 0.84f, 1.00f, 1.04f, 0.60f, 0.66f);
            box(BLACK, sign * 0.84f, sign * 0.96f, 0.97f, 1.08f, 0.52f, 0.70f);
        }
        box(BLACK, -0.60f, 0.60f, 1.40f, 1.44f, -0.55f, -0.50f);
        box(BLACK, -0.60f, 0.60f, 1.40f, 1.44f, -0.05f, 0.0f);
    }

    // dark tunnel behind each wheel so a turned wheel has room inside the body
    private void arches() {
        for (float sign : new float[]{-1, 1}) {
            for (float cz : new float[]{REAR_AXLE, FRONT_AXLE}) {
                archX(BLACK, sign * 0.58f, WHEEL_RADIUS, cz, ARCH_RADIUS, 16, sign);
                PVector ref = v(sign * 0.69f, WHEEL_RADIUS, cz);
                for (int i = 0; i < 8; i++) {
                    float a0 = PApplet.PI * i / 8, a1 = PApplet.PI * (i + 1) / 8;
                    PVector p0 = v(sign * 0.58f, WHEEL_RADIUS + PApplet.sin(a0) * ARCH_RADIUS, cz + PApplet.cos(a0) * ARCH_RADIUS);
                    PVector p1 = v(sign * 0.80f, WHEEL_RADIUS + PApplet.sin(a0) * ARCH_RADIUS, cz + PApplet.cos(a0) * ARCH_RADIUS);
                    PVector p2 = v(sign * 0.80f, WHEEL_RADIUS + PApplet.sin(a1) * ARCH_RADIUS, cz + PApplet.cos(a1) * ARCH_RADIUS);
                    PVector p3 = v(sign * 0.58f, WHEEL_RADIUS + PApplet.sin(a1) * ARCH_RADIUS, cz + PApplet.cos(a1) * ARCH_RADIUS);
                    quad(BLACK, p0, p1, p2, p3, ref);
                }
            }
        }
    }

    // ---------------------------------------------------------------- number plate text

    // letters as polylines in a unit box (0..1 across, 0..1 up)
    private static final java.util.Map<Character, float[][]> GLYPHS = new java.util.HashMap<>();

    static {
        GLYPHS.put('N', new float[][]{{0, 0, 0, 1}, {0, 1, 1, 0}, {1, 0, 1, 1}});
        GLYPHS.put('E', new float[][]{{0, 0, 0, 1}, {0, 1, 1, 1}, {0, 0.5f, 0.8f, 0.5f}, {0, 0, 1, 0}});
        GLYPHS.put('Z', new float[][]{{0, 1, 1, 1}, {1, 1, 0, 0}, {0, 0, 1, 0}});
        GLYPHS.put('D', new float[][]{{0, 0, 0, 1}, {0, 1, 0.6f, 1}, {0.6f, 1, 1, 0.7f}, {1, 0.7f, 1, 0.3f}, {1, 0.3f, 0.6f, 0}, {0.6f, 0, 0, 0}});
        GLYPHS.put('R', new float[][]{{0, 0, 0, 1}, {0, 1, 0.8f, 1}, {0.8f, 1, 1, 0.85f}, {1, 0.85f, 1, 0.65f}, {1, 0.65f, 0.8f, 0.5f}, {0.8f, 0.5f, 0, 0.5f}, {0.5f, 0.5f, 1, 0}});
        GLYPHS.put('U', new float[][]{{0, 1, 0, 0.2f}, {0, 0.2f, 0.2f, 0}, {0.2f, 0, 0.8f, 0}, {0.8f, 0, 1, 0.2f}, {1, 0.2f, 1, 1}});
        GLYPHS.put('J', new float[][]{{1, 1, 1, 0.2f}, {1, 0.2f, 0.8f, 0}, {0.8f, 0, 0.2f, 0}, {0.2f, 0, 0, 0.2f}});
    }

    // black letters on a plate facing dir (+1 front, -1 rear); text reads left to right as seen from outside
    private void plateText(String text, float cx, float cy, float z, float dir, float width, float height) {
        float pitch = width / text.length();
        float letterW = pitch * 0.72f;
        float thick = height * 0.16f;
        int ink = rgb(20, 20, 24);
        PVector ref = v(cx, cy, z - dir);
        float zz = z + dir * 0.003f;
        for (int i = 0; i < text.length(); i++) {
            float[][] strokes = GLYPHS.get(text.charAt(i));
            float x0 = -width / 2 + pitch * i + (pitch - letterW) / 2;
            float y0 = cy - height / 2;
            for (float[] st : strokes) {
                float ax = x0 + st[0] * letterW, ay = y0 + st[1] * height;
                float bx = x0 + st[2] * letterW, by = y0 + st[3] * height;
                float dx = bx - ax, dy = by - ay, len = PApplet.sqrt(dx * dx + dy * dy);
                float ex = dx / len * thick * 0.4f, ey = dy / len * thick * 0.4f;
                float nx = -dy / len * thick / 2, ny = dx / len * thick / 2;
                ax -= ex; ay -= ey; bx += ex; by += ey;
                quad(ink, v(cx + dir * (ax + nx), ay + ny, zz), v(cx + dir * (bx + nx), by + ny, zz),
                        v(cx + dir * (bx - nx), by - ny, zz), v(cx + dir * (ax - nx), ay - ny, zz), ref);
            }
        }
    }

    // ---------------------------------------------------------------- bonnet butterfly

    private static final float HOOD_LEN = (float) Math.sqrt(0.98 * 0.98 + 0.19 * 0.19);

    // bonnet point: u across, v up the bonnet towards the windshield, h above the surface
    private static PVector hoodPt(float u, float v, float h) {
        float t = 0.5f + v;
        return v(u, 0.74f + 0.19f / HOOD_LEN * t + 0.98f / HOOD_LEN * h, 1.90f - 0.98f / HOOD_LEN * t + 0.19f / HOOD_LEN * h);
    }

    private void hoodPoly(int col, float[][] uv, float h) {
        PVector ref = hoodPt(0, 0, -1);
        PVector c = new PVector();
        for (float[] q : uv) c.add(hoodPt(q[0], q[1], h));
        c.div(uv.length);
        for (int i = 0; i < uv.length; i++) {
            float[] a = uv[i], b = uv[(i + 1) % uv.length];
            tri(col, c, hoodPt(a[0], a[1], h), hoodPt(b[0], b[1], h), ref);
        }
    }

    private void hoodStroke(int col, float u0, float v0, float u1, float v1, float width, float h) {
        float du = u1 - u0, dv = v1 - v0, len = PApplet.sqrt(du * du + dv * dv);
        float ex = du / len * width * 0.5f, ey = dv / len * width * 0.5f;
        float nu = -dv / len * width / 2, nv = du / len * width / 2;
        u0 -= ex; v0 -= ey; u1 += ex; v1 += ey;
        hoodPoly(col, new float[][]{{u0 + nu, v0 + nv}, {u1 + nu, v1 + nv}, {u1 - nu, v1 - nv}, {u0 - nu, v0 - nv}}, h);
    }

    // white wings with black outline, veins, body and antennae, symmetric about the bonnet centre
    private void butterfly() {
        int fill = rgb(250, 250, 250), line = rgb(12, 12, 14);
        float[][] upper = {{0.02f, 0.03f}, {0.06f, 0.22f}, {0.20f, 0.36f}, {0.40f, 0.38f}, {0.50f, 0.30f}, {0.46f, 0.16f}, {0.30f, 0.04f}, {0.04f, -0.01f}};
        float[][] lower = {{0.04f, -0.02f}, {0.26f, 0.0f}, {0.34f, -0.10f}, {0.26f, -0.26f}, {0.14f, -0.30f}, {0.06f, -0.18f}};
        float[][][] veins = {{{0.03f, 0.01f}, {0.38f, 0.32f}}, {{0.03f, 0.01f}, {0.46f, 0.22f}}, {{0.03f, 0.0f}, {0.32f, 0.06f}},
                {{0.04f, -0.05f}, {0.28f, -0.14f}}, {{0.04f, -0.05f}, {0.14f, -0.26f}}};
        for (float side : new float[]{-1, 1}) {
            for (float[][] wing : new float[][][]{upper, lower}) {
                float[][] w = new float[wing.length][];
                for (int i = 0; i < wing.length; i++) w[i] = new float[]{side * wing[i][0], wing[i][1]};
                hoodPoly(fill, w, 0.004f);
                for (int i = 0; i < w.length; i++) {
                    float[] a = w[i], b = w[(i + 1) % w.length];
                    hoodStroke(line, a[0], a[1], b[0], b[1], 0.014f, 0.0065f);
                }
            }
            for (float[][] vein : veins) {
                hoodStroke(line, side * vein[0][0], vein[0][1], side * vein[1][0], vein[1][1], 0.010f, 0.0065f);
            }
            hoodStroke(line, 0, 0.17f, side * 0.09f, 0.31f, 0.010f, 0.0065f);
        }
        hoodStroke(line, 0, 0.19f, 0, -0.30f, 0.035f, 0.008f);
    }

    // ---------------------------------------------------------------- rear window sticker

    private static final int WHITE = rgb(250, 250, 250);
    private static final float STICKER_SIZE = 0.20f;
    private float fanShift;
    private static final float DELTA = 0.0007f;

    // white outline cards fanned like a poker hand, 2d over 3s; each card masks the outlines behind it with window-coloured glass
    private void cardSticker() {
        float[][] card = roundedRect(0.72f, 0.95f, 0.08f);
        char[] ranks = {'2', '3'};
        int[] suits = {2, 1};
        for (int i = 0; i < 2; i++) {
            float theta = PApplet.radians((1 - i) * 9f);
            fanShift = -(1 - i) * 0.16f;
            float h = 0.010f + i * 6 * DELTA;
            float[][] outline = new float[card.length][];
            for (int k = 0; k < card.length; k++) outline[k] = fanPoint(theta, card[k][0], card[k][1]);
            stickerPoly(GLASS, outline, h);
            stickerLoop(WHITE, outline, 0.028f, h + DELTA);
            stickerRank(ranks[i], theta, -0.58f, 0.76f, 0.10f, h + 2 * DELTA);
            stickerSuit(suits[i], theta, -0.58f, 0.60f, 0.05f, h + 2 * DELTA);
            if (i == 1) {
                for (float y : new float[]{0.74f, 0.475f, 0.21f}) stickerSuit(suits[i], theta, -0.36f, y, 0.1f, h + 2 * DELTA);
                stickerRank(ranks[i], theta, -0.14f, 0.19f, -0.10f, h + 2 * DELTA);
                stickerSuit(suits[i], theta, -0.14f, 0.35f, -0.05f, h + 2 * DELTA);
            }
        }
    }

    // card-space point (origin at the card's bottom-right corner) rotated about that corner and placed in the sticker
    private float[] fanPoint(float theta, float x, float y) {
        return new float[]{0.80f + fanShift + x * PApplet.cos(theta) - y * PApplet.sin(theta), 0.03f + x * PApplet.sin(theta) + y * PApplet.cos(theta)};
    }

    // sticker space: a to the right and b up as seen from behind the car
    private static PVector stickerPt(float a, float b, float h) {
        PVector origin = v(0.56f, 0.95f + 0.10f * 0.773f, -1.78f + 0.10f * 0.633f);
        return v(origin.x - a * STICKER_SIZE + 0f, origin.y + (b * STICKER_SIZE + 0f) * 0.773f + 0.633f * h,
                origin.z + (b * STICKER_SIZE) * 0.633f - 0.773f * h);
    }

    private void stickerPoly(int col, float[][] pts, float h) {
        PVector ref = stickerPt(0.4f, 0.4f, -1);
        PVector c = new PVector();
        for (float[] p : pts) c.add(stickerPt(p[0], p[1], h));
        c.div(pts.length);
        for (int i = 0; i < pts.length; i++) {
            float[] p = pts[i], q = pts[(i + 1) % pts.length];
            tri(col, c, stickerPt(p[0], p[1], h), stickerPt(q[0], q[1], h), ref);
        }
    }

    private void stickerStroke(int col, float a0, float b0, float a1, float b1, float width, float h) {
        float da = a1 - a0, db = b1 - b0, len = PApplet.sqrt(da * da + db * db);
        float ea = da / len * width * 0.5f, eb = db / len * width * 0.5f;
        float na = -db / len * width / 2, nb = da / len * width / 2;
        a0 -= ea; b0 -= eb; a1 += ea; b1 += eb;
        stickerPoly(col, new float[][]{{a0 + na, b0 + nb}, {a1 + na, b1 + nb}, {a1 - na, b1 - nb}, {a0 - na, b0 - nb}}, h);
    }

    private void stickerLoop(int col, float[][] pts, float width, float h) {
        for (int i = 0; i < pts.length; i++) {
            float[] p = pts[i], q = pts[(i + 1) % pts.length];
            stickerStroke(col, p[0], p[1], q[0], q[1], width, h);
        }
    }

    // card outline in card space: x from -w to 0, y from 0 to height
    private static float[][] roundedRect(float w, float hh, float r) {
        java.util.ArrayList<float[]> pts = new java.util.ArrayList<>();
        float[][] centres = {{-r, hh - r}, {-w + r, hh - r}, {-w + r, r}, {-r, r}};
        for (int c = 0; c < 4; c++) {
            for (int i = 0; i <= 4; i++) {
                float ang = PApplet.HALF_PI * (c + i / 4f);
                pts.add(new float[]{centres[c][0] + r * PApplet.cos(ang), centres[c][1] + r * PApplet.sin(ang)});
            }
        }
        return pts.toArray(new float[0][]);
    }

    // glyph at card-space (x, y), scaled by k; a negative k turns it upside down
    private float[] glyphPt(float theta, float x, float y, float k, float gu, float gv) {
        return fanPoint(theta, x + k * gu, y + k * gv);
    }

    // strokes of a rank glyph in a -1..1 box, each polyline drawn as connected segments
    private void stickerRank(char rank, float theta, float x, float y, float k, float h) {
        float[][][] lines = rank == '2'
                ? new float[][][]{{{-0.8f, 0.55f}, {-0.45f, 1f}, {0.45f, 1f}, {0.8f, 0.55f}, {0.8f, 0.2f}, {-0.8f, -1f}, {0.8f, -1f}}}
                : new float[][][]{{{-0.8f, 0.75f}, {-0.4f, 1f}, {0.4f, 1f}, {0.8f, 0.7f}, {0.8f, 0.3f}, {0.4f, 0.05f}, {-0.2f, 0.05f}},
                {{0.4f, 0.05f}, {0.8f, -0.2f}, {0.8f, -0.7f}, {0.4f, -1f}, {-0.4f, -1f}, {-0.8f, -0.75f}}};
        for (float[][] line : lines) {
            for (int i = 0; i < line.length - 1; i++) {
                float[] a = glyphPt(theta, x, y, k, line[i][0], line[i][1]);
                float[] b = glyphPt(theta, x, y, k, line[i + 1][0], line[i + 1][1]);
                stickerStroke(WHITE, a[0], a[1], b[0], b[1], Math.abs(k) * 0.4f, h);
            }
        }
    }

    // 0 heart, 1 spade, 2 diamond, 3 club
    private void stickerSuit(int suit, float theta, float x, float y, float k, float h) {
        if (suit == 2) {
            stickerGlyph(new float[][]{{0, 1.3f}, {0.9f, 0}, {0, -1.3f}, {-0.9f, 0}}, theta, x, y, k, h);
            return;
        }
        if (suit == 3) {
            for (float[] c : new float[][]{{0, 0.65f}, {-0.62f, -0.2f}, {0.62f, -0.2f}}) {
                float[][] circle = new float[10][];
                for (int i = 0; i < 10; i++) {
                    float t = PApplet.TWO_PI * i / 10;
                    circle[i] = new float[]{c[0] + 0.55f * PApplet.cos(t), c[1] + 0.55f * PApplet.sin(t)};
                }
                stickerGlyph(circle, theta, x, y, k, h);
            }
            stickerGlyph(new float[][]{{-0.12f, -0.1f}, {0.12f, -0.1f}, {0.35f, -1.3f}, {-0.35f, -1.3f}}, theta, x, y, k, h);
            return;
        }
        float flip = suit == 1 ? -1 : 1;
        int n = 28;
        float[][] heart = new float[n][];
        for (int i = 0; i < n; i++) {
            float t = PApplet.TWO_PI * i / n;
            float hx = 16 * PApplet.pow(PApplet.sin(t), 3);
            float hy = 13 * PApplet.cos(t) - 5 * PApplet.cos(2 * t) - 2 * PApplet.cos(3 * t) - PApplet.cos(4 * t);
            heart[i] = new float[]{hx * 0.075f, flip * (hy + 2.5f) * 0.075f};
        }
        stickerGlyph(heart, theta, x, y, k, h);
        if (suit == 1) stickerGlyph(new float[][]{{-0.12f, -0.6f}, {0.12f, -0.6f}, {0.4f, -1.4f}, {-0.4f, -1.4f}}, theta, x, y, k, h);
    }

    private void stickerGlyph(float[][] pts, float theta, float x, float y, float k, float h) {
        float[][] out = new float[pts.length][];
        for (int i = 0; i < pts.length; i++) out[i] = glyphPt(theta, x, y, k, pts[i][0], pts[i][1]);
        stickerPoly(WHITE, out, h);
    }

    // height of the top edge of the lower body side at z
    private static float topY(float z) {
        if (z >= 0.92f) return PApplet.map(z, 0.92f, 1.90f, 0.93f, 0.74f);
        if (z >= -1.78f) return PApplet.map(z, -1.78f, 0.92f, 0.95f, 0.93f);
        return PApplet.map(z, -1.90f, -1.78f, 0.91f, 0.95f);
    }

    // side wall of the lower body with a round hole at each wheel: solid pieces between the arches, strips above them
    private void sideWithArches(int sign) {
        float front = FRONT_AXLE, rear = REAR_AXLE, r = ARCH_RADIUS;
        solidSide(sign, 1.90f, front + r);
        archStrip(sign, front);
        solidSide(sign, front - r, rear + r);
        archStrip(sign, rear);
        solidSide(sign, rear - r, -1.90f);
    }

    private void solidSide(int sign, float z0, float z1) {
        java.util.ArrayList<float[]> pts = new java.util.ArrayList<>();
        pts.add(new float[]{z0, 0.26f});
        pts.add(new float[]{z1, 0.26f});
        pts.add(new float[]{z1, topY(z1)});
        for (float kink : new float[]{-1.78f, 0.92f}) {
            if (kink > z1 && kink < z0) pts.add(new float[]{kink, topY(kink)});
        }
        pts.add(new float[]{z0, topY(z0)});
        sideFan(RED, sign, 0, pts.toArray(new float[0][]), true);
    }

    private void archStrip(int sign, float cz) {
        int n = 8;
        java.util.ArrayList<Float> zs = new java.util.ArrayList<>();
        for (int i = 0; i <= n; i++) zs.add(cz + ARCH_RADIUS - 2 * ARCH_RADIUS * i / n);
        if (0.92f < zs.get(0) && 0.92f > zs.get(n)) {
            zs.add(0.92f);
            java.util.Collections.sort(zs, java.util.Collections.reverseOrder());
        }
        for (int i = 0; i < zs.size() - 1; i++) {
            float za = zs.get(i), zb = zs.get(i + 1);
            float ya = WHEEL_RADIUS + PApplet.sqrt(Math.max(ARCH_RADIUS * ARCH_RADIUS - (za - cz) * (za - cz), 0));
            float yb = WHEEL_RADIUS + PApplet.sqrt(Math.max(ARCH_RADIUS * ARCH_RADIUS - (zb - cz) * (zb - cz), 0));
            sideQuad(RED, sign, 0, new float[][]{{za, ya}, {zb, yb}, {zb, topY(zb)}, {za, topY(za)}});
        }
    }

    // fan for a convex polygon of {z, y} corners on the side of the body
    private void sideFan(int col, int sign, float eps, float[][] pts, boolean faceOut) {
        PVector ref = faceOut ? v(0, 1.0f, 0.35f) : v(sign * 3f, 1.0f, 0.35f);
        for (int i = 1; i < pts.length - 1; i++) {
            tri(col, sidePoint(sign, eps, pts[0]), sidePoint(sign, eps, pts[i]), sidePoint(sign, eps, pts[i + 1]), ref);
        }
    }

    private PVector sidePoint(int sign, float eps, float[] zy) {
        return v(sign * (hwAt(zy[1]) + eps), zy[1], zy[0]);
    }

    // ---------------------------------------------------------------- helpers

    private float[][] scaled(float[][] corners, float factor) {
        float cz = 0, cy = 0;
        for (float[] c : corners) {
            cz += c[0] / corners.length;
            cy += c[1] / corners.length;
        }
        float[][] out = new float[corners.length][2];
        for (int i = 0; i < corners.length; i++) {
            out[i][0] = cz + (corners[i][0] - cz) * factor;
            out[i][1] = cy + (corners[i][1] - cy) * factor;
        }
        return out;
    }

    // quad on the side of the body; corners are {z, y} pairs and x follows the body's tumblehome
    private void sideQuad(int col, float sign, float eps, float[][] c) {
        PVector[] p = new PVector[4];
        for (int i = 0; i < 4; i++) {
            p[i] = v(sign * (hwAt(c[i][1]) + eps), c[i][1], c[i][0]);
        }
        quad(col, p[0], p[1], p[2], p[3], v(0, 1.0f, (c[0][0] + c[2][0]) / 2));
    }

    private void glassLayer(int col, PVector p00, PVector p10, PVector p11, PVector p01,
                            float s0, float s1, float t0, float t1, float offset) {
        PVector n = PVector.sub(p10, p00).cross(PVector.sub(p01, p00)).normalize();
        PVector centroid = PVector.add(PVector.add(p00, p10), PVector.add(p11, p01)).div(4);
        if (n.dot(PVector.sub(centroid, CABIN_CENTER)) < 0) n.mult(-1);
        n.mult(offset);
        PVector a = bilerp(p00, p10, p11, p01, s0, t0).add(n);
        PVector b = bilerp(p00, p10, p11, p01, s1, t0).add(n);
        PVector c = bilerp(p00, p10, p11, p01, s1, t1).add(n);
        PVector d = bilerp(p00, p10, p11, p01, s0, t1).add(n);
        quad(col, a, b, c, d, CABIN_CENTER);
    }

    private static PVector bilerp(PVector p00, PVector p10, PVector p11, PVector p01, float s, float t) {
        PVector bottom = PVector.lerp(p00, p10, s);
        PVector top = PVector.lerp(p01, p11, s);
        return PVector.lerp(bottom, top, t);
    }

    private void flatZ(int col, float x0, float x1, float y0, float y1, float z) {
        PVector ref = v((x0 + x1) / 2, (y0 + y1) / 2, z > 0 ? z - 1 : z + 1);
        quad(col, v(x0, y0, z), v(x1, y0, z), v(x1, y1, z), v(x0, y1, z), ref);
    }

    private void box(int col, float xa, float xb, float y0, float y1, float z0, float z1) {
        float x0 = Math.min(xa, xb), x1 = Math.max(xa, xb);
        PVector ref = v((x0 + x1) / 2, (y0 + y1) / 2, (z0 + z1) / 2);
        quad(col, v(x0, y0, z0), v(x1, y0, z0), v(x1, y1, z0), v(x0, y1, z0), ref);
        quad(col, v(x0, y0, z1), v(x1, y0, z1), v(x1, y1, z1), v(x0, y1, z1), ref);
        quad(col, v(x0, y0, z0), v(x0, y0, z1), v(x0, y1, z1), v(x0, y1, z0), ref);
        quad(col, v(x1, y0, z0), v(x1, y0, z1), v(x1, y1, z1), v(x1, y1, z0), ref);
        quad(col, v(x0, y0, z0), v(x1, y0, z0), v(x1, y0, z1), v(x0, y0, z1), ref);
        quad(col, v(x0, y1, z0), v(x1, y1, z0), v(x1, y1, z1), v(x0, y1, z1), ref);
    }

    private void cylinderX(int col, float cx, float cy, float cz, float r, float halfWidth, int seg) {
        PVector ref = v(cx, cy, cz);
        for (int i = 0; i < seg; i++) {
            float a0 = PApplet.TWO_PI * i / seg, a1 = PApplet.TWO_PI * (i + 1) / seg;
            PVector p0 = v(cx - halfWidth, cy + PApplet.sin(a0) * r, cz + PApplet.cos(a0) * r);
            PVector p1 = v(cx + halfWidth, cy + PApplet.sin(a0) * r, cz + PApplet.cos(a0) * r);
            PVector p2 = v(cx + halfWidth, cy + PApplet.sin(a1) * r, cz + PApplet.cos(a1) * r);
            PVector p3 = v(cx - halfWidth, cy + PApplet.sin(a1) * r, cz + PApplet.cos(a1) * r);
            quad(col, p0, p1, p2, p3, ref);
            tri(col, v(cx - halfWidth, cy, cz), p0, p3, ref);
            tri(col, v(cx + halfWidth, cy, cz), p1, p2, ref);
        }
    }

    // vertical prism over a convex polygon of {x, z} corners
    private void prism(int col, float[][] xz, float y0, float y1) {
        PVector ref = v(0, 0, 0);
        for (float[] p : xz) ref.add(p[0] / xz.length, 0, p[1] / xz.length);
        ref.y = (y0 + y1) / 2;
        for (int i = 1; i < xz.length - 1; i++) {
            tri(col, v(xz[0][0], y0, xz[0][1]), v(xz[i][0], y0, xz[i][1]), v(xz[i + 1][0], y0, xz[i + 1][1]), ref);
            tri(col, v(xz[0][0], y1, xz[0][1]), v(xz[i][0], y1, xz[i][1]), v(xz[i + 1][0], y1, xz[i + 1][1]), ref);
        }
        for (int i = 0; i < xz.length; i++) {
            float[] a = xz[i], b = xz[(i + 1) % xz.length];
            quad(col, v(a[0], y0, a[1]), v(b[0], y0, b[1]), v(b[0], y1, b[1]), v(a[0], y1, a[1]), ref);
        }
    }

    private void cylinderZ(int col, float cx, float cy, float z0, float z1, float r, int seg) {
        PVector ref = v(cx, cy, (z0 + z1) / 2);
        for (int i = 0; i < seg; i++) {
            float a0 = PApplet.TWO_PI * i / seg, a1 = PApplet.TWO_PI * (i + 1) / seg;
            quad(col, v(cx + PApplet.cos(a0) * r, cy + PApplet.sin(a0) * r, z0), v(cx + PApplet.cos(a0) * r, cy + PApplet.sin(a0) * r, z1),
                    v(cx + PApplet.cos(a1) * r, cy + PApplet.sin(a1) * r, z1), v(cx + PApplet.cos(a1) * r, cy + PApplet.sin(a1) * r, z0), ref);
        }
        discZ(col, cx, cy, z0, r, seg, -1f);
    }

    private void discX(int col, float x, float cy, float cz, float r, int seg, float facing) {
        PVector ref = v(x - facing, cy, cz);
        for (int i = 0; i < seg; i++) {
            float a0 = PApplet.TWO_PI * i / seg, a1 = PApplet.TWO_PI * (i + 1) / seg;
            tri(col, v(x, cy, cz), v(x, cy + PApplet.sin(a0) * r, cz + PApplet.cos(a0) * r),
                    v(x, cy + PApplet.sin(a1) * r, cz + PApplet.cos(a1) * r), ref);
        }
    }

    // wheel arch: a disc on the body side that stops at the sill
    private void archX(int col, float x, float cy, float cz, float r, int seg, float facing) {
        PVector ref = v(x - facing, cy, cz);
        for (int i = 0; i < seg; i++) {
            float a0 = PApplet.TWO_PI * i / seg, a1 = PApplet.TWO_PI * (i + 1) / seg;
            float y0 = Math.max(cy + PApplet.sin(a0) * r, 0.26f);
            float y1 = Math.max(cy + PApplet.sin(a1) * r, 0.26f);
            tri(col, v(x, cy, cz), v(x, y0, cz + PApplet.cos(a0) * r), v(x, y1, cz + PApplet.cos(a1) * r), ref);
        }
    }

    private void discZ(int col, float cx, float cy, float z, float r, int seg, float facing) {
        PVector ref = v(cx, cy, z - facing);
        for (int i = 0; i < seg; i++) {
            float a0 = PApplet.TWO_PI * i / seg, a1 = PApplet.TWO_PI * (i + 1) / seg;
            tri(col, v(cx, cy, z), v(cx + PApplet.cos(a0) * r, cy + PApplet.sin(a0) * r, z),
                    v(cx + PApplet.cos(a1) * r, cy + PApplet.sin(a1) * r, z), ref);
        }
    }

    private void quad(int col, PVector a, PVector b, PVector c, PVector d, PVector ref) {
        tri(col, a, b, c, ref);
        tri(col, a, c, d, ref);
    }

    // flat-shaded triangle whose normal is flipped to point away from ref
    private void tri(int col, PVector a, PVector b, PVector c, PVector ref) {
        PVector n = PVector.sub(b, a).cross(PVector.sub(c, a));
        if (n.magSq() < 1e-12f) return;
        n.normalize();
        PVector centroid = PVector.add(PVector.add(a, b), c).div(3);
        if (n.dot(PVector.sub(centroid, ref)) < 0) n.mult(-1);
        tris.computeIfAbsent(col, k -> new ArrayList<>()).add(new PVector[]{a, b, c, n});
    }

    private PShape toShape(PApplet p) {
        PShape group = p.createShape(PApplet.GROUP);
        for (Map.Entry<Integer, List<PVector[]>> entry : tris.entrySet()) {
            PShape child = p.createShape();
            child.beginShape(PApplet.TRIANGLES);
            child.noStroke();
            child.fill(entry.getKey());
            for (PVector[] t : entry.getValue()) {
                for (int i = 0; i < 3; i++) {
                    child.normal(t[3].x, t[3].y, t[3].z);
                    child.vertex(t[i].x, t[i].y, t[i].z);
                }
            }
            child.endShape();
            group.addChild(child);
        }
        return group;
    }

    private static PVector v(float x, float y, float z) {
        return new PVector(x, y, z);
    }

    private static int rgb(int r, int g, int b) {
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }
}
