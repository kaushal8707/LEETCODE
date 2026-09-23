package ai.ml.leetcode.programming_questions.strings;

public class banana_b_ba_ban_bana_banan_banana_a_an
{
    public static void main(String[] args) {
        String str="banana";
        for(int i=0;i<str.length();i++)
        {
            for (int j=i;j<str.length();j++)
            {
               for(int k=i;k<=j;k++)
               {
                   System.out.print(str.charAt(k));
               }
                System.out.print(",");
            }
        }
    }
}
