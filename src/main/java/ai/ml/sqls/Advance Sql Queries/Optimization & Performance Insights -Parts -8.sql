create database dandes_db;
use dandes_db;

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

# Query Optimization Basics

/**
	 Optimization means making queries run faster by reducing:
		 Disk I/O
		 CPU usage
		 Number of rows processed
		 Network transfer

	 SQL Optimizer does 80% of the work,
	 BUT we must write efficient queries.

	Most expensive operations (slowest):
		 FULL TABLE SCAN
		 FULL TABLE JOIN
		 SORT (ORDER BY)
		 GROUP BY on large tables
		 DISTINCT on large datasets

	Our goal = reduce these using:
		 Indexes
		 Filters in WHERE
		 Proper join conditions
		 LIMIT
		 Pre-aggregated tables (optional)

How Indexes Work
----------------
	 Indexes = like a book index → fast lookup.
	Types of Indexes
	 There are mainly Two Tyes of Indexes
	- 1. Clustered Index
	- 2. Non-Clustered Index

1. Clustered Index
-------------------

	 Data stored physically in sorted order
	 Only ONE clustered index per table
	 Usually on Primary Key (PK)
	 FAST for range scans and equality lookups
    
Example - customers table
	 Assume customers(cid) is the Primary Key → so it becomes a clustered index.
		SELECT *
		FROM customers
		WHERE cid BETWEEN 100 AND 200;

	 This is extremely fast
	o Because rows from cid=100 → cid=200 are stored sequentially on disk.
	o Perfect use-case for clustered index
	o DB reads only the required range
	o No full table scan

	 When clustered index helps?
		o PK lookups
		o Range filters on PK
		o Sorting by PK
		o Joins using PK

2. Non-Clustered Index
-----------------------

 Separate index structure → stores pointer to actual row
 MANY non-clustered indexes can exist
 Useful for columns frequently used in search conditions

	Example 1: phone
	 Create Index:
		CREATE INDEX idx_customers_phone ON customers(phone);
        
	 Query using this index:
		SELECT cid, cname, phone
		FROM customers
		WHERE phone = '123456';
        
 DB uses idx_customers_phone → fast lookup
 Without index → full table scan

Example 1: city
	 Create Index:
		CREATE INDEX idx_customers_city ON customers(city);
	 Query using this index:
		SELECT cid, cname, city
		FROM customers
		WHERE city = 'Bangalore';
        
 DB jumps directly to all Bangalore entries
 Useful for filtering by city

**/

CREATE INDEX idx_customers_phone ON customers(phone);

SELECT cname, email, phone
FROM customers
WHERE phone = 555;

SELECT * FROM idx_customers_phone;

SHOW index
FROM customers;


/**
#>> Where Indexes Help Most

	1) WHERE filters
		 WHERE phone = '9876543210'
		 WHERE city = 'Bangalore'
		 WHERE cid > 5000

	2) JOINs
		 customers.cid = orders.cid

	3) ORDER BY
		 If index matches sorting:
		ORDER BY phone
		ORDER BY city

	4) GROUP BY
		 GROUP BY city
		If city has index → grouping becomes faster.

**/
/**
When Indexes Do NOT Help

1) Using functions on indexed columns
	Example:
	WHERE LOWER(city) = 'mumbai';

	This breaks index.
	DB can’t use index → full scan.

2. wildcards
	Case 1: Index CAN be used
	Example:
	WHERE city LIKE 'Bang%'
	Meaning → starts with "Bang"

Why index works here?
	- Because the DB can jump directly to the section of the index where all values starting with
	“Bang” begin.
	- So DB goes:
	Start at 'Bang' → read forward → stop when pattern breaks.

	 FAST
	 INDEX USED
	 Range scan

Case 2: Index CANNOT be used
	Example:
	WHERE city LIKE '%lore'
	Meaning → ends with “lore”

Why index does NOT work?
- Because DB does NOT know what the starting letters are.
- So DB must do:
→ Full table scan
→ Check each row one by one
→ MATCH last letters
→ Slow

3. Very small tables
	 DB chooses a full scan because:
	 Full scan is cheaper than using an index tree

4. High-write tables (insert/update/delete)
	 Indexes slow down writes because:
	 DB updates index tree for every write
	 More indexes → slower inserts/updates

	Good rule:
	Keep indexes only on columns used frequently in WHERE, JOIN, ORDER BY, GROUP BY
**/