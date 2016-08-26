package yt.har;
import java.sql.Date;

public class uTubeVideoComment {
		private uTubeVideoData video; public void setvideo(uTubeVideoData video){ this.video = video;} public uTubeVideoData getVideo(){return video;} 
		private String Author; public String getAuthor(){return Author;} public void setAuthor(String author){Author = author;}
		private Date CreatedOn;  public Date getCreatedOn(){return CreatedOn;} public void setCreatedOn(Date createdOn){CreatedOn = createdOn;}
		private String Content; public String getContent(){return Content;} public void setContent(String content){this.Content = content;}		 
		private String authorEmail;
		private String authorUri;
		private String authorLanguage;
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
}
