package com.java.hime.LinkedList;

public class SwapNodesInPairs {

    private static ListNode swapPairs(ListNode head) {
        if(head==null || head.next==null){
            return head;
        }

        ListNode dummy = new ListNode(0);
        dummy.next=head;
        ListNode prev=dummy;
        ListNode curr=head;
        while(curr!=null && curr.next!=null){
            ListNode first = curr;
            ListNode second = curr.next;

            prev.next = second;
            first.next = second.next;
            second.next = first;

            prev = first;
            curr = first.next;
        }
        return dummy.next;
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
        ListNode node2=new ListNode(2);
        ListNode node3=new ListNode(3);
        ListNode node4=new ListNode(4);
        head.next=node2;
        node2.next=node3;
        node3.next=node4;
        ListNode swapPairs = swapPairs(head);
        print_list_node(swapPairs);
        System.out.println();

        //when we have 3 nodes
        ListNode head1=new ListNode(1);
        ListNode node_2=new ListNode(2);
        ListNode node_3=new ListNode(3);
        head1.next=node_2;
        node_2.next=node_3;
        ListNode swapPairs1 = swapPairs(head1);
        print_list_node(swapPairs1);
        System.out.println();

        //when we have a single node
        ListNode head11=new ListNode(1);
        ListNode swapPairs11 = swapPairs(head11);
        print_list_node(swapPairs11);
        System.out.println();

        //when no node
        ListNode head111=null;
        ListNode swapPairs111 = swapPairs(head111);
        print_list_node(swapPairs111);
        System.out.println();
    }
}
