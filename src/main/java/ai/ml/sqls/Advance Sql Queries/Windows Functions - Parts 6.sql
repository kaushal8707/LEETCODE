USE dandes_db;
create database dandes_db;
# A) Create Table – myorders

DROP TABLE IF EXISTS myorders;
CREATE TABLE myorders (
  order_id      INT PRIMARY KEY,
  customer_name VARCHAR(50) NOT NULL,
  region        VARCHAR(20) NOT NULL,
  product_name  VARCHAR(50) NOT NULL,
  order_date    DATE NOT NULL,
  quantity      INT NOT NULL,
  unit_price    DECIMAL(10,2) NOT NULL,
  total_amount  DECIMAL(12,2) NOT NULL
);

# B) Insert Sample Data (30 rows)

INSERT INTO myorders
(order_id, customer_name, region, product_name, order_date, quantity, unit_price, total_amount)
VALUES
(101,'Sri','East','Laptop Sleeve',    '2024-01-05',1,5000.00,5000.00),
(102,'Vas','West','Wireless Mouse',   '2024-01-06',1,3000.00,3000.00),
(103,'sd','North','Keyboard',         '2024-01-06',1,7000.00,7000.00),
(104,'ds','South','Stapler',          '2024-01-05',1,3500.00,3500.00),
(105,'Hello','East','Desk Pad',       '2024-01-06',1,1200.00,1200.00),

(106,'Sri','East','Notebook',         '2024-01-07',1,2000.00,2000.00),
(107,'Vas','West','Pen Set',          '2024-01-07',1,1000.00,1000.00),
(108,'sd','North','USB Cable',        '2024-01-08',1,4000.00,4000.00),
(109,'Hai','West','USB Hub',          '2024-01-06',1,2500.00,2500.00),

(110,'Sri','East','Backpack',         '2024-01-09',1,8000.00,8000.00),
(111,'Vas','West','Headphones',       '2024-01-10',1,9000.00,9000.00),
(112,'sd','North','Fiction Book',     '2024-01-07',1,1500.00,1500.00),
(113,'ds','South','Rice Bag',         '2024-01-07',1,2200.00,2200.00),
(114,'Hello','East','Whiteboard',     '2024-01-08',1,1800.00,1800.00),

(115,'Vas','West','Office Chair',     '2024-01-08',1,6500.00,6500.00),
(116,'sd','North','Desk Lamp',        '2024-01-09',1,2500.00,2500.00),
(117,'ds','South','T-Shirt',          '2024-01-09',1,3200.00,3200.00),
(118,'Hai','West','Portable Speaker', '2024-01-09',1,4200.00,4200.00),

(119,'Sri','East','Laptop',           '2024-01-06',1,55000.00,55000.00),
(120,'Hello','East','Phone Stand',    '2024-01-10',1, 900.00, 900.00),
(121,'Hai','West','Desk Organizer',   '2024-01-10',1,1300.00,1300.00),

(122,'sd','North','Coffee Powder',    '2024-01-11',1, 700.00, 700.00),
(123,'ds','South','Jeans',            '2024-01-11',1,5200.00,5200.00),
(124,'Sri','East','External SSD',     '2024-01-10',1,8500.00,8500.00),
(125,'Vas','West','Notebook Pack',    '2024-01-10',1,1200.00,1200.00),

(126,'Sri','East','Mixer Grinder',    '2024-01-12',1,3200.00,3200.00),
(127,'sd','North','Cookbook',         '2024-01-13',1,4100.00,4100.00),
(128,'ds','South','Air Fryer',        '2024-01-13',1,9000.00,9000.00),
(129,'Hello','East','Bluetooth Keyboard','2024-01-12',1,2600.00,2600.00),
(130,'Hai','West','Bag Pack',         '2024-01-12',1,2700.00,2700.00);
INSERT INTO myorders
(order_id, customer_name, region, product_name, order_date, quantity, unit_price, total_amount)
VALUES (131,'Hai','West','Bag Pack',         '2024-02-12',1,2700.00,2700.00);

