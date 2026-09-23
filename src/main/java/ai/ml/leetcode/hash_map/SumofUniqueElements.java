package ai.ml.leetcode.hash_map;

import java.util.HashMap;
import java.util.Map;

public class SumofUniqueElements {
    public static void main(String[] args) {
       int[] nums = {1,2,3,2}; // {1,1,1,1,1}
        int sumOfUnique = sumOfUnique(nums);
        System.out.println(sumOfUnique);
    }
    private static int sumOfUnique(int[] nums) {
        Map<Integer, Integer> map = new HashMap();
        int sum=0;
        for(int i:nums){
            map.put(i, map.getOrDefault(i,0) + 1);
        }
        for(int i : map.keySet()){
            if(map.get(i) == 1){
                sum += i;
            }
        }
        return sum;
    }
}
