DROP PROCEDURE IF EXISTS `utubedb_riyadh`.`exportUoDTitlesData`;
SET SQL_SAFE_UPDATES=0;

DELIMITER ##
CREATE DEFINER=`uTube`@`localhost` PROCEDURE `utubedb_riyadh`.`exportUoDTitlesData`(IN wordCountLimit INT, IN coWordCountLimit INT)
BEGIN
	DECLARE auth VARCHAR(150);
	DECLARE word VARCHAR(150);
	DECLARE coword VARCHAR(150);
	DECLARE wordCount INT;
	DECLARE coWordCount INT;
	DECLARE authCount INT; 	
	DECLARE cowords_loop_cntr INT DEFAULT 0;
	DECLARE words_loop_cntr INT DEFAULT 0;	
	DECLARE num_rows INT DEFAULT 0;
	

	-- WORDS_CURSOR DECLARATION
	DECLARE words_cursor CURSOR FOR 
	SELECT Words, Max(Frequency) AS MAX_FREQ 
	FROM UoD 
	WHERE Frequency > wordCountLimit AND Words NOT IN ('في','يا','من','بن', 'عن', 'مع', 'ال')
	GROUP BY Words  
	ORDER BY MAX_FREQ DESC;
	-- COWORDS_CURSOE DECLARATION	
	DECLARE cowords_cursor CURSOR FOR 
		select CoWords, CoOccurCount from UoD  
        WHERE Words = word AND CoOccurCount > coWordCountLimit 
		AND CoWords NOT IN ('في','يا','من','بن', 'عن', 'مع', 'ال')  -- ('part', 'the', 'new', 'and', 'not', 'you','aur', 'hai', 'very', 'best', 'with') 
        ORDER BY CoOccurCount DESC;

	OPEN words_cursor;		
	words_loop: LOOP			
			FETCH words_cursor 
			INTO word, wordCount;			
			
			SELECT COUNT(DISTINCT(Author)) FROM utubevideos 
			WHERE Title LIKE CONCAT('%', word, '%')
			INTO authCount;

			OPEN cowords_cursor;		
			cowords_loop: LOOP			
				FETCH cowords_cursor 
				INTO coword, coWordCount;
				-- INSERT INTO CommentAuthorsProfile
				INSERT INTO CommentAuthorsProfile (Author, CommentsCount, AllVideosCount, OthersVideosCount)
				VALUES (auth, comntCount, allVidCount, othersVidCount); 				
				SELECT auth, vid_auth, num_rows, vid_id, comntCount, allVidCount, othersVidCount;			
			END LOOP cowords_loop;
			SET words_loop_cntr = words_loop_cntr + 1;
			IF words_loop_cntr > 100 THEN
				CLOSE words_cursor;
				LEAVE words_loop;
			END IF;
	END LOOP words_loop;	
END; ##