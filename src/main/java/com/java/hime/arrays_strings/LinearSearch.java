package com.java.hime.arrays_strings;

public class LinearSearch {
    public static void main(String[] args) {
        //2,3,1,9,6,7
        int key=11;
        int res = linearsearch(new int[] {2,3,1,9,6,7},key);
        String result = res == -1 ? key+" Not Found" : key+" Found at "+res;
        System.out.println(result);
    }

    private static int linearsearch(int[] arr,int key){
        for(int i=0;i<arr.length;i++){
            if(arr[i]==key){
                return i+1;
            }
        }
        return -1;
    }
}
