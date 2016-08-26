DROP PROCEDURE IF EXISTS utubedb_riyadh.CommentVideoAuthorsProfile;
-- DROP TABLE IF EXISTS utubedb_riyadh.UoD;
-- CREATE  TABLE `utubedb_riyadh`.`UoD`(`Words` VARCHAR(60) NOT NULL ,`Frequency` INT NULL ,PRIMARY KEY (`Words`) );
-- GRANT ALL ON utubedb_riyadh.UoD TO 'uTubeConnTest'@'localhost' IDENTIFIED BY 'uTubePWD';
SET SQL_SAFE_UPDATES=0;
DELIMITER ##
CREATE DEFINER=`uTube`@`localhost` PROCEDURE `utubedb_riyadh`.`CommentVideoAuthorsProfile`()
BEGIN	
	DECLARE strCommentAuthor VARCHAR(100);
	DECLARE intCommentCount INT DEFAULT 0;	

	DECLARE strVideoAuthor VARCHAR(100);
	DECLARE strVideoID VARCHAR(100);
	DECLARE strVideoTitle VARCHAR(200);	
		
	-- counters
	DECLARE videos_cursor_num_rows INT DEFAULT 0;	
	DECLARE authors_cursor_num_rows INT DEFAULT 0;	
	DECLARE videos_loop_cntr INT DEFAULT 1;	
	DECLARE authors_loop_cntr INT DEFAULT 1;	
	
	-- Declare comment_authors_cursor
	DECLARE comment_authors_cursor CURSOR FOR 
	SELECT Author AS CommentAuthor, COUNT(*) AS CommentCount
    FROM utubevideocomments
    GROUP BY Author
    Order By CommentCount DESC LIMIT 100;	

	-- Declare videos_cursor
	DECLARE videos_cursor CURSOR FOR 
	SELECT  DISTINCT(VideoID), VideoTitle, VideoAuthor 
	FROM utubevideocomments
	WHERE Author = 	strCommentAuthor;		
		
	OPEN comment_authors_cursor;
		SELECT FOUND_ROWS() INTO authors_cursor_num_rows;
		
		SET authors_loop_cntr = 0 ;
		comment_authors_loop: LOOP			
		-- FETCH NEXT Author FROM comment_authors_cursor
			FETCH FROM comment_authors_cursor INTO strCommentAuthor, intCommentCount;										
				OPEN videos_cursor;					
					SELECT FOUND_ROWS() INTO videos_cursor_num_rows;						
					SET videos_loop_cntr = 0 ;
					videos_loop: LOOP 
						-- FETCH NEXT Video FROM videos_cursor
						FETCH FROM videos_cursor INTO strVideoID, strVideoTitle, strVideoAuthor;					
						INSERT INTO Commentvideoauthors (CommentAuthor, CommentCount, VideoAuthor, VideoID, VideoTitle)
						VALUES (strCommentAuthor, intCommentCount, strVideoAuthor, strVideoID, strVideoTitle);
						-- UNTIL LAST Video IN videos_cursor
						-- Control the Loop													
						SET videos_loop_cntr = videos_loop_cntr + 1;
						-- SELECT strWord, strCoWord, intCoOccurenceCount, cowords_loop_cntr, rows_count, coword_rows_pointer;							
						IF (videos_loop_cntr >= videos_cursor_num_rows) THEN							
							CLOSE videos_cursor;							
							LEAVE videos_loop;	
							SELECT 'EXITING', authors_cursor_num_rows, authors_loop_cntr, videos_cursor_num_rows, videos_loop_cntr;			
						END IF;									
					END LOOP videos_loop;	
				-- UNTIL LAST Author IN comment_authors_cursor
				-- Control the Loop									
				SET authors_loop_cntr = authors_loop_cntr + 1;							
				IF authors_loop_cntr >= authors_cursor_num_rows THEN
					-- rows_count-1 because last strWord will not have a coWord	
					CLOSE comment_authors_cursor;
					LEAVE comment_authors_loop;
				END IF;					
			-- UNTIL LAST Author IN comment_authors_cursor
		END LOOP comment_authors_loop;			
END;##