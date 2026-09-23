
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

# 3.4. Correlated Subqueries
# Inner query references the outer table’s column, creating a correlation.

# Q1) Customers Having Balance > Average Balance   # This is not correlated bcz the inner query doesn't reference any other columns.
SELECT c.cname, a.bal
FROM customers c
JOIN accounts a ON c.cid=a.cid
WHERE a.bal > (SELECT AVG(bal) FROM accounts);  # Runs only once bcz, This is not correlated this is independent sub query
## >>>>> which mean find those customers where balance is greater than average where city is same

# Q2 Find Customers whose balance is greater than their city's average balance (Correlated Subqueries)
SELECT cname, bal, city
FROM customers c
JOIN accounts ac1 on c.cid=ac1.cid
JOIN address ad1 on c.cid=ad1.cid
WHERE ac1.bal > (
	SELECT AVG(ac2.bal)
    FROM accounts ac2 
    JOIN address ad2 on ac2.cid=ad2.cid
    WHERE ad1.city=ad2.city
);

# Q3 Find Customers whose balance is greater than their city's average balance (USING JOINS)
SELECT cust.cname, acc1.bal, adr1.city
FROM customers cust
JOIN accounts acc1 ON cust.cid=acc1.cid
JOIN address adr1 ON cust.cid=adr1.cid
JOIN (
	SELECT adr2.city, AVG(acc2.bal) AS avg_bal
    FROM accounts acc2
    JOIN address adr2 ON acc2.cid=adr2.cid
    GROUP BY (adr2.city)
    ) AS city_avg
ON acc1.bal > city_avg.avg_bal
AND adr1.city = city_avg.city;

# Q4 Customers Having the Maximum Balance in Their City (Correlated Subquery)
SELECT c.cname, ac1.bal, ad1.city
FROM customers c
JOIN accounts ac1 ON c.cid=ac1.cid
JOIN address ad1 ON c.cid=ad1.cid
WHERE ac1.bal = (
	SELECT MAX(ac2.bal) 
    FROM accounts ac2
    JOIN address ad2 ON ac2.cid=ad2.cid
    WHERE ad2.city=ad1.city);

# Q5 Customers Having the Maximum Balance in Their City (USING JOINS)
SELECT cust.cname, adr1.city, acc1.bal
FROM customers cust
JOIN accounts acc1 ON cust.cid=acc1.cid
JOIN address adr1 ON cust.cid=adr1.cid
JOIN (
	SELECT adr2.city, MAX(acc2.bal) as max_bal
    FROM address adr2
    JOIN accounts acc2 ON adr2.cid=acc2.cid
    GROUP BY adr2.city
   ) AS city_max
ON adr1.city=city_max.city
AND acc1.bal = city_max.max_bal;


# EXISTS / NOT EXISTS
# Q1 Customers Having an Accout. 
SELECT cust.cname, cust.cid
FROM customers cust 
WHERE EXISTS(
	SELECT 1 
    FROM accounts ac
    WHERE ac.cid=cust.cid
);

# Q2 Customers Without Any Accounts
SELECT cust.cid, cust.cname
FROM customers cust
WHERE NOT EXISTS(
	SELECT 1 
    FROM accounts ac
    WHERE ac.cid = cust.cid
);

# Q3.a Customers Living In Bangalore Having an Accounts
SELECT cust.cname, cust.cid
FROM customers cust 
WHERE EXISTS(
	SELECT 1 
    FROM accounts ac 
    WHERE ac.cid=cust.cid
)
AND EXISTS(
	SELECT 1 
    FROM address adr 
    WHERE adr.cid=cust.cid
    AND adr.city='Blore'
);

# Q3.b Customers Living In Delhi doesn't Having an Accounts
SELECT cust.cname, cust.cid
FROM customers cust 
WHERE NOT EXISTS(
	SELECT 1 
    FROM accounts ac 
    WHERE ac.cid=cust.cid
)
AND EXISTS(
	SELECT 1 
    FROM address adr 
    WHERE adr.cid=cust.cid
    AND adr.city='Delhi'
);

# Q4 Customers Having High Balance Accounts (> 15000)
SELECT cust.cid, cust.cname
FROM customers cust 
WHERE EXISTS(
	SELECT 1
    FROM accounts ac 
    WHERE ac.cid=cust.cid
    AND ac.bal > 15000
);

# Q5 Customers Who Don't have an Address
SELECT cust.cid, cust.cname
FROM customers cust
WHERE NOT EXISTS (
	SELECT 1
    FROM address adr
    WHERE adr.cid=cust.cid
);