SELECT * FROM myorders;
####################### ROW_NUMBER() ########################
#Queries
# Q1 List all Orders with a sequence number based on order_date (oldest_order=1)

SELECT order_id, customer_name, region, product_name, order_date, total_amount,
	   ROW_NUMBER() OVER(
			ORDER BY order_date, order_id
	   ) AS row_num
       FROM myorders
       ORDER BY order_date, order_id;
       
# Q2 Writing Q1) with CTE

WITH orders_CTE AS(
	SELECT order_id, customer_name, region, product_name, order_date, total_amount,
    ROW_NUMBER() OVER(
		ORDER BY order_date, order_id
	) AS row_num
    FROM myorders
)
SELECT * 
FROM orders_CTE
ORDER BY order_date, order_id;

# Q3 Writing Q1) With CTE and Showing only rows 11 to 20
WITH orders_CTE AS(
	SELECT order_id, customer_name, region, product_name, order_date, total_amount,
    ROW_NUMBER() OVER(
		ORDER BY order_date, order_id
	) as row_num
    FROM myorders
)
SELECT * 
FROM orders_CTE
WHERE row_num BETWEEN 11 AND 20
ORDER BY row_num;

#Q4 For Each region, order the orders by highest total_amount first, and assign a row number inside each region
#- This is Top - N per group Pattern

SELECT order_id, customer_name, region, product_name, order_date, total_amount,
	ROW_NUMBER() OVER(
		PARTITION BY region
        ORDER BY total_amount DESC
	) AS row_num_in_region
    FROM myorders
    ORDER BY region, row_num_in_region;
    
# Q5 Writing Q4 WIth CTE
WITH region_orders_CTE AS(
	SELECT order_id, customer_name, region, product_name, order_date, total_amount,
	ROW_NUMBER() OVER(
		PARTITION BY region
        ORDER BY total_amount DESC
	) AS row_num_in_region
    FROM myorders
)
SELECT *
FROM region_orders_CTE
ORDER BY region, row_num_in_region;


# Q6 Top 3 Per Region
WITH region_orders_CTE AS(
	SELECT order_id, customer_name, region, product_name, order_date, total_amount,
    ROW_NUMBER() OVER(
		PARTITION BY region
        ORDER BY total_amount DESC
	) AS row_num_in_region
    FROM myorders
)
SELECT *
FROM region_orders_CTE
WHERE row_num_in_region <=3
ORDER BY region, row_num_in_region;


####################### RANK() & DENSE_RANK() ########################
# Queries
# Q1 Rank orders by total_amount (biggest first) accross the entire table

SELECT order_id, customer_name, region, product_name, order_date, total_amount,
	RANK() OVER (
		ORDER BY total_amount DESC
	) AS sale_rank,
    DENSE_RANK() OVER (
		ORDER BY total_amount DESC
	) AS sale_dense_rank
    FROM myorders
    ORDER BY sale_rank;
    
    
# Q2 Writing Q1) WIth CTE
WITH ranked_orders_cte AS(
	SELECT order_id, customer_name, region, product_name, order_date, total_amount,
	RANK() OVER (
		ORDER BY total_amount DESC
	) AS sale_rank,
    DENSE_RANK() OVER (
		ORDER BY total_amount DESC
	) AS sale_dense_rank
    FROM myorders
)
SELECT *
FROM ranked_orders_cte
ORDER BY sale_rank;


# Q3 Top 5 highest orders using ranking
WITH ranked_orders_CTE AS (
	SELECT order_id, customer_name, region, product_name, order_date, total_amount,
    RANK() OVER (
		ORDER BY total_amount DESC
	) AS sale_rank
    FROM myorders
)
SELECT * 
FROM ranked_orders_CTE
WHERE sale_rank <= 5
ORDER BY sale_rank;
    
# Q4 Rank orders within each region (highest first)
# Very useful for "Top orders per region" analysis
SELECT order_id, customer_name, region, product_name, order_date, total_amount,
	RANK() OVER (
		PARTITION BY region 
        ORDER BY total_amount DESC
	) AS region_rank,
	DENSE_RANK() OVER (
		PARTITION BY region 
        ORDER BY total_amount DESC
	) AS region_dense_rank
