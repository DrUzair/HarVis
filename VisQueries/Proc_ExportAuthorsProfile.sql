DROP PROCEDURE IF EXISTS `utubedb_riyadh`.`exportAuthorsProfile`;
SET SQL_SAFE_UPDATES=0;

DELIMITER ##
CREATE DEFINER=`uTube`@`localhost` PROCEDURE `utubedb_riyadh`.`exportAuthorsProfile`(IN commentCountLimit INT)
BEGIN
	DECLARE auth VARCHAR(150);
	DECLARE vid_auth VARCHAR(150);
	DECLARE vid_id VARCHAR(150);
	DECLARE num_rows INT DEFAULT 0;
	DECLARE comntCount INT;
	DECLARE allVidCount INT; -- Including self and others
	DECLARE othersVidCount INT; -- Only others videos
	DECLARE auth_loop_cntr INT DEFAULT 0;
	DECLARE video_loop_cntr INT DEFAULT 0;

	DECLARE video_cursor CURSOR FOR 
	SELECT VideoAuthor, VideoID FROM utubevideocomments
	WHERE Author = auth
	GROUP BY VideoID;

	-- DR CURSOR DECLARATION
	DECLARE author_cursor CURSOR FOR 
		SELECT Author, COUNT(*) AS CommentsCount
		FROM utubevideocomments 
		GROUP BY Author
		ORDER BY CommentsCount DESC;			
	OPEN author_cursor;		
	author_loop: LOOP			
			FETCH author_cursor 
			INTO auth, comntCount;			
			
			-- FIND VideoCount IF comntCount > commentCountLimit
			IF comntCount > commentCountLimit THEN					
				OPEN video_cursor;
				select FOUND_ROWS() into num_rows;
				SET allVidCount = 0;
				SET othersVidCount = 0;
				SET video_loop_cntr = 1;
				-- loop through video_cursor
				video_loop: LOOP
					FETCH video_cursor
					INTO vid_auth, vid_id;
					SET allVidCount = allVidCount + 1;					
					IF vid_auth != auth THEN
						SET othersVidCount = othersVidCount + 1;
					END IF;					
					SET video_loop_cntr = video_loop_cntr + 1;
					IF video_loop_cntr >= num_rows THEN
						CLOSE video_cursor;
						LEAVE video_loop;
					END IF;					
					
				END LOOP video_loop;
				-- SELECT auth, comntCount, allVidCount, othersVidCount, vid_id;
				-- SELECT COUNT(DISTINCT(VideoID)) AS VideoCount 
				-- FROM utubedb_riyadh.utubevideocomments 
				-- WHERE Author = auth 
				-- INTO allVidCount;
				
				-- SELECT COUNT(DISTINCT(VideoID)) AS VideoCount 
				-- FROM utubedb_riyadh.utubevideocomments 
				-- WHERE Author = auth AND VideoAuthor != author
				-- INTO othersVidCount;
				
			-- INSERT INTO CommentAuthorsProfile
				INSERT INTO CommentAuthorsProfile (Author, CommentsCount, AllVideosCount, OthersVideosCount)
				VALUES (auth, comntCount, allVidCount, othersVidCount); 				
				SELECT auth, vid_auth, num_rows, vid_id, comntCount, allVidCount, othersVidCount;
			END IF;

			SET auth_loop_cntr = auth_loop_cntr + 1;
			IF auth_loop_cntr > 100 THEN
				CLOSE author_cursor;
				LEAVE author_loop;
			END IF;
	END LOOP author_loop;	
END; ##