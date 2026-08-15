package com.java.hime.hash_map;

import java.util.*;

public class SortCharactersByFrequency_Bucket_Sort {
    public static void main(String[] args) {
        String s = "tree";  // cccaaa   Aabb
        String frequencySort = frequencySort(s);
        System.out.println(frequencySort);
    }
    private static String frequencySort(String s) {
        Map<Character, Integer> map = new HashMap();
        for(char ch : s.toCharArray()){
            map.put(ch, map.getOrDefault(ch, 0)+1);
        }
        List<Character> arr[]= new ArrayList[s.length()];
       //from map need to store data in List<Character> Array
        for(char ch : map.keySet()){
            if(arr[map.get(ch)] == null){
                arr[map.get(ch)] = new ArrayList<>();
            }
            arr[map.get(ch)].add(ch);    //arr prepared with List of characters
        }
        StringBuilder sb = new StringBuilder();
        System.out.println(arr.length);
        for(int i=arr.length-1;i>=0;i--){
            if(arr[i]!=null){
                for(char ch:arr[i]){
                    for(int k=0;k<i;k++){
                        sb.append(ch);
                    }
                }
            }
        }
        System.out.println(sb);





        return new String();
    }
}
