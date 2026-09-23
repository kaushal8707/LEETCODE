package ai.ml.core_concepts.java_8_api.Practise_Set_MiscellaneousExamples;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Multiple Loops in java 8 **/

public class PracticeSet_7 {
    public static void main(String[] args) {
        stateList().stream()
                .filter(state-> state.startsWith("p")).forEach(state->{
                    cityList().stream().forEach(city->{
                        System.out.println(state+" - - - - "+city);
                    });
                });
        System.out.println("======================================");
        stateList().stream()
                .filter(state-> state.startsWith("p")).forEach(state->{
                    cityList().stream().filter(city-> city.startsWith("m")).forEach(city->{
                        System.out.println(state+" - - - - "+city);
                    });
                });

    }

    private static List<String> stateList() {
        return Stream.of("punjab","pune","karnataka","kochin","Bihar").collect(Collectors.toList());
    }
    private static List<String> cityList() {
        return Stream.of("abc","def","ghi","jkl","mno","muyt").collect(Collectors.toList());
    }
}
