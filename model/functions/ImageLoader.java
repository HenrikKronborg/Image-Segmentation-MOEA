package model.functions;

import model.Position;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageLoader {
    private BufferedImage image;

    public ImageLoader() {

    }

    public BufferedImage loadImage() {
        try {
            image = ImageIO.read(new File("test.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return image;
    }

    public int[] getPixelValue(Position pos) {
        return null;
    }
}
