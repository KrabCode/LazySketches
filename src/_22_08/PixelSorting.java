package _22_08;

import _0_utils.Utils;
import processing.core.PApplet;
import processing.core.PGraphics;
import lazy.LazyGui;
import processing.core.PImage;

import java.util.ArrayList;
import java.util.Arrays;

// Adapted for LazyGui
// All credit for the effect goes to the original creator kimasendorf
// https://github.com/kimasendorf/ASDFPixelSort/blob/master/ASDFPixelSort.pde

public class PixelSorting extends PApplet {
    private LazyGui gui;
    private PGraphics pg;
    private PImage sourceImage;
    String sketchInstanceId = Utils.generateRandomShortId();

    private int mode = 0;
    private int whiteValue = -12345678;
    private int blackValue = -3456789;
    private float brightValue = 127;
    private float darkValue = 223;

    private int recStarted = -1;
    private int saveIndex = 1;
    private int recLength = 0;

    public static void main(String[] args) {
        PApplet.main(java.lang.invoke.MethodHandles.lookup().lookupClass());
    }

    // TODO use a saved image series from your other sketches as input!
    // TODO try changing sort direction

    @Override
    public void settings() {
        size(1000,1000, P2D);
//        fullScreen(P2D);
    }

    @Override
    public void setup() {
        gui = new LazyGui(this);
        pg = createGraphics(width, height, P2D);
//        sourceImage = loadImage("_22_08/rocks.jpg");
        sourceImage = loadImage("C:\\Users\\Krab\\Downloads\\kai-cheng-wce-mhX7BuQ-unsplash (1).jpg");
        colorMode(RGB, 255,255,255, 1);
    }

    @Override
    public void draw() {
        pg.beginDraw();
        pg.translate(pg.width / 2f, pg.height / 2f);
        pg.tint(gui.colorPicker("image/tint", color(255)).hex);
        pg.scale(gui.slider("image/scale", 1));
        pg.imageMode(CENTER);
        pg.image(sourceImage, 0, 0);
        selectMode();
        int sortEveryNthFrame = gui.sliderInt("pixel sort/sort every n frames", 1,1, Integer.MAX_VALUE);
        if(frameCount % sortEveryNthFrame == 0){
            applyPixelSortEffect();
        }
        pg.endDraw();
        clear();
        image(pg, 0, 0);
        record();
    }

    float easeInOutCubic(float x){
        return x < 0.5 ? 4 * x * x * x : 1 - pow(-2 * x + 2, 3) / 2;
    }

    private void record() {
        recLength = gui.sliderInt("rec/frames", 600);
        if(gui.button("rec/start")){
            recStarted = frameCount;
        }
        if(gui.button("rec/stop")){
            sketchInstanceId = Utils.generateRandomShortId();
            recStarted = -1;
        }
        int recordRectPosX = gui.sliderInt("rec/rect pos x");
        int recordRectPosY = gui.sliderInt("rec/rect pos y");
        int recordRectSizeX = gui.sliderInt("rec/rect size x", width);
        int recordRectSizeY = gui.sliderInt("rec/rect size y", height);
        if(recStarted != -1 && frameCount < recStarted + recLength){
            println("rec " + saveIndex + " / " + recLength);
            PImage cutout = pg.get(recordRectPosX, recordRectPosY, recordRectSizeX, recordRectSizeY);
            cutout.save("out/recorded images/PixelSorting_" + sketchInstanceId + "/" + saveIndex++ + ".png");
        }
        if(gui.toggle("rec/show rect")){
            stroke(255);
            noFill();
            rect(recordRectPosX, recordRectPosY, recordRectSizeX, recordRectSizeY);
        }
    }

    private void selectMode() {
        ArrayList<String> modeOptions = new ArrayList<>(Arrays.asList("white", "black", "bright", "dark"));
        mode = modeOptions.indexOf(gui.radio("pixel sort/mode", modeOptions));
    }

    private void applyPixelSortEffect() {
        int loopCols = gui.sliderInt("pixel sort/loop columns", 1, 0, 100);
        int loopRows = gui.sliderInt("pixel sort/loop rows", 1, 0, 100);
        whiteValue = gui.colorPicker("pixel sort/thresholds/white", -12345678).hex;
        blackValue = gui.colorPicker("pixel sort/thresholds/black", -3456789).hex;
        brightValue = gui.slider("pixel sort/thresholds/bright value", 127, 0, 256);
        darkValue  = gui.slider("pixel sort/thresholds/dark value", 223, 0, 256);
        float recNorm = norm(frameCount, recStarted, recStarted+recLength);
        recNorm = 1-abs(1 - recNorm * 2);
        if(recStarted != -1){
            darkValue = 255 * easeInOutCubic(recNorm);
        }
        int column = 0;
        int row = 0;
        for (int i = 0; i < max(loopCols, loopRows); i++) {
            while (column < pg.width-1 && i < loopCols) {
                pg.loadPixels();
                sortColumn(column);
                column++;
                pg.updatePixels();
            }
            while (row < pg.height-1 && i <loopRows) {
                pg.loadPixels();
                sortRow(row);
                row++;
                pg.updatePixels();
            }
        }
    }

