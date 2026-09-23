package ai.ml.leetcode.programming_questions.strings;
import java.util.LinkedHashSet;
import java.util.Set;

public class abbbccddddToPrinta1b3c2d4
{
    static Set<String> set=new LinkedHashSet<String>();
    public static void main(String[] args) {
        String str="abbbccdddd";
        for(int i=0;i<str.length();i++) {
            int count=0;
            for (int j = 0; j < str.length(); j++) {
                    if (str.charAt(i) == str.charAt(j)) {
                        count++;
                    }
                }
            set.add(str.charAt(i) + "" + count);
        }
        for(String s:set)
            System.out.print(s);
        }
    }
