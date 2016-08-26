SET sql_safe_updates=0;
DROP PROCEDURE IF EXISTS CommentVideoAuthorsNet_MDVA;
delimiter $$
CREATE DEFINER=`uTube`@`localhost` PROCEDURE 
`CommentVideoAuthorsNet_MDVA`(IN TOP_N_VIDEO_AUTHORS int, IN MIN_COMMENT_AUTHORS_COUNT INT, IN MIN_COMMENTED_VIDEOS_COUNT INT)
BEGIN	
	DECLARE strCommentAuthor VARCHAR(100);
	DECLARE intCommentedVideosCount INT DEFAULT 0;	
	DECLARE intCommentAuthorCount INT DEFAULT 0;	
	DECLARE intTotalRecievedCommentCount INT DEFAULT 0;	
	DECLARE intVideosCountRcvngComntFromCommentAuthor INT DEFAULT 0;	
	DECLARE intUploadedVideosCount INT DEFAULT 0;	

	DECLARE strVideoAuthor VARCHAR(100);
	DECLARE strVideoID VARCHAR(100);
	DECLARE strVideoTitle VARCHAR(500);	
	
	-- counters
	DECLARE video_authors_cursor_num_rows INT DEFAULT 0;	
	DECLARE comment_authors_cursor_num_rows INT DEFAULT 0;	
	DECLARE videos_authors_loop_cntr INT DEFAULT 1;	
	DECLARE comment_authors_loop_cntr INT DEFAULT 1;	
	
	-- Declare video_authors_cursor [most commented-on video_authors]
	DECLARE video_authors_cursor CURSOR FOR 	
	SELECT Author, SUM(CommentsCount) AS CommentsCount, COUNT(ID) AS VideosCount
	FROM utubevideos
	GROUP BY Author		
	ORDER BY CommentsCount DESC 
	LIMIT TOP_N_VIDEO_AUTHORS;

	-- Declare comment_authors_cursor [authors who commented on videos of strVideoAuthor]
	DECLARE comment_authors_cursor CURSOR FOR 
	SELECT Distinct(Author) from utubevideocomments
	WHERE VideoAuthor = strVideoAuthor;	

	-- DELETE the contents of commentvideoauthors_mdva
	DELETE FROM commentvideoauthors_mdva;	
	OPEN video_authors_cursor;		
		SELECT FOUND_ROWS() INTO video_authors_cursor_num_rows;	
		SET videos_authors_loop_cntr = 1;		
		videos_authors_loop: LOOP			
			-- FETCH NEXT Most Discussed Video FROM videos_cursor
			FETCH FROM video_authors_cursor INTO strVideoAuthor, intTotalRecievedCommentCount, intUploadedVideosCount;												
				OPEN comment_authors_cursor;									
					SELECT FOUND_ROWS() INTO comment_authors_cursor_num_rows;				
					IF (comment_authors_cursor_num_rows > MIN_COMMENT_AUTHORS_COUNT) THEN
						SET comment_authors_loop_cntr = 1 ;
						comment_authors_loop: LOOP 
							-- FETCH NEXT Author FROM comment_authors_cursor 
							-- who commented on video of strVideoAuthor
							FETCH FROM comment_authors_cursor INTO strCommentAuthor;																	
							SELECT COUNT(DISTINCT(VideoID)) FROM utubevideocomments
							WHERE Author = strCommentAuthor
							INTO intCommentedVideosCount; 						
						
							SELECT COUNT(DISTINCT(VideoID)) FROM utubevideocomments
							WHERE VideoAuthor = strVideoAuthor
							AND Author = strCommentAuthor						
							INTO intVideosCountRcvngComntFromCommentAuthor;
						
							IF intCommentedVideosCount >= MIN_COMMENTED_VIDEOS_COUNT THEN
								INSERT INTO CommentVideoAuthors_MDVA (CommentAuthor, CommentedVideosCount, VideoAuthor, VideosCountRcvngComntFromCommentAuthor, TotalRecievedCommentCount, UploadedVideosCount)
								VALUES (strCommentAuthor, intCommentedVideosCount, strVideoAuthor, intVideosCountRcvngComntFromCommentAuthor, intTotalRecievedCommentCount, intUploadedVideosCount);							
							END IF;
							-- UNTIL LAST Author IN comment_authors_cursor
							-- Control the comment_authors_loop 																			
							SET comment_authors_loop_cntr = comment_authors_loop_cntr + 1;
							-- SELECT strWord, strCoWord, intCoOccurenceCount, cowords_loop_cntr, rows_count, coword_rows_pointer;							
							IF (comment_authors_loop_cntr >= comment_authors_cursor_num_rows) THEN							
								CLOSE comment_authors_cursor;							
								LEAVE comment_authors_loop;	
								-- SELECT 'EXITING', authors_cursor_num_rows, authors_loop_cntr, videos_cursor_num_rows, videos_loop_cntr;			
							ELSE
								ITERATE comment_authors_loop;
							END IF;									
						END LOOP comment_authors_loop;	
					-- UNTIL LAST Author IN comment_authors_cursor
					-- Control the Loop	
					ELSE
						SELECT comment_authors_cursor_num_rows, "Not enough comments Closing Author Cursor" ;
						CLOSE comment_authors_cursor;
					END IF; -- MIN_COMMENT_AUTHORS_COUNT condition
					SET videos_authors_loop_cntr = videos_authors_loop_cntr + 1;							
					IF videos_authors_loop_cntr >= video_authors_cursor_num_rows THEN					
						CLOSE video_authors_cursor;
						LEAVE videos_authors_loop;
					ELSE 	
						ITERATE videos_authors_loop;
					END IF;								
					-- UNTIL LAST Author IN comment_authors_cursor			
		END LOOP videos_authors_loop;			
END$$

