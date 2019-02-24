package model.utils;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;
import model.Position;
import java.io.File;

public class ImageLoader {
    private Color[][] pixels;
    private static int width;
    private static int height;

    public ImageLoader() {

    }

    public Image loadImage(String name) {
        Image image = new Image(new File("./src/img/" + name).toURI().toString());

        width = (int) image.getWidth();
        height = (int) image.getHeight();

        PixelReader pixelReader = image.getPixelReader();

        // Create 2D array of image
        pixels = new Color[height][width];

        for(int i = 0; i < height; i++) {
            for(int j = 0; j < width; j++) {
                pixels[i][j] = pixelReader.getColor(j, i);
            }
        }

        return image;
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
