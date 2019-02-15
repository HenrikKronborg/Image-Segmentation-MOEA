package model.functions;

import model.Position;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class ImageLoader {
    private Color[][] pixels;
    private static int width;
    private static int height;

    public ImageLoader() {

    }

    public void loadImage(String name) {
        BufferedImage image = null;

        try {
            image = ImageIO.read(new File("./src/img/" + name));
        } catch (IOException e) {
            e.printStackTrace();
        }

        width = image.getWidth();
        height = image.getHeight();

        // Create 2D array of image
        pixels = new Color[height][width];

        for(int i = 0; i < height; i++) {
            for(int j = 0; j < width; j++) {
                pixels[i][j] = new Color(image.getRGB(j, i));
            }
        }

    }

    public Color getPixelValue(Position pos) {
        return pixels[pos.getY()][pos.getX()];
    }

    /*
     * Getters and Setters
     */
    public Color[][] getPixels() {
        return pixels;
    }
    public static int getWidth() {
        return width;
    }
    public static int getHeight() {
        return height;
    }
}
