package ai.ml.leetcode.hash_map;

import java.util.*;
import java.util.stream.Collectors;

public class FindPlayersWithZeroorOneLosses {
    public static void main(String[] args) {
        int[][] nums = {{1,3},{2,3},{3,6},{5,6},{5,7},{4,5},{4,8},{4,9},{10,4},{10,9}};  // op - [[1,2,10],[4,5,7,8]]
        List<List<Integer>> playersZeroOneLoses = findWinners(nums);
        System.out.println("Players Zero One Loses - "+playersZeroOneLoses);

    }

    public static List<List<Integer>> findWinners(int[][] matches) {
        List<Integer> zeroLoses=new ArrayList<Integer>();
        List<Integer> oneLoses = new ArrayList<Integer>();
        Map<Integer, Integer> map=new HashMap();
        for(int team[] : matches){
            map.put(team[0], map.getOrDefault(team[0], 0) + 0);
            map.put(team[1], map.getOrDefault(team[1], 0) + 1);
        }

        for(int p : map.keySet()){
            if(map.get(p)==0){
                zeroLoses.add(p);
            }else if (map.get(p)==1){
                oneLoses.add(p);
            }
        }
        Collections.sort(oneLoses);
        Collections.sort(zeroLoses);
        return Arrays.asList(zeroLoses, oneLoses);
    }
}
