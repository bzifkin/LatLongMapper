package me.bzifkin;

import java.util.ArrayList;

/**
 * Created by bzifkin on 5/20/16.
 */
public class City {

    String locId = "";
    String name = "";
    double lat = 0.0;
    double lon = 0.0;
    int pop = 0;
    String region;
    String country = "";

    public City (String id, String name, String region, String country, double lat,  double lon, int population ){
        this.name = name;
        this.country = country;
        this.lat = lat;
        this.lon = lon;
        this.pop = population;
        this.region = region;
        locId = id;
    }

    public String toCSV() {
        return country+ "," + region + "," + lat + "," + lon +"\n";
    }

    public String toTSV() {
        return name+ "\t" + region + "\t"+  country+  "\n";
    }

    public City(){}

    ArrayList<String> altNames = new ArrayList<String>();
}
