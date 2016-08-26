package yt;

import yt.har.Word;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

public class BayesClassifier {
	boolean filterStopWords = false;
	private String words_2b_filtered = "";
	private final String delim = " \n\t.,:;?؟`~!@#$%^&*+-=_/|{}()[]<>\"\'1234567890";	
	
	
	Map<String, Map<String, Word>> trainingData = new HashMap<String, Map<String, Word>>();
	Map<String, ArrayList<String>> textCategoryMap = new HashMap<String, ArrayList<String>>();
	Map<String, String> uniqueWordsMap = new HashMap<String, String>();
	void addTextToCategory(String categoryName, String text){
		if (textCategoryMap.containsKey(categoryName))
			textCategoryMap.get(categoryName).add(text);
		else{
			ArrayList<String> categoryTextsList = new ArrayList<String>();
			categoryTextsList.add(text);
			textCategoryMap.put(categoryName, categoryTextsList);
		}
	}	
	private float getPriorProbability(String categoryName){		
		int categoryTextsCount = 0;
		if (textCategoryMap.containsKey(categoryName)){
			categoryTextsCount = textCategoryMap.get(categoryName).size();
		}
		return (categoryTextsCount*1.0F)/(getTotalTextsCount()*1.0F);
	}
	private int getTotalTextsCount(){
		Set<String> categories = textCategoryMap.keySet();
		Iterator<String> categoryIterator = categories.iterator();
		int totalInstances = 0;
		while(categoryIterator.hasNext()){
			totalInstances += textCategoryMap.get(categoryIterator.next()).size();
		}
		return totalInstances;
	}
	private int getTotalUniqueWords(){
		return uniqueWordsMap.size();
	}
	private float getConditionalProbability(String GivenWord, String categoryName){
		/**
		 * P(ThisWord | Category) =  
		 * (ThisWord_WordCount_In_Category + 1 ) 
		 * (Sum(AllWords_WordCount_In_Category) + TotalWords_In_AllCategories)
		*/
		
		Map<String, Word> categoryWordsMap = trainingData.get(categoryName);
		float x = 0.0F;
		if (categoryWordsMap.containsKey(GivenWord))
			x = ((categoryWordsMap.get(GivenWord).wordCount+1)*1.0F);
		else 
			x = 1.0F;
		
		Collection<Word> wordsCollection = categoryWordsMap.values();
		Iterator<Word> wordsIterator = wordsCollection.iterator();
		int allWords_WordCount = 0;
		while(wordsIterator.hasNext()){
			Word word = wordsIterator.next();
			allWords_WordCount += word.wordCount;
		}		
		float y = (allWords_WordCount + getTotalUniqueWords())*1.0F;
		
		float condProb = x/y; 
		
		return condProb;
	}	
	public void prepareClassifier(){
		Set<String> categoriesSet = textCategoryMap.keySet();
		Iterator<String> categoriesIterator = categoriesSet.iterator();
		while(categoriesIterator.hasNext()){
			String categoryName = categoriesIterator.next();
			ArrayList<String> categoryTextsList = textCategoryMap.get(categoryName);
			Map<String, Word> categoryWordsMap = new HashMap<String, Word>();
			for (int i = 0 ; i < categoryTextsList.size() ; i ++){
				text2WordsFx(categoryTextsList.get(i), categoryWordsMap);
			}
			if (trainingData.containsKey(categoryName)){
				trainingData.remove(categoryName);				
				trainingData.put(categoryName, categoryWordsMap);
			}else{
				trainingData.put(categoryName, categoryWordsMap);
			}
		}
	}	
	public String classify(String text){
		ArrayList<String> listOfWords = text2Words(text);
		Set<String> categoriesSet = textCategoryMap.keySet();
		Iterator<String> categoriesIterator = categoriesSet.iterator();
		double posteriorProbs [] = new double[categoriesSet.size()];
		double maxPosteriorProb = 0;
		String estimatedCategory = "";
		int categoryIndex = 0;
		while(categoriesIterator.hasNext()){
			String categoryName = categoriesIterator.next();
			float priorProb = getPriorProbability(categoryName);			
			float multiplicationOfCondProbs = 1.0F;
//			for(int i = 0 ; i < listOfWords.size() ; i ++){
//				float condProb = getConditionalProbability(listOfWords.get(i).toString(), categoryName);
//				multiplicationOfCondProbs *= condProb;
//			}
			for(int i = 0 ; i < listOfWords.size() ; i ++){
				float condProb = getConditionalProbability(listOfWords.get(i).toString(), categoryName);
				multiplicationOfCondProbs += Math.log(condProb);
			}
//			float posteriorProb = priorProb * multiplicationOfCondProbs;
			double posteriorProb = Math.log(priorProb) + multiplicationOfCondProbs;
			System.out.println( categoryName + " : " + posteriorProb);
			posteriorProbs[categoryIndex] = posteriorProb;
			if (categoryIndex == 0){
				maxPosteriorProb = posteriorProb;
				estimatedCategory = categoryName;
			}
			else{
				if (posteriorProb > maxPosteriorProb){
					maxPosteriorProb = posteriorProb;
					estimatedCategory = categoryName;
				}
			}
			categoryIndex++;	
		}		
		return estimatedCategory;
	}
	private void text2WordsFx(String text, Map<String, Word> wordsMap){		
		String strWord;
		Word newWord;		
		text  = text.replaceAll("[^\\u0000-\\uFFFF]",""); // Replace non-alphanumeric characters
		StringTokenizer st = new StringTokenizer(text, delim);		
		while (st.hasMoreTokens()) {
			strWord = st.nextToken().toLowerCase();	
			if (filterStopWords){
				if (isfilterWord(strWord))
					continue;
			}
			if (strWord.length() > 2){
				newWord = (Word) wordsMap.get(strWord); 
				if (newWord == null) {
					newWord = new Word(strWord, 1);					
					wordsMap.put(strWord, newWord); 
				} else {					
					newWord.wordCount++;						
				}				
				if (uniqueWordsMap.containsKey(strWord) == false)
					uniqueWordsMap.put(strWord, strWord);
			}
		}		
	}
	private ArrayList<String> text2Words(String text){		
		String strWord;
		ArrayList<String> listOfWords = new ArrayList<String>();				
		text  = text.replaceAll("[^\\u0000-\\uFFFF]",""); // Replace non-alphanumeric characters
		StringTokenizer st = new StringTokenizer(text, delim); 
		while (st.hasMoreTokens()) {
			strWord = st.nextToken().toLowerCase();
			if (filterStopWords){
				if (isfilterWord(strWord))
					continue;
			}
			if (strWord.length() > 2){
				listOfWords.add(strWord);						
			}
		}		
		return listOfWords;
	}
	
