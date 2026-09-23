package ai.ml.core_concepts.collection_frameworks.list.stack;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Stack;

public class Stack_Impl {
    public static void main(String[] args) {

        Stack<Integer> stack = new Stack<>();
        stack.push(1);
        stack.push(2);
        stack.push(3);
        stack.push(4);
        stack.push(5);
        System.out.println(stack);

        Integer removedElement = stack.pop();
        System.out.println(stack);

        Integer peekedElement = stack.peek();
        System.out.println("peek - "+peekedElement);
        System.out.println(stack);

        System.out.println(stack.isEmpty());
        System.out.println(stack.size());

        int search = stack.search(3);
        System.out.println(search);


        /** Linked List as Stack */
        LinkedList<Integer> linkedList = new LinkedList<>();
        linkedList.addLast(1);
        linkedList.addLast(2);  // as Push
        linkedList.addLast(3);
        linkedList.getLast();     // as Peek
        linkedList.removeLast();  // as Pop
        linkedList.size();
        linkedList.isEmpty();


        /** Array List as Stack */
        ArrayList<Integer> arrayList = new ArrayList<>();
        arrayList.add(1);
        arrayList.add(3);
        arrayList.add(5);

        arrayList.get(arrayList.size()-1);           //peek
        arrayList.remove(arrayList.size()-1);  // pop

    }
}
