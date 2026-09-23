/******************************************************************************************
  # IMDB Movies Database - SQL Assignment
  # Section 7: SQL Performance & Index Optimization (Q61 – Q65)
  # Focus:
  #   - Using EXPLAIN to understand query plans
  #   - Index design for JOINs and filters
  #   - Query rewrites for better index usage
  #   - Function-based / generated-column indexing
******************************************************************************************/
USE myda_imdb;


# Q61) Suggest appropriate indexes to speed up JOIN operations between movie, ratings, 
#      genre, director_mapping, and role_mapping tables.
#      Write actual index creation SQL.


CREATE INDEX idx_movie_id ON movie(id);
CREATE INDEX idx_ratings_movie_id ON ratings(movie_id);
CREATE INDEX idx_genre_movie_id ON genre(movie_id);
CREATE INDEX idx_director_mapping_name_id ON director_mapping(name_id);
CREATE INDEX idx_role_mapping_name_id ON role_mapping(name_id);
CREATE INDEX idx_names_id ON names(id);


# Q62) Rewrite this query to make it index-friendly.
# Given slow query:
#   SELECT *
#   FROM movie
#   WHERE YEAR(date_published) = 2019;
#
# Problem: YEAR() on the column prevents index usage on date_published.

-- Type your optimized query below:
-- Optimized version using a range on date_published:

SELECT *
   FROM movie
   WHERE YEAR(date_published) = 2019;

CREATE INDEX idx_movie_date_published 
ON movie(date_published);

SELECT * 
FROM movie
WHERE date_published BETWEEN '2017-11-17' AND '2019-03-09';
	

# Q63) Which queries in this IMDB assignment benefit from indexing country, languages, 
#      year, and date_published columns?
#      (Write the question numbers + explanation in text.)

/* Suggested answer (text):

+---------------------------+---------------------------------------------------------------+
| query_reference           | why_index_is_useful                                           |
+---------------------------+---------------------------------------------------------------+
| Section 2 – Q15           | Filters by country LIKE '%India%' and groups by year.        |
| Section 2 – Q16           | Filters year = 2019 and groups by MONTH(date_published).     |
| Section 2 – Q17           | Filters m.country LIKE '%USA%' and joins ratings.            |
| Section 2 – Q18           | Filters year, country, and avg_rating > 8.                   |
| Section 3 – Q30           | Filters m.year >= 2019 for genre-wise counts.                |
| Section 6 – Q51/Q52       | Filters by country (India) and languages (Hindi).            |
+---------------------------+---------------------------------------------------------------+
*/

CREATE INDEX idx_movie_year ON movie(year);
CREATE INDEX idx_movie_country ON movie(country);
CREATE INDEX idx_movie_date_published ON movie(date_published);


/*- For languages LIKE '%German%' or '%Italian%', consider:
    * Fulltext / functional index (if supported),
    * Or refactor schema to a separate movie_languages table.
*/


# Q64) For queries ranking movies by worldwide_gross_income, suggest the right indexing solution.
#      (Column worldwide_gross_income is VARCHAR with '$', '€', and commas.)
#      Convert to numeric and index that column.


select * from movie ;

UPDATE movie SET worldwide_gross_income = TRIM(REPLACE(worldwide_gross_income, '$', ''));
UPDATE movie SET worldwide_gross_income = TRIM(REPLACE(worldwide_gross_income, 'INR', ''));

ALTER TABLE movie 
	MODIFY COLUMN worldwide_gross_income BIGINT,
	ADD INDEX idx_movie_worldwide_gross_income(worldwide_gross_income);


# Q65) Use EXPLAIN to determine whether this query uses an index, and suggest the index.
#       Query:
#           SELECT *
#           FROM movie
#           WHERE country LIKE 'USA%';

# Answer in two parts:
#   1) Create index
#   2) Show EXPLAIN query

/* Output format (conceptual):
+-----------------------------+
| index_used?                 |
+-----------------------------+
| YES (country prefix search) |
+-----------------------------+
| improvement                 |
+-----------------------------+
| Add idx_movie_country       |
+-----------------------------+
*/

-- Type your code + explanation below:

EXPLAIN SELECT *
FROM movie
WHERE country LIKE 'USA%';


CREATE INDEX idx_movie_country ON movie(country);

EXPLAIN SELECT *
FROM movie
WHERE country LIKE 'USA%';