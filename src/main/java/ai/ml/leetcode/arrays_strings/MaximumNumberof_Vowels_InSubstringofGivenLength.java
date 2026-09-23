package ai.ml.leetcode.arrays_strings;

public class MaximumNumberof_Vowels_InSubstringofGivenLength
{
    public static void main(String[] args) {
       String s = "abciiidef";  // aeiou
       int k = 3;   // 2
        int maxVowels = maxVowels(s, k);
        System.out.println("Max Number of Vowels :"+maxVowels);
    }

    private static int maxVowels(String s, int k) {
        char chArr[] = s.toCharArray();
        int l=0;
        int r=0;
        int window = 0;
        int result = 0;
        for (int i=0; i<k; i++){
            window += isVowels(chArr[i]);
        }
        result=window;
        for(r=k; r<chArr.length; r++){

            window += isVowels(chArr[r]);
            window -= isVowels(chArr[l]);
            l++;

            result = Math.max(result, window);
        }
        return result;
    }

    private static int isVowels(char ch){
        return ch == 'a' | ch == 'e' | ch == 'i' | ch == 'o' | ch == 'u' ? 1 : 0;
    }
}
