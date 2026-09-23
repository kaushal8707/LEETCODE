package ai.ml.leetcode.programming_questions.strings;

public class LengthofWordInSentence
{
    public static void main(String[] args) {

        String str="kaushal kumar singh";
        str=str+" ";
        int start=0;
        for(int i=0;i<str.length();i++)
        {
           if(str.charAt(i)==' ')
           {
               System.out.println(str.substring(start,i)+"   "+(i-start));
               start=i+1;
           }
        }
    }
}
