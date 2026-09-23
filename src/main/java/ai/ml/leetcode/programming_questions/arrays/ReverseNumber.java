package ai.ml.leetcode.programming_questions.arrays;
public class ReverseNumber
{
    public static void main(String[] args)
    {
        int number=75896001;
        int reverse=0;
        int reminder=0;
        while(number!=0)
        {
            reminder=number%10;
            reverse=reverse*10+reminder;
            number=number/10;
        }
        System.out.println("Reverse number is : "+reverse);
    }
}
