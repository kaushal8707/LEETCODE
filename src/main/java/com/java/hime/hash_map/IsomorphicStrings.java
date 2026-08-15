package com.java.hime.hash_map;

import java.util.HashMap;
import java.util.Map;

public class IsomorphicStrings {
    public static void main(String[] args) {
        String s = "egg";  //f11  paper   ab
        String t = "add";  //b23   title  aa
        boolean isomorphic = isIsomorphic(s, t);
        System.out.println(isomorphic);
    }

    private static boolean isIsomorphic(String s, String t) {
        Map<Character, Character> map=new HashMap<>();
        if(s.length()!=t.length()) {
            return false;
        }
        for(int i=0; i<s.length();i++){
            char ss = s.charAt(i);
            char tt = t.charAt(i);
            if(map.containsKey(ss) && map.get(ss)!=tt ||
               !map.containsKey(ss) && map.values().contains(tt)){
                return false;
            }
            map.put(ss, tt);
        }
        return true;
    }
}
