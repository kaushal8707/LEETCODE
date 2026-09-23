package ai.ml.leetcode.hash_map;

public class CheckIftheSentenceIsPangram {
    public static void main(String[] args) {
        String sentence = "thequickbrownfoxjumpsoverthelazydog";
        boolean checkIfPangram = checkIfPangram(sentence);
        System.out.println("Is Panagram - "+checkIfPangram);
    }

    private static boolean checkIfPangram(String sentence) {
        int ca[]=new int[26];
        for(char ch : sentence.toCharArray()){
            ca[ch-'a']++;
        }
        for(int i : ca){
            if(i<1){
                return false;
            }
        }
        return true;
    }
}
