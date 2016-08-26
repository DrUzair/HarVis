DELIMITER ##
CREATE DEFINER=`uTube`@`localhost` PROCEDURE `utubedb_riyadh`.`test`()
BEGIN

DECLARE word VARCHAR(50);
SET word = 'الرياض';
SELECT COUNT(DISTINCT(Author)) FROM utubevideos 
WHERE Title LIKE CONCAT('%',word , '%');

END ##