package ai.ml.leetcode.temp_practise_java8.Java8_Programming_Questions.lambda.exression;

import java.util.Comparator;

public class BookComparatorTrad implements Comparator<Book> {
	public int compare(Book b1, Book b2) {
		return b1.getName().compareTo(b2.getName());
	}
}
