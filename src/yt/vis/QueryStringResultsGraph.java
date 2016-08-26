package yt.vis;

import java.awt.GridLayout;
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

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;

import yt.har.uTubeDataManager;

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
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
 
public class QueryStringResultsGraph {  
    public JPanel panel;
    ArrayList<Tooltip> toolTips;
    private DropShadow ds = new DropShadow();
    uTubeDataManager dataManager;
    public QueryStringResultsGraph(uTubeDataManager dataManager){
    	this.dataManager = dataManager;
    	this.panel = new JPanel();
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
    void queryStringResultsHistogram(JFXPanel queryStringResultsFxPanel){    	
    	String [][]dataArray = fetchQueryStringResultsData();
        //stage.setTitle("Upload Histogram");
        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();
        
        xAxis.setLabel("Query String Video Results Count");
        yAxis.setLabel("Videos Count");        
        
        ObservableList<XYChart.Series<String, Number>> barChartData = FXCollections.observableArrayList();
        Series<String, Number> aSeries = new Series<String, Number>();
        for(int i = 0 ; i < dataArray[0].length ; i ++){        	
        	String queryString = dataArray[0][i];
        	Integer vidCount = Integer.parseInt(dataArray[1][i]);
        	aSeries.getData().add(new XYChart.Data<String, Number>(i + "", vidCount));        	
        } 
        
        final BarChart<String,Number> barChart = new BarChart<String,Number>(xAxis,yAxis);
        barChart.setTitle("Query String Results");
       
        barChartData.addAll(aSeries);        
        barChart.setData(barChartData);
        barChart.setLegendVisible(false);
        BorderPane pane = new BorderPane();
        pane.setCenter(barChart); 
        Scene scene  = new Scene(pane,1100,750);
        
        ObservableList datalist = barChartData.get(0).getData();
        Iterator iter = datalist.iterator();
        int i = 0;
        while(iter.hasNext()){
        	XYChart.Data data = ((XYChart.Data) iter.next());        	
        	Tooltip.install(data.getNode(), toolTips.get(i++));
        	applyMouseEvents(data);         
        }
        queryStringResultsFxPanel.setScene(scene); 
                
    }
  
    String [][] fetchQueryStringResultsData(){
    	try {
			Connection conn = dataManager.getDBConnection();
			Statement dbSummaryQuery = conn.createStatement();
			ResultSet dbSummaryQueryResultSet = dbSummaryQuery.executeQuery(
					"SELECT QueryString, Count(*) AS QueryResultCount FROM uTubeVideos "+				
					"GROUP BY QueryString "+
					"ORDER BY QueryResultCount DESC");			
			int rowCount = 0;
			while(dbSummaryQueryResultSet.next()){
				int queryRsltCount = dbSummaryQueryResultSet.getInt("QueryResultCount");
				if (queryRsltCount > 50)
					rowCount++;
			}
			String [][]dataArray = new String[2][rowCount];
			rowCount = 0;
			dbSummaryQueryResultSet.beforeFirst();
			toolTips = new ArrayList<Tooltip>();
			while(dbSummaryQueryResultSet.next()){
				String queryString = dbSummaryQueryResultSet.getString("QueryString");
				int queryRsltCount = dbSummaryQueryResultSet.getInt("QueryResultCount");
				if (queryRsltCount > 50){
					dataArray [0][rowCount] = queryString;
					dataArray [1][rowCount] = queryRsltCount+"";
					toolTips.add(new Tooltip(queryString + "\n" + queryRsltCount + " Results."));
					rowCount++;
				}
			}
			conn.close();
			System.out.println("DB Connection Closed.\n");
			return dataArray;
		}	
		catch(Exception e){
			e.printStackTrace();
		}
    	return null;
    }
    
    
//    private Connection getDBConnection() {
//	    try{
//	    	Connection conn = null;
//		    Properties connectionProps = new Properties();
//		    connectionProps.put("user", "uTube");
//		    connectionProps.put("password", "uTubePWD");	    
//		    conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/uTubeDB_test", connectionProps);	    
//		    return conn;
//	    }catch (SQLException sqlExc){
//	    	sqlExc.printStackTrace();
//	    }
//	    return null;
//	}
    public void initAndShowGUI() {
        // This method is invoked on the EDT thread        
        final JFXPanel queryStringResultsFxPanel = new JFXPanel();        
        panel.add(queryStringResultsFxPanel);       
        panel.setVisible(true);    

        Platform.runLater(new Runnable() {
            @Override
            public void run() {           
            	queryStringResultsHistogram(queryStringResultsFxPanel);           
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
