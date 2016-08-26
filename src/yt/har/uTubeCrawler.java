package yt.har;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import yt.HarVis;

import com.google.common.collect.Maps;
import javax.mail.MessagingException;

import com.google.gdata.client.youtube.YouTubeService;
import com.google.gdata.data.DateTime;
import com.google.gdata.data.Entry;
import com.google.gdata.data.Feed;
import com.google.gdata.data.Person;
import com.google.gdata.data.TextConstruct;
import com.google.gdata.data.TextContent;
import com.google.gdata.data.extensions.Comments;
import com.google.gdata.data.extensions.Rating;
import com.google.gdata.data.media.mediarss.MediaCategory;
import com.google.gdata.data.youtube.VideoEntry;
import com.google.gdata.data.youtube.VideoFeed;
import com.google.gdata.data.youtube.YouTubeMediaGroup;
import com.google.gdata.data.youtube.YtRating;
//import com.google.gdata.client.youtube.YouTubeQuery;
import com.google.gdata.util.ServiceException;
import com.google.gdata.util.XmlBlob;

public class uTubeCrawler implements Runnable{	
	private static final String YOUTUBE_URL = "http://gdata.youtube.com/feeds/api/videos"; 
	private int SEARCH_ROUND = 1;
	private int SERVICE_EXECUTION_COUNT = 0;
	private String words_2b_filtered = "";
	protected HashMap<String, HashMap<String, uTubeQuery>> queryStringsMap;
	private Map<String, String> uniqueKeyWordsMap;
	//protected HashMap<String, uTubeQuery> evovledQueryObjects;
	protected static YouTubeService service;	
	private Map<String, uTubeVideoData> videoDataMap;
	//private String developerKey = "AI39si6Vi8lLyuPXuqBTT0iDsGuhvcayuccp6L16xV5um8VT5dAR7B3-I6LDDxuErg0S8D1XdiIbg3GeatcRWfu2SgkRdPJ0gQ";
	private HarVis harvis;
	// private uTubeClientThreadsManager threadManager;
	private int videoCount;		
	private int maxResults;
	private int mode;
	public static final int VIDEOS_BY_KEYWORD = 0;
	public static final int VIDEOS_BY_AUTHOR = 1;	
	public static final int VIDEOS_BY_VIDEO_ID = 2;
 	private boolean keyWordsOrderFixed = true;	
	// true: Key words should appear in same order/place in the Title/Desc of the videos
	// false: Key words may appear in any order/any where in the Title/Desc of the videos
 	private boolean allKeyWordsMustMatch = true;	
 	public boolean isAllKeyWordsMustMatch() {
		return allKeyWordsMustMatch;
	}

	public void setAllKeyWordsMustMatch(boolean allKeyWordsMustMatch) {
		this.allKeyWordsMustMatch = allKeyWordsMustMatch;
	}
	// true: All keyWords must exist in Title/Desc
 	// false: One of the keywords must exist in Title/Desc
	private final String delim = " _-=+&\t\n.,:;?!@#$%^&*~/{}()[]\"\'1234567890";
	// NOTE: - character should not be used as delimiter
	// 			because may be used to name proper nouns
	
	public void setKeyWordsOrderFixed(boolean keyWordsOrderFixed) {
		this.keyWordsOrderFixed = keyWordsOrderFixed;
	}

	// CONSTRUCTOR FOR NEW CAMPAIGN MODE
	public uTubeCrawler(HashMap<String, HashMap<String, uTubeQuery>> queryStringsMap,						  					  
						  int maxReslts,
						  int download_mode,
						  HarVis harvis){				
		videoCount = 0 ;
		maxResults = maxReslts;
		mode = download_mode;
		videoDataMap = new HashMap<String, uTubeVideoData>();		
		
		try {
			// Make sure classes are available required by YouTubeService
			Class.forName("com.google.common.collect.Maps");
			com.google.common.collect.Maps.class.getClass();
			Class.forName("javax.mail.MessagingException");
			javax.mail.MessagingException.class.getClass();
			service = new YouTubeService(harvis.getHarvesterUserID(), harvis.getHarvesterUserKey());
		} catch (ClassNotFoundException e) {				
			e.printStackTrace();
		}
		this.harvis = harvis;				
		this.queryStringsMap = queryStringsMap;
	}
	
	// CONSTRUCTOR FOR RESUME CAMPAIGN MODE
	
