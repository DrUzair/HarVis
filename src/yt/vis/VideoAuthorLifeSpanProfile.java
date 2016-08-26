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
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;


import org.joda.time.DateTime;
import org.joda.time.Months;

import yt.har.uTubeDataManager;

import com.google.gdata.util.ParseException;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.JFXPanel;
import javafx.event.Event;
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
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

public class VideoAuthorLifeSpanProfile {
	public static JPanel panel = new JPanel();
	private DropShadow ds = new DropShadow();
	private ArrayList<AuthorLifeSpan> authorLifeSpanDataList;
	JTabbedPane categoriesTabbedPane;
    JComboBox<String> videoCategoriesCombo;
    JComboBox<String> videoViewsLimitCombo;
    JButton btnLaunchUploadHistoryGraph;
    int VIEWS_COUNT_LIMIT = 1000000;
	uTubeDataManager dataManager;
	final Label caption = new Label("");
	private static final String LABEL_FONT_STYLE = "-fx-font: 16 arial;";
	public VideoAuthorLifeSpanProfile(uTubeDataManager dataManager) {
		this.dataManager = dataManager;		
	}
	void  fetchAuthorLifeSpanDataFromDB(String category, int minViewsCount){
		this.authorLifeSpanDataList = new ArrayList<AuthorLifeSpan>();
    	try{    		
    		Connection conn = uTubeDataManager.getDBConnection();
    		System.out.println("DB Connection Secured.\n");    		
			Statement authorLifeSpanQuery = conn.createStatement();
			ResultSet authorLifeSpanQueryResultSet = authorLifeSpanQuery.executeQuery(
			"SELECT VT.Author, COUNT(VT.ID) AS VideosCount,  "+
			"SUM(VT.ViewsCount) AS TotalViews, "+
			"Date_format(min(VT.CreatedOn), '%d-%m-%Y') AS FirstContributionDate, "+ 
			"Date_format(max(VT.CreatedOn), '%d-%m-%Y') AS LastContributionDate "+ 
			"FROM utubevideos AS VT "+	
			"WHERE  (VT.Category LIKE '%"+ category +"%') " +
			"AND VT.ViewsCount > " + minViewsCount + " " + 
			"GROUP By VT.Author "+
			"ORDER BY VideosCount DESC");
			System.out.println("Data fetched.\n"); 			
			while (authorLifeSpanQueryResultSet.next()){
				AuthorLifeSpan authLifeSpan = new AuthorLifeSpan();
				authLifeSpan.setAuthor(authorLifeSpanQueryResultSet.getString("Author"));
				String strFirstContributionDate = authorLifeSpanQueryResultSet.getString("FirstContributionDate"); 	
        		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");        		
        		Date firstContributionDate = sdf.parse(strFirstContributionDate);        		
        		authLifeSpan.setFirstContributionDate(firstContributionDate);
				String strLastCommentDate = authorLifeSpanQueryResultSet.getString("LastContributionDate");        		        		
        		Date lastCommentDate = sdf.parse(strLastCommentDate);        		
        		authLifeSpan.setLastContributionDate(lastCommentDate);
				authLifeSpan.setViewsCount(authorLifeSpanQueryResultSet.getInt("TotalViews"));
        		authLifeSpan.setVideosCount(authorLifeSpanQueryResultSet.getInt("VideosCount"));
				authorLifeSpanDataList.add(authLifeSpan);
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

	void authorLifeSpanBubbleChart(JFXPanel authorLifeSpanFxPanel, int lifeSpanStart, int lifeSpanEnd){    	
    	XYChart.Series videoLife0Series = new XYChart.Series();    	      	
        ArrayList<Tooltip> toolTips0 = new ArrayList<Tooltip>();        
        Date oldestContributionDate = new Date();       
        
        int contCount = 0;
        int maxContCount = 0;
        for (AuthorLifeSpan authLifeSpan : authorLifeSpanDataList){
        	if (oldestContributionDate.compareTo(authLifeSpan.getFirstContributionDate()) > 0){
        		oldestContributionDate = authLifeSpan.getFirstContributionDate();
        	}
        	contCount = authLifeSpan.getVideosCount();
        	if (contCount > maxContCount) {
        		// Required to set Y-Axis Upper-Limit
        		maxContCount = contCount;        		
        	}
        }
        Date latestContributionDate = oldestContributionDate;
        contCount = 0;
        int minContCount = 10000000; // Arbitrarily Large Number       
        for (AuthorLifeSpan authLifeSpan : authorLifeSpanDataList){
        	if (latestContributionDate.compareTo(authLifeSpan.getLastContributionDate()) < 0){
        		latestContributionDate = authLifeSpan.getLastContributionDate();
        	}
        	contCount = authLifeSpan.getVideosCount();
        	if (contCount < minContCount) {
        		// Required to set Y-Axis Upper-Limit
        		minContCount = contCount;        		
        	}            
        }
        for (AuthorLifeSpan authLifeSpan : authorLifeSpanDataList){
        	float normalizedContributionCount = 0;
        	normalizedContributionCount = ((authLifeSpan.getVideosCount()-minContCount)*1.0F)/((maxContCount-minContCount)*1.0F);
        	authLifeSpan.setNormalizedVideosCount(normalizedContributionCount*10.0F);	// Normalized 0~10
        	normalizedContributionCount = authLifeSpan.getNormalizedVideosCount();
        	System.out.println(normalizedContributionCount);
        }
        // DateTime of OldestVideo Uploaded on this Topic
    	Calendar calendar = new GregorianCalendar(); 
    	calendar.setTime(latestContributionDate);	
    	DateTime latestContrinutionDateTime = new DateTime().withDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DAY_OF_MONTH));
    	// DateTime of LatestVideo Uploaded on this topic    	
    	calendar.setTime(oldestContributionDate);	
    	DateTime oldestContributionDateTime = new DateTime().withDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DAY_OF_MONTH));
    	// Time Difference (in months) between latest, oldest 
        Months mt = Months.monthsBetween(oldestContributionDateTime, latestContrinutionDateTime);         
        int numTotalMonths = mt.getMonths();       
    	
