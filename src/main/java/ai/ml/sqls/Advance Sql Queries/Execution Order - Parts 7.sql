create database dandes_db;
use dandes_db;

# Written Order VS Actual Order

# -> SQL does not execute in the order we write
# -> SQL Engine follows a hidden internal order

CREATE TABLE customers (
    cid INT PRIMARY KEY,
    cname CHAR(15) NOT NULL,
    email CHAR(25) NOT NULL UNIQUE,
    phone BIGINT NOT NULL UNIQUE
);

INSERT INTO customers VALUES
(101,'sri','sri@jlc',111),
(102,'vas','vas@jlc',222),
(103,'sd','sd@jlc',333),
(104,'ds','ds@jlc',444),
(105,'hello','hello@jlc',555),
(106,'hai','hai@jlc',666),
(107,'aaa','aaa@jlc',777),
(108,'bbb','bbb@jlc',888),
(109,'ccc','ccc@jlc',999);

SELECT * FROM customers;

CREATE TABLE myorders (
order_id INT PRIMARY KEY,
customer_name VARCHAR(50) NOT NULL,
region VARCHAR(20) NOT NULL,
product_name VARCHAR(50) NOT NULL,
order_date DATE NOT NULL,
quantity INT NOT NULL,
unit_price DECIMAL(10,2) NOT NULL,
total_amount DECIMAL(12,2) NOT NULL,
cid INT NOT NULL
);

INSERT INTO myorders
(order_id, customer_name, region, product_name, order_date, quantity, unit_price,
total_amount,cid)
VALUES
(201,'Sri','East','Laptop Sleeve', '2024-01-05',1,5000.00,5000.00, 101),
(202,'Vas','West','Wireless Mouse', '2024-01-06',1,3000.00,3000.00,102),
(203,'sd','North','Keyboard', '2024-01-06',1,7000.00,7000.00,103),
(204,'ds','South','Stapler', '2024-01-05',1,3500.00,3500.00,104),
(205,'Hello','East','Desk Pad', '2024-01-06',1,1200.00,1200.00,105),
(206,'Sri','East','Notebook', '2024-01-07',1,2000.00,2000.00, 106),
(207,'Vas','West','Pen Set', '2024-01-07',1,1000.00,1000.00,107),
(208,'sd','North','USB Cable', '2024-01-08',1,4000.00,4000.00,108),
(209,'Hai','West','USB Hub', '2024-01-06',1,2500.00,2500.00,109),
(210,'Sri','East','Backpack', '2024-01-09',1,8000.00,8000.00,110);

SELECT * FROM myorders;

SELECT cname, SUM(total_amount)
FROM customers c
JOIN myorders o ON c.cid=o.cid
WHERE total_amount > 500
GROUP BY cname
HAVING SUM(total_amount) > 2000
ORDER BY SUM(total_amount) DESC
LIMIT 5;

#  We write in this order:
#  SELECT → FROM → JOIN → WHERE → GROUP BY → HAVING → ORDER BY → LIMIT

#  But SQL executes in this order following Order
#  FROM → JOIN → WHERE → GROUP BY → HAVING → WINDOW FUNCTIONS → SELECT →
#  DISTINCT → UNION/INTERSECT/EXCEPT → ORDER BY → LIMIT

SELECT cname, SUM(total_amount)
FROM customers c
JOIN myorders o ON c.cid = o.cid
WHERE total_amount > 500
GROUP BY cname
HAVING SUM(total_amount) > 2000
ORDER BY SUM(total_amount) DESC
LIMIT 5;

# Q1) Top 3 customers with highest purchase.   
	 #FROM - JOIN - WHERE - GROUP BY - HAVING - WINDOWS FUNCTION - SELECT - DISTINCT - UNION/INTERSECTION/EXCEPT - ORDER BY - LIMIT
SELECT cname, SUM(total_amount) AS total_amt
FROM customers
JOIN myorders USING (cid)
GROUP BY cname
ORDER BY total_amt DESC
LIMIT 3;

/** Internal Execution
		1) FROM → load both tables
		2) JOIN → match cid
		3) WHERE → (none)
		4) GROUP BY → group per customer
		5) HAVING → (none)
		6) WINDOW FUNCTIONS → (none)
		7) SELECT → cname, SUM(amount)
		8) DISTINCT → (none)
		9) UNION / INTERSECT / EXCEPT → (none)
		10) ORDER BY → total_amt DESC
		11) LIMIT → pick top 3 rows
**/

/** Why Execution Order Matters
		 A) Avoid Errors
		 Why we can’t use SELECT alias in WHERE?
		 Because WHERE runs before SELECT.
		 Why aggregate doesn’t work in WHERE?

		 Because aggregates are computed at GROUP BY / HAVING stage.

		 B) Write Faster, Correct Queries
		 Correct use of WHERE vs HAVING
		 Correct place for window functions (SELECT / ORDER BY)
		 C) Improve Performance
		 Filter early using WHERE
		 Reduce rows before GROUP BY
		 Reduce sorting work in ORDER BY


select * from customers;
select * from myorders;