FROM myorders
ORDER BY region, region_rank;

# Q5 Writing Q4) With CTE
WITH region_ranked_CTE AS(
	SELECT order_id, customer_name, region, product_name, order_date, total_amount,
		RANK() OVER (
			PARTITION BY region 
			ORDER BY total_amount DESC
		) AS region_rank
	FROM myorders
)
SELECT *
FROM region_ranked_CTE
ORDER BY region, region_rank;

# Q6 Top 3 highest orders per region (most common use-case)
WITH region_ranked_cte AS (
	SELECT order_id, customer_name, region, product_name, order_date, total_amount,
    RANK() OVER (
		PARTITION BY region
        ORDER BY total_amount DESC
	) AS region_rank
    FROM myorders
)
SELECT *
FROM region_ranked_cte
WHERE region_rank <= 3
ORDER BY region, region_rank;

####################### NTILE(n) ########################

# Q1 Divide all orders into 4 buckets(quartiles) based on total_amounts
SELECT order_id, customer_name, region, product_name, order_date, total_amount,
	NTILE(4) OVER (
		ORDER BY total_amount DESC
	) AS amount_quartile
FROM myorders
ORDER BY amount_quartile,total_amount DESC;

# Q2 NTILE(3) - Divide orders into 3 performance tiers
SELECT order_id, customer_name, region, total_amount,
	NTILE(3) OVER (
		ORDER BY total_amount DESC
	) AS performance_tier
FROM myorders
ORDER BY performance_tier, total_amount DESC;   # used for High Value Order, Medium Value Order and Low value order

# Q3 NTILE(4) per region (quartiles inside each region)
# Helpful to compare "top performers inside each region"   -> If region wise quartile ranking
SELECT order_id, customer_name, region, total_amount,
	NTILE(4) OVER (
		PARTITION BY region
        ORDER BY total_amount DESC
	) AS region_quartile
FROM myorders
ORDER BY region, region_quartile, total_amount DESC;

# Q4 Decile Distribution
SELECT
	order_id, customer_name, total_amount,
    NTILE(10) OVER (
		ORDER BY total_amount DESC
	) AS decile
FROM myorders
ORDER BY decile, total_amount DESC;

#Practise Test
# In East Region In First Quarter top performer
WITH region_quarter_cte AS (
	SELECT order_id, region, customer_name, total_amount,
    NTILE(4) OVER (
		PARTITION BY region
        ORDER BY total_amount DESC
	) AS per_region_quarter
    FROM myorders
)
SELECT *
FROM region_quarter_cte
WHERE region='East' AND per_region_quarter=1
ORDER BY region, per_region_quarter, total_amount DESC;

######################## LAG() AND LEAD() #########################
# Use case=> compare today's sale with yesterday,  check if sales are increasing or decreasing,  detect spikes or drops
# Oldest -> Newest Order

# Q1 Compare each order's total_amount with previous order (overall timeline)
SELECT order_id, customer_name, order_date, total_amount,
	LAG(total_amount) OVER (
		ORDER BY order_date, order_id
	) AS prev_amount,
	LEAD(total_amount) OVER (
		ORDER BY order_date, order_id
	) AS next_amount
FROM myorders
ORDER BY order_date, order_id;

# Q2 Compare each customer's orders (trend per customer)
# perfect for customer purchase history
SELECT order_id, customer_name,product_name, order_date, total_amount,
	LAG(total_amount) OVER (
		PARTITION BY customer_name
        ORDER BY order_date
	) AS prev_order_amount,
	LEAD(total_amount) OVER (
		PARTITION BY customer_name
        ORDER BY order_date
	) AS next_order_amount
FROM myorders
ORDER BY customer_name, order_date;

# Q3 LAG() with default value (avoid NULLs)
SELECT order_id, order_date, total_amount,
	LAG(total_amount, 1, 0) OVER (
		ORDER BY order_date
	) AS prev_amount_default_0
