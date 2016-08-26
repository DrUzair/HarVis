package yt.vis;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.functors.ConstantTransformer;
import org.apache.commons.collections15.functors.MapTransformer;

import yt.har.uTubeCommentAuthor;
import yt.har.uTubeDataManager;
import yt.har.uTubeVideoAuthor;
import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.SpringLayout;
import edu.uci.ics.jung.algorithms.layout.util.Relaxer;
import edu.uci.ics.jung.algorithms.scoring.VoltageScorer;
import edu.uci.ics.jung.algorithms.scoring.util.VertexScoreTransformer;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.ObservableGraph;
import edu.uci.ics.jung.graph.util.Graphs;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.NumberFormattingTransformer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.picking.PickedState;
import edu.uci.ics.jung.visualization.renderers.Renderer;

@SuppressWarnings("serial")
public class CommentVideoAuthorsNet extends JPanel implements ActionListener{

	public static final Font EDGE_FONT = new Font("SansSerif", Font.PLAIN, 10);
	public static final Font VERTEX_BOLD_FONT = new Font("SansSerif", Font.BOLD, 11);
	public static final Font VERTEX_PLAIN_FONT = new Font("SansSerif", Font.PLAIN, 11);
	/**/
	private VertexStrokeHighlight<String, Number> vertexStrokehilite;
	protected VertexDisplayPredicate<Integer,Number> vertexDisplayPredicate;
	private JCheckBox chkBoxHiliteOnStroke, v_small;
	private Graph<String,Number> graph = null;
	private AbstractLayout<String,Number> frLayout = null;
	private VisualizationViewer<String,Number> visViewer;
	private JButton btnSwitchLayout;	
	private Integer commentAuthorsCountLimit = 0;
	private Integer commentedVideosCountLimit = 0;
	private Transformer<String, Double> voltage_transformer; 
	private Transformer<Number,String> edge_transformer; 
	private Transformer<String, String> vertex_transformer;
	private Map<String,Number> transparencyMap = new HashMap<String,Number>();
	private static final int EDGE_LENGTH = 100;	
	private int MAX_COMMENTED_VIDEOS_COUNT = 0;
	private int MAX_COMMENTERS_COUNT = 0;
	private Timer timer;
	private Dimension screenDimension = Toolkit.getDefaultToolkit().getScreenSize();
	private boolean done;	
	private HashMap<uTubeVideoAuthor, ArrayList<uTubeCommentAuthor>> videoCommentAuthorsMap;
	private uTubeDataManager uTubeDM;
	private String srcTableName = "";
	boolean srcTableNamesLoaded = false;
	public CommentVideoAuthorsNet(uTubeDataManager dataManager){
		this.uTubeDM = dataManager; 
		this.videoCommentAuthorsMap = new HashMap<uTubeVideoAuthor, ArrayList<uTubeCommentAuthor>>();
		setLayout(new BorderLayout());				
		add(prepareVisParamComponents(), BorderLayout.NORTH);
		populateMDVA_Data(commentAuthorsCountLimit/*commentAuthCountLimit*/, commentedVideosCountLimit/*commentedVideosCount*/);
		prepareGraph();
		initFrLayout();
		timer = new Timer();
		startFrLayoutTimer();
	}
	
