DROP PROCEDURE IF EXISTS utubedb_riyadh.Discover_CommentsUoD_CoWords;
-- DROP TABLE IF EXISTS utubedb_riyadh.UoD;
-- CREATE  TABLE `utubedb_riyadh`.`UoD`(`Words` VARCHAR(60) NOT NULL ,`Frequency` INT NULL ,PRIMARY KEY (`Words`) );
-- GRANT ALL ON utubedb_riyadh.UoD TO 'uTubeConnTest'@'localhost' IDENTIFIED BY 'uTubePWD';
SET SQL_SAFE_UPDATES=0;
DELIMITER ##
CREATE DEFINER=`uTube`@`localhost` PROCEDURE `utubedb_riyadh`.`Discover_CommentsUoD_CoWords`(IN authCount INT)
BEGIN	
	DECLARE strWord VARCHAR(100);
	DECLARE intWordCount INT DEFAULT 0;
	DECLARE intWordAuthorsCount INT DEFAULT 0;	

	DECLARE strCoWord VARCHAR(100);
	DECLARE intCoWordCount INT DEFAULT 0;
	DECLARE intCoWordAuthorsCount INT DEFAULT 0;	
	
	DECLARE strCoWordCount INT DEFAULT 0;	
	DECLARE intCoOccurenceCount INT DEFAULT 0;	
	
	-- counters
	DECLARE words_loop_cntr INT DEFAULT 0;	
	DECLARE cowords_loop_cntr INT DEFAULT 0;	
	DECLARE rows_count INT DEFAULT 0;		
	DECLARE coword_rows_pointer INT DEFAULT 0;		

	-- Declare words_cursors
	DECLARE words_cursor CURSOR FOR 
	SELECT Word, WordCount, AuthorsCount FROM UoD_Comments_Temp 
	WHERE AuthorsCount > authCount 
	ORDER BY AuthorsCount DESC
	LIMIT 0, rows_count;	
	-- Declare cowords_cursors
	DECLARE cowords_cursor CURSOR FOR 
	SELECT Word, WordCount, AuthorsCount FROM UoD_Comments_Temp 
	WHERE AuthorsCount > authCount 
	ORDER BY AuthorsCount DESC
	LIMIT coword_rows_pointer, rows_count;

	-- COUNT coword_rows_count parameter for cowords_cursor
	SELECT COUNT(Word) FROM UoD_Comments_Temp 
	WHERE AuthorsCount > authCount 					
	INTO rows_count;						

	OPEN words_cursor;
		words_loop: LOOP			
		-- FETCH NEXT Word FROM UoD
			FETCH FROM words_cursor INTO strWord, intWordCount, intWordAuthorsCount;										
				-- INCREMENT coword_rows_pointer parameter for cowords_cursor	
				SET coword_rows_pointer = coword_rows_pointer + 1;																		
				SET cowords_loop_cntr = coword_rows_pointer;
				OPEN cowords_cursor;					
					cowords_loop: LOOP 
						-- FETCH NEXT Word FROM UoD
						FETCH FROM cowords_cursor INTO strCoWord, intCoWordCount, intCoWordAuthorsCount;					
						-- COUNT ALL Titles containing Word AND CoWord 				
						SET intCoOccurenceCount = 0;
						SELECT COUNT(Content)
						FROM utubevideocomments
						-- Put spaces around the strWord to find only complete words
						-- It will restrict counting Titles containing strWord mixed into other words (warrior <--> war)
						WHERE Title LIKE CONCAT('% ', strWord,' %') AND
						Title LIKE CONCAT('% ', strCoWord,' %')
						INTO intCoOccurenceCount;
						IF (intCoOccurenceCount > 0) THEN
							INSERT INTO UoD_Comments (Word, WordCount, AuthorsCount, CoWord, CoWordCount, CoWordAuthorsCount, CoOccurenceCount)
							VALUES (strWord, intWordCount, intWordAuthorsCount, strCoWord, intCoWordCount, intCoWordAuthorsCount, intCoOccurenceCount);
						END IF;
						-- UNTIL LAST Word IN cowords_cursor
						-- Control the Loop													
						SET cowords_loop_cntr = cowords_loop_cntr + 1;
						-- SELECT strWord, strCoWord, intCoOccurenceCount, cowords_loop_cntr, rows_count, coword_rows_pointer;							
						IF (cowords_loop_cntr >= rows_count) THEN							
							CLOSE cowords_cursor;
							LEAVE cowords_loop;
						END IF;												
					END LOOP cowords_loop;	
				-- UNTIL LAST Word IN words_cursor
				-- Control the Loop					
				-- SELECT strWord, words_loop_cntr, rows_count;
				SET words_loop_cntr = words_loop_cntr + 1;							
				IF words_loop_cntr >= (rows_count-1) THEN
					-- rows_count-1 because last strWord will not have a coWord	
					CLOSE words_cursor;
					LEAVE words_loop;
				END IF;					
			-- UNTIL LAST Word IN words_cursor
		END LOOP words_loop;			
END;##