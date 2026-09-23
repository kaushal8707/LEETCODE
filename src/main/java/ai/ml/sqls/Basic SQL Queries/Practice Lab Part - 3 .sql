
use dandesdb;

/****  JOINS   *****/
CREATE TABLE customers (
	cid INT(3) PRIMARY KEY,
    cname CHAR(15) NOT NULL,
    email CHAR(15) NOT NULL UNIQUE,
    phone INT(10) NOT NULL UNIQUE
);

CREATE TABLE accounts(
	mycid INT(3),
    accno INT(5) PRIMARY KEY,
    actype CHAR(2) NOT NULL,
    bal DOUBLE NOT NULL
);

CREATE TABLE address(
	mycid INT(3),
    addid INT(3) PRIMARY KEY,
    street CHAR(15) NOT NULL,
	city CHAR(15) NOT NULL,
    state CHAR(15) NOT NULL
);

INSERT INTO customers values(101,'sri','sri@jlc',111);
INSERT INTO customers values(102,'vas','vas@jlc',222);
INSERT INTO customers values(103,'sd','sd@jlc',333);
INSERT INTO customers values(104,'ds','ds@jlc',444);
INSERT INTO customers values(105,'hello','hello@jlc',555);
INSERT INTO customers values(106,'hai','hai@jlc',666);
INSERT INTO customers values(107,'aaa','aaa@jlc',777);
INSERT INTO customers values(108,'bbb','bbb@jlc',888);
INSERT INTO customers values(109,'ccc','ccc@jlc',999);

INSERT INTO accounts values(101,12345,'SA',5000);
INSERT INTO accounts values(102,12346,'SA',15000);
INSERT INTO accounts values(103,12347,'SA',25000);
INSERT INTO accounts values(107,12348,'SA',3000);
INSERT INTO accounts values(108,12349,'SA',13000);
INSERT INTO accounts values(109,12350,'SA',18000);

INSERT INTO address values(101,1,'BTM','Blore','KA');
INSERT INTO address values(102,2,'MHA','Blore','KA');
INSERT INTO address values(103,3,'P1','Pune','MH');
INSERT INTO address values(104,4,'D1','Delhi','Delhi');
INSERT INTO address values(109,5,'D2','Delhi','Delhi');
INSERT INTO address values(110,6,'P2','Pune','MH');
INSERT INTO address values(111,7,'H1','Hyd','TG');
INSERT INTO address values(112,8,'pp','Patna','BR');

/** INNER JOIN **/
# Q1 Customers with matching Accounts??
SELECT cid, cname, phone, accno, bal
FROM customers c
INNER JOIN accounts a 
ON c.cid = a.mycid;

# Q2 Customers with matching Address// or customers which is having an addresses???
SELECT cid, cname, phone, city, state
FROM customers c
INNER JOIN address ad
ON c.cid = ad.mycid;

# Q3 combine all customers who is having accounts and having some addresses???
SELECT cid, cname, phone, accno, bal, city, state
FROM customers c
INNER JOIN accounts ac ON c.cid=ac.mycid
INNER JOIN address ad ON c.cid=ad.mycid;

/** LEFT OUTER JOIN **/
# Q1 customers and their accounts (include customers without accounts)
SELECT cid, cname, phone, accno, bal
FROM customers c 
LEFT JOIN accounts ac
ON c.cid=ac.mycid;

# Q2 customers and their addresses (include customers without addresses)
SELECT cid, cname, phone, city, state
FROM customers c 
LEFT JOIN address ad
ON c.cid=ad.mycid;

# Q3 combine customers, accounts and addresses (include customers missing in either)
SELECT cid, cname, phone, accno, bal, street, state
FROM customers c 
LEFT JOIN accounts ac ON c.cid=ac.mycid
LEFT JOIN address ad ON c.cid=ad.mycid;

