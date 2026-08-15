package com.java.hime.stacks;
import java.util.Arrays;
import java.util.Stack;

public class FinalPricesWithSpecialDiscountInaShop {
    public static void main(String[] args) {
        int[] prices = {8,4,6,2,3}; //[1,2,3,4,5]       //op - [4,2,4,2,3]   // op- [1,2,3,4,5]
        int[] finalPrices = finalPrices(prices);
        System.out.println(Arrays.toString(finalPrices));
    }
    private static int[] finalPrices(int[] prices) {
        Stack<Integer> stack = new Stack();
        int[] resultant = new int[prices.length];
        for(int i=0;i<prices.length;i++){
            while(!stack.isEmpty() && prices[i]<prices[stack.peek()]){
                int popped = stack.pop();
                int discount = prices[popped] - prices[i];
                resultant[popped]=discount;
            }
            stack.push(i);
        }
        for(int i : stack){
            resultant[i] = prices[i];
        }
        return resultant;
    }
}
