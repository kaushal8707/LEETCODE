package ai.ml.leetcode.miscellaneous;
import java.util.Stack;

public class RemovingStarsFromaString {
    public static void main(String[] args) {
        String s = "leet**cod*e";
        String s1 = "erase*****";
        String removeStars = removeStars(s);
        System.out.println(removeStars);
    }

    private static String removeStars(String s) {
        Stack<Character> stack = new Stack();
        for(char ch : s.toCharArray()){
            if(!stack.isEmpty() && ch == '*'){
                stack.pop();
            }else{
                stack.push(ch);
            }
        }
        StringBuilder sb = new StringBuilder();
        for(char ch : stack){
            sb.append(ch);
        }
        return sb.toString();
    }
}
