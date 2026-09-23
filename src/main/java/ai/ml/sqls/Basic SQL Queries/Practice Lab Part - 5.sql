use dandesdb;

# Base Table (simple, single source for easy updatable views)
DROP TABLE customers;

CREATE TABLE customers (
    cid   INT PRIMARY KEY,
    cname CHAR(15)      NOT NULL,
    email CHAR(15)      NOT NULL UNIQUE,
    phone INT       NOT NULL UNIQUE,
    city  CHAR(15)      NOT NULL,
    status CHAR(15)     NOT NULL,
    accno INT        NOT NULL UNIQUE,
    atype CHAR(2)       NOT NULL,
    branch CHAR(10)     NOT NULL,
    bal   DOUBLE
);

# Sample Data
INSERT INTO customers VALUES
(101,'sri','sri@myjlc',123,'Blore','Active',5001,'SA','B01',25000),
(102,'vas','vas@myjlc',321,'Blore','Active',5002,'SA','B01',25000),
(103,'sd','sd@myjlc',234,'Hyd','Active',5003,'SA','B01',25000),
(104,'ds','ds@myjlc',345,'Hyd','Active',5004,'SA','B01',25000),
(105,'aa','aa@myjlc',111,'Delhi','Active',5005,'SA','B01',25000),
(106,'bb','bb@myjlc',222,'Delhi','Active',5006,'SA','B01',25000),
(107,'cc','cc@myjlc',333,'Blore','Active',5007,'SA','B01',25000),
(108,'dd','dd@myjlc',444,'Blore','Active',5008,'SA','B01',25000),
(109,'ee','ee@myjlc',555,'Hyd','Active',5009,'SA','B01',25000),
(110,'ff','ff@myjlc',666,'Hyd','Active',5010,'SA','B01',25000),
(111,'gg','gg@myjlc',777,'Delhi','Active',5011,'SA','B01',25000),
(112,'hh','hh@myjlc',888,'Delhi','Active',5012,'SA','B01',25000);

SELECT * FROM customers;

# Teller View (limited columns)
#Create View
	CREATE VIEW teller_view AS
	SELECT cid, cname, city, accno, atype, bal
	FROM customers;
# Fetch data from View
	SELECT * FROM teller_view;
    SELECT email FROM teller_view;
    
# City-wise Teller Views:
#Create View
	CREATE VIEW blore_teller_view AS
    SELECT cid, cname, city, accno, atype, bal
    FROM customers
    WHERE city='Blore';
	
	CREATE VIEW hyd_teller_view AS
    SELECT cid, cname, city, accno, atype, bal
    FROM customers
    WHERE city='Hyd';
    
    CREATE VIEW delhi_teller_view AS
    SELECT cid, cname, city, accno, atype, bal
    FROM customers
    WHERE city='Delhi';
    
#Fetch data from View:
	SELECT * FROM blore_teller_view;
	SELECT * FROM delhi_teller_view;
    SELECT * FROM hyd_teller_view;
    
# Table in Use: customers
# Task 1 — Create a Manager View
# Create a view that shows only customer details relevant to a bank manager:
-- cid, cname, city, accno, bal, status.

CREATE VIEW manager_view AS
SELECT cid, cname, city, accno, bal, status
FROM customers;

SELECT * FROM manager_view;

# Task 2 — Create a Filtered Manager View (City-Wise)
-- Create a view that lists all Hyderabad customers along 
-- with their account numbers and balances.

CREATE VIEW hyd_manager_view AS
SELECT cid, cname, city, accno, bal
FROM customers
WHERE city = 'Hyd';


SELECT * FROM hyd_manager_view;

# Task 3 — Update Data through a View
-- Use an updatable view to modify or delete records, 
-- then verify that the changes reflect in the base table.

-- Create updatable view
CREATE VIEW myview AS
SELECT cid, cname, email, phone, city, status
FROM customers;

-- Verify
SELECT * FROM myview;
SELECT * FROM customers;

-- Update one record
UPDATE myview
SET status = 'Inactive'
WHERE cid = 103;

-- Delete a record from View
DELETE FROM myview
WHERE cid = 104;

-- Verify
SELECT * FROM myview;
SELECT * FROM customers;


/** Arthematic Functions **/
SELECT SQRt(25);
SELECT POWER(25, 2);
SELECT POWER(25, 0.5);
SELECT MOD(10, 3);
SELECT ABS(-9);
SELECT CEIL(5.2);
SELECT FLOOR(5.9);
SELECT ROUND(5.6);

/** String Functions **/
SELECT LENGTH("Srinivas");
SELECT TRIM(" Sri nivas ");
SELECT RPAD("JLC",6,"*");
SELECT LPAD("JLC",5,"*");
SELECT SUBSTR("srinivas",3,4);
SELECT LOWER("SriNiVas");
SELECT UPPER("SriNiVas");
SELECT ASCII('A');
SELECT CHAR(97);
SELECT CONCAT('hello','guys');
SELECT REPLACE('helloguys','hello','JLC');

/** DATE TIME Functions **/
SELECT SYSDATE();
SELECT NOW();
SELECT DATE(SYSDATE());
SELECT TIME(SYSDATE());
SELECT DAY(SYSDATE());
SELECT MONTH(SYSDATE());
SELECT YEAR(SYSDATE());

/** Formatting Dates **/
SELECT DATE_FORMAT(SYSDATE(), '%D %M %Y');
SELECT DATE_FORMAT(SYSDATE(), '%d-%m-%Y');
SELECT DATE_FORMAT(SYSDATE(), '%W %d-%m-%Y');
SELECT DATE_FORMAT(SYSDATE(), '%d-%b-%Y');
SELECT TIME_FORMAT(SYSDATE(), '%r');
SELECT TIME_FORMAT(SYSDATE(), '%T');

/** Conversion Function **/
CREATE TABLE hello( 
	id INT, 
    hello DATE
);
INSERT INTO hello VALUES(101, STR_TO_DATE('10 August 2024', '%d %M %Y'));
INSERT INTO hello VALUES(102, STR_TO_DATE('11 Aug 2024', '%d %M %Y'));
INSERT INTO hello VALUES(103, STR_TO_DATE('12 Sep 2025', '%Y %M %D'));

SELECT * FROM hello;
SELECT id, DATE_FORMAT(hello, '%d-%m-%Y') AS formatted_date FROM hello;

/*******   self try *******/
# STR_TO_DATE() -> converts string -> Date (for storage)
# DATE_FORMAT() -> converts DATE -> string (for display)
CREATE TABLE temp( 
	id INT, 
    purchase_on DATE
);

INSERT INTO temp VALUES(101, STR_TO_DATE('10 August 2024', '%d %M %Y'));

/** AGgregate functions **/
SELECT
	COUNT(*) AS total_students,
    AVG(bal) AS avg_balance,
    MAX(bal) AS highest_balance
FROM mystudents;