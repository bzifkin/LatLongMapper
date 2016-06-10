package me.bzifkin;

import java.util.*;
import java.util.Comparator;
/**
 * Created by bzifkin on 6/9/16.
 */
public class BookStatistics {


    String name;
    public BookStatistics(String name){
        this.name = name;
    }


        ArrayList<String> cities =
                new ArrayList<>();
        ArrayList<String> states =
                new ArrayList<>();
        ArrayList<String> countries =
                new ArrayList<>();

    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append(this.name + "\n"+"Top Cities: \n" );
        int citiesLimit = (cities.size()>=3) ? 3 : cities.size();
        int statesLimit = (states.size()>=3) ? 3 : states.size();
        int countryLimit = (countries.size()>=3) ? 3 : countries.size();

        for(int i=0; i<=citiesLimit-1;i++){
            sb.append(i+1+ ") "+ cities.get(i) + "\n");
        }

        sb.append("Top Countries: \n" );
        for(int i=0; i<=countryLimit-1;i++){
            sb.append(i+1+ ") "+ countries.get(i) + "\n");
        }

        sb.append("Top Regions: \n" );
        for(int i=0; i<=statesLimit-1;i++){
            sb.append(i+1+ ") "+ states.get(i) + "\n");
        }

        return sb.toString();
    }


    public void sortLists(){
        Collections.sort(cities, new StringLengthComparator());
        Collections.sort(states, new StringLengthComparator());
        Collections.sort(countries, new StringLengthComparator());
    }


}


 class StringLengthComparator implements Comparator<String> {
    @Override
    public int compare(String x, String y) {

            int xMentions = Integer.parseInt(x.split("\\t")[1]);
            int yMentions = Integer.parseInt(x.split("\\t")[1]);
            if (xMentions > yMentions) {
                return -1;
            }
            if (xMentions < yMentions) {
                return 1;
            }

        return 0;
    }
}