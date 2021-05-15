package com.company;

import org.javatuples.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class Individual {
    Random random = new Random();
    ArrayList<ArrayList<Truck>> genome = new ArrayList<>(StaticValues.depotCount);

    ArrayList<Truck> chromosome = new ArrayList<>(StaticValues.depotCount*StaticValues.maxVehiclesPerDepositCount);//For easier access
    double fitness;



    public Individual() {
        for(int i =0; i<StaticValues.depotCount; i++){
            genome.add(new ArrayList<>());
            for(int j = 0; j<StaticValues.maxVehiclesPerDepositCount; j++){
                Truck truck = new Truck(j+1, StaticValues.depots.get(i));
                genome.get(i).add(truck);
                chromosome.add(truck);
            }
        }
        for (Customer customer: StaticValues.customers ) {
            ArrayList<Truck> depot = genome.get(getClosestDepotIndex(customer));
            Truck randomTruckFromDepot = depot.get(random.nextInt(depot.size()));
            randomTruckFromDepot.addCustomer(customer);
        }
        chromosome.forEach(Truck::shuffle);
        evaluateTrucks();
        fixInvalidLoads();
        calculateFitness();
    }
    public int getClosestDepotIndex(Customer c){
        int i;
        int closestDepot = -1;
        double distanceToClosest = -1;
        for (i = 0; i<StaticValues.depotCount; i++){
            double distance = Truck.calculateDistance(StaticValues.depots.get(i), c);
            if (distanceToClosest ==-1 || distance<distanceToClosest){
                closestDepot = i;
                distanceToClosest  =distance;
            }
        }
        return closestDepot;
    }
    public Individual(Individual copySource){
        for(int i =0; i<StaticValues.depotCount; i++){
            this.genome.add(new ArrayList<>());
            for(int j = 0; j<StaticValues.maxVehiclesPerDepositCount; j++){
                Truck truck = copySource.genome.get(i).get(j).copy();
                this.genome.get(i).add(truck);
                this.chromosome.add(truck);
            }
        }
    }


    private void removeCustomers(List<Customer> customers){
        chromosome.forEach(truck -> truck.customers.removeAll(customers));
    }

    public Truck getRandomTruck(){

        return chromosome.get(random.nextInt(chromosome.size()));
    }
    public Truck getRandomNonEmptyTruck(){
        Truck t = getRandomTruck();
        while (t.customers.size() == 0){
            t = getRandomTruck();
        }
        return t;
    }

    public Truck getRandomTruckWeighted(){
        //TODO might want to use weight based on distance in a truck NavigableTree is an option
        int index = random.nextInt(StaticValues.totalCustomerCount);
        List<Integer> trucksCustCount = chromosome.stream().map(t-> t.customers.size()).collect(Collectors.toList());
        int cumSum = 0;
        for (int i = 0; i< chromosome.size(); i++){
            cumSum += trucksCustCount.get(i);
            if(cumSum>index){
                return chromosome.get(i);
            }
        }
        throw new IllegalStateException("Should have returned before this");

    }

    private boolean isTrucksValid(){
        if(StaticValues.implicitLoadConstraint){
            return true;
        }
        return chromosome.stream().allMatch(Truck::isLoadValid);
    }
    public boolean isValid(){
        if(StaticValues.implicitLoadConstraint){
            return true;
        }
        return isTrucksValid();
    }

    public void fixInvalidLoads(){
        if (StaticValues.implicitLoadConstraint){
            return;
        }
        while (!isTrucksValid()) { //TODO Might get stuck/take long time
            chromosome.stream().filter(t -> !t.isLoadValid).forEach(t -> {
                Truck randomTruck = getRandomTruck();
                randomTruck.insertCustomerRandom(t.removeRandomCustomer());
                randomTruck.evaluate();
                t.evaluate();
            });
        }
    }
    public void evaluateTrucks(){
        chromosome.forEach(Truck::evaluate);
    }

    public double getFitness() {
        return fitness;
    }


    public void calculateFitness(){
        evaluateTrucks(); //TODO For safety, should be removed later
        if(!isTrucksValid()){
            throw new IllegalStateException("Trucks need to be valid in order to calculate fitness");
        }
        fitness = chromosome.stream().mapToDouble(Truck::getFitness).sum();
    }

    /**
     *
     * @param partner
     * @return result from
     */
    public Individual withinDepotCrossover(Individual partner){
        int depotIndex = random.nextInt(StaticValues.depotCount);
        ArrayList<Truck> depot = genome.get(depotIndex);
        Truck t1 = depot.get(random.nextInt(depot.size()));

        ArrayList<Customer> customers = t1.getRandomCustomersInOrder(random.nextInt(StaticValues.maxCrossover));

        Individual offspring = new Individual(partner);
        offspring.removeCustomers(customers);
        Truck t2 = offspring.genome.get(depotIndex).get(random.nextInt(depot.size())); //TODO refers to this and not partner depot. but should not matter in this case
        t2.insertCustomersAtRandomIndexInOrder(customers);
        return offspring;
    }
    public Individual allCrossover(Individual partner){
        Truck t = getRandomTruckWeighted();
        ArrayList<Customer> customers = t.getRandomCustomersInOrder(random.nextInt(StaticValues.maxCrossover));

        Individual offspring = new Individual(partner);
        offspring.removeCustomers(customers);
        offspring.getRandomTruck().insertCustomersAtRandomIndexInOrder(customers);
        return offspring;
    }
    private Individual crossover(Individual partner){
        if(random.nextDouble() < StaticValues.noCrossoverProb){
            return new Individual(this);
        }
        switch (StaticValues.getCrossoverMethod()){
            case ALL -> {
                return allCrossover(partner);
            }
            case WITHIN_DEPOT -> {
                return withinDepotCrossover(partner);
            }
            default -> throw new IllegalStateException("Should not get here");
        }
    }
    private void swapMutation(){
        Truck t1 = getRandomNonEmptyTruck();
        Pair<Integer, Customer> c1 = t1.getRandomCustomer();
        Customer c2 = getRandomNonEmptyTruck().swapRandomCustomer(c1.getValue1());
        t1.swapCustomer(c1.getValue0(), c2);
    }
    private void insertionMutation(){
        getRandomTruck().insertCustomerRandom(getRandomNonEmptyTruck().removeRandomCustomer());
    }
    private void mutation(){

        switch (StaticValues.getMutationType()){
            case NONE -> {
            }

            case SWAP -> swapMutation();

            case INSERTION -> insertionMutation();
        }
    }

    /**
     *
     * @param partner
     * @return induvidual that satisfies load constraint and has calculated fitness
     */
    public Individual mate(Individual partner){
        Individual offspring = this.crossover(partner);
        while(StaticValues.mutationProb >= random.nextDouble()) {
            offspring.mutation();
        }

        offspring.evaluateTrucks();
        offspring.fixInvalidLoads();
        offspring.calculateFitness();
        return offspring;
    }



    @Override
    public String toString() {
        String s = String.format("%.2f",fitness);
        s += chromosome.stream().filter(t->!t.customers.isEmpty()).map(Truck::toString).reduce("", (acc, tString) -> acc + "\n" + tString);
        return s;
    }
}
