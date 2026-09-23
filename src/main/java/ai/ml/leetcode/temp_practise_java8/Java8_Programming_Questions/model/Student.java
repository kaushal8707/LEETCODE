package ai.ml.leetcode.temp_practise_java8.Java8_Programming_Questions.model;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
@Builder
@NoArgsConstructor
@Getter
@AllArgsConstructor
public class Student {
	private int id;
	private String name;
	private String address;
	private double fee;
}
