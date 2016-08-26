DROP PROCEDURE IF EXISTS utubedb_riyadh.Discover_Comments_UoD_Words;
SET SQL_SAFE_UPDATES=0;
DELIMITER ##
CREATE DEFINER=`uTube`@`localhost` PROCEDURE `utubedb_riyadh`.`Discover_Comments_UoD_Words`(IN start_index INT, IN batch_size INT)
BEGIN	
	DECLARE strWord VARCHAR(1000);
	DECLARE wordCount INT DEFAULT 0;
	DECLARE authorsCount INT DEFAULT 0;	
	DECLARE sentence VARCHAR(2000);
	DECLARE loop_cntr INT DEFAULT 1;	
	
	
	DECLARE end_pos INT DEFAULT 1;
	DECLARE wordExists INT DEFAULT 0;
	DECLARE len INT DEFAULT 0;
	
	-- Declare cursor
	DECLARE comments_cursor CURSOR FOR 
	SELECT Content from utubevideocomments LIMIT start_index, batch_size; 
	OPEN comments_cursor;					
		comments_loop: LOOP			
		-- FETCH NEXT Comment FROM utubevideoComments 	
			FETCH FROM comments_cursor INTO sentence;		
			
			SET sentence = LOWER(sentence);	
			-- PRUNE NON_ALPHABET CHARACTERS
SET sentence = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(sentence, '{',' '),'}', ' '), '[', ' '), ']',' '),'(',' '), ')',' '), '<', ' '), '>', ' '), '\'',' '), '/', ' '), ',',' '), '.',' '), '"', ' ');					
SET sentence = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(sentence, '\n', ' '), '_', ' '), '=' , ' '), '؟',' '),'?', ' '), '`', ' '), '!',' '), '@',' '), '#', ' '), '$', ' '), '%',' '),'^', ' '), '&', ' '), '*', ' '), ';', ' '), ':', ' ');
SET sentence = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(sentence, '+',' '), '-',' '), '÷', ' '), '0',' '),'1', ' '), '2',' '),'3',' '), '4',' '), '5', ' '), '6',' '), '7', ' '), '8',' '), '9',' ');					
			SET len = CHAR_LENGTH(sentence);		
		-- PARSE Words (Seperated by Space) IN sentence
			WHILE len > 0 DO				
		-- FOR EACH Word in Sentece			
				SET end_pos = LOCATE(' ', sentence);				
				IF end_pos > 0 THEN 
					SET strWord = TRIM(SUBSTRING(sentence, 1, end_pos));				
					SET sentence = TRIM(SUBSTRING(sentence, end_pos));
				ELSE 
					SET strWord = TRIM(SUBSTRING(sentence, 1, CHAR_LENGTH(sentence)));				
					SET sentence = '';
				END IF;					
				SET len = CHAR_LENGTH(sentence);
				-- SKIP Words like (في, من, مع, in, to, go, my etc)						
				IF (CHAR_LENGTH(strWord) > 2 AND CHAR_LENGTH(strWord) < 40) THEN
					-- CHECK IF Word EXISTS in UoD_Comments_Temp				
					SELECT count(*) FROM UoD_Comments_Temp 
					WHERE Word = strWord 
					INTO wordExists;
					IF (wordExists < 1) THEN				
					-- COUNT WordCount, AuthorsCount FROM ALL Titles 
					-- CONTAINING Word in utubevideos.Title
						SELECT COUNT(Content), COUNT(Distinct(Author)) 
						FROM UtubeVideoComments
						-- Put spaces around the strWord to find only complete words
						-- It will restrict counting Titles containing strWord mixed into other words (warrior <--> war)
						WHERE Content LIKE CONCAT('% ', strWord,' %') 
						INTO WordCount, AuthorsCount;
					-- CREATE NEW RECORD (Word, WordCount, AuthorsCount) 
						IF (WordCount > 0) THEN
							INSERT INTO UoD_Comments_Temp (Word, WordCount, AuthorsCount) 
							VALUES (strWord, wordCount, authorsCount);
						END IF;
						-- SELECT sentence, loop_cntr, strWord, wordCount, authorsCount;
					END IF;			
				END IF;	
			END WHILE; 
			-- UNTIL LAST Word IN sentence
			-- Control the Loop		
			SET loop_cntr = loop_cntr + 1;			
			IF loop_cntr > batch_size THEN
				CLOSE comments_cursor;
				LEAVE comments_loop;
			END IF;			
		-- UNTIL LAST sentence IN comments_cursor
		END LOOP comments_loop;			
END;##