FROM myorders;

# Q4 LEAD() to predict next expected amount
SELECT order_id, order_date, total_amount,
	LEAD(total_amount, 1, 0) OVER (
		ORDER BY order_date
	) AS next_amount
FROM myorders;

# Q5 Show difference between current and previous order amount (overall-timeline)
SELECT order_id, customer_name, order_date, total_amount,
	LAG(total_amount) OVER (
		ORDER BY order_date
	) AS previous_amount,
    (total_amount - LAG(total_amount) OVER(ORDER BY order_date)) AS diff_from_previous
FROM myorders
ORDER BY order_date;
    
# Q5 WITH CTE
WITH amount_diff_CTE AS (
	SELECT order_id, customer_name, order_date, total_amount,
    LAG(total_amount,1,0) OVER (
		ORDER BY order_date
	) AS prev_amount
    FROM myorders
    ORDER BY order_date
)
SELECT order_id, customer_name, order_date, total_amount, prev_amount,
	 (total_amount - prev_amount) AS diffs
FROM amount_diff_CTE
ORDER BY order_date;

# Q6 Find customers whose purchase amount increased compared to previous order (Positive Trends)
WITH amount_diff_CTE AS (
	SELECT order_id, region, customer_name, order_date, total_amount,
	LAG(total_amount,1,0) OVER (
		PARTITION BY customer_name
        ORDER BY order_date, order_id
	) AS prev_amount
    FROM myorders
)
SELECT order_id, customer_name, order_date, total_amount, prev_amount, (total_amount-prev_amount) AS diffs
FROM amount_diff_CTE
WHERE total_amount > prev_amount
ORDER BY customer_name;

# Q7 Region Wise - Negative Trend
WITH amount_diffs_CTE AS (
	SELECT order_id, region, customer_name, order_date, total_amount,
		LAG(total_amount,1,0) OVER (
			PARTITION BY region
            ORDER BY order_date, order_id
		) AS prev_amount
	FROM myorders
)
SELECT order_id, customer_name, region, order_date, total_amount, prev_amount, (total_amount-prev_amount) AS diffs
FROM amount_diffs_CTE
WHERE total_amount < prev_amount
ORDER BY region;

############################# Running Total ( Cumulative SUM ######################

# Q1 Running Total of all orders (full timeline)
SELECT order_id, customer_name, order_date, total_amount,
	SUM(total_amount) OVER (
		ORDER BY order_date , order_id
	) AS running_total
FROM myorders
ORDER BY order_date , order_id;

# Q2 Running Total per Region
SELECT order_id, region, customer_name, order_date, total_amount,
	SUM(total_amount) OVER (
		PARTITION BY region
        ORDER BY order_date, order_id
	)AS region_running_total
FROM myorders
ORDER BY region, order_date;

# Q3 Running Total per Customers
SELECT order_id, customer_name, order_date, total_amount,
	SUM(total_amount) OVER (
		PARTITION BY customer_name
        ORDER BY order_date
	) AS customer_running_total
FROM myorders
ORDER BY customer_name, order_date;

# Q4) Running Total per customer using CTE
WITH customer_running_total_CTE AS	(
	SELECT order_id, customer_name, order_date, total_amount,
		SUM(total_amount) OVER (
			PARTITION BY customer_name
            ORDER BY order_date
		) AS per_customer_running_total
	FROM myorders
)
SELECT * 
FROM customer_running_total_CTE
ORDER BY customer_name, order_date;

#############################( Moving Total & Moving Average( Rolling Windows ) ######################

# Q1 3-Row Moving Total (rolling sum)
SELECT order_id, customer_name, order_date, total_amount,
	SUM(total_amount) OVER (
		ORDER BY order_date, order_id
        ROWS BETWEEN 2 PRECEDING AND CURRENT ROW
	) AS moving_total_3
FROM myorders
ORDER BY order_date, order_id;

