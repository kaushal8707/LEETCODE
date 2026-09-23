/******************************************************************************************
  # IMDB Movies Database - SQL Assignment
  # Section 8: Business Interpretation & Insights (Q66 – Q75)
  # Focus:
  #   - Converting SQL outputs into business decisions
  #   - Genre & market selection strategy
  #   - Multilingual vs single-language performance
  #   - Revenue vs ratings correlation
  #   - Commercial + critical balance (votes + ratings)
******************************************************************************************/
USE myda_imdb;


# Q66) Based on data, which genre should Dandes Movies Company prioritize for highest audience engagement?
#      Use movie counts + average ratings.

/* Output format:
+-----------+---------------+-------------+
| genre     | movie_count   | avg_rating  |
+-----------+---------------+-------------+
| Drama     |     2312      |    7.40     |
| Comedy    |     1875      |    6.80     |
| ...       |      ...      |     ...     |
+-----------+---------------+-------------+
*/


SELECT genre,
       COUNT(G.movie_id) AS movie_count,
       ROUND(AVG(R.avg_rating), 2) AS avg_ratings
FROM genre G 
INNER JOIN ratings R ON G.movie_id = R.movie_id
GROUP BY genre
ORDER BY movie_count DESC, avg_ratings DESC;


# Q67) Do multilingual movies perform better than single-language movies?
#      Define:
#         - Multilingual: languages contains a comma
#         - Single-language: otherwise

/* Output format:
+-------------------+-------------+
| movie_type        | avg_rating  |
+-------------------+-------------+
| Multilingual      |    7.32     |
| Single-language   |    6.85     |
+-------------------+-------------+
*/

SELECT 
	IF (languages LIKE '%,%', 'Multilingual', 'Single-language') AS movie_type,
	ROUND(AVG(avg_rating), 2) AS avg_rating
FROM movie M 
INNER JOIN ratings R 
ON M.id = R.movie_id
GROUP BY movie_type;


# OR

SELECT
	CASE
		WHEN languages LIKE '%,%' THEN 'Multilingual'
        WHEN languages NOT LIKE '%,%' THEN 'Single-language'
	END AS movie_type,
	ROUND(AVG(avg_rating), 2) AS avg_rating
FROM movie M 
INNER JOIN ratings R 
ON M.id = R.movie_id
WHERE languages IS NOT NULL
GROUP BY movie_type;


-- 📌 Insight:
-- If multilingual > single-language, Dandes Movies Company should plan multi-language releases (theatrical or OTT).


# Q68) Which production companies produce the most consistently high-rated movies?
#      Consider only companies with at least 5 movies.

/* Output format:
+------------------------+-------------+------------+
| production_company     | movie_count | avg_rating |
+------------------------+-------------+------------+
| Warner Bros            |     12      |    7.85    |
| A24                    |      7      |    7.60    |
+------------------------+-------------+------------+
*/

SELECT production_company,
	   COUNT(id) AS movie_count,
       ROUND(AVG(COALESCE(avg_rating, 0)), 2) AS avg_rating
FROM movie M 
INNER JOIN ratings R 
ON M.id = R.movie_id
WHERE production_company IS NOT NULL
GROUP BY production_company
ORDER BY movie_count DESC, avg_rating DESC
LIMIT 5;


-- 📌 Insight:
-- Shortlist the top few companies here as ideal long-term partners for Dandes Movies Company.


# Q69) Which countries produce the most “hit” movies (avg_rating > 8)?
/* Output format:
+-----------+-------------+
| country   | hit_movies  |
+-----------+-------------+
| USA       |    342      |
| India     |    201      |
+-----------+-------------+
*/


SELECT country,
       COUNT(id) AS hit_movies
FROM movie M 
INNER JOIN ratings R 
ON M.id = R.movie_id
WHERE R.avg_rating > 8 
GROUP BY country 
ORDER BY hit_movies DESC
LIMIT 5;


-- 📌 Insight:
-- Top 3–5 countries here are prime markets for distribution, promotions, and co-productions.


# Q70) Which genres have the highest median rating?
/* Output format:
+-----------+------------------+
| genre     | median_rating    |
+-----------+------------------+
| Thriller  |       8.00       |
| Drama     |       7.20       |
+-----------+------------------+
*/

SELECT genre,
       ROUND(AVG(R.median_rating), 2) AS median_rating
FROM genre G 
INNER JOIN ratings R 
ON G.movie_id = R.movie_id
GROUP BY genre
ORDER BY median_rating DESC;



-- 📌 Insight:
-- Genres at the top are more “consistent” in audience perception (less skewed by a few hits).


