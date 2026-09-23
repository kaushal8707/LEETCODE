package ai.ml.leetcode.arrays_strings;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class LengthofAllWords {
    public static void main(String[] args) {
        String s = "  fly me   to  the moon   ";
        Map<String, Integer> mapLengthOfWords = lengthOfLastWord(s);
        System.out.println(mapLengthOfWords);
    }

    public static Map<String, Integer> lengthOfLastWord(String s) {
        int i=0;
        int j=0;
        int c=0;
        Map<String, Integer> map=new LinkedHashMap<>();
        for(;i<s.length();i++){
            if(s.charAt(i) != ' '){
                c++;
            }else if(c > 0){
               map.put(s.substring(j, i),c);
                j=i;
                c=0;
            }
        }
        return map;
    }
}
