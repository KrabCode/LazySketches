package _23_05.FlockingSim;

import processing.core.PApplet;
import processing.core.PSurface;

import static processing.core.PApplet.floor;

public class Utils {
    public static void setupSurface(PApplet app, PSurface surface) {
        surface.setSize(1000, app.displayHeight-500);
        surface.setLocation(PApplet.floor(app.displayWidth-app.width-75), 150);
    }
}
