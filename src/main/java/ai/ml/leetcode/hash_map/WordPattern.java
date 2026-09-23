package ai.ml.leetcode.hash_map;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class WordPattern {
    public static void main(String[] args) {
        String pattern = "abba"; //"abba"          "aaaa"
        String s = "dog cat cat dog"; //"dog cat cat fish"       "dog cat cat dog"
        boolean wordPattern = wordPattern(pattern, s);
        System.out.println(wordPattern);
    }

    private static boolean wordPattern(String pattern, String s) {
        String strArr[]=s.split(" ");
        Map<Character, String> map=new HashMap();
        if(pattern.length()!=strArr.length) {
            return false;
        }
        for(int i=0;i<pattern.length();i++){
            char p = pattern.charAt(i);
            String str = strArr[i];
            if((map.containsKey(p) && !map.get(p).equals(str)) ||
            (!map.containsKey(p) && map.values().contains(str))){
                return false;
            }
            map.put(p, str);
        }
        return true;
    }
}
