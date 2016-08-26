package yt;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.LayoutManager;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

import yt.har.uTubeAuthor;
import yt.har.uTubeAuthorVideosQuery;
import yt.har.uTubeCrawler;
import yt.har.uTubeCampaignManager;
import yt.har.uTubeClientThreadsManager;
import yt.har.uTubeDataManager;
import yt.har.uTubeQuery;
import yt.har.uTubeVideoData;
import yt.har.uTubeVideoQuery;
import yt.vis.AuthorSubscribersNet;
import yt.vis.ButtonTabComponent;
import yt.vis.CommentAuthorsProfile;
import yt.vis.CommentVideoAuthorsNet;
import yt.vis.ContentAuthorProfile;
import yt.vis.UoD_NetGraph;
import yt.vis.UploadHistoryGraph;
import yt.vis.VideoAuthorLifeSpanProfile;
import yt.vis.VideoCategoriesGraph;
import yt.vis.VideoLifeSpanProfile;
import yt.vis.VideoPopularityStats;
import yt.vis.WordUsageHistoryGraph;

public class HarVis extends JFrame implements ActionListener{	
	private static final long serialVersionUID = 1343984134332577915L;	
	private static final String YOUTUBE_URL = "http://gdata.youtube.com/feeds/api/videos"; 
	private static final String YOUTUBE_AUTHOR_URL = "http://gdata.youtube.com/feeds/api/users/"; 
	private static final String YOUTUBE_VIDEO_URL = "https://gdata.youtube.com/feeds/api/videos/";
	public yt.har.uTubeCampaignManager campaignManager;	
	// Visualizer Controls
	private JButton btnUoD_Net = new JButton("UoD Net");
	private JButton btnUoDUsageHistory = new JButton("UoD Word Usage History");
	private JButton btnCommentVideoAuthorGraph = new JButton("Comment & Video Authors' Graph");
	private JButton btnUploadHistory = new JButton("Launch Upload History");	
	private JButton btnVidPopularityStats = new JButton("Video Popularity Stats");
	private JButton btnVidLifeSpanProfile = new JButton("Video Lifespan Profile");	
	private JButton btnVideoAuthorLifeSpan = new JButton("Video Authors' Lifespan Profile");
	private JButton btnVideoAuthorProfile = new JButton("Video Authors' Profile");
	private JButton btnCommentAuthorProfile = new JButton("Comment Authors' Profile");	
	private JButton btnVidCategories = new JButton("Video Categories Summary");
	private JPanel visControlPanel = new JPanel();
	private UoD_NetGraph uodGraphics;
	private WordUsageHistoryGraph uodWordUsgHistory;
	private CommentVideoAuthorsNet commVidAuthorsGraph;
	private AuthorSubscribersNet authorSubscribersNet;
	private UploadHistoryGraph uploadHistoryGraph;
	private ContentAuthorProfile contentAuthorProfile;
	private CommentAuthorsProfile commentAuthorsProfile;
	private VideoPopularityStats videoPopularityStats;	
	private VideoLifeSpanProfile videoLifeSpanProfile;
	private JTabbedPane visTabsPane;
	private JPanel visTabsPanel = new JPanel();
	private JPanel visMainPanel = new JPanel();
	
	// DB Controls
	JPanel dbControlPanel = new JPanel();
	JPanel dbConnectionControls = new JPanel();
	
	JLabel dbUserLabel = new JLabel();
	JLabel dbPasswordLabel = new JLabel();
	JLabel dbUrlLabel = new JLabel();
	JLabel dbSchemaName = new JLabel();
	
	JTextField dbUserTextField = new JTextField("uTube");
	JPasswordField dbPasswordTextField = new JPasswordField("uTubePWD");
	JTextField dbUrlTextField = new JTextField("localhost");
	JTextField dbSchemaNameTextField = new JTextField(30);
	JButton dbConnectBtn = new JButton("Connect to Existing DB");
	JButton dbCreateBtn = new JButton("Create a new DB");
	
	// Facebook Harvester Controls
//	private JPanel fbHarvesterMainPanel = new JPanel();
//	private JButton btnCrawlFB = new JButton("Face Book");
//	private JTextArea fbTextArea = new JTextArea(20,60);
	private static String harvesterUserID;
	public String getHarvesterUserID() {
		return harvesterUserID;
	}
	public void setHarvesterUserID(String harvesterUserID) {
		this.harvesterUserID = harvesterUserID;
	}
	public String getHarvesterUserKey() {
		return harvesterUserKey;
	}
	public void setHarvesterUserKey(String harvesterUserKey) {
		this.harvesterUserKey = harvesterUserKey;
	}


	private static String harvesterUserKey; 
	// YouTube Harvester GUI Controls
	JPanel ytHarvesterControlPanel = new JPanel(); 
	private JRadioButton radioCrawlingModeResume = new JRadioButton();
	private JRadioButton radioCrawlingModeNew = new JRadioButton();
	private ButtonGroup radiogroup = new ButtonGroup();
	private JButton btnCrawluTube = new JButton("Crawl Youtube !");	
	private JButton btnDownloadAuthVideos = new JButton("Download Author Videos");
	private JButton btnDownloadVideo = new JButton("Download Video");
		
