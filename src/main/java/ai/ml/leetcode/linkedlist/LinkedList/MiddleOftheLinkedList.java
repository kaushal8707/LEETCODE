package ai.ml.leetcode.linkedlist.LinkedList;

public class MiddleOftheLinkedList {
    ListNode head;
    private void insert_at_head(int data){
        ListNode node=new ListNode(data);
        node.next=head;
        head=node;
    }
    static ListNode middleNode(ListNode head) {
        ListNode slowptr=head;
        ListNode fastptr=head;
        while(fastptr!=null && fastptr.next!=null){
            slowptr=slowptr.next;
            fastptr=fastptr.next.next;
        }
        return slowptr;
    }
    public static void main(String[] args) {
       MiddleOftheLinkedList list=new MiddleOftheLinkedList();
       ListNode head=new ListNode(1);
       ListNode node2=new ListNode(2);
       ListNode node3=new ListNode(3);
       ListNode node4=new ListNode(4);
       ListNode node5=new ListNode(5);
       ListNode node6=new ListNode(6);

       head.next=node2;
       node2.next=node3;
       node3.next=node4;
       node4.next=node5;
       node5.next=node6;
       ListNode middleNode = middleNode(head);
       System.out.println("Middle of a Node in a Linked List is : "+middleNode.val);
    }
}
