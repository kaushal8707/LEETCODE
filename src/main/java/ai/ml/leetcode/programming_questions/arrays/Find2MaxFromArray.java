package ai.ml.leetcode.programming_questions.arrays;

public class Find2MaxFromArray
{
    public static void main(String[] args) {
        int arr[]={11,2,31,4,91,8,7,61,15};
        int max1=0,max2=0;
        for(int i=0;i<arr.length;i++)
        {
           if(arr[i]>max2){
               max2=max1;
               max1=arr[i];
           }else{
               max2=arr[i];
           }
        }
        System.out.println("Max1 = "+max1+"   Max2 ="+max2);
    }
}
