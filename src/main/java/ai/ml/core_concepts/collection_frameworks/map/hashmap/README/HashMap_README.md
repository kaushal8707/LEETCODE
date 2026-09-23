

* Map does not extends Collection.

* Map is a separate Interface.

* Hash Map not maintain Insertion order.
  

![img.png](img.png)


![img_1.png](img_1.png)


![img_2.png](img_2.png)


![img_3.png](img_3.png)


![img_4.png](img_4.png)


![img_5.png](img_5.png)





![img_6.png](img_6.png)

![img_7.png](img_7.png)

![img_8.png](img_8.png)

![img_9.png](img_9.png)

![img_10.png](img_10.png)

![img_11.png](img_11.png)


    Primary Purpose of Hash Function is :

        You can take an Input of any mo. of size In Output you will get a fixed sized output.


![img_12.png](img_12.png)




How Data is Stored in HashMap
-----------------------------

![img_15.png](img_15.png)

![img_16.png](img_16.png)

![img_17.png](img_17.png)

![img_18.png](img_18.png)




How Hash Map Retrieve Data
--------------------------

![img_19.png](img_19.png)

![img_20.png](img_20.png)

![img_21.png](img_21.png)

![img_22.png](img_22.png)

![img_23.png](img_23.png)

![img_24.png](img_24.png)

![img_25.png](img_25.png)

![img_26.png](img_26.png)

![img_27.png](img_27.png)

![img_28.png](img_28.png)

![img_29.png](img_29.png)

![img_30.png](img_30.png)

![img_31.png](img_31.png)

![img_32.png](img_32.png)

![img_33.png](img_33.png)

![img_35.png](img_35.png)

![img_34.png](img_34.png)




        we want to store a custom Object Student in Hash Map. we will create a class Student and that object

        we will store in HashMap object. 

        Here we saw in Example of Orange and Grape Collision occurred. 

        then we saw during Retrieval this linked list will get traversed and checked what we are trying to get
        that is present in our linkedlist or not. so we check using map.get("Grape"). then this linkedlist get traversed
        so first it will check the first Item is equal to Grape or not. if not then will go to next element and check
        either 2nd element is equal to grape or not If yes then we will return 20. so, How it get check?????

        >> The equality will be check with the help of equal() method.

        >> But If we have a class called Student with 2 fields id and name. and we have a HashMap of type Student and marks.


   ![img_36.png](img_36.png)


   ![img_37.png](img_37.png)

   ![img_38.png](img_38.png)

   so, here key is Student and value is Integer.

   Earlier In our HashMap as a key we were having Either String or Integer. but now we are having Custom object
   Student as a key.

   so, By default hashcode and equals method would be there.

   Because Every Class By Default Implements Object Class and Inside Object class both the methods hashcode and
   ------------------------------------------------------------------------------------------------------------
   equals method will be present.
   ------------------------------ 





 Let's see What is Role of hashcode inside an Object
====================================================

    <> hashcode will be generated for an object using Memory address.
    <> which means all the instances of Student class we create will have a different hashcode.
    <> equals also see the reference or address.
    <> so, here both hashcode & equals using memory address only let's see what would be the problem in this case.


   let's see the Problem -> 

       public class HashCodeAndEqualMethod {
       public static void main(String[] args) {
    
       HashMap<Student, String> map=new HashMap<>();
            Student s1=new Student("Alice", 1);
            Student s2=new Student("Bob", 2);
            Student s3=new Student("Alice", 1);  // here s1 is same student as s3

        /** now we have 3 objects but actually we have only 2 student */

        map.put(s1, "Engineer");  // hashcode --> index
        map.put(s2, "Designer");  // hashcode --> index
        map.put(s3, "Manager");   // hashcode --> index

        /** Now we want s3 which is Alice only his designation replace with manager
         * But will it work
         *
         * as we know hashcode will get generate from that we can find Index for a key, here key is student
         *
         * Because hashcode definition we have not given. Student extend Object class then inside Object class hashcode.
         *
         * so, with the help of Memory Address hashcode generated. bcz we have used here new keyword so hashcode will always be different.
         * so, both object will be place on a different Index(Bucket)
         * so, Inside Map there will be a 3 entries.
         * 
         *
         * */
        }
        }

   let's see another example ->

   ![img_40.png](img_40.png)

   
   Analysis :-

      we want actually in case of custom object as a key in HashMap, should generate the same hashcode as s1.
      the reason is hashcode for object is generating through Memory address intead of class fields that the reason
      its generating different for same object field. 


   ![img_41.png](img_41.png)

   >> Map size should be 2.
   >> Student value s1 should be Manager
 


  How we can tell Both the Object is same using override hashcode() & equals() method
-----------------------------------------------------------------------------------------

   we want every student should have a different hashcode and while checking equality check id and name both. 

   ![img_42.png](img_42.png)

      
      Inside Objects.hash() internally using
      
      Arrays.hashCode(values);

   ![img_43.png](img_43.png)

   ![img_44.png](img_44.png)

      It will make sure If Input is same then output(hashcode) will also be a same.

      equals() will call intern() method.

   ![img_45.png](img_45.png)

   -> so whenever using custom object in Hash map always make sure to override hashcode() and equals() method.

   -> let's take If we are using Set. In a Set there will be a Unique elements. In set if we add these 3 students objs
      all 3 get store but should store only 2. In that case we also have to write hashcode and equals methods.

  Hash Map Principle is Going to use in Hash Set.
  -----------------------------------------------




   Time Complexity
   ===============


   ![img_47.png](img_47.png)

   ![img_48.png](img_48.png)



   
      



