package ai.ml.leetcode.hash_map;

import java.util.HashMap;
import java.util.Map;

public class MinimumConsecutiveCardstoPickUp {
    public static void main(String[] args) {
        int[] cards = {3,4,2,3,4,7};   //{1,0,5,3}
        int minimumCardPickup = minimumCardPickup(cards);
        System.out.println("Minimum Cards to Picked Up : "+minimumCardPickup);
    }

    public static int minimumCardPickup(int[] cards) {
        Map<Integer, Integer> map = new HashMap<>();
        int min = Integer.MAX_VALUE;

        for(int i=0;i<cards.length;i++){
            if(map.containsKey(cards[i])){
               min = Math.min(min, i - map.get(cards[i]) + 1);
            }
            map.put(cards[i], i);
        }
        return min == Integer.MAX_VALUE ? -1 : min;
    }
}
