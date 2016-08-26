package yt.har;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import javax.swing.JTextArea;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.google.gdata.client.youtube.YouTubeService;
import com.google.gdata.data.Extension;
import com.google.gdata.data.youtube.UserEventEntry;
import com.google.gdata.data.youtube.UserEventFeed;
import com.google.gdata.data.youtube.UserProfileEntry;
import com.google.gdata.data.youtube.UserProfileFeed;
import com.google.gdata.data.youtube.YtUserProfileStatistics;
import com.google.gdata.model.atom.Category;
import com.google.gdata.model.atompub.Collection;
import com.google.gdata.util.ServiceException;

public class uTubeAuthorsCrawler extends Thread{
	protected static YouTubeService service;	
	JTextArea txtAreaOutput;
	public uTubeAuthorsCrawler(JTextArea txtAreaOutput) {
		this.txtAreaOutput = txtAreaOutput;
	}	
	public void run(){
		startDownloadingAuthorProfiles();
	}
	private void startDownloadingAuthorProfiles(){
		Iterator<String> videoAuthor_UserIDsIter = uTubeDataManager.getVideoAuthor_UserIDs().iterator();
		Vector<uTubeAuthor> existingAuthorsVector = uTubeDataManager.getVideoAuthorProfiles();
		while(videoAuthor_UserIDsIter.hasNext()){			
			String authorUserID = videoAuthor_UserIDsIter.next();
			uTubeAuthor authorProfile = new uTubeAuthor(authorUserID);
			if (existingAuthorsVector.contains(authorProfile) == true)
				continue;			// Author Profile is already downloaded, move to next.
			try {
				double d = Math.random()*20*1000;
				int sleepTime = (int)d;
				Thread.sleep(sleepTime);
				authorProfile.parseAuthorProfile();				
				txtAreaOutput.setText("");
				txtAreaOutput.append("User \t\t" + authorProfile.getAuthorID() + "\n" 
						+"Title \t\t" + authorProfile.getAuthor_title() + "\n"
						+"Name \t\t" + authorProfile.getAuthorName() + "\n"
						+"Location \t\t" + authorProfile.getLocation() + "\n"
						+"Published \t\t" + authorProfile.getPublished() + "\n"
						+"Subscribers Count \t" + authorProfile.getSubscriberCount() + "\n"
						+"Views Count \t" + authorProfile.getViewsCount() + "\n"						
						);						
				uTubeDataManager.insertAuthorProfile(authorProfile);
				Thread.sleep(sleepTime);
				authorProfile.parseSubscriptions();
				uTubeDataManager.insertAuthorSubscriptions(authorProfile);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			} catch (SAXException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}	
}