	private JButton btnPause = new JButton("Pause");
	// Obsolete ? private JButton btnFilter = new JButton("Filter");
	private JCheckBox chkBoxCrawlerKeywordOrderFixed = new JCheckBox("Key words must appear in the same order.");
	private JCheckBox chkBoxCrawlerAllKeyWordsMustMatch = new JCheckBox("All key words must match");
	private JPanel ytHarvesterMainPanel = new JPanel();
	private JSplitPane ytHarvesterMainSplitPane = new JSplitPane();
	private JSplitPane splitPane4DMnCrawler;
	private JSplitPane splitPane4TMnClient;
	
	private JTextField txtFieldAuthor = new JTextField(15);
	private JTextField txtFieldVideoID = new JTextField(15);
	private JCheckBox chkBoxRelatedVideosNeeded = new JCheckBox("Download Related Videos");
	private JTextArea seedQueryTextArea = new JTextArea(5,25);	
//	private JLabel label = new JLabel();
	
	private static JPasswordField harvesterUserIDField = new JPasswordField(15);
	private JLabel labelHarvesterUserID = new JLabel();
	
	public JTextArea textAreaCrawler = new JTextArea(35,80);
	private JPanel crawlerPanel = new JPanel();
	private JScrollPane scrollPane4CrawlerTA = new JScrollPane(textAreaCrawler);
	
	private JPanel dmPanel = new JPanel();
	public JTextArea textAreaDM = new JTextArea(35,80);
	private JScrollPane scrollPane4DMTextArea = new JScrollPane(textAreaDM);
	
	private JPanel tmPanel = new JPanel();
	public  JTextArea textAreaTM = new JTextArea(35,80);
	private JScrollPane scrollPane4TMTextArea = new JScrollPane(textAreaTM);
	
	private JPanel clientPanel = new JPanel();
	public JTextArea textAreaClient = new JTextArea(35,80);
	private JScrollPane scrollPane4ClientTA = new JScrollPane(textAreaClient);	
	
	public JTextArea textAreaUodOutput = new JTextArea(20, 50);
	
	private JTabbedPane harvisTabs;	
	
