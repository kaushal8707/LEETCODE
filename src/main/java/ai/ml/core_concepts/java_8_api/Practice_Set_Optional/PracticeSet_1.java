package ai.ml.core_concepts.java_8_api.Practice_Set_Optional;

import com.zero.to.hero.java_8_api.Practice_Set_Optional.model.Employee;

import java.util.Optional;

public class PracticeSet_1 {
    public static void main(String[] args) {
        /** there are 3 ways to create an Optional Object
             empty()   -  	       It will simply return an Empty Optional Object
             of()		-  	           when we sure that the object which is passing to the of() method is Not Null then only we should go for Optional.of() method otw it will throw Null Pointer Exception
             ofNullable()	-      If the Object is Null Return the empty Optional object instead of throwing the Null Pointer Exception, If the value Presents then just return the value
                                       - So, ofNullable() is a combination of both empty () and of () method
             orElse()       -  return defaut value in case valu is null in ofNullable parameter if not null then give actual value
             orElse () and orElseGet () both are similar only difference is orElse just take input as String but orElseGet () will take input as a Supplier
             orElseGet () and orElseThrow () both will take input as a Supplier
         **/
        Employee employee=new Employee(111,"kaushal",null,14252.12);
        Optional emptyOptional = Optional.empty();
        System.out.println("empty() optional : "+emptyOptional);
        Optional<Integer> ofOptionalInteger = Optional.of(employee.getId());
        System.out.println("of Optional in case value is present : "+ofOptionalInteger.get());
        Optional<Integer> ofNullableForNotNull = Optional.ofNullable(employee.getId());                              //in case not null will return value
        System.out.println("ofNullable in case value is not null : "+ofNullableForNotNull.get());
        Optional<String> ofNullableForNull = Optional.ofNullable(employee.getDept());                              //in case null won't throw null-pointer-exception will return EMPTY
        System.out.println("ofNullable in case value is null : "+ofNullableForNull);
        //Optional<String> ofOptionalString = Optional.of(employee.getDept());                                         //in case null throw null-pointer-exception
        //System.out.println("of Optional in case value is null : "+ofOptionalString);

        String defaulDept = Optional.ofNullable(employee.getDept()).orElse("department@gmail.com");
        System.out.println("default department in case department is null  : "+defaulDept);
        Integer idForOrElseCheck = Optional.ofNullable(employee.getId()).orElse(11111);
        System.out.println("actual value  in case id is not null  : "+idForOrElseCheck);

        String departmentNotFoundOrElseGet = Optional.ofNullable(employee.getDept()).orElseGet(() ->"Department Not Found");
        System.out.println("or else get in case value provided is null : "+departmentNotFoundOrElseGet);
        String departmentNotFoundOrElseThrow = Optional.ofNullable(employee.getDept()).orElseThrow(() -> new IllegalArgumentException("Department Not Found"));
        System.out.println("or else throw in case value provided is null : "+departmentNotFoundOrElseThrow);


    }
}
