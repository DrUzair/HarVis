package yt.vis;


import java.awt.GridLayout;


import java.io.BufferedReader;

import java.io.FileReader;

import java.sql.Connection;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.Vector;

import java.util.Iterator;



import javax.swing.JPanel;

import javax.swing.SwingUtilities;

import yt.har.uTubeDataManager;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.JFXPanel;
import javafx.event.EventHandler;
import javafx.scene.Cursor;

import javafx.scene.Node;
import javafx.scene.Scene;

import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;

import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;

 
public class CommentAuthorsProfile {	
	final int MIN_COMMENTCOUNT_BOUND = 1;
	private DropShadow ds = new DropShadow();
	uTubeDataManager dataManager;
	ArrayList<CommentAuthorData> commentAuthorsDataList;
	public CommentAuthorsProfile(uTubeDataManager dataManager) {
		this.dataManager = dataManager;
	}
    public JPanel panel = new JPanel();
    void commentAuthorsProfileLineChart(JFXPanel commentAuthorProfileFxPanel){   	
    	//String [][]commentAuthorProfileData = fetchCommentAuthorProfileDataFromDB(commentsOnAllVideos);
    	
    	fetchCommentAuthorProfileDataFromDB();
    	
    	final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Authors");
        yAxis.setLabel("Comments Count");
        ObservableList<XYChart.Series<String, Number>> lineChartData = FXCollections.observableArrayList();
        Series<String, Number> aSeries = new Series<String, Number>();        
        aSeries.setName("a");       
        
        ArrayList<Tooltip> commentAuthorToolTips = new ArrayList<Tooltip>();
        Iterator<CommentAuthorData> commentAuthorProfileDataIter = commentAuthorsDataList.iterator();
        int i = 1; // Data Count
        while(commentAuthorProfileDataIter.hasNext()){
        	CommentAuthorData commentAuthor = (CommentAuthorData)commentAuthorProfileDataIter.next();
        	String author = commentAuthor.getAuthorID();				//Comment Author
        	int commentCount = commentAuthor.getCommentsCount();		// Comment Count
        	int allVidCount = commentAuthor.getAllVideosCount();		// Distinct CommentedOn_AllVideos Count
        	int othersVidCount = commentAuthor.getOthersVideosCount();	// Distinct CommentedOn_OthersVideos Count
        	aSeries.getData().add(new XYChart.Data(Integer.toString(i++), commentCount));
        	Tooltip tooltip = new Tooltip(	"Author:"+ author + 
        									"\n Comments " + commentCount + 
        									"\n Commented on "+ (allVidCount-othersVidCount) +" own videos" +
        									"\n and " + othersVidCount + " others videos");
            tooltip.setStyle("-fx-font: 14 arial;  -fx-font-smoothing-type: lcd;");   
            commentAuthorToolTips.add(tooltip);
        }     
        
        lineChartData.addAll(aSeries);     
        final LineChart<String,Number> lineChart = new LineChart<String,Number>(xAxis,yAxis);
        lineChart.setData(lineChartData);       
        lineChart.setLegendVisible(false);
       	lineChart.setTitle("Comment Authors Profile");
        	
        ObservableList datalist = lineChartData.get(0).getData();
        Iterator iter = datalist.iterator();
        i = 0;
        while(iter.hasNext()){
        	XYChart.Data data = ((XYChart.Data) iter.next());
        	Tooltip.install(data.getNode(), commentAuthorToolTips.get(i++));
        	applyMouseEvents(data);         
        }
        BorderPane pane = new BorderPane();
        pane.setCenter(lineChart); 
        Scene scene  = new Scene(pane,400,400);  
        commentAuthorProfileFxPanel.setScene(scene);                       
    }
    ArrayList<CommentAuthorData>  fetchCommentAuthorProfileData(){
    	commentAuthorsDataList = new ArrayList<CommentAuthorData>();
    	try{
    		String dataFilePath = CommentAuthorsProfile.class.getResource("Data/CommentAuthorsProfileData.csv").getPath();		
    	    
    		FileReader fileReader = new FileReader(dataFilePath);
    	    BufferedReader lineReader = new BufferedReader(fileReader);    	    
    	    String line = lineReader.readLine();
    	    line = lineReader.readLine(); // SKIP The headers line.
    	    while (line != null)
    	    {   	    	
    	    	CommentAuthorData commentAuthor = new CommentAuthorData();
    	    	String author = line.substring(0, line.indexOf(',')); 						// Comment Author Name
    	    	commentAuthor.setAuthorID(author);
    	    	line = line.substring(line.indexOf(',')+1);
    	    	
    	    	int commentCount = Integer.parseInt(line.substring(0, line.indexOf(','))); 	// Comment Count
    	    	commentAuthor.setCommentsCount(commentCount);
    	    	line = line.substring(line.indexOf(',')+1);
    	    	
    	    	int allVideosCount = Integer.parseInt(line.substring(0, line.indexOf(',')));// DISTINCT_AllVideoCount
    	    	commentAuthor.setAllVideosCount(allVideosCount);
    	    	line = line.substring(line.indexOf(',')+1);
    	    	
    	    	int othersVideosCount = Integer.parseInt(line.substring(0));					// DISTINCT_OthersVideoCount
    	    	commentAuthor.setOthersVideosCount(othersVideosCount);
    	    	if (commentCount > MIN_COMMENTCOUNT_BOUND)
    	    		commentAuthorsDataList.add(commentAuthor); 	    	
    	    	line = lineReader.readLine();    	        	    	
    	    }
    	    fileReader.close();
    	    lineReader.close();
    	    return commentAuthorsDataList;
    	}catch(Exception e){
    		e.printStackTrace();
    	}

    	return null;
    }
    ArrayList<CommentAuthorData> fetchCommentAuthorProfileDataFromDB(){
    	commentAuthorsDataList = new ArrayList<CommentAuthorData>();
    	try{    		
    		Connection conn = uTubeDataManager.getDBConnection();
    		System.out.println("DB Connection Secured.\n");    		
			Statement commentAuthorProfileQuery = conn.createStatement();
			String queryString = "";
			
			queryString =	"SELECT Author, CommentsCount, AllVideosCount, OthersVideosCount "+ 
							"FROM CommentAuthorsProfile "+
							"ORDER BY CommentsCount DESC";
			ResultSet commentAuthorProfileResultSet = commentAuthorProfileQuery.executeQuery(queryString);
			System.out.println("Data fetched.\n");			 
			Vector<String []> dataVector = new Vector<String[]>();
			commentAuthorProfileResultSet.beforeFirst();
			while(commentAuthorProfileResultSet.next()){
		        	String author = commentAuthorProfileResultSet.getString("Author");		        	     
		        	Integer comntCount = commentAuthorProfileResultSet.getInt("CommentsCount");
		        	Integer allVidCount = commentAuthorProfileResultSet.getInt("AllVideosCount");
		        	Integer othersVidCount = commentAuthorProfileResultSet.getInt("OthersVideosCount");
		        	if (comntCount > MIN_COMMENTCOUNT_BOUND ){
		        		CommentAuthorData commentAuthor = new CommentAuthorData();
		        		commentAuthor.setAuthorID(author);
		        		commentAuthor.setCommentsCount(comntCount);
		        		commentAuthor.setAllVideosCount(allVidCount);
		        		commentAuthor.setOthersVideosCount(othersVidCount);
		        		commentAuthorsDataList.add(commentAuthor);
			    	}
		    }
			conn.close();
			System.out.println("DB Connection Closed.\n");
			return commentAuthorsDataList;
    	}
	    catch (SQLException e){
	    	System.out.println("SQL code does not execute." + e.getMessage());
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}   	
    	return null;
    }
        