# Q6 Customers Living In Pune
SELECT cust.cid, cust.cname
FROM customers cust 
WHERE EXISTS(
	SELECT 1
    FROM address adr
    WHERE adr.cid=cust.cid
    AND adr.city='Pune'
);

# 3.7 EXISTS VS IN (Customers Having an Accounts)
########## EXISTS
SELECT *
FROM customers cust
WHERE EXISTS (
	SELECT 1
    FROM accounts ac
    WHERE ac.cid=cust.cid
);
############ IN
SELECT *
FROM customers cust 
WHERE cust.cid IN (
	SELECT acc.cid
    FROM accounts acc
);

# 3.8 EXISTS VS JOIN (Customers who have at least one Accounts)
########### JOIN
SELECT DISTINCT cust.cname
FROM customers cust 
JOIN accounts ac1
ON cust.cid=ac1.cid;

########## EXISTS
SELECT cust.cname
FROM customers cust 
WHERE EXISTS(
	SELECT 1
    FROM accounts acc1
    WHERE acc1.cid=cust.cid
);

######==================Summary======================

#1 Independent Subquery (Customer with balance > Global average)
SELECT cust.cname,acc.bal
FROM customers cust
JOIN accounts acc ON cust.cid=acc.cid
WHERE acc.bal > (SELECT AVG(acc2.bal) FROM accounts acc2);

#2 Correlated Subquery (Customers with Balance > City Average)
SELECT cust.cname, acc1.bal, add1.city
FROM customers cust
JOIN accounts acc1 ON cust.cid=acc1.cid
JOIN address add1 ON cust.cid=add1.cid
WHERE acc1.bal > (
		SELECT AVG(acc2.bal)
        FROM accounts acc2
        JOIN address add2 ON acc2.cid=add2.cid
        WHERE add2.city=add1.city
);

#3 Customers with their accounts (USING JOIN)
SELECT cust.cname, acc.bal, acc.accno
FROM customers cust
JOIN accounts acc 
ON cust.cid=acc.cid;

#3 Customers with their accounts (USING Subquery)
SELECT cust.cname
FROM customers cust 
WHERE cust.cid IN (SELECT acc.cid FROM accounts acc);

#4 Customers with their accounts (USING Scalar Sybquery)
SELECT cust.cname,
	(SELECT acc1.bal FROM accounts acc1 WHERE acc1.cid=cust.cid) AS balance
FROM customers cust;

#5 Customers with their accounts (Using EXISTS)
SELECT cust.cname
FROM customers cust
WHERE EXISTS(
	SELECT 1
    FROM accounts acc
    WHERE acc.cid=cust.cid);
    
#6 Customers living in cities where at least one person has balance > 15000 (Nested Subquery)
SELECT cust.cname, acc1.bal, add1.city
FROM customers cust
JOIN accounts acc1 ON cust.cid=acc1.cid
JOIN address add1 ON cust.cid=add1.cid
WHERE add1.city IN (
	SELECT add2.city
    FROM address add2
    JOIN accounts acc2 ON add2.cid=acc2.cid
    WHERE acc2.bal> 15000);

#7 Customers living in cities where at least one person has balance > 15000 (USING JOINS)
SELECT cust.cname, acc.bal, adr.city
FROM customers cust
JOIN address adr ON cust.cid=adr.cid
JOIN accounts acc ON cust.cid=acc.cid AND acc.bal > 15000;

# Correlated Subquery VS JOIN
# Customers whose balance is > their city Average

#>>> Correlated SubQUery
SELECT cust.cname, acc1.bal, add1.city
FROM customers cust
JOIN accounts acc1 ON cust.cid=acc1.cid
JOIN address add1 ON cust.cid=add1.cid
WHERE acc1.bal > (
	SELECT AVG(acc2.bal)
    FROM accounts acc2
    JOIN address add2 ON acc2.cid=add2.cid
    WHERE add1.city=add2.city);

#>>> JOIN + GROUP BY
SELECT cust.cname, acc1.bal, add1.city
FROM customers cust
JOIN accounts acc1 ON cust.cid=acc1.cid
JOIN address add1 ON cust.cid=add1.cid
JOIN (
	SELECT add2.city, AVG(acc2.bal) as avg_bal
    FROM address add2
    JOIN accounts acc2 ON add2.cid=acc2.cid
    GROUP BY add2.city) AS city_avg
ON acc1.bal > city_avg.avg_bal
AND add1.city=city_avg.city;