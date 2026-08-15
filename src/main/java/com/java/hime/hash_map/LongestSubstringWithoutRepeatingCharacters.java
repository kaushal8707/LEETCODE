package com.java.hime.hash_map;

import java.util.HashSet;
import java.util.Set;

public class LongestSubstringWithoutRepeatingCharacters {
    public static void main(String[] args) {
        String s = "abcabcbb"; //abba   abcbdacbbasxz
        int lengthOfLongestSubstring = lengthOfLongestSubstring(s);
        System.out.println(lengthOfLongestSubstring);
    }

    private static  int lengthOfLongestSubstring(String s) {
        Set<Character> set = new HashSet<>();
        int l = 0;
        int max=0;
        char chArr[] = s.toCharArray();
        for(int r=0; r<chArr.length; r++){
          while(set.contains(chArr[r])){
            set.remove(chArr[l]);
            l++;
          }
          set.add(chArr[r]);
          max = Math.max(max, r-l+1);
        }
        return max;
    }
}
