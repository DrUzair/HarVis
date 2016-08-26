package yt.har;

import java.util.HashMap;

public class Word implements Comparable<Word>{
	public String word; 
	HashMap<String, String> authors = new HashMap<String, String>();
	public int wordCount;
	public int authorCount;
	public boolean isQueryKeyWord = false;
	public HashMap<String, Word> coWords = new HashMap<String, Word>();	 
	public void addAuthor(String author){
		if (authors.get(author) == null){
			authors.put(author, author);
			authorCount = authors.keySet().size();
		}
	}
	
	public Word(String word, int wordCount) {
	    this.word = word; 
	    this.wordCount = wordCount; 
  }

	@Override
	public int compareTo(Word wordObj) {
		// TODO Auto-generated method stub
		return (this.wordCount > wordObj.wordCount ? -1 : (wordObj.wordCount == this.wordCount ? 0 : 1)); 
	}
}

