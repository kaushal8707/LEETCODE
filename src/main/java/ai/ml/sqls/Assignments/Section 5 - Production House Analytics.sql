/******************************************************************************************
  # IMDB Movies Database - SQL Assignment
  # Section 5: Production House Analytics (Q39 – Q48)
  # Focus:
  #   - Ranking production houses
  #   - Revenue-based analysis
  #   - Hit-movie analysis (avg_rating > 8)
  #   - Genre-wise strengths of production houses
  #   - Identifying consistently high-performing companies
******************************************************************************************/
USE myda_imdb;


# Q39) Which production house has produced the most number of hit movies (avg_rating > 8)?
#      Show rank also. If more than one at rank 1, show all.

/* Output format:
+------------------------+-------------+-------------------+
| production_company     | movie_count | prod_company_rank |
+------------------------+-------------+-------------------+
| Dream Warrior Pictures |      3      |         1         |
| National Theatre Live  |      3      |         1         |
+------------------------+-------------+-------------------+
*/

WITH production_house_hit_cte AS (
	SELECT production_company, COUNT(*) AS  movie_count,
		   DENSE_RANK() OVER (
				ORDER BY COUNT(*) DESC
		   ) AS prod_company_rank
	FROM movie m
	INNER JOIN ratings r
	ON  m.id = r.movie_id
	WHERE production_company IS NOT NULL AND r.avg_rating > 8
	GROUP BY production_company
)
SELECT production_company, movie_count, prod_company_rank
FROM production_house_hit_cte
WHERE prod_company_rank <=1;



# Q40) Which are the top three production houses based on the total number of votes 
#      received by their movies?
/* Output format:
+------------------------+------------+-------------------+
| production_company     | vote_count | prod_comp_rank    |
+------------------------+------------+-------------------+
| Marvel Studios         |   985432   |         1         |
| Warner Bros.           |   865210   |         2         |
| Yash Raj Films         |   754321   |         3         |
+------------------------+------------+-------------------+
*/


WITH top_three_prod_house_cte AS (
	SELECT production_company, SUM(total_votes) AS vote_count,
		DENSE_RANK() OVER (
			ORDER BY SUM(total_votes) DESC
		) AS prod_comp_rank
	FROM movie mv 
	INNER JOIN ratings rt 
	ON mv.id = rt.movie_id
	GROUP BY production_company
)
SELECT production_company, vote_count, prod_comp_rank
FROM top_three_prod_house_cte
WHERE prod_comp_rank <= 3;


# Q41) Which are the five highest-grossing movies of each year that belong to the top three genres?
#      Top 3 genres = genres with highest movie count overall.

/* Output format:
+-----------+------+---------------------------+------------------------+-----------+
| genre     | year | movie_name                | worldwide_gross_income | movie_rank|
+-----------+------+---------------------------+------------------------+-----------+
| Drama     | 2017 | Movie A                   | $123,456,789           |     1     |
| Drama     | 2017 | Movie B                   | $ 98,000,000           |     2     |
| ...       | ...  | ...                       | ...                    |   ...     |
+-----------+------+---------------------------+------------------------+-----------+
*/


WITH highest_grossing_movie_year_cte AS (
	SELECT genre1.genre, movie1.year, movie1.title, worldwide_gross_income,
		   DENSE_RANK() OVER (
			ORDER BY worldwide_gross_income DESC
        ) AS movie_rank
	FROM movie movie1
	INNER JOIN genre genre1 ON movie1.id = genre1.movie_id
	INNER JOIN (
		SELECT genre, COUNT(*) AS movie_count
		FROM genre genre2
		INNER JOIN movie movie2 
		ON genre2.movie_id = movie2.id
		GROUP BY genre
		ORDER BY  COUNT(*) DESC
		LIMIT 3
	) AS top_3_genres
	ON genre1.genre = top_3_genres.genre
)
SELECT genre, year, title, worldwide_gross_income, movie_rank
FROM highest_grossing_movie_year_cte
WHERE movie_rank <= 5;


# Q42) Which are the top two production houses that have produced the highest number of hits
#      (median_rating >= 8) among multilingual movies?
#      Multilingual = languages column contains a comma.

/* Output format:
+------------------------+-------------+-------------------+
| production_company     | movie_count | prod_comp_rank    |
+------------------------+-------------+-------------------+
| National Theatre Live  |     4       |         1         |
| Marvel Studios         |     3       |         2         |
+------------------------+-------------+-------------------+
*/

WITH highest_hit_multilingual_cte AS (
	SELECT m.production_company, COUNT(*) AS movie_count,
    DENSE_RANK() OVER (
		ORDER BY COUNT(*) DESC
    ) AS prod_comp_rank
    FROM movie m
    INNER JOIN ratings r ON m.id = r.movie_id
    WHERE r.median_rating >= 8 AND languages LIKE '%,%' AND production_company IS NOT NULL
    GROUP BY m.production_company
    ORDER BY COUNT(*) DESC
)
SELECT production_company, movie_count, prod_comp_rank
FROM highest_hit_multilingual_cte
WHERE prod_comp_rank <= 2;


# Q43) For each production house with at least 5 movies, find the average movie rating.
/* Output format:
+------------------------+-------------+------------+
| production_company     | movie_count | avg_rating |
+------------------------+-------------+------------+
| Marvel Studios         |     10      |    7.98    |
| Warner Bros.           |     15      |    7.85    |
+------------------------+-------------+------------+
*/


