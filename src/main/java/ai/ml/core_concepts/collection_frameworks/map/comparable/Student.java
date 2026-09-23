package ai.ml.core_concepts.collection_frameworks.map.comparable;

public class Student implements Comparable<Student>{

    private String name;
    private double gpa;

    public Student(String name, double gpa) {
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
        return "Student{" +
                "name='" + name + '\'' +
                ", gpa=" + gpa +
                '}';
    }
    // 3.9, 3.7
    // 3.9-3.7  ->(o1, o2) -> o1-o2 -> +ve -> 3.7 come before and 3.9 go after but we don't want in ascending order
    // so here if return -Ve them 3.9 come before 3.

    @Override
    public int compareTo(Student o) {
        return Double.compare(o.getGpa(), this.getGpa());
    }
}
