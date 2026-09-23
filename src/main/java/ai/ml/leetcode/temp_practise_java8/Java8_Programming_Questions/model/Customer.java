package ai.ml.leetcode.temp_practise_java8.Java8_Programming_Questions.model;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
@AllArgsConstructor
@ToString
@NoArgsConstructor
@Getter
public class Customer {

	private int id;
	private long salary;
	private String name;
	private String email;
	private List<String> phoneNumbers;
}
