package ai.ml.leetcode.break_and_prevent_singleton;
import java.lang.reflect.Constructor;
// This example shows how reflection can break the singleton pattern with Java reflect. 
// You will get two hash codes, as shown below. It has a break on the singleton pattern.

public class ReflectionSingleton {
public static void main(String[] args) {
    Singleton objOne = Singleton.getInstance();
    Singleton objTwo = null;
    try {
        Constructor constructor = Singleton.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        objTwo = (Singleton) constructor.newInstance();
    } catch (Exception ex) {
        System.out.println(ex);
    }
    System.out.println("Hashcode of Object 1 - "+objOne.hashCode());
    System.out.println("Hashcode of Object 2 - "+objTwo.hashCode());
 }		
}


// How To Prevent to Break Singleton??

// There are many ways to prevent Singleton pattern from Reflection API, but one of the best 
// solutions is to throw a run-time exception in the constructor if the instance already 
// exists. In this, we can not able to create a second instance.