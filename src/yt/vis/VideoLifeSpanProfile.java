package yt.vis;


import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileInputStream;

import java.io.InputStreamReader;
import java.sql.Connection;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;



import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;


import org.joda.time.DateTime;
import org.joda.time.Months;

import yt.har.uTubeDataManager;

import com.google.gdata.util.ParseException;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.JFXPanel;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.BubbleChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

public class VideoLifeSpanProfile {
	public static JPanel panel = new JPanel();
	private DropShadow ds = new DropShadow();
	private ArrayList<VideoLifeSpan> videoLifeSpanDataList;
	JTabbedPane categoriesTabbedPane;
    JComboBox<String> videoCategoriesCombo;
    JComboBox<String> videoViewsLimitCombo;
    JButton btnLaunchUploadHistoryGraph;
    int VIEWS_COUNT_LIMIT = 1000000;
	uTubeDataManager dataManager;
	final Label caption = new Label("");
	private static final String LABEL_FONT_STYLE = "-fx-font: 16 arial;";
	public VideoLifeSpanProfile(uTubeDataManager dataManager) {
		this.dataManager = dataManager;		
	}
	void  fetchVideoLifeSpanDataFromDB(String category, int viewsCount){
		this.videoLifeSpanDataList = new ArrayList<VideoLifeSpan>();
    	try{    		
    		Connection conn = dataManager.getDBConnection();
    		System.out.println("DB Connection Secured.\n");    		
			Statement videoLifeSpanQuery = conn.createStatement();
			ResultSet videoLifeSpanQueryResultSet = videoLifeSpanQuery.executeQuery(
			"SELECT VT.ID, VT.Title, VT.Author, COUNT(CT.ID) CommentsCount, VT.ViewsCount, "+
			"Date_format(VT.CreatedOn, '%d-%m-%Y') AS UploadDate, "+
			"Date_format(min(CT.CreatedOn), '%d-%m-%Y') AS FirstCommentDate, "+ 
			"Date_format(max(CT.CreatedOn), '%d-%m-%Y') AS LastCommentDate "+
			"FROM utubevideos AS VT	"+
			"INNER JOIN utubevideocomments AS CT "+
			"ON VT.ID = CT.VideoID " +
			"WHERE  (VT.ID = CT.VideoID) " +
			"AND (VT.ViewsCount > "+viewsCount+") " +
			"AND (VT.Category LIKE '%"+category+"%') " +			
			"GROUP By CT.VideoID "+ 
			"ORDER BY CommentsCount DESC ");
			System.out.println("Data fetched.\n"); 			
			while (videoLifeSpanQueryResultSet.next()){
				VideoLifeSpan vidLifeSpan = new VideoLifeSpan();
				vidLifeSpan.setVideoID(videoLifeSpanQueryResultSet.getString("ID"));
				vidLifeSpan.setVideoTitle(videoLifeSpanQueryResultSet.getString("Title"));
				vidLifeSpan.setAuthor(videoLifeSpanQueryResultSet.getString("Author"));
				String strVidUploadDate = videoLifeSpanQueryResultSet.getString("UploadDate"); 	
        		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");        		
        		Date upLoadDate = sdf.parse(strVidUploadDate);        		
				vidLifeSpan.setUploadDate(upLoadDate);
				String strFirstCommentDate = videoLifeSpanQueryResultSet.getString("FirstCommentDate");        		        		
        		Date firstCommentDate = sdf.parse(strFirstCommentDate);        		
				vidLifeSpan.setfirstCommentDate(firstCommentDate);
				String strLastCommentDate = videoLifeSpanQueryResultSet.getString("LastCommentDate");       		        		
        		Date lastCommentDate = sdf.parse(strLastCommentDate);        		
				vidLifeSpan.setLastCommentDate(lastCommentDate);
				vidLifeSpan.setLastCommentDate(lastCommentDate);
				vidLifeSpan.setCommentsCount(videoLifeSpanQueryResultSet.getInt("CommentsCount"));
				vidLifeSpan.setViewsCount(videoLifeSpanQueryResultSet.getInt("ViewsCount"));			
				videoLifeSpanDataList.add(vidLifeSpan);
			}		
			conn.close();
			System.out.println("DB Connection Closed.\n");			
    	}
	    catch (SQLException e){
	    	System.out.println("SQL code does not execute." + e.getMessage());
		} catch (java.text.ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}       	
    }
	void fetchVideoLifeSpanData(){
		this.videoLifeSpanDataList = new ArrayList<VideoLifeSpan>();
		String strVidID = "";
	    String strVidTitle ="";
    	try{
    		String dataFilePath = "";
    		dataFilePath = VideoLifeSpanProfile.class.getResource("Data/VideoLifeSpanData.csv").getPath();
    		InputStreamReader fileReader = new InputStreamReader(new FileInputStream(dataFilePath), "UTF-8");
    	    BufferedReader lineReader = new BufferedReader(fileReader);    	    
    	    String line = lineReader.readLine();
    	    line = lineReader.readLine();   // Remove Headers   	    
    	    while (line != null)
    	    {     // ID,Title,Author,CommentsCount,ViewsCount,S,Upto 	    	
    	    	VideoLifeSpan vidLifeSpan = new VideoLifeSpan();

    	    	// ID is double-comma "enclosed"
    	    	// Therefore, Ignore first " at index 0 
    	    	// And start reading from index 1    	    	
    	    	line = line.substring(1);
    	    	int endOfID = line.indexOf('"');
    	    	// "tag:youtube.com,2008:video:xf__LmSQnzQ"
    	    	strVidID = line.substring(0, endOfID); 								// VideoID
    	    	vidLifeSpan.setVideoID(strVidID);
    	    	line = line.substring(endOfID+2); // skip 2 characters ",    	    	
    	    	
    	    	int endOfTitle = 0 ;
    	    	// A title, just like VideoID, may contain a , character 
    	    	// That can cause wrong interpretation of other values
    	    	// Therefore all , signs are replaced in Titles 
    	    	// by uTubeCrawler.captureVideoData()
    	    	if (line.startsWith("\"")){
    	    		line = line.substring(1);    	    	
    	    		endOfTitle = line.indexOf('"');
    	    		strVidTitle = line.substring(0, endOfTitle);						// VideoTitle
        	    	vidLifeSpan.setVideoTitle(strVidTitle);
        	    	line = line.substring(endOfTitle+2); // skip 2 characters ",
    	    	}else{    	    		    	    	
    	    		endOfTitle = line.indexOf(',');
    	    		strVidTitle = line.substring(0, endOfTitle);						// VideoTitle
        	    	vidLifeSpan.setVideoTitle(strVidTitle);
        	    	line = line.substring(endOfTitle+1); // skip 1 character ,
    	    	}
    	    	
    	    	String strVidAuthor = line.substring(0, line.indexOf(',')); 			// VideoAuthor
    	    	vidLifeSpan.setAuthor(strVidAuthor);
    	    	line = line.substring(line.indexOf(',')+1);
    	    	
    	    	String strVidCommentsCount = line.substring(0, line.indexOf(',')); 		// VideoCommentsCount
    	    	vidLifeSpan.setCommentsCount(Integer.parseInt(strVidCommentsCount));
    	    	line = line.substring(line.indexOf(',')+1);
    	    	
    	    	String strVidViewsCount = line.substring(0, line.indexOf(',')); 		// VideoViewsCount
    	    	vidLifeSpan.setViewsCount(Integer.parseInt(strVidViewsCount));
    	    	line = line.substring(line.indexOf(',')+1);   	    	
    	    	
    	    	String strVidUploadDate = line.substring(0, line.indexOf(',')); 		// VideoUploadDate
        		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");        		
        		Date upLoadDate = sdf.parse(strVidUploadDate); 
        		vidLifeSpan.setUploadDate(upLoadDate);
        		line = line.substring(line.indexOf(',')+1);
        		
        		String strFirstCommentDate = line.substring(0, line.indexOf(',')); 		// FirstCommentDate        		        		
        		Date firstCommentDate = sdf.parse(strFirstCommentDate); 
        		vidLifeSpan.setfirstCommentDate(firstCommentDate);
        		line = line.substring(line.indexOf(',')+1);
        		
        		String strLastCommentDate = line.substring(0);		 					// LastCommentDate    	    	        		
        		Date lastCommentDate = sdf.parse(strLastCommentDate); 
        		vidLifeSpan.setLastCommentDate(lastCommentDate);
        		
        		videoLifeSpanDataList.add(vidLifeSpan);        		
    	    	line = lineReader.readLine();
    	    	    	    	
    	    }
    	    fileReader.close();
    	    lineReader.close();
    	    //return dataVector;
    	}catch(Exception e){
    		System.out.println(strVidTitle);
    		e.printStackTrace();
    	}

//    	return null;
    }
	void videoLifeSpanBubbleChart(JFXPanel videoLifeSpanFxPanel){
    	
    	final float WeeksInAnYear = 52.14F;
    	
    	
    	XYChart.Series videoLife0Series = new XYChart.Series();
    	videoLife0Series.setName("~6 Months Life span Videos ");    	
    	XYChart.Series videoLife1Series = new XYChart.Series();
    	videoLife1Series.setName("6~12 Months Life span Videos ");
//        XYChart.Series videoLife2Series = new XYChart.Series();
//        videoLife2Series.setName("80%-60% Life span Videos ");
//        XYChart.Series videoLife3Series = new XYChart.Series();
//        videoLife3Series.setName("100%-80% Life span Videos ");
        ArrayList<Tooltip> toolTips0 = new ArrayList();
        ArrayList<Tooltip> toolTips1 = new ArrayList();
//        ArrayList<Tooltip> toolTips2 = new ArrayList();
//        ArrayList<Tooltip> toolTips3 = new ArrayList();        
        // GET THE DATA INTO Series objects        
        
        Date oldestVideoUploadDate = new Date();
        Date latestVideoUploadDate = new Date();
        
        for (VideoLifeSpan vidLifeSpan : videoLifeSpanDataList){
        	if (oldestVideoUploadDate.compareTo(vidLifeSpan.getUploadDate()) > 0){
        		oldestVideoUploadDate = vidLifeSpan.getUploadDate();
        	}
        	if (latestVideoUploadDate.compareTo(vidLifeSpan.getUploadDate()) < 0){
        		latestVideoUploadDate = vidLifeSpan.getUploadDate();
        	}        	
        }
        // DateTime of OldestVideo Uploaded on this Topic
    	Calendar calendar = new GregorianCalendar(); 
    	calendar.setTime(oldestVideoUploadDate);	
    	DateTime oldestVideoUploadDateTime = new DateTime().withDate(calendar.get(Calendar.YEAR), calendar.get(calendar.MONTH)+1, calendar.get(calendar.DAY_OF_MONTH));
    	// DateTime of LatestVideo Uploaded on this topic    	
    	calendar.setTime(latestVideoUploadDate);	
    	DateTime latestVideoUploadDateTime = new DateTime().withDate(calendar.get(Calendar.YEAR), calendar.get(calendar.MONTH)+1, calendar.get(calendar.DAY_OF_MONTH));
    	// Time Difference (in months) between latest, oldest 
        Months mt = Months.monthsBetween(oldestVideoUploadDateTime, latestVideoUploadDateTime);         
        int numTotalMonths = mt.getMonths();
        int maxCommentCount = 0;
    	int minCommentCount = 10000000; // Arbitrarily Large Number
    	
    	
        caption.setTextFill(Color.DARKORANGE);
        caption.setStyle(LABEL_FONT_STYLE);
    	
        int k = 0;
        for (VideoLifeSpan vidLifeSpan : videoLifeSpanDataList){        	
        	// DateTime of this Video
        	calendar.setTime(vidLifeSpan.getUploadDate());						// CreatedOn
        	DateTime thisVideoUploadDateTime = new DateTime().withDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1,calendar.get(Calendar.DAY_OF_MONTH));
        	// Time Difference (in months) between this and oldest video
        	Months monthsBetween = Months.monthsBetween(oldestVideoUploadDateTime, thisVideoUploadDateTime);         
            int uploadMonthNumber = monthsBetween.getMonths();
        	
            float videoLifeSpanInMonths = vidLifeSpan.getLifeSpanInMonths();
            float videoLifeSpan = (videoLifeSpanInMonths/(numTotalMonths*1.0F))*10;
        	
        	// GET THE CommentCount for Y-Axis
            
        	
        	int commentCount = 0;
        	commentCount = vidLifeSpan.getCommentsCount();		
        	commentCount = commentCount/10;							// CommentsCount
        	if (commentCount > maxCommentCount) {
        		// Required to set Y-Axis Upper-Limit
        		maxCommentCount = commentCount;        		
        	}
        	
        	if (commentCount < minCommentCount) {
        		// Required to set Y-Axis Upper-Limit
        		minCommentCount = commentCount;        		
        	}
        	if (videoLifeSpan != 0.0F){		            		
        		if (videoLifeSpan > 10){
        			System.out.println("Ooops");        			
        		}        
        		
        		//System.out.println(videoLifeSpan);
//        		if (videoLifeSpan >= 8){        			
//        			videoLife3Series.getData().add(new XYChart.Data(uploadMonthNumber, commentCount, videoLifeSpan)); 	
//        			Tooltip tooltip = new Tooltip(
//        					"Upload Time " + vidLifeSpan.getUploadDate().toString() + 
//        					"\n Lifetime (Months) " + videoLifeSpanInMonths + 
//        					"\n Views " + vidLifeSpan.getViewsCount() +
//        					"\n Comments " + commentCount + ",00 " + 
//        					"\n Video Title " + vidLifeSpan.getVideoTitle() +
//        					"\n Video Author " + vidLifeSpan.getAuthor());
//        			tooltip.setStyle("-fx-font: 14 arial;  -fx-font-smoothing-type: lcd;");
//        			toolTips3.add(tooltip);
//        		}else if (videoLifeSpan < 8 && videoLifeSpan >= 6){
//        			videoLife2Series.getData().add(new XYChart.Data(uploadMonthNumber, commentCount, videoLifeSpan));
//        			Tooltip tooltip = new Tooltip(
//        					"Upload Time " + vidLifeSpan.getUploadDate().toString() + 
//        					"\n Lifetime (Months) " + videoLifeSpanInMonths + 
//        					"\n Views " + vidLifeSpan.getViewsCount() +
//        					"\n Comments " + commentCount + ",00 " +  
//        					"\n Video Title " + vidLifeSpan.getVideoTitle() +
//        					"\n Video Author " + vidLifeSpan.getAuthor());
//        			tooltip.setStyle("-fx-font: 14 arial;  -fx-font-smoothing-type: lcd;");
//        			toolTips2.add(tooltip);
//        		}else 
        			if (videoLifeSpan > 6 ){
        			videoLife1Series.getData().add(new XYChart.Data(uploadMonthNumber, commentCount, videoLifeSpan));
        			Tooltip tooltip = new Tooltip(
        					"Upload Time " + vidLifeSpan.getUploadDate().toString() + 
        					"\n Lifetime (Months) " + videoLifeSpanInMonths + 
        					"\n Views " + vidLifeSpan.getViewsCount() +
        					"\n Comments " + commentCount + ",0 " + 
        					"\n Video Title " + vidLifeSpan.getVideoTitle() +
        					"\n Video Author " + vidLifeSpan.getAuthor());
        			tooltip.setStyle("-fx-font: 14 arial;  -fx-font-smoothing-type: lcd;");
        			toolTips1.add(tooltip);
        		}else if (videoLifeSpan < 6.0 ){
        			final XYChart.Data data0 = new XYChart.Data(uploadMonthNumber, commentCount, videoLifeSpan);
        			videoLife0Series.getData().add(data0); 
        			Tooltip tooltip = new Tooltip(
        					"Upload Time " + vidLifeSpan.getUploadDate().toString() + 
        					"\n Lifetime (Months) " + videoLifeSpanInMonths + 
        					"\n Views " + vidLifeSpan.getViewsCount() +
        					"\n Comments " + commentCount + ",0 " + 
        					"\n Video Title " + vidLifeSpan.getVideoTitle() +
        					"\n Video Author " + vidLifeSpan.getAuthor());
        			tooltip.setStyle("-fx-font: 14 arial;  -fx-font-smoothing-type: lcd;");
        			toolTips0.add(tooltip);
        		}      			
        	}

        }
        maxCommentCount = (maxCommentCount)+5;
    	int tickMarkSize = (int)((maxCommentCount)/50);
        final NumberAxis yAxis = new NumberAxis(0, (int)maxCommentCount+5, tickMarkSize); //(minX, maxX, Tick)
        final NumberAxis xAxis = new NumberAxis(0, numTotalMonths, 5);//(minY, maxY, Tick)
        final BubbleChart<Number,Number> bblc = new BubbleChart<Number,Number>(xAxis,yAxis);       
        xAxis.setLabel("Upload Week No. (Since "+ oldestVideoUploadDate.toString() +" Until " + latestVideoUploadDate.toString() +")");
        yAxis.setLabel("Comments Count Max Comments " + (int)maxCommentCount + ", " );        
        bblc.setTitle("Video Life Span");
        ObservableList<XYChart.Series<Number, Number>> bblcData = FXCollections.observableArrayList();
        bblcData.addAll(videoLife1Series, videoLife0Series);//, videoLife2Series, videoLife3Series);        
        //bblcData.addAll(videoLife0Series, videoLife1Series);
        bblc.setData(bblcData);       
        
