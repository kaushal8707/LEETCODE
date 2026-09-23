package ai.ml.core_concepts.collection_frameworks.map.weakhashmap;

import java.lang.ref.WeakReference;

public class GC {
    public static void main(String[] args) {

        Phone phone = new Phone("apple", "16 pro max");
        /** phone - is Strong Reference
         *
         * bcz directly we can access our object with this reference
         * */

        System.out.println(phone);
        phone = null;
        System.out.println(phone);

        /**
         * JVM will see this is the Weak Reference and also
         * its not getting used so JVM will remove
         *
         */
        WeakReference<Phone> phoneWeakReference =
                new WeakReference<>(new Phone("apple", "16 pro max"));
        System.out.println(phoneWeakReference.get());
        System.gc();
        try {
            Thread.sleep(10000);       // here after 10 sec also JVM is not remove weak reference memory
        } catch (Exception exception) {

        }
        System.out.println(phoneWeakReference.get());
    }
}

class Phone {

    String brand;
    String model;

    public Phone(String brand, String model) {
        this.brand = brand;
        this.model = model;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    @Override
    public String toString() {
        return "Phone{" +
                "brand='" + brand + '\'' +
                ", model='" + model + '\'' +
                '}';
    }
}