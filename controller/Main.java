package controller;

import model.Position;
import model.functions.ImageLoader;

import java.awt.*;
import java.util.Arrays;

public class Main {
    public static void main(String... args) {
        ImageLoader image = new ImageLoader();
        image.loadImage("86016.jpg");

        Color[][] pixels = image.getPixels();

        for(Color[] i : pixels) {
            for(Color j : i) {
                System.out.println(j);
            }
            System.out.println();
        }
    }
}
