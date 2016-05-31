package me.bzifkin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import com.linkedin.paldb.api.PalDB;
import com.linkedin.paldb.api.StoreReader;


public class Main {


    public static HashMap<String, String> countryLookUp = new HashMap<>();
    public static HashMap<String, ArrayList<City>> cities = new HashMap<>();


    public static void sortPopCities() {

        cities.forEach((k, v) -> {

            if (v.size() > 1) {
                v.sort((city1, city2) -> city2.pop - city1.pop);
            }

        });
    }


    public static void main(String[] args) throws IOException {

        //read in country codes to associate ISO code to an actual country name
        try (BufferedReader br1 =
                     new BufferedReader(new InputStreamReader(Main.class.getResourceAsStream("/countryCodes")))) {
            br1.lines().forEach(line1 -> {
                String[] tokens = line1.split(";");
                if (tokens.length < 2) return;
                countryLookUp.put(tokens[1].toLowerCase(), tokens[0].toLowerCase()); //put ISO code as key, country name as value.
            });
        }

        //Keep track of previous country name and the counter to create a unique location ID for lookup/reference table
        //TODO: not sure if necessary but should be by 3NF
       final String[] prevCntryName = {""};
       final int[] counter = {0};
        try (BufferedReader br2 =
                     new BufferedReader(new InputStreamReader(Main.class.getResourceAsStream("/worldcitiespop.txt")))) {
            br2.lines().forEach(line2 -> {

                String[] tokens = line2.split(",");

                //Some countries from maxmind list may not be on above list
                //TODO find a way to get those countries in to the list
                if (countryLookUp.get(tokens[0]) == null) {
                    return;
                }

                String cntryName = countryLookUp.get(tokens[0]);
                if(!cntryName.equals(prevCntryName[0])){
                    counter[0] =0;
                    prevCntryName[0] = cntryName;
                }
                else counter[0]++;

                String cityName = tokens[1];
                ArrayList<City> tempCities = (cities.containsKey(cityName)) ? cities.get(cityName): new ArrayList<City>();
                String locId = tokens[0] + counter[0];
                int pop = (tokens[4].equals("")) ? 0 : Integer.valueOf(tokens[4]);
                double lat = Double.valueOf(tokens[5]);
                double lon = Double.valueOf(tokens[6]);
                City city = new City(locId, cityName, tokens[3], cntryName, lat, lon, pop);
                tempCities.add(city);
                cities.put(cityName, tempCities);

            });
            sortPopCities();
        }

        StringBuilder sb = new StringBuilder();
        for(City city : cities.get("paris")){
            sb.append("LocID: "+ city.locId+ " City Name: " + city.name + " Pop: " +city.pop + " Lat: " + city.lat + " Long: " + city.lon + "\n");
        }
        System.out.println(sb.toString());
    }
}
