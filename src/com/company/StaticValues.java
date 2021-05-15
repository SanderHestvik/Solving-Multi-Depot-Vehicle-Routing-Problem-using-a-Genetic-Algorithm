package com.company;

import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

enum MutationType{
    NONE,
    SWAP,
    INSERTION,
    RANDOM //Swap or insertion
}
enum MatingSelectionType{
    RANK_ORDER,
    N_BEST,
    N_BEST_SHUFFLE,
    TOURNAMENT
}
enum SurvivalMethod{
    N_BEST,
    TOURNAMENT
}
enum CrossoverMethod{
    ALL,
    WITHIN_DEPOT
}

public class StaticValues {
    private static final Random random = new Random();
    public static int maxVehiclesPerDepositCount, totalCustomerCount, depotCount = -1;
    private static boolean hasReached30=false, hasReached20=false, hasReached10=false, hasReached05 = false;
    public static ArrayList<Depot> depots = new ArrayList<>();
    public static ArrayList<Customer> customers = new ArrayList<>();
    public static TreeMap<Double, Integer> rankOrderIndexTree;
    public static Population population;
    public static int currentGeneration;
    public static boolean STOP = false;
    public static boolean isGenerationFinished = false;
    public static double targetFitness = 0;
    public static double bestFitness;
    public static long startTime;
    public static String timingInfo = "";
    public static int endGeneration = 0;
    public static int generationsSinceImprovedBest = 0;
    public static double lastFitness;
    //TODO All parameter control should be done here
    public static String fileName = "p41";
    public static final boolean implicitLoadConstraint = true;

    public static int maxCrossover; //is set in initialization code

    public static double mutationProbBaseline = 0.3; //Has to be under 1
    public static double mutationProb = 0.3; //Has to be under 1
    public static double maxMutationProb = 0.6; //Has to be under 1
    public static double mutationChangeFactor = 1.01; //Has to be  1 or higher 1 means its off

    public static final double noCrossoverProbBaseline = 0.05;
    public static double noCrossoverProb = 0.05;
    public static double maxNoCrossoverProb = 0.2;
    public static double noCrossoverProbChangeFactor = 1.01;

    public static double survivalPressureBaseline = 0.9;
    public static double survivalPressure = 0.9; //Has to be under 1
    public static double survivalPressureMin = 0.7;
    public static double survivalPressureChangeFactor = 0.98;

    public static double selectionPreassureBaseline = 0.9;
    public static double selectionPreassure = 0.9;
    public static double selectionPreassureMin = 0.7; //Must be in range 0.5-1.0
    public static double selectionPreassureChangeFactor = 0.98;

    public static boolean displayDuringRun = false;
    public static boolean isDistanceConstraintActive = true;
    public static int turnOnDurationConstraintAtThisGeneration = 1;

    public static int distanceConstraintBreakPenalty = 100000;
    public static int distanceConstraintBreakPenaltyFactor = 1000;
    public static int loadConstraintBreakPenalty = 30000;
    public static int loadConstraintBreakPenaltyFactor = 2000;

    //public static int incrementConstraintPenaltyEvery = 300000000;
    public static int changeParametersNoImprovementInXgens = 2200;
    public static final int populationSize = 2500;
    public static final int matingPoolSize = 900; //Must be divisible by 2
    public static final int numberOfGenerations = 350000;

    private static final MutationType mutationType = MutationType.RANDOM;
    private static MatingSelectionType matingSelectionType = MatingSelectionType.TOURNAMENT;
    private static SurvivalMethod survivalMethod = SurvivalMethod.TOURNAMENT;
    //private static CrossoverMethod crossoverMethod = CrossoverMethod.WITHIN_DEPOT;
    public static int elites = 20;

