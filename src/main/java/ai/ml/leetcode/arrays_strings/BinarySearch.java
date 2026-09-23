package ai.ml.leetcode.arrays_strings;

public class BinarySearch {
    public static void main(String[] args) {
        int arr[] = {2,3,4,7,55,99};
        int element = 12;
        int search_result = binary_search_of_sorted_elements(arr, element);
        String result = search_result != -1 ? element+" Found at Position : "+search_result : element+" Not Found";
        System.out.println(result);
    }

    private static int binary_search_of_sorted_elements(int[] arr, int element) {
        int l=0;
        int r=arr.length-1;
        while( l<=r){
            int mid = ( l + r) / 2;
            if (arr[mid] == element){
                return mid+1;
            }else if (arr[mid] < element){
                l=mid+1;
            }else if (arr[mid] > element){
                r=mid-1;
            }
        }
        return -1;
    }
}
