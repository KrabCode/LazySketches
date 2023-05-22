package _0_utils;

import com.krab.lazy.Input;
import com.krab.lazy.LazyGui;
import com.krab.lazy.PickerColor;
import com.krab.lazy.ShaderReloader;
import com.krab.lazy.stores.NormColorStore;
import processing.core.*;
import processing.opengl.PShader;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.UUID;

import static processing.core.PApplet.*;
import static processing.core.PConstants.CENTER;

public class Utils {
    private static int recStarted = -1;
    private static int saveIndex = 1;
    private static String recordingId = generateRandomShortId();
    private static float moveShaderTime = 0;
    private static int frameRateTargetLastFrame = -1;
    private static final ArrayList<Float> frameRateHistory = new ArrayList<>();

    private static PImage cursorImage;

    public static String generateRandomShortId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

    public static void record(PApplet pApplet, LazyGui gui){
        gui.sliderSet("rec/current frame", saveIndex);
        int recLength = gui.sliderInt("rec/frames total", 600);
        if (gui.button("rec/start (ctrl + k)") ||
                (Input.getCode(CONTROL).down && Input.getChar('k').pressed)) {
            recordingId = generateRandomShortId();
            recStarted = pApplet.frameCount;
            saveIndex = 1;
        }
        boolean stopCommand = gui.button("rec/stop (ctrl + l)");
        if (stopCommand ||
                (Input.getCode(CONTROL).down && Input.getChar('l').pressed)) {
            recStarted = -1;
        }
        String sketchMainClassName = pApplet.getClass().getSimpleName();
        String recDir = pApplet.dataPath("video/" + sketchMainClassName +"_" + recordingId);
        String recDirAbsolute = Paths.get(recDir).toAbsolutePath().toString();
        if(gui.button("rec/open folder")){
            Desktop desktop = Desktop.getDesktop();
            File dir = new File(recDirAbsolute + "\\");
            if(!dir.exists()){
                //noinspection ResultOfMethodCallIgnored
                dir.mkdirs();
            }
            try {
                desktop.open(dir);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        PVector recordRectPos = PVector.add(gui.plotXY("rec/rect pos"), new PVector(pApplet.width/2f, pApplet.height/2f));
        PVector recordRectSize = gui.plotXY("rec/rect size", 1000);
        int recordRectSizeX = floor(recordRectSize.x);
        int recordRectSizeY = floor(recordRectSize.y);
        if(recordRectSizeX % 2 != 0){
            recordRectSizeX += 1;
        }
        if(recordRectSizeY % 2 != 0){
            recordRectSizeY += 1;
        }
        String recImageFormat = ".jpg";
        if (recStarted != -1 && pApplet.frameCount < recStarted + recLength) {
            println("saved " + saveIndex + " / " + recLength);
            PImage cutout = pApplet.get(
                    floor(recordRectPos.x) - recordRectSizeX / 2,
                    floor(recordRectPos.y) - recordRectSizeY / 2,
                    recordRectSizeX,
                    recordRectSizeY
            );
            cutout.save( recDir + "/" + saveIndex++ + recImageFormat);
        }
        if(stopCommand || (recStarted != -1 && pApplet.frameCount == recStarted + recLength)){
            println("Recorded image series folder: " + recDirAbsolute);
        }
        if (gui.toggle("rec/show rect")) {
            pApplet.pushStyle();
            pApplet.stroke(pApplet.color(0xFFFFFFFF));
            pApplet.noFill();
            pApplet.rectMode(CENTER);
            pApplet.rect(recordRectPos.x, recordRectPos.y, recordRectSizeX, recordRectSizeY);
            pApplet.popStyle();
        }

        int ffmpegFramerate = gui.sliderInt("rec/ffmpeg fps", 60, 1, Integer.MAX_VALUE);
        if(gui.button("rec/ffmpeg make mp4")){
            String outMovieFilename = recDirAbsolute + "/_" + generateRandomShortId();
            String inputFormat = recDirAbsolute + "/%01d" + recImageFormat;
            String command = String.format("ffmpeg  -r %s -i %s -start_number_range 100000 -an %s.mp4",
                    ffmpegFramerate, inputFormat, outMovieFilename);
            println("running ffmpeg: " + command);
            try {
                Process proc = Runtime.getRuntime().exec(command);
                new Thread(() -> {
                    Scanner sc = new Scanner(proc.getErrorStream());
                    while (sc.hasNextLine()) {
                        println(sc.nextLine());
                    }
                }).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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

    public static void shaderMove(PGraphics pg, LazyGui gui, String customPath) {
        String moveShaderPath = "_0_templates_glsl/move.glsl";
        if(customPath!=null){
            moveShaderPath=customPath;
        }
        moveShaderTime += radians(gui.sliderInt("move/time speed", 1));
        PShader moveShader = ShaderReloader.getShader(moveShaderPath);
        moveShader.set("time", moveShaderTime);
        moveShader.set("timeRadius", gui.slider("move/time radius", 1));
        moveShader.set("baseAngle", gui.slider("move/base angle", 1));
        moveShader.set("angleRange", gui.slider("move/angle range", 8));
        moveShader.set("frequency", gui.slider("move/frequency", 2));
        moveShader.set("octaves", gui.sliderInt("move/octaves", 4));
        moveShader.set("freqMult", gui.slider("move/freqMult", 2.5f));
        moveShader.set("ampMult", gui.slider("move/ampMult", 0.5f));
        moveShader.set("strength", gui.slider("move/strength", 0.1f, 0, Float.MAX_VALUE));
        moveShader.set("centerForce", gui.slider("move/center force"));
        ShaderReloader.filter(moveShaderPath, pg);
    }

    /**
     * Hue values loop at the 1 - 0 border both in the positive and negative direction, just like two pi loops back to 0.
     * @param hue value to transfer to the [0-1] range without changing apparent color value
     * @return hue in the range between 0-1
     */
    public static float hueModulo(float hue){
        if (hue < 0.f){
            return hue % 1f + 1f;
        } else {
            return hue % 1f;
        }
    }


    public static void updateGetFrameRateAverage(PApplet app, LazyGui gui, int frameRateTargetDefault) {
        gui.pushFolder("fps");
        int frameRateStackSize = gui.sliderInt("framesToAverage", 256);
        frameRateHistory.add(app.frameRate);
        if(frameRateHistory.size() > frameRateStackSize) {
            frameRateHistory.remove(0);
        }
        float frameRateAverage = 0;
        if(!frameRateHistory.isEmpty()){
            float sum = 0;
            for (float n : frameRateHistory) {
                sum += n;
            }
            frameRateAverage = sum / frameRateHistory.size();
        }
        gui.sliderSet("frameRate avg", frameRateAverage);
        int frameRateTargetTemp = gui.sliderInt("frameRate target", frameRateTargetDefault);
        if(frameRateTargetTemp != frameRateTargetLastFrame){
            app.frameRate(frameRateTargetTemp);
        }
        frameRateTargetLastFrame = frameRateTargetTemp;
        gui.popFolder();
    }


    public static void drawCustomCursor(PApplet app, LazyGui gui) {
        gui.pushFolder("custom cursor");
        if(cursorImage == null){
            cursorImage = app.loadImage("recording_assets/cursor.png");
            println("loaded cursor", cursorImage.width , cursorImage.height);
        }
        if(gui.toggle("active")){
            app.noCursor();
            app.pushMatrix();
            app.translate(app.mouseX, app.mouseY);
            app.scale(gui.slider("cursor scale", 1));
            PickerColor cursorClickedColor = gui.colorPicker("cursor clicked", NormColorStore.color(1));
            PickerColor cursorIdleColor = gui.colorPicker("cursor idle", NormColorStore.color(1));
            if(gui.toggle("circle")){
                float clickCircleSize = gui.slider("circle size", 100);
                PickerColor circleColor = gui.colorPicker("circle color");
                PVector circlePos = gui.plotXY("circle pos");
                if(app.mousePressed){
                    app.fill(circleColor.hex);
                    app.noStroke();
                    app.ellipse(circlePos.x, circlePos.y, clickCircleSize, clickCircleSize);
                }
            }

            if(app.mousePressed){
                app.tint(cursorClickedColor.hex);
            }else{
                app.tint(cursorIdleColor.hex);
            }
            app.image(cursorImage, 0, 0);
            app.noTint();
            app.popMatrix();
        }
        if(gui.button("reset cursor")){
            app.cursor();
        }
        gui.popFolder();
    }

    public static void setupSurface(PApplet app, PSurface surface) {
        surface.setSize(1000, app.displayHeight-200);
        surface.setLocation(PApplet.floor(app.displayWidth-app.width-75), 150);
        surface.setAlwaysOnTop(true);
    }
}
