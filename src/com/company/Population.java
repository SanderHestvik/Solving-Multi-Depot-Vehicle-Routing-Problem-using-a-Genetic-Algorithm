package com.company;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Population {
    Random random = new Random();
    ArrayList<Individual> individuals;
    ArrayList<Individual> matingPool = new ArrayList<>(StaticValues.populationSize);
    public Population(){
        this.individuals = new ArrayList<>(StaticValues.populationSize);
        for(int i = 0; i<StaticValues.populationSize; i++) {
            individuals.add(new Individual());
        }

        recalculateAllFitnesses();
    }
    public void selectMatingPoolRankOrder(){
        matingPool.clear();
        IntStream.range(0, StaticValues.matingPoolSize).forEach((i)->{
            matingPool.add(individuals.get(StaticValues.getRankOrderIndex(random)));
        });
    }
    public void selectMatingPoolNBest(){
        matingPool = new ArrayList<>(individuals.subList(0, StaticValues.matingPoolSize));
    }
    public void selectMatingPoolNBestAndShuffle(){
        matingPool = new ArrayList<>(individuals.subList(0, StaticValues.matingPoolSize));
        Collections.shuffle(matingPool);
    }
    public void selectMatingPoolTournament(){
        matingPool.clear();
        IntStream.range(0, StaticValues.matingPoolSize).forEach((i)->{
            matingPool.add(tournamentSelection(StaticValues.selectionPreassure));
        });
    }
    public void selectMatingPool(){
        switch (StaticValues.getMatingSelectionType()){
            case RANK_ORDER -> {selectMatingPoolRankOrder();
            break;}
            case N_BEST -> {selectMatingPoolNBest();
            break;}
            case N_BEST_SHUFFLE -> { selectMatingPoolNBestAndShuffle();
            break;}
            case TOURNAMENT -> {
                selectMatingPoolTournament(); break;
            }
        }
    }
    public void recalculateAllFitnesses(){
        individuals.forEach(Individual::calculateFitness);
    }
    public void sortIndividuals(){
        //To prevent crash from nullpointerexception, dont know why nulls are added about every 10000 generation
        individuals = (ArrayList<Individual>) individuals.stream().filter(Objects::nonNull).collect(Collectors.toList());
        individuals.sort(Comparator.comparingDouble(Individual::getFitness));
    }

    public void mate(){
        individuals.forEach(Individual::fixInvalidLoads);
        sortIndividuals();
        selectMatingPool();

        Thread t1 = new Thread(()->{
            for(int i = 0; i<StaticValues.matingPoolSize/2; i+=2){
                Individual offspring = matingPool.get(i).mate( matingPool.get(i+1));
                Individual offspring2 = matingPool.get(i+1).mate(matingPool.get(i));

                individuals.add(offspring);
                individuals.add(offspring2);

            }
        });
        Thread t2 = new Thread(()->{
            for(int i = StaticValues.matingPoolSize/2; i<StaticValues.matingPoolSize; i+=2){
                Individual offspring = matingPool.get(i).mate( matingPool.get(i+1));
                Individual offspring2 = matingPool.get(i+1).mate(matingPool.get(i));

                individuals.add(offspring);
                individuals.add(offspring2);

            }
        });
        t1.start();
        t2.start();
        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
    public  Individual tournamentBattle(Individual i1, Individual i2, double preassure){
        if (i1.fitness < i2.fitness){
            return random.nextDouble() < preassure ? i1 : i2;
        }
        if (i1.fitness > i2.fitness){
            return random.nextDouble() < preassure ? i2 : i1;
        }
        return random.nextDouble() < 0.5 ? i1 : i2;
    }
    public Individual tournament(Individual i1, Individual i2, Individual i3, Individual i4, double preassure){
        Individual r1Winner1 = tournamentBattle(i1, i2, preassure);
        Individual r1Winner2 = tournamentBattle(i3, i4, preassure);
        return tournamentBattle(r1Winner1, r1Winner2, preassure);
    }
    public Individual getRandomIndividualFromIndividuals(){
        return individuals.get(random.nextInt(individuals.size()));
    }
    public Individual tournamentSurvivorSelection(){

        Individual winner = tournamentSelection(StaticValues.survivalPressure);
        individuals.remove(winner);
        return winner;
    }
    public Individual tournamentSelection(double preassure){
        Individual i1 = getRandomIndividualFromIndividuals();
        Individual i2 = getRandomIndividualFromIndividuals();
        Individual i3 = getRandomIndividualFromIndividuals();
        Individual i4 = getRandomIndividualFromIndividuals();
        return tournament(i1, i2, i3, i4, preassure);
    }
    public double getAverageFitness(){
        return individuals.stream().mapToDouble(Individual::getFitness).sum()/individuals.size();
    }
    public Individual getFittestIndividual(){
        return individuals.get(0);
    }
    public void selectSurvivors(){
        ArrayList<Individual> newGeneration = new ArrayList<>();
        this.sortIndividuals();
        List<Individual> elites = individuals.subList(0,StaticValues.elites);
        newGeneration.addAll(elites);
        elites.clear();

        switch (StaticValues.getSurvivalMethod()){
            case N_BEST: {
                newGeneration.addAll(individuals.subList(0, StaticValues.populationSize-StaticValues.elites));
                break;
            }
            case TOURNAMENT:{
                for (int i = 0; i<StaticValues.populationSize-StaticValues.elites;i++ ){
                    newGeneration.add(tournamentSurvivorSelection());
                }
                break;
            }
        }


        individuals = newGeneration;
        this.sortIndividuals();

    }




}
