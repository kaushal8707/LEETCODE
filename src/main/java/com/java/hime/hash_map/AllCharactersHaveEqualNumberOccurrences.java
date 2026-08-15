package com.java.hime.hash_map;

public class AllCharactersHaveEqualNumberOccurrences {
    public static void main(String[] args) {
        String s = "abacbc"; // aaabb   // honnable
        boolean areOccurrencesEqual = areOccurrencesEqual(s);
        System.out.println("Has Equals No of Occurrences "+areOccurrencesEqual);
    }

    private static boolean areOccurrencesEqual(String s) {
        int ca[]=new int[26];
        for(char ch : s.toCharArray()){
            ca[ch -'a']++;
        }
        int x=0;
        for(int i : ca){
            if(i!=0 && x==0){
                x=i;
            }else if(i!=0){
                if(i!=x){
                    return false;
                }
            }
        }
        return true;

    }

}
