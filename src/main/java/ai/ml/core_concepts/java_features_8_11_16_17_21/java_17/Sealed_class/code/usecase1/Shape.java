package ai.ml.core_concepts.java_features_8_11_16_17_21.java_17.Sealed_class.code.usecase1;

// Shape.java
// The 'sealed' keyword restricts inheritance to the classes listed in the 'permits' clause
public sealed abstract class Shape permits Circles, Squares, Rectangles {
    // Common methods or properties can be defined here
    public abstract double calculateArea();
}
