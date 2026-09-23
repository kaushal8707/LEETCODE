package ai.ml.leetcode.temp_practise_java8.Java8_Programming_Questions.model;
import lombok.AllArgsConstructor; 
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
 
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Book
{ 
    private String bookName;
    private Integer price;

    @Override
    public boolean equals(Object obj)
    {
        if(obj==null) //If obj is null, return without comparing obj & Book class.
            return false;
        if(this.getClass()!=obj.getClass()) //identifies whether obj is instance of Book class or not.
            return false;
        Book book= (Book) obj;  //type cast obj into book instance.
        return (book.getBookName()==this.getBookName() || book.getBookName().equals(this.getBookName())
         && book.getPrice()==this.getPrice() || book.getPrice().equals(this.getPrice()));
    }

    @Override
    public int hashCode()
    {
        int hash=(this.bookName==null ? 0:this.bookName.hashCode())+
                (this.price==null ? 0:this.price.hashCode());
        return hash;
    }
    
    public void print() {
    	System.out.println("Name = "+bookName+"   Price = "+price);
    }
}
