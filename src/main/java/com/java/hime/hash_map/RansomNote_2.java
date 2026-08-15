package com.java.hime.hash_map;

public class RansomNote_2 {
    public static void main(String[] args) {
        String ransomNote = "a"; //aa  aa
        String magazine = "b";  //ab   aab
        boolean canConstruct = canConstruct(ransomNote, magazine);
        System.out.println("Can Construct  - "+canConstruct);

    }

    private static boolean canConstruct(String ransomNote, String magazine) {
        int r[]=new int[26];
        int m[]=new int[26];
        for(char c: ransomNote.toCharArray()){
            r[c - 'a']++;
        }
        for(char c: magazine.toCharArray()){
            m[c - 'a']++;
        }
        for(char ch : ransomNote.toCharArray()){
            if(r[ch-'a']  <= m[ch-'a']){
                return true;
            }
        }
        return false;
    }
}
