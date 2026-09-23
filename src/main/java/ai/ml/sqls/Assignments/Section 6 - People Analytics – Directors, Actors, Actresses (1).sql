/******************************************************************************************
  # IMDB Movies Database - SQL Assignment
  # Section 6: People Analytics – Directors, Actors, Actresses (Q49 – Q60)
  # Focus:
  #   - Director & actor performance
  #   - Weighted ratings (using votes)
  #   - India / Hindi / Drama specific analysis
  #   - Career span & collaborations
  #   - Genre coverage per actor
******************************************************************************************/
USE myda_imdb;

# Q49) Who are the top three directors in the top three genres whose movies have an average rating > 8?
#      (Top 3 genres = genres with the most movies having avg_rating > 8.)

/* Output format:
+----------------------+-------------+
| director_name        | movie_count |
+----------------------+-------------+
| James Mangold        |      4      |
| Christopher Nolan    |      3      |
| Anurag Kashyap       |      3      |
+----------------------+-------------+
*/


WITH top_three_genre_cte AS (
	SELECT genre, 
		   COUNT(m.id) AS movie_count,
		   RANK() OVER (
				ORDER BY COUNT(m.id) DESC
		   ) AS genre_rank
	FROM movie m 
	INNER JOIN genre g 
	ON g.movie_id = m.id
	INNER JOIN ratings r
	ON r.movie_id = m.id
	WHERE r.avg_rating > 8 
	GROUP BY genre 
	LIMIT 3
)
SELECT name, COUNT(dm.movie_id) AS movie_count
FROM director_mapping dm
INNER JOIN names n  ON n.id = dm.name_id
INNER JOIN genre g2 USING (movie_id)
INNER JOIN ratings r USING (movie_id)
INNER JOIN top_three_genre_cte USING (genre)
WHERE r.avg_rating > 8 
GROUP BY name
ORDER BY movie_count DESC
limit 3; 

			
# Q50) Who are the top two actors whose movies have a median rating ≥ 8?
/* Output format:
+----------------------+-------------+
| actor_name           | movie_count |
+----------------------+-------------+
| Christian Bale       |     10      |
| Leonardo DiCaprio    |      8      |
+----------------------+-------------+
*/


WITH movie_median_rating AS (
	SELECT m.id,
           COUNT(m.id) AS movie_count
	FROM movie m 
    INNER JOIN ratings r
    ON r.movie_id = m.id
    WHERE r.median_rating >= 8
    GROUP BY m.id
    ORDER BY COUNT(m.id) DESC
)
SELECT n.name AS actor_name,
       COUNT(rm.movie_id) AS movie_count 
FROM role_mapping rm 
INNER JOIN names n 
ON n.id = rm.name_id AND rm.category='actor'
INNER JOIN movie_median_rating mmr 
ON mmr.id = rm.movie_id
GROUP BY  n.name
ORDER BY COUNT(rm.movie_id) DESC
LIMIT 2;

# Q51) Rank actors with movies released in India using weighted average rating.
#      Conditions:
#        - Actor must have acted in at least 5 Indian movies.
#        - Use weighted avg rating based on votes.
#        - Break ties by total_votes (higher first).

/* Output format:
+---------------+-------------+-------------+--------------------+------------+
| actor_name    | total_votes | movie_count | actor_avg_rating   | actor_rank |
+---------------+-------------+-------------+--------------------+------------+
| Yogi Babu     |    3455     |     11      |       8.42         |     1      |
+---------------+-------------+-------------+--------------------+------------+
*/


WITH movie_released_rating AS (
	SELECT N.name, SUM(R.total_votes) AS total_votes,
		   COUNT(M.id) AS movie_count,
           ROUND(SUM(R.avg_rating * R.total_votes) / SUM(total_votes), 2) AS actor_avg_rating
    FROM movie M 
    INNER JOIN ratings R ON R.movie_id = M.id
    INNER JOIN role_mapping RM ON RM.movie_id = M.id
    INNER JOIN names N ON N.id = RM.name_id
    WHERE RM.category = 'actor' AND M.country = 'India'
    GROUP BY N.name
    HAVING movie_count >= 5
)
SELECT *, 
	   RANK() OVER (
			ORDER BY actor_avg_rating DESC
	   ) AS actor_rank
FROM movie_released_rating;


# Q52) Find the top five actresses in Hindi movies released in India.
#      Conditions:
#        - Category = 'actress'
#        - Country contains 'India'
#        - Languages contains 'Hindi'
#        - At least 3 movies per actress
#        - Weighted average rating using votes

/* Output format:
+----------------+-------------+-------------+----------------------+--------------+
| actress_name   | total_votes | movie_count | actress_avg_rating   | actress_rank |
+----------------+-------------+-------------+----------------------+--------------+
| Taapsee Pannu  |    3455     |     11      |         7.74         |      1       |
+----------------+-------------+-------------+----------------------+--------------+
*/