	public static void main(String args[]){		
		BayesClassifier classifier = new BayesClassifier();		
		System.out.println("x");
		classifier.loadFilterWords();
		classifier.loadTrainingData("D:\\Dev\\Weka\\Zain\\NOT_ZAIN_Training_Data_363.csv", "NOT_ZAIN");
		classifier.loadTrainingData("D:\\Dev\\Weka\\Zain\\ZAIN_TITLE_TRAINING_DATA_50.csv", "ZAIN");
		// Prepare
		classifier.prepareClassifier();
		// Test
		//classifier.testClassifier("D:\\Dev\\Weka\\Zain\\ZAIN_Test_Data.csv", "D:\\Dev\\Weka\\Zain\\Zain_Results.csv");
		classifier.testClassifier("D:\\Dev\\Weka\\Zain\\NOT_ZAIN_BothTokensMissing_Test.csv", "D:\\Dev\\Weka\\Zain\\NOT_Zain_Results.csv");	
	}
	
	void loadTrainingData(String fileName, String categoryName){
		try{
			BufferedReader bfReader = new BufferedReader(new FileReader(fileName));
			String text = bfReader.readLine();
			while (text != null){
				addTextToCategory(categoryName, text);
				text = bfReader.readLine();
			}
			bfReader.close();
		}catch(IOException io){
			io.printStackTrace();
		}
	}
	void testClassifier(String testDataFileName, String resultsDataFileName){
		String results = "";					
		try {
			BufferedReader bfReader = new BufferedReader(new FileReader(testDataFileName));
			String text = bfReader.readLine();
			while (text != null){				
				results += classify(text) +"\n";
				text = bfReader.readLine();
			}
			bfReader.close();
			System.out.println(results);
			BufferedWriter bfWriter = new BufferedWriter(new FileWriter(resultsDataFileName));
			bfWriter.write(results);
			bfWriter.flush();
			bfWriter.close();
		}catch(IOException io){
			io.printStackTrace();
		}
	}
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
	private void loadFilterWords(){
		System.out.println("\t --> In UoD_NetGraph.loadFilterWords()");
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
}
