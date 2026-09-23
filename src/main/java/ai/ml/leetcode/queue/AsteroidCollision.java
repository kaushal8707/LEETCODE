package ai.ml.leetcode.queue;

import java.util.Arrays;
import java.util.Stack;
// whenever -ve asteroid will come then only we will pop up asteroid from stack that's why using while loop to check
// and compare not in any other way
public class AsteroidCollision {
    public static void main(String[] args) {
        int[] asteroids = {5, 10, -5};  // Input: asteroids = [3,5,-6,2,-1,4],  Output: [-6,2,4]
        int[] asteroids1 = {3,5,-6,2,-1,4};
        String result = Arrays.toString(asteroidCollision(asteroids1));
        System.out.println(" R E S U L T -"+result);
    }

    private static int[] asteroidCollision(int[] asteroids){
        Stack<Integer> stack = new Stack();

        for(int aster : asteroids){
            boolean explodede = false;

            while(!stack.isEmpty() && stack.peek() > 0 && aster < 0){
                if(stack.peek() < Math.abs(aster)){
                    stack.pop();
                    continue;
                } else if(stack.peek() == Math.abs(aster)){
                    stack.pop();
                    explodede = true;
                    break;
                } else {
                    explodede = true;
                    break;
                }
            }
            if(!explodede){
                stack.push(aster);
            }
        }
        int result[]=new int[stack.size()];
        for(int i = result.length-1; i>=0; i--){
            result[i] = stack.pop();
        }
        return result;
    }
}
