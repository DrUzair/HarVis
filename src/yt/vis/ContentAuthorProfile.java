package yt.vis;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
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
import java.util.Properties;
import java.util.Set;
import java.util.Vector;
import java.util.Date;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;

import yt.HarVis;
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
import javafx.scene.chart.PieChart;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
 
public class ContentAuthorProfile implements ActionListener{  
	int AVG_VIEWCOUNT_BOUND = 10000;
	ArrayList<VideoAuthorProfile> videoAuthorProfileDataList;
	ArrayList<Tooltip> vidAuthorToolTipsList;
	private JTabbedPane categoriesTabbedPane;
	private JComboBox<String> videoCategoriesCombo;
	private JComboBox<String> videoViewsLimitCombo;
	private JButton btnLaunchUploadHistoryGraph;
	private DropShadow ds = new DropShadow();
	uTubeDataManager dataManager;
	public ContentAuthorProfile(uTubeDataManager dataManager) {
		this.dataManager = dataManager;
		videoAuthorProfileDataList = new ArrayList<VideoAuthorProfile>();
		vidAuthorToolTipsList = new ArrayList<Tooltip>();
	}
    public JPanel panel = new JPanel();
    void commentAuthorsProfileLineChart(JFXPanel commentAuthorProfileFxPanel){    	
    	
    	String [][]data = fetchCommentAuthorProfileData();
    	
    	final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Authors");
        final LineChart<String,Number> lineChart = new LineChart<String,Number>(xAxis,yAxis);
                
        lineChart.setTitle("Authors Commenting Profile");
                          
        XYChart.Series series1 = new XYChart.Series();
        series1.setName("Comment Count");
       
        if (data != null){
	        for(int i = 0 ; i < data[0].length ; i ++){
	        	String author = data[0][i];  		        	     
	        	Integer vidCount = Integer.parseInt(data[1][i]);  
	        	series1.getData().add(new XYChart.Data(author, vidCount));        		       	
	        }                
        }
        Scene scene  = new Scene(lineChart,800,600);       
        lineChart.getData().addAll(series1);
        lineChart.setLegendVisible(false);
        commentAuthorProfileFxPanel.setScene(scene);        
    }
    void videoAuthorsProfileLineChart(JFXPanel videoAuthorProfileFxPanel){    	
    	
    	final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Authors");
             
        
        
                            
        ObservableList<XYChart.Series<String, Number>> lineChartData = FXCollections.observableArrayList();
        Series<String, Number> aSeries = new Series<String, Number>();        
        aSeries.setName("a");       
        
        int i = 0;
        for(VideoAuthorProfile vidAuthProfile : videoAuthorProfileDataList){        	        	
        	aSeries.getData().add(new XYChart.Data(Integer.toString(i++), vidAuthProfile.getUploadedVideosCount()));
        } 
        
        final LineChart<String,Number> lineChart = new LineChart<String,Number>(xAxis,yAxis);
        lineChart.setTitle("Author Uploading Profile");
        lineChartData.addAll(aSeries);        
        lineChart.setData(lineChartData);       
        lineChart.setLegendVisible(false);
        
        ObservableList datalist = lineChartData.get(0).getData();
        Iterator iter = datalist.iterator();
        i = 0;
        while(iter.hasNext()){
        	XYChart.Data data = ((XYChart.Data) iter.next());        	
        	Tooltip.install(data.getNode(), vidAuthorToolTipsList.get(i++));
        	applyMouseEvents(data);         
        }
        BorderPane pane = new BorderPane();
        pane.setCenter(lineChart); 
        Scene scene  = new Scene(pane,600,600);  
        videoAuthorProfileFxPanel.setScene(scene);    
    }
    
