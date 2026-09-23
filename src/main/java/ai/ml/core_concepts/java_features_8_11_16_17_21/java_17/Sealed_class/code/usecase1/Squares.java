package ai.ml.core_concepts.java_features_8_11_16_17_21.java_17.Sealed_class.code.usecase1;

// Square.java
// A permitted subclass can be declared 'non-sealed' (can be extended by any class)
public non-sealed class Squares extends Shape {
    private final double side;

    public Squares(double side) {
        this.side = side;
    }

    @Override
    public double calculateArea() {
        return side * side;
    }
}
