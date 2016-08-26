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
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

import yt.D2I_GUI;
import yt.HarVis;
import yt.har.FPTree.Node;



public class uTubeDataManager  implements Runnable{
	// Database parameters (Required for getDBConnection())
	private static String dbUser = null;
	private static String dbPassword = null;
	private static String dbURL = null;
	private static String dbSchema = null;
	public static final int STORE_MODE = 0;
	public static final int UoD_MODE = 1;
	public static final int FILTER_MODE = 2;
	private int mode;	
	private boolean keepRunning;
	private boolean dataSaved;	
	//protected uTubeCampaignManager campaignManager;
	protected static HarVis harvis;
	//protected D2I_GUI d2iGUI;
	//private uTubeClientThreadsManager threadManager;
	static int mainDataCursor;
	//private String Mode;
	HashMap<String, uTubeVideoData> videoDataMap;
	// JDBC Variables
	private Connection dbConnection = null;
	// UoD Parameters
	UoD_Param uod_param;
//	private int wordCountLimit;
//	private int coWordCountLimit;
//	private int coOccurCountLimit;
//	private String srcTable;
//	private String destTable;
	//================================================================================
    // Constructors
    //================================================================================
	// When a DataManager is required for Storing, Filtering, UoD
	public uTubeDataManager(HarVis harvisObject, int mode) 
	{
		harvis = harvisObject;
		keepRunning = true;
		mainDataCursor = 1;	
		this.mode = mode;
	}
	// When a DataManager is required for UoD Discovery
	public uTubeDataManager(UoD_Param uod_param) 
	{		
		keepRunning = true;
		mainDataCursor = 1;	
		this.mode = UoD_MODE;
		setUoD_Param(uod_param);
	}
	// When a DataManager is required for fetching data for visualization
	public uTubeDataManager(HarVis harvisObject) 
	{
		harvis = harvisObject;
		keepRunning = true;
		mainDataCursor = 1;		
	}
	// When a DataManager is required for filtering videos
	public uTubeDataManager(){}
	public void setUoD_Param(UoD_Param uod_param){
		this.uod_param = uod_param;				
	}
	public void stopRunning(){
		keepRunning = false;
	}

	boolean isDataSaved(){
		return dataSaved;
	}
	
	public void setVideoDataMap(HashMap<String, uTubeVideoData> videoDataMap){
		Set<Entry<String, uTubeVideoData>> dataSet = videoDataMap.entrySet();		
		harvis.textAreaDM.setText("\n\t uTubeDM: Received new batch of " + dataSet.size() +  " videos. \n");
		this.videoDataMap = videoDataMap;
	}
	
	private void storeDataProcess(){
		uTubeVideoData videoData = new uTubeVideoData();
		int localDataCursor = 1;
		Iterator iterator;
		Set dataSet;
		
		while(keepRunning){
			boolean ex = false;
			try{				
				dataSaved = false;
				videoDataMap = harvis.campaignManager.threadManager.getDataReceivedFromCrawler();				
				harvis.textAreaDM.append("\n\t uTubeDM: Storing new batch \n");
				dataSet = videoDataMap.entrySet();			 
				iterator = dataSet.iterator();
				dbConnection = getDBConnection();											
				while(iterator.hasNext()){
					Map.Entry entry = (Map.Entry) iterator.next();
					String ID = (String)entry.getKey();
					videoData = (uTubeVideoData)entry.getValue();
					if ( videoData.isSaved() )
						continue;					
					harvis.textAreaDM.append("\n uTubeDM: Saving data for " + videoData.getTitle());
					//System.out.println(localDataCursor + " " + videoData.getVideoID() + " saving");
					insertVideoRecordIntoDB(videoData);					
					//System.out.println(localDataCursor++ + " " + videoData.getVideoID() + " saved.");
					if (harvis.textAreaDM.getLineCount() > 500)
						harvis.textAreaDM.setText("");
				}			
			}catch(java.util.ConcurrentModificationException concurExc){
				harvis.textAreaDM.append("\n\t uTubeDM: Exception Concur ");
				//concurExc.printStackTrace();
				ex = true;
			}
			catch(Exception e){
				e.printStackTrace();
				harvis.textAreaDM.append("\n\t " + e.getMessage());
				harvis.textAreaDM.append("\n\t uTubeDM: Exception e Video ID " + videoData.getVideoID());
				harvis.textAreaDM.append("\n\t uTubeDM: Exception Video Title " + videoData.getTitle());
				//e.printStackTrace();
				ex = true;
			}	
			if (dbConnection != null) {
				try {
					dbConnection.close();					
					//uTubeClient.textArea.append("\n\t uTubeDM: Connection Closed.\n");
				}catch (SQLException e) {
					e.printStackTrace();
				}
			}	
			if (ex == false){
				dataSaved = true;
				harvis.campaignManager.threadManager.dataSavedSuccessfull();				
			}
		}
	}
	public void run(){
		switch(mode){
			case uTubeDataManager.STORE_MODE:
				storeDataProcess(); break;
			case uTubeDataManager.UoD_MODE:
				discoverUoD_Words_FPTree(); break;
			case uTubeDataManager.FILTER_MODE:
				filterVideos();
			default:
				storeDataProcess(); break;
		}		
	}
	
