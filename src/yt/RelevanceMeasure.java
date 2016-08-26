package yt;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.sound.sampled.DataLine;

import yt.har.uTubeDataManager;

public class RelevanceMeasure {
	uTubeDataManager dbManager = new uTubeDataManager();
	Connection conn;
	int totalWordsCount = 0;
	int totalWordCoOccurencesCount = 0;
	String uod_table = "uod_titles";
	String dbUser = "uTube";
	String dbPassword = "uTubePWD";
	String dbSchema = "stc_unfiltered_unevolved1";//"zain_unfiltered_unevolved_2";//"stc_unfiltered_unevolved1";	
	int minWordCountLimit = 0;
	int minCoOccurCountLimit = 0;
	ArrayList<String> keyWordsList = new ArrayList<String>();
	public static void main(String[] args){
		RelevanceMeasure rm = new RelevanceMeasure();
		// get DB Conncection
		try {
			rm.getDBConnection();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return;
		} catch (SQLException e) {			
			e.printStackTrace();
			return;
		}
		
		rm.getTotalWordCountFromDB();
		rm.getTotalWordCoOccurencesFromDB();
		rm.loadFilterWords();
		// Load Data into dataList
		//String fileAddress = "D:\\Dev\\Weka\\synthetic\\SyntheticData3.csv"; 
		//String fileAddress = "D:\\Dev\\Weka\\Zain\\OnlyOneTokenPresent\\Zain_OneTokenPresentRelevant.csv";
		String fileAddress = "D:\\Dev\\Weka\\stc\\OnlyOneTokenPresent\\STC_OneTokenPresentRelevant.csv";
		rm.loadDataFromFile(fileAddress);
		//
		// for each Data Item
		String results = "";
		rm.keyWordsList.add("stc");
		rm.keyWordsList.add("saudi");
//		rm.keyWordsList.add("تصالات");
//		rm.keyWordsList.add("زين");
		for( int i = 0 ; i < 15 ; i ++){
			String text = rm.dataList.get(i);
			// Measure relevance
			System.out.println("\n\n Given Text: "+ text);
			
			ArrayList<String> wordsList = rm.text2UniqueWords(rm.dataList.get(i));		
			
			float uod_Net_Weight = rm.calcUoD_NetworkWeight(wordsList);
			float keyWordsPresenceWeight = 0;//rm.getKeyWordsPresenceWeight(wordsList);	
			float relevance = (keyWordsPresenceWeight + (uod_Net_Weight));		
			results += text + "," + keyWordsPresenceWeight + "," +uod_Net_Weight + "," + relevance +"\n";						
		}
		try {
			rm.conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		BufferedWriter bfWriter;
		try {
			//fileAddress = "D:\\Dev\\Weka\\synthetic\\SyntheticData3Results.csv";
			//fileAddress = "D:\\Dev\\Weka\\Zain\\OnlyOneTokenPresent\\Zain_OneTokenPresentRelevantResults.csv";
			fileAddress = "D:\\Dev\\Weka\\stc\\OnlyOneTokenPresent\\STC_OneTokenPresentRelevantResults.csv";	 
			bfWriter = new BufferedWriter(new FileWriter(fileAddress));
					
			bfWriter.write(results);
			bfWriter.flush();
			bfWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}	
	float calcRelationshipWeight(String keyWord, String textWord){
		System.out.println("\n Relationship Aspect");
		System.out.println("\t keyWord: " + keyWord  +" \t textWord: " + textWord);
		ArrayList<String> coWordsList1 = getCoWordsList(keyWord);
		ArrayList<String> coWordsList2 = getCoWordsList(textWord);
		ArrayList<String> commonWordsList = new ArrayList<String>();
		for (int i = 0 ; i < coWordsList1.size() ; i ++){			
			for (int j = 0 ; j < coWordsList2.size() ; j ++){
				if(coWordsList1.get(i).equalsIgnoreCase(coWordsList2.get(j))){
					commonWordsList.add(coWordsList1.get(i));
				}
			}
		}
		if (commonWordsList.size() < 1)
			return 0;
		float relationshipWeightage = 0.0F;
		for(int i = 0 ; i < commonWordsList.size() ; i ++){
			float f = calCoOccurenceWeight(keyWord, commonWordsList.get(i))+
					  calCoOccurenceWeight(textWord, commonWordsList.get(i));
					//getWordCountFromDB(commonWordsList.get(i))/(totalWordsCount*1.0F);
			relationshipWeightage += f;	
			System.out.println("\t CommonWord : " + commonWordsList.get(i) + " Weight : " + f);
		}		
		return relationshipWeightage;
	}
	int getTotalWordCountFromDB(){		
		try {			
			Statement wordCountQuery = conn.createStatement();
			String sqlStatement = "Select Word, WordCount from " + uod_table + " GROUP BY Word"; 					
			ResultSet wordCountQueryResultSet = wordCountQuery.executeQuery(sqlStatement);
			while(wordCountQueryResultSet.next()){
				if(wordCountQueryResultSet.getInt("WordCount") >= minWordCountLimit)
					totalWordsCount += wordCountQueryResultSet.getInt("WordCount");
			}
			// CoWords(Leaves of UoD Graph)
			sqlStatement = "Select DISTINCT(CoWord), CoWordCount from " + uod_table + 
			" WHERE CoWord NOT IN (SELECT DISTINCT(Word) from uod_titles);"; 					
			wordCountQueryResultSet = wordCountQuery.executeQuery(sqlStatement);
			while(wordCountQueryResultSet.next()){
				if ( wordCountQueryResultSet.getInt("CoWordCount") >= minWordCountLimit)
					totalWordsCount += wordCountQueryResultSet.getInt("CoWordCount");
			}
		} catch (SQLException e) {			
			e.printStackTrace();
		}
		return totalWordsCount;
	}
	int getTotalWordCoOccurencesFromDB(){		
		try {			
			Statement wordCountQuery = conn.createStatement();
			String sqlStatement = "Select SUM(CoOccurenceCount) from " + uod_table; 					
			ResultSet wordCountQueryResultSet = wordCountQuery.executeQuery(sqlStatement);
			while(wordCountQueryResultSet.next()){
				if (wordCountQueryResultSet.getInt(1) >= minCoOccurCountLimit)
					totalWordCoOccurencesCount = wordCountQueryResultSet.getInt(1);
			}			
		} catch (SQLException e) {			
			e.printStackTrace();
		}
		return totalWordCoOccurencesCount;
	}
	float getKeyWordsPresenceWeight(ArrayList<String> wordsList){
		float KeyWordsPresenceWeight = 0;
		System.out.println("\nKey Words Presence Weight Aspect");
		int keyWordPresentCount = 0;
		for (int i = 0 ; i < keyWordsList.size(); i ++){
			String keyWord = keyWordsList.get(i);
			if (wordsList.contains(keyWord)){
				KeyWordsPresenceWeight += getWordCountFromDB(keyWord)/(totalWordsCount*1.0F);
			}
		}		
		return KeyWordsPresenceWeight;//
	}
	float getMatchingWordsWeight(ArrayList<String> wordsList){
		float matchingWordsWeight = 0;
		int matchingWordsCount = 0;
		int notMatchingWordsCount = 0;
		System.out.println("\nMatching Words Aspect");
		ArrayList<String> coWordsList = new ArrayList<String>();
		for (int i = 0 ; i < wordsList.size(); i ++){
			String word = wordsList.get(i);
			System.out.print("\n\t word: \t" +  word);
			int wordCount = getWordCountFromDB(word);
			if (wordCount != 0){
				matchingWordsCount++;
				coWordsList.add(word);
				float wordWeight = ((wordCount*1.0F)/(totalWordsCount*1.0F));
				System.out.print("\t weight: " +  wordWeight);
				matchingWordsWeight += wordWeight;
			}
			else
				notMatchingWordsCount++;						
		}
		// matchingWeight = (matchin-notmatching)/(matching+notmatching) [-1,1]
		//float matchingWeight = 
		//((matchingWordsCount-notMatchingWordsCount)*1.0F)/((matchingWordsCount+notMatchingWordsCount)*1.0F);
		// squeez range to [0,1]
		//matchingWeight = (matchingWeight-(-1))/(1-(-1));
		//System.out.println("\n\n \t matchingWeight: " + matchingWeight + " \t matchingWordsWeight: " + matchingWordsWeight);
		// matchingWeight is not a penalty but an advantage
		return matchingWordsWeight;//
	}
	float calcUoD_NetworkWeight(ArrayList<String> wordsList){
		float uod_Net_Weight = 0.0F;
		float CoOccurenceWeight = 0.0F;
		float RelationshipWeight = 0.0F;
		for(int i = 0 ; i < keyWordsList.size() ; i ++){
			String keyWord = keyWordsList.get(i);	
			//if (wordsList.contains(keyWord))
				//RelationshipWeight += getWordCountFromDB(keyWord)/(totalWordsCount*1.0F);
			// Having a keyWord in wordsList increases RelationshipWeight  

			//	Common-Friends in Friends of keyWord and wordsList words
			for (int j = 0; j < wordsList.size() ; j ++){				
				String testWord = wordsList.get(j);	
				if (keyWordsList.contains(testWord))
					continue;
				//CoOccurenceWeight += calCoOccurenceWeight(keyWord, testWord);
				RelationshipWeight += calcRelationshipWeight(keyWord, testWord);				
			}
		}
		uod_Net_Weight += RelationshipWeight; 
		return uod_Net_Weight;
	}
	float calCoOccurenceWeight(String word, String coWord){
		float coOccurenceWeight = 0.0F;		
		int coOccurrenceCount = 0;
		int wordCount = 0 ;
		int coWordCount = 0;
		System.out.println("\n CoOccurence With Keywords Aspect");
		System.out.println("\t word: " + word  +" \t coWord: " + coWord);
		try {
			Statement coOccurenceCountQuery = conn.createStatement();
			String sqlStatement = 
					" Select CoOccurenceCount from " + uod_table +
					" WHERE (Word='"+word+"' AND CoWord='"+coWord+"')" +
					" OR (CoWord='"+word+"' AND Word='"+coWord+"')"; 	
			ResultSet coOccurenceCountQueryResultSet = coOccurenceCountQuery.executeQuery(sqlStatement);					
			if(coOccurenceCountQueryResultSet.next()){
				coOccurrenceCount = coOccurenceCountQueryResultSet.getInt("CoOccurenceCount");				
				wordCount = getWordCountFromDB(word);
				coWordCount = getWordCountFromDB(coWord);
				
				// L*(w1+w2)				
				float w1_plus_w2 = ((wordCount*1.0F)/(totalWordsCount*1.0F))+((coWordCount*1.0F)/(totalWordsCount*1.0F));
				float L = (coOccurrenceCount*1.0F/totalWordCoOccurencesCount*1.0F);
				coOccurenceWeight = (w1_plus_w2)*L;				
//				System.out.println("\t wordCount: " + wordCount  +" \t Weightage: " + (wordCount*1.0F)/(totalWordsCount*1.0F));
//				System.out.println("\t coWordCount: " + coWordCount +" \t Weightage: " + (coWordCount*1.0F)/(totalWordsCount*1.0F));
//				System.out.println("\t coOccurCount: " + coOccurrenceCount + " \t Weitghtage: " + coOccurenceWeight);
			}				
		} catch (SQLException e) {			
			e.printStackTrace();
		}			
		return coOccurenceWeight;
	}
	int getWordCountFromDB(String word){
		int wordCount = 0;
		try {			
			Statement wordCountQuery = conn.createStatement();
			String sqlStatement = "Select WordCount from " + uod_table + " WHERE Word='"+word+"'";
			ResultSet wordCountQueryResultSet = wordCountQuery.executeQuery(sqlStatement);
			if(wordCountQueryResultSet.next()){
				wordCount = wordCountQueryResultSet.getInt("WordCount");
			}else{
				//  Try UoD_Net graph leaves (CoWords with no further CoWords)
				sqlStatement = "Select CoWordCount from " + uod_table + " WHERE CoWord='"+word+"'";
				wordCountQueryResultSet = wordCountQuery.executeQuery(sqlStatement);
				if(wordCountQueryResultSet.next()){
					wordCount = wordCountQueryResultSet.getInt("CoWordCount");
				}
			}
			//			if (wordCount == 0){
//				// Try UoD_Net graph leaves (CoWords with no further CoWords)
//				sqlStatement = "Select CoWordCount from " + uod_table + " WHERE CoWord='"+word+"'";
//				while(wordCountQueryResultSet.next()){
//					wordCount = wordCountQueryResultSet.getInt("CoWordCount");
//				}				
//			}
		} catch (SQLException e) {			
			e.printStackTrace();
		}
		return wordCount;
	}
	ArrayList<String> getCoWordsList(String word){
		ArrayList<String> coWordsList = new ArrayList<String>();
		try {			
			Statement coWordsQuery = conn.createStatement();
			ResultSet coWordsQueryResultSet = coWordsQuery.executeQuery(
					"Select CoWord from " + uod_table + " WHERE Word = '"+word+"'");
			while(coWordsQueryResultSet.next()){
				String coWord = coWordsQueryResultSet.getString("CoWord");
				if(keyWordsList.contains(coWord))
					continue;
				coWordsList.add(coWordsQueryResultSet.getString("CoWord"));
			}	
			coWordsQueryResultSet = coWordsQuery.executeQuery(
					"Select Word from " + uod_table + " WHERE CoWord = '"+word+"'");
			while(coWordsQueryResultSet.next()){
				String coWord = coWordsQueryResultSet.getString("Word");
				if(keyWordsList.contains(coWord))
					continue;
				coWordsList.add(coWordsQueryResultSet.getString("Word"));
			}
		} catch (SQLException e) {			
			e.printStackTrace();
		}
		return coWordsList;
	}
	private void getDBConnection() throws SQLException, ClassNotFoundException {
    	Class.forName("com.mysql.jdbc.Driver");    		
	   	Properties connectionProps = new Properties();
	    connectionProps.put("user", dbUser);
	    connectionProps.put("password", dbPassword);	    
	    String connString = "";
	    connString = "jdbc:mysql://localhost/"+dbSchema+"?useUnicode=true&amp;characterEncoding=UTF-8";
	    conn = DriverManager.getConnection(connString, connectionProps);    
	}
	ArrayList<String> dataList = new ArrayList<String>();
	private String words_2b_filtered = "";
	private final String delim = " \n\t.,:;?؟`~!@#$%^&*+-=_/|{}()[]<>\\\"\'1234567890";
	boolean isfilterWord(String strWord){
		StringTokenizer st = new StringTokenizer(words_2b_filtered, ",");		
		boolean skipWord = false;
		while (st.hasMoreTokens()) {			
			String strFilterWord = st.nextToken().toLowerCase();     	    				
			if (strWord.equalsIgnoreCase(strFilterWord)){ 	 	    					
				skipWord = true;
				break;	
			}
		}
		return skipWord;
	}
	void loadDataFromFile(String fileName){
		try{
			BufferedReader bfReader = new BufferedReader(new FileReader(fileName));
			String text = bfReader.readLine().trim();
			while (text != null){
				dataList.add(text);
				text = bfReader.readLine();
			}
			bfReader.close();
		}catch(IOException io){
			io.printStackTrace();
		}
	}
	private void loadFilterWords(){
		System.out.println("\t --> In loadFilterWords()");
		URL fileURL = HarVis.class.getResource("Resources/word_2b_filtered");
		words_2b_filtered = "";
		try {			
			FileReader fr = new FileReader(fileURL.getFile());
			BufferedReader br = new BufferedReader(fr);
			String line = br.readLine();
			words_2b_filtered += line;
			while(line != null){				
				line = br.readLine();
				words_2b_filtered += line;
			}
		} catch (FileNotFoundException e) {			
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private ArrayList<String> text2UniqueWords(String text){		
		String strWord;
		ArrayList<String> listOfWords = new ArrayList<String>();				
		text  = text.replaceAll("[^\\u0000-\\uFFFF]",""); // Replace non-alphanumeric characters
		StringTokenizer st = new StringTokenizer(text, delim); 
		while (st.hasMoreTokens()) {
			strWord = st.nextToken().toLowerCase().trim();
			if (isfilterWord(strWord))
				continue;
			boolean alreadyListed = false;
			for(int i = 0 ; i < listOfWords.size() ; i ++){
				if (listOfWords.get(i).equalsIgnoreCase(strWord)){
					alreadyListed = true;
				}					
			}
			if (! alreadyListed && strWord.length() > 1)
				listOfWords.add(strWord);						
			
		}		
		return listOfWords;
	}
}
