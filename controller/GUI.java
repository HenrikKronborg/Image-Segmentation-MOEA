package controller;

import controller.MOEA;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import model.functions.ImageLoader;

import java.net.URL;
import java.util.ResourceBundle;

public class GUI implements Initializable {
    @FXML
    private Canvas canvas;
    //private ImageView imageView;

    private MOEA algorithm;
    private ImageLoader image;
    private Thread calculateThread;

    private GraphicsContext gc;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        gc = canvas.getGraphicsContext2D();

        image = new ImageLoader();
        Image view = image.loadImage("86016.jpg");
        //imageView.setImage(view);

        initCalculateThread();
        calculateThread.start();
    }

    private void initCalculateThread() {
        calculateThread = new Thread(new Runnable() {
            public void run() {
                algorithm = new MOEA();
                algorithm.run(image);
            }
        });
    }

    private void drawSegments() {

    }
}