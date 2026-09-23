package ai.ml.core_concepts.java_features_8_11_16_17_21.java__16.records.code.usecase_2;

public record Rectangle(int width, int height) {

    public int area() {
        return width * height;
    }

    public static void main(String[] args) {
        Rectangle rectangle=new Rectangle(3,4);
        System.out.println(rectangle.area());
    }
}
