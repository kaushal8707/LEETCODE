package ai.ml.leetcode.programming_questions.arrays;

public class MoveZeroToEndNonZeroToFront
{
    public static void main(String[] args)
    {
        int count=0;
        int arr[]={2,4,0,6,5,0,1,6,0,11};
        for(int i=0;i<arr.length;i++)
        {
            if(arr[i]!=0)
            {
                arr[count]=arr[i];
                count++;
            }
        }
        while(count<arr.length)
        {
            arr[count]=0;
            count++;
        }
        for(int k:arr)
        {
            System.out.print(k+" ");
        }
    }
}
