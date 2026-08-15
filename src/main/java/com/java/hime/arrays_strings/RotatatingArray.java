package com.java.hime.arrays_strings;

public class RotatatingArray{
    public static void main(String[] args) {
        int k = 3;  // number of steps requiredto rotate from right
        int arr[]={1,2,3,4,5,6,7};    // o/p - {5,6,7,1,2,3,4}
        int rp=arr.length-k;  // rotation point
        rotate_array_at_specific_point(new int[]{1,2,3,4,5,6,7}, rp);
    }

    private static void rotate_array_at_specific_point(int[] arr, int k) {
        revsrse_series(arr, 0, k-1);
        revsrse_series(arr, k, arr.length-1);
        revsrse_series(arr, 0, arr.length-1);

        for(int i : arr){
            System.out.print(i + " ");
        }
    }

    private static void revsrse_series(int arr[], int l, int r)
    {
        while(l<r){
            int temp = arr[l];
            arr[l]=arr[r];
            arr[r]=temp;

            l++;
            r--;
        }
    }



}
