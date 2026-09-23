package ai.ml.core_concepts.java_8_api.Practise_Set_MiscellaneousExamples;
import com.zero.to.hero.java_8_api.Practise_Set_MiscellaneousExamples.models.User;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class PractiseSet_4 {
    public static void main(String[] args) {
        //list-Q1 find the person name who is smallest in age
        String personSmallestInAge = getUserList().stream()
                .min(Comparator.comparing(User::getAge))
                .map(User::getUserName).get();
        System.out.println("person smallest in age : "+personSmallestInAge);

        //list-Q2 find the person whose salary is highest
        String highestSallaryPerson = getUserList().stream().max(Comparator.comparing(User::getSalary)).map(User::getUserName).get();
        System.out.println("person whose salary is highest "+ highestSallaryPerson);
        String highestSalariedPerson = getUserList().stream().sorted((a, b) -> (int) (b.getSalary() - a.getSalary())).map(User::getUserName).findFirst().get();
        System.out.println("highest salaried user : "+highestSalariedPerson);

        //map-Q4 find the person name who is highest in age
        String oldAgePerson = getUserMap().entrySet().stream()
                .max(Comparator.comparing(entry -> entry.getKey().getAge()))
                .map(entry -> entry.getKey().getUserName()).get();
        System.out.println("person oldest in age : "+oldAgePerson);

        //map-find the person name whose salary is lowest
        String lowestSalariedEmployee = getUserMap().entrySet().stream()
                .min(Map.Entry.comparingByKey(Comparator.comparing(user -> user.getSalary()))).get().getKey().getUserName();
        System.out.println("lowest salaried employee : "+lowestSalariedEmployee);
    }

    private static List<User> getUserList() {
        return Arrays.asList(new User("kaushal",33,11423.22f), new User("nagraju",77, 657113.22f),
                new User("columbus",82,44171.11f), new User("Monto",52,445615.112f));
    }

    private static Map<User, Integer> getUserMap() {
        return Map.of(new User("kaushal",33,11423.22f), 55,
                new User("nagraju",77, 657113.22f),33,
                new User("columbus",82,44171.11f), 44,
                new User("Monto",52,445615.112f), 11);
    }
}