	public void filterVideos(String a){
		try{
			HashMap<String, String> queryStrings = new HashMap<String, String>();			
			queryStrings = reloadQueryStrings();			
			HashMap<String, uTubeVideoData> existingVideoData = reLoadVideoDataLite();	
			Set<java.util.Map.Entry<String, uTubeVideoData>> videoSet = existingVideoData.entrySet();
			Iterator<java.util.Map.Entry<String, uTubeVideoData>> videoSetIterator = videoSet.iterator();
			int filterCount = 0;
			int relevantCount = 0;
			while(videoSetIterator.hasNext()){
				Map.Entry<String, uTubeVideoData> videoEntry = (Map.Entry<String, uTubeVideoData>) videoSetIterator.next();
				uTubeVideoData videoData = videoEntry.getValue();
				String titleAndDesc = videoData.getTitle() + "\n" + videoData.getDescription();
				harvis.textAreaDM.setText(" Finding Relevance for \n " + videoData.getTitle() + "\n Using Keywords ");
				Set<String> queryStringsKeySet = queryStrings.keySet();
				Iterator<String> queryStringsKeySetIterator = queryStringsKeySet.iterator();
				boolean matchExists = false;
				while (queryStringsKeySetIterator.hasNext()){
					String queryString = (String) queryStringsKeySetIterator.next();	
					harvis.textAreaDM.append("\n " + queryString);
					matchExists = wordsMatchExists(queryString, titleAndDesc);		
					if(matchExists)
						break;
				}					
				if (matchExists == false){
					filterCount++;
					harvis.textAreaDM.append(" NOT RELEVENT \n " );
				}else
					relevantCount++;
			}
			harvis.textAreaDM.append(" Total Videos  " + existingVideoData.size() );
			harvis.textAreaDM.append(" Irrelevant Videos  " + filterCount );
			harvis.textAreaDM.append(" Relevant Videos  " + relevantCount );
			
		}catch(Exception e){
			e.printStackTrace();		 
			harvis.textAreaDM.append("\n Grrrr " + e.getMessage());
			harvis.textAreaClient.append("\n Grrrr " + e.getMessage());			 
		}	
	}
	public void filterVideos(){
		try{
			HashMap<String, String> queryStrings = new HashMap<String, String>();			
			queryStrings = reloadQueryStrings();			
			HashMap<String, uTubeVideoData> existingVideoData = reLoadVideoDataLite();	
			Set<java.util.Map.Entry<String, uTubeVideoData>> videoSet = existingVideoData.entrySet();
			Iterator<java.util.Map.Entry<String, uTubeVideoData>> videoSetIterator = videoSet.iterator();
			int filterCount = 0;
			int relevantCount = 0;
			while(videoSetIterator.hasNext()){
				Map.Entry<String, uTubeVideoData> videoEntry = (Map.Entry<String, uTubeVideoData>) videoSetIterator.next();
				uTubeVideoData videoData = videoEntry.getValue();
				String titleAndDesc = videoData.getTitle() + "\n" + videoData.getDescription();
				harvis.textAreaDM.setText(" Finding Relevance for \n " + videoData.getTitle() + "\n Using Keywords ");
				Set<String> queryStringsKeySet = queryStrings.keySet();
				Iterator<String> queryStringsKeySetIterator = queryStringsKeySet.iterator();
				boolean matchExists = false;
				while (queryStringsKeySetIterator.hasNext()){
					String queryString = (String) queryStringsKeySetIterator.next();	
					harvis.textAreaDM.append("\n " + queryString);
					matchExists = wordsMatchExists(queryString, titleAndDesc);		
					if(matchExists)
						break;
				}					
				if (matchExists == false){
					filterCount++;
					harvis.textAreaDM.append(" NOT RELEVENT \n " );
				}else
					relevantCount++;
			}
			harvis.textAreaDM.append(" Total Videos  " + existingVideoData.size() );
			harvis.textAreaDM.append(" Irrelevant Videos  " + filterCount );
			harvis.textAreaDM.append(" Relevant Videos  " + relevantCount );
			
		}catch(Exception e){
			e.printStackTrace();		 
			harvis.textAreaDM.append("\n Grrrr " + e.getMessage());
			harvis.textAreaClient.append("\n Grrrr " + e.getMessage());			 
		}	
	}
//	public static Connection getDBConnection(String dbUser, String dbUsrPwd, String dbServerUrl) throws SQLException, ClassNotFoundException{
//		Connection conn = null;
//    	Class.forName("com.mysql.jdbc.Driver");    		
//	   	Properties connectionProps = new Properties();
//	    connectionProps.put("user", dbUser);
//	    connectionProps.put("password", dbUsrPwd);
//	    //String connString = "jdbc:mysql://"+dbServerUrl+"/?user="+dbUser+"&password="+dbUsrPwd;
//	    String connString = "jdbc:mysql://"+dbServerUrl+"/?useUnicode=true&amp;characterEncoding=UTF-8";
//	    conn = DriverManager.getConnection(connString, connectionProps);
//    return conn;
//	}
	public static Connection getDBConnection() throws SQLException, ClassNotFoundException {
	    	Connection conn = null;
	    	Class.forName("com.mysql.jdbc.Driver");    		
		   	Properties connectionProps = new Properties();
		    connectionProps.put("user", dbUser);
		    connectionProps.put("password", dbPassword);	    
		    String connString = "";
		    if(dbSchema.equals(""))
		    	connString = "jdbc:mysql://"+dbURL+"/?useUnicode=true&amp;characterEncoding=UTF-8";
		    else
		    	connString = "jdbc:mysql://"+dbURL+"/"+dbSchema+"?useUnicode=true&amp;characterEncoding=UTF-8";
		    conn = DriverManager.getConnection(connString, connectionProps);
	    return conn;
	}
	public void reLoadVideoDataFull(){
		videoDataMap = new HashMap<String, uTubeVideoData>();
		try {
			Connection conn = getDBConnection();
			Statement selectVideoDataQuery = conn.createStatement();
			ResultSet selectVideoDataQueryResultSet = selectVideoDataQuery.executeQuery("Select * from uTubeVideos");
			int videoCount = 0;
			while(selectVideoDataQueryResultSet.next()){
	        	uTubeVideoData videoData = new uTubeVideoData();
				videoData.setAuthor(selectVideoDataQueryResultSet.getString("Author")); 			// Author
				videoData.setCategory(selectVideoDataQueryResultSet.getString("Category"));			// Category
	        	videoData.setCommentsCount(selectVideoDataQueryResultSet.getInt("CommentsCount")); 	// CommentsCount
	        	videoData.setCreatedOn(selectVideoDataQueryResultSet.getDate("CreatedOn"));			// CreatedOn	
	        	videoData.setDescription(selectVideoDataQueryResultSet.getString("Description"));	// Description
	        	videoData.setDislikesCount(selectVideoDataQueryResultSet.getInt("Dislikes"));		// Dislikes
	        	videoData.setDuration(selectVideoDataQueryResultSet.getFloat("Duration"));			// Duration 
	        	videoData.setFavoriteCount(selectVideoDataQueryResultSet.getInt("FavoritesCount"));	// FavoritesCount	        	
	        	videoData.setLikesCount(selectVideoDataQueryResultSet.getInt("Likes"));				// LikesCount
	        	videoData.setLocation(selectVideoDataQueryResultSet.getString("Location"));			// Location
	        	URL url = new URL(selectVideoDataQueryResultSet.getString("QueryUrl"));				// QueryUrl	        	
	        	uTubeQuery queryObj = new uTubeQuery(url);
	        	queryObj.setQueryString(selectVideoDataQueryResultSet.getString("QueryString"));	// QueryString
	        	videoData.setQueryObject(queryObj);
	        	videoData.setRating(selectVideoDataQueryResultSet.getFloat("Rating"));				// Rating
	        	videoData.setTitle(selectVideoDataQueryResultSet.getString("Title"));				// Title
	        	videoData.setVideoID(selectVideoDataQueryResultSet.getString("ID"));				// VideoID
	        	videoData.setViewCount(selectVideoDataQueryResultSet.getInt("ViewsCount"));			// ViewsCount
	        	
	        	videoData.setSaved();
	        	videoData.comments = loadVideoCommentsData(videoData);
	        	videoDataMap.put(videoData.getVideoID(), videoData);	 
	        	harvis.textAreaDM.append("\n Video # "+ ++videoCount +" reloaded ");
	        	
	        	if(videoCount > 10)
	        		break;
			}
			conn.close();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		harvis.campaignManager.setReloadedData(videoDataMap);
	}
	Vector<uTubeVideoComment> loadVideoCommentsData(uTubeVideoData videoDataObj){
		Vector<uTubeVideoComment> comments = new Vector<uTubeVideoComment>();
		Connection conn;
		try {
			conn = getDBConnection();
			Statement selectVideoCommentsQuery = conn.createStatement();
			ResultSet selectVideoCommentsQueryResultSet = selectVideoCommentsQuery.executeQuery("Select * from uTubeVideoComments WHERE VideoID=\'" + videoDataObj.getVideoID() + "\'");			 
			while(selectVideoCommentsQueryResultSet.next()){
				uTubeVideoComment commentObj = new uTubeVideoComment();
				commentObj.setAuthor(selectVideoCommentsQueryResultSet.getString("Author"));	// Author
				commentObj.setContent(selectVideoCommentsQueryResultSet.getString("Content"));	// Content
				commentObj.setCreatedOn(selectVideoCommentsQueryResultSet.getDate("CreatedOn"));// CreatedON
				commentObj.setvideo(videoDataObj);
				comments.add(commentObj);
			}
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return comments;
	}
	private void discoverUoD_Words_FPTree(){		
		// 1. Create a Map of uniqueQueryWords
		HashMap<String, String> queryStringsMap = reloadQueryStrings();
		Set<String> queryStringsSet = queryStringsMap.keySet();
		Iterator<String> queryStringsSetIter = queryStringsSet.iterator();
		String allQueryStrings = "";
		while(queryStringsSetIter.hasNext()){
			allQueryStrings += queryStringsSetIter.next() + "\n";
		}
		StringReader keyWordsStringReader = new StringReader(allQueryStrings);
		BufferedReader keyWordsReader = new BufferedReader(keyWordsStringReader);
		HashMap<String, String> uniqueQueryWordsMap = new HashMap<String, String>();
		try {
			String keyWordsLine = "";
			String delim = " ";
			while((keyWordsLine = keyWordsReader.readLine()) != null){
				StringTokenizer mustKeyWordsTokenizer = new StringTokenizer(keyWordsLine, delim); 
				while (mustKeyWordsTokenizer.hasMoreTokens()) {
					String word = mustKeyWordsTokenizer.nextToken();
					if (uniqueQueryWordsMap.get(word) == null){						
						uniqueQueryWordsMap.put(word, word);						
					}					
				}				
			}
			keyWordsStringReader.close();
			keyWordsReader.close();
		} catch (IOException e1) {			
			e1.printStackTrace();
		}
		
		// 3. Load Data		
		String strSqlCommand = "";
		ArrayList<ContentItem> contentList = new ArrayList<ContentItem>();
		if (uod_param.getSrcTable().equals("utubevideocomments")){
			String []contentCols = new String[1];			
			contentCols[0] = "Content";
			strSqlCommand =	"Select Author, Content from utubevideocomments ";
			if (uod_param.getUodKeywordFilter() != null){				
				strSqlCommand += " WHERE Content LIKE '%"+ uod_param.getUodKeywordFilter()+"%'" +
						" OR VideoID IN (" +
						" Select ID from utubevideos " +
						" WHERE Title LIKE '%"+ uod_param.getUodKeywordFilter()+"%'" +
						" OR Description LIKE '%"+ uod_param.getUodKeywordFilter()+"%');";
			}
			contentList = fetchDataFromDB(contentCols, strSqlCommand);
		}else if(uod_param.getSrcTable().equals("utubevideos")){
			String []contentCols = new String[2];			
			contentCols[0] = "Title";
			contentCols[1] = "Description";
			strSqlCommand =	"Select Author, Title, Description from utubevideos";
			if (uod_param.getUodKeywordFilter() != null){				
				//strSqlCommand += " WHERE Title LIKE '%"+ uod_param.getUodKeywordFilter()+"%' OR Description LIKE '%"+ uod_param.getUodKeywordFilter()+"%'";
				strSqlCommand += " WHERE CONCAT(`Author`, ' ', `Description` ,' ' ,`Title`) " +
						" REGEXP '.*"+uod_param.getUodKeywordFilter()+".*'";
			}
			contentList = fetchDataFromDB(contentCols, strSqlCommand);
		}
		FPTree fpTree = new FPTree(this.uod_param.getWordCountLimit(), contentList);
		// construct FPTree
		uod_param.uodOutputTextArea.append("Constructing FPTree ... \n");
		fpTree.constructFPTree();
		// construct conditional pattern basis.		
		uod_param.uodOutputTextArea.append("Constructing Condition Pattern basis... \n");
		HashMap<String, FPTree.PatternBasis> pattBasisMap = fpTree.constructCondPattBasis();
		Iterator<String> keys = pattBasisMap.keySet().iterator();
		while(keys.hasNext()){
			String key = keys.next();
			uod_param.uodOutputTextArea.append(key + "\n");
			ArrayList<String> pattBasis = pattBasisMap.get(key).getPatternBaseBranches();
			for(String basis : pattBasis){
				uod_param.uodOutputTextArea.append("\t"+ basis + "\n");
			}			
		}
		// construct conditional FP Tree.		
		uod_param.uodOutputTextArea.append("Constructing Condition Pattern basis... \n");		
		HashMap<Node, ArrayList<Node>> condPatternsMap = fpTree.constructCondFpTree();
		Iterator<Node> keys2 = condPatternsMap.keySet().iterator();
		while(keys2.hasNext()){		
			Node key = keys2.next();
			uod_param.uodOutputTextArea.append("\n"+ key.getNodeName() + "\t");
			ArrayList<Node> freqPattNodes = condPatternsMap.get(key);
			for(Node node : freqPattNodes){
				uod_param.uodOutputTextArea.append(node.getNodeName() +":"+node.getNodeValue() + "   ");
				insertUoD_Node_IntoDB(key /*word*/, node /*coWordObj*/, node.getNodeValue()/*coOccurenceCount*/);
			}			
		}		
		harvis.textAreaUodOutput.append("\t \t \n Finished");
}
	private void discoverUoD_Words(){		
		// 1. Create a Map of uniqueQueryWords
		HashMap<String, String> queryStringsMap = reloadQueryStrings();
		Set<String> queryStringsSet = queryStringsMap.keySet();
		Iterator<String> queryStringsSetIter = queryStringsSet.iterator();
		String allQueryStrings = "";
		while(queryStringsSetIter.hasNext()){
			allQueryStrings += queryStringsSetIter.next() + "\n";
		}
		StringReader keyWordsStringReader = new StringReader(allQueryStrings);
		BufferedReader keyWordsReader = new BufferedReader(keyWordsStringReader);
		HashMap<String, String> uniqueQueryWordsMap = new HashMap<String, String>();
		try {
			String keyWordsLine = "";
			String delim = " ";
			while((keyWordsLine = keyWordsReader.readLine()) != null){
				StringTokenizer mustKeyWordsTokenizer = new StringTokenizer(keyWordsLine, delim); 
				while (mustKeyWordsTokenizer.hasMoreTokens()) {
					String word = mustKeyWordsTokenizer.nextToken();
					if (uniqueQueryWordsMap.get(word) == null){						
						uniqueQueryWordsMap.put(word, word);						
					}					
				}				
			}
			keyWordsStringReader.close();
			keyWordsReader.close();
		} catch (IOException e1) {			
			e1.printStackTrace();
		}
		
		// 2. Load words_2b_filtered
		String words_2b_filtered = "";
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
		// 3. Load Data		
		String strSqlCommand = "";
		ArrayList<ContentItem> contentList = new ArrayList<ContentItem>();
		if (uod_param.getSrcTable().equals("utubevideocomments")){
			String []contentCols = new String[1];			
			contentCols[0] = "Content";
			strSqlCommand =	"Select Author, Content from utubevideocomments ";
			if (uod_param.getUodKeywordFilter() != null){				
				strSqlCommand += " WHERE Content LIKE '%"+ uod_param.getUodKeywordFilter()+"%'" +
						" OR VideoID IN (" +
						" Select ID from utubevideos " +
						" WHERE Title LIKE '%"+ uod_param.getUodKeywordFilter()+"%'" +
						" OR Description LIKE '%"+ uod_param.getUodKeywordFilter()+"%');";
			}
			contentList = fetchDataFromDB(contentCols, strSqlCommand);
		}else if(uod_param.getSrcTable().equals("utubevideos")){
			String []contentCols = new String[2];			
			contentCols[0] = "Title";
			contentCols[1] = "Description";
			strSqlCommand =	"Select Author, Title, Description from utubevideos";
			if (uod_param.getUodKeywordFilter() != null){				
				//strSqlCommand += " WHERE Title LIKE '%"+ uod_param.getUodKeywordFilter()+"%' OR Description LIKE '%"+ uod_param.getUodKeywordFilter()+"%'";
				strSqlCommand += " WHERE CONCAT(`Author`, ' ', `Description` ,' ' ,`Title`) " +
						" REGEXP '.*"+uod_param.getUodKeywordFilter()+".*'";
			}
			contentList = fetchDataFromDB(contentCols, strSqlCommand);
		}
		
		int dataItemNumber = 0;		
		Map<String, Word> wordsMap = new HashMap<String, Word>();
		Iterator<ContentItem> contentListIterator = contentList.iterator();
		while(contentListIterator.hasNext()){
			ContentItem contentItem =  contentListIterator.next();			
						
			String author = contentItem.getAuthor().replace(" ", ""); // Remove WhiteSpaces, Required for RelevanceMeasure Algorithm
			String content  = author + " " + contentItem.getContent().replaceAll("[^\\u0000-\\uFFFF]",""); // Replace non-alphanumeric characters
			String delim = " \t\n\r.,:;?؟`~!@#$%^&*+-=_/|{}()[]<>\"\'";
			String strWord;	
			Word newWord;	
			uod_param.uodOutputTextArea.setText("\t" + ++dataItemNumber + " Parsing Words for : \n " + (content.length() > 60 ? content.substring(0, 60) : content) + "..." );			
			StringTokenizer st = new StringTokenizer(content, delim); 
			while (st.hasMoreTokens()) {
				strWord = st.nextToken().toLowerCase().trim();	
				if (strWord.length() > 2){
					newWord = (Word) wordsMap.get(strWord); 
					if (newWord == null) {
						newWord = new Word(strWord, 1);
						// Check if the newWord is among the uniqueQueryWords
						if (uniqueQueryWordsMap.get(strWord) != null)
							newWord.isQueryKeyWord = true;
						newWord.addAuthor(author);
						wordsMap.put(strWord, newWord); 
					} else {
						newWord.addAuthor(author);
						newWord.wordCount++;						
					}						
				}
			}						
		}		
	
		// 4. Remove words_2b_filtered from wordsMap
		StringTokenizer st = new StringTokenizer(words_2b_filtered, ","); 
		System.out.println("Words before removal of filterwords.. " + wordsMap.entrySet().size());		
		while (st.hasMoreTokens()) {
			String strFilterWord = st.nextToken().toLowerCase();	
			Word wordObj = wordsMap.get(strFilterWord);
			if (wordObj != null){  
				wordsMap.remove(strFilterWord);		
				uod_param.uodOutputTextArea.append(strFilterWord + " removed as filter word \n");
			}
		}
		uod_param.uodOutputTextArea.setText("Words after removal of filterwords .. " + wordsMap.entrySet().size());		
		// 5. Remove Word with less occurrence_count than this.wordCountLimit | this.coWordCountLimit
		Set<Map.Entry<String, Word>> wordsSet = wordsMap.entrySet(); 
		Iterator<Map.Entry<String, Word>> wordsSetIter = wordsSet.iterator();
		uod_param.uodOutputTextArea.setText("Removing less frequent words than wordCountLimit ... ");
			
		int limit = (this.uod_param.getWordCountLimit() < this.uod_param.getCoWordCountLimit() ? this.uod_param.getWordCountLimit() :  this.uod_param.getCoWordCountLimit());
		int i = 1;
		while(wordsSetIter.hasNext()){			
			Word wordObj = wordsSetIter.next().getValue();			
			if(wordObj.wordCount < limit){
				try{
					System.out.println("\n\t Removing " + i++ + " " + wordObj.word + " WordCount " + wordObj.wordCount);
					uod_param.uodOutputTextArea.append("\n\t Removing " + wordObj.word + " WordCount " + wordObj.wordCount);
				}catch(Exception e){
					e.printStackTrace();
				}
				if ( uod_param.uodOutputTextArea.getLineCount() > 50)
					uod_param.uodOutputTextArea.setText("Removing less frequent words than wordCountLimit ...");
				wordsMap.remove(wordObj.word);
				wordsSet = wordsMap.entrySet(); 
				wordsSetIter = wordsSet.iterator();
			}
		}
		uod_param.uodOutputTextArea.setText("Words after removal of less frequent than limit ... " + wordsMap.entrySet().size());
		// Make a wordList and sort the words w.r.t occurrence_count		
		java.util.List<Word> wordList = new java.util.ArrayList<Word>();
		wordsSet = wordsMap.entrySet(); 
		wordsSetIter = wordsSet.iterator();
		while(wordsSetIter.hasNext()){
			uod_param.uodOutputTextArea.setText("Sorting words wrt occurenceCount ... ");
			Word wordObj = wordsSetIter.next().getValue();
			wordList.add(wordObj);
		}
		wordsMap.clear();
		wordsMap = null;		
		Collections.sort(wordList, new WordComparator());
		// Prepare words/co-words maps				
		int wordNumber = 1;
		Iterator<Word> wordListIter = wordList.iterator();
		while (wordListIter.hasNext()){			
			Word wordObj = wordListIter.next();
			boolean isArabicWord = wordObj.word.matches("[ ء-ي]+");			
			if(isArabicWord)
				uod_param.uodOutputTextArea.setText("Searching CoWords for " + " (" + wordNumber++ +"/" + wordList.size() +") " +  wordObj.word );
			else				
				uod_param.uodOutputTextArea.setText("Searching CoWords for " + wordObj.word + " (" + wordNumber++ +"/" + wordList.size() +")");		
			Iterator<Word> coWordListIter = wordList.iterator();
			if( wordObj.wordCount >= this.uod_param.getWordCountLimit()) {
				while (coWordListIter.hasNext()){				
					Word coWordObj = coWordListIter.next();				
					if (wordObj.word.equalsIgnoreCase(coWordObj.word) == false && 
						coWordObj.wordCount >= this.uod_param.getCoWordCountLimit() &&
						(wordObj.coWords.containsKey(coWordObj.word) == false && 
						coWordObj.coWords.containsKey(wordObj.word) == false)){	
						int coOccurenceCount = getCoOccurenceCount(wordObj, coWordObj);
						if (coOccurenceCount >= this.uod_param.getCoOccurCountLimit()){
							uod_param.uodOutputTextArea.append("\n Word \t" + coWordObj.word + " \t Count " + coWordObj.wordCount + " \t CoOccurenceCount " + coOccurenceCount);
							insertUoD_Word_IntoDB(wordObj, coWordObj, coOccurenceCount);
							wordObj.coWords.put(coWordObj.word, coWordObj);
							coWordObj.coWords.put(wordObj.word, wordObj);
						}else{
							uod_param.uodOutputTextArea.setText("Ignoring Word " + wordObj.word + " and CoWord " + coWordObj.word);
							uod_param.uodOutputTextArea.append(", already picked as CoWord OR OccurLimit does not satisfy.");
						}
					}
				}
			}
		}			
		harvis.textAreaUodOutput.append("\t \t \n Finished");
}
	
	/* This Function is to be used by d2i
	 *  
	 */
	public static Vector<String> getTableNames(String name){
		Vector<String> tableNamesVector = new Vector<String>();
		try {
			Connection conn = uTubeDataManager.getDBConnection();
			DatabaseMetaData dbmd = conn.getMetaData();
			ResultSet tableNamesResultSet = dbmd.getTables(null, null, name, null);
			while(tableNamesResultSet.next()){
				tableNamesVector.add(tableNamesResultSet.getString(3));
			}	
			return tableNamesVector;
		} catch (ClassNotFoundException e) {			
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	/* This Function is to be used by d2i
	 *  
	 */
	public static Vector<String> getVideoAuthor_UserIDs(){
		Vector<String> videoAuthor_UserIDs = new Vector<String>();
		try {
			Connection conn = uTubeDataManager.getDBConnection();
			Statement userIdStatement = conn.createStatement();
			ResultSet userIdResultSet = userIdStatement.executeQuery("Select DISTINCT(UserID) FROM uTubeVideos");
			 
			while(userIdResultSet.next()){
				videoAuthor_UserIDs.add(userIdResultSet.getString("UserID"));				
			}	
			return videoAuthor_UserIDs;
		} catch (ClassNotFoundException e) {			
			e.printStackTrace();
		} catch (SQLException e) {

			e.printStackTrace();
		}
		return null;
	}
	/* This Function is to be used by d2i
	 *  
	 */
	public static Vector<uTubeAuthor> getVideoAuthorProfiles(){
		Vector<uTubeAuthor> videoAuthorProfiles = new Vector<uTubeAuthor>();
		try {
			Connection conn = uTubeDataManager.getDBConnection();
			Statement userProfileStatement = conn.createStatement();
			String sql = " SELECT authorID, title, fname, lname, fullname," +
					" location, published, content, subscribers_count," +
					" videos_watch_count, views_count, totalUploadViews " +
					" FROM utubeauthors;";
			ResultSet userProfileResultsSet = userProfileStatement.executeQuery(sql);
			 
			while(userProfileResultsSet.next()){
				uTubeAuthor authorProfile = new uTubeAuthor(userProfileResultsSet.getString("authorID"));				
				videoAuthorProfiles.add(authorProfile);								
			}	
			return videoAuthorProfiles;
		} catch (ClassNotFoundException e) {			
			e.printStackTrace();
		} catch (SQLException e) {			
			e.printStackTrace();
		}
		return null;
	}
	public static void createNewDB_Schema(String dbSchemaName){
		try {
			Connection conn = uTubeDataManager.getDBConnection();					
			Statement statement = conn.createStatement();
			String newDB_SQL = "CREATE DATABASE "+ dbSchemaName;
		    statement.executeUpdate(newDB_SQL);		    
			conn.close();			
			JOptionPane.showMessageDialog(null, "Database created successfully.", "I Got It.", JOptionPane.INFORMATION_MESSAGE);			
		} catch (ClassNotFoundException e) {			
			JOptionPane.showMessageDialog(null, "Database driver is not properly installed \n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			return;
		} catch (SQLException e) {
			JOptionPane.showMessageDialog(null, "Database connection parameters are not correct \n" + e.getErrorCode(), "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		dbSchema = dbSchemaName;
		create_CommentVideoAuthors_Table();
		create_uTubeSearchRound_Table();
		create_uTubeVideoComments_Table();
		create_uTubeVideos_Table();
		create_CommentAuthorsProfile_Table();
		create_CommentVideoAuthors_MDVA_Table(); //MDVA (Most Discussed VA)
		createUoD_Table("uod_titles");	
		create_utubeAuthors_Table(); 
	}
	private static String create_CommentVideoAuthors_Table(){		
		try {
            Connection connection = getDBConnection();            
            Statement statement = connection.createStatement();  
            String createTableCommand =
       		"CREATE TABLE `commentvideoauthors` ( "+
            " `CommentAuthor` varchar(100) NOT NULL, "+
            " `CommentCount` int(11) DEFAULT NULL, "+
            " `VideoID` varchar(100) DEFAULT NULL, "+
            " `VideoTitle` varchar(500) DEFAULT NULL, "+
            " `VideoAuthor` varchar(100) DEFAULT NULL "+
            " ) ENGINE=InnoDB DEFAULT CHARSET=utf8";
            statement.executeUpdate(createTableCommand);            
        } catch (SQLException e) { 
            e.printStackTrace();
            return e.getMessage();
        } catch (ClassNotFoundException e) { 
			e.printStackTrace();
			return e.getMessage();
		}		
		return "CommentVideoAuthors created successfully.";
	}
	private static String create_uTubeSearchRound_Table(){		
		try {
            Connection connection = getDBConnection();            
            Statement statement = connection.createStatement();  
            String createTableCommand =
            		"CREATE TABLE `utubesearchround` ( "+
            		"  `SeedQueryString` varchar(500) COLLATE utf8_bin NOT NULL, "+
            		"  `SearchRound` int(11) NOT NULL, "+
            		"  `VideosCount` int(11) DEFAULT NULL, "+
            		"  `TimeInMinutes` int(11) DEFAULT NULL, "+
            		"  `ServiceExecutionCount` int(11) DEFAULT NULL, "+
            		"  `VideosReturnedByService` int(11) DEFAULT NULL "+
            		" ) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin";           	
            statement.executeUpdate(createTableCommand);            
        } catch (SQLException e) { 
            e.printStackTrace();
            return e.getMessage();
        } catch (ClassNotFoundException e) { 
			e.printStackTrace();
			return e.getMessage();
		}		
		return "uTubeSearchRound created successfully.";
	}
	private static String create_uTubeVideoComments_Table(){		
		try {
            Connection connection = getDBConnection();            
            Statement statement = connection.createStatement();  
            String createTableCommand =
            		" CREATE TABLE `utubevideocomments` ( "+
            		"  `ID` varchar(60) CHARACTER SET utf8 NOT NULL, "+
            		"  `VideoID` varchar(45) CHARACTER SET utf8 DEFAULT NULL, "+
            		"  `Author` varchar(150) CHARACTER SET utf8 DEFAULT NULL, "+
            		"  `CreatedOn` varchar(45) CHARACTER SET utf8 DEFAULT NULL, "+
            		"  `Content` text COLLATE utf8_bin, "+
            		"  `VideoTitle` varchar(200) CHARACTER SET utf8 DEFAULT NULL, "+
            		"  `VideoAuthor` varchar(150) CHARACTER SET utf8 DEFAULT NULL, "+
            		"  PRIMARY KEY (`ID`), "+
            		"  FULLTEXT KEY `VideoID_Index` (`VideoID`), "+
            		"  FULLTEXT KEY `CommentAuthor_Index` (`Author`), "+
            		"  FULLTEXT KEY `CommentContent_Index` (`Content`) "+
            		" ) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_bin";            	
            statement.executeUpdate(createTableCommand);            
        } catch (SQLException e) { 
            e.printStackTrace();
            return e.getMessage();
        } catch (ClassNotFoundException e) { 
			e.printStackTrace();
			return e.getMessage();
		}		
		return "uTubeVideoComments created successfully.";
	}
	private static String create_CommentAuthorsProfile_Table(){
		int result = 0;
		try {
            Connection connection = getDBConnection();            
            Statement statement = connection.createStatement();  
            String createTableCommand =
            "CREATE TABLE `commentauthorsprofile` ( "+
			" `Author` varchar(150) NOT NULL, "+
			" `CommentsCount` int(11) DEFAULT NULL, "+
			" `AllVideosCount` int(11) DEFAULT NULL, "+
			" `OthersVideosCount` int(11) DEFAULT NULL, "+
			"  PRIMARY KEY (`Author`) "+
			" ) ENGINE=InnoDB DEFAULT CHARSET=utf8";            	
            result = statement.executeUpdate(createTableCommand);            
        } catch (SQLException e) { 
            e.printStackTrace();
            return e.getMessage();
        } catch (ClassNotFoundException e) { 
			e.printStackTrace();
			return e.getMessage();
		}		
		return "CommentAuthorsProfile created successfully.";
	}
	private static String create_CommentVideoAuthors_MDVA_Table(){
		int result = 0;
		try {
            Connection connection = getDBConnection();            
            Statement statement = connection.createStatement();  
            String createTableCommand =
            		"CREATE TABLE `commentvideoauthors_mdva` ( "+
            				"`CommentAuthor` varchar(150) DEFAULT NULL, " +
            				"`CommentedVideosCount` int(11) DEFAULT NULL, " +
            				"`VideoAuthor` varchar(150) DEFAULT NULL, " +
            				"`UploadedVideosCount` int(11) DEFAULT NULL, " +
            				"`TotalRecievedCommentCount` int(11) DEFAULT NULL, " +
            				"`VideosCountRcvngComntFromCommentAuthor` int(11) DEFAULT NULL " +
            				") ENGINE=InnoDB DEFAULT CHARSET=utf8";            	
            result = statement.executeUpdate(createTableCommand);            
        } catch (SQLException e) { 
            e.printStackTrace();
            return e.getMessage();
        } catch (ClassNotFoundException e) { 
			e.printStackTrace();
			return e.getMessage();
		}		
		return "CommentAuthorsProfile created successfully.";
	}
	private static String create_utubeAuthors_Table(){
		int result = 0;
		try {
            Connection connection = getDBConnection();            
            Statement statement = connection.createStatement();  
            String createTableCommand =
				"CREATE TABLE `utubeauthors` ( "+
				"  `authorID` varchar(200) NOT NULL, " +
				"  `fname` varchar(100) DEFAULT NULL, " +
				"  `lname` varchar(100) DEFAULT NULL, " +
				"  `fullname` varchar(200) DEFAULT NULL, " +
				"  `title` varchar(500) DEFAULT NULL, " +
				"  `content` varchar(5000) DEFAULT NULL," +
				"  `location` varchar(10) DEFAULT NULL," +
				"  `published` varchar(50) DEFAULT NULL," +
				"  `views_count` int(11) DEFAULT NULL," +
				"  `subscribers_count` int(11) DEFAULT NULL," +
				"  `videos_watch_count` int(11) DEFAULT NULL," +	
				" `totalUploadViews` int(11) DEFAULT NULL "+
				" ) ENGINE=InnoDB DEFAULT CHARSET=utf8";
            result = statement.executeUpdate(createTableCommand);
            
            createTableCommand = "CREATE TABLE `utubeauthorsubs` ( "+
            		  			 "`authorID` varchar(200) NOT NULL, "+
            		  			 "`subscribedToAuthorID` varchar(200) NOT NULL "+
            		  			 ") ENGINE=InnoDB DEFAULT CHARSET=utf8";
            
            result = statement.executeUpdate(createTableCommand);
		}catch (SQLException e) { 
            e.printStackTrace();
            return e.getMessage();
        } catch (ClassNotFoundException e) { 
			e.printStackTrace();
			return e.getMessage();
		}	
		return "Table utubeAuthors created successfully.";
	}
	private static String create_uTubeVideos_Table(){
		int result = 0;
		try {
            Connection connection = getDBConnection();            
            Statement statement = connection.createStatement();  
            String createTableCommand =
            		"CREATE TABLE `utubevideos` ( "+
            		"  `Author` varchar(150) DEFAULT NULL, "+
            		"  `UserID` varchar(150) NOT NULL,"+
            		"  `Category` varchar(45) DEFAULT NULL, "+
            		"  `CommentsCount` int(11) DEFAULT NULL, "+
            		"  `CreatedOn` datetime DEFAULT NULL, "+
            		"  `Description` varchar(600) DEFAULT NULL, "+
            		"  `DescriptionXed` varchar(600) DEFAULT NULL, "+
            		"  `Dislikes` int(11) DEFAULT NULL, "+
            		"  `Duration` float DEFAULT NULL, "+
            		"  `DL_TimeStamp` timestamp NULL DEFAULT CURRENT_TIMESTAMP, "+
            		"  `FavoritesCount` int(11) DEFAULT NULL, "+
            		"  `ID` varchar(50) NOT NULL, "+
            		"  `Likes` int(11) DEFAULT NULL, "+
            		"  `Location` varchar(100) DEFAULT NULL, "+
            		"  `Rating` float DEFAULT NULL, "+
            		"  `RatersCount` int(11) DEFAULT NULL,"+
            		"  `MinRate` int(11) DEFAULT NULL,"+
            		"  `MaxRate` int(11) DEFAULT NULL,"+
            		"  `QueryString` varchar(200) DEFAULT NULL, "+
            		"  `QueryUrl` varchar(500) DEFAULT NULL, "+
            		"  `QueryFullText` varchar(200) DEFAULT NULL, "+
            		"  `Title` varchar(200) DEFAULT NULL, "+
            		"  `ViewsCount` bigint(20) DEFAULT NULL, "+
            		"  `SearchRound` int(11) DEFAULT NULL,"+
            		"  PRIMARY KEY (`ID`) "+            		
            		" ) ENGINE=MyISAM DEFAULT CHARSET=utf8";            	
            result = statement.executeUpdate(createTableCommand);            
        } catch (SQLException e) { 
            e.printStackTrace();
            return e.getMessage();
        } catch (ClassNotFoundException e) { 
			e.printStackTrace();
			return e.getMessage();
		}		
		return "uTubeVideos created successfully.";
	}
	public static String createUoD_Table(String tableName){
		int result = 0;
		try {
            Connection connection = getDBConnection();            
            Statement statement = connection.createStatement();  
            String createTableCommand =				
    				"CREATE TABLE "+tableName+" ( " +
    				"`Word` varchar(100) NOT NULL, "+				
    				"`WordCount` int(11) DEFAULT NULL, "+
    				"`AuthorsCount` int(11) DEFAULT NULL, "+
    				"`CoWord` varchar(45) DEFAULT NULL, "+
    				"`CoWordAuthorsCount` int(11) DEFAULT NULL, "+
    				"`CoOccurenceCount` int(11) DEFAULT NULL, "+
    				"`CoWordCount` int(11) DEFAULT NULL, "+
    				"`isQueryWord` bit(1) DEFAULT b'0' "+
    				") ENGINE=InnoDB DEFAULT CHARSET=utf8";            	
            result = statement.executeUpdate(createTableCommand);            
        } catch (SQLException e) { 
            e.printStackTrace();
            return e.getMessage();
        } catch (ClassNotFoundException e) { 
			e.printStackTrace();
			return e.getMessage();
		}		
		return tableName + " created successfully.";
	}
	/* This function is to be used by discoverUoD_Word(). 130409 
	 * */
	private ArrayList<ContentItem> fetchDataFromDB(String strContentColNames[], String strSqlQuery){
		String strCols = "";
		ArrayList<ContentItem> contentList = new ArrayList<ContentItem>();
		for (int i = 0 ; i < strContentColNames.length; i ++){
			strCols += strContentColNames[i] +", ";			
		}
		strCols = strCols.substring(0, strCols.lastIndexOf(",")); // Remove Last Comma ,
		try {
			Connection conn = getDBConnection();
			Statement selectDataQuery = conn.createStatement();			
			ResultSet selectDataQueryResultSet = selectDataQuery.executeQuery(strSqlQuery);			
			while(selectDataQueryResultSet.next()){
				ContentItem contentItem = new ContentItem();
				// Append values to the content
				for (int i = 0 ; i < strContentColNames.length; i ++){
					contentItem.setContent(contentItem.getContent() + ", "+selectDataQueryResultSet.getString(strContentColNames[i]));					
				}				
				contentItem.setAuthor(selectDataQueryResultSet.getString("Author"));
				contentList.add(contentItem);
			}						
			conn.close();
			return contentList;
		} catch (SQLException e) {
			harvis.textAreaUodOutput.setText(e.getMessage());
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			harvis.textAreaUodOutput.setText(e.getMessage());
			e.printStackTrace();
		}		
		return null;		
	}
	/* This function primarily works in cooperation with 
	 * uTubeCrawler to ensure that same videos are 
	 * not being accumulated in the videosDataMap. 130409
	 * */
	public HashMap<String, uTubeVideoData> reLoadVideoDataLite(){
		videoDataMap = new HashMap<String, uTubeVideoData>();
		try {
			Connection conn = getDBConnection();
			Statement selectVideoDataQuery = conn.createStatement();
			ResultSet selectVideoDataQueryResultSet = selectVideoDataQuery.executeQuery(
					"Select ID, Title, Description, Author, SearchRound from uTubeVideos ORDER BY SearchRound DESC");
			int videoCount = 0;
			while(selectVideoDataQueryResultSet.next()){
	        	uTubeVideoData videoData = new uTubeVideoData();
				videoData.setVideoID(selectVideoDataQueryResultSet.getString("ID"));				// VideoID	        	
	        	videoData.setTitle(selectVideoDataQueryResultSet.getString("Title"));	 			// Title
	        	videoData.setDescription(selectVideoDataQueryResultSet.getString("Description"));	// Description
	        	videoData.setAuthor(selectVideoDataQueryResultSet.getString("Author")); 			// Author
	        	videoData.setSearchRound(selectVideoDataQueryResultSet.getInt("SearchRound")); 		// SearchRound
				videoData.setSaved();
				videoDataMap.put(videoData.getVideoID(), videoData);				
				videoCount++;
			}
			harvis.textAreaClient.append("\n\t " + videoCount + " videos loaded into memory.");			
			conn.close();
			return videoDataMap;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return null;
	}
	private int getCoOccurenceCount(Word wordObj, Word coWordObj){
		try {
			Connection conn = getDBConnection();
			Statement selectCoWordQuery = conn.createStatement();
			String strSqlCommand = "";	
			int coOccurrenceCount = 0;
			String predicateWordObj = "CONCAT('%"+ wordObj.word + "%')";			
			String predicateCoWordObj = "CONCAT('%"+ coWordObj.word + "%')";
			// If English, put spaces around the strWord to find only complete words
			// It will restrict counting Titles containing strWord mixed into other words (warrior <--> war)						
			if (wordObj.word.matches("[A-Za-z0-9]+")){
				predicateWordObj = "CONCAT('% "+ wordObj.word + " %')";				
			}
			if (coWordObj.word.matches("[A-Za-z0-9]+")){							
				predicateCoWordObj = "CONCAT('% "+ coWordObj.word + " %')";
			}
			if (uod_param.getSrcTable().equals("utubevideocomments")){				
				strSqlCommand =	"SELECT COUNT(Content) AS CoOccurrenceCount "+
						"FROM utubevideocomments ";
						if (wordObj.word.matches("[A-Za-z0-9]+")){
							strSqlCommand += "WHERE Content LIKE " + predicateWordObj + 
							" AND Content LIKE " + predicateCoWordObj;
						}
						if (wordObj.word.matches("[ء-ي]+")){
							strSqlCommand += "WHERE Content LIKE " + predicateWordObj + 
							" AND Content LIKE " + predicateCoWordObj;
						}
				ResultSet selectCoWordQueryResultSet = selectCoWordQuery.executeQuery(strSqlCommand);				
				while(selectCoWordQueryResultSet.next()){
					coOccurrenceCount = selectCoWordQueryResultSet.getInt("CoOccurrenceCount"); 		// SearchRound				
				}				
			}else if(uod_param.getSrcTable().equals("utubevideos")){
				strSqlCommand =	"SELECT COUNT(Title) AS CoOccurrenceCount "+
						"FROM utubevideos "+
						"WHERE CONCAT(`Author`, ' ', `Description` ,' ' ,`Title`) " +
						"REGEXP '.*"+wordObj.word+".*"+coWordObj.word+".*|.*"+coWordObj.word+".*"+wordObj.word+".*'";
				ResultSet selectCoWordQueryResultSet = selectCoWordQuery.executeQuery(strSqlCommand);			
				while(selectCoWordQueryResultSet.next()){
					coOccurrenceCount = selectCoWordQueryResultSet.getInt("CoOccurrenceCount"); 		// SearchRound				
				}
			}
			conn.close();
			return coOccurrenceCount;
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}		
		return 0;
	}
	public int getMaxSearchRound(String queryString){		
		int searchRound = 0;
		try {
			Connection conn = getDBConnection();
			Statement selectMaxSearchRoundQuery = conn.createStatement();
			ResultSet selectMaxSearchRoundResultSet = selectMaxSearchRoundQuery.executeQuery(
					"Select MAX(SearchRound) AS MaxSearchRound from utubesearchround WHERE SeedQueryString LIKE '" + queryString  +"'");			
			while(selectMaxSearchRoundResultSet.next()){
				searchRound = selectMaxSearchRoundResultSet.getInt("MaxSearchRound"); 		// SearchRound				
			}						
			conn.close();
			return searchRound;
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}		
		return 0;
	}
	public HashMap<String, uTubeQuery> reloadQueryObjects(String queryString){
		int searchRound = getMaxSearchRound(queryString);
		try {
			Connection conn = getDBConnection();			
			Statement selectQueryURLQuery = conn.createStatement();
			ResultSet selectQueryResultSet = selectQueryURLQuery.executeQuery(
					"SELECT QueryString, QueryURL, MAX(SearchRound) AS MaxSearchRound FROM utubevideos "+
					"WHERE QueryString LIKE '%" + queryString +"%' " +
					"GROUP BY QueryString");
			int queryCount = 0;
			HashMap<String, uTubeQuery> queryObjects = new HashMap<String, uTubeQuery>();
			
			while(selectQueryResultSet.next()){
				queryString = selectQueryResultSet.getString("QueryString");
				String queryURL = selectQueryResultSet.getString("QueryURL");
				uTubeQuery checkedQuery = new uTubeQuery(new URL(queryURL));			
				checkedQuery.setQueryString(queryString);				
				checkedQuery.setFullTextQuery(queryString);
				checkedQuery.setChecked();
				checkedQuery.setMaxResults(50);
				checkedQuery.setSearchRound(searchRound);//(selectQueryResultSet.getInt("MaxSearchRound"));
				queryObjects.put(queryString, checkedQuery);				
				queryCount++;
			}
			harvis.textAreaClient.append("\n\t " + queryCount + " existing queries loaded into memory for keyword/s " + queryString);			
			conn.close();			
			return queryObjects;
		} catch (SQLException e) {			
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			 
			e.printStackTrace();
		}	
		return null;
	}
	public HashMap<String, uTubeQuery> reloadQueryObjects(){		
		try {
			Connection conn = getDBConnection();			
			Statement selectQueryURLQuery = conn.createStatement();
			ResultSet selectQueryResultSet = selectQueryURLQuery.executeQuery(
					"SELECT QueryString, QueryURL, MAX(SearchRound) AS MaxSearchRound FROM utubevideos "+					
					"GROUP BY QueryString");
			int queryCount = 0;
			HashMap<String, uTubeQuery> queryObjects = new HashMap<String, uTubeQuery>();
			
			while(selectQueryResultSet.next()){
				String queryString = selectQueryResultSet.getString("QueryString");
				String queryURL = selectQueryResultSet.getString("QueryURL");
				uTubeQuery checkedQuery = new uTubeQuery(new URL(queryURL));			
				checkedQuery.setQueryString(queryString);				
				checkedQuery.setFullTextQuery(queryString);
				checkedQuery.setChecked();
				checkedQuery.setMaxResults(50);
				checkedQuery.setSearchRound(0);//(selectQueryResultSet.getInt("MaxSearchRound"));
				queryObjects.put(queryString, checkedQuery);				
				queryCount++;
			}
			harvis.textAreaClient.append("\n\t " + queryCount + " existing queries loaded into memory.");			
			conn.close();			
			return queryObjects;
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}	
		return null;
	}
	public static HashMap<String, String> reloadQueryStrings(){		
		try {
			Connection conn = getDBConnection();			
			Statement selectQueryURLQuery = conn.createStatement();
			ResultSet selectQueryResultSet = selectQueryURLQuery.executeQuery(
					"SELECT SeedQueryString, MAX(SearchRound) AS MaxSearchRound FROM utubesearchround "+			
					"GROUP BY SeedQueryString");			
			HashMap<String, String> queryObjects = new HashMap<String, String>();
			
			while(selectQueryResultSet.next()){
				String fullQueryString = selectQueryResultSet.getString("SeedQueryString");
//				String queryString = fullQueryString.substring(1, fullQueryString.indexOf('"', 1));
//				if (queryObjects.containsKey(queryString) == false)
//					queryObjects.put(queryString, fullQueryString);
				fullQueryString = fullQueryString.replaceAll("\"", "");
				queryObjects.put(fullQueryString, fullQueryString);
			}
			
			conn.close();			
			return queryObjects;
		} catch (SQLException e) {			 
			e.printStackTrace();
		} catch (ClassNotFoundException e) {			 
			e.printStackTrace();
		}	
		return null;
	}
	public boolean wordsMatchExists(String KeyWords, String titleAndDesc ){	
		BufferedReader mustKeyWordsReader = new BufferedReader(new StringReader(KeyWords));
		String delim = " &\t\n.,:;?!@#$%^&*~+-/{}()[]\"\'1234567890";
		//String text1Line = "";
		String titleAndDescLine = "";		
		String titleAndDescWord;
		boolean matchExists = false;		
		try {
			while((KeyWords = mustKeyWordsReader.readLine()) != null){
				StringTokenizer mustKeyWordsTokenizer = new StringTokenizer(KeyWords, delim); 
				while (mustKeyWordsTokenizer.hasMoreTokens()) {
					String keyWord = mustKeyWordsTokenizer.nextToken().toLowerCase().trim();
					matchExists = false;
					BufferedReader titleAndDescReader = new BufferedReader(new StringReader(titleAndDesc));
					while ((titleAndDescLine = titleAndDescReader.readLine()) != null){
						StringTokenizer titleAndDescTokenizer = new StringTokenizer(titleAndDescLine, delim);
						while (titleAndDescTokenizer.hasMoreTokens()) {
							titleAndDescWord = titleAndDescTokenizer.nextToken().toLowerCase().trim();
							if (keyWord.length() == titleAndDescWord.length()){								
								matchExists = keyWord.equalsIgnoreCase(titleAndDescWord);
								if (matchExists) {									
									break;			
								}
							}
						}// while titleAndDescTokenizer has more tokens
						if (matchExists)
							break; // Match found, stop finding in remaining words of titleAndDescLine
					} // while titleAndDescLine has a line to be processed	
					titleAndDescReader.close();					
				}				
			}			
		} catch (IOException e1) {
			 
			e1.printStackTrace();
		}
		return matchExists;
	}
	protected void insertSearchRoundRecordIntoDB(String searchKeyWord, 
												int searchRound, 
												int timeInMinutes, 
												int videosCount,
												int serviceExecutionCount,
												int videosReturnedByService){
		PreparedStatement preparedStatement = null;
		String insertSearchRoundRecord = 
				"INSERT INTO utubesearchround (SeedQueryString, SearchRound, TimeInMinutes, VideosCount, ServiceExecutionCount, VideosReturnedByService) "+
				"VALUES "+
				"(?,?,?,?,?,?)";				
		try {
			Connection conn = getDBConnection();
			preparedStatement = conn.prepareStatement(insertSearchRoundRecord);
			preparedStatement.setString(1, searchKeyWord);
			preparedStatement.setInt(2, searchRound);
			preparedStatement.setInt(3, timeInMinutes);
			preparedStatement.setInt(4, videosCount);
			preparedStatement.setInt(5, serviceExecutionCount);
			preparedStatement.setInt(6, videosReturnedByService);			
			preparedStatement.executeUpdate();			
			conn.close();
		} catch (SQLException e1) {
			 
			e1.printStackTrace();
		} catch (ClassNotFoundException e) {
			 
			e.printStackTrace();
		}
	}
	protected void insertUoD_Word_IntoDB(Word wordObj, Word coWordObj, int coOccurenceCount){
		PreparedStatement preparedStatement = null;
		String insertSearchRoundRecord = 
				"INSERT INTO "+ uod_param.getDestTable() + " (Word, WordCount, AuthorsCount, CoWord, CoWordCount,CoWordAuthorsCount, CoOccurenceCount, isQueryWord) "+
				"VALUES "+
				"(?,?,?,?,?,?,?,?)";				
		try {
			Connection conn = getDBConnection();
			
			preparedStatement = conn.prepareStatement(insertSearchRoundRecord);
			preparedStatement.setString	(1, wordObj.word);
			preparedStatement.setInt	(2, wordObj.wordCount);
			preparedStatement.setInt	(3, wordObj.authorCount);
			preparedStatement.setString	(4, coWordObj.word);
			preparedStatement.setInt	(5, coWordObj.wordCount);
			preparedStatement.setInt	(6, coWordObj.authorCount);
			preparedStatement.setInt	(7, coOccurenceCount);
			preparedStatement.setBoolean(8, wordObj.isQueryKeyWord);
			
			preparedStatement.executeUpdate();		
			
			conn.close();
		} catch (SQLException e1) {
			e1.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	protected void insertUoD_Node_IntoDB(Node wordObj, Node coWordObj, int coOccurenceCount){
		PreparedStatement preparedStatement = null;
		String insertSearchRoundRecord = 
				"INSERT INTO "+ uod_param.getDestTable() + " (Word, WordCount, AuthorsCount, CoWord, CoWordCount,CoWordAuthorsCount, CoOccurenceCount, isQueryWord) "+
				"VALUES "+
				"(?,?,?,?,?,?,?,?)";				
		try {
			Connection conn = getDBConnection();
			
			preparedStatement = conn.prepareStatement(insertSearchRoundRecord);
			preparedStatement.setString	(1, wordObj.getNodeName());
			preparedStatement.setInt	(2, wordObj.getNodeValue());
			preparedStatement.setInt	(3, 1);
			preparedStatement.setString	(4, coWordObj.getNodeName());
			preparedStatement.setInt	(5, coWordObj.getNodeValue());
			preparedStatement.setInt	(6, 1);
			preparedStatement.setInt	(7, coOccurenceCount);
			preparedStatement.setBoolean(8, false);
			
			preparedStatement.executeUpdate();		
			
			conn.close();
		} catch (SQLException e1) {
			e1.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	private void insertVideoCommentsIntoDB(uTubeVideoData videoData){
		uTubeVideoComment comment = null;
		PreparedStatement preparedStatement = null;
		String insertVideoCommentsSQL = 
				"INSERT INTO uTubeVideoComments " + 
				"(Author, Content, CreatedOn, ID, VideoID, VideoTitle, VideoAuthor) VALUES " +
				"(?,?,?,?,?,?,?)";		
		
		try {
			preparedStatement = dbConnection.prepareStatement(insertVideoCommentsSQL);
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		int commentCount = 1;
		Iterator<uTubeVideoComment> commentsIter = videoData.comments.iterator();
		for(; commentsIter.hasNext() ; ){								
				comment = commentsIter.next();
				try{
					preparedStatement.setString(1, 	comment.getAuthor().replaceAll("[^\\u0000-\\uFFFF]", ""));
					preparedStatement.setString(2, 	comment.getContent().replaceAll("[^\\u0000-\\uFFFF]", ""));
					preparedStatement.setDate(	3, 	comment.getCreatedOn());
					preparedStatement.setString(4,	videoData.getVideoID()+" Comment " + commentCount++);
					preparedStatement.setString(5,	videoData.getVideoID());
					preparedStatement.setString(6,	videoData.getTitle().replaceAll("[^\\u0000-\\uFFFF]", ""));
					preparedStatement.setString(7,	videoData.getAuthor().replaceAll("[^\\u0000-\\uFFFF]", ""));
					
				
					preparedStatement.executeUpdate();
				}catch(SQLException e){
					e.printStackTrace();
					System.out.println(comment.getContent());					
				}
		}		
	}
	private void insertVideoRecordIntoDB(uTubeVideoData videoData)  {		
		PreparedStatement preparedStatement = null; 
		String insertVideoDataSQL = 
		"INSERT INTO uTubeVideos " + 
		"(Author, UserID, Category, CommentsCount, CreatedOn, Description, Dislikes, Duration, " +
		"FavoritesCount, ID, DL_TimeStamp, Likes, Location, Rating, RatersCount, MinRate, MaxRate, QueryString, QueryUrl, QueryFullText, " +
		"Title, ViewsCount, SearchRound) VALUES " +
		"(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";		
		try {			
			preparedStatement = dbConnection.prepareStatement(insertVideoDataSQL); 
			preparedStatement.setString(1, videoData.getAuthor().replaceAll("[^\\u0000-\\uFFFF]", "")); 											//Author
			preparedStatement.setString(2, videoData.getUserID().replaceAll("[^\\u0000-\\uFFFF]", "")); 											//UserID
			preparedStatement.setString(3, videoData.getCategory());										//Category
			preparedStatement.setInt(	4, videoData.getCommentsCount());										//CommentsCount
			preparedStatement.setDate(	5, videoData.getCreatedOn());											//CreatedOn 
			preparedStatement.setString(6, videoData.getDescription().replaceAll("[^\\u0000-\\uFFFF]", ""));//Description
			preparedStatement.setInt(	7, videoData.getDislikesCount()); 										//Dislikes
			preparedStatement.setFloat(	8, videoData.getDuration()); 										//Duration
			preparedStatement.setLong(	9, videoData.getFavoriteCount());										//FavoritesCount
			preparedStatement.setString(10,videoData.getVideoID()); 											// ID
			preparedStatement.setTimestamp(11, null);														// TimeStamp
			preparedStatement.setInt(	12, videoData.getLikesCount()); 										//Likes
			preparedStatement.setString(13, videoData.getLocation()); 										//Location
			preparedStatement.setFloat(	14, videoData.getRating()); 											//Rating
			preparedStatement.setFloat(	15, videoData.getRatersCount());										//RatersCount
			preparedStatement.setFloat( 16, videoData.getMinRate()); 											//MinRating
			preparedStatement.setFloat( 17, videoData.getMaxRate()); 											//MaxRating
			preparedStatement.setString(18, videoData.getQueryObject().getQueryString()); 					// QueryString			
			preparedStatement.setString(19, videoData.getQueryObject().getFullUrl() + ""); 					// QueryUrl
			preparedStatement.setString(20, videoData.getQueryObject().getFullTextQuery());					// FullTextQuery
			preparedStatement.setString(21, videoData.getTitle().replaceAll("[^\\u0000-\\uFFFF]", ""));		//Title
			preparedStatement.setLong(  22, videoData.getViewCount());										//ViewsCount
			preparedStatement.setLong(  23, videoData.getSearchRound());										//SearchRound
			

			preparedStatement.executeUpdate();
			
			videoData.setSaved();
			// harvis.textAreaDM.append("\n\t uTubeDM: Data for video " + videoData.getTitle() + " saved.");
			// Save Comments
			insertVideoCommentsIntoDB(videoData);
		}catch(com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException e){
			harvis.textAreaDM.append("\n\t uTubeDM: IntegrityConstraint Exception \n\t" + videoData.getVideoID() + " already inserted");
			e.printStackTrace();							 
		}catch (SQLException e) { 
			harvis.textAreaDM.append("\n\t uTubeDM: SQL Exception " + e.getMessage());			
			e.printStackTrace(); 
		}
		finally { 
			if (preparedStatement != null) {
				try {
					preparedStatement.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}			
		} 
	}
	public static void setDBConnParameters(String dbUserName, String dbPwd, String dbUrl, String dbSchemaName){
		dbUser = dbUserName;
		dbPassword = dbPwd;
		dbURL = dbUrl;
		dbSchema = dbSchemaName;
	}
	/* This method is to be used by 
	 * 
	 * */
	public static void setDBConnParameters(String dbUserName, String dbPwd, String dbUrl){
		dbUser = dbUserName;
		dbPassword = dbPwd;
		dbURL = dbUrl;
	}
	public static String getDbUser() {
		return dbUser;
	}
	public static String getDbPassword() {
		return dbPassword;
	}
	public static String getDbURL() {
		return dbURL;
	}
	public static String getDbSchemaName() {
		return dbSchema;
	}
	public static void insertAuthorProfile(uTubeAuthor authorProfile){
		PreparedStatement preparedStatement = null;
		String insertAuthorProfileRecord = 				
				"INSERT INTO utubeauthors (authorID, " +
				"fname, " +
				"lname, " +
				"fullname, " +
				"title, " +
				"content, " +
				"location, " +
				"published, " +
				"views_count, " +
				"subscribers_count, " +
				"videos_watch_count) "+
				"VALUES "+
				"(?,?,?,?,?,?,?,?,?,?,?)";				
		try {
			Connection conn = getDBConnection();
			
			preparedStatement = conn.prepareStatement(insertAuthorProfileRecord);
			preparedStatement.setString	(1, authorProfile.getAuthorID());
			preparedStatement.setString	(2, authorProfile.getFirstName());
			preparedStatement.setString	(3, authorProfile.getLastName());
			preparedStatement.setString	(4, authorProfile.getAuthorname());
			preparedStatement.setString	(5, authorProfile.getAuthor_title());
			String content = authorProfile.getContent();
			if(content.length() > 5000)
				content = content.substring(0, 4999);
			preparedStatement.setString	(6, content);
			preparedStatement.setString	(7, authorProfile.getLocation());
			preparedStatement.setString(8, authorProfile.getPublished());
			preparedStatement.setInt(9, authorProfile.getViewsCount());
			preparedStatement.setInt(10, authorProfile.getSubscriberCount());
			preparedStatement.setInt(11, authorProfile.getVideoWatchCount());			
			
			preparedStatement.executeUpdate();
			
			
			conn.close();
		} catch (SQLException e1) {
			e1.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	public static void insertAuthorSubscriptions(uTubeAuthor authorProfile){
		PreparedStatement preparedStatement = null;
		try {
			Connection conn = getDBConnection();			
			ArrayList<uTubeAuthor> list = authorProfile.getListOf_SubscribedTo_Authors();
			Iterator<uTubeAuthor> iterator = list.iterator();
			while(iterator.hasNext()){
				uTubeAuthor subscribedToAuthor = iterator.next();				
				String insertAuthorSubsRecord = 				
						"INSERT INTO utubeauthorsubs (authorid, subscribedToAuthorID)" +
						"VALUES (?,?)";	
				preparedStatement = conn.prepareStatement(insertAuthorSubsRecord);
				preparedStatement.setString(1, authorProfile.getAuthorID());
				preparedStatement.setString(2, subscribedToAuthor.getAuthorID());
				
				preparedStatement.executeUpdate();
			}			
			conn.close();
		} catch (SQLException e1) {
			e1.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}		
	}
}

