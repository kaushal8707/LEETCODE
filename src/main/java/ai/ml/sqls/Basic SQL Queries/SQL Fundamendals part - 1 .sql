/** 2.2 - SQL Fundamendals   */
CREATE DATABASE dandesdb;
USE dandesdb;

/** CREATE **/
CREATE TABLE mystudents (
	sid INT PRIMARY KEY,
    sname CHAR(15),
    email CHAR(15),
    phone INT,
    course CHAR(10),
    fee DOUBLE
    );
    
DESC mystudents;
/** INSERT**/
INSERT INTO mystudents VALUES(101,'sri','sri@myjlc',12345,'Java',25000);
INSERT INTO mystudents(sid,sname,email,phone) VALUES(102,'vas','vas@myjlc',54321);
INSERT INTO mystudents(sid,sname,fee) VALUES(103,'sd',10000);
INSERT INTO mystudents VALUES(104,'ds','ds@myjlc',98765,'Python',30000);
INSERT INTO mystudents VALUES(105,'hello','hello@myjlc',67890,'SQL',20000);
INSERT INTO mystudents VALUES(106,'hai','hai@myjlc',11223,'ML',40000);

/** SELECT**/
SELECT * 
FROM mystudents;

SELECT sname, course
FROM mystudents;

SELECT *
FROM mystudents
WHERE course='Python';

/** UPDATE**/
UPDATE mystudents
SET email='sd@myjlc',phone=9999,course='JavaFSD'
WHERE sid=103;

UPDATE mystudents
SET course='DataScienc'
WHERE sid=105;

/**DELETE**/
DELETE FROM mystudents
WHERE sid=103;

DELETE FROM mystudents 
WHERE sname='hello';

/**ALTER**/
ALTER TABLE mystudents
ADD city CHAR(15);

ALTER TABLE mystudents
MODIFY sname VARCHAR(30);

ALTER TABLE mystudents
RENAME COLUMN sname TO name;

ALTER TABLE mystudents
DROP COLUMN city;

/** DROP TABLE**/
DROP TABLE mystudents;

/** TRUNCATE TABLE **/
TRUNCATE TABLE mystudents;

/** 3.2 - SQL Constraints  */

/** NOT NULL constraints **/
CREATE TABLE mycustomers1 (
	cid INT NOT NULL,
    cname CHAR(15) NOT NULL,
    email CHAR(15) NOT NULL,
    phone INT(10),
    city CHAR(15)
);

DESC mycustomers1;

INSERT INTO mycustomers1(cid)         /**cname & email can not be null **/
VALUES(101);

INSERT INTO mycustomers1(cid, cname, email) 
VALUES(101,'sri','sri@jlc.com');

/** UNIQUE constraints **/
CREATE TABLE mycustomers2 (
	cid INT NOT NULL UNIQUE,
    cname CHAR(15) NOT NULL,
    email CHAR(15) NOT NULL UNIQUE,
    phone INT(10) UNIQUE,
    city CHAR(15)
);

INSERT INTO mycustomers2
VALUES(101,'sri','sri@da.com',12345,'Blore');

INSERT INTO mycustomers2
VALUES(102,'sd','sri@da.com',54321,'Blore');

INSERT INTO mycustomers2
VALUES(103,'ds','ds@da.com',NULL,'Blore');

/** PRIMARY KEY constraints **/
CREATE TABLE mycustomers3 (
	cid INT PRIMARY KEY,
    cname CHAR(15) NOT NULL,
    email CHAR(15) NOT NULL UNIQUE,
    phone INT(10) NOT NULL UNIQUE,
    city CHAR(15)
);

INSERT INTO mycustomers3
VALUES(101,'sri','sri@myjlc.com',12345,'Blore');

SELECT * 
FROM mycustomers3
WHERE cid=101;

/** COMPOSITE KEY constraints */
CREATE TABLE students (
	bid CHAR(3),
    sid INT,
    sname CHAR(15) NOT NULL,
    email CHAR(15) NOT NULL UNIQUE,
    phone INT(10) NOT NULL UNIQUE,
    course CHAR(15),
    PRIMARY KEY (bid, sid)
);

