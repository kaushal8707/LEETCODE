/******************************************************************************************
  # IMDB Movies Database - SQL Assignment
  # Section 2: Date, Time & Country Trends (Q11 – Q18)
  # Focus:
  #   - Extracting year, month, and day from dates
  #   - Year-wise and month-wise trend analysis
  #   - Country-wise movie production trends
  #   - Range queries using dates
******************************************************************************************/
USE myda_imdb;

# Q11) For each year and country, how many movies were released?
/* Output format:
+------------+---------------------------+-------------+
| year       | country                   | movie_count |
+------------+---------------------------+-------------+
| 2017       | USA                       |     853     |
| 2017       | India                     |     347     |
| 2017       | UK                        |     151     |
| 2017       | Japan                     |     107     |
| 2017       | Canada                    |      85     |
+------------+---------------------------+-------------+
*/

SELECT year, country, COUNT(*) AS movie_count
FROM movie
GROUP BY year, country;


# Q12) How many movies released in each genre during March 2017 in the USA had more than 1,000 votes?
/* Output format:
+-----------+-------------+
| genre     | movie_count |
+-----------+-------------+
| Drama     |     24      |
| Comedy    |      9      |
| Action    |      8      |
+-----------+-------------+
*/

# Using JOINS
select g.genre, count(*) as co
from genre g
inner join movie m on g.movie_id=m.id
inner join ratings r on g.movie_id=r.movie_id AND total_votes >= 1000
where DATE_FORMAT(date_published, '%M %Y') = 'March 2017' AND country = 'USA'
group by genre;

# OR

# Using Sub-query
select genre, COUNT(*) AS movie_count
FROM genre
WHERE movie_id IN(
	SELECT id
	FROM movie
	WHERE DATE_FORMAT(date_published, '%M %Y') = 'March 2017' AND country = 'USA'
)
GROUP BY genre;

# Q13) Of the movies released between 1 April 2018 and 1 April 2019 (inclusive),
#      how many were given a median rating of 8?
/* Output format:
+----------------------+
| movie_count          |
+----------------------+
|        361           |
+----------------------+
*/


SELECT COUNT(*) AS movie_count
FROM movie 
WHERE date_format(date_published, '%Y-%m-%d') BETWEEN '2018-04-01' AND '2019-04-01'
AND id IN(
	SELECT movie_id FROM ratings WHERE median_rating = 8
);


# Q14) Do German movies get more votes than Italian movies?
#     Compare the total votes for German vs Italian language movies.
/* Output format:
+------------+-------------+
| language   | total_votes |
+------------+-------------+
| German     |   4421525   |
| Italian    |   2003623   |
+------------+-------------+
*/

	SELECT languages, SUM(total_votes) AS total_votes
	FROM movie m 
	INNER JOIN ratings r ON m.id = r.movie_id
	WHERE languages IN ('German','Italian')
	GROUP BY languages;

-- From the result, you can decide if German > Italian in total votes.

	# TBD


# Q15) For each year, how many movies were released in India?
/* Output format:
+------------+-------------+
| year       | movie_count |
+------------+-------------+
| 2017       |    372      |
| 2018       |    387      |
| 2019       |    309      |
+------------+-------------+
*/

SELECT year, COUNT(*) AS movie_count
FROM movie
WHERE country = 'India'
GROUP BY year;



# Q16) In the year 2019, how many movies were released in each month (across all countries)?
/* Output format:
+-----------+-------------+
| month_num | movie_count |
+-----------+-------------+
| 1         |     210     |
| 2         |     198     |
| 3         |     225     |
| ...       |     ...     |
+-----------+-------------+
*/


WITH movie_2019_CTE AS (
	SELECT DATE_FORMAT(date_published, '%m-%Y') AS released_month 
	FROM movie
) 
SELECT released_month, COUNT(*) AS movie_count
FROM movie_2019_CTE
WHERE released_month LIKE '%2019'
GROUP BY released_month
ORDER BY released_month;



# Q17) For each year, what is the average rating of movies produced in the USA?
/* Output format:
+------------+------------------+
| year       | avg_rating_usa   |
+------------+------------------+
| 2017       |      5.29        |
| 2018       |      5.30        |
| 2019       |      5.55        |
+------------+------------------+
*/


SELECT year, ROUND(AVG(avg_rating), 2) AS avg_rating_usa
FROM movie m
INNER JOIN ratings r
ON m.id = r.movie_id
WHERE country = 'USA'
GROUP BY year;


# Q18) Find the top 6 (year, country) combinations with the highest number of hit movies (avg_rating > 8).
/* Output format:
+------------+---------------------------+-------------+
| year       | country                   | hit_movies  |
+------------+---------------------------+-------------+
| 2019       | India                     |      60     |
| 2018       | India                     |      33     |
| 2017       | India                     |      26     |
| 2019       | USA                       |      18     |
| 2018       | USA                       |      11     |
| 2017       | USA                       |       9     |
+------------+---------------------------+-------------+
*/


WITH highest_hit_movie_CTE AS (
	SELECT year, country, 
            COUNT(*) AS hit_movies,
			ROW_NUMBER() OVER (
				ORDER BY COUNT(*) DESC
			) AS hit_movie_seq
	FROM movie
	WHERE id IN (
		SELECT movie_id FROM ratings WHERE avg_rating > 8
	)
	GROUP BY year, country
) 
SELECT year, country, hit_movies
FROM highest_hit_movie_CTE
WHERE hit_movie_seq <= 6



