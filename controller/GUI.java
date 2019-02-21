package controller;

import controller.MOEA;
import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import model.Position;
import model.Segment;
import model.Solution;
import model.functions.ImageLoader;

import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;
import java.util.ResourceBundle;

public class GUI implements Initializable {
    @FXML
    private Canvas canvas1;
    @FXML
    private Canvas canvas2;
    //private ImageView imageView;

    private GraphicsContext gc1;
    private GraphicsContext gc2;

    private MOEA algorithm;
    private ImageLoader image;
    private Thread calculateThread;

    private ArrayList<LinkedList<Solution>> listenerList;
    private LinkedList<Solution> front;
    Solution bestSolution;
    int listenerListSize = 0;
    Random r = new Random();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Load image and draw onto canvas
        gc1 = canvas1.getGraphicsContext2D();
        gc2 = canvas2.getGraphicsContext2D();

        image = new ImageLoader();
        Image view = image.loadImage("353013.jpg");
        gc1.drawImage(view, 0, 0);

        // Algorithm and calculations in threads
        initListener();
        initCalculateThread();
        calculateThread.start();
    }

    private void initCalculateThread() {
        calculateThread = new Thread(new Runnable() {
            public void run() {
                algorithm = new MOEA();
                algorithm.loadObservableList(listenerList);
                algorithm.run(image);
            }
        });
    }

    private void drawSegments(Solution solution) {
        gc2.clearRect(0,0,canvas2.getWidth(),canvas2.getHeight());
        for(Segment segment : solution.getSegments()) {
            gc2.setFill(new Color(Math.random(), Math.random(), Math.random(), 0.5));

            for(Position position : segment.getPixels()) {
                gc2.fillRect(position.getX(), position.getY(), 1, 1);
            }
        }
    }

    public void initListener(){
        listenerList = new ArrayList<>();

        new AnimationTimer() {
            public void handle(long currentNanoTime) {
                if(listenerList.size() > listenerListSize) {
                    listenerListSize = listenerList.size();

                    front = listenerList.get(listenerListSize-1);
                    bestSolution = front.get(r.nextInt(listenerList.size()));

                    drawSegments(bestSolution);
                }
            }
        }.start();
    }
}