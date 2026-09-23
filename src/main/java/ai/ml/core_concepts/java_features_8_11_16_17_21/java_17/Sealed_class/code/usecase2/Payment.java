package ai.ml.core_concepts.java_features_8_11_16_17_21.java_17.Sealed_class.code.usecase2;

public sealed interface Payment permits Paypal, CreditCard, Cash{
}
