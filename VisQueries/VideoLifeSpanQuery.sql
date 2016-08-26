SELECT Date_format(min(CreatedOn), '%d-%m-%Y') AS Since, 
DATEDIFF(max(CreatedOn),min(CreatedOn))  AS LifeTime, 
count(*) CommentCount, 
VideoID, VideoTitle, VideoAuthor 
FROM utubevideocomments 
GROUP By VideoID 
ORDER BY CommentCount DESC