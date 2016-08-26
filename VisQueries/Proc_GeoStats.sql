DROP PROCEDURE IF EXISTS `Proc_GeoStats`;
SET SQL_SAFE_UPDATES=0;
DELETE FROM utube_geostats;
delimiter $$
CREATE DEFINER=`uTube`@`localhost` PROCEDURE `Proc_GeoStats`()
BEGIN		
	DECLARE strISO_Code VARCHAR(2);
	DECLARE strCountry VARCHAR(100);
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
			FETCH FROM countries_cursor INTO strISO_Code;			
			SELECT COUNT(Title) FROM utubeVideos WHERE userID IN (
			SELECT authorID FROM utubeauthors WHERE Location =  strISO_Code)
			INTO intVideoCount;			
			SELECT COUNT(*) FROM utubeauthors WHERE Location = 	strISO_Code
			INTO intAuthorCount;			
			SELECT Country FROM country_iso_codes WHERE ISO_Code=strISO_Code	
			INTO strCountry;
			INSERT INTO utube_geostats (ISO_Code, Country, AuthorsCount, VideosCount)
			VALUES (strISO_Code, strCountry, intAuthorCount, intVideoCount);							
			
			SET countries_cursor_loop_cntr = countries_cursor_loop_cntr + 1;
			IF countries_cursor_loop_cntr >= countries_cursor_num_rows THEN					
				CLOSE countries_cursor;
				LEAVE countries_cursor_loop;
			ELSE 	
				ITERATE countries_cursor_loop;
			END IF;		
		END LOOP countries_cursor_loop;			
END$$

