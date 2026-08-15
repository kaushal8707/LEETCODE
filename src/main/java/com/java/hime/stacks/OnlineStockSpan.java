package com.java.hime.stacks;

import java.util.Stack;

public class OnlineStockSpan {
    static Stack<int[]> stack;
    OnlineStockSpan(){
        this.stack=new Stack<>();
    }
    private static int next(int price) {
        int span=1;
        while(!stack.isEmpty() && price > stack.peek()[0]){
            int popped = stack.pop()[1];
            span += popped;
        }
        stack.push(new int[]{price, span});
        return span;
    }
    public static void main(String[] args) {
        OnlineStockSpan stockSpan = new OnlineStockSpan();
        System.out.println(stockSpan.next(100));
        System.out.println(stockSpan.next(80));
        System.out.println(stockSpan.next(60));
        System.out.println(stockSpan.next(70));
        System.out.println(stockSpan.next(60));
        System.out.println(stockSpan.next(75));
        System.out.println(stockSpan.next(85));
    }
}
