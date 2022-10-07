package _0_utils;

import lazy.LazyGui;
import processing.core.PApplet;
import processing.core.PImage;

import java.nio.file.Paths;

import static processing.core.PConstants.CENTER;

public class Utils {
    private static int recStarted = -1;
    private static int saveIndex = 1;
    private static String recordingId = lazy.Utils.generateRandomShortId();

    public static void record(PApplet pApplet, LazyGui gui){
        int recLength = gui.sliderInt("rec/frames", 600);
        if (gui.button("rec/start")) {
            recStarted = pApplet.frameCount;
            saveIndex = 1;
        }
        boolean stopCommand = gui.button("rec/stop");
        if (stopCommand) {
            recordingId = lazy.Utils.generateRandomShortId();
            recStarted = -1;
        }
        int recordRectPosX = pApplet.width / 2 + gui.sliderInt("rec/rect center x", 0);
        int recordRectPosY = pApplet.height / 2 + gui.sliderInt("rec/rect center y", 0);
        int recordRectSizeX = gui.sliderInt("rec/rect size x div 2", pApplet.width / 4);
        int recordRectSizeY = gui.sliderInt("rec/rect size y div 2", pApplet.height / 4);
        if(recordRectSizeX % 2 != 0){
            recordRectSizeX += 1;
        }
        if(recordRectSizeY % 2 != 0){
            recordRectSizeY += 1;
        }
        String sketchMainClassName = pApplet.getClass().getSimpleName();
        String recDir = "out/rec/" + sketchMainClassName +"_" + recordingId;
        if (recStarted != -1 && pApplet.frameCount < recStarted + recLength) {
            PApplet.println("saved " + saveIndex + " / " + recLength);
            PImage cutout = pApplet.get(
                    recordRectPosX - recordRectSizeX / 2,
                    recordRectPosY - recordRectSizeY / 2,
                    recordRectSizeX,
                    recordRectSizeY
            );
            cutout.save( recDir + "/" + saveIndex++ + ".png");
        }
        if(stopCommand || (recStarted != -1 && pApplet.frameCount == recStarted + recLength)){

            PApplet.println("Recorded image series folder: " + Paths.get(recDir).toAbsolutePath());
        }
        if (gui.toggle("rec/show rect")) {
            pApplet.pushStyle();
            pApplet.stroke(255);
            pApplet.noFill();
            pApplet.rectMode(CENTER);
            pApplet.rect(recordRectPosX, recordRectPosY, recordRectSizeX, recordRectSizeY);
            pApplet.popStyle();
        }

        // TODO call ffmpeg on last series with gui button, uuid filename, allow to choose playback framerate with slider
    }

    public static void initSurface(PApplet pApplet, boolean onTop) {
        int w = pApplet.displayWidth / 2;
        int h = pApplet.displayHeight;
        pApplet.getSurface().setSize(w, h);
        pApplet.getSurface().setLocation(pApplet.displayWidth-w, 0);
        if(onTop){
            pApplet.getSurface().setAlwaysOnTop(true);
        }
    }
}