	private Box prepareVisParamComponents(){
		
		Box paramBox = Box.createHorizontalBox();
			

		JPanel visParamPanel = new JPanel();
		//visParamPanel.setLayout(new GridLayout(2,1));
		
		JSlider sliderMinCommentAuthorsCount = new JSlider();
		sliderMinCommentAuthorsCount.setBorder(BorderFactory.createTitledBorder("Minimum Comment Authors"));
		sliderMinCommentAuthorsCount.setMajorTickSpacing(20);
		sliderMinCommentAuthorsCount.setMinorTickSpacing(2);
		sliderMinCommentAuthorsCount.setPaintTicks(true);
		sliderMinCommentAuthorsCount.setPaintLabels(true);
		sliderMinCommentAuthorsCount.setValue(10);
		sliderMinCommentAuthorsCount.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent event) {
				JSlider slider = (JSlider)event.getSource();
				if (! slider.getValueIsAdjusting()) {
					commentAuthorsCountLimit = slider.getValue();
					prepareGraph();
					frLayout.setGraph(graph);
				}
			}
		});
		visParamPanel.add(sliderMinCommentAuthorsCount);
		
		JSlider sliderMinCommentedVideosCount = new JSlider();
		sliderMinCommentedVideosCount.setBorder(BorderFactory.createTitledBorder("Minimum Videos commented by Comment Authors"));
		sliderMinCommentedVideosCount.setMajorTickSpacing(20);
		sliderMinCommentedVideosCount.setMinorTickSpacing(5);
		sliderMinCommentedVideosCount.setPaintTicks(true);
		sliderMinCommentedVideosCount.setPaintLabels(true);
		sliderMinCommentedVideosCount.setValue(10);
		sliderMinCommentedVideosCount.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent event) {
				JSlider slider = (JSlider)event.getSource();
				if (! slider.getValueIsAdjusting()) {
					commentedVideosCountLimit = slider.getValue();
					prepareGraph();
					frLayout.setGraph(graph);
				}
			}
		});    
	    visParamPanel.add(sliderMinCommentedVideosCount);
	    
		paramBox.add(visParamPanel);		
		Vector<String> tableNamesVector = new Vector<String>();
		JComboBox<String> srcTableNamesCombo = new JComboBox<String>(tableNamesVector);
		srcTableNamesCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {				
				JComboBox<String> source = (JComboBox<String>)event.getSource();
				srcTableName = source.getSelectedItem().toString();				
			}
		});
		srcTableNamesCombo.addMouseListener(new MouseAdapter() {
			public void mouseEntered(MouseEvent event) {				
				if (srcTableNamesLoaded == false){
					Vector<String> tablesNamesVector = uTubeDM.getTableNames("%");
					@SuppressWarnings("unchecked")
					JComboBox<String> source = (JComboBox<String>)event.getSource();
					for(int i = 0 ; i < tablesNamesVector.size() ; i ++){
						source.addItem(tablesNamesVector.get(i));					
					}
					srcTableNamesLoaded = true;
				}
		    }
		});
		paramBox.add(srcTableNamesCombo);
		return paramBox;
	}
	public void populateMDVA_Data(int commentAuthCountLimit, int commentedVideosCountLimit){
		System.out.println("\t --> In Authors_NetGraph.populateMDVA_Data()");
		try{			
    		Connection conn = uTubeDataManager.getDBConnection();    		
    		Statement commentAuthorsCountQuery = conn.createStatement();
    		ResultSet commentAuthorsCountResultSet = 
    				commentAuthorsCountQuery.executeQuery(
    						"SELECT VideoAuthor, COUNT(CommentAuthor) AS CommentAuthorCount " +
    						"FROM commentvideoauthors_mdva "+
    						"GROUP BY VideoAuthor");
    		System.out.println("\t\t -- videoCommentAuthorsMap.clear()");
    		videoCommentAuthorsMap.clear();    		    		
    		while (commentAuthorsCountResultSet.next()){
    			Integer commentAuthorCount =  commentAuthorsCountResultSet.getInt("CommentAuthorCount");
    			String  strVideoAuthor =  commentAuthorsCountResultSet.getString("VideoAuthor");
    			if (MAX_COMMENTERS_COUNT < commentAuthorCount)
					MAX_COMMENTERS_COUNT = commentAuthorCount;	
    			//if (commentAuthorCount > commentAuthCountLimit){
    				Statement mostDiscussedVideoAuthorsQuery = conn.createStatement();    		
    	    		ResultSet mostDiscussedVideoAuthorsResultSet = mostDiscussedVideoAuthorsQuery.executeQuery(
    	    				"SELECT CommentAuthor, CommentedVideosCount " +
    	    				"FROM commentvideoauthors_mdva " +
    	    				"WHERE VideoAuthor='"+strVideoAuthor+"' AND CommentedVideosCount >=" + commentedVideosCountLimit);
    	    		// strVideoAuthor is intentionally substringed and reversed
    	    		if (strVideoAuthor.length() > 5)
    	    			strVideoAuthor = new StringBuffer(strVideoAuthor.substring(0, 4)).reverse().toString();
    	    		else
    	    			strVideoAuthor = new StringBuffer(strVideoAuthor).reverse().toString();
    	    		uTubeVideoAuthor videoAuthor = new uTubeVideoAuthor(strVideoAuthor);
    	    		while (mostDiscussedVideoAuthorsResultSet.next()){
    	    			String strCommentAuthor = mostDiscussedVideoAuthorsResultSet.getString("CommentAuthor");    	    			
    	    			Integer intCommentedVideosCount = mostDiscussedVideoAuthorsResultSet.getInt("CommentedVideosCount");
    	    			if( intCommentedVideosCount > MAX_COMMENTED_VIDEOS_COUNT)
    	    				MAX_COMMENTED_VIDEOS_COUNT = intCommentedVideosCount; 
    	    			// strVideoAuthor is intentionally substringed and reversed
        	    		if (strCommentAuthor.length() > 5)
        	    			strCommentAuthor = new StringBuffer(strCommentAuthor.substring(0, 4)).reverse().toString();
        	    		else
        	    			strCommentAuthor = new StringBuffer(strCommentAuthor).reverse().toString();
    	    			uTubeCommentAuthor commentAuthor = new uTubeCommentAuthor(strCommentAuthor);
    		        	commentAuthor.setVideosCommented(intCommentedVideosCount);
    		        	if (videoCommentAuthorsMap.containsKey(videoAuthor) == false){
        	    			ArrayList<uTubeCommentAuthor> commentAuthorsListNew = new ArrayList<uTubeCommentAuthor>();
        	    			commentAuthorsListNew.add(commentAuthor);
        	    			videoCommentAuthorsMap.put(videoAuthor, commentAuthorsListNew);
        	    		}else{
        	    			((ArrayList<uTubeCommentAuthor>)(videoCommentAuthorsMap.get(videoAuthor))).add(commentAuthor);    	    			
        	    		}
    	       		}    	    		
    			//}
    		}    		
    		System.out.println("\t\t -- videoCommentAuthorsMap.entrySet().size() == " + videoCommentAuthorsMap.entrySet().size());
    		conn.close();			
    	}catch (Exception e){
    		e.printStackTrace();
    	}
	}
	public void startFrLayoutTimer() {
		System.out.println("\t --> In Authors_NetGraph.startFrLayoutTimer()");
		validate();	
		timer.schedule(new FRLayoutTaskReminder(), 5000, 5000); //subsequent rate
	}
	class FRLayoutTaskReminder extends TimerTask {
		@Override
		public void run() {
			System.out.println("\t --> In Authors_NetGraph.FRLayoutTaskReminder.run()");
			visViewer.updateUI();
			if(done) cancel();
		}
	}
	private void clearEdges(){
		System.out.println("\t --> In Authors_NetGraph.clearEdges()");
		int edgeCount = graph.getEdgeCount();       	
       	for(int i = 0 ; i < edgeCount ; i++){
       		System.out.println("Removing Edge # "  + i);
       		graph.removeEdge(i);       		
       	}        
	}
	public void prepareGraph() {
		System.out.println("\t --> In Authors_NetGraph.prepareGraph()");
		this.graph = new ObservableGraph<String , Number>(
        								Graphs.<String,Number>synchronizedDirectedGraph(
        										new DirectedSparseMultigraph<String,Number>()));
		Set<String> seedVertices = new HashSet<String>(); 
		Map<Number,Number> edgeWeightsMap = new HashMap<Number,Number>();
		
		for (uTubeVideoAuthor videoAuthor : videoCommentAuthorsMap.keySet() ) {
			int commentersCount = ((ArrayList<uTubeCommentAuthor>)(videoCommentAuthorsMap.get(videoAuthor))).size();
			if (commentersCount >= commentAuthorsCountLimit){
				String videoAuthorVertex = "VA: "+videoAuthor +"#"+commentersCount; 
				graph.addVertex(videoAuthorVertex) ;
				seedVertices.add(videoAuthorVertex);
				ArrayList<uTubeCommentAuthor> commentAuthorList = videoCommentAuthorsMap.get( videoAuthor );
				Iterator<uTubeCommentAuthor> commentAuthorListIterator = commentAuthorList.iterator();
				while(commentAuthorListIterator.hasNext()){
					uTubeCommentAuthor commentAuthor =  ((uTubeCommentAuthor)commentAuthorListIterator.next());
					if (commentAuthor.getVideosCommented() >= commentedVideosCountLimit){
						String commentAuthorVertex = "CA: "+commentAuthor.getAuthorID() +"#"+commentAuthor.getVideosCommented();
						graph.addEdge(graph.getEdgeCount(), commentAuthorVertex, videoAuthorVertex);
						edgeWeightsMap.put(graph.getEdgeCount()-1, .4);
					}
				}
			}
		}		
        edge_transformer = new NumberFormattingTransformer<Number>(MapTransformer.getInstance(edgeWeightsMap)); 
          
        // collect the seeds used to define the random graph 
  
        if (seedVertices.size() < 2) 
            System.out.println("need at least 2 seeds (one source, one sink)"); 
          
        // use these seeds as source and sink vertices, run VoltageRanker 
        boolean source = true; 
        Set<String> sources = new HashSet<String>(); 
        Set<String> sinks = new HashSet<String>(); 
        for(String v : seedVertices) 
        { 
            if (source) 
                sources.add(v); 
            else 
                sinks.add(v); 
            source = !source; 
        } 
        try{
	        VoltageScorer<String, Number> voltage_scores =  
	            new VoltageScorer<String, Number>(this.graph,  
	                    MapTransformer.getInstance(edgeWeightsMap), sources, sinks); 
	        voltage_scores.evaluate(); 
	        voltage_transformer = new VertexScoreTransformer<String, Double>(voltage_scores); 
	        vertex_transformer = new NumberFormattingTransformer<String>(voltage_transformer);
        }catch(Exception e){
        	e.printStackTrace();
        }
          
        Collection<String> verts = this.graph.getVertices(); 
          
        // assign a transparency value of 0.9 to all vertices 
        for(String v : verts) { 
        	transparencyMap.put(v, new Double(0.9)); 
        }       
    }	
	
	public void initFrLayout() {
		System.out.println("\t --> In Authors_NetGraph.initFrLayout()");	
		frLayout = new FRLayout<String,Number>(this.graph);   
		final Dimension d = new Dimension(screenDimension.width-250, screenDimension.height-400);
		visViewer = new VisualizationViewer<String,Number>(frLayout, d);
		visViewer.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.CNTR);
		visViewer.getRenderContext().setVertexLabelTransformer(new ToStringLabeller<String>());
		visViewer.setForeground(Color.BLACK);
		add(visViewer);
		btnSwitchLayout = new JButton("Switch to SpringLayout");
		btnSwitchLayout.addActionListener(new ActionListener() {

			@SuppressWarnings("unchecked")
			public void actionPerformed(ActionEvent ae) {
								
				if (btnSwitchLayout.getText().indexOf("Spring") > 0) {
					btnSwitchLayout.setText("Switch to FRLayout");
					frLayout = new SpringLayout<String,Number>(graph,
							new ConstantTransformer(EDGE_LENGTH));
					frLayout.setSize(d);
					visViewer.getModel().setGraphLayout(frLayout, d);					
				} else {
					btnSwitchLayout.setText("Switch to SpringLayout");
					frLayout = new FRLayout<String,Number>(graph, d);
					visViewer.getModel().setGraphLayout(frLayout, d);					
				}
			}
		});

		// Transformer maps the vertex number to a vertex property
		Transformer<String, Paint> vertexColor = new Transformer<String, Paint>() {
			public Paint transform(String str) {
				Color color = new Color(1);						
				if (str.substring(0, 3).equals("VA:"))
					color = Color.GREEN;
				if (str.substring(0, 3).equals("CA:"))
					color = Color.BLUE;								
				return color;
			}
		};
		visViewer.getRenderContext().setVertexFillPaintTransformer(vertexColor);
		Transformer<String, String> vertexLabeler = new Transformer<String, String>() {
			public String transform(String label) {
				String newLabel = label;						
				if (label.substring(0, 3).equals("VA:"))
					newLabel = label.substring(3, label.indexOf('#'));
				if (label.substring(0, 3).equals("CA:")){
					newLabel = label.substring(3, label.indexOf('#'));		
				}
				return newLabel;
			}
		};
		visViewer.getRenderContext().setVertexLabelTransformer(vertexLabeler);
		// Transformer maps the vertex number to a vertex property
		Transformer<String, Font> vertexFont = new Transformer<String, Font>() {
			public Font transform(String vertexLabel) {				
				Font font = new Font("Serif", Font.PLAIN, 10);			
				String vertexFontSize;
				Integer size = 1;                
                if ( vertexLabel.indexOf("#") > 0 ){
                	vertexFontSize = vertexLabel.substring(vertexLabel.lastIndexOf("#") + 1);	            
	                try{
	                	size = Integer.parseInt(vertexFontSize);
	                }catch (Exception e) {
	                	e.printStackTrace();                
	                }
                }               
                if(size == 1) return font;
                else{
                	float scale = 0.0F;
                	if (vertexLabel.substring(0,3).equals("VA:"))
	                	scale = (size*1.0F)/(MAX_COMMENTERS_COUNT*1.0F) * 2;
	                else if (vertexLabel.substring(0,3).equals("CA:"))
	                	scale = (size*1.0F)/(MAX_COMMENTED_VIDEOS_COUNT*1.0F) * 2;
	                return font.deriveFont(font.getSize()+scale);
                }			
			}
		};
		visViewer.getRenderContext().setVertexFontTransformer(vertexFont);
		Transformer<String, Shape> vertexSize = new Transformer<String, Shape>(){
            public Shape transform(String vertexLabel){
            	Ellipse2D circle = new Ellipse2D.Double(-15, -15, 30, 30);
                // in this case, the vertex is twice as large
                Integer size = 1;
                String vertexSize = "";
                if ( vertexLabel.indexOf("#") > 0 ){
                	vertexSize = vertexLabel.substring(vertexLabel.lastIndexOf("#") + 1);	            
	                try{
	                	size = Integer.parseInt(vertexSize);
	                }catch (Exception e) {
	                	e.printStackTrace();                
	                }
                }   
                float scale = 0.0F;
                if (vertexLabel.substring(0,3).equals("VA:")){
                	scale = (size*1.0F)/(MAX_COMMENTERS_COUNT*1.0F) * 2;
                	return AffineTransform.getScaleInstance(scale, scale).createTransformedShape(circle);
                }
                else if (vertexLabel.substring(0,3).equals("CA:")){
                	scale = (size*1.0F)/(MAX_COMMENTED_VIDEOS_COUNT*1.0F) * 2;
                	return AffineTransform.getScaleInstance(scale, scale).createTransformedShape(circle);
                }	 
                return circle;
            }
        };
        visViewer.setVertexToolTipTransformer(new VertexToolTipTransformer<String>());
        visViewer.getRenderContext().setVertexShapeTransformer(vertexSize);		
		Transformer<Number,String> edgeLabeler = new Transformer<Number,String>(){
			 public String transform(Number e) {
				 return "Edge:" + e;
			 }
		 };
		Transformer<String, String> edgeLabeler2 = new Transformer<String, String>(){
			 public String transform(String s) {
				 return "Edge:" + s;
			 }
		 };
		
		 visViewer.getRenderContext().setEdgeFontTransformer(new Transformer<Number, Font>() {
            public Font transform(Number arg0) {
                return EDGE_FONT;
            }
        });		 
		/*Picking and Highlighting Picked Vertexes*/
		PickedState<String> picked_state = visViewer.getPickedVertexState();	 
		vertexStrokehilite = new VertexStrokeHighlight<String,Number>(this.graph, picked_state);
		visViewer.getRenderContext().setVertexStrokeTransformer(vertexStrokehilite); 
		vertexDisplayPredicate = new VertexDisplayPredicate<Integer,Number>(true); 
		 
        final DefaultModalGraphMouse graphMouse = new DefaultModalGraphMouse();
		visViewer.setGraphMouse(graphMouse);
        visViewer.addKeyListener(graphMouse.getModeKeyListener());
        JComboBox modeComboBox = graphMouse.getModeComboBox();
        modeComboBox.addItemListener(graphMouse.getModeListener());
		graphMouse.setMode(ModalGraphMouse.Mode.TRANSFORMING);

		Box vertexBox = Box.createVerticalBox();
		vertexBox.setBorder(BorderFactory.createTitledBorder("Vertices"));
		chkBoxHiliteOnStroke = new JCheckBox("stroke highlight on selection"); 
		chkBoxHiliteOnStroke.addActionListener(this); 
        vertexBox.add(chkBoxHiliteOnStroke);       
        v_small = new JCheckBox("hide vertecis"); 
        v_small.addActionListener(this); 
        vertexBox.add(v_small); 
        
        JPanel graphControlPanel = new JPanel();        		
        graphControlPanel.add(vertexBox);		
        graphControlPanel.add(btnSwitchLayout);		
        graphControlPanel.add(modeComboBox);		
		
		JPanel uodVisGraphPanel = new JPanel();
		uodVisGraphPanel.add(visViewer);
		
		add(uodVisGraphPanel, BorderLayout.CENTER);
		add(graphControlPanel, BorderLayout.SOUTH);       
	}
	private final static class VertexToolTipTransformer<V> implements Transformer<V, String> 
    {		
		public String transform(V v) {	
			return v.toString();
		}
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
	} 
}


