package model.functions;

import model.Solution;

import java.util.LinkedList;
import java.util.List;

public class Validators {

    public static String validateRank(LinkedList<LinkedList<Solution>> rankedPopulation){
        // Step 1: is there any solution in a rank that dominates another?
        for(List<Solution> rank : rankedPopulation){
            for(Solution ind : rank){
                for(Solution cmp : rank){
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
            for(Solution ind : rankedPopulation.get(i)){
                boolean dominated = false;

                for(Solution cmp : rankedPopulation.get(i-1)){
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
