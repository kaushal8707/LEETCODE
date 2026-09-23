package ai.ml.leetcode.linkedlist.LinkedList;

public class RemoveNthNodeFromEndofList {
    private static ListNode removeNthFromEnd(ListNode head, int n) {   // n=2 >> second last node need to be removed
        ListNode fast=head;
        ListNode slow=head;

        for(int i=0;i<n;i++){
            fast=fast.next;
        }
        //edge case when u have only 2 nodes and want to delete 2nd last node
        if(fast==null){
            return head.next;
        }
        //when you have only one node and want to remove last node
        if(slow.next==null){
            return null;
        }

        while( fast!=null && fast.next!=null){
            fast=fast.next;
            slow=slow.next;
        }
        slow.next=slow.next.next;
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
        int pos=3;
        ListNode head=new ListNode(1);
        ListNode node2=new ListNode(2);
        ListNode node3=new ListNode(3);
        ListNode node4=new ListNode(4);
        ListNode node5=new ListNode(5);

        head.next=node2;
        node2.next=node3;
        node3.next=node4;
        node4.next=node5;
        ListNode listNode = removeNthFromEnd(head, pos);
        print_list_node(listNode);
        System.out.println();

        //Handling Edge Case
        //If you have 2 nodes and want to remove last node
        int pos_1=2;
        ListNode head_1=new ListNode(1);
        ListNode node_2=new ListNode(2);
        head_1.next=node_2;
        ListNode listNode_1 = removeNthFromEnd(head_1, pos_1);
        print_list_node(listNode_1);
        System.out.println();

        //Handling Edge Case
        //If you have 1 node and want to remove last node
        int pos_2=1;
        ListNode head_2=new ListNode(1);
        ListNode listNode_2 = removeNthFromEnd(head_2, pos_2);
        print_list_node(listNode_2);

    }
}
