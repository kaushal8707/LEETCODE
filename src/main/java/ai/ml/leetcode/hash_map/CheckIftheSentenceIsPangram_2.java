package ai.ml.leetcode.hash_map;

import java.util.HashSet;
import java.util.Set;

public class CheckIftheSentenceIsPangram_2 {
    public static void main(String[] args) {
        String sentence = "thequickbrownfoxjumpsoverthelazydog";
        boolean checkIfPangram = checkIfPangram(sentence);
        System.out.println("Is Panagram - "+checkIfPangram);
    }

    private static boolean checkIfPangram(String sentence) {

        Set<Character> set = new HashSet();
        for(char ch : sentence.toCharArray()){
            set.add(ch);
        }
        if(set.size()!=26){
            return false;
        }
        return true;
    }
}
