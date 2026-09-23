package ai.ml.core_concepts.java_features_8_11_16_17_21.java__16.records.code.usecase_1;

public class Main {
    public static void main(String[] args) {

        User user = new User(111L,"kaushal singh");
        System.out.println("id - "+user.id());
        System.out.println("name - "+user.name());

//      user.name = "Hi";                     // compile-time error, can't re-instantiate because name is private and final
    }
}
