package ai.ml.core_concepts.java_8_api.Practice_Set_Optional;

import com.zero.to.hero.java_8_api.Practice_Set_Optional.model.Customer;
import com.zero.to.hero.java_8_api.Practice_Set_Optional.model.EKartDataBase;

public class PracticeSet_2 {
    public static void main(String[] args) throws Exception {

        /** orElse & orElseThrow demo **/
        Customer orElseCheck = getCustomerByEmailPart2("def@gmail.com");
        System.out.println("or Else Check : "+orElseCheck);
        Customer orElseThrow = getCustomerByEmail("abc@gmail.com");
        System.out.println("or Else Throw Check : "+orElseThrow);
    }

    private static Customer getCustomerByEmail(String email) throws Exception {
        return EKartDataBase.getAll().stream()
                .filter(customer-> email.equals(customer.getEmail())).findFirst().orElseThrow(()-> new Exception("Email Not Found"));
    }
    private static Customer getCustomerByEmailPart2(String email){
        return EKartDataBase.getAll().stream()
                .filter(customer-> email.equals(customer.getEmail())).findFirst().orElse(new Customer());
    }
}
