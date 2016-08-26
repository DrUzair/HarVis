package yt.har;

import java.awt.image.TileObserver;
import java.sql.Date;
import java.util.Vector;

public class uTubeVideoData {
	public int getSearchRound() {
		return searchRound;
	}
	public void setSearchRound(int searchRound) {
		this.searchRound = searchRound;
	}
	public String getAuthorEmail() {
		return authorEmail;
	}
	public void setAuthorEmail(String authorEmail) {
		this.authorEmail = authorEmail;
	}
	public String getAuthorUri() {
		return authorUri;
	}
	public void setAuthorUri(String authorUri) {
		this.authorUri = authorUri;
	}
	public String getAuthorLanguage() {
		return authorLanguage;
	}
	public void setAuthorLanguage(String authorLanguage) {
		this.authorLanguage = authorLanguage;
	}
		public uTubeVideoData(){
			this.comments = new Vector<uTubeVideoComment>(5,5);
			saved = false;
		}
		private String authorEmail;
		private String authorUri;
		private String authorLanguage;
		private int searchRound;
		private String Author; public String getAuthor(){return Author;} public void setAuthor(String author){Author = author;}
		private int CommentsCount; public int getCommentsCount(){return CommentsCount;} public void setCommentsCount(int commentsCount){CommentsCount = commentsCount;}
		/*CommentersCount is required to make AuthorsNet*/
		private int CommentersCount; public int getCommentersCount(){return CommentersCount;} public void setCommentersCount(int commentersCount){CommentersCount = commentersCount;}
		private Date CreatedOn;  public Date getCreatedOn(){return CreatedOn;} public void setCreatedOn(Date createdOn){CreatedOn = createdOn;}
		private String category; public String getCategory(){return category;} public void setCategory(String cat){category = cat;}
		private String Description; 
		public String getDescription(){
			return Description;
		} 
		public void setDescription(String description){Description = description;}
		private int Dislikes; public int getDislikesCount(){return Dislikes;} public void setDislikesCount(int dislikes){Dislikes = dislikes;}
		private float Duration; public float getDuration(){return Duration;} public void setDuration(float duration){Duration = duration;}
		private String ID; public String getVideoID(){return ID;} public void setVideoID(String id){ID = id;}
		private int Likes; public int getLikesCount(){return Likes;} public void setLikesCount(int likes){Likes = likes;}
		private String Location; public String getLocation(){return Location;} public void setLocation(String location){Location = location;}
		private float Rating; public float getRating(){return Rating;} public void setRating(float rating){Rating = rating;}
		private int RatersCount;
		public int getRatersCount() {
			return RatersCount;
		}
		public void setRatersCount(int ratersCount) {
			RatersCount = ratersCount;
		}
		public int getMinRate() {
			return MinRate;
		}
		public void setMinRate(int minRate) {
			MinRate = minRate;
		}
		public int getMaxRate() {
			return MaxRate;
		}
		public void setMaxRate(int maxRate) {
			MaxRate = maxRate;
		}
		public String getUserID() {
			return userID;
		}
		public void setUserID(String userID) {
			this.userID = userID;
		}
		private int MinRate;
		private int MaxRate;
		private String userID;
		private uTubeQuery queryObj; public uTubeQuery getQueryObject(){return queryObj;} public void setQueryObject(uTubeQuery query){queryObj = query;}
		private String Title; 
		public String getTitle(){
			if ( Title.length() > 199){
				Title = Title.substring(0, 199);
			}
			return Title;
		} 
		public void setTitle(String title){Title = title;}	
		private long viewCount; public long getViewCount(){return viewCount;} public void setViewCount(long viewCount){ this.viewCount = viewCount;}
		private long favoriteCount;public long getFavoriteCount(){return favoriteCount;} public void setFavoriteCount(long favCount){favoriteCount = favCount;}

		public Vector<uTubeVideoComment> comments;
		private boolean saved; void setSaved(){saved = true;} boolean isSaved(){return saved;}
		private String relatedVideosFeedUrl; public String getRelatedVideosFeedUrl(){return relatedVideosFeedUrl;} 
		public void setRelatedVideosFeedUrl(String relatedVideosFeedUrl){this.relatedVideosFeedUrl = relatedVideosFeedUrl;}
		private boolean relatedVideosFetched = false;
		public void setRelatedVideosFetched(){
			relatedVideosFetched = true;
		}
		public boolean getRelatedVideosFetched(){
			return relatedVideosFetched;
		}
		
}