    void authorsProfilePieChart(JFXPanel authorsVideoCountCategoriesFxPanel){    	
        ObservableList<PieChart.Data> pieChartData =  FXCollections.observableArrayList();
        Iterator<VideoAuthorProfile> iterator = videoAuthorProfileDataList.iterator();
        int dataIndex = 0;
        HashMap<String, Integer> authorsVideoCountCategories = new HashMap<String, Integer>(); 
        int authOf1video = 0;
        int authOf2to5Videos = 0;
        int authOf6to10Videos = 0;
        int authOf10to100Videos = 0;
        int authOfAbove100Videos = 0;
        while (iterator.hasNext()){
        	VideoAuthorProfile videoAuthor = iterator.next();
        	int videosCount = videoAuthor.getUploadedVideosCount();
        	if (videosCount == 1)
        		authOf1video++;
        	else if (videosCount > 1 && videosCount <=5)
        		authOf2to5Videos++;
        	else if (videosCount > 5 && videosCount <=10)
        		authOf6to10Videos++;
        	else if (videosCount > 10 && videosCount <=100)
        		authOf10to100Videos++;
        	else if (videosCount > 100)
        		authOfAbove100Videos++;
        }
        
        
        pieChartData.add(0 , new PieChart.Data(" 1 Video Authors", authOf1video));
        pieChartData.add(1 , new PieChart.Data(" 2 to 5 Videos Authors", authOf2to5Videos));
        pieChartData.add(2 , new PieChart.Data(" 6 to 10 Videos Authors", authOf6to10Videos));
        pieChartData.add(3 , new PieChart.Data(" 10 to 100 Videos Authors", authOf10to100Videos));
        pieChartData.add(4 , new PieChart.Data(" Above 100 Videos Authors", authOfAbove100Videos));
        
        PieChart pieChart = new PieChart(pieChartData);
        pieChart.setTitle("Videos & Authors");
        
        final Label caption = new Label("");
        caption.setTextFill(Color.DARKORANGE);
        caption.setStyle("-fx-font: 24 arial;");
        caption.setVisible(true); 
        
        for (final PieChart.Data data : pieChart.getData()) {
            data.getNode().addEventHandler(MouseEvent.MOUSE_CLICKED,
                new EventHandler<MouseEvent>() {
                    @Override public void handle(MouseEvent e) {
                        caption.setTranslateX(e.getSceneX());
                        caption.setTranslateY(e.getSceneY());
                        caption.setText(String.valueOf(data.getPieValue()) + " videos");                                
                     }
                });
        }       
        pieChart.setPrefSize(1100,700);
        Scene scene  = new Scene(new Group());
        ((Group) scene.getRoot()).getChildren().add(pieChart);
        ((Group) scene.getRoot()).getChildren().add(caption);
        
        authorsVideoCountCategoriesFxPanel.setLayout(new BorderLayout());
        authorsVideoCountCategoriesFxPanel.setScene(scene);                          
    }
    
    
    void videoAuthorsViewsProfileLineChart(JFXPanel videoAuthorProfileFxPanel){    	
    	   	
    	final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Authors");
        final LineChart<String,Number> lineChart = new LineChart<String,Number>(xAxis,yAxis);     
        
        lineChart.setTitle("Average Views of Authors' Videos (Minimum Average View Count)" + AVG_VIEWCOUNT_BOUND);
                          
        ObservableList<XYChart.Series<String, Number>> lineChartData = FXCollections.observableArrayList();
        Series<String, Number> aSeries = new Series<String, Number>();        
        aSeries.setName("a");       
        
        int i = 0;
        for(VideoAuthorProfile vidAuthProfile : videoAuthorProfileDataList){        	        	
        	aSeries.getData().add(new XYChart.Data(Integer.toString(i++), vidAuthProfile.getAvgViewsCount()));
        } 
        
        lineChartData.addAll(aSeries);        
        lineChart.setData(lineChartData);       
        lineChart.setLegendVisible(false);
        
        ObservableList datalist = lineChartData.get(0).getData();
        Iterator iter = datalist.iterator();
        i = 0;
        while(iter.hasNext()){
        	XYChart.Data data = ((XYChart.Data) iter.next());        	
        	Tooltip.install(data.getNode(), vidAuthorToolTipsList.get(i++));
        	applyMouseEvents(data);         
        }
        BorderPane pane = new BorderPane();
        pane.setCenter(lineChart); 
        Scene scene  = new Scene(pane,800,600);  
        videoAuthorProfileFxPanel.setScene(scene);         
    }
    
