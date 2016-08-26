package yt.vis;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
import java.util.Observable;
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

import com.google.gdata.data.apt.DataAnnotationProcessorFactory;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.JFXPanel;
import javafx.event.ActionEvent;
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
import javafx.scene.chart.PieChart;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.chart.XYChart.Series;
 
public class VideoPopularityStats {  
    public static JPanel panel = new JPanel();
    JTabbedPane categoriesTabbedPane;
    JComboBox<String> videoCategoriesCombo;
    JComboBox<String> videoViewsLimitCombo;
    JButton btnLaunchUploadHistoryGraph;
    final String LABEL_FONT_STYLE = "-fx-font: 16 arial;";
    private ArrayList<VideoDurationCategory> vidDurationDataList;
    int VIEWS_COUNT_LIMIT = 1000000;
    
    ContextMenu contextMenu;
    private XYChart.Series selectedSeries;
    private XYChart.Data selectedData;
    private DropShadow ds = new DropShadow();
    uTubeDataManager dataManager;
    public VideoPopularityStats(uTubeDataManager dataManager) {
		this.dataManager = dataManager;		
		vidDurationDataList = new ArrayList<VideoDurationCategory>();	
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
    void videoDurationHistogram(JFXPanel fxPanel){    	
        //stage.setTitle("Duration Histogram");
        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();
        final BarChart<String,Number> bc = 
            new BarChart<String,Number>(xAxis,yAxis);
        bc.setTitle("Video Duration and Authors");
       
        XYChart.Series<String, Number> series1 = new XYChart.Series<String, Number>();
        XYChart.Series<String, Number> series2 = new XYChart.Series<String, Number>();
        series1.setName("Duration");
        series2.setName("Authors");        
        ArrayList categories = new ArrayList<String>();
        int i = 0;
        for(VideoDurationCategory vidDurationCat : vidDurationDataList){        	        	     
	       	categories.add(vidDurationCat.getDurationRange());
        	series1.getData().add(new XYChart.Data<String, Number>(vidDurationCat.getDurationRange(), vidDurationCat.getVideoCount()));	       	
	       	series2.getData().add(new XYChart.Data<String, Number>(vidDurationCat.getDurationRange(), vidDurationCat.getAuthorsCount()));	       	
        }
       
        xAxis.setLabel("Years");
        ObservableList categoriesObsList = FXCollections.observableList(categories);
        xAxis.setCategories(categoriesObsList);
        
        yAxis.setLabel("Videos Count");
        
        Scene scene  = new Scene(bc,800,600);
        bc.getData().addAll(series1, series2);
        fxPanel.setScene(scene);        
    } 
    void videoViewsStatsChart(JFXPanel likeDislikeFavPanel, final ArrayList<VideoPopularityInfo> vidPopDataList){    	
    	final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();
         xAxis.setLabel("Videos  (Ordered by Views)");
        final LineChart lineChart = new LineChart(xAxis,yAxis);
        VIEWS_COUNT_LIMIT = Integer.parseInt(videoViewsLimitCombo.getSelectedItem().toString());
    	
        lineChart.setTitle("Video Views (videos above " + VIEWS_COUNT_LIMIT +" views)");     
        
        ObservableList<XYChart.Series<String, Number>> lineChartData = FXCollections.observableArrayList();
        Series<String, Number> aSeries = new Series<String, Number>();        
        aSeries.setName("a");        
        
        int i = 0;
        for(VideoPopularityInfo vidPop :  vidPopDataList){        	
        	aSeries.getData().add(new XYChart.Data(Integer.toString(i++), vidPop.getViewsCount()));
        }
        
        lineChartData.addAll(aSeries);        
        lineChart.setData(lineChartData);       
        lineChart.setLegendVisible(false);        
        
        ArrayList<Tooltip> toolTipsList = new ArrayList<Tooltip>();
        ObservableList datalist = lineChartData.get(0).getData();
        Iterator iter = datalist.iterator();
        final Label caption = new Label("");
        caption.setTextFill(Color.DARKORANGE);
        caption.setStyle(LABEL_FONT_STYLE);
        
        while(iter.hasNext()){
        	final XYChart.Data data = ((XYChart.Data) iter.next());
        	data.getNode().addEventHandler(MouseEvent.MOUSE_MOVED,
                    new EventHandler<MouseEvent>() {
                        @Override public void handle(MouseEvent e) {
                            caption.setTranslateX(e.getSceneX());
                            caption.setTranslateY(e.getSceneY());
                            int vidIndex = Integer.parseInt(data.getXValue().toString());
                            VideoPopularityInfo vidPopObject = vidPopDataList.get(vidIndex);
                            caption.setText(String.valueOf(" Author: " + vidPopObject.getVideoAuthor() + 
                            								" \n TiTle: " + vidPopObject.getVideoTitle() + 
                            								" \n Index: " + vidIndex));
                         }
                    });
        }
        Scene scene  = new Scene(new Group());       
        ((Group) scene.getRoot()).getChildren().add(lineChart);
        ((Group) scene.getRoot()).getChildren().add(caption);        
        likeDislikeFavPanel.setScene(scene);         
    }
    void videoLikesStatsChart(JFXPanel likeDislikeFavPanel, final ArrayList<VideoPopularityInfo> vidPopDataList){   	    	
    	final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Videos  (Ordered by Views)");
        final LineChart lineChart = new LineChart(xAxis,yAxis);        
        lineChart.setTitle("Likes");                          
        
        ObservableList<XYChart.Series<String, Number>> lineChartData = FXCollections.observableArrayList();
        Series<String, Number> aSeries = new Series<String, Number>();        
        aSeries.setName("a");       
        int i = 0;
        for(VideoPopularityInfo vidPop :  vidPopDataList){        	
        	aSeries.getData().add(new XYChart.Data(Integer.toString(i++), vidPop.getLikesCount()));
        } 
        
        lineChartData.addAll(aSeries);        
        lineChart.setData(lineChartData);       
        lineChart.setLegendVisible(false);
               
        ObservableList datalist = lineChartData.get(0).getData();
        Iterator iter = datalist.iterator();
        i = 0;
        final Label caption = new Label("");
        caption.setTextFill(Color.DARKORANGE);
        caption.setStyle(LABEL_FONT_STYLE);
        
        while(iter.hasNext()){
        	final XYChart.Data data = ((XYChart.Data) iter.next());
        	data.getNode().addEventHandler(MouseEvent.MOUSE_MOVED,
                    new EventHandler<MouseEvent>() {
                        @Override public void handle(MouseEvent e) {
                            caption.setTranslateX(e.getSceneX());
                            caption.setTranslateY(e.getSceneY());
                            int vidIndex = Integer.parseInt(data.getXValue().toString());
                            VideoPopularityInfo vidPopObject = vidPopDataList.get(vidIndex);
                            caption.setText(String.valueOf(" Author: " + vidPopObject.getVideoAuthor() + 
                            								" \n TiTle: " + vidPopObject.getVideoTitle() + 
                            								" \n Index: " + vidIndex));
                         }
                    });
        }
        Scene scene  = new Scene(new Group());       
        ((Group) scene.getRoot()).getChildren().add(lineChart);
        ((Group) scene.getRoot()).getChildren().add(caption);        
        likeDislikeFavPanel.setScene(scene);               
    }
    void videoDisLikesStatsChart(JFXPanel likeDislikeFavPanel, final ArrayList<VideoPopularityInfo> vidPopDataList){    	
    	final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();
         xAxis.setLabel("Videos  (Ordered by Views)");
        final LineChart lineChart = new LineChart(xAxis,yAxis);       
        lineChart.setTitle("Dislikes");
                          
        ObservableList<XYChart.Series<String, Number>> lineChartData = FXCollections.observableArrayList();
        Series<String, Number> aSeries = new Series<String, Number>();        
        aSeries.setName("a");       
        
        for(int i = 0 ; i < vidPopDataList.size() ; i ++){
        	VideoPopularityInfo vidPop = vidPopDataList.get(i);        	
        	aSeries.getData().add(new XYChart.Data(Integer.toString(i), vidPop.getDislikesCount()));
        } 
        
        lineChartData.addAll(aSeries);        
        lineChart.setData(lineChartData);       
        lineChart.setLegendVisible(false);
        
        ObservableList datalist = lineChartData.get(0).getData();
        Iterator iter = datalist.iterator();
        int i = 0;
        final Label caption = new Label("");
        caption.setTextFill(Color.DARKORANGE);
        caption.setStyle(LABEL_FONT_STYLE);
        
        while(iter.hasNext()){
        	final XYChart.Data data = ((XYChart.Data) iter.next());
        	data.getNode().addEventHandler(MouseEvent.MOUSE_MOVED,
                    new EventHandler<MouseEvent>() {
                        @Override public void handle(MouseEvent e) {
                            caption.setTranslateX(e.getSceneX());
                            caption.setTranslateY(e.getSceneY());
                            int vidIndex = Integer.parseInt(data.getXValue().toString());
                            VideoPopularityInfo vidPopObject = vidPopDataList.get(vidIndex);
                            caption.setText(String.valueOf(" Author: " + vidPopObject.getVideoAuthor() + 
                            								" \n TiTle: " + vidPopObject.getVideoTitle() + 
                            								" \n Index: " + vidIndex));
                         }
                    });
        }
        Scene scene  = new Scene(new Group());       
        ((Group) scene.getRoot()).getChildren().add(lineChart);
        ((Group) scene.getRoot()).getChildren().add(caption);        
        likeDislikeFavPanel.setScene(scene);               
    }
    void videoFavoritesStatsChart(JFXPanel likeDislikeFavPanel, ArrayList<VideoPopularityInfo> vidPopDataList){   	
    	final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();
         xAxis.setLabel("Month");
        final LineChart<String,Number> lineChart = 
                new LineChart<String,Number>(xAxis,yAxis);
              
        lineChart.setTitle("Favorites");
                          
        XYChart.Series series1 = new XYChart.Series();
        lineChart.setLegendVisible(false);
        
        for(int i = 0 ; i < vidPopDataList.size() ; i ++){
        	VideoPopularityInfo vidPop = vidPopDataList.get(i);  
        	series1.getData().add(new XYChart.Data(i+"", vidPop.getFavCount()));	       	
        }                
        Scene scene  = new Scene(lineChart,800,600);       
        lineChart.getData().addAll(series1);
       
        likeDislikeFavPanel.setScene(scene);        
    }
    void videoCommentsStatsChart(JFXPanel likeDislikeFavPanel, final ArrayList<VideoPopularityInfo> vidPopDataList){   	    	
    	final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Videos  (Ordered by Views)");
        yAxis.setLabel("Comments Received");
        final LineChart lineChart = new LineChart(xAxis,yAxis);        
        lineChart.setTitle("Comments");                          
        
        ObservableList<XYChart.Series<String, Number>> lineChartData = FXCollections.observableArrayList();
        Series<String, Number> aSeries = new Series<String, Number>();        
        aSeries.setName("a");       
        int i = 0;
        ArrayList<Tooltip> toolTipsList = new ArrayList<Tooltip>();
        for(VideoPopularityInfo vidPop :  vidPopDataList){        	
        	aSeries.getData().add(new XYChart.Data(Integer.toString(i++), vidPop.getCommentsCount()));        	
        } 
        
        lineChartData.addAll(aSeries);        
        lineChart.setData(lineChartData);       
        lineChart.setLegendVisible(false);
               
        ObservableList datalist = lineChartData.get(0).getData();
        Iterator iter = datalist.iterator();
        i = 0;
        
        final Label caption = new Label("");
        caption.setTextFill(Color.DARKORANGE);
        caption.setStyle(LABEL_FONT_STYLE);
        
        while(iter.hasNext()){
        	final XYChart.Data data = ((XYChart.Data) iter.next());
        	data.getNode().addEventHandler(MouseEvent.MOUSE_MOVED,
                    new EventHandler<MouseEvent>() {
                        @Override public void handle(MouseEvent e) {
                            caption.setTranslateX(e.getSceneX());
                            caption.setTranslateY(e.getSceneY());
                            int vidIndex = Integer.parseInt(data.getXValue().toString());
                            VideoPopularityInfo vidPopObject = vidPopDataList.get(vidIndex);
                            caption.setText(String.valueOf(" Author: " + vidPopObject.getVideoAuthor() + 
                            								" \n TiTle: " + vidPopObject.getVideoTitle() + 
                            								" \n Index: " + vidIndex));
                         }
                    });
        }
        Scene scene  = new Scene(new Group());       
        ((Group) scene.getRoot()).getChildren().add(lineChart);
        ((Group) scene.getRoot()).getChildren().add(caption);        
        likeDislikeFavPanel.setScene(scene);        
    }
    ArrayList<VideoPopularityInfo> fetchVideoPopularityData(){   	    	
    	try{
    		String dataFilePath = "";
    		dataFilePath = VideoPopularityStats.class.getResource("Data/VideoPopularityData.csv").getPath();
    		InputStreamReader fileReader = new InputStreamReader(new FileInputStream(dataFilePath), "UTF-8");

    		ArrayList<VideoPopularityInfo> vidPopDataList = new ArrayList<VideoPopularityInfo>();
    		//FileReader fileReader = new FileReader(dataFilePath);
    	    BufferedReader lineReader = new BufferedReader(fileReader);    	    
    	    String line = lineReader.readLine();    	    
    	    line = lineReader.readLine();  // Skip headers    	    
    	    while (line != null)
    	    {   	    	
    	    	VideoPopularityInfo vidPop = new VideoPopularityInfo();   	    	
    	    	
    	    	String s = line.substring(0, line.indexOf(',')); 	// ViewsCont    	    	
    	    	vidPop.setViewsCount(Integer.parseInt(s));
    	    	line = line.substring(line.indexOf(',')+1);
    	    	
    	    	s = line.substring(0, line.indexOf(',')); 			// Likes
    	    	vidPop.setLikesCount(Integer.parseInt(s));
    	    	line = line.substring(line.indexOf(',')+1);
    	    	
    	    	s = line.substring(0, line.indexOf(',')); 			// DisLikes
    	    	vidPop.setDislikesCount(Integer.parseInt(s));
    	    	line = line.substring(line.indexOf(',')+1);
    	    	
    	    	s = line.substring(0, line.indexOf(',')); 			// FavoritesLikes
    	    	vidPop.setFavCount(Integer.parseInt(s));
    	    	line = line.substring(line.indexOf(',')+1);
    	    	
    	    	s = line.substring(0, line.indexOf(',')); 			// VideoTitle
    	    	vidPop.setVideoTile(s);
    	    	line = line.substring(line.indexOf(',')+1);
    	    	
    	    	s = line.substring(0);			 					// VideoAuthor
    	    	vidPop.setVideoAuthor(s);    	    	
    	    	VIEWS_COUNT_LIMIT = Integer.parseInt(videoViewsLimitCombo.getSelectedItem().toString());
    	    	if (vidPop.getViewsCount() > VIEWS_COUNT_LIMIT){
    	    		vidPopDataList.add(vidPop);    	    		
    	    	}
    	    	
    	    	line = lineReader.readLine();    	    	    	    	
    	    }
    	    fileReader.close();
    	    lineReader.close();    
    	    return vidPopDataList;
    	}catch(Exception e){
    		e.printStackTrace();
    	}   	
    	return null;
    }
    ArrayList<VideoPopularityInfo>  fetchVideoPopularityDataFromDB(String videoCategory){
    	ArrayList<VideoPopularityInfo> vidPopDataList = new ArrayList<VideoPopularityInfo>();
		try{  
    		
    		VIEWS_COUNT_LIMIT = Integer.parseInt(videoViewsLimitCombo.getSelectedItem().toString());
	    	
    		Connection conn = dataManager.getDBConnection();
    		System.out.println("DB Connection Secured.\n");    		
			Statement yearlyUploadHistoryQuery = conn.createStatement();
			ResultSet yearlyUploadHistoryResultSet = yearlyUploadHistoryQuery.executeQuery(
			"SELECT CommentsCount, ViewsCount, Likes, Dislikes, FavoritesCount, Title, Author " +
			"FROM utubevideos " +
			"WHERE ViewsCount > "+ VIEWS_COUNT_LIMIT + " " +
			"AND Category LIKE '%"+ videoCategory +"%'" +
			"ORDER BY ViewsCount DESC LIMIT 10");
			System.out.println("Data fetched.\n");
			
			while(yearlyUploadHistoryResultSet.next()){
					VideoPopularityInfo vidPop = new VideoPopularityInfo();
					vidPop.setCommentsCount(yearlyUploadHistoryResultSet.getInt("CommentsCount"));
		        	vidPop.setViewsCount(yearlyUploadHistoryResultSet.getInt("ViewsCount"));
		        	vidPop.setLikesCount(yearlyUploadHistoryResultSet.getInt("Likes"));
		        	vidPop.setDislikesCount(yearlyUploadHistoryResultSet.getInt("Dislikes"));
					vidPop.setFavCount(yearlyUploadHistoryResultSet.getInt("FavoritesCount"));
					vidPop.setVideoAuthor(yearlyUploadHistoryResultSet.getString("Author"));
					vidPop.setVideoTile(yearlyUploadHistoryResultSet.getString("Title"));
					
					vidPopDataList.add(vidPop);	
		    }
			conn.close();
			System.out.println("DB Connection Closed.\n");	
			return vidPopDataList;
    	}
	    catch (SQLException e){
	    	System.out.println("SQL code does not execute." + e.getMessage());
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  	
    	return null;
    }
    void fetchVideoDurationData(){   	    	
    	try{
    		String dataFilePath = "";
    		dataFilePath = VideoPopularityStats.class.getResource("Data/VideoDurationData.csv").getPath();
    	    FileReader fileReader = new FileReader(dataFilePath);
    	    BufferedReader lineReader = new BufferedReader(fileReader);    	    
    	    String line = lineReader.readLine();    	    
    	    line = lineReader.readLine(); // Skip headers    	    
    	    while (line != null)
    	    {   	    	
    	    	VideoDurationCategory vidDurationCat = new VideoDurationCategory();
    	    	
    	    	String []strings = new String[6];
    	    	
    	    	String s = line.substring(0, line.indexOf(',')); 	// DurationRange    	    	
    	    	vidDurationCat.setDurationRange(s);
    	    	line = line.substring(line.indexOf(',')+1);
    	    	
    	    	s = line.substring(0, line.indexOf(',')); 			// VideoCount
    	    	vidDurationCat.setVideoCount(Integer.parseInt(s));
    	    	line = line.substring(line.indexOf(',')+1);
    	    	
    	    	s = line.substring(0);					 			// AuthorsCount
    	    	vidDurationCat.setAuthorsCount(Integer.parseInt(s));
    	    	    	    	    	    	
    	    	vidDurationDataList.add(vidDurationCat);
    	    	
    	    	line = lineReader.readLine();    	    	    	    	
    	    }
    	    fileReader.close();
    	    lineReader.close();    	    
    	}catch(Exception e){
    		e.printStackTrace();
    	}   	
    }
    void fetchVideoDurationDataFromDB(){
    	try{    		
    		Connection conn = dataManager.getDBConnection();
    		System.out.println("DB Connection Secured.\n");    		
			Statement durationQuery = conn.createStatement();
			ResultSet durationQueryResultSet = durationQuery.executeQuery(
			"		SELECT \'00~05 Min\' as DurationRange, COUNT(Duration) as VideoCount, COUNT(DISTINCT(author)) AS AuthorCount FROM uTubeVideos WHERE Duration BETWEEN 1 and 300"+
			" union SELECT \'05~10 Min\' as DurationRange, COUNT(Duration) as VideoCount, COUNT(DISTINCT(author)) AS AuthorCount FROM uTubeVideos WHERE Duration BETWEEN 301 and 600"+
			" union SELECT \'10~15 Min\' as DurationRange, COUNT(Duration) as VideoCount, COUNT(DISTINCT(author)) AS AuthorCount FROM uTubeVideos WHERE Duration BETWEEN 601 and 900"+
			" union SELECT \'15~20 Min\' as DurationRange, COUNT(Duration) as VideoCount, COUNT(DISTINCT(author)) AS AuthorCount FROM uTubeVideos WHERE Duration BETWEEN 901 and 1200"+
			" union SELECT \'20~   Min\' as DurationRange, COUNT(Duration) as VideoCount, COUNT(DISTINCT(author)) AS AuthorCount FROM uTubeVideos WHERE Duration > 1200"
			);
			System.out.println("Data fetched.\n"); 
			 
			while(durationQueryResultSet.next()){
					VideoDurationCategory vidDurationCat = new VideoDurationCategory();
					vidDurationCat.setDurationRange(durationQueryResultSet.getString("DurationRange"));
					vidDurationCat.setAuthorsCount(durationQueryResultSet.getInt("AuthorCount"));
					vidDurationCat.setVideoCount(durationQueryResultSet.getInt("VideoCount"));
					vidDurationDataList.add(vidDurationCat);
		    }
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
    public void initAndShowGUI() {
        // This method is invoked on the EDT thread
    	categoriesTabbedPane = new JTabbedPane();    	
    	JPanel popularityStatsPanel = new JPanel();
    	categoriesTabbedPane.add("All Categories", popularityStatsPanel);
    	int tabIndex = categoriesTabbedPane.getTabCount();
	 	ButtonTabComponent btnTabComp = new ButtonTabComponent(categoriesTabbedPane);
	 	categoriesTabbedPane.setTabComponentAt(tabIndex-1, btnTabComp);
    	final JFXPanel videoViewsStatsPanel = new JFXPanel();
        final JFXPanel videoLikesStatsPanel = new JFXPanel();
        final JFXPanel videoDisLikesStatsPanel = new JFXPanel();
        final JFXPanel videoCommentsStatsPanel = new JFXPanel();
        popularityStatsPanel.setLayout(new GridLayout(2,2));
        popularityStatsPanel.add(videoViewsStatsPanel);     
        popularityStatsPanel.add(videoLikesStatsPanel);        
        popularityStatsPanel.add(videoCommentsStatsPanel);
        popularityStatsPanel.add(videoDisLikesStatsPanel);
        panel.setLayout(new BorderLayout());
        JPanel localControlsPanel = new JPanel();
        populateCategoriesCombo();
        JLabel label  = new JLabel();
        label.setText("Select desired  video category");
        btnLaunchUploadHistoryGraph = new JButton("Launch Histogram");
        btnLaunchUploadHistoryGraph.addActionListener(new ActionListener() {
        	 @Override
        	public void actionPerformed(java.awt.event.ActionEvent arg0) {        		
				final String category = videoCategoriesCombo.getSelectedItem().toString();				 
				final JFXPanel videoViewsStatsPanel = new JFXPanel();
		        final JFXPanel videoLikesStatsPanel = new JFXPanel();
		        final JFXPanel videoDisLikesStatsPanel = new JFXPanel();
		        final JFXPanel videoCommentsStatsPanel = new JFXPanel();
		        JPanel popularityStatsPanel = new JPanel();
		        popularityStatsPanel.setLayout(new GridLayout(2,2));
		        popularityStatsPanel.add(videoViewsStatsPanel);     
		        popularityStatsPanel.add(videoLikesStatsPanel);        
		        popularityStatsPanel.add(videoCommentsStatsPanel);
		        popularityStatsPanel.add(videoDisLikesStatsPanel);
				categoriesTabbedPane.add(category, popularityStatsPanel);	
				int tabIndex = categoriesTabbedPane.getTabCount();
			 	ButtonTabComponent btnTabComp = new ButtonTabComponent(categoriesTabbedPane);
			 	categoriesTabbedPane.setTabComponentAt(tabIndex-1, btnTabComp);
				Platform.runLater(new Runnable() {
		            @Override
		            public void run() {           	
		            	ArrayList<VideoPopularityInfo> vidPopDataList = fetchVideoPopularityDataFromDB(category);
		            	videoViewsStatsChart(videoViewsStatsPanel, vidPopDataList);    
		            	videoLikesStatsChart(videoLikesStatsPanel, vidPopDataList);
		            	videoCommentsStatsChart(videoCommentsStatsPanel, vidPopDataList);
		            	videoDisLikesStatsChart(videoDisLikesStatsPanel, vidPopDataList);              
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
            	ArrayList<VideoPopularityInfo> vidPopDataList = fetchVideoPopularityDataFromDB("");
            	videoViewsStatsChart(videoViewsStatsPanel, vidPopDataList);    
            	videoLikesStatsChart(videoLikesStatsPanel, vidPopDataList);
            	videoCommentsStatsChart(videoCommentsStatsPanel, vidPopDataList);
            	videoDisLikesStatsChart(videoDisLikesStatsPanel, vidPopDataList);            	
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

class VideoPopularityInfo{
	
	public int getCommentsCount() {
		return commentsCount;
	}
	public void setCommentsCount(int commentsCount) {
		this.commentsCount = commentsCount;
	}
	private int viewsCount, likesCount, dislikesCount, favoritesCount, commentsCount;
	private String videoTitle, videoAuthor;
	public void setViewsCount(int vc){viewsCount = vc;}
	public void setLikesCount(int lc){likesCount = lc;}
	public void setDislikesCount(int dc){dislikesCount = dc;}
	public void setFavCount(int fc){favoritesCount = fc;}
	public void setVideoTile(String vt){videoTitle = vt;}
	public void setVideoAuthor(String va){videoAuthor  = va;}
	
	public int getViewsCount(){return viewsCount;}
	public int getLikesCount(){return likesCount;}
	public int getDislikesCount(){return dislikesCount;}
	public int getFavCount(){return favoritesCount;}
	public String getVideoTitle(){return videoTitle;}
	public String getVideoAuthor(){return videoAuthor;}	
}
class VideoDurationCategory{
	private String durationRange; public void setDurationRange(String s ){durationRange = s;} public String getDurationRange(){return durationRange;}
	private int videosCount; public void setVideoCount(int vc){videosCount = vc;} public int getVideoCount(){return videosCount;}
	private int authorsCount; public void setAuthorsCount(int ac){authorsCount = ac;} public int getAuthorsCount(){return authorsCount;}
	
}
