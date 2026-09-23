package ai.ml.leetcode.programming_questions.arrays;

public class Concat2Arrays
{
    public static void main(String[] args) {

        int arr1[]={6,7,8,9,10};
        int arr2[]={1,2,3,4,5};
        int count=0;
        int arr[]=new int[arr1.length+arr2.length];
        for(int i=0;i<arr1.length;i++)
        {
            arr[count]=arr1[i];
            count++;
        }
        for(int j=0;j<arr2.length;j++)
        {
            arr[count]=arr2[j];
            count++;
        }
        for(int l:arr)
        {
            System.out.println(l);
        }

    }
}
