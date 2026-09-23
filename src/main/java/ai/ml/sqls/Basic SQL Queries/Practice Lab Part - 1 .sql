CREATE DATABASE practicedb;
USE practicedb;

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

# Q1 Display Full Info of all Students

	SELECT * FROM mystudents;
    
# Q2 Display name, phone and balance of all Students
    
    SELECT sname, phone, bal
    FROM mystudents;
    
# Q3 Display name and course of all Students

	SELECT sname, course
    FROM mystudents;
    
# Q4 Display student whose sid  = 105

	SELECT * FROM mystudents
    WHERE sid = 105;
    
# Q5 Display student from Bangalore
	
    SELECT * FROM mystudents
    WHERE city = 'Blore';
    
# Q6 Students Staying in Hyd or Blore

	SELECT * FROM mystudents
    WHERE city IN ('Hyd', 'Blore');
    
    #####or######
    
    SELECT * FROM mystudents
    WHERE city = 'Blore' OR city = 'Hyd';

# Q7 Students Staying in Hyd, Delhi or Pune

	SELECT * FROM mystudents
    WHERE city IN ('Hyd', 'Delhi', 'Pune');
    
# Q8 Students not Staying in Hyd or Blore

	SELECT * FROM mystudents
    WHERE city NOT IN ('Hyd', 'Blore');
    
# Q9 Students whose balance > 5000

	SELECT * FROM mystudents
    WHERE bal > 5000;

# Q10 Students whose balance <= 5000

	SELECT * FROM mystudents
    WHERE bal <= 5000;
    
# Q11 Students whose balance is between 8000 and 20000

	SELECT * FROM mystudents
    WHERE bal BETWEEN 8000 AND 20000;
    
# Q12 Students whose balance > 8000 and < 20000

	SELECT * FROM mystudents
    WHERE bal > 8000 AND bal < 20000;
    
# Q13 Students whose balance <= 8000 or >= 20000

	SELECT * FROM mystudents
    WHERE bal <= 8000 OR bal >= 20000;
    
# Q14 Students whose balance is NOT between 8000 and 20000

	SELECT * FROM mystudents
    WHERE bal NOT BETWEEN 8000 AND 20000;

# Q15 Students whose balance >= 15000 and city = 'Blore'

	SELECT * FROM mystudents
	WHERE bal >= 15000 AND city = 'Blore'

# Q16 Students whose balance >= 5000, city = 'Blore' and course = 'AWS'

    SELECT * FROM mystudents
    WHERE bal >= 5000 AND city = 'Blore' AND course = 'AWS';
    
# Q17 Students who joined Java, AWS or Python

	SELECT * FROM mystudents
    WHERE course IN ('Java', 'AWS', 'Python');

# Q18 Bangalore Students who joined Java, AWS or Python

	SELECT * FROM mystudents
    WHERE city = 'Blore' AND course IN ('Java', 'AWS', 'Python');
    
# Q19 Students from Blore or Delhi, joined Java/AWS, and balance between 8000-20000

	SELECT * FROM mystudents
    WHERE city IN ('Blore', 'Dehi') 
    AND course IN ('Java', 'AWS')
    AND bal BETWEEN 8000 AND 20000;
    
# Q20 Display all students sorted by name(ascending)

	SELECT * FROM mystudents 
    ORDER BY sname;
    
# Q21 Display all students sorted by balance(descending)

	SELECT * FROM mystudents 
	ORDER by bal DESC;
    
# Q22 Display all students sorted by city(ascending) and balance(descending)

	SELECT * FROM mystudents 
	ORDER BY city ASC, bal DESC;

# Q23 Display all students sorted by course(ascending) and name(ascending)

	SELECT * FROM mystudents
    ORDER BY course, sname;