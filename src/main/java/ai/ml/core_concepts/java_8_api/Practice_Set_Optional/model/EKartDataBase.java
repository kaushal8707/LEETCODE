package ai.ml.core_concepts.java_8_api.Practice_Set_Optional.model;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EKartDataBase {

	public static List<Customer> getAll()
	{
		return Stream.of(
				new Customer(101, 12345, "john", "john@gmail.com", Arrays.asList("8797651423","9191928273")),
				new Customer(102, 76543, "smith", "smith@gmail.com", Arrays.asList("7676767671","6545654321")),
				new Customer(103, 91827, "piter", "piter@gmail.com", Arrays.asList("2435243142","1213121312")),
				new Customer(104, 76154, "kely", "kely@gmail.com", Arrays.asList("7766554434","8877665544"))
		  ).collect(Collectors.toList());
	}
} 
