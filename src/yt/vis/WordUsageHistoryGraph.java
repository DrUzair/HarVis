package yt.vis;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
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
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;

import com.google.gdata.data.DateTime;

import yt.har.uTubeDataManager;
import yt.vis.WordUsageHistory.WordUsage;

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

 
public class WordUsageHistoryGraph {  
    public static JPanel panel = new JPanel();
    private final JFXPanel uploadHistogram_ByWordsFxPanel = new JFXPanel();
    private final JFXPanel uploadHistogram_ByAuthorsFxPanel = new JFXPanel();
    private uTubeDataManager dataManager;
    private Integer startingMonth = 1;
    private Integer startingYear = 2005;
    private Integer endingMonth = 1;
    private Integer endingYear = 2013;
	private boolean wordsInCombo = false;
	private JComboBox<String> comboWords = new JComboBox<String>();
	private JComboBox<String> srcTableNamesCombo = new JComboBox<String>();
	private Vector<String> wordsVector = new Vector<String>(); 
	private ArrayList<XYChart.Series<String, Number>> listOfDataSeries_4Words = new ArrayList<XYChart.Series<String,Number>>();
	private ArrayList<XYChart.Series<String, Number>> listOfDataSeries_4Authors = new ArrayList<XYChart.Series<String,Number>>();
	private ArrayList<WordUsageHistory> listOfWordUsageHistories = new ArrayList<WordUsageHistory>();
	private boolean srcTableNamesLoaded;
    public WordUsageHistoryGraph(uTubeDataManager dataManager) {
		this.dataManager = dataManager;
		initAndShowGUI();
	}
    @SuppressWarnings("unchecked")
    private LineChart<String, Number> createLineChart(String title, String xAxisLabel, String yAxisLabel){
    	final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel(xAxisLabel);        
        yAxis.setLabel(yAxisLabel);        
        final LineChart<String,Number> lineChart = new LineChart<String,Number>(xAxis,yAxis);
        lineChart.setTitle(title);       
        return lineChart;
    }
    
	void wordUsageHistogramByWords(JFXPanel uploadHistogramFxPanel){   
		int seriesCount = listOfDataSeries_4Words.size();
		for(int i = 0 ; i < seriesCount ; i++){
			listOfDataSeries_4Words.remove(0);
		}		
        Iterator<WordUsageHistory> listOfWordUsageHitsIter = listOfWordUsageHistories.iterator();
        while (listOfWordUsageHitsIter.hasNext()){ 	
        	WordUsageHistory wordUsageHistory = (WordUsageHistory)listOfWordUsageHitsIter.next();
        	XYChart.Series<String, Number> series = new XYChart.Series<String, Number>();
        	series.setName(wordUsageHistory.word);
        	Iterator<WordUsageHistory.WordUsage> wordUsageListIter = wordUsageHistory.wordUsageList.iterator();
        	while (wordUsageListIter.hasNext()){
        		WordUsageHistory.WordUsage wordUsage = (WordUsageHistory.WordUsage)wordUsageListIter.next();
        		series.getData().add(new XYChart.Data<String, Number>(wordUsage.timePeriod, wordUsage.usageFreq));        		
        	}
        	listOfDataSeries_4Words.add(series);
        }
        
        LineChart<String,Number> lineChart = createLineChart("UoD Word Usage History", "Time Period",  "Word Usage Frequency");
        Scene scene  = new Scene(lineChart,1100,750);        
        Iterator<XYChart.Series<String, Number>> listOfDataSeriesIter = listOfDataSeries_4Words.iterator();
        while (listOfDataSeriesIter.hasNext()){
        	lineChart.getData().add((XYChart.Series<String, Number>)listOfDataSeriesIter.next());
        }        
        uploadHistogramFxPanel.setScene(scene);
    }
    void wordUsageHistogramByAuthors(JFXPanel uploadHistogramFxPanel){
    	int seriesCount = listOfDataSeries_4Authors.size();
    	for(int i = 0 ; i < seriesCount ; i++){
    		listOfDataSeries_4Authors.remove(0);
		}    	
    	Iterator<WordUsageHistory> listOfWordUsageHitsIter = listOfWordUsageHistories.iterator();
        while (listOfWordUsageHitsIter.hasNext()){ 	
        	WordUsageHistory wordUsageHistory = (WordUsageHistory)listOfWordUsageHitsIter.next();
        	XYChart.Series<String, Number> series = new XYChart.Series<String, Number>();
        	series.setName(wordUsageHistory.word);
        	Iterator<WordUsageHistory.WordUsage> wordUsageListIter = wordUsageHistory.wordUsageList.iterator();
        	while (wordUsageListIter.hasNext()){
        		WordUsageHistory.WordUsage wordUsage = (WordUsageHistory.WordUsage)wordUsageListIter.next();
        		series.getData().add(new XYChart.Data<String, Number>(wordUsage.timePeriod, wordUsage.authorsCount));        		
        	}
        	listOfDataSeries_4Authors.add(series);
        }
        
        LineChart<String,Number> lineChart = createLineChart("UoD Authors History", "Time Scale (in months)", "Authors");
        Scene scene  = new Scene(lineChart,1100,750);        
        Iterator<XYChart.Series<String, Number>> listOfDataSeriesIter = listOfDataSeries_4Authors.iterator();
        while (listOfDataSeriesIter.hasNext()){
        	lineChart.getData().add((XYChart.Series<String, Number>)listOfDataSeriesIter.next());
        }       
        uploadHistogramFxPanel.setScene(scene);
    }
  