WITH top_actress_hindi_movie AS (
	SELECT N.name AS actress_name, 
		   SUM(R.total_votes) as total_votes,
		   COUNT(R.movie_id) AS movie_count,
		   ROUND(SUM(R.avg_rating * R.total_votes) / SUM(R.total_votes), 2) AS actress_avg_rating
	FROM movie M
	INNER JOIN ratings R ON M.id = R.movie_id
	INNER JOIN role_mapping RM ON M.id = RM.movie_id
	INNER JOIN names N ON RM.name_id =  N.id
	WHERE RM.category = 'actress' AND M.country = 'INDIA' AND M.languages LIKE '%Hindi%'
	GROUP BY N.name
	HAVING movie_count >= 3
)
SELECT *, 
       RANK() OVER (
			ORDER BY actress_avg_rating DESC
	   ) AS actress_rank
FROM top_actress_hindi_movie
LIMIT 5;


# Q53) Who are the top three actresses in 'Drama' superhit movies (avg_rating > 8)?
#      Use weighted average rating based on votes.

/* Output format:
+----------------+-------------+-------------+----------------------+--------------+
| actress_name   | total_votes | movie_count | actress_avg_rating   | actress_rank |
+----------------+-------------+-------------+----------------------+--------------+
| Laura Dern     |    1016     |     1       |        9.60          |      1       |
+----------------+-------------+-------------+----------------------+--------------+
*/


WITH top_actress_hindi_movie AS (
	SELECT N.name AS actress_name, 
		   SUM(R.total_votes) as total_votes,
		   COUNT(R.movie_id) AS movie_count,
		   ROUND(SUM(R.avg_rating * R.total_votes) / SUM(R.total_votes), 2) AS actress_avg_rating
	FROM movie M
	INNER JOIN ratings R ON M.id = R.movie_id
	INNER JOIN role_mapping RM ON M.id = RM.movie_id
	INNER JOIN names N ON RM.name_id =  N.id
    INNER JOIN genre G ON M.id = G.movie_id
	WHERE RM.category = 'actress' AND G.genre = 'Drama' AND R.avg_rating > 8
	GROUP BY N.name
)
SELECT *, 
       RANK() OVER (
			ORDER BY movie_count DESC
	   ) AS actress_rank
FROM top_actress_hindi_movie
LIMIT 3;


# Q54) Get the following metrics for the top 9 directors (based on number of movies):
#      director_id, director_name, number_of_movies, avg_inter_movie_days,
#      avg_rating, total_votes, min_rating, max_rating, total_duration.

/* Output format:
+-------------+----------------+-------------------+----------------------+------------+------------+------------+------------+----------------+
| director_id | director_name  | number_of_movies  | avg_inter_movie_days | avg_rating | total_votes| min_rating | max_rating | total_duration |
+-------------+----------------+-------------------+----------------------+------------+------------+------------+------------+----------------+
| nm1777967   | A.L. Vijay     |        5          |         177          |    5.65    |   1754     |    3.7     |    6.9     |      613       |
+-------------+----------------+-------------------+----------------------+------------+------------+------------+------------+----------------+
*/

WITH director_matrics AS (
	SELECT DM.name_id AS director_id,
		   N.name AS director_name,
		   R.avg_rating,
		   R.total_votes,
		   M.duration,
		   M.date_published,
		   LEAD (M.date_published, 1) OVER (
				PARTITION BY DM.name_id
				ORDER BY M.date_published
		   ) AS next_date_published
	FROM director_mapping DM
	INNER JOIN Movie M ON DM.movie_id = M.id
	INNER JOIN names N ON DM.name_id = N.id
	INNER JOIN ratings R USING (movie_id)
    
)
SELECT director_id,
	   COUNT(director_name) AS number_of_movies,
       AVG(DATEDIFF(next_date_published, date_published))AS avg_inter_movie_days,
       AVG(avg_rating) AS avg_rating,
       SUM(total_votes) AS total_votes,
       MIN(avg_rating) AS min_rating,
       MAX(avg_rating) AS max_rating,
       SUM(duration) AS total_duration
FROM director_matrics
GROUP BY director_id
ORDER BY number_of_movies DESC
LIMIT 9

             
       

select * from director_mapping;
select * from ratings;
select * from movie;



# Q55) List the top 10 directors based on average movie rating (minimum 3 movies).
/* Output format:
+----------------------+-------------+------------+
| director_name        | movie_count | avg_rating |
+----------------------+-------------+------------+
| Christopher Nolan    |      5      |    8.85    |
+----------------------+-------------+------------+
*/


