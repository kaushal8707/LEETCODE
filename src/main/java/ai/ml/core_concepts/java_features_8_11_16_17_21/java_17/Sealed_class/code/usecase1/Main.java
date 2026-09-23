package ai.ml.core_concepts.java_features_8_11_16_17_21.java_17.Sealed_class.code.usecase1;

// Main.java
public class Main {
    public static void main(String[] args) {
        // Create instances of the permitted subclasses
        Shape circle = new Circles(5);
        Shape square = new Squares(4);
        Shape rectangle = new Rectangles(3, 6);
        Shape filledRectangle = new FilledRectangle(2, 4, 0xFF0000);

        Shape shape = square;
        // Use them polymorphically
        System.out.println("Circle area: " + circle.calculateArea());
        System.out.println("Square area: " + square.calculateArea());
        System.out.println("Rectangle area: " + rectangle.calculateArea());
        System.out.println("Filled Rectangle area: " + filledRectangle.calculateArea());

        // This pattern is especially useful with switch expressions, where the compiler
        // can ensure all permitted subclasses are handled, requiring no 'default' case
        String shapeType = switch (shape) {
            case Circles c -> "It's a circle";
            case Squares s -> "It's a square";
            case FilledRectangle fr -> "It's a filled rectangle";
            case Rectangles r -> "It's a general rectangle";
        };
        System.out.println(shapeType);
    }
}
