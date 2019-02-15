package model.functions;

import model.Position;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageLoader {
    private Color[][] pixels;

    public ImageLoader() {

    }

    public void loadImage(String name) {
        BufferedImage image = null;

        try {
            image = ImageIO.read(new File("./src/img/" + name));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Create 2D array of image
        pixels = new Color[image.getWidth()][image.getHeight()];

        for(int i = 0; i < image.getWidth(); i++) {
            for(int j = 0; j < image.getHeight(); j++) {
                pixels[i][j] = new Color(image.getRGB(i, j));
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
}
