package me.bzifkin;

import java.io.*;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import com.linkedin.paldb.api.PalDB;
import com.linkedin.paldb.api.StoreWriter;

public class Main {


    public static HashMap<String, String> countryLookUp = new HashMap<>(); //read in br1, key is ISO code (e.g. AF, US, UK), country name as value.
    public static HashMap<String, ArrayList<City>> cities = new HashMap<>(); //read in br2, key is name of the city, value is list of all cites w/ that name
    public static HashMap<String, HashMap<String, String>> regionLookUp = new HashMap<>(); //read in br3, key to outer map is country name, key to inner map is region code, value is region name
    public static HashMap<String, String> stateLookUp = new HashMap<>(); //read in br4, key is state abbreviation (e.g. IL, MA, etc) value is state name
    public static ArrayList<String> allRegions = new ArrayList<>();
    public static ArrayList<BookStatistics> books = new ArrayList<>();
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

    public static void findUniqueLocations(String dir) throws IOException {
        final File folder = new File(dir);
        for (final File fileEntry : folder.listFiles()) {

            String bookId = String.valueOf(fileEntry).split("/")[7];
            ArrayList<String> regions = new ArrayList<String>();
            StringBuilder sb = new StringBuilder();
            sb.append(bookId + " :\n");
            BookStatistics bs = new BookStatistics(bookId);
            final int[] countryMentions = {0};
            final int[] regionMentions = {0};
            final int[] cityMentions = {0};
            final int[] almostUniqLocs = {0};
            final int[] numRegions = {0};
            //if(fileEntry.isDirectory()) continue;
            try (BufferedReader br =
                         new BufferedReader(new InputStreamReader(new FileInputStream(fileEntry)))) {
                br.lines().forEach(line -> {
                    if(line!=null ) {
                        String location = line.split("\\t")[0];
                        if (countryLookUp.containsValue(location)){
                            System.out.println("added to countries: " +line);
                            bs.countries.add(line);
                        }


                       else if (allRegions.contains(location) || stateLookUp.containsValue(location)){
                            regions.add(line);
                            System.out.println("added to state: " +line);
                            bs.states.add(line);
                            numRegions[0]++;
                        }

                       else if(cities.get(location)!=null ){
                                //legitLocs[0]++;
//                            if(cities.get(line).size() == 1) {
//                                //uniqLocs[0]++;
//                                City city = cities.get(line).get(0);
//                                sb.append("Unique- " + city.toTSV());
//                            }
//                            else if (cities.get(line).size()==2){
//                                almostUniqLocs[0]++;
//
//                            }
                            System.out.println("added to cities: " +line);
                            bs.cities.add(line);

                        }
                    }
                });

            }

            bs.sortLists();
            books.add(bs);
            //System.out.println(sb.toString());

        }
    }

    private static double distance(double lat1, double lon1, double lat2, double lon2, String unit) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        if (unit == "K") {
            dist = dist * 1.609344;
        } else if (unit == "N") {
            dist = dist * 0.8684;
        }

        return (dist);
    }

//	This function converts decimal degrees to radians

    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

	//	This function converts radians to decimal degrees

    private static double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
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
            countryLookUp.put("gbb", "great britain");
            countryLookUp.put("hey", "hayti");
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
                    String region = tokens[1].split("\\[")[0].toLowerCase().trim();
                    allRegions.add(region);
                    allRegions.add("guadaloupe");
                    allRegions.add("hispaniola");
                    regionLookUp.get(cntryName[0]).put(code, region);
                }

            });
        }

        try (BufferedReader br4 =
                     new BufferedReader(new InputStreamReader(Main.class.getResourceAsStream("/states.txt")))) {
            br4.lines().forEach(line4 -> {

                String[] tokens = line4.split(",");
                stateLookUp.put(tokens[0], tokens[1].toLowerCase());
            });
        }

        System.out.println(countryLookUp.containsValue("grenada"));

        //sortCitiesByPop();
        assignStates();
        assignRegions();
        findUniqueLocations("/home/bzifkin/IdeaProjects/lemur-galago/location-files/indices/");

        StringBuilder sb = new StringBuilder();
        for(BookStatistics bs: books){
            sb.append(bs.toString() + "\n");
        }

        System.out.println(sb.toString());

//        StringBuilder sb = new StringBuilder();
//        for(City city : cities.get("london")){
//            sb.append("LocID: "+ city.locId+ " City Name: " + city.name + " Country Name: " + city.country+ " Region: " + city.region + " Pop: " +city.pop + " Lat: " + city.lat + " Long: " + city.lon + "\n");
//        }
//        System.out.println(sb.toString());

//        StoreWriter writer = PalDB.createWriter(new File("/home/bzifkin/IdeaProjects/lemur-galago/cities.paldb"));
//        for(Map.Entry<String, ArrayList<City>> city : cities.entrySet())
//        {
//            ArrayList<City> cityList = city.getValue();
//            String[] newCityList = new String[cityList.size()];
//            cityList.forEach(tempCity->{
//                newCityList[cityList.indexOf(tempCity)] = tempCity.toCSV();
//            });
//
//            writer.put(city.getKey(), newCityList);
//
//        }
//        writer.close();
        final long endTime = System.currentTimeMillis();
        System.out.println("Total execution time: " + (endTime - startTime) );

    }
}
