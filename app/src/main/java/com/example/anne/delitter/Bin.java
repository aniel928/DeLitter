package com.example.anne.delitter;

/**
 * Created by Anne on 4/21/18.
 */

public class Bin {
    public double latitude;
    public double longitude;
    public String text;

    public Bin(double latitude, double longitude, String text){
        this.latitude = latitude;
        this.longitude = longitude;
        this.text = text;
    }

    public Bin(){

    }
}