    public static void decreaseSelectionPressure(){
        selectionPreassure *= selectionPreassureChangeFactor;
        selectionPreassure = Math.max(selectionPreassure, selectionPreassureMin);
    }
    public static void decreaseSurvivalPressure(){
        survivalPressure *= survivalPressureChangeFactor;
        survivalPressure = Math.max(survivalPressure, survivalPressureMin);
    }
    public static void increaseNoCrossoverProb(){
        noCrossoverProb *= noCrossoverProbChangeFactor;
        noCrossoverProb = Math.min(maxNoCrossoverProb, noCrossoverProb);
    }
    public static void increaseMutation(){

        mutationProb *= mutationChangeFactor;
        mutationProb = Math.min(mutationProb, maxMutationProb);
    }
    public static void checkIfBestImproved(){
        if(population.getFittestIndividual().getFitness() == lastFitness){
            generationsSinceImprovedBest ++;
        }
        else {
            generationsSinceImprovedBest = 0;
        }
        lastFitness = population.getFittestIndividual().getFitness();
    }
    public static void adjustParametersIf(){
        if (generationsSinceImprovedBest == 0){
            mutationProb = mutationProbBaseline;
            noCrossoverProb = noCrossoverProbBaseline;
            survivalPressure = survivalPressureBaseline;
            selectionPreassure = selectionPreassureBaseline;
        }
        else if(generationsSinceImprovedBest %changeParametersNoImprovementInXgens ==0){
            increaseMutation();
            increaseNoCrossoverProb();
            decreaseSelectionPressure();
            decreaseSurvivalPressure();
        }
    }
    public static void incrementGeneration(){
        currentGeneration++;
        checkIfBestImproved();
        adjustParametersIf();
        checkAndAppendToTimingString();
        /*if(currentGeneration%incrementConstraintPenaltyEvery == 0){
            distanceConstraintBreakPenalty+= 1000;
            population.recalculateAllFitnesses();
        }*/
        //TODO change a little before demo
        if (targetFitness != 0 && bestFitness<= targetFitness*1.048){ //Ends for loop since we have found target
            endGeneration = currentGeneration;
            currentGeneration = numberOfGenerations;

        }
        if(currentGeneration == turnOnDurationConstraintAtThisGeneration){
            isDistanceConstraintActive = true;
            population.recalculateAllFitnesses();
        }
    }
    public static MutationType getMutationType(){
        if(mutationType == MutationType.RANDOM){
            return random.nextDouble() < 0.5 ? MutationType.INSERTION : MutationType.SWAP;
        }
        return mutationType;
    }
    public static MatingSelectionType getMatingSelectionType() {
        return matingSelectionType;
    }
    

   private static void initializeLinearRankOrderIndexTree(){
        rankOrderIndexTree = new TreeMap<>();
        for (int i = 0; i<populationSize; i++){
            double keyValue = Math.pow((((double)populationSize-i)/populationSize), 2.0);
            assert keyValue>=0 && keyValue <=1;
            rankOrderIndexTree.put(keyValue, i);
        }
       System.out.println("LastKey: " + rankOrderIndexTree.lastEntry());
       System.out.println("FirstKey: " + rankOrderIndexTree.firstEntry());

   }
   public static int getRankOrderIndex(Random r){
        return rankOrderIndexTree.higherEntry(r.nextDouble()).getValue();
   }
   private static void checkAndAppendToTimingString(){
       if (targetFitness==0){
           return;
       }
       if (!hasReached30 && bestFitness<= targetFitness*1.30){
           hasReached30 = true;
           long totalRuntimeSeconds = (System.currentTimeMillis()-StaticValues.startTime)/1000;
           String s = String.format("Got to 30 percent in: %d minutes and %d seconds\n", totalRuntimeSeconds/60, totalRuntimeSeconds%60);
           timingInfo += s;
           System.out.println(s);
       }
       if (!hasReached20 && bestFitness<= targetFitness*1.20){
           hasReached20 = true;
           long totalRuntimeSeconds = (System.currentTimeMillis()-StaticValues.startTime)/1000;
           String s = String.format("Got to 20 percent in: %d minutes and %d seconds\n", totalRuntimeSeconds/60, totalRuntimeSeconds%60);
           timingInfo += s;
           System.out.println(s);
       }
       if (!hasReached10 && bestFitness<= targetFitness*1.10){
           hasReached10 = true;
           long totalRuntimeSeconds = (System.currentTimeMillis()-StaticValues.startTime)/1000;
           String s = String.format("Got to 10 percent in: %d minutes and %d seconds\n", totalRuntimeSeconds/60, totalRuntimeSeconds%60);
           timingInfo += s;
           System.out.println(s);
       }
       if (!hasReached05 && bestFitness<= targetFitness*1.05){
           hasReached05 = true;
           long totalRuntimeSeconds = (System.currentTimeMillis()-StaticValues.startTime)/1000;
           String s = String.format("Got to 5 percent in: %d minutes and %d seconds\n", totalRuntimeSeconds/60, totalRuntimeSeconds%60);
           timingInfo += s;
           System.out.println(s);
       }

   }


