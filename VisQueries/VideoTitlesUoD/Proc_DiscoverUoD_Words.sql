DROP PROCEDURE IF EXISTS utubedb_riyadh.Discover_UoD_Words;
-- DROP TABLE IF EXISTS utubedb_riyadh.UoD;
-- CREATE  TABLE `utubedb_riyadh`.`UoD`(`Words` VARCHAR(60) NOT NULL ,`Frequency` INT NULL ,PRIMARY KEY (`Words`) );
-- GRANT ALL ON utubedb_riyadh.UoD TO 'uTubeConnTest'@'localhost' IDENTIFIED BY 'uTubePWD';
SET SQL_SAFE_UPDATES=0;
DELIMITER ##
CREATE DEFINER=`uTube`@`localhost` PROCEDURE `utubedb_riyadh`.`Discover_UoD_Words`(IN start_index INT, IN batch_size INT)
BEGIN	
	DECLARE strWord VARCHAR(100);
	DECLARE wordCount INT DEFAULT 0;
	DECLARE authorsCount INT DEFAULT 0;	
	DECLARE sentence VARCHAR(500);
	DECLARE loop_cntr INT DEFAULT 1;
	
	DECLARE end_pos INT DEFAULT 1;
	DECLARE wordExists INT DEFAULT 0;
	DECLARE len INT DEFAULT 0;
	
	-- Declare cursor
	DECLARE titles_cursor CURSOR FOR 
	SELECT Title from utubevideos LIMIT start_index, batch_size; 
	OPEN titles_cursor;					
		titles_loop: LOOP			
		-- FETCH NEXT Title FROM utubevideos 	
			FETCH FROM titles_cursor INTO sentence;					
			SET Sentence = LOWER(Sentence);	
			-- PRUNE NON_ALPHABET CHARACTERS
SET sentence = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(sentence, '{',' '),'}', ' '), '[', ' '), ']',' '),'(',' '), ')',' '), '<', ' '), '>', ' '), '\'',' '), '/', ' '), ',',' '), '.',' '), '"', ' ');					
SET sentence = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(sentence, '\n', ' '), '_', ' '), '=' , ' '), '؟',' '),'?', ' '), '`', ' '), '!',' '), '@',' '), '#', ' '), '$', ' '), '%',' '),'^', ' '), '&', ' '), '*', ' '), ';', ' '), ':', ' ');
SET sentence = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(sentence, '+',' '), '-',' '), '÷', ' '), '0',' '),'1', ' '), '2',' '),'3',' '), '4',' '), '5', ' '), '6',' '), '7', ' '), '8',' '), '9',' ');					

			SET len = CHAR_LENGTH(Sentence);		
		-- PARSE Words (Seperated by Space) IN Sentence
			WHILE len > 0 DO				
		-- FOR EACH Word in Sentece			
				SET end_pos = LOCATE(' ', sentence);
				IF end_pos > 0 THEN 
					SET strWord = TRIM(SUBSTRING(Sentence, 1, end_pos));				
					SET Sentence = TRIM(SUBSTRING(Sentence, end_pos));
				ELSE 
					SET strWord = TRIM(SUBSTRING(Sentence, 1, CHAR_LENGTH(Sentence)));				
					SET Sentence = '';
				END IF;			
				SET len = CHAR_LENGTH(sentence);
				-- SKIP Words like (في, من, مع, in, to, go, my etc)						
				IF (CHAR_LENGTH(strWord) > 2) THEN
					-- CHECK IF Word EXISTS in UOD				
					SELECT count(*) FROM UoD 
					WHERE Word = strWord 
					INTO wordExists;
					IF (wordExists < 1) THEN				
					-- COUNT WordCount, AuthorsCount FROM ALL Titles 
					-- CONTAINING Word in utubevideos.Title
						SELECT COUNT(Title), COUNT(Distinct(Author)) 
						FROM utubevideos
						-- Put spaces around the strWord to find only complete words
						-- It will restrict counting Titles containing strWord mixed into other words (warrior <--> war)
						WHERE Title LIKE CONCAT('% ', strWord,' %') 
						INTO WordCount, AuthorsCount;
					-- CREATE NEW RECORD (Word, WordCount, AuthorsCount) 
						INSERT INTO UoD (Word, WordCount, AuthorsCount) 
						VALUES (strWord, wordCount, authorsCount);
						-- SELECT sentence, loop_cntr, strWord, wordCount, authorsCount;
					END IF;			
				END IF;	
			END WHILE; 
			-- UNTIL LAST Word IN Sentence
			-- Control the Loop		
			SET loop_cntr = loop_cntr + 1;			
			IF loop_cntr > batch_size THEN
				CLOSE titles_cursor;
				LEAVE titles_loop;
			END IF;			
		-- UNTIL LAST Sentence IN titles_cursor
		END LOOP titles_loop;			
END;##