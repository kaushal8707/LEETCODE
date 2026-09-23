/**
When you write:
	EXPLAIN SELECT * FROM myorders WHERE region = 'East';
	EXPLAIN SELECT * FROM myorders WHERE order_id = 101;
 SQL does NOT run the query.
 It tells you HOW it WOULD run it.

Exploring the Output Columns

	1) id
		 Query step number
		 Higher = deeper subquery.

	2) table
		 Which table is being accessed

This helps you know:
	 Does it use Index?
	 Does it scan entire table?
	 How many rows it expects to process?
	 Which join method it picks?
	 Which step is slowest?
    
	3) type → ACCESS TYPE (VERY IMPORTANT)
		 This tells you how SQL is accessing the table.
		ACCESS TYPE Values — Ordered from BEST to WORST
			a) const (BEST)
			b) eq_ref
			c) ref
			d) range
			e) index
			f) ALL (WORST — FULL TABLE SCAN)

	a) const (BEST)
		 Uses PK or unique index lookup, returns 1 row.
		Example:
		WHERE cid = 10
		 Super fast → constant time lookup.
	b) eq_ref
		 Used for joins on Primary Key or UNIQUE key.
		Example:
		orders.cid = customers.cid
		 Best join performance.
	c) ref
		 Non-unique index lookup.
		Example:
		WHERE city = 'Delhi'
		 Fast, uses index.
	d) range
		 Index is used for a range scan.
		Example:
		WHERE cid BETWEEN 100 AND 500
		WHERE order_date > '2024-01-01'
		 Still very good.
	e) index
		 Scanning full index.
		 Better than full table scan because index is smaller than table.
	f) ALL (WORST — FULL TABLE SCAN)
		 Reads every row.
		Example:
		WHERE LOWER(city) = 'delhi'
		WHERE name LIKE '%raj'
		 Index cannot be used → BAD sign.
	4) key
		 Which index is used
		 Shows the index chosen by optimizer
		 If NULL → index NOT used
		Example:
		key = myindex → Good.
		key = NULL → Index not used → slow.
	5) rows
		 Estimated number of rows scanned
		 Small number → good
		 Huge number → bad
		Example:
		rows = 40 → Perfect.
		rows = 3500000 → Bad → heavy scan.
	6) Extra → additional info
		Important flags:
		 Using index
		- Query satisfied by index alone (covering index) → BEST
		 Using where
		- MySQL applied a filter.
		 Using temporary
		- Temporary table created → usually slow
		- Seen in GROUP BY or ORDER BY
		 Using filesort
		- ORDER BY cannot use index → slow
		 Using join buffer
		- Means join is not using index → slow join
**/

# Putting All Together — Example
# Query:

/**
EXPLAIN SELECT *
FROM myorders
WHERE region = 'East';


# EXPLAIN Result: (without Index)

	id    table     type    key    rows    Extra
	1    myorders   ALL    Null    30      Using where
    
-	 This query works fine on small dataset, but it is not optimized.
		 Full table scan (ALL)
		 No index (key = NULL)
		 Scans all rows (rows = 30)
		 Filtering happens after reading entire table

	- Now Create an index on the WHERE column to fix this.
	  CREATE INDEX myindex on myorders(region);

# EXPLAIN Result: (with Index)

	EXPLAIN SELECT *
	FROM myorders
	WHERE region = 'East';

	id    table    type   key      ref    rows   Extra
	1    myorders  ref    myindex const   10    Null
    
	- Meaning:
		 Using index myindex → good
		 Access type = ref → good
		 rows=10 → only 10 rows scanned
		 This query is optimized.
        
 BAD Example
	SELECT *
	FROM myorders
	WHERE LOWER(region) = 'East';
    
	EXPLAIN Result:
	id    table      type   key   rows       Extra
	1     myorders   ALL    NULL 1,200,000  Using where
    
	- Meaning:
		 Full table scan (ALL)
		 No index used
		 Scanning 1.2 million rows
		 Very Slow
		- Why?
		 Because using LOWER() breaks index.

	WITH cust_totals AS (
		SELECT
		a.cid, ad.city, SUM(a.bal) AS total_balance, COUNT(*) AS account_count
		FROM accounts a
		JOIN address ad ON a.cid = ad.cid
		GROUP BY a.cid, ad.city
		),
		avg_total AS (
		SELECT AVG(total_balance) AS avg_bal
		FROM (
		SELECT cid, SUM(bal) AS total_balance
		FROM accounts
		GROUP BY cid
		) t
		)
		SELECT
		c.cid, c.cname, ct.city, ct.total_balance, ct.account_count
		FROM customers c
		JOIN cust_totals ct ON c.cid = ct.cid
		CROSS JOIN avg_total av
		WHERE ct.total_balance > av.avg_bal
		ORDER BY ct.total_balance DESC
		LIMIT 5;
		DO the following Steps
		- Run the above Query with Explain
		- See the Output ( Better Writedown)
		
	- Create Index

		ALTER TABLE accounts ADD INDEX idx_accounts_cid (cid);
		ALTER TABLE address ADD INDEX idx_address_cid (cid);

		- Run the above Query Again with Explain
		- See the Output ( Better Writedown)
		- Now Compare
        
**/        