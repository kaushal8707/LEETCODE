/******************************************************************************************
  # IMDB Movies Database - SQL Assignment
  # Section 3: Genre Insights & Aggregations (Q19 – Q30)
  # Focus:
  #   - Genre-level aggregations
  #   - GROUP BY, HAVING, COUNT, AVG
  #   - Window functions (running total, moving average)
  #   - Ranking genres
  #   - Categorizing movies using CASE
******************************************************************************************/
USE myda_imdb;


# Q19) For each genre, find the number of movies and the percentage of total movies.
/* Output format:
+-----------+-------------+----------------------+
| genre     | movie_count | movie_pct_of_total   |
+-----------+-------------+----------------------+
| Drama     |    4285     |        35.42         |
| Comedy    |    3000     |        24.80         |
| Thriller  |    2100     |        17.35         |
+-----------+-------------+----------------------+
*/

SELECT genre, COUNT(*) AS movie_count, 
       ROUND(COUNT(*) * 100 / SUM(COUNT(*)) OVER(), 2) AS movie_pct_of_total          # Calculates the grand total of all rows across all genre without collapsing the results.
FROM genre g 
INNER JOIN movie m 
ON g.movie_id=m.id 
GROUP BY genre
ORDER BY movie_count DESC;


# Q20) For each year, find the genre with the highest number of movies.
/* Output format:
+------+-----------+-------------+------------+
| year | genre     | movie_count | genre_rank |
+------+-----------+-------------+------------+
| 2017 | Drama     |    1500     |     1      |
| 2017 | Comedy    |    1200     |     2      |
| 2018 | Drama     |    1400     |     1      |
+------+-----------+-------------+------------+
-- (We keep only rank = 1 to show top genre per year.)
*/

SELECT year, genre, COUNT(*) AS movie_count,
	   RANK() OVER (
			PARTITION BY year
            ORDER BY COUNT(*) DESC
       ) AS genre_rank
FROM genre g 
INNER JOIN movie m
ON g.movie_id=m.id
GROUP BY year, genre;


# only rank = 1 to show top genre per year
# Using CTE
WITH top_genre_per_year_cte AS (
	SELECT year, genre, COUNT(*) AS movie_count,
		   RANK() OVER (
				PARTITION BY year
				ORDER BY COUNT(*) DESC
		   ) AS genre_rank
	FROM genre g 
	INNER JOIN movie m
	ON g.movie_id=m.id
	GROUP BY year, genre
)
SELECT year, genre, movie_count, genre_rank
FROM top_genre_per_year_cte 
WHERE genre_rank = 1;


# Q21) For each genre, find the average and median rating of its movies.
/* Output format:
+-----------+------------------+--------------------+
| genre     | avg_rating       | avg_median_rating  |
+-----------+------------------+--------------------+
| Drama     |      7.25        |        7.00        |
| Thriller  |      7.45        |        7.00        |
| Comedy    |      6.80        |        6.00        |
+-----------+------------------+--------------------+
*/


SELECT genre, 
	   ROUND(AVG(COALESCE(avg_rating, 0)), 2) AS avg_rating,
       ROUND(AVG(COALESCE(median_rating, 0)), 2) AS avg_median_rating
FROM genre g 
INNER JOIN ratings r ON g.movie_id=r.movie_id
GROUP BY genre;


# Q22) For each genre, find the total votes received by all its movies.
/* Output format:
+-----------+----------------+
| genre     | total_votes    |
+-----------+----------------+
| Drama     |   12345678     |
| Thriller  |    9876543     |
| Action    |    8765432     |
+-----------+----------------+
*/

SELECT genre, 
	   SUM(total_votes) AS total_votes
FROM genre gr 
INNER JOIN ratings rt
ON gr.movie_id = rt.movie_id
GROUP BY genre;


# Q23) For each genre, how many movies are multilingual (more than one language)?
/* Output format:
+-----------+-------------------------+
| genre     | multilingual_movie_count|
+-----------+-------------------------+
| Drama     |          320            |
| Thriller  |          210            |
| Action    |          190            |
+-----------+-------------------------+
*/

WITH movie_multilingual_cte AS (
	SELECT genre, title AS movie, languages
	FROM  genre g
    INNER JOIN movie m 
    ON g.movie_id = m.id 
	WHERE CHAR_LENGTH(languages) - CHAR_LENGTH(REPLACE(languages, ',', '')) > 0
)
SELECT genre, COUNT(*) AS multilingual_movie_count
FROM movie_multilingual_cte
GROUP BY genre
ORDER BY multilingual_movie_count DESC;


# Q24) Consider Thriller movies having at least 25,000 votes.
#      Classify them based on avg_rating into:
#         > 8   : Superhit
#         7–8   : Hit
#         5–7   : One-time-watch
#         < 5   : Flop

/* Output format:
+-------------------------+------------------+
| movie_name              | movie_category   |
+-------------------------+------------------+
| Get Out                 | Hit              |
| The Dark Knight         | Superhit         |
| ...                     | ...              |
+-------------------------+------------------+
*/