# Q2 3-Row Moving Average (rolling average)
WITH moving_avg_CTE AS (
	SELECT order_id, customer_name, order_date, total_amount,
		AVG(total_amount) OVER (
			ORDER BY order_date, order_id
            ROWS BETWEEN 2 PRECEDING AND CURRENT ROW
		) AS moving_avg
	FROM myorders
    ORDER BY order_date
)
SELECT order_id, customer_name, order_date, total_amount, ROUND(moving_avg) 
FROM moving_avg_CTE;

# Q3 Moving Total per region
SELECT order_id, customer_name, region, order_date, total_amount,
	SUM(total_amount) OVER (
		PARTITION BY region
        ORDER BY order_date, order_id
        ROWS BETWEEN 2 PRECEDING AND CURRENT ROW
	) AS region_moving_total_3
FROM myorders
ORDER BY region, order_date;
        
# Q4 7-Row Moving Total (Weekly Trend)
SELECT order_id, customer_name, region, order_date, total_amount,
	SUM(total_amount) OVER (
		ORDER BY order_date
        ROWS BETWEEN 6 PRECEDING AND CURRENT ROW
	) AS moving_total_7
FROM myorders;

# Q5 7-Row Moving Total(Weekly Trend) - Q4 with CTE
WITH weekly_trend_CTE AS (
	SELECT order_id, order_date, total_amount,
		SUM(total_amount) OVER (
			ORDER BY order_date, order_id
			ROWS BETWEEN 6 PRECEDING AND CURRENT ROW
		) AS moving_total_7
	FROM myorders
)
SELECT *
FROM weekly_trend_CTE;

# Q6 Moving Totals & Average using CTE
WITH moving_CTE AS (
	SELECT order_id, order_date, total_amount,
		SUM(total_amount) OVER (
				ORDER BY order_date
                ROWS BETWEEN 2 PRECEDING AND CURRENT ROW
		) AS moving_total_3,
		AVG(total_amount) OVER (
				ORDER BY order_date
                ROWS BETWEEN 2 PRECEDING AND CURRENT ROW
		) AS moving_avg_3
	FROM myorders
)
SELECT *
FROM moving_CTE
ORDER BY order_date;

####################### Frame Clauses ( ROWS BETWEEN.....  RANGE BETWEEN.....)######################
# Unbounded Preceding / Unbounded Following / N Preceding / N Following

# Q1 Running Total (default frame)
# - Uses UNBOUNDED PRECEDING automatically
SELECT order_id,order_date, total_amount,
	SUM(total_amount) OVER (
		ORDER BY order_date
        ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW
	) AS running_total
FROM myorders
ORDER BY order_date;

SELECT order_id,order_date, total_amount,
	SUM(total_amount) OVER (
		ORDER BY order_date
        RANGE BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW
	) AS running_total
FROM myorders
ORDER BY order_date;

# Q2 Running Total
SELECT order_id,order_date, total_amount,
	SUM(total_amount) OVER (
		ORDER BY order_date
        ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW
	) AS moving_total
FROM myorders
ORDER BY order_date;

# Q3 3-Rows Moving Total
SELECT order_id,order_date, total_amount,
	SUM(total_amount) OVER (
		ORDER BY order_date, order_id
        ROWS BETWEEN 2 PRECEDING AND CURRENT ROW
	) AS moving_total_3
FROM myorders
ORDER BY order_date, order_id;

# Q4 3-Row Window centered around current row
SELECT order_id,order_date, total_amount,
	SUM(total_amount) OVER (
		ORDER BY order_date, order_id
        ROWS BETWEEN 1 PRECEDING AND 1 FOLLOWING
	) AS centered_total
FROM myorders
ORDER BY order_date, order_id;

#Q5 UNBOUNDED FOLLOWING
# Backward Cumulative totals
SELECT order_id,order_date, total_amount,
	SUM(total_amount) OVER (
		ORDER BY order_date, order_id
        ROWS BETWEEN CURRENT ROW AND UNBOUNDED FOLLOWING
	) AS backward_running_total
FROM myorders;

