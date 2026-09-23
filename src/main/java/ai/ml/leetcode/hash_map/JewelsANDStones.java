package ai.ml.leetcode.hash_map;

import java.util.HashSet;
import java.util.Set;

public class JewelsANDStones {
    public static void main(String[] args) {
        String jewels = "aA";  // z
        String stones = "aAAbbbb";  //ZZ
        int numJewelsInStones = numJewelsInStones(jewels, stones);
        System.out.println(numJewelsInStones);
    }

    private static int numJewelsInStones(String jewels, String stones) {
        Set<Character> set = new HashSet<>();
        int nums = 0;
        for(char ch : jewels.toCharArray()){
            set.add(ch);
        }

        for(char ch : stones.toCharArray()){
            if(set.contains(ch)){
                nums++;
            }
        }
        return nums;
    }
}