	public uTubeCrawler(HashMap<String, HashMap<String, uTubeQuery>> queryStringsMap,
			Map<String, uTubeVideoData> existingVideosDataMap,
			int maxReslts,
			int download_mode,
			HarVis harvis){		
		this.queryStringsMap = queryStringsMap;			
		videoCount = existingVideosDataMap.size();
		maxResults = maxReslts;		
		mode = download_mode;
		videoDataMap = existingVideosDataMap;
		try {
			// Make sure classes are available required by YouTubeService
			Class.forName("com.google.common.collect.Maps");
			com.google.common.collect.Maps.class.getClass();
			Class.forName("javax.mail.MessagingException");
			javax.mail.MessagingException.class.getClass();
			service = new YouTubeService(harvis.getHarvesterUserID(), harvis.getHarvesterUserKey());
		} catch (ClassNotFoundException e) {				
			e.printStackTrace();
		}		
		this.harvis = harvis;					
	}
	private void loadFilterWords(){		
		words_2b_filtered = "";
		try {
			InputStream in = HarVis.class.getResourceAsStream("Resources/word_2b_filtered");
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String line = br.readLine();
			words_2b_filtered += line;
			while(line != null){
				System.out.println(line);
				line = br.readLine();
				words_2b_filtered += line;
			}
		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
	}
	public void setSearchRound(int i ){
		SEARCH_ROUND = i;
	}
	// MUST BE CALLED AFTER ascertaining videoExistsInCollection
	private void addVideoData2Collection(uTubeVideoData videoData){		
		videoDataMap.put(videoData.getVideoID(), videoData);
		videoCount++;		 
	}
	private boolean videoExistsInCollection(String videoID){		
		boolean videoExists = videoDataMap.containsKey(videoID); 
		if (videoExists)
			return true;
		return false;
	}
	private int getVideoCount(){return videoCount;}
	private String getFirstCategory(VideoEntry e) {
        YouTubeMediaGroup mg = e.getMediaGroup();
        MediaCategory fcat = mg.getCategories() != null && mg.getCategories().size() > 0 ? mg.getCategories().get(0) : null;
        return fcat != null? fcat.getLabel(): "-";
    }
	private uTubeVideoData captureVideoData( VideoEntry oneVideo, uTubeQuery query){       	      	
        	uTubeVideoData videoData = new uTubeVideoData();    
        	String trimmedID = oneVideo.getId().substring(27).trim();        	
        	//System.out.println(oneVideo.getId() + " Trimmed to "+ trimmedID);
            videoData.setVideoID(trimmedID); 											// ID
            DateTime dateTime = oneVideo.getPublished();
            videoData.setCreatedOn(new java.sql.Date(dateTime.getValue())); 			// CreatedOn
            TextConstruct oneVideoTitle = oneVideo.getTitle();
            String title = oneVideoTitle.getPlainText();
            title.replace('"', ' '); 
            // This is to make data readable from CSV, see VideoLifeSpanProfile
            title.replace(',', ' '); 
            // This is to make data readable from CSV, see VideoLifeSpanProfile
            videoData.setTitle(title);													// Video Title

            YouTubeMediaGroup mediaGroup = oneVideo.getMediaGroup();

            //System.out.println("Uploaded by: " + mediaGroup.getUploader());
            String description = mediaGroup.getDescription().getPlainTextContent().toLowerCase();
            if (description.length() > 600) 
            	// Description field in uTubeVideos table is Varchar(600)
            	description = description.substring(0, 599);
            videoData.setDescription(description);										// Description

            //String test  = oneVideo.getPlainTextContent();								// Description ?
            
            XmlBlob xmlBlob = oneVideo.getXmlBlob();
            String xml = xmlBlob.getFullText();
            
            
            List<Person> allAuthors = oneVideo.getAuthors();
            Iterator<Person> itAllAuthors = allAuthors.iterator();
            while (itAllAuthors.hasNext()){
                Person oneAuthor = itAllAuthors.next();         
                String userID 	= oneAuthor.getUri().substring(oneAuthor.getUri().lastIndexOf("/")+1);
                videoData.setAuthor(oneAuthor.getName());                				// Author
                videoData.setUserID(userID);											// User ID
                videoData.setAuthorEmail(oneAuthor.getEmail());							// Email
                videoData.setAuthorUri(oneAuthor.getUri());								// Uri
                videoData.setAuthorLanguage(oneAuthor.getNameLang());					// Name Language                
            }            
            videoData.setLocation(oneVideo.getLocation());								// Location
            videoData.setCategory(getFirstCategory(oneVideo));							// Category
            if (oneVideo.getStatistics() != null){            	
            	videoData.setViewCount(oneVideo.getStatistics().getViewCount());		// ViewsCount
            	videoData.setFavoriteCount(oneVideo.getStatistics().getFavoriteCount());// FavoritesCount
            }
            else{
            	videoData.setViewCount(0);
            	videoData.setFavoriteCount(0);
            }
             
            																			//Related Videos Feed Url
            if (oneVideo.getRelatedVideosLink() != null) {
            	  videoData.setRelatedVideosFeedUrl(oneVideo.getRelatedVideosLink().getHref());           	  
            }else{
            	videoData.setRelatedVideosFeedUrl(null);
            }
            																			// Comments
            Comments comments = oneVideo.getComments();
            int commentsCount = 0;
            if (oneVideo.getComments() != null && oneVideo.getComments().getFeedLink() != null){            	
            	commentsCount = comments.getFeedLink().getCountHint();
            	String feedLink = comments.getFeedLink().getHref();            	
            	int startIndex = 1;
            	int entriesReturned = 0; 
            	String startIndexStr = "&start-index="+startIndex;
            	        		            		
            		while(true){		            		            	
            			boolean exceptionOccured = false;
            			try {
	            			Feed feed = oneVideo.getService().getFeed(new URL(feedLink+startIndexStr), Feed.class);
	            			SERVICE_EXECUTION_COUNT++;
			            	entriesReturned = feed.getEntries().size();
			            	if (entriesReturned == 0)
			            		break;		            	
			            	for (Entry e : feed.getEntries()) {
			            		uTubeVideoComment commentObj = new uTubeVideoComment();
			            		commentObj.setvideo(videoData);	            	
			            		if (e.getContent() != null) {
			            			TextContent content = (TextContent) e.getContent();
			            			commentObj.setContent(content.getContent().getPlainText());
			            		}
			            		commentObj.setCreatedOn(new java.sql.Date(e.getUpdated().getValue()));
			            		List<Person> commentAuthors = e.getAuthors();	            			               
			            		Iterator<Person> itCommentAuthors = commentAuthors.iterator();	            		
			            		while (itCommentAuthors.hasNext()){
			            			Person commentAuthor = itCommentAuthors.next();	            			
			            			//Print authors of current title:
			            			commentObj.setAuthor(commentAuthor.getName());								// Name
			            			commentObj.setAuthorEmail(commentAuthor.getEmail());							// Email
			            			commentObj.setAuthorUri(commentAuthor.getUri());								// Uri
			            			commentObj.setAuthorLanguage(commentAuthor.getNameLang());					// Name Language   
			            		}
	//		            		uTubeClient.textAreaCrawler.append("\n\t CA " + commentObj.getAuthor() + "\n\t" + commentObj.getContent());
			            		videoData.comments.add(commentObj);
			            	}
			            	startIndex = startIndex + entriesReturned;
			            	if (startIndex > 975 | entriesReturned < 25 )
			            		break;		            	
			            	startIndexStr = "&start-index="+startIndex;			            	
            			}catch(com.google.gdata.util.InvalidEntryException e){
            				e.printStackTrace();
            				exceptionOccured = true;
            			}catch(com.google.gdata.util.ServiceForbiddenException serviceException){
            				serviceException.printStackTrace();
            				exceptionOccured = true;
            			}catch(Exception generalException){
            				generalException.printStackTrace();
            				exceptionOccured = true;
            			}
            			if (exceptionOccured){
            				break;
            			}
            		}            	          		 	            	
            }
            videoData.setCommentsCount(commentsCount); 								//Comments Count
            			
            videoData.setDuration(oneVideo.getMediaGroup().getDuration());			// Duration ??
            Rating gdrating = oneVideo.getRating();
            int ratersCount = 0;
            int minRate 	= 0;
            int maxRate 	= 0;
            float avgrating = 0.0F;
            if (gdrating 	!= null){
            	avgrating 	= gdrating.getAverage();
            	ratersCount = gdrating.getNumRaters();
            	minRate 	= gdrating.getMin();
            	maxRate 	= gdrating.getMax();
            }
            videoData.setRating(avgrating); 										// Average Rating
            videoData.setRatersCount(ratersCount);									// Raters Count
            videoData.setMinRate(minRate); 											// Min Rating
            videoData.setMaxRate(maxRate)	; 										// Max Rating
            
            YtRating rating = oneVideo.getYtRating(); 
            int dislikesCount = 0;
            int likesCount = 0;
            if (rating != null){
	            videoData.setDislikesCount(rating.getNumDislikes()); 				// Dislikes
	            videoData.setLikesCount(rating.getNumLikes()); 						// Likes
            }else{
            	videoData.setDislikesCount(dislikesCount); 							// Dislikes
 	            videoData.setLikesCount(likesCount); 								// Likes
            }            
            videoData.setQueryObject(query);  
            videoData.setSearchRound(SEARCH_ROUND);
            return videoData;
	}
//	private HashMap<String, uTubeQuery> evolveQueryByAuthor(String seedQueryString){
//		// Fetch Queries Map for seedQueryString
//		HashMap<String, uTubeQuery> evovledQueryObjects = queryStringsMap.get(seedQueryString);
//		
//		// Get all unique authors from videoDataMap		
//		Set<java.util.Map.Entry<String, uTubeVideoData>> dataSet = videoDataMap.entrySet();							 
//		Iterator<java.util.Map.Entry<String, uTubeVideoData>> videoDataSetIterator = dataSet.iterator();		
//		Map<String, String> authorsMap = new HashMap<String, String>();
//		
//		// Make a Map of authors of videos in videoDataMap
//		while(videoDataSetIterator.hasNext()){
//			Map.Entry<String, uTubeVideoData> entry = (Map.Entry<String, uTubeVideoData>) videoDataSetIterator.next();
//			uTubeVideoData videoData = (uTubeVideoData)entry.getValue();
//			String author  = videoData.getAuthor();
//			String authorKey = authorsMap.get(author);			 
//			if (authorKey == null) 
//				authorsMap.put(author, author);			
//		}		
//		// For each author in the Set Make a new Query object
//		Set<java.util.Map.Entry<String, String>> authorSet = authorsMap.entrySet();
//		Iterator<java.util.Map.Entry<String, String>> authorSetIterator = authorSet.iterator();
//		while(authorSetIterator.hasNext()){			
//			Map.Entry<String, String> entry = (Map.Entry<String, String>) authorSetIterator.next();
//			String author = (String)entry.getValue();			
//			// Create a newQuery object with this author						
//			try{
//				uTubeQuery newQuery = new uTubeQuery(new URL(YOUTUBE_URL));			
//				newQuery.setFullTextQuery(seedQueryString +"+"+author);
//				newQuery.setMaxResults(maxResults);			
//				newQuery.setAuthor(author);
//				newQuery.setQueryString(seedQueryString +" "+author);
//				newQuery.setSearchRound(1);
//				// Check if the evolvedQueryObjects does not already have the query 
//				uTubeQuery existingQuery  = evovledQueryObjects.get(newQuery.getQueryString());	
//				if (existingQuery == null){
//					// Add newQuery to evolvedQueryObjects			
//					evovledQueryObjects.put(newQuery.getQueryString(), newQuery);
//					harvis.textAreaCrawler.append("\n\t New Query " + newQuery.getQueryString());
//					if (harvis.textAreaCrawler.getLineCount() > 500)
//						harvis.textAreaCrawler.setText("");
//				}else{
//					harvis.textAreaCrawler.append("\n\n\t Query Checked: " + newQuery.getQueryString());
//					if (harvis.textAreaCrawler.getLineCount() > 500)
//						harvis.textAreaCrawler.setText("");
//				}
//			}catch(Exception e){				
//				e.printStackTrace();
//			}			
//		}		
//		return evovledQueryObjects;
//	}
	public boolean wordsMatchExists(String titleAndDesc){
		if (titleAndDesc.contains("ك")){
			// HarVis.seedQueryTextAreaAll replaced All ك with ک
			titleAndDesc = titleAndDesc.replace('ك', 'ک');			
		}		
		Set<String> keyWordSet = queryStringsMap.keySet();
		boolean matchExists = false;		
		try {			
			// Videos May be searched using "seedQueryWords" + "newWords" found through evolveQueryByTitles()
			// BUT, their relevance is check against only "seedQueryWords"
			Iterator<String> keyWordSetIter = keyWordSet.iterator();
			while(keyWordSetIter.hasNext()){			
					String keyWords = keyWordSetIter.next(); 
					BufferedReader mustKeyWordsReader = new BufferedReader(new StringReader(keyWords));
					BufferedReader titleAndDescReader = new BufferedReader(new StringReader(titleAndDesc));
					String titleAndDescLine = "";
					String keyWord;
					String titleAndDescWord;		
					while((keyWords = mustKeyWordsReader.readLine()) != null){			
						StringTokenizer mustKeyWordsTokenizer = new StringTokenizer(keyWords, delim); 
						while (mustKeyWordsTokenizer.hasMoreTokens()) {
							keyWord = mustKeyWordsTokenizer.nextToken().toLowerCase().trim();													
							while ((titleAndDescLine = titleAndDescReader.readLine()) != null){								
								StringTokenizer titleAndDescTokenizer = new StringTokenizer(titleAndDescLine, delim);
								while (titleAndDescTokenizer.hasMoreTokens()) {
									titleAndDescWord = titleAndDescTokenizer.nextToken().toLowerCase().trim();									
									if (keyWordsOrderFixed == false)
										matchExists = keyWord.equals(titleAndDescWord) ? true : titleAndDesc.contains(keyWord);
									else
										matchExists = keyWord.equals(titleAndDescWord);
									if (matchExists) {										
										break;			
									}									
								}// while titleAndDescTokenizer has more tokens
								if (matchExists)
									break; // Match found, stop finding in remaining words of titleAndDescLine
							} // while titleAndDescLine has a line to be processed
							if (matchExists){
								if (allKeyWordsMustMatch == false)
									break;
								else if (mustKeyWordsTokenizer.hasMoreTokens() == true){
									titleAndDescReader = new BufferedReader(new StringReader(titleAndDesc));
									matchExists = false;	// Only one word match isn't enough. Search for next keyWord Match.
								}
							}else if (mustKeyWordsTokenizer.hasMoreTokens() == true){
								if (allKeyWordsMustMatch == false)									
									titleAndDescReader = new BufferedReader(new StringReader(titleAndDesc));
								else{
									// Some of the keywords are not matching... Return
									break;
								}
							}
						}// while mustKeyWordsTokenizer has more tokens
					}// while mustKeyWordsReader has a line to be processed
					if (matchExists) break;
			}// while keyWordSetIter has more keyWords
		} catch (IOException e) {			
				e.printStackTrace();
		}
		return matchExists;
	}
	public float wordsMatchRatio(String titleAndDesc ){		
		//String text1Line = "";
		String titleAndDescLine = "";		
		String titleAndDescWord;
		boolean matchExists = false;		
		float matchCount = 0.0F;
		Set<String> keyWordSet = queryStringsMap.keySet();
		//Set<String> keyWordSet = uniqueKeyWordsMap.keySet();
		try {			
			Iterator<String> keyWordSetIter = keyWordSet.iterator();
			while(keyWordSetIter.hasNext()){			
					String keyWord = keyWordSetIter.next(); 
					BufferedReader titleAndDescReader = new BufferedReader(new StringReader(titleAndDesc));
					while ((titleAndDescLine = titleAndDescReader.readLine()) != null){
						StringTokenizer titleAndDescTokenizer = new StringTokenizer(titleAndDescLine, delim);
						while (titleAndDescTokenizer.hasMoreTokens()) {
							titleAndDescWord = titleAndDescTokenizer.nextToken().toLowerCase().trim();
							if (keyWord.length() == titleAndDescWord.length()){								
								matchExists = keyWord.equalsIgnoreCase(titleAndDescWord);
								if (matchExists) {
									matchCount ++;
									break;			
								}
							}
						}// while titleAndDescTokenizer has more tokens
						if (matchExists)
							break; // Match found, stop finding in remaining words of titleAndDescLine
					} // while titleAndDescLine has a line to be processed	
					titleAndDescReader.close();
 			}// while more keyWords
		} catch (IOException e) {			
			e.printStackTrace();
		}
		return matchCount;
	}
	
	private HashMap<String, uTubeQuery> evolveQueryByTitles(String seedQueryString){
		
		// Fetch Queries Map for seedQueryString
		HashMap<String, uTubeQuery> evovledQueryObjects = queryStringsMap.get(seedQueryString);
		// Following is intentionally commented out to get un-evolved query results
		Set<Map.Entry<String,uTubeVideoData>> dataSet = videoDataMap.entrySet();
		int videosCount = dataSet.size();
		Iterator<Map.Entry<String,uTubeVideoData>> videoDataSetIterator = dataSet.iterator();
		
		// Fetch Video Titles from the videoDataMap and build wordsMap
		int titleNumber = 0;		
		Map<String, Word> wordsMap = new HashMap<String, Word>();
		while(videoDataSetIterator.hasNext()){
			Map.Entry<String,uTubeVideoData> entry = (Map.Entry<String,uTubeVideoData>) videoDataSetIterator.next();

			uTubeVideoData videoData = (uTubeVideoData)entry.getValue();
			
			String title  = videoData.getTitle().replaceAll("[^\\u0000-\\uFFFF]",""); // Replace non-alphanumeric characters			
			String author = videoData.getAuthor();		
			String strWord;	
			Word newWord;	
			harvis.textAreaDM.setText("\t" + titleNumber++ + " Parsing Words for Title : " + title );
			StringTokenizer st = new StringTokenizer(title, delim); 
			while (st.hasMoreTokens()) {
				strWord = st.nextToken().toLowerCase();	
				if (strWord.length() > 2){
					newWord = (Word) wordsMap.get(strWord); 
					if (newWord == null) {
						newWord = new Word(strWord, 1);
						newWord.addAuthor(author);
						wordsMap.put(strWord, newWord); 
					} else {
						newWord.addAuthor(author);
						newWord.wordCount++;						
					}						
				}
			}						
		}	
		// Remove words_2b_filtered from wordsMap
		StringTokenizer st = new StringTokenizer(words_2b_filtered, ","); 
		while (st.hasMoreTokens()) {
			String strFilterWord = st.nextToken().toLowerCase();	
			Word wordObj = wordsMap.get(strFilterWord);
			if (wordObj != null){  
				wordsMap.remove(strFilterWord);				
			}
		}
		// Remove seedQueryString from wordsMap (if exists)
		Word wordObj = wordsMap.get(seedQueryString.replaceAll("\"", ""));
		if (wordObj != null){  
			wordsMap.remove(seedQueryString.replaceAll("\"", ""));			
			System.out.println(seedQueryString + " is removed.") ;
		}
		Set<Map.Entry<String, Word>> set = wordsMap.entrySet(); 
		Iterator<Map.Entry<String, Word>> iter = set.iterator();
		int N = 10;
		if (N > videosCount)
			N = videosCount;
		Word topNWords[] = new Word[N];
		for (int i = 0; i < N; ){
			set = wordsMap.entrySet(); 
			iter = set.iterator();
			int maxAuthorCount = 0;
			while (iter.hasNext()) {
				Map.Entry<String, Word> entry = (Map.Entry<String, Word>) iter.next();				
				Word word = (Word) entry.getValue();
				if(word.authorCount > maxAuthorCount)
					maxAuthorCount = word.authorCount;
			}
			iter = set.iterator();
			while (iter.hasNext()) {
				Map.Entry<String, Word> entry = (Map.Entry<String, Word>) iter.next();				
				Word word = (Word) entry.getValue();
				if (word.authorCount == maxAuthorCount){		
					// Check if the word is already in the query string (e.g. Arabic music Arabic)
					Set<String> existingQuryStringsSet = evovledQueryObjects.keySet();
					Iterator<String> existingQueryStringsSetIter = existingQuryStringsSet.iterator();
					boolean wordContainedInQuery = false;
					while(existingQueryStringsSetIter.hasNext()){
						String existingQueryWord = existingQueryStringsSetIter.next();
						if (existingQueryWord.contains(word.word)){
							wordContainedInQuery = true;
							break;
						}
					}
					if (wordContainedInQuery == false){
						// Check if the word is not already selected as a search keyword
						if (evovledQueryObjects.get("\"" + seedQueryString.replaceAll("\"", "") + " " + word.word + "\"") == null)		{
							// Add the word to the list of uniqueKeyWords
							if (uniqueKeyWordsMap.get(word.word) == null)
								uniqueKeyWordsMap.put(word.word, word.word);
							// Include the word in topNWordsList
							topNWords[i] = word;	
							// Find the next topNWord
							i++;
						}
					}
					// Remove the word from wordsMap to find next best word
					iter.remove();
					break;
				}
			}
		}
		set = wordsMap.entrySet(); 
		iter = set.iterator();
		harvis.textAreaCrawler.setText("\t New Query Words" );
		for (int i = 0; i < N; i++){						
			try{
				harvis.textAreaCrawler.append("\n\t Word " + topNWords[i].word + 
												" Frequency "  + topNWords[i].wordCount + 
												" Authors " + topNWords[i].authorCount);
				uTubeQuery newQuery = new uTubeQuery(new URL(YOUTUBE_URL));
				// newQueryString = "querystring" newword
//				boolean isSeedQueryStringArabic = seedQueryString.replaceAll("\"", "").matches("[ ء-ي]+"); 
//				boolean isNewWordArabic = topNWords[i].word.matches("[ ء-ي]+");
//				if (relaxSelectionCriteria == false){					
//					if (isSeedQueryStringArabic && isNewWordArabic){					
//						newQuery.setFullTextQuery(seedQueryString.replaceAll("\"", "") + "\"" + " " + topNWords[i].word +"\"");					
//						newQuery.setQueryString(seedQueryString.replaceAll("\"", "") + "\"" + " " + topNWords[i].word +"\"");					
//					}else{
//						newQuery.setFullTextQuery("\"" + seedQueryString.replaceAll("\"", "") + "\"" + " " + topNWords[i].word);					
//						newQuery.setQueryString("\"" + seedQueryString.replaceAll("\"", "") + "\"" + " " + topNWords[i].word);
//					}
//				}else{
					newQuery.setFullTextQuery(seedQueryString.replaceAll("\"", "") + " " + topNWords[i].word );					
					newQuery.setQueryString(seedQueryString.replaceAll("\"", "") + " " + topNWords[i].word );
//				}
				System.out.println("New Query " + newQuery.getQueryString() );
				newQuery.setMaxResults(maxResults);
				newQuery.setSearchRound(1);
				// Check if the evolvedQueryObjects does not already have the query 
				uTubeQuery existingQuery  = evovledQueryObjects.get(newQuery.getQueryString());					
				if (existingQuery == null){
					// Add newQuery to evolvedQueryObjects			
					evovledQueryObjects.put(newQuery.getQueryString(), newQuery);
				}
				}catch(Exception e){
					e.printStackTrace();
				}			
		}		
		return evovledQueryObjects;
	}
	private int fetchRelatedVideos(String seedQueryString){
		Set<Map.Entry<String, uTubeVideoData>> dataSet = videoDataMap.entrySet();			 
		Iterator<Map.Entry<String,uTubeVideoData>> iterator = dataSet.iterator();	
		int videosReturnedByService = 0;
		while(iterator.hasNext()){
			Map.Entry<String, uTubeVideoData> entry = (Map.Entry<String,uTubeVideoData>) iterator.next();			
			uTubeVideoData videoData = (uTubeVideoData)entry.getValue();
			if ((videoData.getRelatedVideosFetched() == false) && (videoData.getRelatedVideosFeedUrl() != null)) {
				uTubeQuery relatedVideosQuery = null;
				try {					
					relatedVideosQuery = new uTubeQuery(new URL(videoData.getRelatedVideosFeedUrl()));
					relatedVideosQuery.setMaxResults(maxResults);
					relatedVideosQuery.setStartIndex(1); // Related Videos Feed Is not affected by changing startIndex		
					
				} catch (MalformedURLException e) {					
					e.printStackTrace();
					continue;
				}
				VideoFeed relatedVideosFeed = null;
				try{
					relatedVideosFeed  = service.getFeed(relatedVideosQuery, VideoFeed.class);	
					SERVICE_EXECUTION_COUNT++;
				}catch(com.google.gdata.util.ServiceException serviceException){
					harvis.textAreaCrawler.append("\n\n Service Exception fetching related videos : " + serviceException.getMessage() +"\n");
					serviceException.printStackTrace();
					continue;
				} catch (IOException e) {
					harvis.textAreaCrawler.append("\n\n IO Exception fetching related videos : " + e.getMessage() +"\n");
					e.printStackTrace();
					continue;
				}
				for(VideoEntry relatedVideoEntry : relatedVideosFeed.getEntries() ) {
					videosReturnedByService++;
					String title = relatedVideoEntry.getTitle().getPlainText().toLowerCase();
					String description = relatedVideoEntry.getMediaGroup().getDescription().getPlainTextContent().toLowerCase();										
					boolean wordIsContainedInTitleOrDesc = false;
					float matchRatio = wordsMatchRatio((title +" "+ description).toLowerCase());
					
					if (matchRatio >= .65)
						wordIsContainedInTitleOrDesc = true; //(title + "\n" + description).toLowerCase().contains(keywords);
					else 
						wordIsContainedInTitleOrDesc = false;
					if (wordIsContainedInTitleOrDesc){
						String relatedVideoID = relatedVideoEntry.getId().substring(27).trim();
						if (videoExistsInCollection(relatedVideoID) == false){
							uTubeVideoData relatedVideoData = captureVideoData(relatedVideoEntry, relatedVideosQuery);
							harvis.textAreaCrawler.append("\n\n\t Capturing video # " + getVideoCount() + " = " + relatedVideoData.getTitle() + "\n");												
							addVideoData2Collection(relatedVideoData);
						}else{
							harvis.textAreaCrawler.append("\n\t Already captured... Ignoring video --> " +  title);
						}
					}else{
						harvis.textAreaCrawler.append("\n\t Weak Relevance, Ignoring video --> " +  title);								
					}			
				} // End For Loop
			}
		}// End While	
		return videosReturnedByService;
	}
	private int downloadRelatedVideos(VideoEntry videoEntry){
			int videosReturnedByService = 0;
			uTubeQuery relatedVideosQuery = null;
			try {					
					relatedVideosQuery = new uTubeQuery(new URL(videoEntry.getRelatedVideosLink().getHref()));
					relatedVideosQuery.setMaxResults(maxResults);
					relatedVideosQuery.setStartIndex(1); // Related Videos Feed Is not affected by changing startIndex					
				} catch (MalformedURLException e) {					
					e.printStackTrace();
					return 0;
				}
				VideoFeed relatedVideosFeed = null;
				try{
					relatedVideosFeed  = service.getFeed(relatedVideosQuery, VideoFeed.class);	
					SERVICE_EXECUTION_COUNT++;
				}catch(com.google.gdata.util.ServiceException serviceException){
					harvis.textAreaCrawler.append("\n\n Service Exception fetching related videos : " + serviceException.getMessage() +"\n");
					serviceException.printStackTrace();
					return 0;
				} catch (IOException e) {
					harvis.textAreaCrawler.append("\n\n IO Exception fetching related videos : " + e.getMessage() +"\n");
					e.printStackTrace();
					return 0;
				}
				for(VideoEntry relatedVideoEntry : relatedVideosFeed.getEntries() ) {
					videosReturnedByService++;
					String title = relatedVideoEntry.getTitle().getPlainText().toLowerCase();
					String description = relatedVideoEntry.getMediaGroup().getDescription().getPlainTextContent().toLowerCase();					
					uTubeVideoData relatedVideoData = captureVideoData(relatedVideoEntry, relatedVideosQuery);
					harvis.textAreaCrawler.append("\n\n\t Capturing video # " + getVideoCount() + " = " + relatedVideoData.getTitle() + "\n");
					addVideoData2Collection(relatedVideoData);								
				} // End For Loop						
		return videosReturnedByService;
	}
	private void videoEntryHandler(VideoEntry videoEntry, uTubeQuery query){		
		String title 					= videoEntry.getTitle().getPlainText().toLowerCase();							
		String description 				= videoEntry.getMediaGroup().getDescription().getPlainTextContent().toLowerCase();
		String videoID = videoEntry.getId().substring(27).trim();
		if (mode == uTubeCrawler.VIDEOS_BY_KEYWORD ){
			boolean wordIsContainedInTitleOrDesc = true; //wordsMatchExists((title +" "+ description).toLowerCase());																				
			if (wordIsContainedInTitleOrDesc){									
				if (videoExistsInCollection(videoID) == false){
					uTubeVideoData videoData = captureVideoData(videoEntry, query);
					harvis.textAreaCrawler.append("\n\n\t Capturing video # " + getVideoCount() + " = " + videoData.getTitle() +"\n");
					addVideoData2Collection(videoData);
				}else {
					harvis.textAreaCrawler.append("\n\n\t Video Already Captured. " + title);
				}
			}else{
				harvis.textAreaCrawler.append("\n\n\t Weak Relevance. Ignoring video " + videoEntry.getTitle().getPlainText() +"\n");
			}
		}
		if (mode == uTubeCrawler.VIDEOS_BY_AUTHOR | mode == uTubeCrawler.VIDEOS_BY_VIDEO_ID){
			if (videoExistsInCollection(videoID) == false){
				uTubeVideoData videoData = captureVideoData(videoEntry, query);
				harvis.textAreaCrawler.append("\n\n\t Capturing video # " + getVideoCount() + " = " + videoData.getTitle() +"\n");
				addVideoData2Collection(videoData);
			}else {
				harvis.textAreaCrawler.append("\n\n\t Video Already Captured. " + title);
			}
		}
	}
	private int executeAllQueries ( HashMap<String, uTubeQuery> evovledQueryObjects ){
		Set<java.util.Map.Entry<String, uTubeQuery>> querySet = evovledQueryObjects.entrySet();
		Iterator<java.util.Map.Entry<String, uTubeQuery>> querySetIterator = querySet.iterator();
		ArrayList<uTubeQuery> querySummary = new ArrayList<uTubeQuery>();
		int videoCount = getVideoCount();
		int queryCount = 0;
		int videosReturnedByService = 1;
		for ( ; querySetIterator.hasNext() ; ){			
			Map.Entry<String, uTubeQuery> queryEntry = (Map.Entry<String, uTubeQuery>) querySetIterator.next();
			uTubeQuery query = queryEntry.getValue();
			//System.out.println("Full URL " + query.getFullUrl());
			queryCount++;	       
			if( query.isChecked() == false){				
					int startIndex = 1;					
					while(true){
						query.setStartIndex(startIndex);
						if (startIndex >= 950)
							query.setMaxResults(999-startIndex);
						harvis.textAreaCrawler.append("\n\n New Query String: " + query.getQueryString() + "\t Search Round: " + SEARCH_ROUND);
						harvis.textAreaCrawler.append("\n Invoking service # " +SERVICE_EXECUTION_COUNT+" for Query # "+queryCount+" / " + evovledQueryObjects.size() + ".");						
						harvis.textAreaCrawler.append("\n Query URL :" + query.getFullUrl() + "\n");
						VideoFeed videoFeed = null;
						
						try{
							Thread.sleep(1000);
							if (mode == uTubeCrawler.VIDEOS_BY_KEYWORD)
								videoFeed = service.getFeed(query, VideoFeed.class);
							if (mode == uTubeCrawler.VIDEOS_BY_AUTHOR)								
								videoFeed = service.getFeed(new URL(query.getFullUrl()), VideoFeed.class);							
							SERVICE_EXECUTION_COUNT++;														
						}catch(com.google.gdata.util.ServiceException e){
							harvis.textAreaCrawler.append("\n\n Exception fetching videos : " + e.getMessage() +"\n");
							startIndex++;
							break;
						} catch (InterruptedException e) {						
							harvis.textAreaCrawler.append("\n\n Exception sleeping : " + e.getMessage() +"\n");
							startIndex++;
							break;
						} catch (MalformedURLException e) {
							harvis.textAreaCrawler.append("\n\n MalformedURLException Exception fetching videos : " + e.getMessage() +"\n");
							startIndex++;
							break;
						} catch (IOException e) {
							harvis.textAreaCrawler.append("\n\n IO Exception fetching videos : " + e.getMessage() +"\n");
							startIndex++;
							break;
						}						
						int videoFeedIndex = 0;						
						for(VideoEntry videoEntry : videoFeed.getEntries() ) {
							videoFeedIndex++;
							videosReturnedByService++;
							videoEntryHandler(videoEntry, query);
						} // FOR ENDS HERE
						// Sleep for a while
						double d = Math.random()*20*1000;
						int sleepTime = (int)d;
						try {
							Thread.sleep(sleepTime);
						} catch (InterruptedException e) {							
							e.printStackTrace();
						}						
						if (getVideoCount() > videoCount){	
							harvis.campaignManager.threadManager.setVideoData(videoDataMap);
							videoCount = getVideoCount();							
							harvis.textAreaCrawler.setText("\n ***** Setting batch of " + videoCount + " videos data");
						}else{
							harvis.textAreaCrawler.append("\n no results returned from this startIndex; Don't check next number and move on...");
							break; // no results returned from this startIndex; Don't check next number and move on...
						}
						startIndex += videoFeedIndex;
						harvis.textAreaCrawler.setText("\n Crawling through " + startIndex +" index of current Query");
						if (startIndex > 999)
							break;			
						if ( videoFeedIndex == 0)
							break;					
						}// WHILE ENDS HERE
					harvis.textAreaCrawler.append("\n Total "+ videosReturnedByService + " videos returned by service \n");
					query.setChecked();
					querySummary.add(query);
					harvis.textAreaCrawler.setText("\n Query String : " + query.getQueryString() );
					harvis.textAreaCrawler.append("\n Query # "+queryCount+" / " + evovledQueryObjects.size() + " finished... moving  to next.");
			}		
			else{
				harvis.textAreaCrawler.setText("\n Query String : " + query.getQueryString() );
				harvis.textAreaCrawler.append("\n Query # "+queryCount+" / " + evovledQueryObjects.size() + " is checked ... moving  to next.");
			}				
		}
		harvis.textAreaCrawler.setText("\n Finished All Queries for Search Round: " + SEARCH_ROUND );
		return videosReturnedByService;
	}
	private ArrayList<uTubeVideoData> downloadAuthorVideos(String author){
		ArrayList<uTubeVideoData> authorVideosList = new ArrayList<uTubeVideoData>();
		uTubeQuery newQuery;		
		try{
			newQuery = new uTubeQuery(new URL(YOUTUBE_URL));
			newQuery.setFullTextQuery(author);					
			newQuery.setQueryString(author);
			URL queryURL = new URL("http://gdata.youtube.com/feeds/api/users/"+author+"/uploads");
			VideoFeed videoFeed = service.getFeed(queryURL, VideoFeed.class);
			for(VideoEntry oneVideo : videoFeed.getEntries() ) {
				uTubeVideoData videoData = captureVideoData(oneVideo, newQuery);
				authorVideosList.add(videoData);				
			}														
		}catch(com.google.gdata.util.ServiceException e){
			harvis.textAreaCrawler.append("\n\n Exception fetching videos : " + e.getMessage() +"\n");			
		} catch (IOException e) {
			harvis.textAreaCrawler.append("\n\n Exception fetching videos : " + e.getMessage() +"\n");
		}
		return authorVideosList;
	}
	private void downloadVideosByKeyWords(){
		// Load Filter Words
		loadFilterWords();

		// build uniqueKeyWordsMap (to be used in wordsMatchRatio())		
		Set<String> queryStringsMapKeySet = queryStringsMap.keySet();								 
		Iterator<String> queryStringsMapKeySetSetIterator = queryStringsMapKeySet.iterator();
		String allQueryStrings = "";
		while(queryStringsMapKeySetSetIterator.hasNext()){			
			HashMap<String, uTubeQuery> queryObjects = queryStringsMap.get(queryStringsMapKeySetSetIterator.next());
			Set<String> queryStringsKeySet = queryObjects.keySet();
			Iterator<String> queryStringsKeySetIterator = queryStringsKeySet.iterator();
			while (queryStringsKeySetIterator.hasNext()){
				allQueryStrings += queryStringsKeySetIterator.next().replace("\"", "") + " ";
			}	
		}

		StringReader keyWordsStringReader = new StringReader(allQueryStrings);
		BufferedReader keyWordsReader = new BufferedReader(keyWordsStringReader);
		uniqueKeyWordsMap = new HashMap<String, String>();
		try {
			String keyWordsLine = "";			
			while((keyWordsLine = keyWordsReader.readLine()) != null){
				StringTokenizer mustKeyWordsTokenizer = new StringTokenizer(keyWordsLine, " " /*The space delimiter*/); 
				while (mustKeyWordsTokenizer.hasMoreTokens()) {
					String word = mustKeyWordsTokenizer.nextToken();
					if (uniqueKeyWordsMap.get(word) == null){						
						uniqueKeyWordsMap.put(word, word);						
					}					
				}				
			}
			keyWordsStringReader.close();
			keyWordsReader.close();
		} catch (IOException e1) {			
			e1.printStackTrace();
		}

		HashMap<String, uTubeQuery> evovledQueryObjects = null;		

		int videosCountPrev = getVideoCount();

		Set<java.util.Map.Entry<String, HashMap<String, uTubeQuery>>> queryStringsEntrySet = queryStringsMap.entrySet();							 
		Iterator<java.util.Map.Entry<String, HashMap<String, uTubeQuery>>> queryStringsEntrySetIterator = queryStringsEntrySet.iterator();	
		int newVideosInThisRound = 0;
		while (queryStringsEntrySetIterator.hasNext()) {
			Map.Entry<String, HashMap<String, uTubeQuery>> entry = (Map.Entry<String, HashMap<String, uTubeQuery>>) queryStringsEntrySetIterator.next();
			String seedQueryString = entry.getKey();
			HashMap<String, uTubeQuery> existingQueriesMap  = entry.getValue();
			SEARCH_ROUND = existingQueriesMap.get(seedQueryString).getSearchRound();			
			do{	
				evovledQueryObjects = evolveQueryByTitles(seedQueryString);
				evovledQueryObjects.get(seedQueryString).setSearchRound(SEARCH_ROUND);

				Calendar startTime = Calendar.getInstance();
				int videosReturnedByService = executeAllQueries(evovledQueryObjects);
				// videosReturnedByService += fetchRelatedVideos(seedQueryString);
				Calendar endTime = Calendar.getInstance();			

				long timeInMillis = endTime.getTimeInMillis() - startTime.getTimeInMillis();
				int timeInMinutes = (int)((timeInMillis/1000)/60);

				int videosCountAfter = getVideoCount();					
				harvis.campaignManager.uTubeDM.insertSearchRoundRecordIntoDB(
						seedQueryString, 						//SeedQueryString
						SEARCH_ROUND, 							// SearchRound
						timeInMinutes, 							// TimeInMinutes
						(videosCountAfter - videosCountPrev), 	// VideosCount
						SERVICE_EXECUTION_COUNT,				// ServiceExecutionCount
						videosReturnedByService);				// VideosReturnedByService
				SERVICE_EXECUTION_COUNT = 0;				
				newVideosInThisRound = videosCountAfter - videosCountPrev;
				videosCountPrev = videosCountAfter;				
			}while(SEARCH_ROUND++ < 3 && newVideosInThisRound > 10);						
		}
		int vidCount = getVideoCount();
		harvis.textAreaCrawler.append("\n\n\t uTubeCrawler: Crawler has found " + vidCount + " videos...");				
		if (vidCount >= 1){
			harvis.campaignManager.threadManager.setVideoData(videoDataMap);
		}
		harvis.textAreaCrawler.append("\n\n\t uTubeCrawler: *%*%*%*%*%*% FINISHED CRAWLING *%*%*%*%*%*%*%*");
		harvis.textAreaCrawler.append("\n\n\t uTubeCrawler: *%*%*%*%*%*% EXTRACTED VIDEOS COUNT = "+ getVideoCount() +"*%*%*%*%*%*%*%*");
		harvis.textAreaCrawler.append("\n\n\t uTubeCrawler: *%*%*%*%*%*% Queries LIST *%*%*%*%*%*%*%*");
		Set<java.util.Map.Entry<String, uTubeQuery>> querySet = evovledQueryObjects.entrySet();			
		Iterator<java.util.Map.Entry<String, uTubeQuery>> querySetIterator = querySet.iterator();
		for ( ; querySetIterator.hasNext(); ){
			Map.Entry<String, uTubeQuery> queryEntry = (Map.Entry<String, uTubeQuery>) querySetIterator.next();
			uTubeQuery query = queryEntry.getValue();
			harvis.textAreaCrawler.append("\n\n\t\t Feed URL: "+ query.getFullUrl());
		}		
	}	
	public void run(){
		if (mode == uTubeCrawler.VIDEOS_BY_KEYWORD)
			downloadVideosByKeyWords();
		if (mode == uTubeCrawler.VIDEOS_BY_AUTHOR){
			Iterator<String> iter = queryStringsMap.keySet().iterator();
			if (iter.hasNext()){
				HashMap<String, uTubeQuery> queryObjectMap = queryStringsMap.get(iter.next());
				executeAllQueries(queryObjectMap);				
			}	
		}		
		if(mode == uTubeCrawler.VIDEOS_BY_VIDEO_ID){
			Iterator<String> iter = queryStringsMap.keySet().iterator();
			if (iter.hasNext()){
				HashMap<String, uTubeQuery> queryObjectMap = queryStringsMap.get(iter.next());
				Set<java.util.Map.Entry<String, uTubeQuery>> querySet = queryObjectMap.entrySet();
				Iterator<java.util.Map.Entry<String, uTubeQuery>> querySetIterator = querySet.iterator();				
				for ( ; querySetIterator.hasNext() ; ){			
					Map.Entry<String, uTubeQuery> queryEntry = (Map.Entry<String, uTubeQuery>) querySetIterator.next();
					uTubeQuery query = queryEntry.getValue();
					downloadVideo(query);
				}								
			}
		}
	}
	private void downloadVideo(uTubeQuery query){	
		try{
			VideoEntry videoEntry = service.getEntry(new URL(query.getFullUrl()), VideoEntry.class);
			videoEntryHandler(videoEntry, query);
			if (query.isRelatedVideosNeeded())
				downloadRelatedVideos(videoEntry);
		}catch(IOException e){
			e.printStackTrace();
			harvis.textAreaCrawler.setText("\n " + e.getMessage());
			return;
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		harvis.campaignManager.threadManager.setVideoData(videoDataMap);
		videoCount = getVideoCount();							
		harvis.textAreaCrawler.setText("\n ***** Setting batch of " + videoCount + " videos data");
	}
}
class uTubeFeedUrl{
	private String seedString = "";
	private int startIndex = 1;
	private String category = null;
	private int maxResults = 50;
	private int version = 2;
	private String feedUrl;
	private String googleUrl = "http://gdata.youtube.com/feeds/api/videos?";
	
	public uTubeFeedUrl(String seedString, String Category, int startIndex, int maxResults, int version){
		feedUrl = googleUrl;
		feedUrl += "q="+ seedString;
		feedUrl += "&start-index="+ startIndex;
		feedUrl += "&search_sort=video_view_count";
		if (category != null)
			feedUrl += "&category="+ category; //example News%7CPolitics"
		if (maxResults > 0 ) 
				feedUrl += "&max-results=" + maxResults;
		feedUrl += "&v=2";
	}	
	public uTubeFeedUrl(){
		feedUrl = "http://gdata.youtube.com/feeds/api/videos?";
	}
	void getFeedUrl(){
		 
	}
	void resetCategory(String category){
		
	}
}
