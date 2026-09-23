package ai.ml.leetcode.completablefuture.database;

import ai.ml.leetcode.completablefuture.model.Employee;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

public class EmployeeDataBase {
    public static List<Employee> fetchEmployee(){
        ObjectMapper objectMapper = new ObjectMapper();
        System.out.println(
                "fetch employee records : " +
                        Thread.currentThread().getName()
        );

        try {
            InputStream inputStream =
                    EmployeeDataBase.class
                            .getClassLoader()
                            .getResourceAsStream("employee.json");

            if (inputStream == null) {
                throw new FileNotFoundException(
                        "employee.json not found in src/main/resources"
                );
            }

            return objectMapper.readValue(
                    inputStream,
                    new TypeReference<List<Employee>>() {}
            );

        } catch (Exception e) {
            throw new RuntimeException("Unable to fetch employees", e);
        }
    }
}
