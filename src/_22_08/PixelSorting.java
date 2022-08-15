package _22_08;

import lazy.global.Utils;
import processing.core.PApplet;
import processing.core.PGraphics;
import lazy.LazyGui;
import processing.core.PImage;

import java.util.ArrayList;
import java.util.Arrays;

// Adapted for LazyGui by Krab based on: https://github.com/kimasendorf/ASDFPixelSort/blob/master/ASDFPixelSort.pde

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

    @Override
    public void settings() {
        size(1200, 1200, P2D);
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
        applyPixelSortEffect();
        pg.endDraw();
        clear();
        image(pg, 0, 0);
        record();
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
            cutout.save("out/recorded images/" + sketchInstanceId + "/" + saveIndex++ + ".jpg");
        }
        if(gui.toggle("rec/show rect")){
            stroke(255);
            noFill();
            rect(recordRectPosX, recordRectPosY, recordRectSizeX, recordRectSizeY);
        }
    }

    private void selectMode() {
        ArrayList<String> modeOptions = new ArrayList<>(Arrays.asList("white", "black", "bright", "dark"));
        mode = modeOptions.indexOf(gui.stringPicker("pixel sort/mode", modeOptions));
    }

    private void applyPixelSortEffect() {
        int loops = gui.sliderInt("pixel sort/loops", 1, 1, 100);
        whiteValue = gui.colorPicker("pixel sort/thresholds/white", -12345678).hex;
        blackValue = gui.colorPicker("pixel sort/thresholds/black", -3456789).hex;
        brightValue = gui.slider("pixel sort/thresholds/bright value", 127, 0, 255);
        darkValue  = gui.slider("pixel sort/thresholds/dark value", 223, 0, 255);
        int column = 0;
        int row = 0;
        for (int i = 0; i < loops; i++) {
            while (column < pg.width-1) {
                pg.loadPixels();
                sortColumn(column);
                column++;
                pg.updatePixels();
            }
            while (row < pg.height-1) {
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
        while (xEnd < pg.width-1) {
            switch (mode) {
                case 0:
                    x = getFirstNoneWhiteX(x, y);
                    xEnd = getNextWhiteX(x, y);
                    break;
                case 1:
                    x = getFirstNoneBlackX(x, y);
                    xEnd = getNextBlackX(x, y);
                    break;
                case 2:
                    x = getFirstNoneBrightX(x, y);
                    xEnd = getNextBrightX(x, y);
                    break;
                case 3:
                    x = getFirstNoneDarkX(x, y);
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

        while (yEnd < pg.height-1) {
            switch (mode) {
                case 0:
                    y = getFirstNoneWhiteY(x, y);
                    yEnd = getNextWhiteY(x, y);
                    break;
                case 1:
                    y = getFirstNoneBlackY(x, y);
                    yEnd = getNextBlackY(x, y);
                    break;
                case 2:
                    y = getFirstNoneBrightY(x, y);
                    yEnd = getNextBrightY(x, y);
                    break;
                case 3:
                    y = getFirstNoneDarkY(x, y);
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
    int getFirstNoneWhiteX(int x, int y) {
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
    int getFirstNoneBlackX(int x, int y) {
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
    int getFirstNoneBrightX(int x, int y) {
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
    int getFirstNoneDarkX(int x, int y) {
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
    int getFirstNoneWhiteY(int x, int y) {
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
    int getFirstNoneBlackY(int x, int y) {
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
    int getFirstNoneBrightY(int x, int y) {
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
    int getFirstNoneDarkY(int x, int y) {
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