    void sortRow(int y) {
        // where to start sorting
        int x = 0;
        // where to stop sorting
        int xEnd = 0;
        while (xEnd < pg.width - 1) {
            switch (mode) {
                case 0:
                    x = getFirstNonWhiteX(x, y);
                    xEnd = getNextWhiteX(x, y);
                    break;
                case 1:
                    x = getFirstNonBlackX(x, y);
                    xEnd = getNextBlackX(x, y);
                    break;
                case 2:
                    x = getFirstNonBrightX(x, y);
                    xEnd = getNextBrightX(x, y);
                    break;
                case 3:
                    x = getFirstNonDarkX(x, y);
                    xEnd = getNextDarkX(x, y);
                    break;
                default:
                    break;
            }
            if (x < 0) break;
            int sortingLength = xEnd-x;
            int[] unsorted = new int[sortingLength];
            int[] sorted;
            for (int i = 0; i < sortingLength; i++) {
                unsorted[i] = pg.pixels[x + i + y * pg.width];
            }
            sorted = sort(unsorted);
            for (int i = 0; i < sortingLength; i++) {
                pg.pixels[x + i + y * pg.width] = sorted[i];
            }
            x = xEnd+1;
        }
    }


    void sortColumn(int x) {

        // where to start sorting
        int y = 0;

        // where to stop sorting
        int yEnd = 0;

        while (yEnd < pg.height - 1) {
            switch (mode) {
                case 0:
                    y = getFirstNonWhiteY(x, y);
                    yEnd = getNextWhiteY(x, y);
                    break;
                case 1:
                    y = getFirstNonBlackY(x, y);
                    yEnd = getNextBlackY(x, y);
                    break;
                case 2:
                    y = getFirstNonBrightY(x, y);
                    yEnd = getNextBrightY(x, y);
                    break;
                case 3:
                    y = getFirstNonDarkY(x, y);
                    yEnd = getNextDarkY(x, y);
                    break;
                default:
                    break;
            }

            if (y < 0) break;

            int sortingLength = yEnd-y;

            int[] unsorted = new int[sortingLength];
            int[] sorted;

            for (int i = 0; i < sortingLength; i++) {
                unsorted[i] = pg.pixels[x + (y+i) * pg.width];
            }

            sorted = sort(unsorted);

            for (int i = 0; i < sortingLength; i++) {
                pg.pixels[x + (y+i) * pg.width] = sorted[i];
            }

            y = yEnd+1;
        }
    }


    // white x
    int getFirstNonWhiteX(int x, int y) {
        while (pg.pixels[x + y * pg.width] < whiteValue) {
            x++;
            if (x >= pg.width) return -1;
        }
        return x;
    }

    int getNextWhiteX(int x, int y) {
        x++;
        while (pg.pixels[x + y * pg.width] > whiteValue) {
            x++;
            if (x >= pg.width) return pg.width-1;
        }
        return x-1;
    }

    // black x
    int getFirstNonBlackX(int x, int y) {
        while (pg.pixels[x + y * pg.width] > blackValue) {
            x++;
            if (x >= pg.width) return -1;
        }
        return x;
    }

    int getNextBlackX(int x, int y) {
        x++;
        while (pg.pixels[x + y * pg.width] < blackValue) {
            x++;
            if (x >= pg.width) return pg.width-1;
        }
        return x-1;
    }

    // bright x
    int getFirstNonBrightX(int x, int y) {
        while (brightness(pg.pixels[x + y * pg.width]) < brightValue) {
            x++;
            if (x >= pg.width) return -1;
        }
        return x;
    }

    int getNextBrightX(int x, int y) {
        x++;
        while (brightness(pg.pixels[x + y * pg.width]) > brightValue) {
            x++;
            if (x >= pg.width) return pg.width-1;
        }
        return x-1;
    }

    // dark x
    int getFirstNonDarkX(int x, int y) {
        while (brightness(pg.pixels[x + y * pg.width]) > darkValue) {
            x++;
            if (x >= pg.width) return -1;
        }
        return x;
    }

    int getNextDarkX(int x, int y) {
        x++;
        while (brightness(pg.pixels[x + y * pg.width]) < darkValue) {
            x++;
            if (x >= pg.width) return pg.width-1;
        }
        return x-1;
    }

    // white y
    int getFirstNonWhiteY(int x, int y) {
        if (y < pg.height) {
            while (pg.pixels[x + y * pg.width] < whiteValue) {
                y++;
                if (y >= pg.height) return -1;
            }
        }
        return y;
    }

    int getNextWhiteY(int x, int y) {
        y++;
        if (y < pg.height) {
            while (pg.pixels[x + y * pg.width] > whiteValue) {
                y++;
                if (y >= pg.height) return pg.height-1;
            }
        }
        return y-1;
    }


    // black y
    int getFirstNonBlackY(int x, int y) {
        if (y < pg.height) {
            while (pg.pixels[x + y * pg.width] > blackValue) {
                y++;
                if (y >= pg.height) return -1;
            }
        }
        return y;
    }

    int getNextBlackY(int x, int y) {
        y++;
        if (y < pg.height) {
            while (pg.pixels[x + y * pg.width] < blackValue) {
                y++;
                if (y >= pg.height) return pg.height-1;
            }
        }
        return y-1;
    }

    // bright y
    int getFirstNonBrightY(int x, int y) {
        if (y < pg.height) {
            while (brightness(pg.pixels[x + y * pg.width]) < brightValue) {
                y++;
                if (y >= pg.height) return -1;
            }
        }
        return y;
    }

    int getNextBrightY(int x, int y) {
        y++;
        if (y < pg.height) {
            while (brightness(pg.pixels[x + y * pg.width]) > brightValue) {
                y++;
                if (y >= pg.height) return pg.height-1;
            }
        }
        return y-1;
    }

    // dark y
    int getFirstNonDarkY(int x, int y) {
        if (y < pg.height) {
            while (brightness(pg.pixels[x + y * pg.width]) > darkValue) {
                y++;
                if (y >= pg.height) return -1;
            }
        }
        return y;
    }

    int getNextDarkY(int x, int y) {
        y++;
        if (y < pg.height) {
            while (brightness(pg.pixels[x + y * pg.width]) < darkValue) {
                y++;
                if (y >= pg.height) return pg.height-1;
            }
        }
        return y-1;
    }
}