    public static void initializeFromFile(){
        readTaskFromFile();
        readTargetFitnessFromFile();
        initializeLinearRankOrderIndexTree();
        maxCrossover = totalCustomerCount/(depotCount*2);
    }


    private static void readTargetFitnessFromFile(){

        File file = new File("data/Solution Files/" + fileName + ".res");

        try {
            Scanner scanner = new Scanner(file);
            targetFitness = Double.parseDouble(scanner.next());

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    private static void readTaskFromFile(){

        try {
            File file = new File("data/Data Files/" + fileName);

            Scanner scanner = new Scanner(file);
            String[] firstLine = scanner.nextLine().split(" ");
            StaticValues.maxVehiclesPerDepositCount = Integer.parseInt(firstLine[0]);
            StaticValues.totalCustomerCount = Integer.parseInt(firstLine[1]);
            StaticValues.depotCount = Integer.parseInt(firstLine[2]);

            ArrayList<String[]> preDepots = new ArrayList<>(StaticValues.depotCount);
            for(int i = 0; i<StaticValues.depotCount; i++){
                String[] depotLine = scanner.nextLine().trim().split("[ ]+");
                preDepots.add(depotLine);
            }
            for(int i = 0; i < StaticValues.totalCustomerCount; i++){
                String[] customerLine = scanner.nextLine().trim().split("[ ]+");
                customers.add(new Customer(
                        Integer.parseInt(customerLine[0]),
                        Integer.parseInt( customerLine[1]),
                        Integer.parseInt(customerLine[2]),
                        Integer.parseInt(customerLine[4])
                ));
            }
            Iterator<String[]> iterator = preDepots.iterator();
            for (int i = 1; i<StaticValues.depotCount+1; i++){
                String[] depotLine = scanner.nextLine().trim().split("[ ]+");
                String[] preDepotLine = iterator.next();
                depots.add(new Depot(
                        i,
                        Integer.parseInt(depotLine[1]),
                        Integer.parseInt(depotLine[2]),
                        StaticValues.maxVehiclesPerDepositCount,
                        Integer.parseInt(preDepotLine[0]),
                        Integer.parseInt(preDepotLine[1])
                        ));
            }
        } catch (FileNotFoundException e){
            e.printStackTrace();
        }
    }

    public static void displayIndividual(@NotNull Individual individual){
        System.out.println("Customers in sol: " + individual.chromosome.stream().mapToInt(t -> t.customers.size()).sum());
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("data/Java Solution Files/"+ fileName + ".res"));
            writer.write(individual.toString());

            writer.close();

            //ipython so plt.show wont block flow
            Process process = Runtime.getRuntime().exec(new String[]{"ipython", "python/main.py" , fileName});
            InputStream stdout = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(stdout, StandardCharsets.UTF_8));
            String line;
            while((line = reader.readLine()) != null){
                System.out.println("stdout: "+ line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static SurvivalMethod getSurvivalMethod() {
        return survivalMethod;
    }

    public static CrossoverMethod getCrossoverMethod() {
        return (random.nextDouble()< 0.5)? CrossoverMethod.ALL : CrossoverMethod.WITHIN_DEPOT;
    }
}

