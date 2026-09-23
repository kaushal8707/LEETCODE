package ai.ml.leetcode.arrays_strings;

import java.util.Arrays;

public class ReverseOnlyLetters {
    public static void main(String[] args) {
        String s = "a-bC-dEf-ghIj";   // "ab-cd"
        String reverseOnlyLetters = reverseOnlyLetters(s);
        System.out.println("Reversed Only Letters Result - "+reverseOnlyLetters);
    }

    private static String reverseOnlyLetters(String s) {
        int l=0;
        int r=s.length()-1;
        char arr[]=s.toCharArray();
        while(l<r){
            if(isLetter(arr[l])){
                while(!isLetter(arr[r])){
                    r--;
                }
                char temp = arr[l];
                arr[l] = arr[r];
                arr[r] = temp;
                r--;
            }
            l++;
        }
        return Arrays.toString(arr);
    }

    private static boolean isLetter(char ch){
        return (ch>=97 && ch <=122) ||(ch>=65 && ch<=90);
    }
}
