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

import yt.har.uTubeAuthor;
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
public class AuthorSubscribersNet extends JPanel implements ActionListener{

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
	private HashMap<String, ArrayList<String>> authorSubscribersMap;
	private uTubeDataManager uTubeDM;
	private String srcTableName = "";
	private final Integer SOURCE_VERTEX = 0;
	private final Integer SINK_VERTEX = 1;
	private final Integer SOURCE_AND_SINK_VERTEX = 2;
	boolean srcTableNamesLoaded = false;
	public AuthorSubscribersNet(uTubeDataManager dataManager){
		this.uTubeDM = dataManager; 
		this.authorSubscribersMap = new HashMap<String, ArrayList<String>>();
		setLayout(new BorderLayout());				
		add(prepareVisParamComponents(), BorderLayout.NORTH);
		populateAuthorSubs_Data();
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
				commentedVideosCountLimit = slider.getValue();
				prepareGraph();
				frLayout.setGraph(graph);
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
	public void populateAuthorSubs_Data(){
		System.out.println("\t --> In AuthorSubscribersNet.populateAuthorSubs_Data()");
		try{			
    		Connection conn = uTubeDataManager.getDBConnection();    		
    		Statement authorSubscribersQuery = conn.createStatement();
    		ResultSet authorSubscribersResultSet = 
    				authorSubscribersQuery.executeQuery(
    						"SELECT authorID, subscribedToAuthorID  FROM utubeauthorsubs "+
    						"WHERE subscribedToAuthorID IN "+ 
    						"(SELECT DISTINCT(author) from utubevideos);");
    		System.out.println("\t\t -- videoCommentAuthorsMap.clear()");
    		authorSubscribersMap.clear();    		    		
    		while (authorSubscribersResultSet.next()){
    			String  strAuthorID 		=  authorSubscribersResultSet.getString("authorID").toLowerCase();    			
//    			// strVideoAuthor is intentionally substringed and reversed
//	    		if (strAuthorID.length() > 5)
//	    			strAuthorID = new StringBuffer(strAuthorID.substring(0, 4)).reverse().toString();
//	    		else
//	    			strAuthorID = new StringBuffer(strAuthorID).reverse().toString();
	    		String  strSubsAuthorID 	=  authorSubscribersResultSet.getString("subscribedToAuthorID").toLowerCase();
//	    		// strVideoAuthor is intentionally substringed and reversed
//	    		if (strSubsAuthorID.length() > 5)
//	    			strSubsAuthorID = new StringBuffer(strSubsAuthorID.substring(0, 4)).reverse().toString();
//	    		else
//	    			strSubsAuthorID = new StringBuffer(strSubsAuthorID).reverse().toString();
    			
    			if (authorSubscribersMap.containsKey(strAuthorID) == false){
        	    	ArrayList<String> authorsListNew = new ArrayList<String>();
        	    	authorsListNew.add(strSubsAuthorID);
        	    	
        	    	authorSubscribersMap.put(strAuthorID, authorsListNew);
        	    }else{
        	    	((ArrayList<String>)(authorSubscribersMap.get(strAuthorID))).add(strSubsAuthorID);    	    			
        	    }
    	    }    		
    		System.out.println("\t\t -- authorSubscribersMap.entrySet().size() == " + authorSubscribersMap.entrySet().size());
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
		System.out.println("\t --> In AuthorSubscribersNet.prepareGraph()");
		this.graph = new ObservableGraph<String , Number>(
        								Graphs.<String,Number>synchronizedDirectedGraph(
        										new DirectedSparseMultigraph<String,Number>()));
		Set<String> seedVertices = new HashSet<String>(); 
		Map<Number,Number> edgeWeightsMap = new HashMap<Number,Number>();
		
		for (String subscribingAuthor : authorSubscribersMap.keySet() ) {
			int subscriptionsCount = ((ArrayList<String>)(authorSubscribersMap.get(subscribingAuthor))).size();
			if (subscriptionsCount >= commentAuthorsCountLimit){
				ArrayList<String> subscribedToAuthorsList = authorSubscribersMap.get( subscribingAuthor );
				// strVideoAuthor is intentionally substringed and reversed
				if (subscribingAuthor.length() > 10)
					subscribingAuthor = new StringBuffer(subscribingAuthor.substring(0, 9)).reverse().toString();
	    		else
	    			subscribingAuthor = new StringBuffer(subscribingAuthor).reverse().toString();    			
				graph.addVertex(subscribingAuthor) ;
				seedVertices.add(subscribingAuthor);				
				Iterator<String> subscribedAuthorListIterator = subscribedToAuthorsList.iterator();
				while(subscribedAuthorListIterator.hasNext()){
					//String subscribedToAuthor =  "Chn:"+((String)subscribedAuthorListIterator.next()) +"#5"; //Chn: Channel Author
					String subscribedToAuthor =  ((String)subscribedAuthorListIterator.next());
					if (subscribedToAuthor.length() > 10)
						subscribedToAuthor = new StringBuffer(subscribedToAuthor.substring(0, 9)).reverse().toString();
		    		else
		    			subscribedToAuthor = new StringBuffer(subscribedToAuthor).reverse().toString();
	    			
					graph.addEdge(graph.getEdgeCount(), subscribingAuthor, subscribedToAuthor);
						edgeWeightsMap.put(graph.getEdgeCount()-1, .4);
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
				if (str.substring(0, 4).equals("Sub:"))
					color = Color.GREEN;
				if (str.substring(0, 4).equals("Chn:"))
					color = Color.BLUE;								
				return color;
			}
		};
//		visViewer.getRenderContext().setVertexFillPaintTransformer(vertexColor);
		Transformer<String, String> vertexLabeler = new Transformer<String, String>() {
			public String transform(String label) {
				String newLabel = label;						
				if (label.substring(0, 4).equals("Sub:"))
					newLabel = label.substring(4, label.indexOf('#'));
				if (label.substring(0, 4).equals("Chn:")){
					newLabel = label.substring(4, label.indexOf('#'));		
				}
//				System.out.println(newLabel);
				return newLabel;
			}
		};
//		visViewer.getRenderContext().setVertexLabelTransformer(vertexLabeler);
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
                	if (vertexLabel.substring(0, 4).equals("Sub:"))
	                	scale = (size*1.0F)/(MAX_COMMENTERS_COUNT*1.0F) * 2;
	                else if (vertexLabel.substring(0, 4).equals("Chn:"))
	                	scale = (size*1.0F)/(MAX_COMMENTED_VIDEOS_COUNT*1.0F) * 2;
	                return font.deriveFont(font.getSize()+scale);
                }			
			}
		};
//		visViewer.getRenderContext().setVertexFontTransformer(vertexFont);
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
                if (vertexLabel.substring(0,3).equals("Sub:")){
                	scale = (size*1.0F)/(MAX_COMMENTERS_COUNT*1.0F) * 2;
                	return AffineTransform.getScaleInstance(scale, scale).createTransformedShape(circle);
                }
                else if (vertexLabel.substring(0,3).equals("Chn:")){
                	scale = (size*1.0F)/(MAX_COMMENTED_VIDEOS_COUNT*1.0F) * 2;
                	return AffineTransform.getScaleInstance(scale, scale).createTransformedShape(circle);
                }	 
                return circle;
            }
        };
//        visViewer.getRenderContext().setVertexShapeTransformer(vertexSize);
        visViewer.setVertexToolTipTransformer(new VertexToolTipTransformer<String>());
        		
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
		
	} 
}


