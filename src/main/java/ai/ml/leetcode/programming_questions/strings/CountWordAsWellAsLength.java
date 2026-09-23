package ai.ml.leetcode.programming_questions.strings;
public class CountWordAsWellAsLength
{
    public static void main(String[] args)
    {
        String name="kaushal is working in sapient";
        char chArray[]=name.toCharArray();
        int start=0;
        for(int i=0;i<chArray.length;i++)
        {
            if(chArray[i]==' ')
            {
                System.out.println(name.substring(start,i)+" = "+(i-start));
                start=i+1;
            }
        }

    }
}
