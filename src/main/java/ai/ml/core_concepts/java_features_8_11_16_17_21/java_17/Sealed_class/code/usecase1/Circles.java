package ai.ml.core_concepts.java_features_8_11_16_17_21.java_17.Sealed_class.code.usecase1;

// Circle.java
// A permitted subclass can be declared 'final' (cannot be extended further)
public final class Circles extends Shape {
    private final double radius;

    public Circles(double radius) {
        this.radius = radius;
    }

    @Override
    public double calculateArea() {
        return Math.PI * radius * radius;
    }
}
