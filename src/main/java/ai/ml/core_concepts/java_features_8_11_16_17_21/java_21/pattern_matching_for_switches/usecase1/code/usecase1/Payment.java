package ai.ml.core_concepts.java_features_8_11_16_17_21.java_21.pattern_matching_for_switches.usecase1.code.usecase1;

public sealed interface Payment permits Paypal, CreditCard, Cash {
}
