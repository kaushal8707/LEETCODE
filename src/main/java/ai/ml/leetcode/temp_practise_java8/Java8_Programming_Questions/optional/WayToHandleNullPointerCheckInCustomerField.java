package ai.ml.leetcode.temp_practise_java8.Java8_Programming_Questions.optional;
import java.util.List;
import java.util.Optional;

import lombok.AllArgsConstructor;
@AllArgsConstructor
public class WayToHandleNullPointerCheckInCustomerField {

	private int id;
	private String name;
	private String email;
	private List<String> phoneNumbers;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public Optional<String> getName() {
		return Optional.ofNullable(name);
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public Optional<String> getEmail() {
		return Optional.ofNullable(email); 
	}
	
	public void setEmail(String email) {
		this.email = email;
	}
	public List<String> getPhoneNumbers() {
		return phoneNumbers;
	}
	public void setPhoneNumbers(List<String> phoneNumbers) {
		this.phoneNumbers = phoneNumbers;
	}
	@Override
	public String toString() {
		return "Customer [id=" + id + ", name=" + name + ", email=" + email + ", phoneNumbers=" + phoneNumbers + "]";
	}
	
	
}
