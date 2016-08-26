package yt.har;

//import com.google.gdata.client.Query;
//import com.google.gdata.client.*;
//import com.google.gdata.client.youtube.*;
//import com.google.gdata.client.media.MediaService;
//import javax.mail.MessagingException;

//import com.google.gdata.client.*;
//import com.google.gdata.data.DateTime;
//import com.google.gdata.data.Feed;
//import com.google.gdata.data.Entry;
//import com.google.gdata.data.Person;
//import com.google.gdata.data.TextConstruct;
//import com.google.gdata.data.TextContent;
//import com.google.gdata.data.youtube.VideoFeed;
//import com.google.gdata.data.youtube.VideoEntry;
//import com.google.gdata.data.youtube.YtRating;
//import com.google.gdata.data.youtube.YtStatistics;
//import com.google.gdata.data.extensions.Comments;


import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;



import com.google.gdata.client.Query;
import com.google.gdata.client.youtube.YouTubeQuery;
import com.google.gdata.client.youtube.YouTubeService;
import com.google.gdata.data.Category;
import com.google.gdata.data.youtube.VideoEntry;
import com.google.gdata.data.youtube.VideoFeed;
import com.google.gdata.data.youtube.YouTubeNamespace;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.DisplayMode;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
//import java.net.URL;

// JDBC
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.List;
import java.util.Vector;


public class uTubeClient extends JFrame implements ActionListener{
	static final long serialVersionUID = 123456; 
	private Map<String, uTubeVideoData> videoDataMap = new HashMap<String, uTubeVideoData>();
	JLabel answer = new JLabel("");
	JPanel pane = new JPanel(); // create pane object
	
	JButton btnCrawluTube = new JButton("Crawl Youtube !");
	JButton btnFindKeyWords = new JButton("Universe of Discourse");
	JButton btnReload = new JButton("Reload Video Data");
	JButton btnStop = new JButton("Stop");
	
	protected JPanel mainPanel = new JPanel();
	protected JSplitPane mainSplitPane = new JSplitPane();
	protected JSplitPane splitPane4DMnCrawler;
	protected JSplitPane splitPane4TMnClient;
	
	protected JTextField textField = new JTextField(30);     
	
	protected JTextArea textAreaCrawler = new JTextArea(35,80);
	protected JPanel crawlerPanel = new JPanel();
	protected JScrollPane scrollPane4CrawlerTA = new JScrollPane(textAreaCrawler);
	
	protected JPanel dmPanel = new JPanel();
	protected JTextArea textAreaDM = new JTextArea(35,80);
	protected JScrollPane scrollPane4DMTextArea = new JScrollPane(textAreaDM);
	
	protected JPanel tmPanel = new JPanel();
	protected JTextArea textAreaTM = new JTextArea(35,80);
	protected JScrollPane scrollPane4TMTextArea = new JScrollPane(textAreaTM);
	
	protected JPanel clientPanel = new JPanel();
	protected JTextArea textAreaClient = new JTextArea(35,80);
	protected JScrollPane scrollPane4ClientTA = new JScrollPane(textAreaClient);	
	
	
	
	public Vector<uTubeVideoData> videoDataVector = new Vector<uTubeVideoData>(10, 10); 

	// Working threads
	public uTubeCrawler uTubeCrawler;
	public uTubeDataManager uTubeDM;
	public uTubeClientThreadsManager threadManager;
	// Thread control variables
	protected boolean updateReceived;
	protected boolean updateSaved;
	
	// JDBC Variables
	//private Connection dbConnection = null;
	public uTubeClient(){		
		super("uTube Harvester"); 
		setBounds(50, 50, 1200, 800);
	    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	    
	}
	
	
	void createGUI(){		
	    Container con = this.getContentPane(); // inherit main frame
	    
	    pane.add(textField);
	    pane.add(answer); 
	    pane.add(btnCrawluTube); 
	    btnCrawluTube.setMnemonic('C'); // associate hotkey
	    btnCrawluTube.addActionListener(this);   // register button listener
	    btnCrawluTube.requestFocus();
	    
	    pane.add(btnStop);
	    pane.setBounds(300,300, 1000, 800);
	    btnStop.setMnemonic('X');
	    btnStop.addActionListener(this);
	    
	    pane.add(btnFindKeyWords);
	    pane.setBounds(300,300, 1000, 800);
	    btnFindKeyWords.setMnemonic('F');
	    btnFindKeyWords.addActionListener(this);
	    
	    pane.add(btnReload);
	    pane.setBounds(300,300, 1000, 800);
	    btnReload.setMnemonic('R');
	    btnReload.addActionListener(this);
	    
	    scrollPane4CrawlerTA.setBounds(10,60,780,500); 
	    scrollPane4CrawlerTA.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS); 
	    crawlerPanel.setLayout(new BorderLayout());
	    crawlerPanel.add(new Label("YouTube Crawler Output"), BorderLayout.NORTH);
	    crawlerPanel.add(scrollPane4CrawlerTA, BorderLayout.CENTER);
	    
