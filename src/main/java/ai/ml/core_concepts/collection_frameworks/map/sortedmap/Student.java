package ai.ml.core_concepts.collection_frameworks.map.sortedmap;

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

    @Override
    public int compareTo(Student o) {               //want to print (charlie, Bob) -> a-b-> + so it will shift right
                                                    // so we can do b-a
        return o.getName().compareTo(this.getName());
    }
}
