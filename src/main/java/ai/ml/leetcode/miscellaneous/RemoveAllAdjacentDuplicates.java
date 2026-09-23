package ai.ml.leetcode.miscellaneous;
import java.util.Stack;

public class RemoveAllAdjacentDuplicates {
    public static void main(String[] args) {
        String s = "abbaca";
        String s1 = "azxxzy";
        String removedDuplicates = removeDuplicates(s);
        System.out.println(removedDuplicates);
    }

    private static String removeDuplicates(String s) {
        Stack<Character> stack = new Stack<>();
        char[] chars = s.toCharArray();
        for(Character ch : chars){
            if(!stack.isEmpty() && stack.peek()==ch){
                stack.pop();
            }else{
                stack.push(ch);
            }
        }
        return stack.toString();
    }
}