        bblc.autosize();
        bblc.sceneToLocal(100, 100);
       // Scene scene  = new Scene(bblc, 1000, 700);
        
        ObservableList datalist0 = bblcData.get(0).getData();
        System.out.println(datalist0.size());
        Iterator iter0 = datalist0.iterator();
        int i = 0;
        while(iter0.hasNext() ){
        	XYChart.Data data0 = ((XYChart.Data) iter0.next());        	
        	Tooltip.install(data0.getNode(),toolTips1.get(i++));
        	applyMouseEvents(data0);        	
        }
        datalist0 = bblcData.get(1).getData();
        iter0 = datalist0.iterator();
        i = 0;
        System.out.println(datalist0.size());
        while(iter0.hasNext() ){
        	XYChart.Data data0 = ((XYChart.Data) iter0.next());        	
        	Tooltip.install(data0.getNode(),toolTips0.get(i++));
        	applyMouseEvents(data0);        	
        }
//        datalist0 = bblcData.get(2).getData();
//        iter0 = datalist0.iterator();
//        i = 0;
//        System.out.println(datalist0.size());
//        while(iter0.hasNext() ){
//        	XYChart.Data data0 = ((XYChart.Data) iter0.next());        	
//        	Tooltip.install(data0.getNode(),toolTips2.get(i++));
//        	applyMouseEvents(data0);        	
//        }
//        datalist0 = bblcData.get(3).getData();
//        iter0 = datalist0.iterator();
//        i = 0;        
//        while(iter0.hasNext()){
//        	XYChart.Data data0 = ((XYChart.Data) iter0.next());        	
//        	Tooltip.install(data0.getNode(),toolTips3.get(i++));
//        	applyMouseEvents(data0); 	
//        }
        Scene scene  = new Scene(new Group(), 900, 700);       
        //((Group) scene.getRoot()).setAutoSizeChildren(false);
        ((Group) scene.getRoot()).getChildren().add(bblc);        
        bblc.setPrefSize(850, 650);
        ((Group) scene.getRoot()).getChildren().add(caption);        
        videoLifeSpanFxPanel.setScene(scene);    
    }
	
	void videoLifeSpanBubbleChart(JFXPanel videoLifeSpanFxPanel, int lifeSpanStart, int lifeSpanEnd){    	
    	XYChart.Series videoLife0Series = new XYChart.Series();    	      	
        ArrayList<Tooltip> toolTips0 = new ArrayList<Tooltip>(); 
        
        Date oldestVideoUploadDate = new Date();       
        for (VideoLifeSpan vidLifeSpan : videoLifeSpanDataList){
        	if (oldestVideoUploadDate.compareTo(vidLifeSpan.getUploadDate()) > 0){
        		oldestVideoUploadDate = vidLifeSpan.getUploadDate();
        	}  
        }
        Date latestVideoUploadDate = oldestVideoUploadDate;
        for (VideoLifeSpan vidLifeSpan : videoLifeSpanDataList){
        	if (latestVideoUploadDate.compareTo(vidLifeSpan.getUploadDate()) < 0){
        		latestVideoUploadDate = vidLifeSpan.getUploadDate();
        	}        	
        }
        // DateTime of OldestVideo Uploaded on this Topic
    	Calendar calendar = new GregorianCalendar(); 
    	calendar.setTime(oldestVideoUploadDate);	
    	DateTime oldestVideoUploadDateTime = new DateTime().withDate(calendar.get(Calendar.YEAR), calendar.get(calendar.MONTH)+1, calendar.get(calendar.DAY_OF_MONTH));
    	// DateTime of LatestVideo Uploaded on this topic    	
    	calendar.setTime(latestVideoUploadDate);	
    	DateTime latestVideoUploadDateTime = new DateTime().withDate(calendar.get(Calendar.YEAR), calendar.get(calendar.MONTH)+1, calendar.get(calendar.DAY_OF_MONTH));
    	// Time Difference (in months) between latest, oldest 
        Months mt = Months.monthsBetween(oldestVideoUploadDateTime, latestVideoUploadDateTime);         
        int numTotalMonths = mt.getMonths();
        int maxCommentCount = 0;
    	int minCommentCount = 10000000; // Arbitrarily Large Number
    	
    	
        caption.setTextFill(Color.DARKORANGE);
        caption.setStyle(LABEL_FONT_STYLE);
    	
        int k = 0;
        for (VideoLifeSpan vidLifeSpan : videoLifeSpanDataList){        	
        	// DateTime of this Video
        	calendar.setTime(vidLifeSpan.getUploadDate());						// CreatedOn
        	DateTime thisVideoUploadDateTime = new DateTime().withDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1,calendar.get(Calendar.DAY_OF_MONTH));
        	// Time Difference (in months) between this and oldest video
        	Months monthsBetween = Months.monthsBetween(oldestVideoUploadDateTime, thisVideoUploadDateTime);         
            int uploadMonthNumber = monthsBetween.getMonths(); 	
        	// GET THE CommentCount for Y-Axis        	
        	int commentCount = 0;
        	commentCount = vidLifeSpan.getCommentsCount()/10;						// CommentsCount
        	if (commentCount > maxCommentCount) {
        		// Required to set Y-Axis Upper-Limit
        		maxCommentCount = commentCount;        		
        	}        	
        	if (commentCount < minCommentCount) {
        		// Required to set Y-Axis Upper-Limit
        		minCommentCount = commentCount;        		
        	}
        	float videoLifeSpanInMonths = vidLifeSpan.getLifeSpanInMonths();
        	float videoLifeSpan = (videoLifeSpanInMonths/(numTotalMonths*1.0F))*10;
        	if (videoLifeSpan != 0.0F){		            		
        		if (videoLifeSpan > 10){
        			System.out.println("Ooops");        			
        		}              		
        		if (videoLifeSpanInMonths < lifeSpanEnd && videoLifeSpanInMonths > lifeSpanStart ){
        			final XYChart.Data data0 = new XYChart.Data(uploadMonthNumber, commentCount, videoLifeSpan);
        			videoLife0Series.getData().add(data0); 
        			
        			Format formatter = new SimpleDateFormat("yyyy-MM-dd");
        			calendar = Calendar.getInstance();
        			calendar.setTime(vidLifeSpan.getUploadDate());
        			String strUploadDate = formatter.format(calendar.getTime());        			
        			calendar.setTime(vidLifeSpan.getLastCommentDate());
        			String strLastActivityDate = formatter.format(calendar.getTime());
        			
        			Tooltip tooltip = new Tooltip(
        					" Upload date " + strUploadDate +
        					"\n Last activity date " + strLastActivityDate +
        					"\n Lifetime (Months) " + videoLifeSpanInMonths + " (percentage "+ videoLifeSpan +")"+
        					"\n Views " + vidLifeSpan.getViewsCount() +
        					"\n Comments " + commentCount + "0 " + 
        					"\n Video Title " + vidLifeSpan.getVideoTitle() +
        					"\n Video Author " + vidLifeSpan.getAuthor());
        			tooltip.setStyle("-fx-font: 14 arial;  -fx-font-smoothing-type: lcd;");
        			toolTips0.add(tooltip);
        		}      			
        	}
        }
        maxCommentCount = (maxCommentCount)+5;
    	int imaxCommentCount = (int)maxCommentCount;
    	int tickMarkSize = 10;
    	if (imaxCommentCount > 100){
    		int n = (int)(imaxCommentCount/tickMarkSize);
    		tickMarkSize = (int)(imaxCommentCount/n);
    	}
        
    	final NumberAxis yAxis = new NumberAxis(0, (int)maxCommentCount+5, tickMarkSize); //(minX, maxX, Tick)
        final NumberAxis xAxis = new NumberAxis(0, numTotalMonths, 5);//(minY, maxY, Tick)
        final BubbleChart<Number,Number> bblc = new BubbleChart<Number,Number>(xAxis,yAxis);      
        
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        
        try 
        {
        	oldestVideoUploadDate = formatter.parse(oldestVideoUploadDate.toString());
        }
        catch (Exception e)
        {
        }
        String strOldestVideoUploadDate = formatter.format(oldestVideoUploadDate);
        try 
        {
        	latestVideoUploadDate = formatter.parse(latestVideoUploadDate.toString());
        }
        catch (Exception e)
        {
        }
        String strLatestVideoUploadDate = formatter.format(latestVideoUploadDate);
        xAxis.setLabel("Upload Month No. (Since "+ strOldestVideoUploadDate +" Until " + strLatestVideoUploadDate +")");
        yAxis.setLabel("Comments Count Max Comments " + (maxCommentCount-5) + "0" );   
        yAxis.setTickLabelFormatter(new NumberAxis.DefaultFormatter(yAxis,null, "0"));
        bblc.setTitle(lifeSpanStart + "~" + lifeSpanEnd + " months lifespan videos");
        ObservableList<XYChart.Series<Number, Number>> bblcData = FXCollections.observableArrayList();
        bblcData.addAll(videoLife0Series);       
        bblc.setData(bblcData);     
        bblc.setLegendVisible(false);
        ObservableList datalist0 = bblcData.get(0).getData();
        Iterator iter0 = datalist0.iterator();        
        int i = 0;
        while(iter0.hasNext() ){
        	XYChart.Data data0 = ((XYChart.Data) iter0.next());        	
        	Tooltip.install(data0.getNode(),toolTips0.get(i++));
        	applyMouseEvents(data0);        	
        }
        Scene scene  = new Scene(new Group(), 600, 400);       
        //((Group) scene.getRoot()).setAutoSizeChildren(false);
        ((Group) scene.getRoot()).getChildren().add(bblc);        
        bblc.setPrefSize(550, 400);
        ((Group) scene.getRoot()).getChildren().add(caption);        
        videoLifeSpanFxPanel.setScene(scene);    
    }
	
	private void applyMouseEvents(final XYChart.Data dataNode) {

        final Node node = dataNode.getNode();
        
        node.setOnMouseEntered(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent arg0) {
                node.setEffect(ds);
                node.setCursor(Cursor.HAND);
            }
        });
        node.setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent arg0) {
                node.setEffect(null);
                node.setCursor(Cursor.DEFAULT);
            }
        });        
    }
	void populateCategoriesCombo(){
    	try{    		
    		Connection conn = dataManager.getDBConnection();
    		System.out.println("DB Connection Secured.\n");    		
			Statement categoriesQuery = conn.createStatement();
			ResultSet categoriesResultSet = categoriesQuery.executeQuery(
			"SELECT DISTINCT(Category) FROM utubevideos"); 
			System.out.println("Data fetched.\n"); 
			
			int rowCount = 0;
			
			while(categoriesResultSet.next()){
				rowCount ++;				
		    }
			String categoriesArray[] = new String [rowCount]; 
			categoriesResultSet.beforeFirst();
			rowCount = 0;
			while(categoriesResultSet.next()){
				String category = categoriesResultSet.getString("Category");
				categoriesArray[rowCount++] = category;
			}
			videoCategoriesCombo = new JComboBox<String>(categoriesArray);
			conn.close();
			System.out.println("DB Connection Closed.\n");
			
    	}
	    catch (SQLException e){
	    	System.out.println("SQL code does not execute." + e.getMessage());
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}   	
    	
    }
    private void initFX(JFXPanel fxPanel) {
    	//durationHistogram(fxPanel);    	
    }
    public void initAndShowGUI() {
        // This method is invoked on the EDT thread
    	categoriesTabbedPane = new JTabbedPane();    	
    	JPanel videoLifeSpanPanel = new JPanel();
    	categoriesTabbedPane.add("All Categories", videoLifeSpanPanel);
        final JFXPanel videoLifeSpanFxPanel1 = new JFXPanel();
        final JFXPanel videoLifeSpanFxPanel2 = new JFXPanel();
        final JFXPanel videoLifeSpanFxPanel3 = new JFXPanel();
        final JFXPanel videoLifeSpanFxPanel4 = new JFXPanel();
        videoLifeSpanPanel.setLayout(new GridLayout(2,2));
        videoLifeSpanPanel.add(videoLifeSpanFxPanel1);     
        videoLifeSpanPanel.add(videoLifeSpanFxPanel2);        
        videoLifeSpanPanel.add(videoLifeSpanFxPanel3);
        videoLifeSpanPanel.add(videoLifeSpanFxPanel4);
        panel.setLayout(new BorderLayout());
        JPanel localControlsPanel = new JPanel();
        populateCategoriesCombo();
        JLabel label  = new JLabel();
        label.setText("Select desired  video category");
        btnLaunchUploadHistoryGraph = new JButton(" Fire !");
        btnLaunchUploadHistoryGraph.addActionListener(new ActionListener() {
       	 @Override
       	public void actionPerformed(java.awt.event.ActionEvent arg0) {        		
				final String category = videoCategoriesCombo.getSelectedItem().toString();				 
				final int videoViewsCountLimit = Integer.parseInt(videoViewsLimitCombo.getSelectedItem().toString());
				final JFXPanel videoLifeSpanFxPanel1New = new JFXPanel();
		        final JFXPanel videoLifeSpanFxPanel2New = new JFXPanel();
		        final JFXPanel videoLifeSpanFxPanel3New = new JFXPanel();
		        final JFXPanel videoLifeSpanFxPanel4New = new JFXPanel();
		        JPanel videoLifeSpanPanel = new JPanel();
		        videoLifeSpanPanel.setLayout(new GridLayout(2,2));
		        videoLifeSpanPanel.add(videoLifeSpanFxPanel1New);     
		        videoLifeSpanPanel.add(videoLifeSpanFxPanel2New);        
		        videoLifeSpanPanel.add(videoLifeSpanFxPanel3New);
		        videoLifeSpanPanel.add(videoLifeSpanFxPanel4New);
				categoriesTabbedPane.add(category, videoLifeSpanPanel);			
				Platform.runLater(new Runnable() {
		            @Override
		            public void run() {           	
		            	fetchVideoLifeSpanDataFromDB(category, videoViewsCountLimit);
		            	videoLifeSpanBubbleChart(videoLifeSpanFxPanel1New, 0, 12);
		            	videoLifeSpanBubbleChart(videoLifeSpanFxPanel2New, 12, 24);
		            	videoLifeSpanBubbleChart(videoLifeSpanFxPanel3New, 24, 48);
		            	videoLifeSpanBubbleChart(videoLifeSpanFxPanel4New, 48, 56);              
		            }
		       });				
			}			
		});
        localControlsPanel.add(label);
        localControlsPanel.add(videoCategoriesCombo);
        JLabel label2 = new JLabel();
        label2.setText(" Select views count limit ");
        localControlsPanel.add(label2);        
        String viewsCountLimitArray[] = {"10000", "100000", "1000000", "10000000"};; 
        videoViewsLimitCombo = new JComboBox(viewsCountLimitArray);
        localControlsPanel.add(videoViewsLimitCombo);
        localControlsPanel.add(btnLaunchUploadHistoryGraph);
        panel.add(localControlsPanel, BorderLayout.NORTH);
    	panel.add(categoriesTabbedPane, BorderLayout.CENTER);       
        panel.setVisible(true);
        
        
        Platform.runLater(new Runnable() {
            @Override
            public void run() {          
            	fetchVideoLifeSpanDataFromDB("", 10000);
            	videoLifeSpanBubbleChart(videoLifeSpanFxPanel1, 0, 12);
            	videoLifeSpanBubbleChart(videoLifeSpanFxPanel2, 12, 24);
            	videoLifeSpanBubbleChart(videoLifeSpanFxPanel3, 24, 48);
            	videoLifeSpanBubbleChart(videoLifeSpanFxPanel4, 48, 56);
            }
       });        
    }
    public void fire() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
            initAndShowGUI();
            }
        });
    }	
}
class VideoLifeSpan{
	private Date uploadDate; public void setUploadDate(Date date){uploadDate = date;} public Date getUploadDate(){return uploadDate;}
	private Date firstCommentDate; public void setfirstCommentDate(Date date){firstCommentDate = date;} public Date getFirstCommentDate(){return firstCommentDate;}
	private Date lastCommentDate; public void setLastCommentDate(Date date){lastCommentDate = date;} public Date getLastCommentDate(){return lastCommentDate;}
	
