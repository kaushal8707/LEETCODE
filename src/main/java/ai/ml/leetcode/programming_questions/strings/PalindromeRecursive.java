package ai.ml.leetcode.programming_questions.strings;

public class PalindromeRecursive
{
    public static void main(String[] args) {

        String str="abba";
        boolean check= checkPalindrome(str);
        if(check)
        {
            System.out.println("p");
        }else
        {
            System.out.println("np");
        }
    }

    private static boolean checkPalindrome(String s)
    {
        if(s.length()==0 || s.length()==1)
        {
            return true;
        }
        if(s.charAt(0)==s.charAt(s.length()-1))
        {
            return checkPalindrome(s.substring(1,s.length()-1));
        }
        return false;
    }

}
