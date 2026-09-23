
/*****   Scalar Subqueries   *****/

# Step 1: Create Tables

CREATE TABLE customers (
    cid INT PRIMARY KEY,
    cname CHAR(15) NOT NULL,
    email CHAR(25) NOT NULL UNIQUE,
    phone BIGINT NOT NULL UNIQUE
);

CREATE TABLE accounts (
    cid INT,
    accno INT PRIMARY KEY,
    atype CHAR(2) NOT NULL,
    bal DOUBLE NOT NULL
);

CREATE TABLE address (
    cid INT,
    addid INT PRIMARY KEY,
    street CHAR(15) NOT NULL,
    city CHAR(15) NOT NULL,
    state CHAR(15) NOT NULL
);

# Step 2: Insert Sample Records
-- Customers
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

-- Accounts
INSERT INTO accounts VALUES
(101,12345,'SA',5000),
(102,12346,'SA',15000),
(103,12347,'SA',25000),
(107,12348,'SA',3000),
(108,12349,'SA',13000),
(109,12350,'SA',18000);

-- Address
INSERT INTO address VALUES
(101,1,'BTM','Blore','KA'),
(102,2,'MHA','Blore','KA'),
(103,3,'P1','Pune','MH'),
(104,4,'D1','Delhi','Delhi'),
(109,5,'D2','Delhi','Delhi'),
(110,6,'P2','Pune','MH'),
(111,7,'H1','Hyd','TG'),
(112,8,'PP','Patna','BR');
###### Advanced Subqueries
###### Scalar Subqueries
			# scalar subqueries return a single value(one row - one column)
            # Used in a SELECT statement
            # It behaves like a constant or computed value
            # It is executed once per outer row in the query
            
# A Single Value(One Row , One Column)
# Q1 Display each customer with their account balance
SELECT cname,
		(SELECT bal FROM accounts a WHERE a.cid=c.cid) AS balance
FROM customers c;

# Q2 Display each customer with their city
SELECT cname, phone,
       (SELECT city FROM address adr WHERE adr.cid=c.cid) as city
FROM customers c;

# B Derived / Calculated Columns
# Q3 Display each customer with their balance after adding 10% bonus
SELECT cname,phone,
	   (SELECT bal * 1.10 FROM accounts a WHERE a.cid=c.cid) AS new_balance
FROM customers c;

# Q4 Display customer with both balance and city using subqueries
SELECT cname,
	(SELECT bal FROM accounts ac WHERE ac.cid=c.cid) AS balance,
	(SELECT city FROM address ad WHERE ad.cid=c.cid) AS city
FROM customers c;


/**  Scalar Subquery VS JOIN (Performance & Use Case)  **/
# Customers with their Account Balance

# Approach 1 - Scalar Subquery
SELECT cname,
	(SELECT bal FROM accounts a WHERE a.cid=c.cid) AS balance
FROM customers c;
  # Execution Flow
  # Outer QUery -> pick one customer -> run inner query -> repeat for next customer.


# Approach 2 - JOIN
SELECT cname, bal AS balance
FROM customers c
LEFT JOIN accounts a 
USING (cid);
  # Execution Flow
  # Database engine merges both tables in one pass using indexes -> faster.