        caption.setTextFill(Color.DARKORANGE);
        caption.setStyle(LABEL_FONT_STYLE);        
        int k = 0;
        for (AuthorLifeSpan authLifeSpan : authorLifeSpanDataList){        	
        	// DateTime of this Video
        	calendar.setTime(authLifeSpan.getFirstContributionDate());						// CreatedOn
        	DateTime firstContributionDateTime = new DateTime().withDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1,calendar.get(Calendar.DAY_OF_MONTH));
        	// Time Difference (in months) between this and oldest video
        	Months monthsBetween = Months.monthsBetween(oldestContributionDateTime, firstContributionDateTime);         
            int uploadMonthNumber = monthsBetween.getMonths();            
        	// GET THE CommentCount for Y-Axis        	
        							// VideosCount
        	System.out.println(authLifeSpan.getNormalizedVideosCount());
        	float authLifeSpanInMonths = authLifeSpan.getLifeSpanInMonths();
        	float authLifeSpanPercentage = (authLifeSpanInMonths/(numTotalMonths*1.0F))*10;
        	if (authLifeSpanPercentage != 0.0F){		            		
        		if (authLifeSpanPercentage > 10){
        			System.out.println(contCount);        			
        		}              		
        		if (authLifeSpanInMonths < lifeSpanEnd && authLifeSpanInMonths > lifeSpanStart ){
        			final XYChart.Data data0 = new XYChart.Data(uploadMonthNumber, authLifeSpanInMonths, authLifeSpan.getNormalizedVideosCount());// , authLifeSpanPercentage);
        			videoLife0Series.getData().add(data0);        			
        			
        			Format formatter = new SimpleDateFormat("yyyy-MM-dd");
        			calendar = Calendar.getInstance();
        			calendar.setTime(authLifeSpan.getFirstContributionDate());
        			String strFirstContributionDate = formatter.format(calendar.getTime());        			
        			calendar.setTime(authLifeSpan.getLastContributionDate());
        			String strLastContributionDate = formatter.format(calendar.getTime());      			
        			authLifeSpan.setLifeSpanPercentage(authLifeSpanPercentage);
        			authLifeSpan.setUploadMonthNumber(uploadMonthNumber);
			        Tooltip tooltip = new Tooltip(        					
        					" First video uploaded on " + strFirstContributionDate.toString() + 
        					"\n Last video uploaded on " + strLastContributionDate.toString() +
        					"\n Total videos count " + authLifeSpan.getVideosCount() +
        					"\n Relative videos count " + authLifeSpan.getNormalizedVideosCount() +
        					"\n Lifetime (Months) " + authLifeSpanInMonths + " (percentage "+ authLifeSpanPercentage +")"+
        					"\n Views " + authLifeSpan.getViewsCount() +        					 
        					"\n Author ID " + authLifeSpan.getAuthor());
        			tooltip.setStyle("-fx-font: 14 arial;  -fx-font-smoothing-type: lcd;");
        			toolTips0.add(tooltip);
        		}      			
        	}
        }
