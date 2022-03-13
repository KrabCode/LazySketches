import processing.core.PApplet;
import toolbox.Gui;

public class Template extends PApplet {
    Gui gui;

    public static void main(String[] args) {
        PApplet.main(java.lang.invoke.MethodHandles.lookup().lookupClass());
    }

    @Override
    public void settings() {
        size(1080,1080,P2D);
    }

    @Override
    public void setup() {
        gui = new Gui(this);
    }

    @Override
    public void draw() {
        background(0);

        gui.draw();
        gui.record();
    }
}
