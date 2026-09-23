package ai.ml.leetcode.completablefuture.combining.database;

import ai.ml.leetcode.completablefuture.combining.dto.Employee;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class EmployeeDatabase {

	public static List<Employee> fetchEmployee(){
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			InputStream inputStream = EmployeeDatabase.class .getClassLoader()
					.getResourceAsStream("employee.json");
			if (inputStream == null) {
				throw new FileNotFoundException( "employee.json not found in src/main/resources" );
			}
			return objectMapper.readValue( inputStream, new TypeReference<List<Employee>>() {} );
		} catch (Exception e) {
			throw new RuntimeException( "Failed to load employee.json", e );
		}
	}

}
