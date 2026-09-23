package ai.ml.leetcode.programming_questions.strings;
class RemoveDuplicateCharacterInString
{
    public static void main(String[] args)
    {
        String str="kaushal kumar singh";
        char ch[]=str.toCharArray();
        for(int i=0;i<str.length();i++)
        {
            int count=0;
            for(int j=i+1;j<str.length();j++)
            {
                if(ch[j]==ch[i])
                {
                    count++;
                    ch[j]='0';
                }
            }
            if(ch[i]!='0')
            {
                System.out.print(ch[i]);
            }
        }

    }
}
