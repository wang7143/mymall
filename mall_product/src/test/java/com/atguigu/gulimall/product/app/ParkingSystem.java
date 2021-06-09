package com.atguigu.gulimall.product.app;



public class ParkingSystem {
    private int big;
    private int medium;
    private int small;

    public ParkingSystem(int big, int medium, int small) {
        this.big = big;
        this.medium = medium;
        this.small = small;
    }

    public ParkingSystem() {
    }


    public int getBig() {
        return big;
    }

    public void setBig(int big) {
        this.big = big;
    }

    public int getMedium() {
        return medium;
    }

    public void setMedium(int medium) {
        this.medium = medium;
    }

    public int getSmall() {
        return small;
    }

    public void setSmall(int small) {
        this.small = small;
    }

    public boolean addCar(int carType) {
        if (carType == 1 && this.big > 0){
            this.big--;
            return true;
        }else if (carType == 2 && this.medium > 0){
            this.medium --;
            return true;
        }else if (carType == 3 && this.small >0) {
            this.small --;
            return true;
        }else{
            return false;
        }
    }
}