	public HarVis(){
		campaignManager = new uTubeCampaignManager();		
		setTitle("HarVis V 2.0-230313");
	}
	public static void main(String[] args) {
		HarVis harvis = new HarVis();		
		harvis.createGUI();					
	}
	public String getSelectedCampaignMode(ButtonGroup radioGroup) {
        for (Enumeration<AbstractButton> buttons = radioGroup.getElements(); buttons.hasMoreElements();) {
            AbstractButton button = buttons.nextElement();
            if (button.isSelected()) {
                return button.getActionCommand();
            }
        }
        return null;
    }
	private void connectToDB(){
		Connection conn = null;
		textAreaClient.setText("Harvis connecting to the Database.");
		if (dbUserTextField.getText().length() == 0 | 					
			dbUrlTextField.getText().length() == 0 |
			dbSchemaNameTextField.getText().length() == 0){
			JOptionPane.showMessageDialog(this, "Database connection parameters are incomplete.", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}			
		uTubeDataManager.setDBConnParameters(dbUserTextField.getText(), 
											 new String(dbPasswordTextField.getPassword()),
											 dbUrlTextField.getText(),
											 dbSchemaNameTextField.getText());
		try {
			conn = uTubeDataManager.getDBConnection();					
			conn.close();
			JOptionPane.showMessageDialog(this, "Database connection parameters are correct.", "I Got It.", JOptionPane.INFORMATION_MESSAGE);
			dbUserTextField.setEditable(false);
			dbPasswordTextField.setEditable(false);
			dbUrlTextField.setEditable(false);
			dbSchemaNameTextField.setEditable(false);
		} catch (ClassNotFoundException e) {					
			JOptionPane.showMessageDialog(this, "Database driver is not properly installed", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		} catch (SQLException e) {					
			JOptionPane.showMessageDialog(this, "Database connection parameters are not correct", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		textAreaClient.append("\n\t	" + uTubeDataManager.getDbURL() + "/" + uTubeDataManager.getDbSchemaName() );
		textAreaClient.append("\n\t Connection successful for user " + uTubeDataManager.getDbUser());
	}
	private void createDB_Schema(){
		Connection conn = null;
		textAreaClient.setText("Harvis creating new Database Schema.");
		if (dbUserTextField.getText().length() == 0 | 					
			dbUrlTextField.getText().length() == 0 |
			dbSchemaNameTextField.getText().length() == 0){
			JOptionPane.showMessageDialog(this, "Connection parameters are incomplete.", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}			
		uTubeDataManager.setDBConnParameters(dbUserTextField.getText(), 
				 new String(dbPasswordTextField.getPassword()),
				 dbUrlTextField.getText(),
				 "");
		try {
			conn = uTubeDataManager.getDBConnection();					
			conn.close();					
			//String dbSchemaName = JOptionPane.showInputDialog(this, "Enter the new Dabase Schema name (Alphanumeric).", "Compulsory", JOptionPane.INFORMATION_MESSAGE);					
			uTubeDataManager.createNewDB_Schema(dbSchemaNameTextField.getText());
			JOptionPane.showMessageDialog(this, "Schema created successfully", "DB Message", JOptionPane.INFORMATION_MESSAGE);
			dbUserTextField.setEditable(false);
			dbPasswordTextField.setEditable(false);
			dbUrlTextField.setEditable(false);
			dbSchemaNameTextField.setEditable(false);					
		} catch (ClassNotFoundException e) {					
			JOptionPane.showMessageDialog(this, "Database driver is not properly installed \n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			return;
		} catch (SQLException e) {
			JOptionPane.showMessageDialog(this, "Database connection parameters are not correct \n" + e.getMessage() , "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		textAreaClient.append("\n\t	" + uTubeDataManager.getDbURL() + "/" + uTubeDataManager.getDbSchemaName() );
		textAreaClient.append("\n\t Connection successful for user " + uTubeDataManager.getDbUser());
	}
	public static boolean isValidHarVisUser(){		
		harvesterUserID = new String(harvesterUserIDField.getPassword());
		harvesterUserKey = "";
		if (harvesterUserID.equals("uTubeHarVis")){
			harvesterUserKey = "AI39si4I3aa8cv3sHgIR6SfjdY0CGnEJ4RY6fUso8VlKgyfxYicnsPeTsVRZV6gJzqg11_CPIU0OLgQiAAS28KUSwKVrpLpoeg";			
		}else if (harvesterUserID.equals("uTubeMatrix")) {
			harvesterUserKey = "AI39si6Vi8lLyuPXuqBTT0iDsGuhvcayuccp6L16xV5um8VT5dAR7B3-I6LDDxuErg0S8D1XdiIbg3GeatcRWfu2SgkRdPJ0gQ";			
		}else{
			JOptionPane.showMessageDialog(null, "Please input a valid Harvester User ID.");
			return false;
		}
		return true;
	}
	private void prepareCrawler(){
		if( isValidHarVisUser() == false) return;
					
		String seedQueryStrings = seedQueryTextArea.getText() + "\n";
		
		if(seedQueryStrings.contains("ك"))
			seedQueryStrings = seedQueryStrings.replace("ك", "ک");
		
		HashMap<String, HashMap<String, uTubeQuery>> queryStringsMap = new HashMap<String, HashMap<String, uTubeQuery>>();
		// Prepare Local Memory and Query Collection					
		campaignManager.threadManager = new uTubeClientThreadsManager(this);				
		campaignManager.uTubeDM = new uTubeDataManager(this, uTubeDataManager.STORE_MODE);
		HashMap<String, uTubeQuery> queryObjects = new HashMap<String, uTubeQuery>();
		ArrayList<String> queryStringsArray = new ArrayList<String>();
		if (seedQueryStrings.length() == 0){
			JOptionPane.showMessageDialog(this, "Please input Seed Query Key Words.");
			return;
		}else{
			// Parse query strings (Each line is a query string)					
			
			BufferedReader in = new BufferedReader(new StringReader(seedQueryStrings)); 
			String queryString, temp = "";
			try{
				int i = 1;
				while((queryString = in.readLine()) != null){			
					//queryStringsArray.add("\"" + queryString + "\"");// Stopped working on May 1, 2013. Search key words in quotes don't work anymore with youtube api :=}? 
					queryStringsArray.add(queryString);
					temp += i++  + ": " + queryString + "\n";
				}
			}catch(Exception e){
				e.printStackTrace();
				return;
			}
			if ( JOptionPane.showConfirmDialog(this , "Topic keywords are\n" + temp) != JOptionPane.YES_OPTION)
				return;
			
			// Prepare Query Objects					
			for (int queryStringsIndex = 0 ; queryStringsIndex < queryStringsArray.size() ; queryStringsIndex ++){					
				boolean keyWordIsChanged = true;
				queryObjects = campaignManager.uTubeDM.reloadQueryObjects(queryStringsArray.get(queryStringsIndex));					
				Set<java.util.Map.Entry<String, uTubeQuery>> querySet = queryObjects.entrySet();
				Iterator<java.util.Map.Entry<String, uTubeQuery>> querySetIterator = querySet.iterator();

				for ( ; querySetIterator.hasNext() ; ){
					Map.Entry<String, uTubeQuery> queryEntry = (Map.Entry<String, uTubeQuery>) querySetIterator.next();
					uTubeQuery query = queryEntry.getValue();							
					if( query.getQueryString().equals(queryStringsArray.get(queryStringsIndex))){ 
						keyWordIsChanged = false;
					}							
				}
				if (keyWordIsChanged){
					try {
						uTubeQuery query = new uTubeQuery(new URL(YOUTUBE_URL));							
						query.setFullTextQuery(queryStringsArray.get(queryStringsIndex));
						query.setMaxResults(50);			
						query.setQueryString(queryStringsArray.get(queryStringsIndex));			
						queryObjects.put(query.getQueryString(), query);								
					} catch (MalformedURLException e) {									
						e.printStackTrace();
					}					
				}
				queryStringsMap.put(queryStringsArray.get(queryStringsIndex), queryObjects );
			}										
		}
		String campaignMode = getSelectedCampaignMode(radiogroup);
		if (campaignMode == null ){
			JOptionPane.showMessageDialog(this, "Please select a Campaign Mode.");
			return;
		}			
		else if (campaignMode.equals("RESUME")){
			// Prepare Local Memory and Query Collection					
			campaignManager.threadManager = new uTubeClientThreadsManager(this);				
			campaignManager.uTubeDM = new uTubeDataManager(this, uTubeDataManager.STORE_MODE);				
			HashMap<String, uTubeVideoData> existingVideoData = campaignManager.uTubeDM.reLoadVideoDataLite();	
			campaignManager.setReloadedData(existingVideoData);
			
			campaignManager.uTubeCrawler = new uTubeCrawler(queryStringsMap,
												existingVideoData,														
												50 /*maxResults*/, 
												uTubeCrawler.VIDEOS_BY_KEYWORD,
												this);
			campaignManager.uTubeCrawler.setKeyWordsOrderFixed(chkBoxCrawlerKeywordOrderFixed.isSelected());
			campaignManager.uTubeCrawler.setAllKeyWordsMustMatch(chkBoxCrawlerAllKeyWordsMustMatch.isSelected());
			//campaignManager.uTubeCrawler.setSearchRound(campaignManager.uTubeDM.getMaxSearchRound(queryStringsArray.get(0)));
			
			new Thread(campaignManager.uTubeDM, "uTubeDataManager").start();
			new Thread(campaignManager.uTubeCrawler, "uTubeCrawler").start();				
		}
		else if (campaignMode.equals("NEW")){				
			// Prepare threadManager 
			campaignManager.threadManager = new uTubeClientThreadsManager(this);				
			// Prepare uTubeCrawler and start crawling
			campaignManager.uTubeCrawler = new uTubeCrawler(queryStringsMap,
												50 /*maxResults*/,
												uTubeCrawler.VIDEOS_BY_KEYWORD,
												this);	
			campaignManager.uTubeCrawler.setKeyWordsOrderFixed(chkBoxCrawlerKeywordOrderFixed.isSelected());
			campaignManager.uTubeDM = new uTubeDataManager(this, uTubeDataManager.STORE_MODE);
			new Thread(campaignManager.uTubeCrawler, "uTubeCrawler").start(); 
			new Thread(campaignManager.uTubeDM, "uTubeDataManager").start();
		}	
	}
	private void quickDownloadAuthorVideos(){
		if (isValidHarVisUser() == false) return;
		campaignManager.threadManager = new uTubeClientThreadsManager(this);				
		campaignManager.uTubeDM = new uTubeDataManager(this, uTubeDataManager.STORE_MODE);
		HashMap<String, HashMap<String, uTubeQuery>> queryStringsMap = new HashMap<String, HashMap<String, uTubeQuery>>();
		HashMap<String, uTubeQuery> queryObjects = new HashMap<String, uTubeQuery>();
		String strAuthorID = txtFieldAuthor.getText().trim();
		if (strAuthorID.length() == 0){
			JOptionPane.showMessageDialog(this, "Please input a valid YouTube user ID.");
			return;
		}
		if ( JOptionPane.showConfirmDialog(this , "Author ID is " + strAuthorID) != JOptionPane.YES_OPTION)
			return;
		try {
			uTubeAuthor authorObj = new uTubeAuthor(strAuthorID);
			
			authorObj.parseAuthorProfile();
			//URL authorQueryURL = new URL("http://gdata.youtube.com/feeds/api/users/"+authorObj.getAuthor_id()+"/uploads");
			uTubeAuthorVideosQuery query = new uTubeAuthorVideosQuery(new URL(YOUTUBE_AUTHOR_URL), authorObj);							
			//query.setFullTextQuery(strAuthorID);
			query.setMaxResults(50);			
			//query.setQueryString(strAuthorID);			
			queryObjects.put(query.getQueryString(), query);								
		} catch (IOException e) {
			JOptionPane.showMessageDialog(this , e.getMessage());			
			e.printStackTrace();
			return;
		} catch (ParserConfigurationException e) {
			JOptionPane.showMessageDialog(this , e.getMessage());
			e.printStackTrace();
		} catch (SAXException e) {
			JOptionPane.showMessageDialog(this , e.getMessage());
			e.printStackTrace();
		} 
		queryStringsMap.put(strAuthorID, queryObjects );
		// Prepare threadManager 
		campaignManager.threadManager = new uTubeClientThreadsManager(this);				
		// Prepare uTubeCrawler and start crawling
		campaignManager.uTubeCrawler = new uTubeCrawler(queryStringsMap,
				50 /*maxResults*/,
				uTubeCrawler.VIDEOS_BY_AUTHOR,
				this);	
		campaignManager.uTubeCrawler.setKeyWordsOrderFixed(chkBoxCrawlerKeywordOrderFixed.isSelected());
		campaignManager.uTubeCrawler.setAllKeyWordsMustMatch(chkBoxCrawlerAllKeyWordsMustMatch.isSelected());
		campaignManager.uTubeDM = new uTubeDataManager(this, uTubeDataManager.STORE_MODE);
		new Thread(campaignManager.uTubeCrawler, "uTubeCrawler").start(); 
		new Thread(campaignManager.uTubeDM, "uTubeDataManager").start();
	}
	private void quickDownloadVideos(){
		if (isValidHarVisUser() == false) return;
		campaignManager.threadManager = new uTubeClientThreadsManager(this);				
		campaignManager.uTubeDM = new uTubeDataManager(this, uTubeDataManager.STORE_MODE);
		HashMap<String, HashMap<String, uTubeQuery>> queryStringsMap = new HashMap<String, HashMap<String, uTubeQuery>>();
		HashMap<String, uTubeQuery> queryObjects = new HashMap<String, uTubeQuery>();
		String strVideoID = txtFieldVideoID.getText().trim();
		if (strVideoID.length() == 0){
			JOptionPane.showMessageDialog(this, "Please input a valid YouTube user ID.");
			return;
		}
		if ( JOptionPane.showConfirmDialog(this , "Video ID is " + strVideoID) != JOptionPane.YES_OPTION)
			return;
		try {			
			uTubeVideoQuery query = new uTubeVideoQuery(new URL(YOUTUBE_VIDEO_URL), strVideoID);
			query.setRelatedVideosNeeded(chkBoxRelatedVideosNeeded.isSelected());
			queryObjects.put(query.getQueryString(), query);								
		} catch (IOException e) {
			JOptionPane.showMessageDialog(this , e.getMessage());			
			e.printStackTrace();
			return;
		} 
		queryStringsMap.put(strVideoID, queryObjects );
		// Prepare threadManager 
		campaignManager.threadManager = new uTubeClientThreadsManager(this);				
		// Prepare uTubeCrawler and start crawling
		campaignManager.uTubeCrawler = new uTubeCrawler(queryStringsMap,
				50 /*maxResults*/,
				uTubeCrawler.VIDEOS_BY_VIDEO_ID,
				this);	
		campaignManager.uTubeCrawler.setKeyWordsOrderFixed(chkBoxCrawlerKeywordOrderFixed.isSelected());
		campaignManager.uTubeCrawler.setAllKeyWordsMustMatch(
				chkBoxCrawlerAllKeyWordsMustMatch.isSelected());
		campaignManager.uTubeDM = new uTubeDataManager(this, uTubeDataManager.STORE_MODE);
		new Thread(campaignManager.uTubeCrawler, "uTubeCrawler").start(); 
		new Thread(campaignManager.uTubeDM, "uTubeDataManager").start();
	}
	public void actionPerformed(ActionEvent ae){			
		if(ae.getSource() == dbConnectBtn){
			connectToDB();	
		}
		if(ae.getSource() == dbCreateBtn){
			createDB_Schema();
		}
		if(ae.getSource() == btnCrawluTube){
			prepareCrawler();
		}
		if(ae.getSource() == btnDownloadAuthVideos){
			quickDownloadAuthorVideos();
		}
		if(ae.getSource() == btnDownloadVideo){
			quickDownloadVideos();
		}
		if(ae.getSource() == btnPause){
			try{
				campaignManager.uTubeDM.stopRunning();				
				textAreaDM.append(" Stopping uTubeDataManager Thread. ");
			}catch(Exception e){
				e.printStackTrace();		 
				textAreaDM.append("\n Grrrr " + e.getMessage());
				textAreaClient.append("\n Grrrr " + e.getMessage());			 
			}	
		}
//		if(ae.getSource() == btnFilter){			
//			campaignManager.uTubeDM = new uTubeDataManager(this, uTubeDataManager.FILTER_MODE);
//			new Thread(campaignManager.uTubeDM, "uTubeDataManager").start();					
//		}
				
		// Visualization Listeners
		campaignManager.uTubeDM = new uTubeDataManager(this);
		
		if(ae.getSource() == btnVideoAuthorLifeSpan){
			VideoAuthorLifeSpanProfile contentAuthorLifespanProfile = new VideoAuthorLifeSpanProfile(campaignManager.uTubeDM);
			contentAuthorLifespanProfile.fire();	
			visTabsPane.add("Video Author Lifespan Profile", VideoAuthorLifeSpanProfile.panel);			 
		 	int tabIndex = visTabsPane.getTabCount();
		 	ButtonTabComponent btnTabComp = new ButtonTabComponent(visTabsPane);
		 	visTabsPane.setTabComponentAt(tabIndex-1, btnTabComp); 
		}
		if(ae.getSource() == btnVidCategories){
			VideoCategoriesGraph videoCategoriesGraph = new VideoCategoriesGraph(campaignManager.uTubeDM);
			videoCategoriesGraph.fire();	
			visTabsPane.add("Video Categories Summary", videoCategoriesGraph.panel);
			int tabIndex = visTabsPane.getTabCount();
		 	ButtonTabComponent btnTabComp = new ButtonTabComponent(visTabsPane);
		 	visTabsPane.setTabComponentAt(tabIndex-1, btnTabComp);
		}		
		if( ae.getSource() == btnUploadHistory){		
			uploadHistoryGraph = new UploadHistoryGraph(campaignManager.uTubeDM);			
			uploadHistoryGraph.fire();
			visTabsPane.add("Upload History", UploadHistoryGraph.panel);
			int tabIndex = visTabsPane.getTabCount();
		 	ButtonTabComponent btnTabComp = new ButtonTabComponent(visTabsPane);
		 	visTabsPane.setTabComponentAt(tabIndex-1, btnTabComp);
		}
		if( ae.getSource() == btnVidLifeSpanProfile){		
			videoLifeSpanProfile = new VideoLifeSpanProfile(campaignManager.uTubeDM);			
			videoLifeSpanProfile.fire();
			visTabsPane.add("Video Lifespan Profile", VideoLifeSpanProfile.panel);
			int tabIndex = visTabsPane.getTabCount();
		 	ButtonTabComponent btnTabComp = new ButtonTabComponent(visTabsPane);
		 	visTabsPane.setTabComponentAt(tabIndex-1, btnTabComp);
		}
		if( ae.getSource() == btnVidPopularityStats){		
			videoPopularityStats = new VideoPopularityStats(campaignManager.uTubeDM);			
			videoPopularityStats.fire();
			visTabsPane.add("Video Popularity Stats", VideoPopularityStats.panel);
			int tabIndex = visTabsPane.getTabCount();
		 	ButtonTabComponent btnTabComp = new ButtonTabComponent(visTabsPane);
		 	visTabsPane.setTabComponentAt(tabIndex-1, btnTabComp);
		}
		if( ae.getSource() == btnVideoAuthorProfile){			
			contentAuthorProfile  = new ContentAuthorProfile(campaignManager.uTubeDM);	
			contentAuthorProfile.fire();			
			visTabsPane.add("Video Author's Profile", contentAuthorProfile.panel);
			int tabIndex = visTabsPane.getTabCount();
		 	ButtonTabComponent btnTabComp = new ButtonTabComponent(visTabsPane);
		 	visTabsPane.setTabComponentAt(tabIndex-1, btnTabComp);
		}
		if( ae.getSource() == btnCommentAuthorProfile){			
			commentAuthorsProfile  = new CommentAuthorsProfile(campaignManager.uTubeDM);	
			commentAuthorsProfile.fire();	
			visTabsPane.add("Comment Author's Profile", commentAuthorsProfile.panel);
			int tabIndex = visTabsPane.getTabCount();
		 	ButtonTabComponent btnTabComp = new ButtonTabComponent(visTabsPane);
		 	visTabsPane.setTabComponentAt(tabIndex-1, btnTabComp);
		}
	}	
	private void ytDatabaseGUI(){
			    
	    dbControlPanel.setBorder(BorderFactory.createTitledBorder("DB Configuration"));	    
	    
	    dbUserLabel.setText("Input DB User Name");	    
	    dbConnectionControls.setLayout(new GridLayout(5,1));
	    dbConnectionControls.setBorder(BorderFactory.createTitledBorder("Connection Parameters"));
	    
	    JPanel dbUserNamePanel = new JPanel();
	    dbUserNamePanel.setLayout(new GridLayout(1,2));
	    dbUserNamePanel.add(dbUserLabel);
	    dbUserNamePanel.add(dbUserTextField);
	    
	    JPanel dbPwdPanel = new JPanel();
	    dbPwdPanel.setLayout(new GridLayout(1,2));
	    dbPasswordLabel.setText("Input DB Password");
	    dbPwdPanel.add(dbPasswordLabel);
	    dbPwdPanel.add(dbPasswordTextField);
	    
	    JPanel dbUrlPanel = new JPanel();
	    dbUrlPanel.setLayout(new GridLayout(1,2));
	    dbUrlLabel.setText("Input DB Url");
	    dbUrlPanel.add(dbUrlLabel);
	    dbUrlPanel.add(dbUrlTextField);
	    
	    JPanel dbSchemaPanel = new JPanel();
	    dbSchemaPanel.setLayout(new GridLayout(1,2));
	    dbSchemaName.setText("Input DB Schema Name");
	    dbSchemaPanel.add(dbSchemaName);
	    dbSchemaPanel.add(dbSchemaNameTextField);
	    
	    dbConnectionControls.add(dbUserNamePanel);
	    dbConnectionControls.add(dbPwdPanel);
	    dbConnectionControls.add(dbUrlPanel);
	    dbConnectionControls.add(dbSchemaPanel);
	    JPanel dbBtnPanel = new JPanel();
	    dbConnectBtn.addActionListener(this);
	    dbBtnPanel.add(dbConnectBtn);
	    dbCreateBtn.addActionListener(this);
	    dbBtnPanel.add(dbCreateBtn);
	    dbConnectionControls.add(dbBtnPanel);
//	    Box vertexBox = Box.createVerticalBox();
//		vertexBox.setBorder(BorderFactory.createTitledBorder("Create New Schema"));
//		JTextField txtFieldSchemaName = new JTextField(10);
//		vertexBox.add(new JLabel("Enter schema name"));
//		vertexBox.add(txtFieldSchemaName);
//		JButton btnCreateSchema = new JButton("Create");
//		btnCreateSchema.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent arg0) {
//				
//			}
//		});
//		vertexBox.add(btnCreateSchema);
//		dbControlPanel.add(vertexBox);
	    dbControlPanel.add(dbConnectionControls);	    
	}
	private void ytHarvesterGUI(){
		ytHarvesterControlPanel.setLayout(new GridLayout(2,1));	    
	    JPanel launchCampaignPanel = new JPanel();	   
	    launchCampaignPanel.setBorder(BorderFactory.createTitledBorder("Launch Campaign"));
	   // launchCampaignPanel.setLayout(new GridLayout(2,2));
	    JPanel harvesterUserIDPanel = new JPanel();
	    harvesterUserIDPanel.setLayout(new GridLayout(3,1));
	    harvesterUserIDPanel.add(labelHarvesterUserID);
	    harvesterUserIDPanel.add(harvesterUserIDField);
	    harvesterUserIDField.setText("uTubeHarVis");
	    labelHarvesterUserID.setText("Input HarVis User ID");
	    harvesterUserIDPanel.add(new JLabel());
	    harvesterUserIDPanel.setBorder(BorderFactory.createTitledBorder("User Credentials"));
	    launchCampaignPanel.add(harvesterUserIDPanel);    
	    
	    JPanel queryInputPanel = new JPanel();
	    //queryInputPanel.setLayout(new GridLayout(2,1));
	    //label.setText("Input Seed Query Key Words");
	    //queryInputPanel.add(label);
	    queryInputPanel.add(new JScrollPane(seedQueryTextArea));	    
	    queryInputPanel.setBorder(BorderFactory.createTitledBorder("Search Criteria Key Words"));
	    
	    launchCampaignPanel.add(queryInputPanel);
	    
	    
	    JPanel campaignModePanel = new JPanel();
	    campaignModePanel.setLayout(new GridLayout(2,1));
	    radioCrawlingModeResume.setText("Resume Existing Campaign");
	    radioCrawlingModeResume.setActionCommand("RESUME");
	    radioCrawlingModeNew.setText("New Campaign");
	    radioCrawlingModeNew.setActionCommand("NEW");
	    radiogroup.add(radioCrawlingModeResume);
	    radiogroup.add(radioCrawlingModeNew);
	    campaignModePanel.add(radioCrawlingModeResume);
	    campaignModePanel.add(radioCrawlingModeNew);	    
	    campaignModePanel.setBorder(BorderFactory.createTitledBorder("Campaign Mode"));
	   
	    launchCampaignPanel.add(campaignModePanel);
	    
	    
	    JPanel crawlBtnPanel = new JPanel();
	    crawlBtnPanel.setLayout(new GridLayout(3,1));
	    crawlBtnPanel.add(btnCrawluTube);
	    btnCrawluTube.setMnemonic('C'); 		// associate hotkey
	    btnCrawluTube.addActionListener(this);   // register button listener
	    btnCrawluTube.requestFocus();
	    
	    
	    crawlBtnPanel.add(chkBoxCrawlerAllKeyWordsMustMatch);
	    chkBoxCrawlerAllKeyWordsMustMatch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AbstractButton chkBox = (AbstractButton)e.getSource();
				if (chkBox.isSelected() == false){					
					chkBoxCrawlerKeywordOrderFixed.setSelected(false);
					chkBoxCrawlerKeywordOrderFixed.setEnabled(false);
				}else
					chkBoxCrawlerKeywordOrderFixed.setEnabled(true);
			}
		});
	    
	    crawlBtnPanel.add(chkBoxCrawlerKeywordOrderFixed);
	    chkBoxCrawlerKeywordOrderFixed.addActionListener(this);
	    
	    
	    launchCampaignPanel.add(crawlBtnPanel);
	    
//	    launchCampaignPanel.add(btnPause);	    
//	    btnPause.setMnemonic('X');
//	    btnPause.addActionListener(this);
	    
	    
//	    launchCampaignPanel.add(btnFilter);	    
//	    btnFilter.setMnemonic('F');
//	    btnFilter.addActionListener(this);
	    
	    JPanel quickDownloadsControlPanel = new JPanel();
	    //downloadVideosPanel.setLayout(new GridLayout(2,1));
	    quickDownloadsControlPanel.add(btnDownloadAuthVideos);
	    btnDownloadAuthVideos.addActionListener(this);
	    quickDownloadsControlPanel.add(txtFieldAuthor);
	    quickDownloadsControlPanel.setBorder(BorderFactory.createTitledBorder("Quick Download"));
	    
	    quickDownloadsControlPanel.add(btnDownloadVideo);
	    btnDownloadVideo.addActionListener(this);
	    
	    quickDownloadsControlPanel.add(txtFieldVideoID);
	    quickDownloadsControlPanel.add(chkBoxRelatedVideosNeeded);
	    
	    ytHarvesterControlPanel.add(launchCampaignPanel);
	    ytHarvesterControlPanel.add(quickDownloadsControlPanel);
	    
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
	    
	    textAreaClient.setText("Harvester is not connected to the database.\n Please provide valid credentials in DB Control Panel.");
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
	    
	    ytHarvesterMainSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, splitPane4TMnClient, splitPane4DMnCrawler);
	    ytHarvesterMainSplitPane.setOneTouchExpandable(true);
	    ytHarvesterMainSplitPane.setDividerLocation(350);
	    ytHarvesterMainSplitPane.setBounds(100,100, 800,800);
	    
	    ytHarvesterMainPanel.setLayout(new BorderLayout());
	    ytHarvesterMainPanel.add(ytHarvesterControlPanel, BorderLayout.NORTH);
	    ytHarvesterMainPanel.add(ytHarvesterMainSplitPane, BorderLayout.CENTER);
	}
	private void ytVisualizationGUI(){
		visControlPanel.setLayout(new GridLayout(2, 6));
		btnUoD_Net.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent ae) {					
					uodGraphics = new UoD_NetGraph(campaignManager.uTubeDM);
					visTabsPane.add("UoD Titles", uodGraphics);
					int tabIndex = visTabsPane.getTabCount();
				 	ButtonTabComponent btnTabComp = new ButtonTabComponent(visTabsPane);
				 	visTabsPane.setTabComponentAt(tabIndex-1, btnTabComp);			 							
			}
		});	    
	    visControlPanel.add(btnUoD_Net);	 
	    /*UoD Histogram*/
	    btnUoDUsageHistory.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {							
					uodWordUsgHistory = new WordUsageHistoryGraph(campaignManager.uTubeDM);					
					visTabsPane.add("Word Usage History", WordUsageHistoryGraph.panel);
					int tabIndex = visTabsPane.getTabCount();
				 	ButtonTabComponent btnTabComp = new ButtonTabComponent(visTabsPane);
				 	visTabsPane.setTabComponentAt(tabIndex-1, btnTabComp);
			}
		});	    
	    visControlPanel.add(btnUoDUsageHistory);
	    /*CommentVideoAuhtors Network Graph*/
	    btnCommentVideoAuthorGraph.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				commVidAuthorsGraph = new CommentVideoAuthorsNet(campaignManager.uTubeDM);
				JPanel cv_authorsNetPanel = new JPanel();
				visTabsPane.add("Comment Video Authors Network", cv_authorsNetPanel);
				cv_authorsNetPanel.add(commVidAuthorsGraph);
				int tabIndex = visTabsPane.getTabCount();
			 	ButtonTabComponent btnTabComp = new ButtonTabComponent(visTabsPane);
			 	visTabsPane.setTabComponentAt(tabIndex-1, btnTabComp);
			 	
