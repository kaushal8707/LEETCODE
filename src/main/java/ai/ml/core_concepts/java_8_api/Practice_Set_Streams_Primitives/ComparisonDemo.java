package ai.ml.core_concepts.java_8_api.Practice_Set_Streams_Primitives;

import java.util.stream.IntStream;

public class ComparisonDemo {
    public static void main(String[] args) {

        int count=0;
        for(int i=1;i<=10;i++){
            if(i%2==0){
                count++;
            }
        }
        System.out.println("event num count :"+count);
        System.out.println("let's try using java 8");
        long countEvenNums = IntStream.rangeClosed(1, 10).filter(x -> x % 2 == 0).count();
        System.out.println("event nums count : "+countEvenNums);
    }
}
