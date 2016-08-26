SELECT Author, COUNT(*) AS VidCount, 
	Date_format(MIN(CreatedOn), '%d-%m-%y') AS ActiveSince,
	Date_format(MAX(CreatedOn), '%d-%m-%y') AS ActiveUntil,
	ROUND(DATEDIFF(MAX(CreatedOn), MIN(CreatedOn))/365,2) AS LifeTimeInYears,
	ROUND(AVG(ViewsCount)) AS AVG_ViewsCount 
FROM utubevideos 
GROUP BY Author Order By VidCount DESC 
LIMIT 100