package me.bzifkin;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import com.linkedin.paldb.api.PalDB;
import com.linkedin.paldb.api.StoreWriter;

public class Main {


    public static HashMap<String, String> countryLookUp = new HashMap<>(); //read in br1, key is ISO code (e.g. AF, US, UK), country name as value.
    public static HashMap<String, ArrayList<City>> cities = new HashMap<>(); //read in br2, key is name of the city, value is list of all cites w/ that name
    public static HashMap<String, HashMap<String, String>> regionLookUp = new HashMap<>(); //read in br3, key to outer map is country name, key to inner map is region code, value is region name
    public static HashMap<String, String> stateLookUp = new HashMap<>(); //read in br4, key is state abbreviation (e.g. IL, MA, etc) value is state name

    public static void sortCitiesByPop() {

        cities.forEach((k, v) -> {

            if (v.size() > 1) {
                v.sort((city1, city2) -> city2.pop - city1.pop);
            }

        });
    }

    public static void assignRegions() {

        HashSet<String> badRegionCodes = new HashSet<>();
        cities.forEach((k, v) -> {
            if (v.size() > 1) {
                v.forEach((city)->{

                    String region = city.region;
                    if(regionLookUp.get(city.country.toUpperCase()) == null){
                        return;
                    }
                    if(regionLookUp.get(city.country.toUpperCase()).get(region) == null){
                        badRegionCodes.add(city.region + " is bad in " + city.country.toUpperCase());
                        return;
                    }

                    city.region = regionLookUp.get(city.country.toUpperCase()).get(region);

                });
            }
            else{
                String region = v.get(0).region;
                if(regionLookUp.get(v.get(0).country.toUpperCase()) == null){
                    return;
                }
                if(regionLookUp.get(v.get(0).country.toUpperCase()).get(region) == null){
                    badRegionCodes.add(v.get(0).region + " is bad in " + v.get(0).country.toUpperCase());
                    return;
                }
                v.get(0).region = regionLookUp.get(v.get(0).country.toUpperCase()).get(region);
            }

        });
    }

    public static void assignStates(){

        cities.forEach((k, v) -> {
            if (v.size() > 1) {

                v.forEach((city)->{
                    if(city.country.equalsIgnoreCase("united states")){
                        String abbrev = city.region;
                        city.region = stateLookUp.get(abbrev);
                    }

                });
            }
            else{
                if(v.get(0).country.equalsIgnoreCase("united states")){
                    String abbrev =  v.get(0).region;
                    v.get(0).region = stateLookUp.get(abbrev);
                }
            }

        });
    }

    public static boolean isUpperCase(String s)
    {
        for (int i=0; i<s.length(); i++)
        {
            if (Character.isLowerCase(s.charAt(i)))
            {
                return false;
            }
        }
        return true;
    }

    public static void main(String[] args) throws IOException {

        final long startTime = System.currentTimeMillis();
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
        HashSet<String> noCountry = new HashSet<>();
        HashSet<String> allCountry = new HashSet<>();
        try (BufferedReader br2 =
                     new BufferedReader(new InputStreamReader(Main.class.getResourceAsStream("/worldcitiespop.txt")))) {
            br2.lines().forEach(line2 -> {

                String[] tokens = line2.split(",");

                //Some countries from maxmind list may not be on above list
                //TODO find a way to get those countries in to the list
                if (countryLookUp.get(tokens[0]) == null) {
                    noCountry.add(tokens[0]);
                    return;
                }

                String cntryName = countryLookUp.get(tokens[0]);
                allCountry.add(cntryName);
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
                City city = new City(locId, cityName, tokens[3].trim(), cntryName, lat, lon, pop);
                tempCities.add(city);
                cities.put(cityName, tempCities);

            });

        }
        final String[] cntryCode = {""};
        final String[] cntryName = {""};
        try (BufferedReader br3 =
                     new BufferedReader(new InputStreamReader(Main.class.getResourceAsStream("/GEOPOLITICAL_CODES.csv")))) {
            br3.lines().forEach(line3 -> {

                String[] tokens = line3.split(",");

                if(tokens == null || tokens.length == 0) return;

                if(isUpperCase(tokens[1])){
                    cntryCode[0] = tokens[0];
                    cntryName[0] = tokens[1];
                    HashMap<String, String> temp = new HashMap<>();
                   regionLookUp.put(cntryName[0], temp);
                }

                else{
                    String code = tokens[0].replace(cntryCode[0],"");
                    regionLookUp.get(cntryName[0]).put(code, tokens[1]);
                }

            });
        }

        try (BufferedReader br4 =
                     new BufferedReader(new InputStreamReader(Main.class.getResourceAsStream("/states.txt")))) {
            br4.lines().forEach(line4 -> {

                String[] tokens = line4.split(",");
                stateLookUp.put(tokens[0], tokens[1]);
            });
        }

        sortCitiesByPop();
        assignStates();
        assignRegions();

//        StringBuilder sb = new StringBuilder();
//        for(City city : cities.get("riverside")){
//            sb.append("LocID: "+ city.locId+ " City Name: " + city.name + " Country Name: " + city.country+ " Region: " + city.region + " Pop: " +city.pop + " Lat: " + city.lat + " Long: " + city.lon + "\n");
//        }
//        System.out.println(sb.toString());

        StoreWriter writer = PalDB.createWriter(new File("/home/bzifkin/IdeaProjects/lemur-galago/cities.paldb"));
        for(Map.Entry<String, ArrayList<City>> city : cities.entrySet())
        {
            ArrayList<City> cityList = city.getValue();
            String[] newCityList = new String[cityList.size()];
            cityList.forEach(tempCity->{
                newCityList[cityList.indexOf(tempCity)] = tempCity.toTSV();
            });

            writer.put(city.getKey(), newCityList);

        }
        writer.close();
        final long endTime = System.currentTimeMillis();
        System.out.println("Total execution time: " + (endTime - startTime) );

    }
}
