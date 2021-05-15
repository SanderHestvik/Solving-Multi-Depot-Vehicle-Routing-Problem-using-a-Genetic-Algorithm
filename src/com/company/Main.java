package com.company;


import org.math.plot.Plot2DPanel;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.IntStream;

public class Main {
    //For whole project


    //not important
    //TODO Genotype should probably be changed before fitness calc, so that the depot/truck is explicitly specified, and unused trucks can be removed
    //TODO Remove unused trucks from res file
    public static void plotScores(ArrayList<Double> averages, ArrayList<Double> bests){
        double[] x = IntStream.range(0, Main.getEndGeneration()).mapToDouble(i -> (double) i).toArray();
        double[] avg = averages.stream().mapToDouble(Double::doubleValue).toArray();
        double[] bsts = bests.stream().mapToDouble(Double::doubleValue).toArray();
        Plot2DPanel plot = new Plot2DPanel();

        // add a line plot to the PlotPanel
        plot.addLinePlot("Average", x, avg);
        plot.addLinePlot("Best", x, bsts);

        // put the PlotPanel in a JFrame, as a JPanel
        JFrame frame = new JFrame("a plot panel");
        frame.setContentPane(plot);
        frame.setVisible(true);
    }
    public static int getEndGeneration(){
        return StaticValues.endGeneration == 0 ? StaticValues.currentGeneration : StaticValues.endGeneration;
    }
    public static void main(String[] args) {
        StaticValues.initializeFromFile();
        StaticValues.startTime = System.currentTimeMillis();






        // create your PlotPanel (you can use it as a JPanel)

        ArrayList<Double> averages = new ArrayList<>();
        ArrayList<Double> bests = new ArrayList<>();


        Population population = new Population();
        StaticValues.population = population;
        System.out.println(population.individuals.get(0));
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                StaticValues.STOP = true;
                Thread.sleep(200);
                System.out.println("Shutting down ...");
                long currentTime = System.currentTimeMillis();
                while (!StaticValues.isGenerationFinished){ //Ensures that result isnt gotten during generation
                    if ((System.currentTimeMillis() - currentTime)/1000 >= 7){
                        break;
                    }
                }
                System.out.println(population.individuals.get(0));
                long totalRuntinMillis = (System.currentTimeMillis()-StaticValues.startTime);
                long totalRuntimeSeconds = totalRuntinMillis/1000;
                System.out.println(String.format("Code ran for: %d minutes and %d seconds", totalRuntimeSeconds/60, totalRuntimeSeconds%60));
                int endGeneration = Main.getEndGeneration();
                double avgGenTime = ((double)totalRuntinMillis)/endGeneration;
                System.out.println("Average time for each generation (millis): " + avgGenTime);
                System.out.println(StaticValues.timingInfo);
                Main.plotScores(averages, bests);
                StaticValues.displayIndividual(population.individuals.get(0));

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                e.printStackTrace();
            }
        }));



        for(StaticValues.currentGeneration = 0; StaticValues.currentGeneration<StaticValues.numberOfGenerations; StaticValues.incrementGeneration()){
            population.mate();
            population.selectSurvivors();
            long numUniques =  population.individuals.stream().map(Individual::getFitness).distinct().count(); //toString si more accurate but super slow
            long totalRuntimeMillis = (System.currentTimeMillis()-StaticValues.startTime);
            long totalRuntimeSeconds = totalRuntimeMillis/1000;
            if(StaticValues.currentGeneration %100 == 0) {
                System.out.println(String.format("Gen:%5d  TopFit:%9.5f  Uniques:%1d  Time:%02d:%02d", StaticValues.currentGeneration, population.individuals.get(0).getFitness(), numUniques, totalRuntimeSeconds / 60, totalRuntimeSeconds % 60));
            }
            StaticValues.bestFitness = population.individuals.get(0).getFitness();
            bests.add(population.getFittestIndividual().getFitness());
            averages.add(population.getAverageFitness());
            if(StaticValues.displayDuringRun && StaticValues.currentGeneration%1000 ==0){
                //TODO Likely not safe, should be good as long as there is some time between calls to awoid file exceptions
                Thread t = new Thread(()-> StaticValues.displayIndividual(population.individuals.get(StaticValues.populationSize-1)));

                t.start();
            }



            if(StaticValues.STOP){
                StaticValues.isGenerationFinished = true;
                break;
            }

        }
        StaticValues.isGenerationFinished = true;



    }
}
