package edu.umk.mariana;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ImageProcessingTask implements Runnable {
    private final BufferedImage originalImage;
    private final BufferedImage copiedImage;
    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private final float hueSlider;
    private final float saturationSlider;
    private final float brightnessSlider;

    public ImageProcessingTask(final BufferedImage originalImage,
                               final BufferedImage copiedImage,
                               final int x,
                               final int y,
                               final int width,
                               final int height,
                               final float hue,
                               final float saturation,
                               final float brightness) {
        this.copiedImage = copiedImage;
        this.originalImage = originalImage;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.hueSlider = hue;
        this.saturationSlider = saturation;
        this.brightnessSlider = brightness;
    }

    private static float clamp(final float value,
                               final float min,
                               final float max) {
        return Math.max(Math.min(value, max), min);
    }

    public void run() {
        // Perform the image processing task on the specified chunk of the image
        for (int i = x; i < x + width; i++) {
            for (int j = y; j < y + height; j++) {
                final int pixel = originalImage.getRGB(i, j);
                final int red = (pixel >> 16) & 0xff;
                final int green = (pixel >> 8) & 0xff;
                final int blue = pixel & 0xff;

                float[] hsv = Color.RGBtoHSB(red, green, blue, null);
                hsv[0] = (hueSlider / 360 + hsv[0]) % 360;
                hsv[1] = clamp(saturationSlider + hsv[1], 0, 1);
                hsv[2] = clamp(brightnessSlider + hsv[2], 0, 1);
                copiedImage.setRGB(i, j, Color.HSBtoRGB(hsv[0], hsv[1], hsv[2]));
            }
        }
    }
}
