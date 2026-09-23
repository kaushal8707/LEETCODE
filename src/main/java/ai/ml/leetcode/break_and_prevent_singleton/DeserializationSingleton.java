package ai.ml.leetcode.break_and_prevent_singleton;

import java.io.*;

// In serialization, we can save the object of a byte stream into a file or send over a network
// Suppose if you serialize the Singleton class, and then again de-serialize that object, it 
// will create a new instance, hence deserialization will break the Singleton pattern.

public class DeserializationSingleton {
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		String filePath="C://Users/kaukumar3/Desktop/INTERVIEW PREPARATION/file.text";
		Singleton instanceOne = Singleton.getInstance();
		FileOutputStream fileoutStream=new FileOutputStream(filePath);
		ObjectOutput out=new ObjectOutputStream(fileoutStream);
		out.writeObject(instanceOne);
		out.close();
		
		FileInputStream fileInputStream=new FileInputStream(filePath);
		ObjectInput input=new ObjectInputStream(fileInputStream);
		Singleton instanceTwo=(Singleton) input.readObject();
		input.close();
		
        System.out.println("hashCode of instance 1 is - " + instanceOne.hashCode());
        System.out.println("hashCode of instance 2 is - " + instanceTwo.hashCode());

	}
}

// How To Prevent to Break Singleton??

/* To overcome this issue, we need to override readResolve() method in the Singleton class and 
    return the same Singleton instance so, Update Singleton.java with below method

	protected Object readResolve() { 
        return instance; 
  }  

*/
