package yt.vis;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import yt.har.uTubeDataManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.JFXPanel;
import javafx.event.EventHandler;
import javafx.geometry.Side;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;

public class VideoCategoriesGraph {  
    public JPanel panel;
    ArrayList<Tooltip> toolTips;
    
    
    ArrayList<VideoCategory> videoCatagoriesDataList;
    private DropShadow ds = new DropShadow();
    uTubeDataManager dataManager;
    public VideoCategoriesGraph(uTubeDataManager dataManager){
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
    void videosCountAndCategoriesPieChart(JFXPanel videoCategoriesFxPanel){    	
        ObservableList<PieChart.Data> pieChartData =
                FXCollections.observableArrayList();
        Iterator<VideoCategory> videoCategoryIterator = videoCatagoriesDataList.iterator();
        int dataIndex = 0;
        while (videoCategoryIterator.hasNext()){
        	VideoCategory videoCategory = videoCategoryIterator.next();
        	pieChartData.add(dataIndex++ , new PieChart.Data(videoCategory.getCategory(), videoCategory.getVideosCount()));
        }
        PieChart pieChart = new PieChart(pieChartData);
        pieChart.setTitle("Videos & Categories");
        
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
        
        videoCategoriesFxPanel.setLayout(new BorderLayout());
        videoCategoriesFxPanel.setScene(scene);                          
    }
    
    void authorsCountAndCategoriesPieChart(JFXPanel videoCategoriesFxPanel){    	
        ObservableList<PieChart.Data> pieChartData =
                FXCollections.observableArrayList();
        Iterator<VideoCategory> videoCategoryIterator = videoCatagoriesDataList.iterator();
        int dataIndex = 0;
        while (videoCategoryIterator.hasNext()){
        	VideoCategory videoCategory = videoCategoryIterator.next();
        	pieChartData.add(dataIndex++ , new PieChart.Data(videoCategory.getCategory(), videoCategory.getAuthorsCount()));
        }    
        
        
        PieChart pieChart = new PieChart(pieChartData);        
        pieChart.setTitle("Authors & Categories");
        
        final Label caption = new Label("");
        caption.setTextFill(Color.DARKORANGE);
        caption.setStyle("-fx-font: 24 arial;");

        for (final PieChart.Data data : pieChart.getData()) {
            data.getNode().addEventHandler(MouseEvent.MOUSE_CLICKED,
                new EventHandler<MouseEvent>() {
                    @Override public void handle(MouseEvent e) {
                        caption.setTranslateX(e.getSceneX());
                        caption.setTranslateY(e.getSceneY());
                        caption.setText(String.valueOf(data.getPieValue()) + " authors");
                     }
                });
        }      
        
        pieChart.setPrefSize(1100,700);
        Scene scene  = new Scene(new Group());
        ((Group) scene.getRoot()).getChildren().add(pieChart);
        ((Group) scene.getRoot()).getChildren().add(caption);
        
        videoCategoriesFxPanel.setScene(scene);      
    }
    void viewsCountAndCategoriesPieChart(JFXPanel videoCategoriesFxPanel){    	
        ObservableList<PieChart.Data> pieChartData =
                FXCollections.observableArrayList();
        Iterator<VideoCategory> videoCategoryIterator = videoCatagoriesDataList.iterator();
        int dataIndex = 0;
        while (videoCategoryIterator.hasNext()){
        	VideoCategory videoCategory = videoCategoryIterator.next();
        	pieChartData.add(dataIndex++ , new PieChart.Data(videoCategory.getCategory(), videoCategory.getViewsCount()));
        }    
        
        
        PieChart pieChart = new PieChart(pieChartData);        
        pieChart.setTitle("Views & Categories");
        
        final Label caption = new Label("");
        caption.setTextFill(Color.DARKORANGE);
        caption.setStyle("-fx-font: 24 arial;");

        for (final PieChart.Data data : pieChart.getData()) {
            data.getNode().addEventHandler(MouseEvent.MOUSE_CLICKED,
                new EventHandler<MouseEvent>() {
                    @Override public void handle(MouseEvent e) {
                        caption.setTranslateX(e.getSceneX());
                        caption.setTranslateY(e.getSceneY());
                        caption.setText(String.valueOf(data.getPieValue()) + " views");
                     }
                });
        }      
        
        pieChart.setPrefSize(1100,700);
        Scene scene  = new Scene(new Group());
        ((Group) scene.getRoot()).getChildren().add(pieChart);
        ((Group) scene.getRoot()).getChildren().add(caption);
        
        videoCategoriesFxPanel.setScene(scene);      
    }
    void commentsCountAndCategoriesPieChart(JFXPanel videoCategoriesFxPanel){    	
        ObservableList<PieChart.Data> pieChartData =
                FXCollections.observableArrayList();
        Iterator<VideoCategory> videoCategoryIterator = videoCatagoriesDataList.iterator();
        int dataIndex = 0;
        while (videoCategoryIterator.hasNext()){
        	VideoCategory videoCategory = videoCategoryIterator.next();
        	pieChartData.add(dataIndex++ , new PieChart.Data(videoCategory.getCategory(), videoCategory.getCommentsCount()));
        }    
        
        
        PieChart pieChart = new PieChart(pieChartData);        
        pieChart.setTitle("Comments & Categories");
        
        final Label caption = new Label("");
        caption.setTextFill(Color.DARKORANGE);
        caption.setStyle("-fx-font: 24 arial;");

        for (final PieChart.Data data : pieChart.getData()) {
            data.getNode().addEventHandler(MouseEvent.MOUSE_CLICKED,
                new EventHandler<MouseEvent>() {
                    @Override public void handle(MouseEvent e) {
                        caption.setTranslateX(e.getSceneX());
                        caption.setTranslateY(e.getSceneY());
                        caption.setText(String.valueOf(data.getPieValue()) + " Comments");
                     }
                });
        }      
        
        pieChart.setPrefSize(1100,700);
        Scene scene  = new Scene(new Group());
        ((Group) scene.getRoot()).getChildren().add(pieChart);
        ((Group) scene.getRoot()).getChildren().add(caption);
        
        videoCategoriesFxPanel.setScene(scene);      
    }
    void fetchVideoCategoriesData(){
    	try {
			Connection conn = dataManager.getDBConnection();
			Statement videoCategoriesQuery = conn.createStatement();
			ResultSet videoCategoriesResultSet = videoCategoriesQuery.executeQuery(
					"SELECT Category,COUNT(*) VideoCount, Count(DISTINCT(Author)) AuthorsCount, SUM(CommentsCount) AS CommentsCount, SUM(ViewsCount) AS ViewsCount " +
					"FROM utubevideos "+ 
					"GROUP BY Category "+
					"ORDER BY VideoCount DESC");			
			videoCatagoriesDataList = new ArrayList<VideoCategory>();	
			while(videoCategoriesResultSet.next()){
				VideoCategory videoCategory = new VideoCategory();
				videoCategory.setVideosCount(videoCategoriesResultSet.getInt("VideoCount"));
				videoCategory.setAuthorsCount(videoCategoriesResultSet.getInt("AuthorsCount"));
				videoCategory.setCommentsCount(videoCategoriesResultSet.getInt("CommentsCount"));
				videoCategory.setViewsCount(videoCategoriesResultSet.getLong("ViewsCount"));
				videoCategory.setCategory(videoCategoriesResultSet.getString("Category"));
				videoCatagoriesDataList.add(videoCategory);
			}
			conn.close();
			System.out.println("DB Connection Closed.\n");
		}	
		catch(Exception e){
			e.printStackTrace();
		}
    }
    
    
    public void initAndShowGUI() {
        // This method is invoked on the EDT thread
    	JTabbedPane categoriesTabbedPane = new JTabbedPane();    	
    	final JFXPanel videosCountAndCategoriesFxPanel = new JFXPanel();
    	categoriesTabbedPane.add("Videos and Categories", videosCountAndCategoriesFxPanel);
    	final JFXPanel authorsCountAndCategoriesFxPanel = new JFXPanel();
    	int tabIndex = categoriesTabbedPane.getTabCount();
	 	ButtonTabComponent btnTabComp1 = new ButtonTabComponent(categoriesTabbedPane);
	 	categoriesTabbedPane.setTabComponentAt(tabIndex-1, btnTabComp1);
    	categoriesTabbedPane.add("Authors and Categories", authorsCountAndCategoriesFxPanel);
    	final JFXPanel viewsCountAndCategoriesFxPanel = new JFXPanel();
    	tabIndex = categoriesTabbedPane.getTabCount();
	 	ButtonTabComponent btnTabComp2 = new ButtonTabComponent(categoriesTabbedPane);
	 	categoriesTabbedPane.setTabComponentAt(tabIndex-1, btnTabComp2);
    	categoriesTabbedPane.add("Views and Categories", viewsCountAndCategoriesFxPanel);
    	tabIndex = categoriesTabbedPane.getTabCount();
	 	ButtonTabComponent btnTabComp3 = new ButtonTabComponent(categoriesTabbedPane);
	 	categoriesTabbedPane.setTabComponentAt(tabIndex-1, btnTabComp3);
    	final JFXPanel commentsCountAndCategoriesFxPanel = new JFXPanel();
    	categoriesTabbedPane.add("Comments and Categories", commentsCountAndCategoriesFxPanel);
    	tabIndex = categoriesTabbedPane.getTabCount();
	 	ButtonTabComponent btnTabComp4 = new ButtonTabComponent(categoriesTabbedPane);
	 	categoriesTabbedPane.setTabComponentAt(tabIndex-1, btnTabComp4);
    	
	 	panel.setLayout(new BorderLayout());
    	panel.add(categoriesTabbedPane, BorderLayout.CENTER);       
        panel.setVisible(true);   
        
        Platform.runLater(new Runnable() {
            @Override
            public void run() {           
            	fetchVideoCategoriesData();
            	videosCountAndCategoriesPieChart(videosCountAndCategoriesFxPanel);
            	authorsCountAndCategoriesPieChart(authorsCountAndCategoriesFxPanel);
            	viewsCountAndCategoriesPieChart(viewsCountAndCategoriesFxPanel);
            	commentsCountAndCategoriesPieChart(commentsCountAndCategoriesFxPanel);
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

class VideoCategory {
	public long getViewsCount() {
		return viewsCount;
	}
	public void setViewsCount(long viewsCount) {
		this.viewsCount = viewsCount;
	}
	public int getCommentsCount() {
		return commentsCount;
	}
	public void setCommentsCount(int commentsCount) {
		this.commentsCount = commentsCount;
	}
	public int getAuthorsCount() {
		return authorsCount;
	}
	public void setAuthorsCount(int authorsCount) {
		this.authorsCount = authorsCount;
	}
	public int getVideosCount() {
		return videosCount;
	}
	public void setVideosCount(int videosCount) {
		this.videosCount = videosCount;
	}
	public String getCategory() {
		return categoryName;
	}
	public void setCategory(String category) {
		this.categoryName = category;
	}
	private long viewsCount;
	private int commentsCount;
	private int authorsCount;
	private int videosCount;
	private String categoryName; 
}
