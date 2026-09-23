
Hibernate flush() method
========================
    The flush() method in Hibernate is used to synchronize the in-memory state of the persistence context with the underlying
    database. It forces Hibernate to execute SQL statements for pending changes, such as inserts, updates, and deletes, 
    without committing the transaction. Here are some reasons to use flush()

    To make changes visible to other operations within the same transaction:
   
    **If a subsequent operation, like a query, depends on changes made earlier in the transaction, flush() ensures that those
    changes are reflected in the database before the query is executed.

    To manage optimistic locking:

    ** flush() can be used to update the version field of an entity, which is used for optimistic locking. 
    This ensures that concurrent updates are handled correctly.

# Isolation

  In Transaction ISOLATION controls the visibility of changes made by one transaction to another transaction.
  
  For Example, there are 2 Transactions T1 and T2. What T1 is doing It should be update to T2 that is what
  the role of ISOLATION is to make the data consistency between multiple Transactions. 

# ISOLATION.DEFAULT

    This DEFAULT isolation level is specific to your database. so, what database you are using and what is the default isolation level
    set to your database will be use if you defaine Isolation Level as a DEFAULT. 

    you can set any of it from this and it will make as a default isolation level untill we change.

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
  ![img_34.png](img_34.png)
  ![img_35.png](img_35.png)
  
  ![img_36.png](img_36.png)
  ![img_37.png](img_37.png)
  ![img_38.png](img_38.png)
  
  ![img_39.png](img_39.png)
  ![img_40.png](img_40.png)
  
  ![img_41.png](img_41.png)
  ![img_42.png](img_42.png)
  ![img_43.png](img_43.png)
  ![img_44.png](img_44.png)
  
  ![img_45.png](img_45.png)
  
  
  ![img_46.png](img_46.png)
  
  ![img_47.png](img_47.png)
  
  ![img_48.png](img_48.png)