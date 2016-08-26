package yt.har;

//import com.google.gdata.client.Query;
//import com.google.gdata.client.*;
//import com.google.gdata.client.youtube.*;
//import com.google.gdata.client.media.MediaService;
//import javax.mail.MessagingException;

//import com.google.gdata.client.*;
//import com.google.gdata.data.DateTime;
//import com.google.gdata.data.Feed;
//import com.google.gdata.data.Entry;
//import com.google.gdata.data.Person;
//import com.google.gdata.data.TextConstruct;
//import com.google.gdata.data.TextContent;
//import com.google.gdata.data.youtube.VideoFeed;
//import com.google.gdata.data.youtube.VideoEntry;
//import com.google.gdata.data.youtube.YtRating;
//import com.google.gdata.data.youtube.YtStatistics;
//import com.google.gdata.data.extensions.Comments;


import java.util.HashMap;
import java.util.Map;
import java.util.Vector;


public class uTubeCampaignManager {
	static final long serialVersionUID = 123456; 
	private Map<String, uTubeVideoData> videoDataMap = new HashMap<String, uTubeVideoData>();
	public Vector<uTubeVideoData> videoDataVector = new Vector<uTubeVideoData>(10, 10); 

	// Working threads
	public uTubeCrawler uTubeCrawler;
	public uTubeDataManager uTubeDM;
	public uTubeClientThreadsManager threadManager;
	// Thread control variables
	protected boolean updateReceived;
	protected boolean updateSaved;
	
	public uTubeCampaignManager(){		
		
	}
	
//	private static final String YOUTUBE_URL = "http://gdata.youtube.com/feeds/api/videos"; 

	void setCrawlerUpdate(Map<String, uTubeVideoData> videoDataMap){
		this.videoDataMap = videoDataMap;
		updateReceived = true;
	}
	
	public void setReloadedData(Map<String, uTubeVideoData> videoDataMap){
		this.videoDataMap = videoDataMap;		
	}
/**
Create a database in MySQL.
CREATE DATABASE uTubeDB DEFAULT CHARACTER SET utf8 COLLATE utf8_unicode_ci; 


Create an user for Java and grant it access. Simply because using root is a bad practice.
CREATE USER 'uTube'@'localhost' IDENTIFIED BY 'uTubePWD'; 
GRANT ALL ON uTubeDB.* TO 'uTube'@'localhost' IDENTIFIED BY 'uTubePWD'; 

delimiter $$
CREATE TABLE `utubevideos` (
  `Author` varchar(150) DEFAULT NULL,
  `Category` varchar(45) DEFAULT NULL,
  `CommentsCount` int(11) DEFAULT NULL,
  `CreatedOn` datetime DEFAULT NULL,
  `Description` varchar(600) DEFAULT NULL,
  `DescriptionXed` varchar(600) DEFAULT NULL,
  `Dislikes` int(11) DEFAULT NULL,
  `Duration` float DEFAULT NULL,
  `FavoritesCount` int(11) DEFAULT NULL,
  `ID` varchar(50) NOT NULL,
  `Likes` int(11) DEFAULT NULL,
  `Location` varchar(100) DEFAULT NULL,
  `Rating` float DEFAULT NULL,
  `QueryString` varchar(200) DEFAULT NULL,
  `QueryUrl` varchar(200) DEFAULT NULL,
  `QueryFullText` varchar(500) DEFAULT NULL,
  `Title` varchar(200) DEFAULT NULL,
  `ViewsCount` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  FULLTEXT KEY `Title` (`Title`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8$$


delimiter $$
CREATE TABLE `utubevideocomments` (
  `ID` varchar(60) CHARACTER SET utf8 NOT NULL,
  `VideoID` varchar(45) CHARACTER SET utf8 DEFAULT NULL,
  `Author` varchar(150) CHARACTER SET utf8 DEFAULT NULL,
  `CreatedOn` varchar(45) CHARACTER SET utf8 DEFAULT NULL,
  `Content` text COLLATE utf8_bin,
  `VideoTitle` varchar(200) CHARACTER SET utf8 DEFAULT NULL,
  `VideoAuthor` varchar(150) CHARACTER SET utf8 DEFAULT NULL,
  PRIMARY KEY (`ID`),
  FULLTEXT KEY `VideoID_Index` (`VideoID`),
  FULLTEXT KEY `CommentAuthor_Index` (`Author`),
  FULLTEXT KEY `CommentContent_Index` (`Content`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_bin$$


SET SQL_SAFE_UPDATES=0;
DELETE from utubedb.utubevideos
*/
	

}