# Q71) Should Dandes Movies Company focus on shorter movies or longer movies, based on ratings?
#      Define:
#         - Short: duration < 100 minutes
#         - Long:  duration >= 100 minutes

/* Output format:
+-------------------+-------------+
| duration_type     | avg_rating  |
+-------------------+-------------+
| Short (<100m)     |    6.50     |
| Long (>=100m)     |    7.40     |
+-------------------+-------------+
*/

SELECT 
	CASE 
		WHEN duration >= 100 THEN 'Long(>=100)'
        WHEN duration < 100 THEN 'Short(<100)'
	END AS duration_type,
    ROUND(AVG(avg_rating), 2) AS avg_rating
FROM movie M 
INNER JOIN ratings R 
ON M.id = R.movie_id
GROUP BY duration_type;


-- 📌 Insight:
-- If long movies clearly win → Dandes Movies Company should plan deeper stories rather than very short films.


# Q72) Do high-income production houses produce higher-rated movies?
#      (Revenue-to-rating correlation per production company.)

/* Output format:
+------------------------+--------------+-------------+
| production_company     | avg_income   | avg_rating  |
+------------------------+--------------+-------------+
| Disney                 | 1200000000   |    7.90     |
| Warner Bros            |  950000000   |    7.80     |
+------------------------+--------------+-------------+
*/





-- 📌 Insight:
-- Helps see whether “big money studios” also maintain quality or just do high-budget low-quality work.


# Q73) Which actors/actresses have the best balance of commercial success and critical rating?
#      (High total_votes + high avg_rating)

/* Output format:
+----------------------+--------------+------------+
| name                 | total_votes  | avg_rating |
+----------------------+--------------+------------+
| Tom Cruise           |  2345000     |    7.85    |
| Shah Rukh Khan       |  1872000     |    7.50    |
+----------------------+--------------+------------+
*/

SELECT name,
       SUM(total_votes) AS total_votes,
       ROUND(AVG(avg_rating), 2) AS avg_rating
FROM movie M
INNER JOIN ratings R 
ON M.id = R.movie_id
INNER JOIN role_mapping RM 
ON M.id = RM.movie_id
INNER JOIN names N 
ON RM.name_id = N.id
WHERE RM.category IN ('actor', 'actresses')
GROUP BY name
ORDER BY total_votes DESC
LIMIT 5;


-- 📌 Insight:
-- These 4–5 names are ideal casting choices: both popular and well-rated.


# Q74) Which year–genre combinations produced the best average ratings?
#      Show top 10 (year, genre) pairs by avg_rating.

/* Output format:
+------+-----------+-------------+
| year | genre     | avg_rating  |
+------+-----------+-------------+
| 2019 | Thriller  |    7.92     |
| 2018 | Drama     |    7.81     |
+------+-----------+-------------+
*/

SELECT year, genre,
       MAX(avg_rating) AS avg_rating
FROM movie M 
INNER JOIN genre G 
ON M.id = G.movie_id
INNER JOIN ratings R
ON M.id = R.movie_id
GROUP BY year, genre 
ORDER BY avg_rating DESC
LIMIT 10;



-- 📌 Insight:
-- Great for planning “what genre in which year” style strategy (e.g., recent trend patterns).


# Q75) For Dandes Movies Company’s first movie, which country + genre combination looks safest?
#      (Use only combinations with at least 20 movies.)

/* Output format:
+-----------+-----------+-------------+-------------+
| country   | genre     | movie_count | avg_rating  |
+-----------+-----------+-------------+-------------+
| India     | Drama     |     220     |    7.40     |
| USA       | Thriller  |     180     |    7.35     |
+-----------+-----------+-------------+-------------+
*/

WITH safe_launch_slot AS (
	SELECT country, genre, 
		   COUNT(M.id) AS movie_count,
		   ROUND(MAX(R.avg_rating), 2) AS avg_rating,
		   RANK() OVER (
				PARTITION BY country
				ORDER BY COUNT(M.id) DESC
		   ) AS movie_rank
	FROM movie M 
	INNER JOIN genre G 
	ON M.id = G.movie_id
	INNER JOIN ratings R 
	ON M.id = R.movie_id
	GROUP BY country, genre 
	HAVING movie_count > 20 
	ORDER BY movie_count DESC, avg_rating DESC
)
SELECT country, genre,
       movie_count, avg_rating
FROM safe_launch_slot 
WHERE movie_rank <= 1
LIMIT 5;

-- 📌 Insight:
-- Top 3–5 rows here give “safe launch slots” like:
--   • India + Drama
--   • USA + Thriller
-- These are strong candidates for Dandes Movies Company’s first big project.