INSERT INTO students
VALUES('B1', 101, 'sri', 'sri@myjlc.com', 111, 'Java');

INSERT INTO students
VALUES('B1', 102, 'sd', 'sd@myjlc.com', 222, 'Java');

INSERT INTO students
VALUES('B2', 101, 'adf', 'adf@myjlc.com', 333, 'AdvJava');

SELECT *
FROM students
WHERE bid='B1' AND sid=101;

/** FOREIGN KEY **/
CREATE TABLE mycustomers (
	cid INT PRIMARY KEY,
    cname CHAR(15) NOT NULL,
    email CHAR(15) NOT NULL UNIQUE,
    phone INT(10) UNIQUE,
    city CHAR(15)
);

CREATE TABLE myaccounts (
	mycid INT,
    accno INT PRIMARY KEY,
    actype CHAR(15) NOT NULL,
    branch CHAR(15),
    bal DOUBLE,
    FOREIGN KEY(mycid) REFERENCES mycustomers(cid)
);

CREATE TABLE mytransactions (
	myaccno INT,
    txNumber INT PRIMARY KEY,
    txDate DATE NOT NULL,
    amount DOUBLE,
    txType CHAR(2),
    FOREIGN KEY(myaccno) REFERENCES myaccounts(accno)
);

INSERT INTO mycustomers
VALUES(101, 'sri', 'sri@jlc', 12345, 'Blore');

INSERT INTO myaccounts
VALUES(101, 555, 'SA', 'BTM', 25000);

INSERT INTO mytransactions
VALUES(555, 1, CURDATE(), 2000, 'Cr');

SELECT *
FROM mycustomers;
SELECT *
FROM myaccounts;
SELECT *
FROM mytransactions;

/** DEFAULT constraints **/
CREATE TABLE students1 (
	sid INT PRIMARY KEY,
    sname CHAR(15) NOT NULL,
    email CHAR(15) NOT NULL UNIQUE,
    phone INT(14) NOT NULL UNIQUE,
    course CHAR(15) DEFAULT 'JAVA',
    city CHAR(15) DEFAULT 'Blore',
    temp CHAR(10) NOT NULL DEFAULT 'Hii'
);

INSERT INTO students1(sid, sname, email, phone)
VALUES(101,'sri','sri@jlc',123);

INSERT INTO students1(sid, sname, email, phone,temp)
VALUES(102,'sri1','sri1@jlc',1234,'qwerty');

SELECT * 
FROM students1;

/** CHECK constraints **/
CREATE TABLE students2 (
	sid INT PRIMARY KEY,
    sname CHAR(15) NOT NULL,
    email CHAR(15) NOT NULL UNIQUE,
    phone INT(14) UNIQUE,
    course CHAR(15) DEFAULT 'JAVA',
    totalfee DOUBLE CHECK(totalfee>=25000)
);

INSERT INTO students2 VALUES(101,'sri','sri@jlc',123,'Java',20000);  /** Error Code: 3819. Check constraint 'students2_chk_1' is violated.**/

INSERT INTO students2 VALUES(102,'sd','sd@jlc',456,'Java',30000); 

SELECT * 
FROM mystudents_auto;

/** AUTO INCREMENT **/
CREATE TABLE mystudents_auto (
	sid INT AUTO_INCREMENT PRIMARY KEY,
    sname CHAR(15) NOT NULL,
    email CHAR(15) NOT NULL UNIQUE,
    phone INT(14) UNIQUE,
    course CHAR(15) DEFAULT 'JAVA'
);

INSERT INTO mystudents_auto(sname, email, phone, course)
VALUES('Srinivas','sri@da.com',1234,'ML');

INSERT INTO mystudents_auto(sname, email, phone, course)
VALUES('Hello','hello@da.com',67890,'SQL');

/** SELECT STATEMENT **/
