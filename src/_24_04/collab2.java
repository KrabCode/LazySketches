package _24_04;

import com.krab.lazy.ShaderReloader;
import processing.core.PApplet;
import processing.core.PGraphics;
import com.krab.lazy.LazyGui;
import processing.core.PVector;

import java.util.ArrayList;

public class collab2 extends PApplet {
    LazyGui gui;
    PGraphics pg;
    PGraphics pgMask;

    private static float easeInOutQuint(float x) {
        x = max(x, 0f);
        x = min(x, 1f);
        return x < 0.5f ? 16f * x * x * x * x * x : 1f - (float) Math.pow(-2f * x + 2f, 5f) / 2f;

    }

    private static final class Arrow {
        PVector pos;
        PVector size;
        float t;
        float rowIndex;

        Arrow(PVector p, PVector s) {
            pos = p;
            size = s;
        }
    }

    ArrayList<Arrow> arrows = new ArrayList<>();

    public static void main(String[] args) {
        PApplet.main(java.lang.invoke.MethodHandles.lookup().lookupClass());
    }

    @Override
    public void settings() {
        fullScreen(P3D);
    }

    @Override
    public void setup() {
        gui = new LazyGui(this);
        colorMode(HSB, 1, 1, 1, 1);
        pg = createGraphics(width, height, P3D);
        pg.camera(500, 500, 500, 0, 0, 0, 0, 0, 1);
        pgMask = createGraphics(width, height, P2D);
        pg.beginDraw();
        pg.colorMode(HSB, 1, 1, 1, 1);
        pg.endDraw();
        frameRate(144);
        generateArrows(5);
    }
    int rowCount;
    private void generateArrows(int _rowCount) {
        rowCount = _rowCount;
        arrows.clear();
        final int cols = 21;
        final float WIDTH = width / (float) (cols) / 2f;
        for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
            float rowNorm = norm(rowIndex, 0, rowCount - 1);
            float margin = 200;
            float y = map(rowNorm, 0, 1, margin*0.5f, height - margin*0.5f);

            for (int i = 0; i <= cols; i++) {
                float offset = 2 * WIDTH * i + WIDTH / 2;
                PVector pos = new PVector(offset, y);
                PVector size = new PVector(WIDTH, 100);
                Arrow arr = new Arrow(pos, size);

                arr.rowIndex = rowIndex;
                arrows.add(arr);
            }
        }
    }

    private void pyramid(PGraphics pg, float n){
        pg.beginShape();
        pg.vertex(-n, -n, -n);
        pg.vertex( n, -n, -n);
        pg.vertex(   0,    0,  n);

        pg.vertex( n, -n, -n);
        pg.vertex( n,  n, -n);
        pg.vertex(   0,    0,  n);

        pg.vertex( n, n, -n);
        pg.vertex(-n, n, -n);
        pg.vertex(   0,   0,  n);

        pg.vertex(-n,  n, -n);
        pg.vertex(-n, -n, -n);
        pg.vertex(   0,    0,  n);
        pg.endShape();
    }

    private void arrow(PGraphics pg, float slope, float width, float height) {
        pg.beginShape();
        pg.vertex(-width / 2f - slope, -height / 2);
        pg.vertex(width / 2f - slope, -height / 2);
        pg.vertex(width / 2f, 0);
        pg.vertex(width / 2f - slope, height / 2);
        pg.vertex(-width / 2f - slope, height / 2);
        pg.vertex(-width / 2f, 0);
        pg.endShape();
    }

    @Override
    public void draw() {
        float t = gui.slider("t");
        gui.sliderSet("t", t + radians(gui.slider("t ++", 1)));

        float curve_id;
        pgMask.beginDraw();
        {
            float repd = 3;
            float rept = (t) % repd;
            float anim_a = 2.4f; // 7.0f;
            float anim_b = 3; // 10.0f;
            float ta = (rept - anim_a) / (anim_b - anim_a);
            curve_id = floor(t/repd);
            float curve_a = easeInOutQuint(ta);

            pgMask.background(0);
            pgMask.noStroke();
            float circum_sz = curve_a;
            if(curve_id % 2.0f < 0.5f){
                circum_sz = 1-circum_sz;
            }
            circum_sz *= gui.slider("invert_rad", 200, 0, height / 2f);
            circum_sz *= 10.0f;

            pgMask.circle(width / 2f, height / 2f, circum_sz);
        }
        pgMask.endDraw();

        pg.beginDraw();
        drawBackground();

        pg.fill(1);
        int rowCount = gui.sliderInt("count");
        if (gui.button("regen arrows")) {
            generateArrows(rowCount);
        }
        pg.pushMatrix();

        float i = 0f;
        for (Arrow a : arrows) {
            i += 1f;
            float repd = 2.0f;
            float rept = (t + (float) a.rowIndex + (i * 215.5f) % 1f) % repd;
            float anim_a = 1.0f;
            float anim_b = 1.5f;
            float anim_c = 2.0f;
            float ta = (rept - anim_a) / (anim_b - anim_a);
            float tb = (rept - anim_b) / (anim_c - anim_b);

            float curve_a = easeInOutQuint(ta) - easeInOutQuint(tb);
            curve_a = curve_a * 0.5f + 0.5f;

            float rot_n_slide_anim;
            float rot_n_slide_floor;
            {
                float rot_n_slide_anim_len = 2.f;
                float rot_n_slide_anim_in = (t + 1.0f * a.rowIndex);
                rot_n_slide_floor = floor(rot_n_slide_anim_in / rot_n_slide_anim_len);
                rot_n_slide_anim = easeInOutQuint(rot_n_slide_anim_in % rot_n_slide_anim_len);
            }
            float push_anim_anim;
            float push_anim_idx;
            float push_anim_dir;
            {
                float push_anim_len = 5f;
                float push_anim_t = t % push_anim_len;
                push_anim_idx = floor(t/push_anim_len) % 2f;
                float push_anim_key_a = 2.5f;
                float push_anim_key_b = 5f;

                float push_anim_driver = (push_anim_t - push_anim_key_a) / (push_anim_key_b - push_anim_key_a);
                push_anim_anim =  easeInOutQuint(push_anim_driver);

                push_anim_dir = push_anim_idx > 0 ? 1f : -1f;
            }


            float arrw = curve_a * a.size.x;
            arrw = 50f;

            float arrh = curve_a * a.size.y;
            pg.pushMatrix();
            pg.translate(a.pos.x, a.pos.y);
            pg.rotate((rot_n_slide_anim + rot_n_slide_floor)*PI);
            arrow(pg, a.size.x / 2f, arrw, arrh);
//            pyramid(pg, a.size.x);
            pg.popMatrix();

            float slide_dir = ((rot_n_slide_floor % 4.0f) < 2.0f) ? 1.0f : -1.0f;
            //                pg.translate(-a.pos.x, -a.pos.y);

            float speed = gui.slider("tx", 1) * slide_dir;
            speed += gui.slider("ex") * rot_n_slide_anim * slide_dir;
            int curve_mode = (int)(curve_id % 2f);
            if (curve_mode == 0) {
                a.pos.x += speed;
                if (slide_dir < 0 && a.pos.x < -a.size.x) {
                    a.pos.x = width + a.size.x;
                } else if (slide_dir > 0 && a.pos.x > width + a.size.x) {
                    a.pos.x = -a.size.x;
                }
            } else {
                float arr_row_idx = a.rowIndex;
                float row_cnt = (float)rowCount;
                if(arr_row_idx/row_cnt < 0.5f){
                    push_anim_dir *= -1f;
                }
                if(push_anim_anim > 0.00001f){
                    a.pos.y += push_anim_dir * push_anim_anim * 3f;
                }
                if (slide_dir < 0 && a.pos.y < -a.size.y) {
                    a.pos.y = height + a.size.y;
                } else if (slide_dir > 0 && a.pos.y > height + a.size.y) {
                    a.pos.y = -a.size.y;
                }
            }
        }

        pg.popMatrix();

        pg.endDraw();
        gui.pushFolder("shader");
        String shaderPath = gui.text("path", "collab_invert_filter.glsl");
        ShaderReloader.getShader(shaderPath).set("tex0", pg);
        ShaderReloader.getShader(shaderPath).set("tex1", pgMask);
        ShaderReloader.getShader(shaderPath).set("time", t);
        ShaderReloader.filter(shaderPath, g);
        gui.popFolder();

//        image(pg, 0, 0);
    }

    private void drawBackground() {
        pg.fill(gui.colorPicker("background").hex);
        pg.noStroke();
        pg.rectMode(CORNER);
        pg.rect(0, 0, width, height);
    }
}

