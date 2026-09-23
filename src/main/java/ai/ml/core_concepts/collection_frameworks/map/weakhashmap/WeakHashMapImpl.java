package ai.ml.core_concepts.collection_frameworks.map.weakhashmap;

import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

public class WeakHashMapImpl {
    public static void main(String[] args) {

        /** Example 1
         *
         * key we are having strong reference bcz
         * we are using string literals
         * so won't be garbage collected throughout the program
         *
         * */
        WeakHashMap<String, Image> imageCache1=new WeakHashMap<>();
        imageCache1.put("img1", new Image("Image 1"));
        imageCache1.put("img2", new Image("Image 2"));

        System.out.println(imageCache1);

        System.gc();
        simulateApplicationRunning();

        System.out.println("Cache after running(some entries may be cleared.."+imageCache1);


        System.out.println("===========================================");

        /** Example 2
         *
         * key we are having weak reference bcz of null
         * we are using non literals
         * so will be garbage collected bcz of weak key reference
         *
         * */
        WeakHashMap<String, Image> imageCache2=new WeakHashMap<>();

        String key1=new String("img1");
        String key2=new String("img2");

        imageCache2.put(key1, new Image("Image 1"));
        imageCache2.put(key2, new Image("Image 2"));

        System.out.println(imageCache2);
        key1=null;
        key2=null;
        System.gc();
        simulateApplicationRunning();

        System.out.println("Cache after running(some entries may be cleared.."+imageCache2);
    }

    private static void simulateApplicationRunning() {
        System.out.println("simulating application running....");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}


class Image {

    private String name;
    Image(String name){
        this.name=name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Image{" +
                "name='" + name + '\'' +
                '}';
    }
}