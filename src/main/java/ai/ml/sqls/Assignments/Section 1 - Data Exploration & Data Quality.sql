/******************************************************************************************
  # IMDB Movies Database - SQL Assignment
  # Section 1: Data Exploration & Data Quality (Q1 – Q10)
  # Focus:
  #   - Understanding table shapes
  #   - Checking NULLs and missing values
  #   - Basic filtering and column-level exploration
  #   - Distinct values
  #   - Data completeness checks
******************************************************************************************/
USE myda_imdb;

# Q1) Find the total number of rows in each table of the schema.
/* Output format:
+---------------------+-------------+
| table_name          | row_count   |
+---------------------+-------------+
| movie               |   7997      |
| genre               |  23684      |
| names               |  23432      |
| director_mapping    |   7997      |
| role_mapping        |  40000      |
| ratings             |   7600      |
+---------------------+-------------+
*/

SELECT 'movie' AS table_name, count(*) AS row_count FROM movie
UNION
SELECT 'genre' AS table_name, count(*) AS row_count FROM genre
UNION
SELECT 'names' AS table_name, count(*) AS row_count FROM names
UNION
SELECT 'director_mapping' AS table_name, count(*) AS row_count FROM director_mapping
UNION
SELECT 'role_mapping' AS table_name, count(*) AS row_count FROM role_mapping
UNION
SELECT 'ratings' AS table_name, count(*) AS row_count FROM ratings; 


# Q2) Which columns in the movie table have NULL values? Show NULL count per column.
/* Output format:
+-----------------------+-------------+
| column_name           | null_count  |
+-----------------------+-------------+
| year                  |   650       |
| date_published        |   300       |
| duration              |    45       |
| country               |   780       |
| worldwide_gross_income|  1200       |
| languages             |   500       |
| production_company    |  1500       |
+-----------------------+-------------+
*/

SELECT 
	SUM(year IS NULL) AS year_null_count,
    SUM(date_published IS NULL) AS date_published_null_count,
    SUM(duration IS NULL) AS duration_null_count,
    SUM(country IS NULL) AS country_null_count,
    SUM(worldwide_gross_income IS NULL) AS worldwide_gross_income_null_count,
    SUM(languages IS NULL) AS languages_null_count,
    SUM(production_company IS NULL) AS production_company_null_count
 FROM movie;
 
 
# Q3) Find the total number of movies released each year + month-wise trend.
/* Output format (part 1: year-wise):
+-----------+------------------+
| year      | number_of_movies |
+-----------+------------------+
| 2017      |      2134        |
| 2018      |      2100        |
| 2019      |      2050        |
+-----------+------------------+

Output format (part 2: month-wise):
+-----------+------------------+
| month_num | number_of_movies |
+-----------+------------------+
| 1         |      134         |
| 2         |      231         |
| 3         |      310         |
+-----------+------------------+
*/

-- Part 1: Year-wise movies
SELECT year, COUNT(*) AS number_of_movie_released
FROM movie
GROUP BY year;

-- Part 2: Month-wise movies

SELECT 
	date_format(date_published, '%m-%Y') AS release_of_month, 
	COUNT(*) AS number_of_movie_released
FROM movie
GROUP BY release_of_month;


# Q4) How many movies were produced in USA or India in 2019?
/* Output format:
+-----------+-------------+
| country   | movie_count |
+-----------+-------------+
| USA       |    340      |
| India     |    420      |
+-----------+-------------+
*/

select 
	country, 
    COUNT(*) AS Movie_Produced
FROM movie
WHERE country IN('USA', 'India') AND year = 2017
GROUP BY country;


# Q5) Find the unique list of genres in the dataset.
/* Output format:
+-----------+
| genre     |
+-----------+
| Drama     |
| Action    |
| Comedy    |
| Thriller  |
+-----------+
*/

SELECT DISTINCT(genre) AS list_of_genre
FROM genre;

# Q6) Which genre had the highest number of movies produced overall?
/* Output format:
+-----------+-------------+------------+
| genre     | movie_count | genre_rank |
+-----------+-------------+------------+
| Drama     |    2312     |     1      |
+-----------+-------------+------------+
*/

WITH movie_genre_cte AS(
	SELECT genre, COUNT(*) AS movie_count,
    	   RANK() OVER(
				ORDER BY COUNT(*) DESC
		  ) AS genre_rank
	FROM genre
    GROUP BY genre
)
SELECT genre, movie_count, genre_rank
FROM movie_genre_cte
WHERE genre_rank <= 1;


# Q7) How many movies belong to only one genre?
/* Output format:
+-------------+
| movie_count |
+-------------+
|    3050     |
+-------------+
*/

SELECT genre, COUNT(movie_id) AS movie_count
FROM genre 
GROUP BY genre 
ORDER BY movie_count DESC;


# Q8) What is the average duration of movies in each genre?
/* Output format:
+-----------+---------------+
| genre     | avg_duration  |
+-----------+---------------+
| Drama     |    106.70     |
| Thriller  |    105.40     |
+-----------+---------------+
*/
-- Type your code below:

SELECT 
	g.genre, 
    ROUND(AVG(m.duration), 2) AS avg_duration
FROM genre g
INNER JOIN movie m
ON g.movie_id = m.id
GROUP BY g.genre;



# Q9) Rank the ‘Thriller’ genre among all genres by number of movies.
/* Output format:
+-----------+-------------+------------+
| genre     | movie_count | genre_rank |
+-----------+-------------+------------+
| Thriller  |    1458     |     3      |
+-----------+-------------+------------+
*/
-- Type your code below:

WITH thriller_genre_cte AS (
	SELECT 
		genre, COUNT(*) AS movie_count,
        RANK() OVER (
			ORDER BY COUNT(*) DESC
		) AS genre_rank
	FROM genre
    GROUP BY genre
)
SELECT genre, movie_count, genre_rank
FROM thriller_genre_cte
WHERE genre = 'Thriller';
			
																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																						

# Q10) List the top 10 longest movies by duration along with their year.
/* Output format:
+------------+------------------------------+----------+------+
| movie_id   | title                        | duration | year |
+------------+------------------------------+----------+------+
| tt1234567  | Some Very Long Movie         |   210    | 2018 |
| tt2345678  | Another Long Movie           |   205    | 2019 |
| ...        | ...                          |   ...    | ...  |
+------------+------------------------------+----------+------+
*/
-- Type your code below:
# USING RANK
WITH largest_movie_cte AS(
	select id AS movie_id, 
	   title, 
       duration, 
       year,
       RANK() OVER (
			ORDER BY duration DESC
		) AS duration_rank
	FROM movie
)
SELECT movie_id,  title, duration, year
FROM largest_movie_cte
WHERE duration_rank < 11;


	# Alternatively
    

# Using LIMIT
select id AS movie_id, 
	   title, 
       duration, 
       year
FROM movie
ORDER BY duration DESC
LIMIT 10;



