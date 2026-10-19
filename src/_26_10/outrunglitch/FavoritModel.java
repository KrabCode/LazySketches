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

    private static final int RED = rgb(205, 20, 30);
    private static final int GLASS = rgb(30, 38, 56);
    private static final int BLACK = rgb(22, 22, 26);
    private static final int BUMPER = rgb(38, 38, 42);
    private static final int HEADLIGHT = rgb(255, 250, 225);
    private static final int INDICATOR = rgb(235, 150, 40);
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
        FavoritModel m = new FavoritModel();
        m.lowerBody();
        m.cabin();
        m.windows();
        m.sideDetails();
        m.frontEnd();
        m.rearEnd();
        m.roofAndMirrors();
        m.arches();
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
        m.discX(RIM_RING, 0.078f, 0, 0, 0.23f, 14, 1);
        m.discX(HUBCAP, 0.079f, 0, 0, 0.19f, 14, 1);
        for (int i = 0; i < 5; i++) {
            float a = PApplet.TWO_PI * i / 5f;
            m.discX(BLACK, 0.081f, PApplet.sin(a) * 0.12f, PApplet.cos(a) * 0.12f, 0.022f, 6, 1);
        }
        m.discX(BLACK, 0.081f, 0, 0, 0.035f, 8, 1);
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
        float cz = 0, cy = 0;
        for (float[] pt : profile) {
            cz += pt[0] / profile.length;
            cy += pt[1] / profile.length;
        }
        for (float sx : new float[]{-hw, hw}) {
            for (int i = 0; i < profile.length; i++) {
                float[] a = profile[i];
                float[] b = profile[(i + 1) % profile.length];
                tri(RED, v(sx, cy, cz), v(sx, a[1], a[0]), v(sx, b[1], b[0]), BODY_CENTER);
            }
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
            sideQuad(BLACK, sign, 0.004f, new float[][]{{1.65f, 0.26f}, {-1.65f, 0.26f}, {-1.65f, 0.34f}, {1.65f, 0.34f}});
            // door cut lines
            for (float zl : new float[]{0.86f, -0.16f, -1.14f}) {
                sideQuad(BLACK, sign, 0.002f, new float[][]{{zl + 0.004f, 0.36f}, {zl - 0.004f, 0.36f}, {zl - 0.004f, 0.98f}, {zl + 0.004f, 0.98f}});
            }
            // door handles
            box(BLACK, sign * 0.80f, sign * 0.825f, 0.80f, 0.84f, -0.10f, 0.04f);
            box(BLACK, sign * 0.80f, sign * 0.825f, 0.80f, 0.84f, -1.04f, -0.90f);
            // indicators wrapping around the front corner, tail lights around the rear corner
            sideQuad(INDICATOR, sign, 0.003f, new float[][]{{1.90f, 0.60f}, {1.74f, 0.60f}, {1.74f, 0.72f}, {1.90f, 0.72f}});
            sideQuad(TAILLIGHT, sign, 0.003f, new float[][]{{-1.78f, 0.62f}, {-1.90f, 0.62f}, {-1.90f, 0.88f}, {-1.78f, 0.88f}});
        }
    }

    private void frontEnd() {
        // big black wrap-around bumper with a lower air dam
        box(BUMPER, -0.82f, 0.82f, 0.30f, 0.56f, 1.88f, 1.97f);
        box(BUMPER, -0.82f, -0.70f, 0.30f, 0.56f, 1.70f, 1.97f);
        box(BUMPER, 0.70f, 0.82f, 0.30f, 0.56f, 1.70f, 1.97f);
        box(BUMPER, -0.70f, 0.70f, 0.23f, 0.31f, 1.84f, 1.94f);
        flatZ(PLATE, -0.26f, 0.26f, 0.38f, 0.47f, 1.974f);
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
        box(rgb(70, 70, 78), -0.48f, -0.42f, 0.20f, 0.26f, -2.03f, -1.88f);
        for (float sign : new float[]{-1, 1}) {
            float x0 = Math.min(sign * 0.55f, sign * 0.795f), x1 = Math.max(sign * 0.55f, sign * 0.795f);
            flatZ(TAILLIGHT, x0, x1, 0.60f, 0.90f, -1.905f);
            float ix0 = Math.min(sign * 0.55f, sign * 0.64f), ix1 = Math.max(sign * 0.55f, sign * 0.64f);
            flatZ(REVERSE, ix0, ix1, 0.62f, 0.68f, -1.908f);
        }
        flatZ(PLATE, -0.26f, 0.26f, 0.64f, 0.72f, -1.905f);
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

    private void arches() {
        for (float sign : new float[]{-1, 1}) {
            for (float cz : new float[]{REAR_AXLE, FRONT_AXLE}) {
                archX(BLACK, sign * 0.8015f, WHEEL_RADIUS, cz, 0.37f, 16, sign);
            }
        }
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
