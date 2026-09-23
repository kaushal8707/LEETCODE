/******************************************************************************************
  # IMDB Movies Database - SQL Assignment
  # Section 4: Movie Ratings & Title Insights (Q31 – Q38)
  # Focus:
  #   - Top-rated movies (avg_rating, median_rating)
  #   - Title-based filters (starts with 'The', etc.)
  #   - Year-wise best movies
  #   - Above-average movies
  #   - Most voted / popular movies
******************************************************************************************/
USE myda_imdb;

# Q31) List the top 10 movies based on average rating (with ranks).
#      If multiple movies share the same rank at position 10, include them all.

/* Output format:
+-------------------------------+------------+------------+
| title                         | avg_rating | movie_rank |
+-------------------------------+------------+------------+
| Fan                           |    9.6     |     1      |
| The Shawshank Redemption      |    9.5     |     2      |
| ...                           |    ...     |    ...     |
+-------------------------------+------------+------------+
*/

WITH average_movie_rating_cte AS (
	SELECT title, avg_rating,
		   DENSE_RANK() OVER (
				ORDER BY avg_rating DESC
		   ) AS movie_rank
	FROM movie mv 
	INNER JOIN ratings rt
	ON mv.id = rt.movie_id
) 
SELECT title, avg_rating, movie_rank
FROM average_movie_rating_cte
WHERE movie_rank <= 10;


# Q32) Summarise the ratings table based on movie counts by median_rating.
/* Output format:
+---------------+-------------+
| median_rating | movie_count |
+---------------+-------------+
|      1        |     105     |
|      2        |     210     |
|      3        |     340     |
|  ...          |     ...     |
+---------------+-------------+
*/

SELECT median_rating, COUNT(*) AS movie_count
FROM ratings
GROUP BY median_rating
ORDER BY median_rating;	   


# Q33) Find all movies (with genre) whose title starts with 'The' and have average rating > 8.
/* Output format:
+-----------------------------+------------+-----------+
| title                       | avg_rating | genre     |
+-----------------------------+------------+-----------+
| Theeran                     |    8.3     | Thriller  |
| The Dark Knight             |    9.0     | Action    |
| The Godfather               |    9.2     | Crime     |
+-----------------------------+------------+-----------+
*/


WITH movie_with_genre_cte AS (
	SELECT title, avg_rating, genre
	FROM movie m 
	INNER JOIN ratings r ON m.id = r.movie_id
	INNER JOIN genre g ON m.id = g.movie_id 
) 
SELECT title, avg_rating, genre
FROM movie_with_genre_cte
WHERE avg_rating > 8 AND title LIKE 'The%';


# Q34) List the top 5 movies based on median_rating (with ranks).
#      If multiple movies share rank 5, include all of them.

/* Output format:
+-----------------------------+---------------+------------+
| title                       | median_rating | median_rank|
+-----------------------------+---------------+------------+
| Movie A                     |      10       |     1      |
| Movie B                     |       9       |     2      |
| ...                         |      ...      |    ...     |
+-----------------------------+---------------+------------+
*/

WITH media_rating_movie_cte AS (
	SELECT title, median_rating,
		   DENSE_RANK() OVER (
				ORDER BY median_rating DESC
		   ) AS median_rank
	FROM movie mv 
	INNER JOIN ratings rt
	ON mv.id = rt.movie_id
) 
SELECT title, median_rating, median_rank
FROM media_rating_movie_cte
WHERE median_rank <= 5;


# Q35) For each year, find the highest-rated movie by average rating.
#      Show: year, title, avg_rating.
#      If multiple movies tie for top rating in a year, include them all.

/* Output format:
+------+-----------------------------+------------+
| year | title                       | avg_rating |
+------+-----------------------------+------------+
| 2017 | Movie A                     |    9.1     |
| 2018 | Movie B                     |    9.0     |
| 2019 | Movie C                     |    8.9     |
+------+-----------------------------+------------+
*/

WITH high_rated_rating_cte AS (
	SELECT year, title, avg_rating,
		   DENSE_RANK() OVER (
				PARTITION BY year
				ORDER BY year, avg_rating DESC
		   ) AS high_rated_rank
	FROM movie m 
	INNER JOIN ratings r 
	ON m.id = r.movie_id
)
SELECT year, title, avg_rating
FROM high_rated_rating_cte
WHERE high_rated_rank <= 1;


# Q36) Find all movies whose average rating is above the overall average rating of all movies.
/* Output format:
+-----------------------------+------------+
| title                       | avg_rating |
+-----------------------------+------------+
| Movie A                     |    8.4     |
| Movie B                     |    8.2     |
| ...                         |    ...     |
+-----------------------------+------------+
*/


SELECT title, avg_rating
FROM movie m
INNER JOIN ratings r 
ON m.id = r.movie_id
WHERE r.avg_rating > (
			SELECT ROUND(AVG(avg_rating), 1) AS overall_avg_ratings
			FROM ratings
)
ORDER BY avg_rating DESC;


# Q37) For each median_rating, find the average of avg_rating and total number of movies.
#      This helps to see if avg_rating aligns with median_rating.

/* Output format:
+---------------+------------------+-------------+
| median_rating | avg_of_avg_rating| movie_count |
+---------------+------------------+-------------+
|      1        |       1.40       |    105      |
|      2        |       2.10       |    210      |
|      3        |       3.00       |    340      |
|  ...          |       ...        |    ...      |
+---------------+------------------+-------------+
*/

SELECT median_rating, 
	   ROUND(AVG(avg_rating), 2) AS avg_of_avg_rating, 
       COUNT(*) AS movie_count
FROM ratings r 
INNER JOIN movie m 
ON r.movie_id = m.id 
GROUP BY median_rating
ORDER BY median_rating;


# Q38) Find the top 10 most voted movies (by total_votes) and show their ratings.
/* Output format:
+-----------------------------+-------------+------------+
| title                       | total_votes | avg_rating |
+-----------------------------+-------------+------------+
| Avengers: Endgame           |   985432    |    8.4     |
| Inception                   |   875321    |    8.8     |
| ...                         |    ...      |    ...     |
+-----------------------------+-------------+------------+
*/

WITH most_voted_movie_cte AS (
	SELECT title, total_votes, avg_rating,
		   ROW_NUMBER() OVER (
				ORDER BY total_votes DESC
		   ) AS total_votes_row
	FROM movie mv 
	INNER JOIN ratings rt 
	ON mv.id = rt.movie_id
)
SELECT title, total_votes, avg_rating
FROM most_voted_movie_cte
WHERE total_votes_row <=10;


