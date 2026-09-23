
/** AGGREGATION , GROUP BY, HAVING and HANDLING NULL in AGGREGATION **/

USE dandesdb;

CREATE TABLE jlcstudents(
	sid INT(3) PRIMARY KEY,
    sname CHAR(15),
    city CHAR(15),
    course CHAR(15),
    feepaid DOUBLE,
    feebal DOUBLE,
    status CHAR(10)
);

INSERT INTO jlcstudents VALUES(101,'sri','Blore','Java',10000,10000,'Active');
INSERT INTO jlcstudents VALUES(102,'vas','Hyd','AWS',5000,15000,'Active');
INSERT INTO jlcstudents VALUES(103,'sd','Delhi','Java',12000,8000,'Active');
INSERT INTO jlcstudents VALUES(104,'ds','Blore','Python',8000,12000,'Active');
INSERT INTO jlcstudents VALUES(105,'aa','Pune','Java',15000,5000,'InActive');
INSERT INTO jlcstudents VALUES(106,'bb','Blore','Java',10000,10000,'InActive');
INSERT INTO jlcstudents VALUES(107,'cc','Delhi','Python',12000,8000,'InActive');
INSERT INTO jlcstudents VALUES(108,'dd','Blore','Java',8000,12000,'Active');
INSERT INTO jlcstudents VALUES(109,'hello','Pune','Python',5000,15000,'Active');
INSERT INTO jlcstudents VALUES(110,'hai','Delhi','Java',15000,15000,'Active');
INSERT INTO jlcstudents(sid,sname,course,feepaid,feebal)
VALUES(111,'abc','Python',5000,15000);
INSERT INTO jlcstudents(sid,sname,course,feepaid,feebal)
VALUES(112,'xyz','Java',5000,15000);
INSERT INTO jlcstudents(sid,sname,course,feepaid)
VALUES(113,'mno','Python',15000);
INSERT INTO jlcstudents(sid,sname,course,feepaid)
VALUES(114,'pqr','Java',15000);

SELECT * FROM jlcstudents;

# Q1 Count Total Number of Row
SELECT COUNT(*) AS total_students
FROM jlcstudents;

SELECT COUNT(city) AS total_cities
FROM jlcstudents;

# Q2 Add all balances
SELECT SUM(feebal) AS total_balance FROM jlcstudents;

# Q3 Find Average balance
SELECT AVG(COALESCE(feebal,0)) AS avg_balance FROM jlcstudents;

# Q4 Find Minimum balance
SELECT MIN(feebal) AS min_balance FROM jlcstudents;

# Q5 Find Maximum balance
SELECT MAX(feebal) AS max_balance FROM jlcstudents;

# Q6 Total Balance per Course
SELECT course, SUM(feebal) AS total_balance
FROM jlcstudents
GROUP BY course;

# Q7 Average Balance per City
SELECT city, AVG(COALESCE(feebal,0)) AS avg_balance
FROM jlcstudents
WHERE city IS NOT NULL
GROUP BY city;

# Q8 Count Students per course
SELECT course, count(*)
FROM jlcstudents
GROUP BY course;

# Q9 Show Courses with total balance > 10000
SELECT course, SUM(feebal) AS total_balance
FROM jlcstudents
GROUP BY course
HAVING total_balance > 10000;

# Q10 Cities with Average balance >= 8000
SELECT city, AVG(COALESCE(feebal,0)) AS avg_bal
FROM jlcstudents
WHERE city IS NOT NULL
GROUP BY city
HAVING avg_bal >= 8000;

# Q11 Multiple conditions
SELECT course, COUNT(*) AS total_students, SUM(feebal) AS total_balance
FROM jlcstudents
GROUP BY course
HAVING total_students >= 2 AND total_balance;

# Q12 Compare COUTN(column) VS COLUMN(*)  # count(*) count all rows including null while count(city)-> count only non null values
SELECT 
	COUNT(city) AS valid_city,
    COUNT(*) AS total_records
FROM jlcstudents;

# Q13 Using COALESCE() to Handle Null Value in Aggregation
SELECT AVG(COALESCE(feebal, 0)) AS avg_balance    /** '10000'  COALESCE replace any NULL value with 0 before Averaging**/
FROM jlcstudents;

SELECT AVG(feebal) AS avg_balance    /** '11666.666666666666' **/    # This is wrong 1400000 / 12 = 11666.6 which is wrong bcz total rows is 14 not 12 so counting is wrong
FROM jlcstudents;

SELECT count(COALESCE(feebal,0)) AS total_feebal_records  ##   14
from jlcstudents;                    ######## correct it will give 14 Instead of wrong value 12 so for Numeric always use COALESCE

SELECT count(feebal) AS total_feebal_records  ##   12 WHich is WRONG because 2 students haveing 0 balance
from jlcstudents;

# Q14 Find Total and Average balance per course, replacing NULL balances with 0
SELECT 
	course,
	SUM(COALESCE(feebal,0)) AS total_balance,   # Ensure courses with NULL value are still included
    AVG(COALESCE(feebal,0)) AS total_average    # COALESCE(feebal,0) treats missing balance as zero before aggregation.
FROM jlcstudents
GROUP BY course;

# Q15 Count Students per city, replacing missing city names with 'Unknown'
SELECT 
COALESCE(city,'UNKNOWN') As city_name,    # Ensure no records are skipped due to missing city information
count(*) AS student_count
FROM jlcstudents
GROUP BY city;
