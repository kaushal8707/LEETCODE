package ai.ml.leetcode.hash_map;

import java.util.Arrays;

/**
 * String will be closed
 * 1. If both are having same lengths
 * 2. character count should be same
 * 3. after sorting both string if both are equals
 */
public class DetermineIfTwoStringsAreClose {
    public static void main(String[] args) {
        String word1 = "cabbba";  // cabbba
        String word2 = "abbccc";  //abbccc
        boolean closeStrings = closeStrings(word1, word2);
        System.out.println(closeStrings);
    }
    private static boolean closeStrings(String word1, String word2) {
        if(word1.length() !=word2.length()){
            return false;
        }
        int[] ca1=new int[26];
        int[] ca2=new int[26];
        for(char ch:word1.toCharArray()){
            ca1[ch-'a']++;
        }
        for(char ch:word2.toCharArray()){
            ca2[ch-'a']++;
        }
        for(int i=0;i<26;i++){
            if(ca1[i]>0 && !(ca2[i]>0) || ca2[i]>0 && !(ca1[i]>0)){
                return false;
            }
        }
        Arrays.sort(ca1);
        Arrays.sort(ca2);
        System.out.println(Arrays.toString(ca1));
        System.out.println(Arrays.toString(ca2));

        return Arrays.equals(ca1, ca2);

    }
}