    String [][] fetchCommentAuthorProfileData(){
    	try{    		
    		Connection conn = uTubeDataManager.getDBConnection();
    		System.out.println("DB Connection Secured.\n");    		
			Statement commentAuthorProfileQuery = conn.createStatement();
			ResultSet commentAuthorProfileResultSet = commentAuthorProfileQuery.executeQuery(
			"SELECT DISTINCT(Author) as Author, COUNT(*) AS CommentCount "+
			"FROM utubevideocomments "+			
			"GROUP BY Author "+ 
			"Order By CommentCount DESC");
			System.out.println("Data fetched.\n");			 
			int rowCount = 0;
			while(commentAuthorProfileResultSet.next()){
				if (commentAuthorProfileResultSet.getInt("CommentCount") > 1)
					rowCount++;
			}
			String [][]dataArray = new String[2][rowCount];
			rowCount = 0;
			commentAuthorProfileResultSet.beforeFirst();
			while(commentAuthorProfileResultSet.next()){
		        	String author = commentAuthorProfileResultSet.getString("Author");		        	     
		        	Integer comntCount = commentAuthorProfileResultSet.getInt("CommentCount"); 	
		        	if (comntCount > 1 ){
			        	dataArray[0][rowCount]= author;
			        	dataArray[1][rowCount]= comntCount.toString();
			        	rowCount++;
		        	}
		    }
			conn.close();
			System.out.println("DB Connection Closed.\n");
			return dataArray;
    	}
	    catch (SQLException e){
	    	System.out.println("SQL code does not execute." + e.getMessage());
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}   	
    	return null;
    }
    void fetchVideoAuthorProfileDataFromDB(String category){
    	this.videoAuthorProfileDataList = new ArrayList<VideoAuthorProfile>();
    	try{    		
    		Connection conn = dataManager.getDBConnection();
    		System.out.println("DB Connection Secured.\n");    		
			Statement VideoAuthorProfileQuery = conn.createStatement();
			ResultSet VideoAuthorProfileResultSet = VideoAuthorProfileQuery.executeQuery(
			"SELECT Author, COUNT(*) AS VidCount, " +
			"Date_format(MIN(CreatedOn), '%D %M %Y') AS ActiveSince, "+
			"Date_format(MAX(CreatedOn), '%D %M %Y') AS ActiveUntil, "+
			"ROUND(DATEDIFF(MAX(CreatedOn), MIN(CreatedOn))/365,2) AS LifeTimeInYears, "+
			"ROUND(AVG(ViewsCount)) AS AVG_ViewsCount "+ 
			"FROM utubevideos "+
			"WHERE Category LIKE '%"+ category + "%'"+
			"GROUP BY Author Order By VidCount DESC " +
			"LIMIT 100");			
			System.out.println("Data fetched.\n");			 
			
			while(VideoAuthorProfileResultSet.next()){
				if (VideoAuthorProfileResultSet.getInt("AVG_ViewsCount") > AVG_VIEWCOUNT_BOUND){
					VideoAuthorProfile vidAuthProfile = new VideoAuthorProfile();
					vidAuthProfile.setAuthorID(VideoAuthorProfileResultSet.getString("Author"));
					vidAuthProfile.setUploadedVideosCount(VideoAuthorProfileResultSet.getInt("VidCount"));
					vidAuthProfile.setActiveSince(VideoAuthorProfileResultSet.getString("ActiveSince"));
					vidAuthProfile.setActiveUntil(VideoAuthorProfileResultSet.getString("ActiveUntil"));
					vidAuthProfile.setLifeTime(VideoAuthorProfileResultSet.getFloat("LifeTimeInYears"));
					vidAuthProfile.setAvgViewsCount(VideoAuthorProfileResultSet.getInt("AVG_ViewsCount"));					
					videoAuthorProfileDataList.add(vidAuthProfile);
					Tooltip tooltip = new Tooltip(	" Author: " + vidAuthProfile.getAuthorID() +
													"\n Active Since " + vidAuthProfile.getActiveSince() +
													"\n Last Upload on " + vidAuthProfile.getActiveUntil() + 
													"\n Average Views/Video " + vidAuthProfile.getAvgViewsCount());
			        tooltip.setStyle("-fx-font: 14 arial;  -fx-font-smoothing-type: lcd;");
			        vidAuthorToolTipsList.add(tooltip);
		        }
		    }
			VideoAuthorProfileResultSet.close();
			VideoAuthorProfileResultSet = null;
			VideoAuthorProfileQuery = null;
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
    void  fetchVideoAuthorProfileData(String category, int minViewsCount){ 
    	this.videoAuthorProfileDataList = new ArrayList<VideoAuthorProfile>();
    	try{
    		String dataFilePath = "";
    		dataFilePath = ContentAuthorProfile.class.getResource("Data/VideoAuthorsProfileData.csv").getPath();
    		InputStreamReader fileReader = new InputStreamReader(new FileInputStream(dataFilePath), "UTF-8");
    	    BufferedReader lineReader = new BufferedReader(fileReader);    	    
    	    String line = lineReader.readLine();    	    
    	    line = lineReader.readLine();    // Skip the headers line    	    
    	    while (line != null)
    	    {   	    	
    	    	VideoAuthorProfile vidAuthProfile = new VideoAuthorProfile();   	    	
    	    	
    	    	String s = line.substring(0, line.indexOf(',')); 	// AuthorID    	    	
    	    	vidAuthProfile.setAuthorID(s);
    	    	line = line.substring(line.indexOf(',')+1);
    	    	
    	    	s = line.substring(0, line.indexOf(',')); 			// VidCount
    	    	vidAuthProfile.setUploadedVideosCount(Integer.parseInt(s));
    	    	line = line.substring(line.indexOf(',')+1);
    	    	
    	    	s = line.substring(0, line.indexOf(',')); 			// ActiveSince
    	    	vidAuthProfile.setActiveSince(s);
    	    	line = line.substring(line.indexOf(',')+1);
    	    	
    	    	s = line.substring(0, line.indexOf(',')); 			// ActiveUntil
    	    	vidAuthProfile.setActiveUntil(s);
    	    	line = line.substring(line.indexOf(',')+1);
    	    	
    	    	s = line.substring(0, line.indexOf(',')); 			// LifeTime
    	    	vidAuthProfile.setLifeTime(Float.parseFloat(s));
    	    	line = line.substring(line.indexOf(',')+1);
    	    	
    	    	s = line.substring(0);					 			// AvgViewsCount
    	    	vidAuthProfile.setAvgViewsCount(Integer.parseInt(s));
    	    	
    	    	videoAuthorProfileDataList.add(vidAuthProfile);
    	    	
    	    	Tooltip tooltip = new Tooltip(" Author: " + vidAuthProfile.getAuthorID() +
						"\n Active Since " + vidAuthProfile.getActiveSince() +
						"\n Last Upload on " + vidAuthProfile.getActiveUntil() + 
						"\n Average Views/Video " + vidAuthProfile.getAvgViewsCount());
    	    	tooltip.setStyle("-fx-font: 14 arial;  -fx-font-smoothing-type: lcd;");
	            vidAuthorToolTipsList.add(tooltip);	 
    	    	
    	    	line = lineReader.readLine();    	    	    	    	
    	    }
    	    fileReader.close();
    	    lineReader.close();    	    
    	}catch(Exception e){
    		e.printStackTrace();
    	}   	
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
    JRadioButton radioAllComment = new JRadioButton("All Comments");
    JRadioButton radioNoSelfComment = new JRadioButton("No Self Comments");
    ButtonGroup radioGroup = new ButtonGroup();
    
    JButton btnFireGraph = new JButton("Fire !");
    
    final JFXPanel commentAuthorProfileFxPanel = new JFXPanel();
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
    	JPanel authorsProfilePanel = new JPanel();
        
    	categoriesTabbedPane.add("All Categories", authorsProfilePanel);    	
	 	ButtonTabComponent btnTabComp = new ButtonTabComponent(categoriesTabbedPane);
	 	categoriesTabbedPane.setTabComponentAt(categoriesTabbedPane.getTabCount()-1, btnTabComp);
    	
	 	
	 	JPanel authorsProfilePieChartpanel = new JPanel();
    	categoriesTabbedPane.add("All Categories Pie", authorsProfilePieChartpanel);    	
	 	ButtonTabComponent btnTabCompPie = new ButtonTabComponent(categoriesTabbedPane);
	 	categoriesTabbedPane.setTabComponentAt(categoriesTabbedPane.getTabCount()-1, btnTabCompPie);   	
	 	
    	final JFXPanel videoAuthorProfileFxPanel = new JFXPanel();
        final JFXPanel videoAuthorViewsProfilePanel = new JFXPanel();      
        authorsProfilePanel.setLayout(new GridLayout(2,1));
        authorsProfilePanel.add(videoAuthorProfileFxPanel);
        authorsProfilePanel.add(videoAuthorViewsProfilePanel);       
        
        final JFXPanel videoAuthorsPieChartFXPanel = new JFXPanel();
        authorsProfilePieChartpanel.add(videoAuthorsPieChartFXPanel);
        
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
				AVG_VIEWCOUNT_BOUND = Integer.parseInt(videoViewsLimitCombo.getSelectedItem().toString());
				final JFXPanel videoAuthorProfileFxPanelNew = new JFXPanel();
		        final JFXPanel videoAuthorViewsProfilePanelNew = new JFXPanel();
		        
		        JPanel authorsProfilePanel = new JPanel();
		        authorsProfilePanel.setLayout(new GridLayout(2,1));
		        authorsProfilePanel.add(videoAuthorProfileFxPanelNew);	        
		        authorsProfilePanel.add(videoAuthorViewsProfilePanelNew);		        
		        categoriesTabbedPane.add(category, authorsProfilePanel);			 	
			 	ButtonTabComponent btnTabComp1 = new ButtonTabComponent(categoriesTabbedPane);
			 	categoriesTabbedPane.setTabComponentAt(categoriesTabbedPane.getTabCount()-1, btnTabComp1);
			 	
			 	
			 	final JFXPanel videoAuthorsPieChartPanelNew = new JFXPanel();
			 	JPanel authorsProfilePieChartpanelNew = new JPanel();
			 	authorsProfilePieChartpanelNew.add(videoAuthorsPieChartPanelNew);        
		        categoriesTabbedPane.add(category, authorsProfilePieChartpanelNew);			 	
			 	ButtonTabComponent btnTabComp2 = new ButtonTabComponent(categoriesTabbedPane);
			 	categoriesTabbedPane.setTabComponentAt(categoriesTabbedPane.getTabCount()-1, btnTabComp2);
			 	
				Platform.runLater(new Runnable() {
		            @Override
		            public void run() {           	
		            	fetchVideoAuthorProfileDataFromDB(category);
		            	//commentAuthorsProfileLineChart(commentAuthorProfileFxPanel);
		            	videoAuthorsProfileLineChart(videoAuthorProfileFxPanelNew);
		            	videoAuthorsViewsProfileLineChart(videoAuthorViewsProfilePanelNew);
		            	authorsProfilePieChart(videoAuthorsPieChartPanelNew);
		            	Iterator<VideoAuthorProfile> iter = videoAuthorProfileDataList.iterator();
		            	while(iter.hasNext())
		            		iter.next().nullify();
		            	iter = null;
		            	videoAuthorProfileDataList.clear();
		            	videoAuthorProfileDataList = null;
		            	System.gc();
		            }
		       });				
			}			
		});
        localControlsPanel.add(label);
        localControlsPanel.add(videoCategoriesCombo);
        JLabel label2 = new JLabel();
        label2.setText(" Select views count limit ");
        localControlsPanel.add(label2);        
        String viewsCountLimitArray[] = {"1000","10000", "100000", "1000000", "10000000"};; 
        videoViewsLimitCombo = new JComboBox(viewsCountLimitArray);
        localControlsPanel.add(videoViewsLimitCombo);
        localControlsPanel.add(btnLaunchUploadHistoryGraph);
        panel.add(localControlsPanel, BorderLayout.NORTH);
    	panel.add(categoriesTabbedPane, BorderLayout.CENTER);       
        panel.setVisible(true);   
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
            	fetchVideoAuthorProfileDataFromDB("");
            	//commentAuthorsProfileLineChart(commentAuthorProfileFxPanel);
            	videoAuthorsProfileLineChart(videoAuthorProfileFxPanel);
            	videoAuthorsViewsProfileLineChart(videoAuthorViewsProfilePanel);
            	authorsProfilePieChart(videoAuthorsPieChartFXPanel);            	
            	
