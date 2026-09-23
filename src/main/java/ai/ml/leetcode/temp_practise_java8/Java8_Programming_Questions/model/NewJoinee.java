package ai.ml.leetcode.temp_practise_java8.Java8_Programming_Questions.model;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class NewJoinee {
	private String name;
	private String address;
	private double fee;
}
