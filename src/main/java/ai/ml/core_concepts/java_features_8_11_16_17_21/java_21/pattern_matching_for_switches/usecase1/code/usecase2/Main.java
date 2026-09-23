package ai.ml.core_concepts.java_features_8_11_16_17_21.java_21.pattern_matching_for_switches.usecase1.code.usecase2;

import java.util.HashSet;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        PaymentResult result= new Success("IAGST5#D$", 16253.11);
        String process = PaymentProcessor.process(result);
        System.out.println("payment processed result - "+process);
    }
}
