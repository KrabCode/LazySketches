package _23_05.FlockingSim;

import com.krab.lazy.LazyGui;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;

import static processing.core.PApplet.lerp;

public class CameraGrid2D {
    PApplet app;
    private final PVector cameraOffset;
    private final PVector playerPos = new PVector();
    private final String guiFolderName = "camera grid";

    public CameraGrid2D(PApplet app) {
        this.app = app;
        this.cameraOffset = new PVector(app.width / 2f, app.height / 2f);
    }

    public void updateCamera(LazyGui gui, PGraphics pg, PVector playerPos) {
        gui.pushFolder(guiFolderName);
        this.playerPos.x = playerPos.x;
        this.playerPos.y = playerPos.y;
        float cameraFollowTightness = gui.slider("camera speed", 0.05f, 0, 1);
        if(app.frameCount == 1){
            cameraFollowTightness = 1; // no weird jumps on startup
        }
        cameraOffset.x = lerp(cameraOffset.x, pg.width * .5f - playerPos.x, cameraFollowTightness);
        cameraOffset.y = lerp(cameraOffset.y, pg.height * .5f - playerPos.y, cameraFollowTightness);
        pg.translate(cameraOffset.x, cameraOffset.y);
        gui.popFolder();
    }

    public void drawGridAroundPlayer(LazyGui gui, PGraphics pg) {
        gui.pushFolder(guiFolderName);
        float cellSize = gui.slider("cell size", 40, 5, 1000);
        pg.strokeWeight(gui.slider("line width", 1));
        pg.stroke(gui.colorPicker("line color").hex);
        float w = pg.width*2;
        float h = pg.height*2;
        pg.pushMatrix();
        PVector gridOffset = new PVector(playerPos.x%cellSize, playerPos.y%cellSize);
        pg.translate(playerPos.x-gridOffset.x, playerPos.y-gridOffset.y);
        for (float x = -w; x <= w; x += cellSize) {
            pg.line(x, -h, x, h);
        }
        for (float y = -h; y <= h; y += cellSize) {
            pg.line(-w, y, w, y);
        }
        pg.popMatrix();
        gui.popFolder();
    }

    public PVector screenPosToWorldPos(float x, float y) {
        return new PVector(x, y).add(-cameraOffset.x, -cameraOffset.y);
    }
}
