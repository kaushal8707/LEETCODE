package ai.ml.core_concepts.collection_frameworks.map.sortedmap;

public class Person{

    private String name;
    private double gpa;

    public Person(String name, double gpa) {
        this.name = name;
        this.gpa = gpa;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getGpa() {
        return gpa;
    }

    public void setGpa(double gpa) {
        this.gpa = gpa;
    }

    @Override
    public String toString() {
        return "Person{" +
                "name='" + name + '\'' +
                ", gpa=" + gpa +
                '}';
    }
}
