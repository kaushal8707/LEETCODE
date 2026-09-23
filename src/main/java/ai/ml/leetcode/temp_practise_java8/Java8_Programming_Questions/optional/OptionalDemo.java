package ai.ml.leetcode.temp_practise_java8.Java8_Programming_Questions.optional;

import ai.ml.leetcode.temp_practise_java8.Java8_Programming_Questions.model.Customer;

import java.util.Arrays;
import java.util.Optional;

public class OptionalDemo {
	public static void main(String[] args) throws Throwable {
		//there are 3 ways to create an Optional Object 
				//empty()   -  	it will simply return an Empty Optional Object
				//of()		-  	when we sure that the object which is passing to the of() method is Not Null then only we should go for Optional.of() method otw it will throwing Null Pointer Exception
				//ofNullable()	- If the Object is Null Return the empty Optional object instead of throwing the Null Pointer Exception, If the value Presents then just print the value
				//				- So, ofNullable() is a combination of both empty () and of () method
				
				
				Customer customer=new Customer(101, 617262, "john", null, Arrays.asList("8797651423","9191928273"));
				//Customer customer=new Customer(101, "john", "kk@gmail.com", Arrays.asList("8797651423","9191928273"));

				
				
				//empty()
				Optional emptyOptional = Optional.empty();
				System.out.println(emptyOptional);
				
				//of()        ->   will throw NullPointerException when gmail is null
//				Optional<String> emailOptional = Optional.of(customer.getEmail());
//				System.out.println(emailOptional);
				
				//ofNullable()   ->   will give EMPTY Optional object if value is null otw will return exact value
				Optional<String> emailOptional2 = Optional.ofNullable(customer.getEmail());
				if(emailOptional2.isPresent()) {
					System.out.println(emailOptional2.get()); 
				}
				
				//orElse
				System.out.println(emailOptional2.orElse("default@gmail.com"));  //when email is null
				
				//orElseThrow    -> it take Supplier as an Input
				//System.out.println(emailOptional2.orElseThrow(()->new IllegalArgumentException("Email Not Found")));
				
				//orElse () and orElseGet () both are similar only difference is orElse just take input as String but orElseGet () will take input as a Supplier
				System.out.println(emailOptional2.map(String::toUpperCase).orElseGet(()-> "default email")); 
				
				//orElseGet () and orElseThrow () both will take input as a Supplier
				//now let’s add some value in Gmail and see how its behaving…if value is present then orElseGet () method
				//won’t execute and it will print original value…
	}
}