# Q6 Frame Clause with PARTITION BY
SELECT region, order_id, total_amount,
	SUM(total_amount) OVER (
		PARTITION BY region
		ORDER BY order_date
        ROWS BETWEEN 2 PRECEDING AND CURRENT ROW
	) AS region_moving_total_3
FROM myorders
ORDER BY region, order_date;

########## ROWS vs RANGE — Key Difference ####################
-- Create Table
CREATE TABLE orders (
    order_id INT PRIMARY KEY,
    order_date DATE,
    total_amount INT
);

-- Insert Sample Data
INSERT INTO orders (order_id, order_date, total_amount) VALUES
(1, '2025-01-01', 100),
(2, '2025-01-02', 200),
(3, '2025-01-02', 300),
(4, '2025-01-03', 400),
(5, '2025-01-04', 500);

SELECT * FROM orders;

# Case 1: Using ROWS
SELECT order_id, order_date, total_amount,
		SUM(total_amount) OVER (
			ORDER BY order_date
            ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW
		) AS row_sum
FROM myorders;


# Case 2: Using RANGE
SELECT order_id, order_date, total_amount,
		SUM(total_amount) OVER (
			ORDER BY order_date
            RANGE BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW
		) AS row_sum
FROM myorders;

############################ FIRST_VALUE() & LAST_VALUE() ################################
# Q1 FIRST_VALUE : Customer's first order amount
SELECT customer_name, order_id, order_date, total_amount,
	FIRST_VALUE(total_amount) OVER (
		PARTITION BY customer_name
        ORDER BY order_date
	) AS first_order_amount
FROM myorders
ORDER BY customer_name, order_date;

# Q2 LAST_VALUE (with correct frame) : Customers latest order amount
SELECT customer_name, order_id, order_date, total_amount,
	LAST_VALUE(total_amount) OVER (
		PARTITION BY customer_name
        ORDER BY order_date
        ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING
	) AS last_order_amount
FROM myorders
ORDER BY customer_name, order_date;

# Q3 Compare each customer's current order with first order
SELECT customer_name, order_id, order_date, total_amount,
	FIRST_VALUE(total_amount) OVER (
		PARTITION BY customer_name
        ORDER BY order_date
	) AS first_order_amount,
    total_amount - 
    FIRST_VALUE(total_amount) OVER (
		PARTITION BY customer_name
        ORDER BY order_date
	) AS diff_from_first
FROM myorders
ORDER BY customer_name, order_date;

# Q4 Writing Q3 with CTE
WITH orders_with_first AS (
	SELECT customer_name, order_id, order_date, total_amount,
	FIRST_VALUE(total_amount) OVER (
		PARTITION BY customer_name
        ORDER BY order_date
	) AS first_order_amount
    FROM myorders
)
SELECT customer_name, order_id, order_date, total_amount, first_order_amount,
       total_amount - first_order_amount AS diff_from_first
FROM orders_with_first
ORDER BY customer_name,order_date;
		
# Q5 Region - wise first and last sale amounts 
SELECT customer_name, region,order_id, order_date, total_amount,
	   FIRST_VALUE(total_amount) OVER (
			PARTITION BY region
            ORDER BY order_date
		) AS region_first_sale,
	   LAST_VALUE(total_amount) OVER (
			PARTITION BY region
            ORDER BY order_date
            ROWS BETWEEN UNBOUNDED PRECEDINg AND UNBOUNDED FOLLOWING
		) AS region_last_sale
FROM myorders
ORDER BY region, order_date;
        
# Q6 Add trend between first and last order
WITH orders_with_first_last AS (
	SELECT customer_name, region,order_id, order_date, total_amount,
	   FIRST_VALUE(total_amount) OVER (
			PARTITION BY region
            ORDER BY order_date
            ROWS BETWEEN UNBOUNDED PRECEDINg AND UNBOUNDED FOLLOWING
		) AS first_order_amount,
	   LAST_VALUE(total_amount) OVER (
			PARTITION BY region
            ORDER BY order_date
            ROWS BETWEEN UNBOUNDED PRECEDINg AND UNBOUNDED FOLLOWING
		) AS last_order_amount
	FROM myorders
)
SELECT customer_name, region,order_id, order_date, total_amount, first_order_amount, last_order_amount,
CASE
	WHEN last_order_amount > first_order_amount THEN 'Increased'
    WHEN last_order_amount < first_order_amount THEN 'Decreased'
    ELSE 'No Change'
