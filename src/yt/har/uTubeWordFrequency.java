package yt.har;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

public class uTubeWordFrequency {
	
	Map<String, WordCount> getWordFrequecyMap(String textBulk) {
		uTubeWordFrequency wordFreq = new uTubeWordFrequency();
		Map<String, WordCount> wordsMap = new HashMap<String, WordCount>(); 
		String delim = " \t\n.,:;?!-/()[]\"\'";
		int crIndex = textBulk.indexOf("\n");
		String line = textBulk.substring(0, crIndex); 
		textBulk = textBulk.substring(crIndex, textBulk.length()).trim(); 
		String word;	
		WordCount count; 
		Enumeration ignoreEnum;
		Vector<String> ignoreNames = new Vector<String>();
		ignoreNames.add("about"); ignoreNames.add("after"); ignoreNames.add("also");ignoreNames.add("after");
		ignoreNames.add("before");
		ignoreNames.add("could");
		ignoreNames.add("does"); ignoreNames.add("did"); ignoreNames.add("done"); ignoreNames.add("from"); ignoreNames.add("for"); ignoreNames.add("here"); ignoreNames.add("have");   
		ignoreNames.add("myself"); ignoreNames.add("even"); ignoreNames.add("it"); ignoreNames.add("with");ignoreNames.add("when"); ignoreNames.add("like"); ignoreNames.add("such");
		ignoreNames.add("just"); ignoreNames.add("should"); 
		ignoreNames.add("must");
		ignoreNames.add("same"); ignoreNames.add("some");  ignoreNames.add("some"); ignoreNames.add("shall");
		ignoreNames.add("that"); ignoreNames.add("they");ignoreNames.add("they"); ignoreNames.add("then");ignoreNames.add("them"); ignoreNames.add("those"); 
		ignoreNames.add("your");
		ignoreNames.add("will");ignoreNames.add("when"); ignoreNames.add("were"); ignoreNames.add("where"); 
		
		ignoreEnum = ignoreNames.elements();
		try {
			while (textBulk.indexOf("\n") != -1) {
				StringTokenizer st = new StringTokenizer(line, delim); 
				while (st.hasMoreTokens()) {
					word = st.nextToken().toLowerCase();
					if (word.length() >= 4 ){
						boolean skip = false;
						while (ignoreEnum.hasMoreElements()){
							if (((String)ignoreEnum.nextElement()).equals(word)){
								skip = true; 
								break; //break out of searching ignoreWords
							}
						}
						ignoreEnum = ignoreNames.elements();
						if (! skip){
							count = (WordCount) wordsMap.get(word); 
							if (count == null) {
								wordsMap.put(word, wordFreq.new WordCount(word, 1)); 
							} else {
								count.i++; 
							}
						}					
					}
				}				
				crIndex = textBulk.indexOf("\n");
//				if (crIndex == -1 ) 
//					break; // bulkText is finished. break out of the while loop.
				line = textBulk.substring(0, crIndex); 
				textBulk = textBulk.substring(crIndex, textBulk.length()).trim(); 
				
			} 
		} catch (Exception e) {
			e.printStackTrace();
		}
		return wordsMap;
	}
	class WordCount{
		public String word; 
		public int i; 
		public WordCount(String word, int i) {
		    this.word = word; 
		    this.i = i; 
	  }
	}
}
