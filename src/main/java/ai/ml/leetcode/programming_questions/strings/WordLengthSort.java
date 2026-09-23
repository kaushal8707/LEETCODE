package ai.ml.leetcode.programming_questions.strings;
import java.util.*;
import java.util.stream.Collectors;

public class WordLengthSort
{
    public static void main(String[] args) {

        String str="kaushal got a good platform to enhance his skill ";
        int start=0;
        Map<String,Integer> map=new HashMap<String,Integer>();
        for(int i=0;i<str.length();i++)
        {
            if(str.charAt(i)==' ')
            {
                System.out.println(str.substring(start,i)+" "+(i-start));

                map.put(str.substring(start,i),(i-start));
                start=i+1;
            }
        }
        Set<Map.Entry<String,Integer>> set=map.entrySet();
        List<Map.Entry<String,Integer>> list=new ArrayList<>(set);
        Collections.sort(list,(entry1,entry2)->entry1.getValue().compareTo(entry2.getValue()));
        list.stream().collect(Collectors.toList()).forEach(System.out::println);

    }

}
