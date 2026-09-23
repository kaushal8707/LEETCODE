package ai.ml.leetcode.linkedlist.LinkedList;

/** Floyd's Tortoise and Hare Problem **/

public class LinkedListCycle {
    private boolean hasCycle(ListNode head) {

        if(head==null || head.next==null){
            return false;
        }
        ListNode slow=head;
        ListNode fast=head;

        while(fast!=null && fast.next!=null){
            slow=slow.next;
            fast=fast.next.next;

            if(slow==fast){
                return true;
            }
        }
        return false;
    }

    public static void main(String[] args) {
        ListNode head=new ListNode(3);
        ListNode node2=new ListNode(2);
        ListNode node3=new ListNode(0);
        ListNode node4=new ListNode(-4);

        head.next=node2;
        node2.next=node3;
        node3.next=node4;
        node4.next=node2;   // node4.next=null for non-cycle

        LinkedListCycle listCycle = new LinkedListCycle();
        boolean hasCycle = listCycle.hasCycle(head);
        System.out.println("Has Cycle : "+hasCycle);
    }
}
