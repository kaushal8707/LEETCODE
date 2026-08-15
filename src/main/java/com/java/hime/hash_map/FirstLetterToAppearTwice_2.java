package com.java.hime.hash_map;

import java.util.HashSet;
import java.util.Set;

public class FirstLetterToAppearTwice_2 {
    public static void main(String[] args) {
        String s = "abccbaacz";  // abcdd
        char repeatedCharacter = repeatedCharacter(s);
        System.out.println("First Repeated Character :" + repeatedCharacter);
    }

    private static char repeatedCharacter(String s) {
        Set<Character> set = new HashSet<>();
        for(char ch : s.toCharArray()){
            if(set.contains(ch)){
                return ch;
            }else{
                set.add(ch);
            }
        }
        return 'a';
    }
}