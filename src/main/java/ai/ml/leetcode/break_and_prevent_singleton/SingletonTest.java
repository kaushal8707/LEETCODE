package ai.ml.leetcode.break_and_prevent_singleton;

public class SingletonTest {
    public static void main(String[] args) {
        Singleton object1 = Singleton.getInstance();
        Singleton object2 = Singleton.getInstance();
        System.out.println("Hashcode of Object 1 - " + object1.hashCode());
        System.out.println("Hashcode of Object 2 - " + object2.hashCode());
    }

}
