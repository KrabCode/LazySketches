package _22_12.flowers;

import _0_utils.Utils;
import _22_03.PostFxAdapter;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;
import com.krab.lazy.LazyGui;
import processing.core.PVector;

import java.util.ArrayList;

public class Flowers extends PApplet {
    LazyGui gui;
    PGraphics pg;
    ArrayList<PVector> posOffsets = new ArrayList<>();
    ArrayList<Float> sizeOffsets = new ArrayList<>();

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
        gui = new LazyGui(this);
        pg = createGraphics(width, height, P3D);
        colorMode(HSB, 1, 1, 1, 1);
        frameRate(144);
    }

    @Override
    public void draw() {
        pg.beginDraw();
        drawBackground();
        drawGrid();
        pg.endDraw();
        PostFxAdapter.apply(this, gui, pg);
        image(pg, 0, 0);
        gui.draw();
        Utils.record(this, gui);
    }

    float gridTime = 0;

    private void drawGrid() {
        pg.pushMatrix();
        gui.pushFolder("grid");
        if (gui.toggle("disable depth test")) {
            pg.hint(PConstants.DISABLE_DEPTH_TEST);
        }
        PVector tA = gui.plotXYZ("translation a", width / 2f, height / 2f, 0);
        PVector rA = gui.plotXYZ("rotation a");
        PVector tB = gui.plotXYZ("translation b");
        PVector rB = gui.plotXYZ("rotation b");
        pg.translate(tA.x, tA.y, tA.z);
        pg.rotateX(rA.x);
        pg.rotateY(rA.y);
        pg.rotateZ(rA.z);
        pg.translate(tB.x, tB.y, tB.z);
        pg.rotateX(rB.x);
        pg.rotateY(rB.y);
        pg.rotateZ(rB.z);
        int count = gui.sliderInt("col\\/row count", 20);
        PVector colsRows = new PVector(count, count);
        PVector gridSize = gui.plotXY("grid size", 600);
        float pointSize = gui.slider("point size", 5);
        float pointSizeMult = gui.slider("point size mult", 5);
        float timeSpeed = gui.slider("time speed", 1);
        gridTime += radians(timeSpeed);
        float waveFreq = gui.slider("wave freq", 0.1f);
        float waveAmpY = gui.slider("wave amp y", 50);
        if (gui.toggle("blend\\/add")) {
            pg.blendMode(ADD);
        } else {
            pg.blendMode(BLEND);
        }
        int columns = floor(colsRows.x);
        int rows = floor(colsRows.y);
        int offsetIndex = -1;
        for (int xi = 0; xi < columns; xi++) {
            for (int yi = 0; yi < rows; yi++) {
                if (posOffsets.size() <= ++offsetIndex) {
                    posOffsets.add(new PVector(randomGaussian(), randomGaussian()));
                }
                PVector offset = PVector.mult(posOffsets.get(offsetIndex), gui.slider("pos offset gauss", 10));
                float x = map(xi, 0, columns, -gridSize.x, gridSize.x) + offset.x;
                float y = map(yi, 0, rows, -gridSize.y, gridSize.y) + offset.y;
                float dist = dist(x + offset.x, y + offset.y, 0, 0);

                float wave = sin(waveFreq * dist + gridTime) + gui.slider("wave const", 0);;
                if (sizeOffsets.size() <= offsetIndex) {
                    sizeOffsets.add(randomGaussian());
                }
                float sizeOffsetGauss = sizeOffsets.get(offsetIndex) * gui.slider("size offset gauss", 2);
                float pointSizeLocal = pointSize * (pointSizeMult + sizeOffsetGauss) * max(0, wave);
                pg.pushMatrix();
                pg.translate(x, y);

                pg.fill(gui.colorPicker("stem fill").hex);
                pg.stroke(gui.colorPicker("stem stroke").hex);
                pg.strokeWeight(gui.slider("stem weight", 1));
                float z = waveAmpY * wave;
                float stemGirth = gui.slider("stem girth", 3);
                float stemOffsetZ = gui.slider("stem z offset");
                float stemLength = max(gui.slider("line min z"), z);
                if(stemLength > 0){
                    pg.pushMatrix();
                    pg.translate(0, 0, stemLength/2f);
                    pg.box(stemGirth, stemGirth, -stemLength);
                    pg.popMatrix();
                }
                pg.translate(0, 0, z+stemOffsetZ);

                pg.stroke(gui.colorPicker("point stroke").hex);
                pg.strokeWeight(gui.slider("point weight", 1));
                if (gui.toggle("point no stroke")) {
                    pg.noStroke();
                }
                int defaultColor = color(255);
                int[] fills = new int[]{
                        gui.colorPicker("point fill a", defaultColor).hex,
                        gui.colorPicker("point fill b", defaultColor).hex,
                        gui.colorPicker("point fill c", defaultColor).hex,
                        gui.colorPicker("point fill d", defaultColor).hex
                };
                int fillIndex = floor(xi + yi) % fills.length;
                pg.fill(fills[fillIndex]);
                pg.ellipse(0, 0, pointSizeLocal, pointSizeLocal);
                pg.popMatrix();
            }
        }
        pg.hint(PConstants.ENABLE_DEPTH_TEST);
        gui.popFolder();
        pg.popMatrix();
        pg.blendMode(BLEND);
    }

    private void drawBackground() {
        gui.pushFolder("bg");
        if (gui.toggle("blend\\/subtract")) {
            pg.blendMode(SUBTRACT);
        } else {
            pg.blendMode(BLEND);
        }
        pg.fill(gui.colorPicker("fill").hex);
        pg.noStroke();
        pg.rectMode(CORNER);
        pg.rect(0, 0, width, height);
        pg.blendMode(BLEND);
        gui.popFolder();
    }
}

