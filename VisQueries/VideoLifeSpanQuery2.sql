SELECT VT.ID, VT.Title, VT.Author, VT.CommentsCount, VT.ViewsCount,
Date_format(VT.CreatedOn, '%d-%m-%Y') AS UploadDate,
Date_format(min(CT.CreatedOn), '%d-%m-%Y') AS FirstCommentDate, 
Date_format(max(CT.CreatedOn), '%d-%m-%Y') AS LastCommentDate 
FROM utubevideos AS VT
INNER JOIN utubevideocomments AS CT
ON VT.ID = CT.VideoID
WHERE  (VT.ID = CT.VideoID) AND (VT.CommentsCount > 100)
GROUP By CT.VideoID 
ORDER BY VT.CommentsCount DESC
LIMIT 2000