END AS life_time_trend
FROM orders_with_first_last
ORDER BY customer_name, order_date;


# Important
# Running Percentage ( % Contributions )
# Q1 Running Percentage of Total Sales ( using CTE )
WITH sales_running AS (
	SELECT
		order_id, order_date, total_amount,
		SUM(total_amount) OVER(		                               #1) Running total by date + order
			ORDER BY order_date, order_id
		) AS running_total,
        SUM(total_amount) OVER() AS total_sum	                   #2) Total Sum of All orders
    FROM myorders
)
SELECT order_id, order_date, total_amount, running_total, total_sum,
       ROUND( (running_total * 100.0) / total_sum, 2) AS running_percentage
FROM sales_running
ORDER BY order_date, order_id;


# Q2 Running % by Region (using CTE)
WITH region_sale_running_CTE AS (
	SELECT region, order_id, order_date, total_amount,
	SUM(total_amount) OVER (				# running total for each region
		PARTITION BY region
        ORDER BY order_date, order_id
	) AS region_running_total,
     SUM(total_amount) OVER(                # total sum per region
		PARTITION BY region
	) AS region_total_sum
    FROM myorders
)
SELECT region, order_id, order_date, total_amount, region_running_total, region_total_sum,
       ROUND((region_running_total * 100.0) / region_total_sum, 2) AS region_running_percentage
FROM region_sale_running_CTE
ORDER BY region, order_date, order_id;

# Q3 Running % per Customer ( Lifetime Contribution)
WITH customer_running AS (
	SELECT customer_name, order_id, order_date, total_amount,
		SUM(total_amount) OVER (                 -- Running total for each customer
			PARTITION BY customer_name
			ORDER BY order_date
		) AS customer_running_total,
		SUM(total_amount) OVER(                  -- Total purchase value for each customer
			PARTITION BY customer_name
		) AS customer_total_sum
	FROM myorders
)
SELECT customer_name, order_id, order_date, total_amount, customer_running_total, customer_total_sum,
ROUND((customer_running_total * 100) / customer_total_sum, 2) AS customer_running_percentage
FROM customer_running
ORDER BY customer_name, order_date;
    
# Q4 Running Percentage of Sales per Month
WITH monthly_sales_running AS (
	SELECT order_id, order_date, total_amount,
		DATE_FORMAT(order_date, '%Y-%m') AS order_month,
		SUM(total_amount) OVER (                      # Running total inside each month
			PARTITION BY DATE_FORMAT(order_date, '%Y-%m')
            ORDER BY order_date, order_id
		) AS month_running_total,
		SUM(total_amount) OVER(						  # Total sale for the month
			PARTITION BY DATE_FORMAT(order_date, '%Y-%m')
		) AS month_total_sum
	FROM myorders
)
SELECT order_month, order_id, order_date, total_amount, month_running_total, month_total_sum,
      ROUND ((month_running_total * 100) / month_total_sum, 2) AS month_running_percentage
FROM monthly_sales_running
ORDER BY order_month, order_date, order_id;

# Q5 Top Contributors ; 80% Cumulative Contribution (Pareto 80 / 20)
# one of the most common business analytics queries
WITH sales_running AS (
	SELECT 
		order_id,
		customer_name,
        order_date,
        total_amount,
        SUM(total_amount) OVER (           # running total sorted by highest value
			ORDER BY total_amount DESC
		) AS running_total,
        SUM(total_amount) OVER() AS total_sum    # total revenue
	FROM myorders
)
SELECT * 
FROM sales_running
WHERE ( running_total * 100.0) / total_sum <=80
ORDER BY total_amount DESC;


