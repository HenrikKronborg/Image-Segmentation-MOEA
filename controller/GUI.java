package controller;

import javafx.animation.AnimationTimer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import model.Individual;
import model.supportNodes.ThreadNode;
import model.utils.ImageLoader;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

public class GUI implements Initializable {
    @FXML
    private Canvas canvas1;
    @FXML
    private Canvas canvas2;
    @FXML
    private Canvas canvasBlackWhite;
    @FXML
    private Label generation;
    @FXML
    private Label segments;
    @FXML
    private Label stats;
    @FXML
    private Button nextIndividual;
    @FXML
    private Button start;
    @FXML
    private Label individualNumber;
    @FXML
    private HBox individualNumberHBox;
    @FXML
    private ChoiceBox cBox;
    @FXML
    private LineChart<Number, Number> linechart;
    @FXML
    private NumberAxis yAxis;
    @FXML
    private NumberAxis xAxis;
    @FXML
    private Button switchBtn;

    XYChart.Series series = new XYChart.Series();

    private GraphicsContext gc1;
    private GraphicsContext gc2;
    private GraphicsContext gcBlackWhite;

    private GAInterface algorithm;
    private ImageLoader image;
    private Thread calculateThread;
    private ThreadNode listener;

    private LinkedList<Individual> front;
    private int frontNumber;
    Individual bestIndividual;
    Random r = new Random();

    boolean GASwitch = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Load image and draw onto canvas
        gc1 = canvas1.getGraphicsContext2D();
        gc2 = canvas2.getGraphicsContext2D();
        gcBlackWhite = canvasBlackWhite.getGraphicsContext2D();

        image = new ImageLoader();

        linechart.getData().add(series);
        yAxis.forceZeroInRangeProperty().setValue(false);
        xAxis.forceZeroInRangeProperty().setValue(false);

