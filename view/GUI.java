package view;

import controller.MOEA;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import model.functions.ImageLoader;

import java.net.URL;
import java.util.ResourceBundle;

public class GUI implements Initializable {
    @FXML
    private ImageView imageView;

    private MOEA algorithm;
    private ImageLoader image;
    private Thread calculateThread;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        image = new ImageLoader();
        Image view = image.loadImage("86016.jpg");
        imageView.setImage(view);

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
}