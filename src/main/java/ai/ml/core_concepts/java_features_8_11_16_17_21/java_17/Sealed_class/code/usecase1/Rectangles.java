package ai.ml.core_concepts.java_features_8_11_16_17_21.java_17.Sealed_class.code.usecase1;

// Rectangle.java
// A permitted subclass can also be 'sealed' itself, further restricting its own hierarchy
public sealed class Rectangles extends Shape permits FilledRectangle {
    private final double length;
    private final double width;

    public Rectangles(double length, double width) {
        this.length = length;
        this.width = width;
    }

    @Override
    public double calculateArea() {
        return length * width;
    }
}
