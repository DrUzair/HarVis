package yt.har;
import java.net.URL;
import java.util.List;

import com.google.gdata.client.Query;
import com.google.gdata.client.youtube.YouTubeQuery;
public class uTubeQuery extends YouTubeQuery{
	public boolean isRelatedVideosNeeded() {
		return relatedVideosNeeded;
	}
	public void setRelatedVideosNeeded(boolean relatedVideosNeeded) {
		this.relatedVideosNeeded = relatedVideosNeeded;
	}
	private String queryString;
	private boolean checked;
	private boolean relatedVideosNeeded = false;
	
	protected boolean isChecked(){return checked;}
	protected void setChecked(){checked = true;}
	private int searchRound = 0;
	
	public int getSearchRound() {
		return searchRound;
	}
	public void setSearchRound(int searchRound) {
		this.searchRound = searchRound;
	}
	public String getQueryString(){
		return queryString;
	}
	public void setQueryString(String strQuery){
		queryString = strQuery;
	}
	
	public uTubeQuery(URL url){
		super(url);	
	} 
	
	public boolean equals(Object query){
		String queryString = ((uTubeKeyWordsQuery)query).getUrl().toString();
		String existingQuryString = this.getUrl().toString();
		if(queryString.equalsIgnoreCase(existingQuryString))
			return true;		
		else
			return false;		
	}
	public String getFullUrl(){		
		URL url = this.getUrl();
		String urlString = url.getProtocol() +"://" + url.getHost() + url.getPath() +"?" +url.getQuery() + "&v=2" ;		
		return urlString;
	}
}
