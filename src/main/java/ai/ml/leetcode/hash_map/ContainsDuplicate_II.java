package ai.ml.leetcode.hash_map;

import java.util.HashMap;
import java.util.Map;

public class ContainsDuplicate_II {
    public static void main(String[] args) {
       int[] nums = {1,2,3,1};  // 1,0,1,1     1,2,3,1,2,3
       int k = 3;  // 1    2
        boolean containsNearbyDuplicate = containsNearbyDuplicate(nums, k);
        System.out.println(containsNearbyDuplicate);
    }

    private static boolean containsNearbyDuplicate(int[] nums, int k) {
        Map<Integer, Integer> map=new HashMap();
        for(int i=0;i<nums.length;i++){
            if(map.containsKey(nums[i]) &&
            Math.abs(i - map.get(nums[i])) <= k){
                return true;
            }
            map.put(nums[i], i);
        }
        return false;
    }
}
