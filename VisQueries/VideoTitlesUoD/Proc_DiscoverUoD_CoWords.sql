DROP PROCEDURE IF EXISTS utubedb_riyadh.Discover_UoD_CoWords;
-- DROP TABLE IF EXISTS utubedb_riyadh.UoD;
-- CREATE  TABLE `utubedb_riyadh`.`UoD`(`Words` VARCHAR(60) NOT NULL ,`Frequency` INT NULL ,PRIMARY KEY (`Words`) );
-- GRANT ALL ON utubedb_riyadh.UoD TO 'uTubeConnTest'@'localhost' IDENTIFIED BY 'uTubePWD';
SET SQL_SAFE_UPDATES=0;
DELIMITER ##
CREATE DEFINER=`uTube`@`localhost` PROCEDURE `utubedb_riyadh`.`Discover_UoD_CoWords`(IN start_index INT, IN batch_size INT)
BEGIN	
	DECLARE strWord VARCHAR(100);
	DECLARE strWordCount INT DEFAULT 0;
	DECLARE strCoWord VARCHAR(100);
	DECLARE strCoWordCount INT DEFAULT 0;	
	DECLARE strCoWordTitlesCount INT DEFAULT 0;	
	DECLARE sentence VARCHAR(2000);
	DECLARE words_loop_cntr INT DEFAULT 0;	
	DECLARE titles_loop_cntr INT DEFAULT 0;	
	DECLARE titlesCount INT DEFAULT 0;	

	DECLARE space_char_pos INT DEFAULT 1;	
	DECLARE newline_char_pos INT DEFAULT 1;	
	DECLARE end_pos INT DEFAULT 1;
	DECLARE len INT DEFAULT 0;
	
	-- Declare cursors
	DECLARE words_cursor CURSOR FOR 
	SELECT Word from UoD WHERE WordCount > 100;
	DECLARE titles_cursor CURSOR FOR 
	SELECT Title from utubevideos WHERE Title LIKE CONCAT('% ', strWord, ' %'); 
	OPEN words_cursor;					
		words_loop: LOOP			
		-- FETCH NEXT Title FROM utubevideos 	
			FETCH FROM words_cursor INTO strWord;	
			SELECT COUNT(Title) from utubevideos WHERE Title LIKE CONCAT('% ', strWord, ' %')
			INTO titlesCount;
			SET titles_loop_cntr = 0;	
			OPEN titles_cursor;
			titles_loop: LOOP
					FETCH FROM titles_cursor INTO sentence;
					SET sentence = LOWER(sentence);	
				-- PRUNE NON_ALPHABET CHARACTERS
					SET sentence = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(sentence, '{',' '),'}', ' '), '[', ' '), ']',' '),'(',' '), ')',' '), '<', ' '), '>', ' '), '\'',' '), '/', ' '), ',',' '), '.',' '), '"', ' ');					
					SET sentence = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(sentence, '؟',' '),'?', ' '), '`', ' '), '!',' '), '@',' '), '#', ' '), '$', ' '), '%',' '),'^', ' '), '&', ' '), '*', ' '), ';', ' '), ':', ' ');								
					SET sentence = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(sentence, '+',' '), '-',' '), '÷', ' '), '0',' '),'1', ' '), '2',' '),'3',' '), '4',' '), '5', ' '), '6',' '), '7', ' '), '8',' '), '9',' ');					
					SET len = LENGTH(sentence);
					
				-- PARSE Words (Seperated by Space) IN sentence
					WHILE len > 0 DO				
				-- FOR EACH Word in Sentece			
						SET space_char_pos = LOCATE(' ', sentence);
						SET end_pos = space_char_pos;
						SET newline_char_pos = LOCATE('\n', sentence);
						IF (newline_char_pos < end_pos) THEN
							SET end_pos = newline_char_pos;
						END IF;
						IF end_pos > 0 THEN 
							SET strCoWord = TRIM(SUBSTRING(sentence, 1, end_pos));				
							SET sentence = TRIM(SUBSTRING(sentence, end_pos));
						ELSE 
							SET strCoWord = TRIM(SUBSTRING(sentence, 1, LENGTH(sentence)));				
							SET sentence = '';
						END IF;	
						
						SET len = LENGTH(sentence);
					-- SKIP Words like (في, من, مع, in, to, go, my etc)						
						IF (CHAR_LENGTH(strCoWord) > 2 AND strCoWord != strWord) THEN
						-- CHECK IF WordCount is significant (100) in UOD				
							SELECT WordCount FROM UoD 
							WHERE Word = strCoWord 
							INTO strCoWordCount;
							SELECT sentence, strWord, strCoWord, strCoWordCount;
							-- IF (strCoWordCount > 100) THEN				
							-- COUNT ALL Titles containing Word AND CoWord 							
								SELECT COUNT(Title)
								FROM utubevideos
							-- Put spaces around the strWord to find only complete words
							-- It will restrict counting Titles containing strWord mixed into other words (warrior <--> war)
								WHERE Title LIKE CONCAT('% ', strWord,' %') AND
								Title LIKE CONCAT('% ', strCoWord,' %')
								INTO strCoWordTitlesCount;
								SELECT sentence, strWord, strCoWord, strCoWordCount, strCoWordTitlesCount;
							-- UPDATE UoD RECORD (CoWord, CoWordCount) WHERE Word = strCoWord 
								UPDATE UoD SET CoWord = strWord, CoWordCount = strCoWordTitlesCount
								WHERE Word = strCoWord;
								SELECT titlesCount, sentence, strWord, strCoWord, coWordCount;
							-- END IF;			
						END IF;	
					END WHILE; -- Words in sentence End here
				SET titles_loop_cntr = titles_loop_cntr + 1;				
				IF titles_loop_cntr > 0 THEN
					CLOSE titles_cursor;
					LEAVE titles_loop;
				END IF;
			END LOOP titles_loop;
			
			-- UNTIL LAST Word IN words_cursor
			-- Control the Loop		
			SET words_loop_cntr = words_loop_cntr + 1;			
			IF words_loop_cntr > batch_size THEN
				CLOSE words_cursor;
				LEAVE words_loop;
			END IF;			
		-- UNTIL LAST Word IN words_cursor
		END LOOP words_loop;			
END;##