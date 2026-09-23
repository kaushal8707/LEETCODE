use dandesdb;

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

# CTE- Common Table Expression
# Q1 Display Customers with their Balance using a CTE
WITH cust_bal_cte AS(
	SELECT cid, bal FROM accounts
)
SELECT cust.cname, cust.email, cb_cte.bal
FROM customers cust
JOIN cust_bal_cte cb_cte
ON cust.cid=cb_cte.cid;

# Q2 Customers having balance  > 15000
WITH high_balance_cte AS (
	SELECT cid, bal
    FROM accounts 
    WHERE bal > 15000
)
SELECT cust.cname, cust.email, hb_cte.bal
FROM customers cust
JOIN high_balance_cte hb_cte
ON cust.cid=hb_cte.cid;

# CTE With JOINS Inside
# Q3 Customers along with their City and Balance
WITH cust_info_cte AS(
	SELECT cust.cid, cust.cname, acc.bal, addr.city
    FROM customers cust
    JOIN accounts acc ON cust.cid=acc.cid
    JOIN address addr ON cust.cid=addr.cid
)
SELECT cname, bal, city
FROM cust_info_cte;

# Multiple CTEs
# Q4 Using two CTE's to calculate average and filter results
WITH avg_bal_cte AS(
	SELECT AVG(bal) as avg_balance
    FROM accounts
),
cust_bal_cte AS(
	SELECT c.cname, a.bal
    FROM customers c
    JOIN accounts a ON c.cid=a.cid
)
SELECT cname, bal
FROM cust_bal_cte, avg_bal_cte
WHERE cust_bal_cte.bal > avg_bal_cte.avg_balance;

# Nested (Chained) CTE's
# Q5 Find Customers who live in 'Blore' and have balance > city average
WITH city_avg_cte AS(
	SELECT ad.city, AVG(a.bal) AS avg_bal
    FROM accounts a
    JOIN address ad ON a.cid=ad.cid
    GROUP BY ad.city
),
cust_info_cte AS(
	SELECT c.cname, ad.city, a.bal
    FROM customers c
    JOIN accounts a ON c.cid=a.cid
    JOIN address ad ON c.cid=ad.cid
)
SELECT ci.cname, ci.city, ci.bal
FROM cust_info_cte ci
JOIN city_avg_cte ca ON ci.city=ca.city
WHERE ci.bal > ca.avg_bal
AND ci.city = 'Blore';


