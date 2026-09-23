package ai.ml.leetcode.temp_practise_java8.Java8_Programming_Questions.important;

public class EvenOddPrintThread implements Runnable{
	Object obj;
	static int i=1;
	public EvenOddPrintThread(Object obj) {
	 this.obj=obj;
	}

	@Override
	public void run() {
		while(i<=10) {
			
			if(i%2==0 && Thread.currentThread().getName().equals("Even Thread")) {
				synchronized(obj) {
					try {
						System.out.println(i+"-"+Thread.currentThread().getName());
						i++;
						obj.wait();

					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

			}
			if(i%2!=0 && Thread.currentThread().getName().equals("Odd Thread")) {
				synchronized(obj) {
					System.out.println(i+"-"+Thread.currentThread().getName());
					i++;
					obj.notify();
				}
			}
		}
	}
	
	public static void main(String[] args) {
		Object obj=new Object();
		EvenOddPrintThread r1=new EvenOddPrintThread(obj);
		EvenOddPrintThread r2=new EvenOddPrintThread(obj);
		Thread t1=new Thread(r1, "Even Thread");
		Thread t2=new Thread(r2, "Odd Thread");
		t1.start();
		t2.start();
	}
}