WITH production_house_movie_average_rating_cte AS (
	SELECT m1.production_company, r1.avg_rating, pc_with_atleast_5_movie.movie_count
	FROM movie m1
	INNER JOIN ratings r1 ON m1.id=r1.movie_id
	INNER JOIN (
		SELECT m2.production_company, COUNT(*) AS movie_count
		FROM movie m2
		GROUP BY m2.production_company
		HAVING COUNT(*) > 5
	) AS pc_with_atleast_5_movie
	ON m1.production_company = pc_with_atleast_5_movie.production_company
)
SELECT production_company, movie_count, avg_rating
FROM production_house_movie_average_rating_cte
ORDER BY avg_rating DESC;


# Q44) For each production house, find the total number of movies and the average duration.
/* Output format:
+------------------------+-------------+---------------+
| production_company     | movie_count | avg_duration  |
+------------------------+-------------+---------------+
| Marvel Studios         |     10      |    128.50     |
| Dharma Productions     |      7      |    135.20     |
+------------------------+-------------+---------------+
*/

SELECT production_company, COUNT(*) AS movie_count, ROUND(AVG(duration), 2) AS avg_duration
FROM movie m 
WHERE production_company IS NOT NULL
GROUP BY production_company
ORDER BY COUNT(*) DESC;


# Q45) Which production house released the highest number of movies in a single year?
#      If there is a tie, show all such (production_company, year) combinations.

/* Output format:
+------------------------+------+-------------+
| production_company     | year | movie_count |
+------------------------+------+-------------+
| Marvel Studios         | 2018 |      6      |
| Dharma Productions     | 2019 |      6      |
+------------------------+------+-------------+
*/

WITH production_house_release_per_year_cte AS (
	SELECT production_company, year, COUNT(*) AS movie_count,
		   DENSE_RANK() OVER (
				PARTITION BY year 
				ORDER BY COUNT(*) DESC
		   ) AS movie_count_rank
	FROM movie
	WHERE production_company IS NOT NULL
	GROUP BY production_company, year
)
SELECT production_company, year, movie_count
FROM production_house_release_per_year_cte
WHERE movie_count_rank <= 1;


# Q46) For each production house, find its highest-rated movie (by avg_rating).
#      If more than one movie shares the same highest rating, show all of them.

/* Output format:
+------------------------+-----------------------------+------------+
| production_company     | title                       | avg_rating |
+------------------------+-----------------------------+------------+
| Marvel Studios         | Avengers: Endgame           |    8.4     |
| Marvel Studios         | Avengers: Infinity War      |    8.4     |
| Yash Raj Films         | Chak De! India              |    8.2     |
+------------------------+-----------------------------+------------+
*/

WITH production_house_highest_rated_movie_cte AS (
	SELECT m1.production_company, m1.title, max_rating_prod_company.avg_ratings
	FROM movie m1 
	INNER JOIN (
		SELECT m2.production_company, MAX(avg_rating) AS avg_ratings
		FROM movie m2 
		INNER JOIN ratings r2 
		ON m2.id = r2.movie_id
		GROUP BY production_company
	) AS max_rating_prod_company
	ON m1.production_company = max_rating_prod_company.production_company
)
SELECT production_company, title, avg_ratings
FROM production_house_highest_rated_movie_cte;


# Q47) Find the top 10 production houses by total worldwide gross income.
#      Use the numeric part of worldwide_gross_income (remove $, €, and commas).
/* Output format:
+------------------------+--------------------+
| production_company     | total_income_num   |
+------------------------+--------------------+
| Marvel Studios         |      3500000000    |
| Disney                 |      3200000000    |
| Warner Bros.           |      2900000000    |
+------------------------+--------------------+
*/

WITH temp_cte AS (
	SELECT production_company, SUM(REPLACE(worldwide_gross_income, '$','')) AS total_income_num,
           RANK() OVER (
				ORDER BY SUM(REPLACE(worldwide_gross_income, '$','')) DESC
           ) AS gross_income_rank
	FROM movie
	GROUP BY production_company
)
SELECT production_company, total_income_num
FROM temp_cte
WHERE total_income_num IS NOT NULL AND gross_income_rank <= 10;


# Q48) For the 'Thriller' genre, find the top 5 production houses by number of hit movies
#      (avg_rating > 8).
/* Output format:
+------------------------+-------------+
| production_company     | hit_movies  |
+------------------------+-------------+
| Marvel Studios         |      5      |
| National Theatre Live  |      4      |
| Blumhouse Productions  |      3      |
+------------------------+-------------+
*/


WITH thriller_hit_movie_cte AS (
	SELECT m2.production_company, COUNT(*) AS hit_movies,
		   RANK() OVER (
				ORDER BY COUNT(*) DESC
		   ) AS hit_movie_rank
	FROM movie m2
	INNER JOIN (
		SELECT production_company, genre, avg_rating
		FROM movie m1
		INNER JOIN genre g1 ON m1.id = g1.movie_id
		INNER JOIN ratings r1 ON m1.id = r1.movie_id
		WHERE r1.avg_rating > 8 AND genre = 'Thriller'
	) AS thriller_hit_movie
	ON thriller_hit_movie.production_company = m2.production_company
	GROUP BY m2.production_company
)
SELECT production_company, hit_movies
FROM thriller_hit_movie_cte
WHERE hit_movie_rank <= 3;

