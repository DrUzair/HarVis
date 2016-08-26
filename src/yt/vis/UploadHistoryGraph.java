package yt.vis;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;

import yt.har.uTubeDataManager;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.BubbleChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
 
public class UploadHistoryGraph {  
    public static JPanel panel = new JPanel();
    uTubeDataManager dataManager;    
    JComboBox<String> videoCategoriesCombo;
    JTabbedPane categoriesTabbedPane;
    JButton btnLaunchUploadHistoryGraph;
    public UploadHistoryGraph(uTubeDataManager dataManager) {
		this.dataManager = dataManager;
	}
    void uploadHistogram(JFXPanel uploadHistogramFxPanel, String videoCategory){    	
    	ArrayList<VideosUploadRecord> videosUploadingDataList = fetchUploadHistoryData(videoCategory);
        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();
        final StackedBarChart<String,Number> bc = 
            new StackedBarChart<String,Number>(xAxis,yAxis);
        bc.setTitle("Monthly Upload History");
       
        XYChart.Series<String, Number> series1 = new XYChart.Series<String, Number>();
        XYChart.Series<String, Number> series2 = new XYChart.Series<String, Number>();
        series1.setName("Videos");
        series2.setName("Content Authors");
        //Set dataSet = data.entrySet();
        
        //Iterator dataSetIterator = dataSet.iterator();
        ArrayList<String> categories = new ArrayList<String>();
        Iterator<VideosUploadRecord> iterator = videosUploadingDataList.iterator();
        while (iterator.hasNext()){
        	VideosUploadRecord videoUploadRecord = iterator.next();        	     
	       	categories.add(videoUploadRecord.getTimePeriod());
        	series1.getData().add(new XYChart.Data<String, Number>(videoUploadRecord.getTimePeriod(), videoUploadRecord.getVideosCount()));
	       	series2.getData().add(new XYChart.Data<String, Number>(videoUploadRecord.getTimePeriod(), videoUploadRecord.getAuthorsCount()));	       	
        }
       
        xAxis.setLabel("Years");
        ObservableList categoriesObsList = FXCollections.observableList(categories);
        xAxis.setCategories(categoriesObsList);
        
        yAxis.setLabel("Videos Count");
        
        Scene scene  = new Scene(bc,1100,750);        
        bc.getData().add(series1);
        bc.getData().add(series2);
        uploadHistogramFxPanel.setScene(scene);
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
    ArrayList<VideosUploadRecord> fetchUploadHistoryData(String videoCategory){  
    	ArrayList<VideosUploadRecord> videosUploadingDataList = new ArrayList<VideosUploadRecord>();
    	try{    		
    		Connection conn = dataManager.getDBConnection();
    		System.out.println("DB Connection Secured.\n");    		
			Statement yearlyUploadHistoryQuery = conn.createStatement();
			ResultSet yearlyUploadHistoryResultSet = yearlyUploadHistoryQuery.executeQuery(
			"SELECT Date_format(CreatedOn, '%Y %M') AS UploadTime, count(*) AS VideosCount, count(DISTINCT(Author)) AS AuthCount from utubevideos " +
			"WHERE Category LIKE '%"+ videoCategory +"%'" +
			"GROUP BY  UploadTime "+
			"ORDER BY CreatedOn");
			System.out.println("Data fetched.\n"); 
			 
			while(yearlyUploadHistoryResultSet.next()){
					VideosUploadRecord videoUploadRecord = new VideosUploadRecord();
					
					videoUploadRecord.setTimePeriod(yearlyUploadHistoryResultSet.getString("UploadTime"));		        	     
					videoUploadRecord.setVideosCount(yearlyUploadHistoryResultSet.getInt("VideosCount"));
		        	videoUploadRecord.setAuthorsCount(yearlyUploadHistoryResultSet.getInt("AuthCount"));
		        	
		        	videosUploadingDataList.add(videoUploadRecord);
		    }
			conn.close();
			System.out.println("DB Connection Closed.\n");
			return videosUploadingDataList;
    	}
	    catch (SQLException e){
	    	System.out.println("SQL code does not execute." + e.getMessage());
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}   	
    	return null;
    }
   
    public void initAndShowGUI() {
        // This method is invoked on the EDT thread
    	categoriesTabbedPane = new JTabbedPane();
        final JFXPanel uploadHistogramFxPanel = new JFXPanel();        
        categoriesTabbedPane.add("All Categories", uploadHistogramFxPanel);
        panel.setLayout(new BorderLayout());
        JPanel localControlsPanel = new JPanel();
        populateCategoriesCombo();
        JLabel label  = new JLabel();
        label.setText("Select desired  video category");
        btnLaunchUploadHistoryGraph = new JButton("Launch Histogram");
        btnLaunchUploadHistoryGraph.addActionListener(new ActionListener() {
        	@Override
			public void actionPerformed(ActionEvent arg0) {        		
				final String category = videoCategoriesCombo.getSelectedItem().toString();				 
				final JFXPanel uploadHistogramFxPanelNew = new JFXPanel();
				categoriesTabbedPane.add(category, uploadHistogramFxPanelNew);			
				Platform.runLater(new Runnable() {
		            @Override
		            public void run() {           	
		            	uploadHistogram(uploadHistogramFxPanelNew, category);              
		            }
		       });				
			}
		});
        localControlsPanel.add(label);
        localControlsPanel.add(videoCategoriesCombo);
        localControlsPanel.add(btnLaunchUploadHistoryGraph);
        panel.add(localControlsPanel, BorderLayout.NORTH);
    	panel.add(categoriesTabbedPane, BorderLayout.CENTER);       
        panel.setVisible(true);   
        
        Platform.runLater(new Runnable() {
            @Override
            public void run() {           	
            	uploadHistogram(uploadHistogramFxPanel, "");              
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

class VideosUploadRecord{
	public String getTimePeriod() {
		return timePeriod;
	}
	public void setTimePeriod(String timePeriod) {
		this.timePeriod = timePeriod;
	}
	public int getVideosCount() {
		return videosCount;
	}
	public void setVideosCount(int videosCount) {
		this.videosCount = videosCount;
	}
	public int getAuthorsCount() {
		return authorsCount;
	}
	public void setAuthorsCount(int authorsCount) {
		this.authorsCount = authorsCount;
	}
	String timePeriod;		        	     
	int videosCount;
	int authorsCount; 
}
