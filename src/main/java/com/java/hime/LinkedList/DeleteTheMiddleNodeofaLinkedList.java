package com.java.hime.LinkedList;
// slow-fast pointer approach
public class DeleteTheMiddleNodeofaLinkedList {
    private static ListNode deleteMiddle(ListNode head) {
        ListNode prev=null;
        ListNode slow=head;
        ListNode fast=head;
        while(fast!=null && fast.next!=null){
            prev=slow;
            slow=slow.next;
            fast=fast.next.next;
        }
        prev.next=slow.next;
        return head;
    }
    private static void print_list_node(ListNode head){
        ListNode curr=head;
        while(curr!=null){
            System.out.print(curr.val+"  ");
            curr=curr.next;
        }
    }
    public static void main(String[] args) {
        ListNode head=new ListNode(1);
        ListNode node2=new ListNode(3);
        ListNode node3=new ListNode(4);
        ListNode node4=new ListNode(7);
        ListNode node5=new ListNode(1);
        ListNode node6=new ListNode(2);
        ListNode node7=new ListNode(6);
        head.next=node2;
        node2.next=node3;
        node3.next=node4;
        node4.next=node5;
        node5.next=node6;
        node6.next=node7;
        ListNode deleteMiddle = deleteMiddle(head);
        print_list_node(deleteMiddle);
    }
}
