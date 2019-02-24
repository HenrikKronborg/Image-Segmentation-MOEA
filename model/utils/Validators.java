package model.utils;

import model.Individual;

import java.util.LinkedList;
import java.util.List;

public class Validators {

    public static String validateRank(LinkedList<LinkedList<Individual>> rankedPopulation){
        // Step 1: is there any solution in a rank that dominates another?
        for(List<Individual> rank : rankedPopulation){
            for(Individual ind : rank){
                for(Individual cmp : rank){
                    if(!ind.equals(cmp)){
                        if(!(cmp.getFitnessConnectivity() == ind.getFitnessConnectivity() && cmp.getFitnessDeviation() == cmp.getFitnessDeviation())){
                            if(cmp.getFitnessConnectivity() <= ind.getFitnessConnectivity()){
                                if(cmp.getFitnessDeviation() <= ind.getFitnessDeviation()) {
                                    return "first";
                                }
                            }
                        }
                    }
                }
            }

        }
        // Step 2: a solution has to be dominated by a solution in the previous rank.
        // Step 3: a solution can not dominate a solution in the previous rank.
        for(int i = 1; i < rankedPopulation.size(); i++){
            for(Individual ind : rankedPopulation.get(i)){
                boolean dominated = false;

                for(Individual cmp : rankedPopulation.get(i-1)){
                    if(!(cmp.getFitnessConnectivity() == ind.getFitnessConnectivity() && cmp.getFitnessDeviation() == cmp.getFitnessDeviation())){

                        if(cmp.getFitnessConnectivity() <= ind.getFitnessConnectivity() && cmp.getFitnessDeviation() <= ind.getFitnessDeviation()){
                            dominated = true;
                        }
                        if(cmp.getFitnessConnectivity() >= ind.getFitnessConnectivity() && cmp.getFitnessDeviation() >= ind.getFitnessDeviation())
                            return "second";
                    }
                }

                if(!dominated)
                    return "third";

            }

        }

        return "funket";
    }
}
