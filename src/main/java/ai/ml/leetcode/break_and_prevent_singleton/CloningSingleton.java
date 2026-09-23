package ai.ml.leetcode.break_and_prevent_singleton;

public class CloningSingleton {

	public static void main(String[] args) throws CloneNotSupportedException {
		Singleton instanceOne = Singleton.getInstance();
        Singleton instanceTwo = (Singleton) instanceOne.clone();
        System.out.println("hashCode of instance 1 - " + instanceOne.hashCode());
        System.out.println("hashCode of instance 2 - " + instanceTwo.hashCode());
	}

}

// How To Prevent to Break Singleton??
/* In the above code, it breaks the Singleton principle, i.e created two instances. To overcome
   the above issue, we need to implement/override the clone() method and throw an exception 
   CloneNotSupportedException from the clone method. If anyone tries to create a clone object 
   of Singleton, it will throw an exception, as shown in the below code.
 

	@Override
	protected Object clone() throws CloneNotSupportedException  {
    	throw new CloneNotSupportedException();
}

*/