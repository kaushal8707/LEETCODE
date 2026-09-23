package ai.ml.core_concepts.java_8_api.Practice_Set_Optional.model;
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
