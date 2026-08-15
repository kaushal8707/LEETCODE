package com.java.hime.hash_map;

import java.util.HashSet;
import java.util.Set;

public class PathCrossing {
    public static void main(String[] args) {
        String path = "NESWW";     // NES
        boolean pathCrossing = isPathCrossing(path);
        System.out.println(pathCrossing);
    }

    private static boolean isPathCrossing(String path) {
        Set<String> set = new HashSet<>();
        int x=0;
        int y=0;
        String origin=x+","+y;
        set.add(origin);
        String vertex="";
       for(char ch : path.toCharArray()){
           if (ch == 'N'){
               y++;
           }else if(ch=='E'){
               x++;
           }else if(ch=='W'){
               x--;
           }else if(ch=='S'){
               y--;
           }
           vertex=x+","+y;
           if(set.contains(vertex)){
               return true;
           }
           set.add(x+","+y);
       }
       return false;
    }
}
