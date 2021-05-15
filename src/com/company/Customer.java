package com.company;

public class Customer implements Node{//TODO kan kanskje erstattes med array
    public final int id;
    public final int x, y;
    // Not used in this project int serviceDurationRequirement;
    public final int demand;
    public Customer(int id, int x, int y, int demand){
        this.id = id;
        this.x = x;
        this.y = y;
        this.demand = demand;
    }
    public int getX(){
        return x;
    }


    public int getY() {
        return y;
    }

    @Override
    public int getDemand() {
        return this.demand;
    }

    @Override
    public String toString() {
        return ""
                 + id;
    }
}
