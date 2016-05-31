package me.bzifkin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import com.linkedin.paldb.api.PalDB;
import com.linkedin.paldb.api.StoreReader;


public class Main {


    public static HashMap<String, String> countryLookUp = new HashMap<>();
    //public static HashMap<String, HashMap<String, List<City>>> countries = new HashMap<>();
    public static HashMap<String, ArrayList<City>> cities = new HashMap<>();

//    public static void addCitiesToCountry(String country, String[] tokens, int[] counter) {
//        String name = tokens[1];
//        String locId = tokens[0]+ counter[0];
//        int pop = (tokens[4].equals("")) ? 0 : Integer.valueOf(tokens[4]);
//        double lat = Double.valueOf(tokens[5]);
//        double lon = Double.valueOf(tokens[6]);
//        City city = new City(locId, name, tokens[3], country, lat, lon, pop);
//        if (countries.get(country).get(name) == null) {
//
//            ArrayList<City> cities = new ArrayList<>();
//            cities.add(city);
//            countries.get(country).put(name,cities);
//        } else {
//            countries.get(country).get(name).add(city);
//        }
//    }

//    public static String printCitiesForCountry(String cName){
//
//        StringBuilder sb = new StringBuilder();
//        HashMap<String, List<City>> temp = countries.get(cName);
//        temp.forEach((k, v)-> {
//
//                    if(v.size()>1){
//                        v.forEach(blah->{
//                            sb.append("LocID: "+ blah.locId+ " City Name: " + k + " Pop: " + blah.pop + " Lat: " + blah.lat + " Long: " + blah.lon + "\n");
//                        });
//                    }
//                    else
//                        sb.append("LocID: "+ v.get(0).locId+ " City Name: " + k + " Pop: " + v.get(0).pop + " Lat: " + v.get(0).lat + " Long: " + v.get(0).lon + "\n");
//                });
//
//        return sb.toString();
//    }
//
//    public static String printTopPopCities(String cName){
//        ArrayList<City> popCities = new ArrayList<City>();
//        HashMap<String, List<City>> temp = countries.get(cName);
//        temp.forEach((k,v) -> {
//
//                    if(v.size()>1){
//                        v.forEach(blah->{
//                            if(blah.pop > 0)
//                            popCities.add(blah);
//                        });
//                    }
//                    else if(v.get(0).pop> 0)
//                        popCities.add(v.get(0));
//
//                });
//
//        popCities.sort((city1,city2)-> city2.pop - city1.pop);
//        StringBuilder sb = new StringBuilder();
//       for(int i =0; i<=9; i++){
//           sb.append("LocID: "+ popCities.get(i).locId+ " City Name: " + popCities.get(i).name + " Pop: " +popCities.get(i).pop + " Lat: " + popCities.get(i).lat + " Long: " + popCities.get(i).lon + "\n");
//       }
//        return sb.toString();
//    }


    public static void main(String[] args) throws IOException {


        try (BufferedReader br1 =
                     new BufferedReader(new InputStreamReader(Main.class.getResourceAsStream("/countryCodes")))) {
            br1.lines().forEach(line1 -> {
                String[] tokens = line1.split(";");
                if (tokens.length < 2) return;
                countryLookUp.put(tokens[1].toLowerCase(), tokens[0].toLowerCase()); //put ISO code as key, country name as value.
            });
        }

       final String[] prevCntryName = {""};
       final int[] counter = {0};
        try (BufferedReader br2 =
                     new BufferedReader(new InputStreamReader(Main.class.getResourceAsStream("/worldcitiespop.txt")))) {
            br2.lines().forEach(line2 -> {

                String[] tokens = line2.split(",");
                if (countryLookUp.get(tokens[0]) == null) {
                    return;
                }

                String cntryName = countryLookUp.get(tokens[0]);
                if(!cntryName.equals(prevCntryName[0])){
                    counter[0] =0;
                    prevCntryName[0] = cntryName;
                }
                else counter[0]++;

                String name = tokens[1];
                ArrayList<City> tempCities = (cities.containsKey(name)) ? cities.get(name): new ArrayList<City>();
                String locId = tokens[0] + counter[0];
                int pop = (tokens[4].equals("")) ? 0 : Integer.valueOf(tokens[4]);
                double lat = Double.valueOf(tokens[5]);
                double lon = Double.valueOf(tokens[6]);
                City city = new City(locId, name, tokens[3], cntryName, lat, lon, pop);
                tempCities.add(city);
                cities.put(name, tempCities);

//                if(countries.get(cntryName)==null){
//                    counter[0] = 0;
//                    HashMap<String, List<City>> temp = new HashMap<>();
//                    ArrayList<City> tempCities = new ArrayList<>();
//                    String name = tokens[1];
//                    String locId = tokens[0] + counter[0];
//                    int pop = (tokens[4].equals("")) ? 0 : Integer.valueOf(tokens[4]);
//                    double lat = Double.valueOf(tokens[5]);
//                    double lon = Double.valueOf(tokens[6]);
//                    City city = new City(locId, name, tokens[3], cntryName, lat, lon, pop);
//                    tempCities.add(city);
//                    temp.put(name, tempCities);
//                    countries.put(cntryName, temp);
//                }
//
//                else{
//                    counter[0]++;
//                    addCitiesToCountry(cntryName, tokens, counter);
//                }

            });

        }
        //System.out.println(printCitiesForCountry("united states"));

        StringBuilder sb = new StringBuilder();
        for(City city : cities.get("chicago")){
            sb.append("LocID: "+ city.locId+ " City Name: " + city.name + " Pop: " +city.pop + " Lat: " + city.lat + " Long: " + city.lon + "\n");
        }
        System.out.println(sb.toString());
    }
}