        initChoiceBox();
    }

    @FXML
    private void startAlgorithm() {
        Image view = SwingFXUtils.toFXImage(image.loadImage((String) cBox.getValue()), null );
        gc1.drawImage(view, 0, 0);

        // Algorithm and calculations in threads
        initListener();
        initCalculateThread();
        calculateThread.start();

        // Hide controllers
        frontNumber = 0;
        nextIndividual.setDisable(true);
        start.setDisable(true);
        cBox.setDisable(true);
        individualNumberHBox.setVisible(false);

        // Clear the segment drawings
        gc2.clearRect(0, 0, ImageLoader.getWidth(), ImageLoader.getHeight());

        // Draw border for black and white result
        gcBlackWhite.setFill(javafx.scene.paint.Color.rgb(0,0,0));
        gcBlackWhite.fillRect(0, 0, ImageLoader.getWidth(), ImageLoader.getHeight());

        gcBlackWhite.setFill(javafx.scene.paint.Color.rgb(255,255,255));
        gcBlackWhite.fillRect(1, 1, ImageLoader.getWidth()-2, ImageLoader.getHeight()-2);

        linechart.getData().clear();
        series = new XYChart.Series();
        linechart.getData().add(series);

    }

    private void initChoiceBox() {
        File folder = new File("./src/img/");
        File[] listOfFiles = folder.listFiles();

        ObservableList<String> objects = FXCollections.observableArrayList();

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                objects.add(listOfFiles[i].getName());
            }
        }
        cBox.setItems(objects.sorted());
        cBox.setValue(cBox.getItems().get(0));
    }

    private void initCalculateThread() {
        calculateThread = new Thread(new Runnable() {
            public void run() {
                if(GASwitch){
                    algorithm = new GeneticAlgorithm(image);
                }else{
                    algorithm = new MOEA(image);
                }
                algorithm.loadObservableList(listener);
                algorithm.run();
            }
        });
    }

    @FXML
    public void saveToFile() throws IOException {
        WritableImage image = new WritableImage(ImageLoader.getWidth(), ImageLoader.getHeight());
        canvasBlackWhite.snapshot(null, image);

        String filename = "GT_" + bestIndividual.getNrSegments();

        File file = new File("./src/output/" + filename + ".png");

        int i = 1;
        while(file.exists()) {
            filename = "GT_" + bestIndividual.getNrSegments() + "_" + i;
            file = new File("./src/output/" + filename + ".png");
            i++;
        }

        ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", new File("./src/output/" + filename + ".png"));
    }

    @FXML
    public void showIndividual() {
        if(!individualNumberHBox.isVisible()) {
            individualNumberHBox.setVisible(true);
        }
        LinkedList<Individual> list = new LinkedList<>();

        front.sort((Individual a, Individual b)-> a.compareCrowdTo(b));

        for(Individual i : front) {
            if(i.getCrowdingDistance() != Double.MAX_VALUE && list.size() < 5) {
                list.add(i);
            }
        }

        if(frontNumber == list.size()) {
            frontNumber = 0;
        }

        bestIndividual = list.get(frontNumber);

        // Update text
        individualNumber.setText((frontNumber + 1) + " out of " + list.size());
        segments.setText(Integer.toString(bestIndividual.getNrSegments()));

        drawSegments(bestIndividual);
        drawResult(bestIndividual);

        frontNumber++;
    }

    @FXML
    private void switchAlg(){
        if(!GASwitch){
            GASwitch = true;
            switchBtn.setText("Switch to MOEA II");
        }else{
            GASwitch = false;
            switchBtn.setText("Switch to weighted-sum");
        }
    }
    private void drawText() {
        generation.setText(Integer.toString(listener.getGeneration()));
        segments.setText(Integer.toString(bestIndividual.getNrSegments()));
    }

    private void drawSegments(Individual individual) {
        gc2.clearRect(0,0,canvas2.getWidth(),canvas2.getHeight());
        HashMap<Integer, Color> colorMap =  new HashMap<>();
        short[][] board = individual.getChromosone();

        for (int y = 0; y < board.length; y++) {
            for (int x = 0; x < board[y].length; x++) {
                int id = board[y][x];
                if(id == 0){

                    Color c = Color.BLACK;
                    gc2.setFill(c);
                }else{
                    if(colorMap.containsKey(id)) {
                        gc2.setFill(colorMap.get(id));
                    } else {
                        Color c = javafx.scene.paint.Color.rgb(r.nextInt(255), r.nextInt(255), r.nextInt(255), 0.5);
                        colorMap.put(id,c);
                        gc2.setFill(c);
                    }
                }
                gc2.fillRect(x, y, 1, 1);
            }
        }
    }

    public void drawResult(Individual individual) {

        stats.setText("Connectivity: "+Math.round(individual.getFitnessConnectivity())+", Deviation: "+Math.round(individual.getFitnessDeviation()));
        gcBlackWhite.setFill(javafx.scene.paint.Color.rgb(255,255,255));
        gcBlackWhite.fillRect(1, 1, ImageLoader.getWidth()-2, ImageLoader.getHeight()-2);

        gcBlackWhite.setFill(javafx.scene.paint.Color.rgb(0,0,0));

        short[][] shadow = individual.getChromosone();

        for(int y = 0; y < ImageLoader.getHeight(); y++) {
            for(int x = 0; x < ImageLoader.getWidth(); x++) {
                int current = shadow[y][x];
                if(x < shadow[0].length-1 && y < shadow.length-1) {
                    if (current != shadow[y][x + 1] || current != shadow[y + 1][x]) {
                        gcBlackWhite.fillRect(x, y, 1, 1);
                    }
                }
            }
        }
    }

    private void upDateChart(LinkedList<Individual> front){
        linechart.getData().clear();
        series = new XYChart.Series();
        linechart.getData().add(series);
        for(Individual i : front){
            series.getData().add(new XYChart.Data(i.getFitnessConnectivity(), i.getFitnessDeviation()));
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
                    drawResult(bestIndividual);
                    drawText();
                    upDateChart(front);

                    if(listener.getGeneration() == MOEA.maxRuns) {
                        nextIndividual.setDisable(false);
                        start.setDisable(false);
                        cBox.setDisable(false);
                    }
                }
            }
        }.start();
    }
}