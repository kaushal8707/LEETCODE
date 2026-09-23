package ai.ml.leetcode.arrays_strings;

public class GetEqualSubstringsWithinBudget {
    public static void main(String[] args) {
       String s = "abcd";  //abcd  //abcd
       String t = "bcdf";  //acde  //cdef
       int maxCost = 3;   //0  //3
       int equalSubstring = equalSubstring(s, t, maxCost);
       System.out.println(equalSubstring);
    }

    private static int equalSubstring(String s, String t, int maxCost) {
        int l = 0;
        int window = 0;
        int max  = -1;
        char sArr[] = s.toCharArray();
        char tArr[] = t.toCharArray();

        for(int r = 0; r < s.length(); r++){
            window += Math.abs(sArr[r] - tArr[r]);

            while(window > maxCost){
                window -= Math.abs(sArr[l] - tArr[l]);
                l++;
            }
            max = Math.max(max, r-l+1);
        }
        return max;
    }
}
