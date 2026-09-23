package ai.ml.core_concepts.java_features_8_11_16_17_21.java__16.records.code.usecase_3;

public record User(String name) implements printable{

    @Override
    public void print() {
        System.out.println("name - "+name);
    }

    public static void main(String[] args) {
        User user=new User("strd");
        user.print();
    }
}