/** Right Outer Join **/
# Q1 customers with accounts (include accounts without customers)   so every customers is having an account no account without any customers
SELECT c.cid, c.cname, c.phone, ac.accno, ac.bal
FROM customers c 
RIGHT JOIN accounts ac
ON c.cid=ac.mycid;

# Q2 customers with address (include addresses not linked to any customers)
SELECT cid, cname, phone, city, state
FROM customers c
RIGHT JOIN address ad 
ON c.cid=ad.mycid;

# Q3 combine customers, accounts and addresses (include orphan accounts or addresses)
SELECT cid, cname, phone, accno, bal, city, state
FROM customers c
RIGHT JOIN accounts ac ON c.cid=ac.mycid
RIGHT JOIN address ad ON c.cid=ad.mycid;

/** FULL OUTER JOIN **/    #Not direct support but using UNION we can do ->  LEFT + UNION + RIGHT -> FOJ
# Return all matching and matching Rows from customer and account table
# Final All Customer Having account or with no accounts and all accounts associated with customers and without customers

SELECT *
FROM customers c 
LEFT JOIN accounts ac ON c.cid=ac.mycid
UNION 
SELECT *
FROM customers c 
RIGHT JOIN accounts ac ON c.cid=ac.mycid;

/** JOIN WITH SOME CONDITIONS **/
# Q1 Customers with Balance >= 15000
SELECT *
FROM customers cust
INNER JOIN accounts acc
ON cust.cid=acc.mycid AND acc.bal >= 15000;
 
# Q2 Customers In Bangalore
SELECT *
FROM customers cust
INNER JOIN address ad
ON cust.cid=ad.mycid AND ad.city ='Blore';

# Q3 Customers in Bangalore or Pune with Balance >= 15000
SELECT *
FROM customers cust
INNER JOIN accounts acc ON cust.cid=acc.mycid AND acc.bal >= 15000
INNER JOIN address ad ON cust.cid=ad.mycid AND ad.city IN ('Blore', 'Pune');

/** SELF JOIN **/
CREATE TABLE myemployees(
	empId INT(3),
    empName CHAR(15),
    mgrId INT(3)
);
INSERT INTO myemployees VALUES
(101,'sri',103),
(102,'vas',103),
(103,'sd',NULL),
(104,'ds',101),
(105,'aaa',101),
(106,'bbb',102);

SELECT * FROM myemployees;

# Display Employee and Their Manager Names
SELECT emp.empName AS 'Employee', mgr.empName AS 'Manager'
FROM myemployees emp
INNER JOIN myemployees mgr
ON emp.mgrId = mgr.empId;

/** CROSS JOIN **/
SELECT cid, cname, accno, bal 
FROM customers c
CROSS JOIN accounts a;

#OR

SELECT cid, cname, accno, bal 
FROM customers c, accounts a;

/** additional **/

## Find only those customers who is not having both accounts and addresses
SELECT cid, cname, phone, accno, bal, street, state
FROM customers c 
LEFT JOIN accounts ac ON c.cid=ac.mycid 
LEFT JOIN address ad ON c.cid=ad.mycid 
WHERE ac.mycid IS NULL AND ad.mycid IS NULL;

## Find only those customers who is not having either accounts or addresses
SELECT cid, cname, phone, accno, bal, street, state
FROM customers c 
LEFT JOIN accounts ac ON c.cid=ac.mycid 
LEFT JOIN address ad ON c.cid=ad.mycid 
WHERE ac.mycid IS NULL OR ad.mycid IS NULL;

## Find only those customers who is not having any accounts
SELECT cid, cname, phone, accno, bal
FROM customers c 
LEFT JOIN accounts ac
ON c.cid=ac.mycid 
WHERE ac.mycid IS NULL;

## show only orphan accounts or addresses
SELECT cid, cname, phone, accno, bal, city, state
FROM customers c
RIGHT JOIN accounts ac ON c.cid=ac.mycid
RIGHT JOIN address ad ON c.cid=ad.mycid
WHERE c.cid IS NULL;