				authorSubscribersNet = new AuthorSubscribersNet(campaignManager.uTubeDM);
				JPanel authorSubscriberNetPanel = new JPanel();
				visTabsPane.add("Author-Subscriber Network", authorSubscriberNetPanel);
				authorSubscriberNetPanel.add(authorSubscribersNet);
				tabIndex = visTabsPane.getTabCount();
			 	ButtonTabComponent btnTabComp2 = new ButtonTabComponent(visTabsPane);
			 	visTabsPane.setTabComponentAt(tabIndex-1, btnTabComp2);
			}
		});
	    visControlPanel.add(btnCommentVideoAuthorGraph);
	    btnUploadHistory.addActionListener(this);
	    visControlPanel.add(btnUploadHistory);
	    btnVidLifeSpanProfile.addActionListener(this);
	    visControlPanel.add(btnVidLifeSpanProfile);
	    btnVideoAuthorLifeSpan.addActionListener(this);
	    visControlPanel.add(btnVideoAuthorLifeSpan);	    
	    btnVidPopularityStats.addActionListener(this);
	    visControlPanel.add(btnVidPopularityStats);	    
	    btnVideoAuthorProfile.addActionListener(this);	    
	    visControlPanel.add(btnVideoAuthorProfile);
	    btnCommentAuthorProfile.addActionListener(this);	    
	    visControlPanel.add(btnCommentAuthorProfile);
	    
	    btnVidCategories.addActionListener(this);
	    visControlPanel.add(btnVidCategories);
	    visTabsPane = new JTabbedPane();
	    
	    visTabsPanel = new JPanel();
	    visTabsPanel.setSize(400, 400);	    	    
	    visMainPanel.setLayout(new BorderLayout());
	    visMainPanel.add(visControlPanel, BorderLayout.NORTH);
	    visMainPanel.add(visTabsPane, BorderLayout.CENTER);
	}
