package ai.ml.leetcode.hash_map.A_HashMap;

import java.util.ArrayList;
import java.util.LinkedList;

public class HashMap<K,V> {
    class Node {
        K key;
        V value;
        Node(K key, V value){
            this.key=key;
            this.value=value;
        }
    }
    //constructor initialisation
    private int n;  // n nodes
    private int N;  // N buckets
    LinkedList<Node> buckets[];
    HashMap(){
        N=4;
        this.buckets=new LinkedList[N];
        for(int i=0;i<N;i++){
            this.buckets[i] = new LinkedList<>();
        }
    }
    //Identify Bucket Index
    private int hashFunction(K key){
        int bi = key.hashCode();
        return Math.abs(bi) % N;
    }

    //remove operation
    private V remove(K key){
        int bi=hashFunction(key);
        int di = searchInLinkedList(key, bi);
        if(di==-1){
            return null;
        }else{
            Node node = buckets[bi].remove(di);
            return node.value;
        }
    }

    //Identify Data Index - search nodes in Map Linked List for availability
    private int searchInLinkedList(K key, int bi){
        LinkedList<Node> listInBucket = buckets[bi];
        for(int i=0;i<listInBucket.size();i++){
            if(listInBucket.get(i).key==key){
                return i;
            }
        }
        return -1;
    }

    //put operation
    private void put(K key, V value){
        int bi = hashFunction(key);
        int di = searchInLinkedList(key, bi);
        if(di==-1){
            buckets[bi].add(new Node(key, value));
        }else{  //key exist
            Node node = buckets[bi].get(di);
            node.value=value;
        }
    }
    //get operation
    private V get(K key){
        int bi = hashFunction(key);
        int di = searchInLinkedList(key, bi);
        if(di==-1){
            return null; //key not exist
        }else{
            Node node = buckets[bi].get(di);
            return node.value;
        }
    }

    //keySet operation
    private ArrayList<K> keySet(){
        ArrayList<K> keySet = new ArrayList();
        for(int i=0;i<buckets.length;i++){ //bi
            LinkedList<Node> nodes = buckets[i];
            for(int j=0;j<nodes.size();j++){ //di
                Node node = nodes.get(j);
                keySet.add(node.key);
            }
        }
        return keySet;
    }

    //contains key
    public boolean containsKey(K key){
        int bi = hashFunction(key);
        int di = searchInLinkedList(key, bi);
        if(di==-1){
            return false;
        }else{
            return true;
        }
    }

    public static void main(String[] args) {
        HashMap<String, Integer> map=new HashMap();
        map.put("India",111);
        map.put("China",222);
        map.remove("China");
        map.put("Russia", 333);
        ArrayList<String> keyedSet = map.keySet();
        for(String key : keyedSet){
            System.out.println(key+"   "+map.get(key));
        }
    }


}