            	Iterator<VideoAuthorProfile> iter = videoAuthorProfileDataList.iterator();
            	while(iter.hasNext())
            		iter.next().nullify();
            	iter = null;
            	videoAuthorProfileDataList.clear();
            	videoAuthorProfileDataList = null;
            	System.gc();
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
    public void fireCommentAuthorGraph() {
    	Platform.runLater(new Runnable() {
            @Override
            public void run() {
            	commentAuthorsProfileLineChart(commentAuthorProfileFxPanel);
            }
        });
    }
	@Override
	public void actionPerformed(ActionEvent event) {
		if (event.getSource() == btnFireGraph){
			fireCommentAuthorGraph();
		}
		
	}
}

class VideoAuthorProfile{
	private String authorID; 
	public String getAuthorID(){return authorID;} 
	public void setAuthorID(String id){authorID = id;} 
	private String activeSince; 
	public String getActiveSince(){return activeSince;} 
	public void setActiveSince(String date){activeSince = date;}
	private String activeUntil; 
	public String getActiveUntil(){return activeUntil;} 
	public void setActiveUntil(String date){activeUntil = date;}
	private Float lifeTime; 
	public Float getLifeTime(){return lifeTime;} 
	public void setLifeTime(float lifetime){this.lifeTime = lifetime;}
	private Integer uploadedVideosCount; 
	public Integer getUploadedVideosCount(){return uploadedVideosCount;} 
	public void setUploadedVideosCount(int vidCount){uploadedVideosCount = vidCount;}
	private Integer avgViewsCount; 
	public Integer getAvgViewsCount(){
		return avgViewsCount;
	} 
	public void setAvgViewsCount(Integer avgViewsCount){ 
		this.avgViewsCount = avgViewsCount;
	}
	public void nullify(){
		authorID = null;
		activeSince = null;
		activeUntil = null;
		lifeTime = null;
		uploadedVideosCount = null;
		avgViewsCount = null;				
	}
}