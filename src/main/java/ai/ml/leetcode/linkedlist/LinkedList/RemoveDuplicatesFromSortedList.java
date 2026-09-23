package ai.ml.leetcode.linkedlist.LinkedList;

public class RemoveDuplicatesFromSortedList {

    private static ListNode deleteDuplicates(ListNode head) {
        //if we have only one node or no node so no duplicates can be found
        if(head==null || head.next==null){
            return head;
        }
        ListNode curr=head;
        while(curr!=null && curr.next!=null){
            if(curr.val == curr.next.val){
                curr.next = curr.next.next;
            }else{
                curr=curr.next;
            }
        }
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
        ListNode head = new ListNode(1, new ListNode(1, new ListNode(2)));
        ListNode deleteDuplicates = deleteDuplicates(head);
        print_list_node(deleteDuplicates);
        System.out.println();

        ListNode head1 = new ListNode(1, new ListNode(1, new ListNode(2, new ListNode(3, new ListNode(3)))));
        System.out.println("---------Before-------------");
        print_list_node(head1);
        System.out.println();
        ListNode deleteDuplicates1 = deleteDuplicates(head1);
        System.out.println("---------After-------------");
        print_list_node(deleteDuplicates1);
        System.out.println();

    }
}
