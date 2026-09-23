package ai.ml.leetcode.programming_questions.strings;
//if 2 strings has equal length in size and same no. of characters but in differents order called anagram
public class CheckAnagram
{
    public static void main(String[] args)
    {
        String str1="silent";
        String str2="listen";
        if(checkAnagram(str1,str2))
        {
            System.out.println("Anagram");
        }else
        {
            System.out.println("Not an Anagram");
        }


    }

    private static boolean checkAnagram(String str1, String str2)
    {
        String s1=doSort(str1);
        String s2=doSort(str2);
      if(str1.length()==str2.length() && s1.equals(s2))
      {
          return true;
      }

      return false;
    }

    private static String doSort(String str)
    {
        char chArr[]=str.toCharArray();
        for(int i=0;i<chArr.length;i++)
        {
            for(int j=i+1;j<chArr.length;j++)
            {
                if(chArr[j]<chArr[i])
                {
                    char temp=chArr[j];
                    chArr[j]=chArr[i];
                    chArr[i]=temp;
                }
            }
        }
        return new String(chArr);
    }
}
