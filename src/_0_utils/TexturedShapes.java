package _0_utils;

import processing.core.PGraphics;
import processing.core.PImage;

import static processing.core.PConstants.NORMAL;
import static processing.core.PConstants.QUADS;


public class TexturedShapes {

    // adapted from TexturedCube(PImage tex) by amnon.owed
    // https://forum.processing.org/one/topic/box-multitextures.html
    public static void box(PGraphics pg, PImage tex, float size) {
        float n = size * 0.5f;
        pg.beginShape(QUADS);
        pg.texture(tex);
        pg.textureMode(NORMAL);

        // +Z "front" face
        pg.vertex(-n, -n, +n, 0, 0);
        pg.vertex(+n, -n, +n, 1, 0);
        pg.vertex(+n, +n, +n, 1, 1);
        pg.vertex(-n, +n, +n, 0, 1);

        // -Z "back" face
        pg.vertex(+n, -n, -n, 0, 0);
        pg.vertex(-n, -n, -n, 1, 0);
        pg.vertex(-n, +n, -n, 1, 1);
        pg.vertex(+n, +n, -n, 0, 1);

        // +Y "bottom" face
        pg.vertex(-n, +n, +n, 0, 0);
        pg.vertex(+n, +n, +n, 1, 0);
        pg.vertex(+n, +n, -n, 1, 1);
        pg.vertex(-n, +n, -n, 0, 1);

        // -Y "top" face
        pg.vertex(-n, -n, -n, 0, 0);
        pg.vertex(+n, -n, -n, 1, 0);
        pg.vertex(+n, -n, +n, 1, 1);
        pg.vertex(-n, -n, +n, 0, 1);

        // +X "right" face
        pg.vertex(+n, -n, +n, 0, 0);
        pg.vertex(+n, -n, -n, 1, 0);
        pg.vertex(+n, +n, -n, 1, 1);
        pg.vertex(+n, +n, +n, 0, 1);

        // -X "left" face
        pg.vertex(-n, -n, -n, 0, 0);
        pg.vertex(-n, -n, +n, 1, 0);
        pg.vertex(-n, +n, +n, 1, 1);
        pg.vertex(-n, +n, -n, 0, 1);

        pg.endShape();
    }
}
