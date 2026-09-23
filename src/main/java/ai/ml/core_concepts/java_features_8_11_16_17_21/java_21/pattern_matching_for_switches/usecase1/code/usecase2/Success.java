package ai.ml.core_concepts.java_features_8_11_16_17_21.java_21.pattern_matching_for_switches.usecase1.code.usecase2;

// 2. Define the permitted implementations (Records work great here)
record Success(String transactionId, double amount) implements PaymentResult { }