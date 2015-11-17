package is.genki.bigredapp.android;

import android.support.v4.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Holds data for maps to be accessed between activities
 */
public class SingletonMapData {

    private static SingletonMapData ourInstance = new SingletonMapData();
    //Double hashmap for category of location and location info
    //EX: <Buildings,<Ives,(42.32,53.432)>>
    private HashMap<String,HashMap<String,Pair<Double,Double>>> mapLocations;

    public static SingletonMapData getInstance() {
        return ourInstance;
    }

    private SingletonMapData() {
        mapLocations = new HashMap<>();
    }


    public void addLocation(String category, String name, double lat, double lon){
        if( mapLocations.containsKey(category)){
            mapLocations.get(category).put(name, new Pair(lat,lon));
        }else{
            HashMap<String,Pair<Double,Double>> catMap = new HashMap<>();
            catMap.put(name, new Pair(lat,lon));
            mapLocations.put(category, catMap);
        }
    }

    public HashMap<String,Pair<Double,Double>> mapForCategory(String category){
        return mapLocations.get(category);
    }

    public Set<String> getCategories(){
        return mapLocations.keySet();
    }

    /**
     * Searches for a string of a builiding in all of the data
     * We may want to find a more efficient data structure for this.
     * For now, it is fast enough though (less than 1 sec)
     */
    public ArrayList<Map.Entry<String,Pair<Double,Double>>> searchString(String query){
        String lower = query.toLowerCase();
        ArrayList<Map.Entry<String,Pair<Double,Double>>> arr = new ArrayList<>();
        for(  HashMap<String,Pair<Double,Double>> catMap : mapLocations.values() ){
            for(Map.Entry<String,Pair<Double,Double>> ent : catMap.entrySet()){
                if(ent.getKey().toLowerCase().contains(lower)){
                    arr.add(ent);
                }
            }

        }
        return arr;
    }
}
