package controller;

import javafx.animation.AnimationTimer;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import model.Individual;
import model.supportNodes.Pixel;
import model.supportNodes.Position;
import model.Segment;
import model.supportNodes.ThreadNode;
import model.utils.ImageLoader;

import java.awt.*;
import java.net.URL;
import java.util.LinkedList;
import java.util.Random;
import java.util.ResourceBundle;

public class GUI implements Initializable {
    @FXML
    private Canvas canvas1;
    @FXML
    private Canvas canvas2;
    @FXML
    private Label generation;

    private GraphicsContext gc1;
    private GraphicsContext gc2;

    private MOEA algorithm;
    private ImageLoader image;
    private Thread calculateThread;

    private ThreadNode listener;

    //private ArrayList<LinkedList<Individual>> listenerList;
    private LinkedList<Individual> front;
    Individual bestIndividual;
    int listenerListSize = 0;
    Random r = new Random();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Load image and draw onto canvas
        gc1 = canvas1.getGraphicsContext2D();
        gc2 = canvas2.getGraphicsContext2D();

        image = new ImageLoader();
        Image view = SwingFXUtils.toFXImage(image.loadImage("353013.jpg"), null );
        gc1.drawImage(view, 0, 0);

        // Algorithm and calculations in threads
        initListener();
        initCalculateThread();
        calculateThread.start();
    }

    private void initCalculateThread() {
        calculateThread = new Thread(new Runnable() {
            public void run() {
                algorithm = new MOEA(image);
                algorithm.loadObservableList(listener);
                algorithm.run();

            }
        });
    }

    private void drawSegments(Individual individual) {
        gc2.clearRect(0,0,canvas2.getWidth(),canvas2.getHeight());
        for(Segment segment : individual.getSegments()) {
            gc2.setFill(javafx.scene.paint.Color.rgb(r.nextInt(255), r.nextInt(255), r.nextInt(255), 0.5));

            for(Position position : segment.getPixels()) {
                gc2.fillRect(position.getX(), position.getY(), 1, 1);
            }
        }
    }
    private void drawText() {
        generation.setText(Integer.toString(listener.getGeneration()));
    }

    @FXML
    public void drawResult() {
        gc1.clearRect(0,0, ImageLoader.getWidth(), ImageLoader.getHeight());

        gc2.setFill(javafx.scene.paint.Color.rgb(0,0,0));
        gc2.fillRect(0, 0, ImageLoader.getWidth(), ImageLoader.getHeight());

        // Draw border
        gc2.setFill(javafx.scene.paint.Color.rgb(255,255,255));
        gc2.fillRect(1, 1, ImageLoader.getWidth()-2, ImageLoader.getHeight()-2);

        gc2.setFill(javafx.scene.paint.Color.rgb(0,0,0));

        int[][] shadow = bestIndividual.getShadow();

        for(int y = 0; y < ImageLoader.getHeight(); y++) {
            for(int x = 0; x < ImageLoader.getWidth(); x++) {
                int current = shadow[y][x];
                if(x < shadow[0].length-1 && y < shadow.length-1) {
                    if (current != shadow[y][x + 1] || current != shadow[y + 1][x]) {
                        gc2.fillRect(x, y, 1, 1);
                    }
                }
            }
        }
    }

    public void initListener(){
        listener = new ThreadNode();

        new AnimationTimer() {
            public void handle(long currentNanoTime) {
                if(listener.changed.get()) {
                    listener.changed.set(false);
                    front = listener.getOb();
                    bestIndividual = front.get(r.nextInt(front.size()));

                    drawSegments(bestIndividual);
                    drawText();
                }
            }
        }.start();
    }
}