package com.java.hime.arrays_strings;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class CustomSortString {
    public static void main(String[] args) {
        String order = "cba";  // "bcafg"   //cba
        String s = "abcd";     // "abcd"    //aaabcd
        String customSortString = customSortString(order, s);
        System.out.println("Custom SOrt String Based on given Order String: "+customSortString);
    }

    public static String customSortString(String order, String s) {
        StringBuilder builder = new StringBuilder();
        Set<Character> set=new HashSet<>();
        int ca[]=new int[26];
        for(char ch:order.toCharArray()){
            set.add(ch);
        }
        for(char c : s.toCharArray()){
            if(!set.contains(c)){
                builder.append(c);
            }else {
                ca[c - 'a']++;
            }
        }
        for(char ch : order.toCharArray()){
            int count = ca[ch-'a'];
            int i=0;
            while(i<count){
                builder.append(ch);
                i++;
            }
        }
        return builder.toString();
    }
}
