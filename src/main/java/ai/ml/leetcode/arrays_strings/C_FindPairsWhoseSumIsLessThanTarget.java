package ai.ml.leetcode.arrays_strings;

import java.util.Arrays;
import java.util.List;

public class C_FindPairsWhoseSumIsLessThanTarget {
    public static void main(String[] args) {
        List<Integer> list = Arrays.asList(-1, 1, 2, 3, 1);  // -1 1 1 2 3  // we perform action on sorted array
        int target = 2;
        int pairCount=countPairs(list, target);
        System.out.println(pairCount);
    }

    public static int countPairs(List<Integer> nums, int target) {
        nums.sort((a,b)-> a-b);
        int l=0;
        int count=0;
        int r=nums.size()-1;
        while(l < r){
            if(nums.get(l) + nums.get(r) < target){
                count = count + (r - l);
                int k=r;
                while(l<k){
                    System.out.println("["+nums.get(l)+","+nums.get(k)+"]");
                    k--;
                }
                l++;
            }else {
                r--;
            }
        }
        return count;
    }

}