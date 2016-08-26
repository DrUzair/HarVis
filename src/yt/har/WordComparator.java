package yt.har;

import java.util.Comparator;

public class WordComparator implements Comparator<Word>{
	@Override    public int compare(Word o1, Word o2) {		
		return (o1.wordCount>o2.wordCount ? -1 : (o1.wordCount==o2.wordCount ? 0 : 1));    
		}
}