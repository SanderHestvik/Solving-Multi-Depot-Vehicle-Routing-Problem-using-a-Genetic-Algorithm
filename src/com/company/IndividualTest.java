package com.company;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class IndividualTest {

    @BeforeAll
    static void setup(){//Equivalent to p01
        StaticValues.fileName = "p01";
        StaticValues.initializeFromFile();

    }


    @Test
    void testFitnessForProblem1WithSolution(){
        /*
        This test uses the given problem and solution for problem 1, if this passes the fitness function should be correct
         */
        //Individual individual = new Individual(new int[]{ 1,0, 0, 0, 0,   2, 0});



        int[] solution1 = new int[]{
        42, 19, 40, 41, 13,
       	0, 17, 37, 15, 33, 45, 44,
        0,
        0, 4, 18, 25,

        0, 46, 11, 32, 1, 27, 6,
        0, 14, 24, 43, 7, 23,
       	0, 47, 12,
       	0, 22, 28, 31, 26, 8, 48,

       	0, 49, 5, 38,
       	0, 10, 39, 30, 34, 9,
        0,
        0,

        0, 20, 3, 36, 35,
       	0, 21, 50, 16, 2, 29,
        0,
        0};
        Individual individual = new Individual();
        individual.chromosome.forEach(truck -> {
            truck.customers.clear();
            truck.isChanged = true;});
        AtomicInteger truckIndex = new AtomicInteger();
        Arrays.stream(solution1).forEachOrdered((i) -> {
            if(i == 0){
                truckIndex.getAndIncrement();
            }
            else{
                individual.chromosome.get(truckIndex.get()).addCustomer(StaticValues.customers.get(i-1));
            }
        });
        double solution1Score = 576.87;
        individual.evaluateTrucks();
        individual.calculateFitness();
        Assertions.assertEquals( solution1Score,individual.getFitness(), 0.01);
    }

    @Test
    void testStuff(){
        /*Individual individual = new Individual(new int[]{0, 46, 4, 0, 49, 32, 38, 3, 9, 26, 20, 25, 0, 36, 30, 0, 0, 16, 0, 0, 0, 21, 8, 27, 19, 11, 1, 43, 14, 48, 39, 10, 47, 42, 23, 50, 24, 35, 7, 34, 37, 31, 45, 17, 0, 0, 22, 0, 5, 0, 6, 0, 12, 13, 15, 29, 44, 0, 33, 28, 0, 2, 40, 41, 18});

        individual.calculateFitness();
        System.out.println(individual);
        System.out.println(individual.getFitness());*/
        Individual individual1 = new Individual();
        Individual individual2 = new Individual();
        individual1.mate(individual2);
    }
    @Test
    void crossover(){
        IntStream.range(0, 30).forEach( i -> {
            Individual i1 = new Individual();
            Individual i2 = new Individual();
            double i1Fitness = i1.fitness;
            double i2Fitness = i2.fitness;
            Individual offspring = i1.mate(i2);
            //offspring.evaluateTrucks();
            Assertions.assertEquals(i1Fitness, i1.fitness, "p1 fitness changed during crossover");
            Assertions.assertEquals(i2Fitness, i2.fitness, "p2 fitness changed during crossover");

            Assertions.assertTrue(offspring.isValid());
        });
    }
}
