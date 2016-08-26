package yt.har;

import java.util.ArrayList;
import java.util.HashMap;

public class uTubeCommentAuthor extends uTubeAuthor{
	private int nVideosCommented = 0;		
	public uTubeCommentAuthor(String id){
		super(id);
		commentsMap = new HashMap<String, ArrayList<uTubeVideoComment>>();
	}
	public int getVideosCommented() {
		return nVideosCommented;
	}
	public void setVideosCommented(int nVideosCommented) {
		this.nVideosCommented = nVideosCommented;
	}
	public HashMap<String, ArrayList<uTubeVideoComment>> commentsMap;
}
