package _22_03;

import ch.bildspur.postfx.builder.PostFX;
import ch.bildspur.postfx.builder.PostFXBuilder;
import processing.core.PApplet;
import processing.core.PGraphics;
import com.krab.lazy.LazyGui;

public class PostFxAdapter {
    static PostFX fx;

    public static void apply(PApplet sketch, LazyGui gui, PGraphics pg){
        if(fx == null){
            fx = new PostFX(sketch);
        }

        if (gui.toggle("fx/enabled")) {
            PostFXBuilder fxBuilder = fx.render(pg);
            if (gui.toggle("fx/blur/enabled")) {
                fxBuilder.blur(gui.sliderInt("fx/blur/size"), gui.slider("fx/blur/sigma"));
            }
            if (gui.toggle("fx/noise/enabled")) {
                fxBuilder.noise(gui.slider("fx/noise/amount"),
                        gui.slider("fx/noise/speed"));
            }
            if (gui.toggle("fx/contrast/enabled")) {
                fxBuilder.brightnessContrast(gui.slider("fx/contrast/brightness"),
                        gui.slider("fx/contrast/contrast", 1));
            }
            if(gui.toggle("fx/denoise/enabled")){
                fxBuilder.denoise(gui.slider("fx/denoise/exponent"));
            }
            if(gui.toggle("fx/grayscale/enabled")){
                fxBuilder.grayScale();
            }
            if(gui.toggle("fx/invert/enabled")){
                fxBuilder.invert();
            }
            if (gui.toggle("fx/bright pass/enabled")) {
                fxBuilder.brightPass(gui.slider("fx/bright pass/threshold"));
            }
            if (gui.toggle("fx/bloom/enabled")) {
                fxBuilder.bloom(gui.slider("fx/bloom/threshold"),
                        gui.sliderInt("fx/bloom/blur size"),
                        gui.slider("fx/bloom/sigma"));
            }
            if (gui.toggle("fx/rgb split/enabled")) {
                fxBuilder.rgbSplit(gui.slider("fx/rgb split/delta"));
            }
            if (gui.toggle("fx/chromab/enabled")) {
                fxBuilder.chromaticAberration();
            }
            fxBuilder.compose(pg);
        }
    }
}
