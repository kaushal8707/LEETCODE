package ai.ml.core_concepts.java_8_api.Practise_Set_MiscellaneousExamples.models;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EmployeeDatabase {
    public static List<CurrentEmployee> getEmployees() {
        return Stream.of(
                new CurrentEmployee(312,"john", "A",60000),
                new CurrentEmployee(771,"peter", "B",30000),
                new CurrentEmployee(981,"mak", "A",90000),
                new CurrentEmployee(481,"cisn", "A",80000),
                new CurrentEmployee(123,"kuy","C",15000)
        ).collect(Collectors.toList());
    }
}
