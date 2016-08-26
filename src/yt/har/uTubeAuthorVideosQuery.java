package yt.har;
import java.net.URL;

public class uTubeAuthorVideosQuery extends uTubeQuery{
	uTubeAuthor authorObj;
	public String getAuthor(){
		return authorObj.getAuthorID();
	}
	public uTubeAuthorVideosQuery(URL url, uTubeAuthor authorObj) {
		super(url);
		this.authorObj = authorObj;
	}
	public String getFullUrl(){		
		URL url = this.getUrl();
		String urlString = url.getProtocol() +"://" + url.getHost() + url.getPath() +authorObj.getAuthorID() +"/uploads?" +url.getQuery() ;		
		return urlString;
	}
}