SELECT title AS movie_name,
CASE
	WHEN avg_rating > 8 THEN 'Superhit'
    WHEN avg_rating BETWEEN 7 AND 8 THEN 'Hit'
    WHEN avg_rating BETWEEN 5 AND 7 THEN 'One-time-watch'
    ELSE 'FLOP'
END AS  'movie_category'
FROM movie mv 
INNER JOIN ratings rt
ON mv.id = rt.movie_id;


# Q25) What is the genre-wise running total and moving average of the average movie duration?
#      Use average duration per genre, then apply window functions.

/* Output format:
+-----------+---------------+-------------------------+----------------------+
| genre     | avg_duration  | running_total_duration  | moving_avg_duration  |
+-----------+---------------+-------------------------+----------------------+
| Action    |    112.34     |        112.34           |        112.34        |
| Comedy    |     98.50     |        210.84           |        105.42        |
| Drama     |    106.77     |        317.61           |        105.87        |
+-----------+---------------+-------------------------+----------------------+
*/

SELECT genre, ROUND(AVG(duration), 2) AS avg_duration,
	   SUM(ROUND(AVG(duration), 2)) OVER (
			ORDER BY genre 
            ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW
       ) AS running_total_duration,
       AVG(ROUND(AVG(duration), 2)) OVER (
			ORDER BY genre                  # By default it will conside ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW because Window size we didn't specified
       ) AS moving_avg_duration
FROM genre gr 
INNER JOIN movie mv 
ON gr.movie_id = mv.id 
GROUP BY genre;


# Q26) Find the top 5 genres with the highest average movie rating.
/* Output format:
+-----------+------------------+
| genre     | avg_genre_rating |
+-----------+------------------+
| Mystery   |      8.10        |
| Thriller  |      7.95        |
| Drama     |      7.80        |
+-----------+------------------+
*/


WITH average_genre_rating_cte AS(
	SELECT genre, AVG(avg_rating) AS avg_genre_rating,
		ROW_NUMBER() OVER (
			ORDER BY AVG(avg_rating) DESC
		) AS avg_genre_rating_rank
	FROM genre g 
	INNER JOIN ratings r
	ON g.movie_id = r.movie_id
	GROUP BY genre
)
SELECT genre, ROUND(avg_genre_rating, 2) AS avg_genre_rating
FROM average_genre_rating_cte
WHERE avg_genre_rating_rank < 6;


# Q27) For each genre, find the total number of hit movies (avg_rating ≥ 8).
/* Output format:
+-----------+------------------+
| genre     | hit_movie_count  |
+-----------+------------------+
| Drama     |       450        |
| Thriller  |       320        |
| Action    |       280        |
+-----------+------------------+
*/


SELECT genre, COUNT(avg_rating) AS hit_movie_count
FROM genre g
INNER JOIN ratings r 
ON g.movie_id = r.movie_id
WHERE avg_rating >= 8
GROUP BY genre
ORDER BY COUNT(avg_rating) DESC;


# Q28) Find all genres that have more than 1000 movies.
/* Output format:
+-----------+-------------+
| genre     | movie_count |
+-----------+-------------+
| Drama     |    2312     |
| Comedy    |    1875     |
+-----------+-------------+
*/

WITH genre_more_than_1k_movie_cte AS (
	SELECT genre, COUNT(*) AS movie_count
	FROM genre gr 
	INNER JOIN movie mv 
	ON gr.movie_id = mv.id
	GROUP BY genre
)
SELECT genre, movie_count
FROM genre_more_than_1k_movie_cte
WHERE movie_count > 1000
ORDER BY movie_count DESC;


# Q29) For each genre, find the longest movie (maximum duration) and its title.
/* Output format:
+-----------+----------------------------+----------+
| genre     | longest_movie              | duration |
+-----------+----------------------------+----------+
| Drama     | The Irishman               |   209    |
| Action    | Avengers: Endgame          |   181    |
+-----------+----------------------------+----------+
*/

WITH genre_longest_duration_cte AS (
	SELECT genre, title AS longest_movie, duration,
		   ROW_NUMBER() OVER (
				PARTITION BY genre
				ORDER BY duration DESC
		   ) AS ranked_based_duration
	FROM genre ge
	INNER JOIN movie mv ON ge.movie_id = mv.id
)
SELECT genre, longest_movie, duration
FROM genre_longest_duration_cte
WHERE ranked_based_duration <= 1
ORDER BY duration DESC;      



# Q30) Find the number of movies in each genre released in or after 2019.
/* Output format:
+-----------+-------------+
| genre     | movie_count |
+-----------+-------------+
| Drama     |    520      |
| Thriller  |    410      |
| Comedy    |    380      |
+-----------+-------------+
*/

WITH movie_genre_after_2019_cte AS (
	SELECT genre, COUNT(*) AS movie_count
	FROM genre g 
	INNER JOIN movie m 
	ON g.movie_id = m.id 
	WHERE year >= 2019
	GROUP BY genre
)
SELECT genre, movie_count
FROM movie_genre_after_2019_cte
ORDER BY movie_count DESC;








