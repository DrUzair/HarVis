package yt.har;
import java.net.URL;

public class uTubeVideoQuery extends uTubeQuery{
	String videoID;
	public String getVideoID(){
		return videoID;
	}
	public uTubeVideoQuery(URL url, String videoID) {
		super(url);
		this.videoID = videoID;
	}
	public String getFullUrl(){		
		URL url = this.getUrl();
		String urlString = url.getProtocol() +"://" + url.getHost() + url.getPath() + videoID +"?v=2";		
		return urlString;
	}
}