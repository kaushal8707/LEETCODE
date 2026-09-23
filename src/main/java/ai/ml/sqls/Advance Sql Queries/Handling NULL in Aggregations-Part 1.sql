USE dandesdb;

# Step 1: Create Table

CREATE TABLE mystudents (
    sid INT PRIMARY KEY,
    sname VARCHAR(20),
    email VARCHAR(30),
    phone BIGINT,
    course VARCHAR(20),
    bal DECIMAL(10,2)
);

# Step 2: Insert Sample Records (with NULLs)

INSERT INTO mystudents VALUES
(101, 'sri',   'sri@da.com',    9991112222, 'SQL',     12000),
(102, 'sd',    'sd@da.com',     NULL,       'SQL',     15000),
(103, 'ds',    'ds@da.com',     8885554444, 'Python',  NULL),
(104, 'vas',   'vas@da.com',    7776663333, 'Python',  8000),
(105, 'hello', 'hello@da.com',  NULL,       'AI/ML',  10000),
(106, 'hai',   'hai@da.com',    9990001111, 'AI/ML',   NULL),
(107, 'aaa',   'aaa@da.com',    8880001111, 'DL',     20000),
(108, 'bbb',   'bbb@da.com',    NULL,       'DL',         0),
(109, 'ccc',   'ccc@da.com',    9995556666, NULL,      NULL),
(110, 'ddd',   'ddd@da.com',   12345, NULL,      NULL);

SELECT * FROM mystudents;
# group by course and handle missing course as Unknown

# percentage of students with phone per course
select round(count(phone)/count(*)*100,2) as percent_with_phone,
		coalesce(course,'Unknow') as course
from mystudents
group by coalesce(course,'Unknow')



