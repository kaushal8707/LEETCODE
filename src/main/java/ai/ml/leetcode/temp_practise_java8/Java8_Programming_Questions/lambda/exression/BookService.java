package ai.ml.leetcode.temp_practise_java8.Java8_Programming_Questions.lambda.exression;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class BookService {

	public static void main(String[] args) {

		//Traditional way to sort Book In order to Name
		List<Book> listOfBooks = BookDao.getBooks();
		Collections.sort(listOfBooks, new BookComparatorTrad());
		listOfBooks.stream().forEach(System.out::print);
		System.out.println("=================================="); 
		
		
		//Anonymous Method way to sort Book In order to Name
		List<Book> listOfBook1 = BookDao.getBooks();
		Collections.sort(listOfBook1, new Comparator<Book>()
		{
			public int compare(Book b1, Book b2) {
				return b2.getName().compareTo(b1.getName());
			}
		});
		listOfBook1.stream().forEach(System.out::print);
		System.out.println("=================================="); 

		
		//Lambda Expression way to sort Book In order to Name
		List<Book> listOfBook2 = BookDao.getBooks();
		Collections.sort(listOfBook2,(b1, b2)->b1.getPages().compareTo(b2.getPages()));
		listOfBook2.stream().forEach(System.out::print);
		

	}

}
