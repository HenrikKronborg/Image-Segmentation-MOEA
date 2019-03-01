package controller;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class Main extends Application {
    public static void main(String[] args) {
        launch(args);

        /*
        System.out.println("\nTest Chrom:");
        //int[] gene = new int[]{1,5,1,3,0,4,2,3,9,10,14,7,8,12,15,15};

        Chromosome c = new Chromosome();
        c.generateRandomGene();
        ArrayList<Segment> segments = c.generatePhenotype();
        System.out.println("test:" + segments.size());

        FitnessCalc f = new FitnessCalc();
        f.setImageLoader(image);
        System.out.println(Arrays.toString(f.generateFitness(segments)));
        */
    }

    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/view/MainScreen.fxml"));
        primaryStage.setTitle("MOEA : NSGA-II");
        primaryStage.setScene(new Scene(root, 850, 700));

        primaryStage.show();
    }
}
