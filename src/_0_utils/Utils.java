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
import java.util.*;

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

    public static void record(PApplet pApplet, LazyGui gui) {
        gui.sliderInt("rec/frame");
        gui.sliderSet("rec/frame", saveIndex);
        int recLengthDefault = 360;
        int recLength = gui.sliderInt("rec/length", recLengthDefault);

        boolean recordingInProgress = recStarted != -1 && pApplet.frameCount < recStarted + recLength;
        boolean recordingJustEnded = recStarted != -1 && pApplet.frameCount == recStarted + recLength;
        if (gui.button("rec/start (ctrl + k)") ||
                (Input.getCode(CONTROL).down && Input.getChar('k').pressed)) {
            recordingId = generateRandomShortId();
            recStarted = pApplet.frameCount;
            saveIndex = 1;
            gui.sliderSet("rec/frame", saveIndex);
        }
        boolean stopCommand = gui.button("rec/stop  (ctrl + l)");
        if (stopCommand ||
                (Input.getCode(CONTROL).down && Input.getChar('l').pressed)) {
            recStarted = -1;
            saveIndex = 1;
            recordingJustEnded = true;
            gui.sliderSet("rec/frame", saveIndex);
        }
        String sketchMainClassName = pApplet.getClass().getSimpleName();
        String recDir = pApplet.dataPath("video/" + sketchMainClassName + "_" + recordingId);
        String recDirAbsolute = Paths.get(recDir).toAbsolutePath().toString();
        if (gui.button("rec/open folder")) {
            Desktop desktop = Desktop.getDesktop();
            File dir = new File(pApplet.dataPath("video"));
            if (!dir.exists()) {
                //noinspection ResultOfMethodCallIgnored
                dir.mkdirs();
            }
            try {
                desktop.open(dir);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        PVector recordRectPos = PVector.add(gui.plotXY("rec/rect pos"), new PVector(pApplet.width / 2f, pApplet.height / 2f));
        PVector recordRectSize = gui.plotXY("rec/rect size", pApplet.width, pApplet.height);
        int recordRectSizeX = floor(recordRectSize.x);
        int recordRectSizeY = floor(recordRectSize.y);
        // prevent resolutions odd numbers because ffmpeg can't work with them
        if (recordRectSizeX % 2 != 0) {
            recordRectSizeX += 1;
        }
        if (recordRectSizeY % 2 != 0) {
            recordRectSizeY += 1;
        }
        String recImageFormat = ".jpg";
        if (recordingInProgress) {
            println("saved " + saveIndex + " / " + recLength);
            PImage cutout = pApplet.get(
                    floor(recordRectPos.x) - recordRectSizeX / 2,
                    floor(recordRectPos.y) - recordRectSizeY / 2,
                    recordRectSizeX,
                    recordRectSizeY
            );
            cutout.save(recDir + "/" + saveIndex++ + recImageFormat);
        }
        if (gui.toggle("rec/show rect")) {
            pApplet.pushStyle();
            pApplet.stroke(pApplet.color(0xA0FFFFFF));
            pApplet.noFill();
            pApplet.rectMode(CENTER);
            pApplet.rect(recordRectPos.x, recordRectPos.y, recordRectSizeX, recordRectSizeY);
            pApplet.popStyle();
        }

        int ffmpegFramerate = gui.sliderInt("rec/ffmpeg fps", 60, 1, Integer.MAX_VALUE);
        if (gui.toggle("rec/ffmpeg", true) && recordingJustEnded) {
            String outMovieFilename = pApplet.dataPath("video/" + sketchMainClassName + "_" + recordingId);
            String inputFormat = recDirAbsolute + "/%01d" + recImageFormat;
            String command = String.format("ffmpeg  -r %s -i %s -start_number_range 100 -an %s.mp4",
                    ffmpegFramerate, inputFormat, outMovieFilename);
            println("running ffmpeg: " + command);
            try {
                Process proc = Runtime.getRuntime().exec(command);
                new Thread(() -> {
                    Scanner sc = new Scanner(proc.getErrorStream());
                    while (sc.hasNextLine()) {
                        println(sc.nextLine());
                    }
                    println("finished recording " + outMovieFilename);
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
        pApplet.getSurface().setLocation(pApplet.displayWidth - w, 0);
        if (onTop) {
            pApplet.getSurface().setAlwaysOnTop(true);
        }
    }

    public static void shaderMove(PGraphics pg, LazyGui gui, String customPath) {
        String moveShaderPath = "_0_templates_glsl/move.glsl";
        if (customPath != null) {
            moveShaderPath = customPath;
        }
        gui.pushFolder("move");
        moveShaderTime += radians(gui.sliderInt("time speed", 1));
        PShader moveShader = ShaderReloader.getShader(moveShaderPath);
        moveShader.set("time", moveShaderTime);
        moveShader.set("timeRadius", gui.slider("time radius", 1));
        moveShader.set("baseAngle", gui.slider("base angle", 1));
        moveShader.set("angleRange", gui.slider("angle range", 8));
        moveShader.set("frequency", gui.slider("frequency", 2));
        moveShader.set("octaves", gui.sliderInt("octaves", 4));
        moveShader.set("freqMult", gui.slider("freqMult", 2.5f));
        moveShader.set("ampMult", gui.slider("ampMult", 0.5f));
        moveShader.set("strength", gui.slider("strength", 0.1f, 0, Float.MAX_VALUE));
        moveShader.set("centerForce", gui.slider("center force"));
        moveShader.set("darken", gui.slider("darken"));
        ShaderReloader.filter(moveShaderPath, pg);
        gui.popFolder();
    }

    /**
     * Hue values loop at the 1 - 0 border both in the positive and negative direction, just like two pi loops back to 0.
     *
     * @param hue value to transfer to the [0-1] range without changing apparent color value
     * @return hue in the range between 0-1
     */
    public static float hueModulo(float hue) {
        if (hue < 0.f) {
            return hue % 1f + 1f;
        } else {
            return hue % 1f;
        }
    }


    public static void updateGetFrameRateAverage(PApplet app, LazyGui gui, int frameRateTargetDefault) {
        gui.pushFolder("fps");
        int frameRateStackSize = gui.sliderInt("framesToAverage", 256);
        frameRateHistory.add(app.frameRate);
        if (frameRateHistory.size() > frameRateStackSize) {
            frameRateHistory.remove(0);
        }
        float frameRateAverage = 0;
        if (!frameRateHistory.isEmpty()) {
            float sum = 0;
            for (float n : frameRateHistory) {
                sum += n;
            }
            frameRateAverage = sum / frameRateHistory.size();
        }
        gui.sliderSet("frameRate avg", frameRateAverage);
        int frameRateTargetTemp = gui.sliderInt("frameRate target", frameRateTargetDefault);
        if (frameRateTargetTemp != frameRateTargetLastFrame) {
            app.frameRate(frameRateTargetTemp);
        }
        frameRateTargetLastFrame = frameRateTargetTemp;
        gui.popFolder();
    }


    public static void drawCustomCursor(PApplet app, LazyGui gui) {
        gui.pushFolder("custom cursor");
        if (cursorImage == null) {
            cursorImage = app.loadImage("recording_assets/cursor.png");
            println("loaded cursor", cursorImage.width, cursorImage.height);
        }
        if (gui.toggle("active")) {
            app.noCursor();
            app.pushMatrix();
            app.translate(app.mouseX, app.mouseY);
            app.scale(gui.slider("cursor scale", 1));
            PickerColor cursorClickedColor = gui.colorPicker("cursor clicked", NormColorStore.color(1));
            PickerColor cursorIdleColor = gui.colorPicker("cursor idle", NormColorStore.color(1));
            if (gui.toggle("circle")) {
                float clickCircleSize = gui.slider("circle size", 100);
                PickerColor circleColor = gui.colorPicker("circle color");
                PVector circlePos = gui.plotXY("circle pos");
                if (app.mousePressed) {
                    app.fill(circleColor.hex);
                    app.noStroke();
                    app.ellipse(circlePos.x, circlePos.y, clickCircleSize, clickCircleSize);
                }
            }

            if (app.mousePressed) {
                app.tint(cursorClickedColor.hex);
            } else {
                app.tint(cursorIdleColor.hex);
            }
            app.image(cursorImage, 0, 0);
            app.noTint();
            app.popMatrix();
        }
        if (gui.button("reset cursor")) {
            app.cursor();
        }
        gui.popFolder();
    }

    public static void setupSurface(PApplet app, PSurface surface) {
        surface.setSize(1000, app.displayHeight - 500);
        surface.setLocation(PApplet.floor(app.displayWidth - app.width - 75), 150);
        surface.setAlwaysOnTop(true);
    }

    private static final Map<String, PFont> fonts = new HashMap<>();

    public static PFont getFont(PApplet app, String name, int size) {
        size = max(1, size);
        String key = name + size;
        if (!fonts.containsKey(key)) {
            println("Loading font \"" + name, size + "\"...");
            fonts.put(key, app.createFont(name, size));
        }
        return fonts.get(key);
    }

    static OpenSimplexNoise noise = new OpenSimplexNoise();

    public static float noise(float x, float y) {
        return (float) noise.eval(x, y);
    }

    public static float noise(float x, float y, float z) {
        return (float) noise.eval(x, y, z);
    }

    public static float noise(float x, float y, float z, float w) {
        return (float) noise.eval(x, y, z, w);
    }


    /**
     * Loads a resource from the sketch's data folder.
     * Not from the global data folder, but from the data folder directly next to the main sketch java file.
     * @param where path to the resource inside {current_sketch_folder}/data
     * @return the absolute path to the resource
     */
    @SuppressWarnings("CallToPrintStackTrace")
    public static String dataPath(String where) {
        try {
            String callingClassName = new Exception().getStackTrace()[1].getClassName();
            Class<?> callingClass = Class.forName(callingClassName);
            String packageName = callingClass.getPackage().getName().replaceAll("\\.", "\\\\");
            String sketchPath = Paths.get("").toAbsolutePath().toString();
            return sketchPath + "\\src\\" + packageName + "\\data\\" + where;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
    // font() related fields
    private static PFont selectedFont;
    private static HashMap<String, Integer> xAligns;
    private static HashMap<String, Integer> yAligns;

    public static void font(PApplet p, PGraphics pg, LazyGui gui) {
        gui.pushFolder("font");
        pg.fill(gui.colorPicker("fill", pg.color(255)).hex);
        int size = gui.sliderInt("size", 64, 1, 256);
        float leading = gui.slider("leading", 64);
        if (xAligns == null || yAligns == null) {
            xAligns = new HashMap<String, Integer>();
            xAligns.put("left", LEFT);
            xAligns.put("center", CENTER);
            xAligns.put("right", RIGHT);
            yAligns = new HashMap<String, Integer>();
            yAligns.put("top", TOP);
            yAligns.put("center", CENTER);
            yAligns.put("bottom", BOTTOM);
        }
        String xAlignSelection = gui.radio("align x", xAligns.keySet().toArray(new String[0]), "center");
        String yAlignSelection = gui.radio("align y", yAligns.keySet().toArray(new String[0]), "center");
        pg.textAlign(xAligns.get(xAlignSelection), yAligns.get(yAlignSelection));
        String fontName = gui.text("font name", "Arial").trim();
        if (gui.button("list fonts")) {
            String[] fonts = PFont.list();
            for (String font : fonts) {
                println(font + "                 "); // some spaces to avoid copying newlines from the console
            }
        }
        if (selectedFont == null || gui.hasChanged("font name") || gui.hasChanged("size")) {
            selectedFont = p.createFont(fontName, size);
        }
        pg.textFont(selectedFont);
        pg.textLeading(leading);
        gui.popFolder();
    }
}
