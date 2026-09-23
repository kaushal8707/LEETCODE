package ai.ml.leetcode.hash_map;

import java.sql.Array;
import java.util.*;

public class DestinationCity {
    public static void main(String[] args) {
       String[][] paths = {{"London","New York"},{"New York","Lima"},{"Lima","Sao Paulo"}};
       List<List<String>> pathList = new ArrayList<>();
       for(String[] path : paths){
           pathList.add(Arrays.asList(path));
       }
        String destCityWithNoDestination = destCity(pathList);
        System.out.println(destCityWithNoDestination);
    }

    private static String destCity(List<List<String>> paths) {
        Set<String> sourceCity=new HashSet();
        for(List<String> path: paths){
            sourceCity.add(path.get(0));
        }
        for(List<String> path : paths){
            if(!sourceCity.contains(path.get(1))){
                return path.get(1);
            }
        }
        return null;
    }
}
