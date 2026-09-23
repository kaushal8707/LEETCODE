package ai.ml.leetcode.arrays_strings;

public class DivisibleANDNon_divisibleSumsDifference {
    public static void main(String[] args) {
        int n = 10; // 5// range from 1 .....to 10
        int m = 3; // 6  // should be divisible by 3
        int differenceOfSums = differenceOfSums(n, m);
        System.out.println("SUm of Difference = "+differenceOfSums);
    }

    private static int differenceOfSums(int n, int m) {
        //suppose total number is 10 from 1 to 10
        //Number divisible by 3 is 3, 6, 9 ...=> 3( 1, 2, 3)   suppose total divisible number is x=>  m * n(n+1)/2
        int x = n / m;
        int num2 = m * (x * (x+1)/2);  // sum of divisible nums
        int total = n * (n+1)/2;
        int num1 = total - num2;      // sum of non - divisible nums

        int diffOfSum = (num1 - num2);
        return diffOfSum;
    }
}
