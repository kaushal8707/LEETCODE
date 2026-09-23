
        1. use-cases - steps to performed
        2. get product inventory
        3. validate product availability < (5)
        4. update total price in order entity
        5. save order
        6. update stock in inventory


To Test

  insert 2 records in Inventory (Product Table)

        insert into Product(name, price, stock_quantity)
        values('Laptop', 25000.0, 10);
        
        insert into Product(name, price, stock_quantity)
        values('SmartPhone', 5000.0, 12);

Open Swagger
http://localhost:9191/swagger-ui/index.html#/

Happy Scenario

Payload:

        {
            "id": 101,
            "productId": 2,    2-> smartphone
            "quantity": 2
        }

To Break the flow or test rollback use below code(Inventory Handler while updating) 

        if(product.getPrice() > 5000) {
            throw new RuntimeException("DB Crashed");
        }

and payload

        {
            "id": 102,
            "productId": 1,    1-> Laptop  (bcz laptop price is 25000 so won't allow update operation)
            "quantity": 5
        }

***
so the point is -

   ** first test happy scenario without @Transactional with smartphone data so record will insert in both the tables.

   ** second test scenario without @Transactional with laptop data so, it will break and only order will got
      placed in order table, but it won't update in inventory table so no use bcz order placed but 
      item availability in stock not updated so data inconsistency.

   ** third enable transaction with annotate @Transactional and perform happy scenario with smartphone data 
      so record would be inserted in both tables.

   ** fourth enable transaction with annotate @Transactional and perform laptop data so, it will break
      but this time transaction got rolled back and record data get reverted back. so no insert and update
      in both inventory and order table. so data consistency occurred.


      
