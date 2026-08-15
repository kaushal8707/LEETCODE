package com.java.hime.sorting;

public class MergeSort {
    public static void main(String[] args) {

        int arr[]={5, 1, 2, 8, 9, 3, 4, 6};
        int left=0;
        int right=arr.length-1;

        System.out.println("\n-----Before Sorting-----\n");
        for(int n:arr){
            System.out.print(n+" ");
        }

        mergeSort(arr, left, right);

        System.out.println("\n-----After sorting----\n");
        for(int i:arr){
            System.out.print(i+" ");
        }
    }

    private static void mergeSort(int[] arr, int left, int right) {
        if(left<right){
            int mid=(left+right)/2;
            mergeSort(arr, left, mid);
            mergeSort(arr, mid+1, right);

            mergeSortedArray(arr, left, mid, right);
        }
    }

    private static void mergeSortedArray(int[] arr, int left, int mid, int right) {

        int lArrSize=mid-left+1;
        int rArrSize=right-mid;

        //declare 2 empty left and right array
        int leftArray[]=new int[lArrSize];
        int rightArray[]=new int[rArrSize];

        //copy the value into left and right array from an actual array
        for(int i=0; i<lArrSize ;i++){
            leftArray[i]=arr[i+left];
        }
        for(int j=0; j<rArrSize; j++){
            rightArray[j]=arr[mid+1+j];
        }

        //comparing 2 arrays and storing into a sorted array
        int i=0;
        int j=0;
        int k=left;
        while(i<lArrSize && j<rArrSize){
            if(leftArray[i]<=rightArray[j]){
                arr[k]=leftArray[i];
                i++;
            }else {
                arr[k]=rightArray[j];
                j++;
            }
            k++;
        }

        while(i<lArrSize) {
            arr[k]=leftArray[i];
            i++;
            k++;
        }
        while(j<rArrSize) {
            arr[k]=rightArray[j];
            j++;
            k++;
        }
    }
}

//  Time Complexity
//  ---------------
//  Time: O(n log n)
//  Extra space: O(n)
//  Best case: O(n log n)
//  Average case: O(n log n)
//  Worst case: O(n log n)