    private void applyMouseEvents(final XYChart.Data series) {
        final Node node = series.getNode();        
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
   
    public void initAndShowGUI() {
    	
        // This method is invoked on the EDT thread
    	 final JFXPanel commentAuthorProfileFxPanel = new JFXPanel();
    	 
    	 panel.setLayout(new GridLayout(1,1));
    	 panel.add(commentAuthorProfileFxPanel);
        
        
    	 panel.setVisible(true);   
    	 Platform.runLater(new Runnable() {
    		 @Override
    		 public void run() {           	
    			 commentAuthorsProfileLineChart(commentAuthorProfileFxPanel);
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

class CommentAuthorData{
	private String authorID; 
	public String getAuthorID(){return authorID;} 
	public void setAuthorID(String id){authorID = id;} 
	private int commentCount; 
	public int getCommentsCount(){return commentCount;} 
	public void setCommentsCount(int commentCount){this.commentCount = commentCount;}
	int othersVideosCount; 
	public int getOthersVideosCount(){return othersVideosCount;} 
	public void setOthersVideosCount(int othersVideosCount){ this.othersVideosCount = othersVideosCount;}
	int allVideosCount; 
	public int getAllVideosCount(){return allVideosCount;} 
	public void setAllVideosCount(int allVideosCount){ this.allVideosCount = allVideosCount;}
}