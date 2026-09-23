package ai.ml.leetcode.programming_questions.strings;

public class NumberOfWordsInString
{
    public static void main(String[] args)
    {
        String sentence="kaushal currently working in a Sapient";
        int count=1;
        for(int i=0;i<sentence.length();i++)
        {
            if(sentence.charAt(i)==' ' && sentence.charAt(i+1)!=' ')
            {
                count++;
            }
        }
        System.out.println(count);

    }
}