//        maxContCount = (maxContCount)+5;
//    	int imaxContCount = (int)maxContCount;
//    	int tickMarkSize = 10;
//    	if (imaxContCount > 100){
//    		int n = (int)(imaxContCount/tickMarkSize);
//    		tickMarkSize = (int)(imaxContCount/n);
//    	}else if (imaxContCount < 100){  		
//    		tickMarkSize = 5;
//    		int n = (int)(imaxContCount/tickMarkSize);
//    		tickMarkSize = (int)(imaxContCount/n);
//    	}
//        
//    	final NumberAxis yAxis = new NumberAxis(0, (int)maxContCount+1, tickMarkSize); //(minX, maxX, Tick)
//        final NumberAxis xAxis = new NumberAxis(0, numTotalMonths, 5);//(minY, maxY, Tick)

        maxContCount = (maxContCount)+5;
    	int imaxContCount = (int)maxContCount;
    	int tickMarkSize = 10;
    	if (imaxContCount > 100){
    		int n = (int)(imaxContCount/tickMarkSize);
    		tickMarkSize = (int)(imaxContCount/n);
    	}else if (imaxContCount < 100){  		
    		tickMarkSize = 5;
    		int n = (int)(imaxContCount/tickMarkSize);
    		tickMarkSize = (int)(imaxContCount/n);
    	}
        
    	final NumberAxis yAxis = new NumberAxis(0, numTotalMonths, 5); //(minX, maxX, Tick)
        final NumberAxis xAxis = new NumberAxis(0, numTotalMonths, 5);//(minY, maxY, Tick)

        
        ArrayList<String> categories = new ArrayList<String>();
        
        Format formatter = new SimpleDateFormat("MMMM yy");		
        final CategoryAxis timeAxis = new CategoryAxis();
        final BubbleChart<Number, Number> bblc = new BubbleChart<Number, Number>(xAxis,yAxis);      
        
        //formatter = new SimpleDateFormat("yyyy m");
		calendar = Calendar.getInstance();
		calendar.setTime(oldestContributionDate);
		String strOldestContributionDate = formatter.format(calendar.getTime());        			
		calendar.setTime(latestContributionDate);
		String strLatestContributionDate = formatter.format(calendar.getTime());      			
		        
        xAxis.setLabel("Upload month No. (Since "+ strOldestContributionDate +" Until " + strLatestContributionDate +")");           
        yAxis.setLabel("Author Activity Lifespan (in months)" );        
        bblc.setTitle(lifeSpanStart + "~" + lifeSpanEnd + " months lifespan Authors");
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
        authorLifeSpanFxPanel.setScene(scene);    
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
        
        node.setOnMouseClicked(new EventHandler<MouseEvent>() {        	
        	@Override        	
            public void handle(MouseEvent me){
        		if (me.getButton() == MouseButton.SECONDARY) { 
        			final ContextMenu contextMenu = new ContextMenu();
        			MenuItem loadDataMenuItem = new MenuItem("Load Data Table");
        			contextMenu.getItems().add(loadDataMenuItem);        			
        			contextMenu.show(node, me.getScreenX(), me.getScreenY());
        			contextMenu.autoHideProperty().setValue(true);
        			loadDataMenuItem.setOnAction(new EventHandler() {
        	        	@Override
						public void handle(Event e) {
        	        		Integer uploadMonthNumber = Integer.parseInt(dataNode.getXValue().toString());        	        		
        	        		Float lifeSpanPercentage = Float.parseFloat(dataNode.getExtraValue().toString());
        	        		fetchAuthorData(uploadMonthNumber, lifeSpanPercentage);
						}
        	        });  
        			
        		}
        	}
        });
	}
	
	void fetchAuthorData(int uploadMonthNumber, float lifeSpanPercentage){
		 for (AuthorLifeSpan authLifeSpan : authorLifeSpanDataList){   
			 if (uploadMonthNumber == authLifeSpan.getUploadMonthNumber() &&
					lifeSpanPercentage == authLifeSpan.getNormalizedVideosCount()){
				 	System.out.println(authLifeSpan.getAuthor() + " " + uploadMonthNumber + " " + lifeSpanPercentage);
				 	JPanel dataPanel = new JPanel();				 	
				 	
				 	// categoriesTabbedPane.add(authLifeSpan.getAuthor() + "'s Data", dataPanel);
				 	String title = authLifeSpan.getAuthor() + "'s Data";
				 	categoriesTabbedPane.add(title, dataPanel); 
				 	int tabIndex = categoriesTabbedPane.getTabCount();
				 	ButtonTabComponent btnTabComp = new ButtonTabComponent(categoriesTabbedPane);
				 	categoriesTabbedPane.setTabComponentAt(tabIndex-1, btnTabComp); 
				 	Calendar calendar;
				 	try{    		
			    		Connection conn = uTubeDataManager.getDBConnection();
			    		Statement authVideosQuery = conn.createStatement();
						ResultSet authVideosQueryResultSet = authVideosQuery.executeQuery(
						"SELECT Title, ViewsCount, ID, CreatedOn, Category FROM utubevideos "+
						"WHERE Author = '"+authLifeSpan.getAuthor()+"'"); 
						
						int rowCount = 0;						
						while(authVideosQueryResultSet.next()){
							rowCount ++;				
					    }						
						int colCount = 5;	
						String[] colNames = {"Video Title",
                                "ViewsCount",
                                "ID",
                                "Uploaded On",
                                "Category"};
						Object data[][] = new Object[rowCount][colCount];
						rowCount = 0;
						authVideosQueryResultSet.beforeFirst();
						while(authVideosQueryResultSet.next()){
							String videoTitle = authVideosQueryResultSet.getString("Title");				
							data[rowCount][0] = videoTitle;		
							String viewsCount = authVideosQueryResultSet.getString("ViewsCount");				
							data[rowCount][1] = viewsCount;		
							String ID = authVideosQueryResultSet.getString("ID");				
							data[rowCount][2] = ID;	
							String createdOn = authVideosQueryResultSet.getString("CreatedOn");	
							
							SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");        		
			        		Date createdOnDate = sdf.parse(createdOn);			        	
			        		//Format formatter = new SimpleDateFormat("yyyy-MM-dd");		        		
			        		calendar = Calendar.getInstance();
		        			calendar.setTime(createdOnDate);
		        			createdOn = sdf.format(calendar.getTime());
			        		
		        			data[rowCount][3] = createdOn;
							
							String category = authVideosQueryResultSet.getString("Category");				
							data[rowCount][4] = category;
							rowCount++;
						}		
						
						
						JTable table = new JTable(data, colNames);	
						table.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );
						table.getColumnModel().getColumn(0).setPreferredWidth(300);
						table.getColumnModel().getColumn(1).setPreferredWidth(90);
						table.getColumnModel().getColumn(2).setPreferredWidth(100);
						table.getColumnModel().getColumn(3).setPreferredWidth(100);
						table.getColumnModel().getColumn(4).setPreferredWidth(100);
						JScrollPane scrollPane = new JScrollPane(table);
						scrollPane.setSize(500, 300);
					    
						dataPanel.setLayout(new BorderLayout());
						dataPanel.add(scrollPane, BorderLayout.CENTER);
						
						JLabel label = new JLabel();
						label.setText("Author " + authLifeSpan.getAuthor() + "'s Videos");
						dataPanel.add(label, BorderLayout.NORTH);
						data = null;
						System.gc();
						conn.close();
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
		 }
	}
	void populateCategoriesCombo(){
    	try{    		
    		Connection conn = uTubeDataManager.getDBConnection();
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
    	
    	//categoriesTabbedPane.add("All Categories", videoLifeSpanPanel);
    	
    	categoriesTabbedPane.add("All Categories", videoLifeSpanPanel); 
	 	int tabIndex = categoriesTabbedPane.getTabCount();
	 	ButtonTabComponent btnTabComp = new ButtonTabComponent(categoriesTabbedPane);
	 	categoriesTabbedPane.setTabComponentAt(tabIndex-1, btnTabComp); 
    	
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
			 	int tabIndex = categoriesTabbedPane.getTabCount();
			 	ButtonTabComponent btnTabComp = new ButtonTabComponent(categoriesTabbedPane);
			 	categoriesTabbedPane.setTabComponentAt(tabIndex-1, btnTabComp); 
				
				Platform.runLater(new Runnable() {
		            @Override
		            public void run() {           	
		            	fetchAuthorLifeSpanDataFromDB(category, videoViewsCountLimit);
		            	authorLifeSpanBubbleChart(videoLifeSpanFxPanel1New, 0, 12);
		            	authorLifeSpanBubbleChart(videoLifeSpanFxPanel2New, 12, 24);
		            	authorLifeSpanBubbleChart(videoLifeSpanFxPanel3New, 24, 48);
		            	authorLifeSpanBubbleChart(videoLifeSpanFxPanel4New, 48, 56);              
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
            	fetchAuthorLifeSpanDataFromDB("", 1000);
            	authorLifeSpanBubbleChart(videoLifeSpanFxPanel1, 0, 12);
            	authorLifeSpanBubbleChart(videoLifeSpanFxPanel2, 12, 24);
            	authorLifeSpanBubbleChart(videoLifeSpanFxPanel3, 24, 48);
            	authorLifeSpanBubbleChart(videoLifeSpanFxPanel4, 48, 56);
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
class AuthorLifeSpan{
	public float getNormalizedVideosCount() {
		return normalizedVideosCount;
	}
	public void setNormalizedVideosCount(float normalizedVideosCount) {
		this.normalizedVideosCount = normalizedVideosCount;
	}
	public int getUploadMonthNumber() {
		return uploadMonthNumber;
	}
	public void setUploadMonthNumber(int uploadMonthNumber) {
		this.uploadMonthNumber = uploadMonthNumber;
	}
	public float getLifeSpanPercentage() {
		return lifeSpanPercentage;
	}
	public void setLifeSpanPercentage(float lifeSpanPercentage) {
		this.lifeSpanPercentage = lifeSpanPercentage;
	}
	public Date getFirstContributionDate() {
		return firstContributionDate;
	}
	public void setFirstContributionDate(Date firstContributionDate) {
		this.firstContributionDate = firstContributionDate;
	}
	public Date getLastContributionDate() {
		return lastContributionDate;
	}
	public void setLastContributionDate(Date lastContributionDate) {
		this.lastContributionDate = lastContributionDate;
	}
	private int uploadMonthNumber; 		//Relative to other Authors in a Collection
	private float lifeSpanPercentage;	//Relative to other Authors in a Collection
	private Date firstContributionDate;
	private Date lastContributionDate; 
	private int videosCount;
	private float normalizedVideosCount;
	public int getVideosCount() {
		return videosCount;
	}
	public void setVideosCount(int videosCount) {
		this.videosCount = videosCount;
	}
	public int getLifeSpanInDays(){
		long diffTime = firstContributionDate.getTime() - lastContributionDate.getTime();
		int diffDays = (int)(diffTime / (1000 * 60 * 60 * 24));		
		return diffDays;
	}
	public int getLifeSpanInMonths(){
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(firstContributionDate);
		DateTime uploadDateTime = new DateTime().withDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DAY_OF_MONTH));
		calendar.setTime(lastContributionDate);
		DateTime lastCommentDateTime = new DateTime().withDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DAY_OF_MONTH));
		Months lifeInMonths = Months.monthsBetween(uploadDateTime, lastCommentDateTime);
		int n = lifeInMonths.getMonths();
		return n;
	}
	
	private int viewsCount; public void setViewsCount(int viewsCount){this.viewsCount = viewsCount;} public int getViewsCount(){return viewsCount;}
	private String author; public void setAuthor(String author){this.author = author;} public String getAuthor(){return author;}		
}

