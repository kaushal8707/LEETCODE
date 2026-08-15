package com.java.hime.stacks;

import java.util.Arrays;
import java.util.Stack;

public class DailyTemperatures {
    public static void main(String[] args) {
        int[] temperatures = {73,74,75,71,69,72,76,73};
        int[] dailyTemperatures = dailyTemperatures(temperatures);
        System.out.println(Arrays.toString(dailyTemperatures));
    }

    private static int[] dailyTemperatures(int[] temperatures) {
        Stack<Integer> stack = new Stack<Integer>();
        int[] resultant = new int[temperatures.length];
        for(int i=0;i<temperatures.length;i++){
            while(!stack.isEmpty() && temperatures[i] > temperatures[stack.peek()]){
                int popped = stack.pop();
                resultant[popped] = i - popped;
            }
            stack.push(i);
        }
        return resultant;
    }
}
