package ai.ml.leetcode.hash_map;

public class RansomNote {
    public static void main(String[] args) {
        String ransomNote = "aa"; //aa  aa
        String magazine = "aab";  //ab   aab
        boolean canConstruct = canConstruct(ransomNote, magazine);
        System.out.println("Can Construct  - "+canConstruct);

    }

    private static boolean canConstruct(String ransomNote, String magazine) {
        int arr[]=new int[26];
        for(char ch : ransomNote.toCharArray()){
            arr[ch-'a']++;
        }
        for(char ch : magazine.toCharArray()){
            arr[ch-'a']--;
        }

        for(int i : arr){
            if(i>0){
                return false;
            }
        }
        return true;
    }
}
