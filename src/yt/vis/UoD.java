package yt.vis;

import java.util.ArrayList;

public class UoD {
	Word word;
	ArrayList<Word> coWords;
	ArrayList<String> authorsList;
	Word getWord(){return word;}
	ArrayList<Word> getCoWordsList(){
		return coWords;
	}
	public UoD(Word word, ArrayList<Word> coWordsList, ArrayList<String> authorsList ){
		this.word = word;
		this.coWords = coWordsList;
		this.authorsList = authorsList;
	}
	public ArrayList<String> getAuthorsList(){ return this.authorsList;}	
}
class Word{
	String word;
	int		count;
}
