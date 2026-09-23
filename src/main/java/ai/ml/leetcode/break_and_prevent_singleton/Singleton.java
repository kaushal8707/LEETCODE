package ai.ml.leetcode.break_and_prevent_singleton;

import java.io.Serializable;

public class Singleton implements Serializable, Cloneable{
    private static Singleton instance = null;
    private Singleton() {
    }

    public static Singleton getInstance() {
        if (instance == null) {
            synchronized (Singleton.class) {
                if (instance == null) {
                    instance = new Singleton();
                }
            }
        }
        return instance;
    }
    
    @Override
    protected Object clone() throws CloneNotSupportedException  {
        return super.clone();
    }

}

