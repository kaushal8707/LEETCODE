package ai.ml.core_concepts.java_features_8_11_16_17_21.java_21.pattern_matching_for_switches.usecase1.code.usecase2;

public class PaymentProcessor {
    public static String process(PaymentResult result) {
        // 3. Use exhaustive pattern matching in Java 21
        return switch (result) {
            case Success s -> "Paid $" + s.amount() + ". Transaction: " + s.transactionId();
            case Failure f -> "Error [" + f.errorCode() + "]: " + f.message();
            case Pending p -> "In progress... Please wait " + p.retryAfterMillis() + "ms";
            // No 'default' branch required because the hierarchy is sealed!
        };
    }
}