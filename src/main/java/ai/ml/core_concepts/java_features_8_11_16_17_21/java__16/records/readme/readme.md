
# What is Records in Java ??

![img.png](img.png)

![img_2.png](img_2.png)

java internally convert into 

![img_1.png](img_1.png)

- There is no setter because it is immutable 
- Java will create a final class and extends that class with java.lang.Record
- Java will take input parameters and convert all into final variables
- Java will create all arguments constructor so that it can instantiate
- Java will create a getter with fields name, without get keyword with no setters


# Why Records is Introduced???

- because there is so many of a boiler code all that replace with a single line.
- This is how java removes all the boilerplate codes 
- Records solves Data Carrier Classes Problems


# Key Properties of Records

1. Immutable 
        classes and fields are final.
        you can not modify them because all are final in nature
    ![img_3.png](img_3.png)


2. Records are Implicitly final so you can't extend a record.

    ![img_4.png](img_4.png)

3. All records internally extend java.lang.Record and you can't change it
    and hence you can not extend any other class because by default it is already extends it,
    but you can implement n number of interfaces.

    ![img_5.png](img_5.png)

4. Difference between class and records

    records - Data Carrier, It carries the data usages around, nest usecase is request and response body never get change throughout an application.
                 those classes which is never going to change can use as a records.


# Interview Traps

# 1. Is Records is completely Immutable? If yes then it is shallow or deep Immutable??

   ![img_7.png](img_7.png)

   ![img_8.png](img_8.png)

   ![img_9.png](img_9.png)

   Important part is How we are instantiating the variables.

   ![img_11.png](img_11.png)

   ![img_13.png](img_13.png)

   ![img_12.png](img_12.png)

   ![img_14.png](img_14.png)

   ![img_15.png](img_15.png)

   ![img_16.png](img_16.png)

   ![img_17.png](img_17.png)

   ![img_18.png](img_18.png)

   This is just a Shallow Immutability.

   ![img_19.png](img_19.png)

   
# 2. How we would make it deep Immutable ??

   ![img_20.png](img_20.png)

   you are creating a copy of the list and when you return this you return a copy of item
    that is different memory area and hence when try to modify it is unmodifiable and
    when try to modify will throw UnSupportedException

   ![img_21.png](img_21.png)
   
   so this Constructor we created is a Compact Constructor

   We can create Deep Immutable Records also But By Default Records are Shallow Immutable.
    It is your task to make the variables completely Immutables 


# 3. Can Records have Methods??

   ![img_22.png](img_22.png)

   ![img_23.png](img_23.png)


# 3. Can Records have Constructors?? What is canonical constructor and compact constructors??

   ![img_24.png](img_24.png)

   most people thinks records implicitly defines constructors because record create an all args constructor internally.
   suppose If you have an user and you want basic check validations like if user's name is null throw an Exception.

   ![img_25.png](img_25.png)

   this constructor is called compact constructor.

   ![img_26.png](img_26.png)

   ![img_27.png](img_27.png)

   ![img_28.png](img_28.png)

   ![img_29.png](img_29.png)

   ![img_30.png](img_30.png)

   ![img_31.png](img_31.png)

   ![img_32.png](img_32.png)

   ![img_33.png](img_33.png)

   ![img_34.png](img_34.png)

   ![img_35.png](img_35.png)

   ![img_36.png](img_36.png)

   ![img_37.png](img_37.png)

   # canonical constructor 

        which is an Implicit all argument constructor created by recod java for you internally.

  # compact constructor

       when you write your logic what you write and you want to embed into your all argument constructor
      that is compact constructor.

when ever you write a compact constructor, your compact constructor logic is copied and pasted into the
internal code of the canonical constructor(what the constructor created by java for you).

   ![img_38.png](img_38.png)

   ![img_39.png](img_39.png)

   
# 4. Can Records implements interfaces ?

   ![img_40.png](img_40.png)

![img_41.png](img_41.png)


# 5. Can Records have instance variables??

   ![img_42.png](img_42.png)

   ![img_43.png](img_43.png)

   ![img_44.png](img_44.png)

   
# 6. When should you use Records ??

![img_45.png](img_45.png)


# 7. WHy should we not use records for a JPA entity ??

![img_47.png](img_47.png)


 # 8. WHen you should not use Records ??

![img_48.png](img_48.png)




   