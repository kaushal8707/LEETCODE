package com.java.hime.hash_map;

import java.util.HashMap;
import java.util.Map;

public class FirstLetterToAppearTwice {
    public static void main(String[] args) {
       String s = "abccbaacz";  // abcdd
        char repeatedCharacter = repeatedCharacter(s);
        System.out.println("First Repeated Character :" +repeatedCharacter);
    }

    private static char repeatedCharacter(String s) {
        Map<Character, Integer> map = new HashMap();
        int c=1;
        char result='0';
        for(char ch : s.toCharArray()){
            if(map.containsKey(ch)){
                c = map.getOrDefault(ch, 0)+1;
                result = c > 1 ? ch : '0';
            }else{
                map.put(ch, c);
            }
        }
        return result;
    }
}
