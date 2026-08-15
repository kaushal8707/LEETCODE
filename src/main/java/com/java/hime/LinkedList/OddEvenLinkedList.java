package com.java.hime.LinkedList;

public class OddEvenLinkedList {

    private static ListNode oddEvenList(ListNode head) {
        ListNode odd = head;
        ListNode even = odd.next;
        ListNode evenHead = even;
        while(even!= null && even.next!=null){
            odd.next = even.next;
            odd = odd.next;

            even.next = odd.next;
            even = even.next;
        }
        odd.next = evenHead;
        return head;
    }

    private static void print(ListNode head) {
        ListNode curr = head;
        while(curr != null){
            System.out.print(curr.val+"->");
            curr = curr.next;
        }
    }

    public static void main(String[] args) {   // Create a sample linked list: 2 -> 1 -> 3 -> 5 -> 6 -> 4 -> 7

        OddEvenLinkedList linkedList = new OddEvenLinkedList();
        ListNode head = new ListNode(2);
        head.next = new ListNode(1);
        head.next.next = new ListNode(3);
        head.next.next.next = new ListNode(5);
        head.next.next.next.next = new ListNode(6);
        head.next.next.next.next.next = new ListNode(4);
        head.next.next.next.next.next.next = new ListNode(7);
        System.out.println("Original Linked List");
        linkedList.print(head);

        linkedList.oddEvenList(head);

        System.out.println("Modified Odd Even Linked List");
        linkedList.print(head);
    }
}
