package ai.ml.leetcode.programming_questions.arrays;

public class RemoveDuplicateFromSortedIntegerArray
{
    public static void main(String[] args) {

        int k=0;
        int arr[]={1,2,2,3,4,5,5};
        for(int i=0;i<arr.length-1;i++)
        {
            if(arr[i]!=arr[i+1])
            {
                arr[k]=arr[i];
                k++;
            }
        }
        arr[k]=arr[arr.length-1];
        for(int m=0;m<=k;m++)
        {
            System.out.print(arr[m]+" ");
        }

    }
}
