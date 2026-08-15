package com.java.hime.arrays_strings;

public class SearchElementsInSortedRotatedArray {
    public static void main(String[] args) {
        int arr[] = {4,5,6,7,0,1,2};
        int target = 3;  // 4,5,6,7,0,1,2], target = 3
        int l=0;
        int r=arr.length-1;
        int pivot = find_pivot_element(arr, l, r);
        int bs = bsearch(arr, l, pivot, target);
        int position=bs == -1 ? bsearch(arr, pivot+1, r, target) : bs;
        System.out.println("Position Found - "+position);
    }

    private static int bsearch(int[] arr, int l, int r, int target) {
        while(l<=r){
            int mid=(l+r)/2;
            if(arr[mid]==target){
                return mid+1;
            }else if(arr[mid] < target){
                l=mid+1;
            }else if(arr[mid] > target){
                r=mid-1;
            }
        }
        return -1;
    }

    private static int find_pivot_element(int[] arr, int l, int r) {
        while(l<=r){
            int mid = (l+r)/2;
            if (arr[mid] > arr[mid+1]){
                return mid;
            }else if (arr[mid] < arr[mid-1]){
                return mid-1;
            }else if(arr[mid] > arr[l]){
                l=mid+1;
            }else {
                r=mid-1;
            }
        }
        return -1;
    }
}
