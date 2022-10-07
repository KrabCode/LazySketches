package _22_09;

import lazy.LazyGui;
import lazy.Utils;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.video.Movie;

import java.util.ArrayList;
import java.util.Arrays;

@SuppressWarnings("DuplicatedCode")
public class VideoFeedback extends PApplet {
    LazyGui gui;
    PGraphics pg;
    Movie movie;

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

    @Override
    public void settings() {
        size(1920, 1080, P2D);

    }

    @Override
    public void setup() {
        gui = new LazyGui(this);
        pg = createGraphics(width, height, P2D);
        movie = new Movie(this, "C:\\Users\\Krab\\Documents\\GitHub\\LazySketches\\resources_big\\videos\\sunset_tree.mp4");
        movie.loop();
    }

    @Override
    public void draw() {
        pg.beginDraw();
        drawBackground();
        movieControl();
        pg.tint(gui.colorPicker("movie/tint", color(255, 100)).hex);
        pg.imageMode(CENTER);
        pg.translate(gui.slider("movie/x", movie.width / 2f), gui.slider("movie/y", movie.height / 2f));
        pg.scale(gui.slider("movie/scale", 1));
        pg.image(movie,
                0,0,
                gui.slider("movie/width", movie.width), gui.slider("movie/height", movie.height)
        );
        selectMode();
        int sortEveryNthFrame = gui.sliderInt("pixel sort/sort every n frames", 1,1, Integer.MAX_VALUE);
        if(frameCount % sortEveryNthFrame == 0){
            applyPixelSortEffect();
        }
        pg.endDraw();
        image(pg, 0, 0);
        record();
        gui.draw();
    }

    private void movieControl() {
        if(gui.button("movie/play")){
            movie.play();
        }
        if(gui.button("movie/loop")){
            movie.loop();
        }
        if(gui.button("movie/stop")){
            movie.stop();
        }
    }

    public void movieEvent(Movie m) {
        m.read();
    }

    private void drawBackground() {
        pg.fill(gui.colorPicker("background").hex);
        pg.noStroke();
        pg.rectMode(CORNER);
        pg.rect(0, 0, width, height);
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
        mode = modeOptions.indexOf(gui.stringPicker("pixel sort/mode", modeOptions));
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

    float easeInOutCubic(float x){
        return x < 0.5 ? 4 * x * x * x : 1 - pow(-2 * x + 2, 3) / 2;
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

