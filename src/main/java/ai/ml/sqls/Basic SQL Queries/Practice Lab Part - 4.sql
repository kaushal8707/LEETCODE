use dandesdb;

/** SUB QUERIES */
CREATE TABLE mystudents(
	sid INT(3) PRIMARY KEY,
    sname CHAR(15),
    email CHAR(15),
    phone LONG,
    city CHAR(15),
    course CHAR(15),
    bal DOUBLE
);

INSERT INTO mystudents
VALUES(101, 'Sri', 'sri@jlc', 123456, 'Blore', 'Java', 9000);
INSERT INTO mystudents
VALUES(102, 'Vas', 'Vas@jlc', 654321, 'Blore', 'Java', 15000);
INSERT INTO mystudents
VALUES(103, 'ds', 'ds@gmail.com', 1234, 'Blore', 'DevOps', 3000);
INSERT INTO mystudents
VALUES(104, 'sd', 'sd@gmail.com', 4321, 'Hyd', 'AWS', 5000);
INSERT INTO mystudents
VALUES(105, 'hello', 'hello@jlc', 5555, 'Delhi', 'Java', 8000);
INSERT INTO mystudents
VALUES(106, 'hai', 'hai@gmail.com', 9999, 'Blore', 'AWS', 6000);
INSERT INTO mystudents
VALUES(107, 'aaa', 'aaa@jlc', 1111, 'Pune', 'DevOps', 22000);
INSERT INTO mystudents
VALUES(108, 'bbb', 'bbb@jlc', 2222, 'Delhi', 'Java', 7000);
INSERT INTO mystudents
VALUES(109, 'ccc', 'ccc@jlc', 3333, 'Blore', 'Java', 20000);
INSERT INTO mystudents
VALUES(110, 'ddd', 'ddd@jlc', 4444, 'Hyd', 'Python', 5000);
INSERT INTO mystudents(sid, sname, email, course)
VALUES(111, 'eee', 'eee@gmail.com', 'Java');
INSERT INTO mystudents(sid, sname, phone, course)
VALUES(112, 'fff', 5555, 'Python');

SELECT *
FROM mystudents;

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



/**   Single-Row SubQuery  
subquery return only one row
used with operator =,<,>,<=,>= **/  
# Compare each students balance with the average balance
SELECT sid, sname, phone, city, course, bal
FROM mystudents 
WHERE bal > (
	SELECT AVG(bal) 
    FROM mystudents
);

/**   Multi-Row SubQuery 
subquery that returns multiple rows
used with Operator : IN, ANY, ALL
 **/
# Returns students who are in the same cities as Java students
SELECT  sid, sname, phone, city, course, bal
FROM mystudents
WHERE city IN (
	SELECT city
	FROM mystudents 
	WHERE course='Java'
);

/*********** PRACTICE LAB - SUB QUERIES *****************/
# use all 3 tables used in joins customers, accounts, address 

#Q1 Display the balance of customer whose phone is 333
# without sub-query
	SELECT cid,cname,bal,phone
	FROM customers c 
	INNER JOIN accounts a 
	ON c.cid=a.mycid AND c.phone=333;
# with sub-query
	SELECT bal
    FROM accounts
    WHERE mycid = ( SELECT cid FROM customers WHERE phone=333 );

#Q2 Display Accno and Balance of customers who are staying in Blore
	SELECT accno, bal
    FROM accounts
    WHERE mycid IN (SELECT mycid FROM address WHERE city='Blore');

#Q3 Display the city of customer whose email is 'ccc@jlc'
	SELECT city
    FROM address 
    WHERE mycid = (SELECT cid FROM customers WHERE email='ccc@jlc');
    
#Q4 Display cname, email and phone of the customer whose accno=12345
	SELECT cname, email, phone
    FROM customers
    WHERE cid = (SELECT mycid FROM accounts WHERE accno = 12345);

#Q5 Display customers who are not maintaining minimum balance (<20000)
	SELECT * 
    FROM customers 
    WHERE cid IN (SELECT mycid FROM accounts WHERE bal < 20000);

#Q6 Display cname, email, street, city of customers not maintaining Minimum balance (< 20000)
	SELECT c.cname,c.email,ad.street,ad.city
    FROM customers c
    INNER JOIN address ad
    ON c.cid = ad.mycid 
    WHERE c.cid IN (SELECT ac.mycid FROM accounts ac WHERE ac.bal < 20000);
	
/** Creating New Tables From Queries **/

#Q7 Create table with all columns and data from customers
	CREATE TABLE mycust1 AS
    SELECT * FROM customers;
    
#Q8 Create table with selected columns and data
	CREATE TABLE mycust2 AS
    SELECT cname,email,phone FROM customers;

#Q9 Create table with structure only (no data)
	CREATE TABLE mycust3 AS
    SELECT * FROM customers WHERE 1=2;
    
#Q10 Create table with selected columns and no data
	CREATE TABLE mycust4 AS
    SELECT cid,cname,phone 
    FROM customers 
    WHERE 1=2;

#Q11 create Table with data filtered by condition
	 CREATE TABLE myadd1 AS
     SELECT * FROM address
     WHERE city='Blore';

#Q12 Create tables from customers, accounts and address(joined)
	CREATE TABLE mydetails AS
    SELECT cid,cname,phone,accno,bal,city,state
    FROM customers c
    INNER JOIN  accounts ac ON c.cid=ac.mycid
    INNER JOIN address ad ON c.cid=ad.mycid;

/******* SET OPERTAIONS *******/
CREATE TABLE students1 (
    sid INT,
    sname CHAR(15),
    course CHAR(15)
);

CREATE TABLE students2 (
    sid INT,
    sname CHAR(15),
    course CHAR(15)
);

INSERT INTO students1 VALUES
(101, 'Srinivas', 'Java'),
(102, 'Vas', 'Python'),
(103, 'Sd', 'ML'),
(104, 'Ds', 'SQL');

INSERT INTO students2 VALUES
(103, 'Sd', 'ML'),
(104, 'Ds', 'SQL'),
(105, 'Hello', 'Python'),
(106, 'Hai', 'AI');

SELECT * FROM students1;
SELECT * FROM students2;

# A) UNION

SELECT sid, sname, course FROM students1
UNION
SELECT sid, sname,  course FROM students2;

# B) UNION ALL

SELECT sid, sname, course FROM students1
UNION ALL
SELECT sid, sname, course FROM students2;

# C) INTERSECT

SELECT sid,sname, course FROM students1
INTERSECT
SELECT sid,sname, course FROM students2;

# ️ Note: MySQL doesn’t support INTERSECT directly in all the vesrions.
# Use an INNER JOIN alternative:

SELECT a.sname, a.course
FROM students1 a
INNER JOIN students2 b
ON a.sname = b.sname AND a.course = b.course;

# D) EXCEPT / MINUS

SELECT sid, sname, course FROM students1
EXCEPT
SELECT sid, sname, course FROM students2;

#  MySQL doesn’t support EXCEPT or MINUS directly.
# You can simulate it using a LEFT JOIN and WHERE IS NULL:

SELECT a.sid, a.sname, a.course
FROM students1 a
LEFT JOIN students1 b
ON a.sname = b.sname AND a.course = b.course;


# 8.5. ORDER BY with Set Operations

SELECT sname, course FROM students1
UNION
SELECT sname, course FROM students2
ORDER BY sname ASC;

-- select * from mydetails;
-- select * from address;
-- select * from accounts;