    void fetchWordsFromDB(String uod_table){
    	try{    		
    		Connection conn = uTubeDataManager.getDBConnection();
    		System.out.println("DB Connection Secured.\n");
    		// FIND starting & ending dates
    		Statement timeFrameQuery = conn.createStatement();
			ResultSet timeFrameQueryResultSet = timeFrameQuery.executeQuery(
			" SELECT MONTH(Date(MIN(CreatedOn))) AS startingMonth, MONTH(Date(MAX(CreatedOn))) AS endingMonth," +
			" YEAR(Date(MIN(CreatedOn))) AS startingYear, YEAR(Date(MAX(CreatedOn))) AS endingYear FROM utubevideos");
			
			while(timeFrameQueryResultSet.next()){
				startingMonth = timeFrameQueryResultSet.getInt("startingMonth");
				startingYear = timeFrameQueryResultSet.getInt("startingYear");
				endingMonth = timeFrameQueryResultSet.getInt("endingMonth");
				endingYear = timeFrameQueryResultSet.getInt("endingYear");
			}
    		// FIND Most Used UoD Words
    		Statement mostUsedWordsQuery = conn.createStatement();
			ResultSet mostUsedWordsResultSet = mostUsedWordsQuery.executeQuery(
			" SELECT DISTINCT(Word) FROM " + uod_table +			
			" ORDER BY WordCount DESC");
			System.out.println("Most Used UoD Words fetched.\n");
			wordsVector.clear();
			while (mostUsedWordsResultSet.next()){
				wordsVector.add(mostUsedWordsResultSet.getString("Word"));
			}
    	}
		catch (SQLException e){
		   	System.out.println("SQL code does not execute." + e.getMessage());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}	
    }
//    
    void fetchWordUsageHistoryData(String word){   	
    	for(int i = 0; i < listOfWordUsageHistories.size() ; i ++){
    		WordUsageHistory wordUsageHistory = listOfWordUsageHistories.get(i);
    		if (wordUsageHistory.word.equalsIgnoreCase(word))
    			return;// Return if the wordUsageHistory for word is already fetched.
    	}
    	try{    		
    		Connection conn = uTubeDataManager.getDBConnection();
    		System.out.println("DB Connection Secured.\n");    			
			int month = startingMonth;
			int year = startingYear;
			WordUsageHistory wordUsageHistory = new WordUsageHistory();
			while(month < endingMonth || year < endingYear) {								
				Statement wordUsageHistoryQuery = conn.createStatement();
				ResultSet wordUsageHistoryResultSet = wordUsageHistoryQuery.executeQuery(													
						" SELECT Count(Title) AS VideosCount, COUNT(DISTINCT(Author)) AS AuthorsCount" +
						" FROM utubevideos" +
						" WHERE (Title LIKE '%"+word+"%' OR Description LIKE '%"+word+"%') AND" +
						" (MONTH(Date(CreatedOn)) = "+month+" AND YEAR(Date(CreatedOn)) = "+year+")");
				wordUsageHistory.word =  word;				
				WordUsageHistory.WordUsage wordUsage = wordUsageHistory.new WordUsage();				
				wordUsage.timePeriod = month+ "-"+ year;
				
				while(wordUsageHistoryResultSet.next()){					
					wordUsage.usageFreq = wordUsageHistoryResultSet.getInt("VideosCount");
					wordUsage.authorsCount = wordUsageHistoryResultSet.getInt("AuthorsCount");					
				}
				System.out.println("WordFred " + wordUsage.usageFreq +", AuthorsCount" + wordUsage.authorsCount);
				ResultSet wordUsageHistoryResultSet2 = wordUsageHistoryQuery.executeQuery(													
						" SELECT Count(*) AS CommentsCount, COUNT(DISTINCT(Author)) AS AuthorsCount" +
						" FROM utubevideocomments" +
						" WHERE (Content LIKE '%"+word+"%') AND" +
						" (MONTH(Date(CreatedOn)) = "+month+" AND YEAR(Date(CreatedOn)) = "+year+")");
				while(wordUsageHistoryResultSet2.next()){					
					wordUsage.usageFreq += wordUsageHistoryResultSet2.getInt("CommentsCount");
					wordUsage.authorsCount += wordUsageHistoryResultSet2.getInt("AuthorsCount");					
				}
				
				System.out.println("WordFred " + wordUsage.usageFreq +", AuthorsCount" + wordUsage.authorsCount);
				wordUsageHistory.wordUsageList.add(wordUsage);
				if (month < 12)
					month ++;
				else{
					month = 1;
					year ++;
				}
			}			
			listOfWordUsageHistories.add(wordUsageHistory);			
			conn.close();
			System.out.println("DB Connection Closed.\n");			
    	}
	    catch (SQLException e){
	    	System.out.println("SQL code does not execute. " + e.getMessage());
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  	
    	
    }
    public void initAndShowGUI() {
        // This method is invoked on the EDT thread    	                
        panel.setLayout(new BorderLayout());
        JPanel cntrlPanel = new JPanel();
                
        if (srcTableNamesLoaded == false){
			Vector<String> tablesNamesVector = uTubeDataManager.getTableNames("uod%");
			for(int i = 0 ; i < tablesNamesVector.size() ; i ++){
				srcTableNamesCombo.addItem(tablesNamesVector.get(i));					
			}
			srcTableNamesCombo.setSelectedItem("UoD_Titles");
			srcTableNamesLoaded = true;
		}
        srcTableNamesCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				fetchWordsFromDB(srcTableNamesCombo.getSelectedItem().toString());
				comboWords.removeAll();
				for(int i = 0 ; i < wordsVector.size() ; i ++){
					comboWords.addItem(wordsVector.get(i));					
				}
			}
		});
        cntrlPanel.add(new JLabel("Select UoD Source"));
        cntrlPanel.add(srcTableNamesCombo);
       
        comboWords.addMouseListener(new MouseAdapter() {
			public void mouseEntered(MouseEvent event) {				
				if (wordsInCombo == false){
					fetchWordsFromDB(srcTableNamesCombo.getSelectedItem().toString());
					for(int i = 0 ; i < wordsVector.size() ; i ++){
						comboWords.addItem(wordsVector.get(i));					
					}
					wordsInCombo = true;
				}
		    }
		});        
        cntrlPanel.add(new JLabel("Select UoD Words"));
        cntrlPanel.add(comboWords);
        JButton btnGo = new JButton("Show");        
        btnGo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {	
				Platform.runLater(new Runnable() {
		            @Override
		            public void run() {
		            	fetchWordUsageHistoryData(comboWords.getSelectedItem().toString());
		            	wordUsageHistogramByWords(uploadHistogram_ByWordsFxPanel);
		            	wordUsageHistogramByAuthors(uploadHistogram_ByAuthorsFxPanel);
		            }
		       });            	            	
			}
		});
        cntrlPanel.add(btnGo );
        JButton btnReset = new JButton("Reset");        
        btnReset.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {	
				Platform.runLater(new Runnable() {
		            @Override
		            public void run() {
		            	listOfWordUsageHistories.clear();
		            	wordUsageHistogramByWords(uploadHistogram_ByWordsFxPanel);
		            	wordUsageHistogramByAuthors(uploadHistogram_ByAuthorsFxPanel);
		            }
		       });            	            	
			}
		});
        cntrlPanel.add(btnReset );
        panel.add(cntrlPanel, BorderLayout.NORTH);
        JPanel fxPanels = new JPanel();
        fxPanels.setLayout(new GridLayout(2, 1));
        fxPanels.add(uploadHistogram_ByWordsFxPanel);
        fxPanels.add(uploadHistogram_ByAuthorsFxPanel);
        
        panel.add(fxPanels, BorderLayout.CENTER);
        panel.setVisible(true);               
    }
       

}

class WordUsageHistory {
	ArrayList<WordUsageHistory.WordUsage> wordUsageList = new ArrayList<WordUsageHistory.WordUsage>();  
	String word;
class WordUsage{	
		String timePeriod;
		int usageFreq;
		int authorsCount;
	}
}