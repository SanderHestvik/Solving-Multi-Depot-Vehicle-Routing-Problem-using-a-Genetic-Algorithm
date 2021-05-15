package com.company;

public class Depot implements Node{
    public final int id;
    public final int x, y;
    public static int truckCount;
    public static int maxDurationPerVehicle;
    public static int maxLoadPerVehicle;

    public Depot(int id, int x, int y, int truckCount, int maxDurationPerVehicle, int maxLoadPerVehicle){
        this.id = id;
        this.x = x;
        this.y = y;
        Depot.truckCount = truckCount;
        Depot.maxDurationPerVehicle = maxDurationPerVehicle;
        Depot.maxLoadPerVehicle = maxLoadPerVehicle;
    }

    @Override
    public int getX(){
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public int getDemand() {
        return 0;
    }
}
