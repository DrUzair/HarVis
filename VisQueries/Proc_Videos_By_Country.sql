DROP PROCEDURE IF EXISTS `Proc_GeoStats`;
SET SQL_SAFE_UPDATES=0;
DELETE FROM utube_geostats;
delimiter $$
CREATE DEFINER=`uTube`@`localhost` PROCEDURE `Proc_GeoStats`()
BEGIN		
	DECLARE strCountry VARCHAR(2);
	DECLARE intVideoCount INT;	
	DECLARE intAuthorCount INT;
	-- counters
	DECLARE countries_cursor_num_rows INT DEFAULT 0;	
	DECLARE countries_cursor_loop_cntr INT;
	-- Declare video_authors_cursor [most commented-on video_authors]
	DECLARE countries_cursor CURSOR FOR 	
	SELECT DISTINCT(Location) FROM utubeauthors;
	OPEN countries_cursor;		
		SELECT FOUND_ROWS() INTO countries_cursor_num_rows;	
		SET countries_cursor_loop_cntr = 1;		
		countries_cursor_loop: LOOP			
			-- FETCH NEXT Country FROM countries_cursor
			FETCH FROM countries_cursor INTO strCountry;			
			SELECT COUNT(Title) from utubeVideos WHERE userID IN (
			SELECT authorID FROM utubeauthors WHERE Location =  strCountry)
			INTO intVideoCount;			
			SELECT COUNT(*) FROM utubeauthors WHERE Location = 	strCountry
			INTO intAuthorCount;
			INSERT INTO utube_geostats (Country, AuthorsCount, VideosCount)
			VALUES (strCountry, intAuthorCount, intVideoCount);							
			
			SET countries_cursor_loop_cntr = countries_cursor_loop_cntr + 1;
			IF countries_cursor_loop_cntr >= countries_cursor_num_rows THEN					
				CLOSE countries_cursor;
				LEAVE countries_cursor_loop;
			ELSE 	
				ITERATE countries_cursor_loop;
			END IF;		
		END LOOP countries_cursor_loop;			
END$$

