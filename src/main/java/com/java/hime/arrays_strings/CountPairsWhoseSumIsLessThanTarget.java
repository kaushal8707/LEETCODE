package com.java.hime.arrays_strings;

import java.util.Arrays;
import java.util.List;

public class CountPairsWhoseSumIsLessThanTarget {
    public static void main(String[] args) {
        List<Integer> list = Arrays.asList(-1,1,2,3,1);   // -1 1 1 2 3
        int target = 2;
        int countedPairs = countPairs(list, target);
        System.out.println(countedPairs);
    }

    public static int countPairs(List<Integer> nums, int target) {
        int size=nums.size();
        nums.sort((a,b)-> a-b);  // we perform operations on sorted array
        int i=0;
        int j=size-1;
        int count=0;
        while(i<j){
            if (nums.get(i) + nums.get(j) < target) {
                count = count + (j - i);
                i++;
            }else{
                j--;
            }
        }
        return count;
    }
}