	    scrollPane4DMTextArea.setBounds(10, 60, 780, 500);
	    scrollPane4DMTextArea.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
	    dmPanel.setLayout(new BorderLayout());
	    dmPanel.add(new Label("Data Manger Output"), BorderLayout.NORTH);
	    dmPanel.add(scrollPane4DMTextArea, BorderLayout.CENTER);
	    
	    scrollPane4ClientTA.setBounds(10, 60, 780, 500);
	    scrollPane4ClientTA.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
	    clientPanel.setLayout(new BorderLayout());
	    clientPanel.add(new Label("YouTube Client Output"), BorderLayout.NORTH);
	    clientPanel.add(scrollPane4ClientTA, BorderLayout.CENTER);
	    
	    scrollPane4TMTextArea.setBounds(10, 60, 780, 500);
	    scrollPane4TMTextArea.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
	    tmPanel.setLayout(new BorderLayout());
	    tmPanel.add(new Label("Thread Manager Output"), BorderLayout.NORTH);
	    tmPanel.add(scrollPane4TMTextArea, BorderLayout.CENTER);
	    
	    splitPane4DMnCrawler = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, crawlerPanel, dmPanel);
	    splitPane4DMnCrawler.setOneTouchExpandable(true);
	    splitPane4DMnCrawler.setDividerLocation(600);
	    splitPane4DMnCrawler.setBounds(100,100, 400,400);
	    
	    splitPane4TMnClient = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tmPanel, clientPanel);
	    splitPane4TMnClient.setOneTouchExpandable(true);
	    splitPane4TMnClient.setDividerLocation(600);
	    splitPane4TMnClient.setBounds(100,100, 400,400);
	    
	    mainSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, splitPane4TMnClient, splitPane4DMnCrawler);
	    mainSplitPane.setOneTouchExpandable(true);
	    mainSplitPane.setDividerLocation(350);
	    mainSplitPane.setBounds(100,100, 800,800);
	    
	    mainPanel.add(mainSplitPane);
	    
	    con.setLayout(new BorderLayout());
	    con.add(pane, BorderLayout.NORTH);	    
	    con.add(mainSplitPane, BorderLayout.CENTER);   
	    setVisible(true); // make frame visible
	}
	void setCrawlerUpdate(Map<String, uTubeVideoData> videoDataMap){
		this.videoDataMap = videoDataMap;
		updateReceived = true;
	}
	
	void setReloadedData(Map<String, uTubeVideoData> videoDataMap){
		this.videoDataMap = videoDataMap;		
	}
	public void actionPerformed(ActionEvent ae){		 
//		if(ae.getSource() == btnCrawluTube){
//			// Prepare uTubeCrawler and start crawling
//			videoDataVector.clear();
//			String searchKeyWords = textField.getText();
//			searchKeyWords = searchKeyWords.replaceAll(" ", "+");			
//			threadManager = new uTubeClientThreadsManager(this);
//			uTubeCrawler = new YouTubeCrawler(searchKeyWords /*key words*/,// %22asdf asdf asdf%22
//												"news" /*category*/, 
//												50 /*maxResults*/, 
//												this, 
//												threadManager);			
//			uTubeDM = new uTubeDataManager(this, threadManager, "STORE");
//			
//			new Thread(uTubeCrawler, "uTubeCrawler").start(); 
//			new Thread(uTubeDM, "uTubeDataManager").start();				
//		}
		if(ae.getSource() == btnStop){
			try{
				uTubeDM.stopRunning();				
				textAreaDM.append(" Stopping uTubeDataManager Thread. ");
			}catch(Exception e){
				textAreaDM.append(e.getMessage());
				 e.printStackTrace();		 
			}	
		}	
//		if(ae.getSource() == btnReload){
//			try{
//				threadManager = new uTubeClientThreadsManager(this);
//				uTubeDM = new uTubeDataManager(this, threadManager, "RELOAD");
//				new Thread(uTubeDM, "uTubeDataManager").start();			
//			}catch(Exception e){
//				 e.printStackTrace();		 
//			}	
//		}
		if(ae.getSource() == btnFindKeyWords){
			//dispResults();	
		}
	}	


