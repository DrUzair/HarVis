SET SQL_SAFE_UPDATES=0;
SELECT ViewsCount, Likes, Dislikes, FavoritesCount, Title, Author 
FROM utubedb_riyadh.utubevideos 
WHERE ViewsCount > 100000 
Order by ViewsCount DESC LIMIT 2000