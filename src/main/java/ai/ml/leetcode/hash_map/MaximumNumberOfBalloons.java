package ai.ml.leetcode.hash_map;

import java.util.HashMap;
import java.util.Map;

public class MaximumNumberOfBalloons {
    public static void main(String[] args) {
        String text = "nlaebolko";   //"loonbalxballpoon"
        int maxNumberOfBalloons = maxNumberOfBalloons(text);
        System.out.println("Max Number Of Balloons    " +maxNumberOfBalloons);
    }

    private static int maxNumberOfBalloons(String text) {
        Map<Character, Integer> map = new HashMap();
        for(char ch : text.toCharArray()){
            if(ch=='b'||ch=='a'||ch=='l'||ch=='o'||ch=='n'){
                map.put(ch, map.getOrDefault(ch,0) + 1);
            }
        }
        int min1 = Math.min(map.getOrDefault('b', 0), Math.min(map.getOrDefault('a', 0), map.getOrDefault('n', 0)));
        int min2 = Math.min(map.getOrDefault('l',0), map.getOrDefault('o',0));
        return Math.min(min1, min2/2);
    }
}