//	private void fbHarvesterGUI(){
//		fbHarvesterMainPanel.setLayout(new GridLayout(2,1));
//		JPanel fbControlPanel = new JPanel();
//		fbControlPanel.add(btnCrawlFB);
//		btnCrawlFB.addActionListener(new ActionListener() {
//	        public void actionPerformed(ActionEvent arg0) {	   
//	        	ConfigurationBuilder cb = new ConfigurationBuilder();
//	        	cb.setDebugEnabled(true)
//	        	  .setOAuthAppId("*********************")
//	        	  .setOAuthAppSecret("******************************************")
//	        	  .setOAuthAccessToken("**************************************************")
//	        	  .setOAuthPermissions("email,publish_stream,...");
//	        	FacebookFactory ff = new FacebookFactory(cb.build());
//	        	Facebook facebook = ff.getInstance();
//	        	try {
//					ResponseList<User> results = facebook.searchUsers("uzair");
//					Iterator<User> iterator = results.iterator();
//					while(iterator.hasNext()){
//						User user = iterator.next();
//						String bio = user.getBio();
//						String bd = user.getBirthday();
//						fbTextArea.append("User Bio " + bio + "\n User BD " + bd );
//					}
//				} catch (FacebookException e) {//					
//					e.printStackTrace();
//				}
//
//	        	
//	        }
//	    });
//		
//		JPanel fbOutputPanel = new JPanel();
//		fbOutputPanel.add(fbTextArea);
//		
//		fbHarvesterMainPanel.add(fbControlPanel);
//		fbHarvesterMainPanel.add(fbOutputPanel);
//		
//	}
	void createGUI(){		

		// YouTube Database Control Panel
		ytDatabaseGUI();
	    // YouTube Harvester Control Panel
	    ytHarvesterGUI();
	    // Prepare visControlPanel
	    ytVisualizationGUI();
	    
	    // Facebook Harvester Control Panel
//	    fbHarvesterGUI();
	    
	    harvisTabs = new JTabbedPane();	    
	    
	    ImageIcon icon = null;	    
	    icon = createImageIcon("Resources/database.gif");
	    harvisTabs.addTab("DB Control Panel", icon, dbControlPanel,
                "Database Configuration and Preprocessing");
	    
	    icon = createImageIcon("Resources/smc2.gif");
	    harvisTabs.addTab("SMC Harvester", icon, ytHarvesterMainPanel,
                "Social Media Content Harvesting Panel");
	    
	    icon = createImageIcon("Resources/d2i.gif");	    
	    harvisTabs.addTab("D2I", icon, new D2I_GUI(), "Data to Information Panel");
	    
	    icon = createImageIcon("Resources/vis2.gif");
	    harvisTabs.addTab("SMC Visualizer", icon, visMainPanel,
                "Social Media Content Visualization Panel");
	    
//	    harvisTabs.addTab("Facebook Harvester", fbHarvesterMainPanel);
	    Container con = this.getContentPane(); // inherit main frame
	    con.add(harvisTabs, BorderLayout.CENTER);
	    Toolkit tk = Toolkit.getDefaultToolkit();  
	    int xSize = ((int) tk.getScreenSize().getWidth());  
	    int ySize = ((int) tk.getScreenSize().getHeight());  
	    setSize(xSize,ySize);  
	    setExtendedState(JFrame.MAXIMIZED_BOTH);
	    setDefaultCloseOperation(EXIT_ON_CLOSE);
	    setVisible(true); // make frame visible
	}
	

	 protected ImageIcon createImageIcon(String path) {         
		 URL imgURL = HarVis.class.getResource(path);
		 return new ImageIcon(imgURL);
	 } 
}

