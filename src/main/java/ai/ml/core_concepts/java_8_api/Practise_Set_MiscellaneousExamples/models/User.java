package ai.ml.core_concepts.java_8_api.Practise_Set_MiscellaneousExamples.models;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class User
{
    private String userName;
    private int age;
    private float salary;

   
    public void showData()
    {
        System.out.println(userName+"  "+age);
    }
    public static void viewRecord()
    {
        System.out.println("inside static ref");
    }
}
