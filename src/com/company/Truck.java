package com.company;

import org.javatuples.Pair;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

public class Truck {
    int id;
    ArrayList<Customer> customers = new ArrayList<>();
    Random random = new Random(); //TODO Maybe move to static context
    private double distance;
    int load;
    boolean isLoadValid;
    private boolean isDistanceConstraintValid;
    boolean isChanged = true;
    Depot depot;
    
    public Truck(int id, Depot depot){
        this.id = id;
        this.depot = depot;
    }

    /**
      @return a new Truck with id and depot set, as well as customers from this. Fitness/load/isValid is NOT calculated
     **/
    public Truck copy(){
        Truck ret = new Truck(this.id, this.depot);
        ret.customers = new ArrayList<>(this.customers);
        return ret;
        
    }

    public boolean isLoadValid(){
        return isLoadValid;
    }

    public void addCustomer(Customer c){
        isChanged = true;
        customers.add(c);

    }
    public double getFitness(){

        int loadPenalty = (StaticValues.implicitLoadConstraint && !isLoadValid) ? (((load - Depot.maxLoadPerVehicle) * StaticValues.loadConstraintBreakPenaltyFactor) + StaticValues.loadConstraintBreakPenalty) : 0;
        return distance + loadPenalty + ( isDistanceConstraintValid ? 0: StaticValues.distanceConstraintBreakPenalty + StaticValues.distanceConstraintBreakPenaltyFactor*(distance-Depot.maxDurationPerVehicle) );
    }
    public void insertCustomer(int index, Customer c){
        isChanged = true;
        customers.add( index, c);
    }
    public Customer removeCustomer(int i){
        isChanged = true;
        return customers.remove(i);
    }

    public Customer swapCustomer(int i, Customer c){
        isChanged = true;
        return customers.set(i, c);
    }

    public Customer swapRandomCustomer(Customer c){
        isChanged = true;
        return customers.set(random.nextInt(customers.size()), c);
    }

    public void insertCustomersAtRandomIndexInOrder(List<Customer> customers){
        isChanged = true;
        int insertionPoint = random.nextInt(this.customers.size()+1);
        this.customers.addAll(insertionPoint, customers);
    }

    public Pair<Integer, Customer> getRandomCustomer(){
        int customerIndex = random.nextInt(customers.size());
        return new Pair<>(customerIndex, customers.get(customerIndex));
    }
    public Customer removeRandomCustomer(){
        isChanged = true;
        return customers.remove(random.nextInt(customers.size()));
    }
    public void insertCustomerRandom(Customer c){
        isChanged = true;
        customers.add(random.nextInt(customers.size()+1), c);
    }


    public ArrayList<Customer> getRandomCustomersInOrder(int maxCustomers){
        if (maxCustomers>= customers.size()){
            return new ArrayList<>(customers);
        }
        int startIndex = random.nextInt(customers.size()-maxCustomers+1);
        return new ArrayList<>(customers.subList(startIndex, startIndex+maxCustomers));

    }
    public void shuffle(){
        isChanged = true;
        Collections.shuffle(customers);
    }
    public static double calculateDistance(Node i, Node j){
        return Point2D.distance(i.getX(), i.getY(), j.getX(), j.getY());
    }
    public void evaluate(){
        if (!isChanged){
            return;
        }
        isChanged = false;
        //TODO use array/hashmap lookup to find pre calculated distances
        if (customers.isEmpty()){
            load = 0;
            distance = 0;
            isLoadValid = true;
            isDistanceConstraintValid = true;
            return;
        }

        distance = calculateDistance(depot, customers.get(0));
        distance += IntStream.range(0,customers.size()-1)
                .mapToDouble(i -> calculateDistance(customers.get(i), customers.get(i+1)))
                .sum();
        distance += calculateDistance(customers.get(customers.size()-1), depot);

        load = customers.stream().mapToInt(Customer::getDemand).sum();

        isLoadValid = load <= Depot.maxLoadPerVehicle;

        isDistanceConstraintValid = Depot.maxDurationPerVehicle == 0 || (distance <= Depot.maxDurationPerVehicle);
    }

    @Override
    public String toString() {
        evaluate(); //TODO just to be safe
        String customersString = customers.stream().map(c -> String.valueOf(c.id)).reduce("", (acc, c) -> acc + c + " ");
        //adding depot.id twice to match solution file from assignement
        return String.format("%-2d %-2d %-7.2f %-3d %-2d %s", depot.id, this.id, distance, load, depot.id, customersString);
    }
}
