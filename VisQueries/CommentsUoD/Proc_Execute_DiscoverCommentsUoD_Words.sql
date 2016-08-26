DROP PROCEDURE IF EXISTS utubedb_riyadh.Execute_Discover_Comments_UoD_Words;
-- DROP TABLE IF EXISTS utubedb_test.UoD;
-- CREATE  TABLE `utubedb_riyadh`.`UoD`(`Words` VARCHAR(60) NOT NULL ,`Frequency` INT NULL ,PRIMARY KEY (`Words`) );
-- GRANT ALL ON uTubeDB_riyadh.UoD TO 'uTubeConnTest'@'localhost' IDENTIFIED BY 'uTubePWD';
SET SQL_SAFE_UPDATES=0;
DELIMITER ##
CREATE DEFINER=`uTube`@`localhost` PROCEDURE `utubedb_riyadh`.`Execute_Discover_Comments_UoD_Words`(IN startIndex INT)
BEGIN	
	DECLARE num_rows INT DEFAULT 0;
	DECLARE batches INT DEFAULT 0;
	DECLARE start_index INT DEFAULT 0;
	DECLARE batch_size INT DEFAULT 1000;
	DECLARE file_name TEXT;
	SELECT COUNT(*) FROM utubevideocomments INTO num_rows;
	SET batches = (num_rows/batch_size);
	SET start_index = startIndex;
	SELECT batches;
	WHILE start_index < num_rows DO		
		CALL Discover_Comments_UoD_Words(start_index, batch_size);
		SET start_index = start_index + batch_size;	
		SELECT @myCommand := concat("SELECT Content From UtubevideoComments LIMIT 1, 1 INTO OUTFILE 'D:\\indices", start_index, ".csv'");
		PREPARE stmt FROM @myCommand;
		EXECUTE stmt;
		SELECT start_index;
	END WHILE;	
END;