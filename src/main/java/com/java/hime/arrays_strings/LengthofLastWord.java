package com.java.hime.arrays_strings;

import javax.sound.midi.Soundbank;

public class LengthofLastWord {
    public static void main(String[] args) {
        String s = "Hello World  ";
        int lengthOfLastWord = lengthOfLastWord(s);
        System.out.println("Length of Last words  " + lengthOfLastWord);
    }

    public static int lengthOfLastWord(String s) {
        int count=0;
        for(int i=s.length()-1; i>0; i--){
            if(s.charAt(i) != ' '){
                count++;
            }else if(count > 0){
                return count;
            }
        }
        return count;
    }
}
