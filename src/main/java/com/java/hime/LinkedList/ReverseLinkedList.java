package com.java.hime.LinkedList;

public class ReverseLinkedList {
    public static void main(String[] args) {
        ListNode head=new ListNode(1);
        ListNode node2=new ListNode(2);
        ListNode node3=new ListNode(3);
        ListNode node4=new ListNode(4);
        ListNode node5=new ListNode(5);

        head.next=node2;
        node2.next=node3;
        node3.next=node4;
        node4.next=node5;
        ListNode reversedList = reverseList(head);
        print_list_node(reversedList);
        System.out.println();

        // when we are having only 2 nodes in a list
        ListNode head1=new ListNode(1);
        ListNode node_2=new ListNode(2);
        head1.next=node_2;
        ListNode reversedList1 = reverseList(head1);
        print_list_node(reversedList1);
        System.out.println();
    }
    private static void print_list_node(ListNode head){
        ListNode curr=head;
        while(curr!=null){
            System.out.print(curr.val+"  ");
            curr=curr.next;
        }
    }
    private static ListNode reverseList(ListNode head) {
        if(head == null){
            return null;
        }
        ListNode nextNode = null;
        ListNode prev = null;
        ListNode curr = head;
        while(curr!=null){
            nextNode = curr.next;
            curr.next = prev;
            prev = curr;
            curr = nextNode;
        }
        return prev;
    }
}
