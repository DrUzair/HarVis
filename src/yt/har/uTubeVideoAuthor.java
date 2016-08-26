package yt.har;
import java.util.HashMap;

public class uTubeVideoAuthor extends uTubeAuthor{	
	public uTubeVideoAuthor(String id){		
		super(id);
		videosMap = new HashMap<String, uTubeVideoData>();
	}	
	public HashMap<String,uTubeVideoData> videosMap;
	public int getCommentersCount(){
		int x = 0;
		for( String key : videosMap.keySet()){
			x += videosMap.get(key).getCommentersCount();
		}
		return x;
	}
	public String toString() {
		return super.authorID;
	}
	public int hashCode() {
		return this.toString().hashCode();
	}
	public boolean equals(Object videoAuthor) {
		if ( videoAuthor == null ) 
			return false;
		if ( this.getClass() != videoAuthor.getClass() ) 
			return false;		
		String name = ((uTubeVideoAuthor)videoAuthor).toString(); 
		
		return this.toString().equals(name);
	} 
}
