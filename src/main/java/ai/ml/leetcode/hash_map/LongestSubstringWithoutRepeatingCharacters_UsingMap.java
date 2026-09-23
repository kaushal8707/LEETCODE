package ai.ml.leetcode.hash_map;

import java.util.HashMap;
import java.util.Map;

public class LongestSubstringWithoutRepeatingCharacters_UsingMap {
        public static void main(String[] args) {
            String s = "abcabcbb"; //abba   abcbdacbbasxz
            int lengthOfLongestSubstring = lengthOfLongestSubstring(s);
            System.out.println(lengthOfLongestSubstring);
        }

        private static int lengthOfLongestSubstring(String s){
            char chArr[] =s.toCharArray();
            int max=-1;
            int l=0;
            Map<Character, Integer> map = new HashMap();
            for(int r=0;r<chArr.length;r++) {
                if(map.containsKey(chArr[r]) && map.get(chArr[r])>=l){
                    l=map.get(chArr[r]) + 1;
                }

                map.put(chArr[r], r);
                max=Math.max(max, r-l+1);
            }
            return max;
        }
}
