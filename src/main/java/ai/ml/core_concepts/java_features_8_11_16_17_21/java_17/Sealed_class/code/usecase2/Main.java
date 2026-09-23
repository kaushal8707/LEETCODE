package ai.ml.core_concepts.java_features_8_11_16_17_21.java_17.Sealed_class.code.usecase2;

import java.util.Random;

public class Main {
    public static void main(String[] args) {
        Payment payment = new Paypal("pypl@mail.com");
        CreditCard card=new CreditCard("123456789");

        String paymentSummary = getPaymentSummary(payment);
        System.out.println(paymentSummary);
    }

    // 2. Use in a switch expression
    public static String getPaymentSummary(Payment payment) {
        return switch (payment) {
            case Paypal paypal -> "Paid with Paypal : "+paypal.email();
            case CreditCard card -> "Paid with Card :"+ card.creditCardNumber();
            case Cash cash -> "Paid with Cash";
        };
    }
}
