package ai.ml.leetcode.programming_questions.arrays;

public class Permutation
{
    public static void main(String[] args)
    {
        String str="abc";
        String result="";
        getPermutation(str,result);
    }

    public static void getPermutation(String str,String result)
    {
       if(str.length()==0)
       {
           System.out.println(result);
       }
       for(int i=0;i<str.length();i++)
       {
           char ch=str.charAt(i);
           String res=str.substring(0,i)+str.substring(i+1);
           getPermutation(res,ch+result);
       }
    }
}
