package ai.ml.leetcode.collection_codes;

public class Employee {
	private Integer id;
	private String name;
	
	//let's Implement hashcode() and equals() methods
	
	@Override
	public boolean equals(Object obj){
		if(this==obj) {
			return true;
		}
		if(obj==null) {
			return false;
		}
		if(this.getClass()!=obj.getClass()) {
			return false;
		}

		Employee emp = (Employee) obj;
		return (this.id==emp.id || this.id.equals(emp.id))&&
				(this.name==emp.name || this.name.equals(emp.name));
	}

	@Override
	public int hashCode() {
		int hash = (this.id.hashCode() + this.name.hashCode());
		return hash;
	}

	public Employee(Integer id, String name) {
		this.id = id;
		this.name = name;
	}

	@Override
	public String toString() {
		return "Employee{" +
				"id=" + id +
				", name='" + name + '\'' +
				'}';
	}
}
