package com.java.hime.LinkedList;
import java.util.NoSuchElementException;

public class LinkedList {
    Node head;
    private void insert_at_head(int data){
        Node newNode = new Node(data);
        newNode.next=head;
        head=newNode;
    }
    private void print_data(){
        Node current=head;
        while(current!=null){
            System.out.print(current.data+"  ");
            current=current.next;
        }
        System.out.println();
    }
    private void delete_at_beginning(){
        if(head==null){
            System.out.println("list is empty");
            return;
        }
        head=head.next;
    }
    private boolean search(int key){
        if(head==null){
           throw new NoSuchElementException("List is Empty!!");
        }
        if(head.next==null && head.data==key){
            return true;
        }
        Node current=head;
        while(current!=null){
            if(current.data==key){
                return true;
            }
            current=current.next;
        }
        return false;
    }
    private void insert_at_end(int data){
        Node newNode=new Node(data);
        Node current=head;
        if(head==null) {
            head=newNode;
            return;
        }
        while(current.next!=null){
            current=current.next;
        }
        current.next=newNode;
    }
    private void insert_at_specific_position(int position, int data){
        Node newNode=new Node(101);
        Node current=head;
        if(position==1){
            insert_at_head(data);
            return;
        }
        if(current==null){
            System.out.println("Index out of bound");
            return;
        }
        for(int i=1;i<position-1;i++){
            current=current.next;
        }
        newNode.next=current.next;
        current.next=newNode;
    }
    private void delete_node(int data){
        boolean found=false;
        Node previous=null;
        Node current=head;
        if(head==null){
            System.out.println("empty");
            return;
        }
        if(head.data==data){
            delete_at_beginning();
            return;
        }
        while(current!=null){
            if(current.data==data){
                found=true;
                previous.next=current.next;
            }
            previous=current;
            current=current.next;
        }
        if(!found){
            System.out.println(data+" Not Found to delete ");
        }
    }
    private int size(){
        int count=0;
        Node curr=head;
        while(curr!=null){
            count++;
            curr=curr.next;
        }
        return count;
    }
    private void delete_specific_position(int position){
        if(position > size()){
            throw new IndexOutOfBoundsException("Position is Invalid for deletion!!");
        }
        if(position==1) {
            delete_at_beginning();
        }
        if(position==2){
            head.next=head.next.next;
        }
        Node curr=head;
        for(int i=1; i<position-1; i++){
            curr=curr.next;
        }
        curr.next=curr.next.next;
    }
    public static void main(String[] args) {
       LinkedList linkedList=new LinkedList();
       linkedList.insert_at_head(11);
        linkedList.insert_at_head(12);
        linkedList.insert_at_head(13);
        linkedList.delete_at_beginning();
        linkedList.insert_at_head(14);
        linkedList.insert_at_end(18);
        System.out.println(linkedList.search(14));
        linkedList.insert_at_specific_position(3,101);
        linkedList.delete_node(29);
        linkedList.delete_specific_position(5);
        linkedList.print_data();
        System.out.println("size-"+linkedList.size());
    }
}
