package yt.har;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class uTubeAuthor {
	public ArrayList<uTubeAuthor> getListOf_SubscribedTo_Authors() {
		return listOf_SubscribedTo_Authors;
	}
	private ArrayList<uTubeAuthor> listOf_SubscribedTo_Authors = new ArrayList<uTubeAuthor>();
	
	// Source:250513 https://developers.google.com/youtube/2.0/reference#youtube_data_api_tag_yt:statistics

	private String authorname 		= "";		// YouTube User 
	private String location			= "";
	private String firstName		= "";
	private String lastName			= "";	
	private String author_title 	= "";
	protected String authorID		= "";		// GD User ID
	private String xmlProfileData 	= "";
	private String content			= "";
	
	private int totalUploadViews 	= 0;		// An accumulated total of all the views on all videos
	private int viewsCount 			= 0;		// Deprecated. When the viewCount attribute refers to a user profile (channel), the attribute specifies the number of times that the user's profile has been viewed.
	private int videoWatchCount 	= 0;		// Specifies the number of videos that a user has watched on YouTube
	private int subscriberCount 	= 0;		// Specifies the number of YouTube users who have subscribed to a particular user's YouTube channel
	private String published 		= null;
    public uTubeAuthor(String authorID)	{
    	this.authorID  = authorID;
    	videosMap = new HashMap<String, uTubeVideoData>();
    }    
    public void parseSubscriptions() throws IOException, ParserConfigurationException, SAXException{
    	URL authorSubscriptionsURL;				
		authorSubscriptionsURL = new URL("http://gdata.youtube.com/feeds/api/users/" + authorID +"/subscriptions?v=2");	        
		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		final DocumentBuilder builder = factory.newDocumentBuilder();
		final Document document = builder.parse(authorSubscriptionsURL.openStream());	

		if 	(document.getElementsByTagName("entry").getLength() > 0){
			NodeList subscriptionsList = document.getElementsByTagName("entry");

			for ( int subscriptionIndex = 0 ; subscriptionIndex < subscriptionsList.getLength() ; subscriptionIndex++){
				Node subscription = document.getElementsByTagName("entry").item(subscriptionIndex);
				if 	(((Element)subscription).getElementsByTagName("yt:username").getLength() > 0){
					String strSubscribedToAuthorName = ((Element)subscription).getElementsByTagName("yt:username").item(0).getTextContent();
					uTubeAuthor subscribedToAuthor 	= new uTubeAuthor(strSubscribedToAuthorName);
					listOf_SubscribedTo_Authors.add(subscribedToAuthor);
					//subscribedToAuthor.parseAuthorProfile(); Delay until ...
				}	        			        	
			}	        	
		}	       	       
    }
    public String parseAuthorProfile() 
    		throws IOException, MalformedURLException, ParserConfigurationException, SAXException   {
		URL authorProfileURL;
		String yt_username = "";		
			authorProfileURL = new URL("http://gdata.youtube.com/feeds/api/users/" + authorID);			
			BufferedReader in = new BufferedReader(new InputStreamReader(authorProfileURL.openStream()));
	        String inputLine = "";
	        String str ="";
	        do{
	        	str = in.readLine();	        	
	        	if(str != null) inputLine += str + "\n";
	        }while (str != null);
	        in.close();
	        xmlProfileData 	= inputLine;
	        
	        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	        final DocumentBuilder builder = factory.newDocumentBuilder();
	        final Document document = builder.parse(authorProfileURL.openStream());	          
	        
	        if 	(document.getElementsByTagName("yt:username").getLength() > 0)
	        	authorname 			= (document.getElementsByTagName("yt:username").item(0).getTextContent());
	        if 	(document.getElementsByTagName("yt:location").getLength() > 0)
	        	location 			= (document.getElementsByTagName("yt:location").item(0).getTextContent());
	        if	(document.getElementsByTagName("yt:firstName").getLength() > 0)
	        	firstName 			= (document.getElementsByTagName("yt:firstName").item(0).getTextContent());
	        if	(document.getElementsByTagName("yt:lastName").getLength() > 0)
	        	lastName			= (document.getElementsByTagName("yt:lastName").item(0).getTextContent());
	        if	(document.getElementsByTagName("title").getLength() > 0)
	        	author_title 		= (document.getElementsByTagName("title").item(0).getTextContent());
	        if	(document.getElementsByTagName("content").getLength() > 0)
	        	content 			= (document.getElementsByTagName("content").item(0).getTextContent());
	        /* <yt:statistics 
	         * 	totalUploadViews="3289171" 
	         * 	viewCount="146314" 
	         * 	videoWatchCount="0" 
	         * 	subscriberCount="8357" 
	         * 	lastWebAccess="1970-01-01T00:00:00.000Z"/> 
	         * */
	        if	(document.getElementsByTagName("yt:statistics").getLength() > 0){
	        	Element ytStatsElement = (Element) document.getElementsByTagName("yt:statistics").item(0);	
	        	try{
	        	totalUploadViews	= Integer.parseInt(ytStatsElement.getAttribute("totalUploadViews"));
	        	viewsCount 			= Integer.parseInt(ytStatsElement.getAttribute("totalUploadViews"));
	        	videoWatchCount 	= Integer.parseInt(ytStatsElement.getAttribute("videoWatchCount"));
	        	subscriberCount 	= Integer.parseInt(ytStatsElement.getAttribute("subscriberCount"));
	        	}catch(NumberFormatException e){
	        		e.getMessage();
	        	}
	        }
	        //	<published>2006-11-22T00:09:26.000Z</published>
	        if	(document.getElementsByTagName("published").getLength() > 0)
	        	published 			= document.getElementsByTagName("published").item(0).getTextContent();
		return yt_username;
	}
    public String getAuthorName() {
		return authorname;
	}
	public String getLocation() {
		return location;
	}
	public String getFirstName() {
		return firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public String getAuthor_title() {
		return author_title;
	}
	public String getXmlProfileData() {
		return xmlProfileData;
	}
	public String getAuthorname() {
		return authorname;
	}
	public String getContent() {
		return content;
	}
	public int getViewsCount() {
		return viewsCount;
	}
	public int getVideoWatchCount() {
		return videoWatchCount;
	}
	public int getSubscriberCount() {
		return subscriberCount;
	}
	public String getPublished() {
		return published;
	}
	public String getAuthorID() {
		return authorID;
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
		return authorID;
	}
	public int hashCode() {
		return this.toString().hashCode();
	}
	public boolean equals(Object videoAuthor) {
		if ( videoAuthor == null ) 
			return false;
		if ( this.getClass() != videoAuthor.getClass() ) 
			return false;		
		String name = ((uTubeAuthor)videoAuthor).toString(); 
		
		return this.toString().equals(name);
	}
	public int getTotalUploadViews() {
		return totalUploadViews;
	}	
}