WITH director_movie_count AS (
	SELECT N.name AS director_name, 
		   COUNT(M.id) as movie_count
	FROM Movie M 
	INNER JOIN director_mapping DM
	ON M.id = DM.movie_id
	INNER JOIN names N 
	ON DM.name_id = N.id
	GROUP BY director_name
	HAVING movie_count >= 3
	ORDER BY movie_count DESC
)
SELECT director_name, movie_count, R.avg_rating as avg_rating
FROM ratings R
INNER JOIN director_mapping DM2 ON R.movie_id = DM2.movie_id
INNER JOIN names N2 ON DM2.name_id = N2.id
INNER JOIN director_movie_count dmc ON dmc.director_name = N2.name
ORDER BY avg_rating DESC
LIMIT 10;


# Q56) Find the actor who has acted in the highest number of movies overall.
/* Output format:
+----------------------+-------------+
| actor_name           | movie_count |
+----------------------+-------------+
| Samuel L. Jackson    |     28      |
+----------------------+-------------+
*/


SELECT N.name AS actor_name, 
	   COUNT(RM.movie_id) AS movie_count
FROM movie M 
INNER JOIN role_mapping RM
ON M.id = RM.movie_id
INNER JOIN names N 
ON N.id = RM.name_id
WHERE RM.category = 'actor'
GROUP BY N.name
ORDER BY movie_count DESC
LIMIT 1;


# Q57) Find the top five actors by total votes across all their movies.
/* Output format:
+----------------------+-------------+
| actor_name           | total_votes |
+----------------------+-------------+
| Robert Downey Jr.    |   985432    |
+----------------------+-------------+
*/


WITH top_5_actor_total_votes AS (
	SELECT N.name AS actor_name, 
		   SUM(R.total_votes) AS total_votes
	FROM movie M 
	INNER JOIN ratings R 
	ON M.id = R.movie_id
	INNER JOIN role_mapping RM
	ON M.id = RM.movie_id
	INNER JOIN names N 
	ON N.id = RM.name_id
	WHERE RM.category = 'actor'
    GROUP BY N.name
) 
SELECT *
FROM top_5_actor_total_votes
ORDER BY total_votes DESC
LIMIT 5;


# Q58) For each director, find their career span in years: first_year and last_year of movie release.
/* Output format:
+----------------------+-------------+-------------+
| director_name        | first_year  | last_year   |
+----------------------+-------------+-------------+
| Steven Spielberg     |    1993     |    2019     |
+----------------------+-------------+-------------+
*/


SELECT N.name AS director_name,
	   MIN(YEAR(M.date_published)) AS first_year,
       MAX(YEAR(M.date_published)) AS last_year,
       (MAX(YEAR(M.date_published)) - MIN(YEAR(M.date_published))) AS career_span_year
FROM Movie M 
INNER JOIN director_mapping DM 
ON M.id = DM.movie_id
INNER JOIN names N 
ON DM.name_id = N.id
GROUP BY N.name 
ORDER BY career_span_year DESC;


# Q59) For each actor, how many distinct genres have they acted in?
#      Show only actors who have acted in at least 5 different genres.

/* Output format:
+----------------------+------------------+
| actor_name           | genre_count      |
+----------------------+------------------+
| Irrfan Khan          |        6         |
+----------------------+------------------+
*/


SELECT N.name AS actor_name, 
	COUNT(DISTINCT(G.genre)) AS genre_count
FROM movie M 
INNER JOIN genre G 
ON M.id = G.movie_id
INNER JOIN role_mapping RM 
ON M.id = RM.movie_id
INNER JOIN names N 
ON RM.name_id = N.id
WHERE RM.category = 'actor'
GROUP BY N.name
HAVING genre_count >= 5
ORDER BY genre_count DESC;


# Q60) Find the top five director–actor pairs with the most collaborations.
/* Output format:
+----------------------+----------------------+-------------+
| director_name        | actor_name           | movie_count |
+----------------------+----------------------+-------------+
| Christopher Nolan    | Michael Caine        |     6       |
+----------------------+----------------------+-------------+
*/


WITH director_movie_count AS (
	SELECT N1.name AS director_name,
           DM.movie_id AS d_movie_id
	FROM Movie M1
    INNER JOIN director_mapping DM 
    ON M1.id = DM.movie_id
    INNER JOIN names N1 
    ON DM.name_id = N1.id
    
) , actor_movie_count AS (
	SELECT N2.name AS actor_name,
           RM.movie_id AS a_movie_id
	FROM Movie M2
    INNER JOIN role_mapping RM 
    ON M2.id = RM.movie_id
    INNER JOIN names N2 
    ON RM.name_id = N2.id
)
SELECT director_name, actor_name, COUNT(MM.id) AS movie_count
FROM movie MM
INNER JOIN director_movie_count dmc 
ON MM.id = dmc.d_movie_id
INNER JOIN actor_movie_count amc
ON MM.id = amc.a_movie_id
GROUP BY director_name, actor_name
ORDER BY movie_count DESC
LIMIT 5;

