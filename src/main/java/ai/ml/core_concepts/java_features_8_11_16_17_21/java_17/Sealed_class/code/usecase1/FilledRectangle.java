package ai.ml.core_concepts.java_features_8_11_16_17_21.java_17.Sealed_class.code.usecase1;

// FilledRectangle.java
// A subclass in the hierarchy must also be 'final', 'sealed', or 'non-sealed'
public final class FilledRectangle extends Rectangles {
    private final int color;

    public FilledRectangle(double length, double width, int color) {
        super(length, width);
        this.color = color;
    }
    // No need to override calculateArea() if not changing the logic
}