/**
Create a database in MySQL.
CREATE DATABASE uTubeDB DEFAULT CHARACTER SET utf8 COLLATE utf8_unicode_ci; 


Create an user for Java and grant it access. Simply because using root is a bad practice.
CREATE USER 'uTube'@'localhost' IDENTIFIED BY 'uTubePWD'; 
GRANT ALL ON uTubeDB.* TO 'uTube'@'localhost' IDENTIFIED BY 'uTubePWD'; 

CREATE TABLE `utubevideos` (
  `Author` varchar(150) DEFAULT NULL,
  `Category` varchar(45) DEFAULT NULL,
  `CommentsCount` int(11) DEFAULT NULL,
  `CreatedOn` datetime DEFAULT NULL,
  `Description` varchar(600) DEFAULT NULL,
  `DescriptionXed` varchar(600) DEFAULT NULL,
  `Dislikes` int(11) DEFAULT NULL,
  `Duration` float DEFAULT NULL,
  `FavoritesCount` int(11) DEFAULT NULL,
  `ID` varchar(50) NOT NULL,
  `Likes` int(11) DEFAULT NULL,
  `Location` varchar(100) DEFAULT NULL,
  `Rating` float DEFAULT NULL,
  `QueryString` varchar(200) DEFAULT NULL,
  `QueryUrl` varchar(200) DEFAULT NULL,
  `Title` varchar(200) DEFAULT NULL,
  `ViewsCount` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8

CREATE TABLE `utubevideocomments` (
  `ID` varchar(60) NOT NULL,
  `VideoID` varchar(45) DEFAULT NULL,
  `Author` varchar(150) DEFAULT NULL,
  `CreatedOn` varchar(45) DEFAULT NULL,
  `Content` text,
  `VideoTitle` varchar(200) DEFAULT NULL,
  `VideoAuthor` varchar(150) DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8

SET SQL_SAFE_UPDATES=0;
DELETE from utubedb.utubevideos
*/
	
	private static final String YOUTUBE_URL = "http://gdata.youtube.com/feeds/api/videos"; 
	public static void main(String[] args) {
		 uTubeClient clientGUI = new uTubeClient();		
		 clientGUI.createGUI();
		 clientGUI.updateReceived = false;		
		 
	} 
//	public void dispResults(){	
//		
//		String textBulk = "";
//		try{
//			Set dataSet = videoDataMap.entrySet();			 
//			Iterator iterator = dataSet.iterator();			
//			while(iterator.hasNext()){
//				Map.Entry entry = (Map.Entry) iterator.next();
//				String ID = (String)entry.getKey();
//				uTubeVideoData videoData = (uTubeVideoData)entry.getValue();
//				Iterator<uTubeVideoComment> comments = videoData.comments.iterator();
//				for( ; comments.hasNext() ;){
//					uTubeVideoComment comment = comments.next();
//					textAreaClient.append("\n\tAuthor:" + comment.getAuthor() + "\n\t\t" + comment.getContent());
//					textBulk+= comment.getAuthor() + " " + comment.getContent() + "\n";
//				}
//			}
//			textAreaClient.append("\n");
//			textAreaClient.append("\n ####################  WORD FREQUENCY ################# \n");
//			uTubeWordFrequency wordFreq = new uTubeWordFrequency();
//			Map words = wordFreq.getWordFrequecyMap(textBulk);
//			Set set = words.entrySet(); 
//			Iterator iter = set.iterator(); 
//			String word = "";
//			uTubeWordFrequency.WordCount count;
//			while (iter.hasNext()) {
//				Map.Entry entry = (Map.Entry) iter.next(); 
//				word = (String) entry.getKey();
//				count = (uTubeWordFrequency.WordCount) entry.getValue(); 
//				if (count.i > 1)
//					textAreaClient.append("\n" + (word + "\t" + 	count.i)); 
//			}
//		}catch(Exception e){
//			System.out.println(e.getMessage());			 
//		}		
//	}
}