	public int getLifeSpanInDays(){
		long diffTime = lastCommentDate.getTime() - uploadDate.getTime();
		int diffDays = (int)(diffTime / (1000 * 60 * 60 * 24));		
		return diffDays;
	}
	public int getLifeSpanInMonths(){
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(uploadDate);
		DateTime uploadDateTime = new DateTime().withDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DAY_OF_MONTH));
		calendar.setTime(lastCommentDate);
		DateTime lastCommentDateTime = new DateTime().withDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DAY_OF_MONTH));
		Months lifeInMonths = Months.monthsBetween(uploadDateTime, lastCommentDateTime);
		int n = lifeInMonths.getMonths();
		return n;
	}
	
	private int commentsCount; public void setCommentsCount(int comCount){commentsCount = comCount;} public int getCommentsCount(){return commentsCount;}
	private int viewsCount; public void setViewsCount(int viewsCount){this.viewsCount = viewsCount;} public int getViewsCount(){return viewsCount;}
	private String author; public void setAuthor(String author){this.author = author;} public String getAuthor(){return author;}
	private String title;public void setVideoTitle(String title){this.title = title;} public String getVideoTitle(){return title;}
	private String ID;public void setVideoID(String ID){this.ID = ID;} public String getVideoID(){return ID;}	
}
