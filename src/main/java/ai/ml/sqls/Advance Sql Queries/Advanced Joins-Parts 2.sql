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

############### NATURAL Join ################
# Q1) Customers with their Accounts   # In Natural Join no nee to give on condition, natural joins will join based on a matching column.
SELECT cname, email, accno, bal
FROM customers
NATURAL JOIN accounts;

# Q2) Customers with their Addresss
SELECT cname, email, city, state
FROM customers
NATURAL JOIN address;

# Q3) Combine customers, accounts, and address
SELECT  cname, email, accno, bal, city, state
FROM customers 
NATURAL JOIN accounts
NATURAL JOIN address;

#############LEFT / RIGHT NATURAL JOIN Examples#############
# Q4) NATURAL LEFT JOIN — include all customers even if account missing
SELECT cname, email, accno, bal
FROM customers
NATURAL LEFT JOIN accounts;

# Q5) NATURAL RIGHT JOIN — include all accounts even if customer missing
SELECT cname, email, accno, bal
FROM customers
NATURAL RIGHT JOIN accounts;

##################### USING() Clause #######################
# Q1) Customers with Accounts
SELECT cname, email, accno, bal
FROM customers
INNER JOIN accounts 
USING (cid);

# Q2) Customers with Address
SELECT accno, bal, city, state
FROM accounts
INNER JOIN address 
USING (cid);

# Q3) Combine all three tables
SELECT cname,email, accno, bal, city, state
FROM customers
INNER JOIN accounts USING (cid)
INNER JOIN address USING (cid);

# Q4) All customers, including those without accounts
SELECT cname, email, accno, bal
FROM customers
LEFT JOIN accounts
USING (cid);

# Q5) All customers, including those without address
SELECT cname, email, city, state
FROM customers
LEFT JOIN address
USING (cid);

# Q6) Combine all three (keep all customers)
SELECT cname, email, accno, bal, city, state
FROM customers
LEFT JOIN accounts USING (cid)
LEFT JOIN address USING (cid);

# Q7) All accounts, including those not linked to any customer
SELECT cname, email, phone, accno, bal
FROM customers
RIGHT JOIN accounts
USING (cid);

# Q8) All addresses, including those without customers
SELECT cname, email, phone, city, state
FROM customers
RIGHT JOIN address
USING (cid);

# Q9) Combine all three (keep all address records)
SELECT cname, email, phone, accno, bal, city, state
FROM customers
RIGHT JOIN accounts USING (cid)
RIGHT JOIN address USING (cid);

###########  N - Table JOIN with FILTER Condition    ###########
#Q1 Customers from Blore or Pune with balance >= 15000
SELECT cname, phone, accno, bal, city, state
FROM customers
JOIN accounts USING (cid)
JOIN address USING (cid)
WHERE city IN ('Pune','Blore') AND bal >= 15000;

#Q2 Keep all customers (even if no account or address)
SELECT cname, phone, accno, bal, city, state
FROM customers
LEFT JOIN accounts USING (cid)
LEFT JOIN address USING (cid);

#Q3 Show all customers who live in Blore OR do not have any address record
SELECT c.cname, c.phone, a.city, a.state
FROM customers c
LEFT JOIN address a USING (cid)
WHERE a.city='Blore' OR a.cid IS NULL;

#Q4 Customers in KA with 'SA' accounts and balance between 10k-25k
SELECT cname, phone, accno, bal, city, state
FROM customers c
LEFT JOIN accounts ac USING (cid)
LEFT JOIN address ad USING (cid)
WHERE ad.state='KA'
AND ac.atype='SA' 
AND ac.bal BETWEEN 10000 AND 25000;