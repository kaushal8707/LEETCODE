package ai.ml.leetcode.miscellaneous;

import java.util.HashMap;
import java.util.Map;

public class MajorityElements_HashMap {
    public static void main(String[] args) {
        int nums[] = {2,2,1,1,1,2,2};
        int majorityElements = majorityElements(nums);
        System.out.println(majorityElements);
    }

    private static int majorityElements(int[] nums) {
        Map<Integer, Integer> map = new HashMap();
        int threshold = nums.length / 2;
        for(int num : nums){
            map.put(num, map.getOrDefault(num, 0) + 1);
        }
        for(int i : map.keySet()){
            if(map.get(i) > threshold){
                return i;
            }
        }
    return -1;
    }
}
