package yt.vis;


import yt.HarVis;
import yt.har.uTubeDataManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.functors.ConstantTransformer;
import org.apache.commons.collections15.functors.MapTransformer;

import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.SpringLayout;
import edu.uci.ics.jung.algorithms.scoring.VoltageScorer;
import edu.uci.ics.jung.algorithms.scoring.util.VertexScoreTransformer;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.ObservableGraph;
import edu.uci.ics.jung.graph.predicates.VertexPredicate;
import edu.uci.ics.jung.graph.util.Graphs;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.NumberFormattingTransformer;
import edu.uci.ics.jung.visualization.picking.PickedInfo;
import edu.uci.ics.jung.visualization.picking.PickedState;
import edu.uci.ics.jung.visualization.renderers.Renderer;

//import edu.uci.ics.jung.graph.decorators.DefaultToolTipFunction;

@SuppressWarnings("serial")
public class UoD_NetGraph extends JPanel implements ActionListener{	
	public static final Font EDGE_FONT = new Font("SansSerif", Font.PLAIN, 10);	
	private VertexStrokeHighlight vertexStrokehilite; 
	
	private Map<Number,Number> edge_weight = new HashMap<Number,Number>(); 
	//	private Transformer<Number,String> edge_transformer; 
//	private Transformer<String, String> vertex_transformer;    
	private String words_2b_filtered = "";
	private Map<String,Number> transparencyMap = new HashMap<String,Number>();
	protected VertexPredicate vertexDisplayPredicate; 	
	private Graph<String,Number> graph = null;
	private AbstractLayout<String,Number> frLayout = null;
	private VisualizationViewer visViewer;
	private JButton btnSwitchLayout = new JButton("Switch to SpringLayout");	
	boolean srcTableNamesLoaded = false;
	Vector<String> tableNamesVector = new Vector<String>();
	private String srcTableName = "";
	private JCheckBox chkBoxHiliteOnStroke, v_small;
		
	private static final int EDGE_LENGTH = 10000;
	private Timer timer;
	private boolean done;
	private ArrayList<UoDEntry> UoDList;
	private ArrayList<UoD_Word> UoD_WordsList = new ArrayList<UoD_Word>();
	private Dimension screenDimension = Toolkit.getDefaultToolkit().getScreenSize();
	private Integer min_word_count = 100000000; // Arbitrarily Large Number
	private Integer max_word_count = 0;	
	private Integer min_coword_count = 100000000; // Arbitrarily Large Number
	private Integer max_coword_count = 0;
	private Integer min_coOccur_count = 100000000; // Arbitrarily Large Number
	private Integer max_coOccur_count = 0;
	private Integer coOccurCountLmt = 0;
	private Integer wordCountLmt = 0;	
	private Integer coWordCountLmt = 0;
	private JSlider sliderWordCountLimit = new JSlider();
	private JSlider sliderCoWordCountLimit = new JSlider();
	private JSlider sliderCoOccurCountLimit = new JSlider();
		
	public UoD_NetGraph(uTubeDataManager dataManager){ 
		System.out.println("\t --> In UoD_NetGraph.UoD_NetGraph()");
		loadFilterWords();
		setLayout(new BorderLayout());		
		add(prepareUoDParamComponents(), BorderLayout.NORTH);
		setBackground(Color.white);
	}	
	private void setSliderValues(){
		System.out.println("\t --> In UoD_NetGraph.setSliderValues()");
		// sliderWordCountLimit
		sliderWordCountLimit.setMajorTickSpacing(((max_word_count-min_word_count)/10));
		//sliderWordCountLimit.setMinorTickSpacing(((max_word_count-min_word_count)/10)/10);
		sliderWordCountLimit.setMinimum(min_word_count);
		sliderWordCountLimit.setMaximum(max_word_count);
		sliderWordCountLimit.repaint();
		// sliderCoWordCountLimit
		sliderCoWordCountLimit.setMajorTickSpacing(((max_coword_count-min_coword_count)/10));
		//sliderCoWordCountLimit.setMinorTickSpacing(((max_coword_count-min_coword_count)/10)/10);
		sliderCoWordCountLimit.setMinimum(min_coword_count);
		sliderCoWordCountLimit.setMaximum(max_coword_count);
		sliderCoWordCountLimit.repaint();
		// sliderCoOccurCountLimit
		sliderCoOccurCountLimit.setMajorTickSpacing(((max_coOccur_count-min_coOccur_count)/10));
		//sliderCoOccurCountLimit.setMinorTickSpacing(((max_coOccur_count-min_coOccur_count)/10)/10);
		sliderCoOccurCountLimit.setMinimum(min_coOccur_count);
		sliderCoOccurCountLimit.setMaximum(max_coOccur_count);		
		sliderCoOccurCountLimit.repaint();
	}
	
	private Box prepareUoDParamComponents(){
		System.out.println("\t --> In UoD_NetGraph.prepareUoDParamComponents()");
		Box paramBox = Box.createHorizontalBox();
		
		JPanel visParamPanel = new JPanel();
		
		visParamPanel.setLayout(new GridLayout(1,3));		
		
		sliderWordCountLimit.setBorder(BorderFactory.createTitledBorder("Word Count Limit"));
		sliderWordCountLimit.setPaintTicks(true);
		sliderWordCountLimit.setPaintLabels(true);		
		sliderWordCountLimit.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent event) {
				JSlider slider = (JSlider)event.getSource();
				if (! slider.getValueIsAdjusting()) {
					wordCountLmt = slider.getValue();
					prepareGraph();
					frLayout.setGraph(graph);
				}
			}			
		});
		visParamPanel.add(sliderWordCountLimit);
		
		sliderCoWordCountLimit.setBorder(BorderFactory.createTitledBorder("CoWord Count Limit"));
		sliderCoWordCountLimit.setPaintTicks(true);
		sliderCoWordCountLimit.setPaintLabels(true);		
		sliderCoWordCountLimit.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent event) {
				JSlider slider = (JSlider)event.getSource();
				if (! slider.getValueIsAdjusting()) {
					coWordCountLmt = slider.getValue();
					prepareGraph();
					frLayout.setGraph(graph);
				}
			}			
		});
		visParamPanel.add(sliderCoWordCountLimit);
		
		sliderCoOccurCountLimit.setBorder(BorderFactory.createTitledBorder("Co-Occurrance Count Limit"));
		sliderCoOccurCountLimit.setPaintTicks(true);
		sliderCoOccurCountLimit.setPaintLabels(true);		
		sliderCoOccurCountLimit.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent event) {
				JSlider slider = (JSlider)event.getSource();
				if (! slider.getValueIsAdjusting()) {
					coOccurCountLmt = slider.getValue();
					prepareGraph();
					frLayout.setGraph(graph);
				}
			}			
		});
		visParamPanel.add(sliderCoOccurCountLimit);
		
		paramBox.add(visParamPanel);	
		
		JPanel comboPanel = new JPanel();
		final JComboBox<String> srcTableNamesCombo = new JComboBox<String>(tableNamesVector);
		srcTableNamesCombo.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				String item = (String)e.getItem();
				if (e.getStateChange() == ItemEvent.SELECTED){
					srcTableName = item;
					System.out.println("\t\t -- " + srcTableName + " is selected.");					
				}
			}
		});
				
		if (srcTableNamesLoaded == false){
			Vector<String> tablesNamesVector = uTubeDataManager.getTableNames("uod%");
			for(int i = 0 ; i < tablesNamesVector.size() ; i ++){
				srcTableNamesCombo.addItem(tablesNamesVector.get(i));					
			}
			srcTableNamesLoaded = true;
		}
		
				
		comboPanel.add(new JLabel("Select Data Source"));
		comboPanel.add(srcTableNamesCombo);
		JButton btnLaunchGraph = new JButton("Launch Graph");
		btnLaunchGraph.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				pauseFrLayoutTimer();
				srcTableName = srcTableNamesCombo.getSelectedItem().toString();				
				populateUoD_Data();				
				prepareGraph();				
				initFrLayout();
				setSliderValues();
				startFrLayoutTimer();
			}
		});
		comboPanel.add(btnLaunchGraph);
		paramBox.add(comboPanel);		
		
		return paramBox;
	}
	public static boolean isNumeric(String str)
	{
	  return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
	}
	private void loadFilterWords(){
		System.out.println("\t --> In UoD_NetGraph.loadFilterWords()");
		URL fileURL = HarVis.class.getResource("Resources/word_2b_filtered");
		words_2b_filtered = "";
		try {			
			FileReader fr = new FileReader(fileURL.getFile());
			BufferedReader br = new BufferedReader(fr);
			String line = br.readLine();
			words_2b_filtered += line;
			while(line != null){				
				line = br.readLine();
				words_2b_filtered += line;
			}
		} catch (FileNotFoundException e) {			
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void populateUoD_Data1(){		
		System.out.println("\t --> In UoD_NetGraph.populateUoD()");
		try{    		
    		Connection conn = uTubeDataManager.getDBConnection();    		
    		UoDList = new ArrayList<UoDEntry>();
    		Statement uodQuery = conn.createStatement();
    		ResultSet uodResultSet = uodQuery.executeQuery(    		
    		" SELECT * FROM "+ srcTableName +
    		" WHERE WordCount >= " + 100 +
    		" AND coWordCount >= " + 100 +    		
    		" ORDER BY WordCount DESC, CoWordCount DESC " +
    		" LIMIT 10000" );
    		
    		while (uodResultSet.next()){
    			String strWord = uodResultSet.getString("Word");
    			if( isNumeric(strWord) ){
    				continue;
    			}
    			StringTokenizer st = new StringTokenizer(words_2b_filtered, ","); 
    			boolean filterWordFound = false;
    			while (st.hasMoreTokens()) {
    				String strFilterWord = st.nextToken().toLowerCase();    				
    				if (strWord.equalsIgnoreCase(strFilterWord)){    					
    					filterWordFound = true;
    					break;	
    				}
    			}
    			if (filterWordFound) continue;
    			
    			int wordCount = uodResultSet.getInt("WordCount");
    			int wordAuthCount = uodResultSet.getInt("AuthorsCount");
    			String strCoWord = uodResultSet.getString("CoWord");
    			int coWordCount = uodResultSet.getInt("CoWordCount");
    			int coWordAuthCount = uodResultSet.getInt("CoWordAuthorsCount");    			
    			int coOccerenceCunt = uodResultSet.getInt("CoOccurenceCount");		
    			boolean isQueryWord = uodResultSet.getBoolean("isQueryWord");        		
        			if (wordCount > max_word_count)
        				max_word_count = wordCount;
        			if (min_word_count > wordCount)
        				min_word_count = wordCount;
        			if (coWordCount > max_coword_count)
        				max_coword_count = coWordCount;
        			if (min_coword_count > coWordCount)
        				min_coword_count = coWordCount;
        			if (coOccerenceCunt > max_coOccur_count)
        				max_coOccur_count = coOccerenceCunt;
        			if (min_coOccur_count > coOccerenceCunt)
        				min_coOccur_count = coOccerenceCunt;
        			
        			UoDEntry uodEntryObj = new UoDEntry(strWord,wordCount, wordAuthCount, strCoWord, coWordCount, coWordAuthCount, coOccerenceCunt, isQueryWord);
        			UoDList.add(uodEntryObj);        		
    		}    		
    		conn.close();    		
    	}catch (Exception e){
    		e.printStackTrace();
    	}
		setSliderValues();
	}	
	public void populateUoD_Data(){		
		System.out.println("\t --> In UoD_NetGraph.populateUoD()");		
		UoD_WordsList.clear();
		min_word_count 		= 100000000; // Arbitrarily Large Number
		max_word_count 		= 0;	
		min_coword_count 	= 100000000; // Arbitrarily Large Number
		max_coword_count 	= 0;
		min_coOccur_count 	= 100000000; // Arbitrarily Large Number
		max_coOccur_count 	= 0;
		try{    		
    		Connection conn = uTubeDataManager.getDBConnection();    		
    		UoDList = new ArrayList<UoDEntry>();
    		Statement wordsQuery = conn.createStatement();
    		ResultSet wordsQueryResultSet = wordsQuery.executeQuery(    		
    		" SELECT Word, WordCount, AuthorsCount, isQueryWord, COUNT(*) CoWordsCount " +
    		" FROM "+ srcTableName +    		
    		" GROUP BY Word "+    		
    		" Order By WordCount DESC");
    		while (wordsQueryResultSet.next()){
    			String strWord = wordsQueryResultSet.getString("Word");
    			Integer coWordsCount = wordsQueryResultSet.getInt("CoWordsCount");
    			Integer wordCount = wordsQueryResultSet.getInt("WordCount");    			
    			boolean isQueryWord = wordsQueryResultSet.getBoolean("isQueryWord");
    			Integer wordAuthCount = wordsQueryResultSet.getInt("AuthorsCount");
    			UoD_Word wordObj = new UoD_Word(wordCount, wordAuthCount, strWord, isQueryWord);
    			if (wordCount > max_word_count)
    				max_word_count = wordCount;
    			if (min_word_count > wordCount)
    				min_word_count = wordCount;
    			if( isNumeric(strWord) ){
    				continue;
    			}    			
    			if (coWordsCount >= 1){
    				Statement coWordsQuery = conn.createStatement();
    	    		ResultSet coWordsQueryResultSet = coWordsQuery.executeQuery(    		
    	    		" SELECT CoWord, CoWordCount, CoWordAuthorsCount, CoOccurenceCount, isQueryWord " +
    	    		" FROM " + srcTableName +
    	    		" WHERE Word = '" + strWord +"'" +
    	    		" ORDER BY CoWordCount DESC");
    	    		
    	    		while (coWordsQueryResultSet.next()){   	    			
    	    			String strCoWord = coWordsQueryResultSet.getString("CoWord").trim();
    	    			Integer coWordCount = coWordsQueryResultSet.getInt("CoWordCount");
    	    			Integer coWordAuthCount = coWordsQueryResultSet.getInt("CoWordAuthorsCount");    			
    	    			Integer coOccurCount = coWordsQueryResultSet.getInt("CoOccurenceCount");    	    			
//    	    			StringTokenizer st = new StringTokenizer(words_2b_filtered, ",");
//    	    			
//    	    			boolean skipWord = false;
//    	    			while (st.hasMoreTokens()) {
//    	    				
//    	    				String strFilterWord = st.nextToken().toLowerCase();     	    				
//    	    				if (strCoWord.equalsIgnoreCase(strFilterWord)){ 	 	    					
//    	    					skipWord = true;
//    	    					break;	
//    	    				}
//    	    			}
//    	    			if (skipWord) continue;
    	    			    	    			
            			if (coWordCount > max_coword_count)
            				max_coword_count = coWordCount;
            			if (min_coword_count > coWordCount)
            				min_coword_count = coWordCount;
            			if (coOccurCount > max_coOccur_count)
            				max_coOccur_count = coOccurCount;
            			if (min_coOccur_count > coOccurCount)
            				min_coOccur_count = coOccurCount;            			
            			UoD_CoWord coWordObj = new UoD_CoWord(coWordCount, coWordAuthCount, strCoWord, false);
            			coWordObj.setCoOccurenceCount(coOccurCount);
            			wordObj.coWordsList.add(coWordObj);            			
            		}
    	    		System.out.println("\t\t" + wordObj.getStrWord() + " CoWords " + wordObj.coWordsList.size());
    	    		UoD_WordsList.add(wordObj);
    			}
    		}  			
    		conn.close();    		
    	}catch (Exception e){
    		e.printStackTrace();
    	}		
	}	
	private void startFrLayoutTimer() {		
		System.out.println("\t --> In UoD_NetGraph.startFrLayoutTimer()");
		validate();	
		timer = new Timer();		
		timer.schedule(new FRLayoutTaskReminder(), 1000, 1000); //subsequent rate
	}
	private void pauseFrLayoutTimer() {		
		System.out.println("\t --> In UoD_NetGraph.pauseFrLayoutTimer()");
		if (timer != null){
			timer.cancel();
			timer.purge();
		}
	}
	
	class FRLayoutTaskReminder extends TimerTask {
		@Override
		public void run() {			
			System.out.println("\t --> In UoD_NetGraph.FRLayoutTaskReminder.run()");
			visViewer.updateUI();
			if(done) cancel();
		}
	}
	private void clearEdges(){
		int edgeCount = graph.getEdgeCount();       	
       	for(int i = 0 ; i < edgeCount ; i++){
       		System.out.println("Removing Edge # "  + i);
       		graph.removeEdge(i);       		
       	}        
	}
	public void initFrLayout() {
		System.out.println("\t --> In UoD_NetGraph.initFrLayout()");		
		frLayout = new FRLayout<String, Number>(this.graph); 
		final Dimension d = new Dimension(screenDimension.width-100, screenDimension.height-150);
		visViewer = new VisualizationViewer<String,Number>(frLayout, d);
		visViewer.setBackground(Color.WHITE);
		visViewer.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.CNTR);
		visViewer.getRenderContext().setVertexLabelTransformer(new VertexLabeler<String>());      	
      	visViewer.getRenderContext().setVertexFillPaintTransformer(new VertexColorTransformer<String>());
      	visViewer.getRenderContext().setVertexDrawPaintTransformer(new VertexColorTransformer<String>());
      	visViewer.setVertexToolTipTransformer(new VertexToolTipTransformer<String>());      	      	
      	Transformer<String, Font> vertexFont = new Transformer<String, Font>() {
			public Font transform(String vertexLabel) {				
				Font font = new Font("Serif", Font.BOLD, 12);			
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
	                scale = (size*1.0F)/(max_word_count*1.0F)*2;	                
	                return font.deriveFont(font.getSize()+scale);
                }			
			}
		};
		visViewer.getRenderContext().setVertexFontTransformer(vertexFont);
		Transformer<String, Shape> vertexShape = new Transformer<String, Shape>() {
				public Shape transform(String label){
					Ellipse2D circle = new Ellipse2D.Double(-15, -15, 30, 30);
					Integer size = 1;
					float scale = 1;
            
					if (label.indexOf("[Auth") == 0){
						size = Integer.parseInt(label.substring(label.lastIndexOf("]") + 1));
						scale = size/100.0F + 1;
						return circle;	
						}else if ( label.indexOf("#") > 0 ){
							label = label.substring(label.lastIndexOf("#") + 1);	            
		                try{
		                	size = Integer.parseInt(label);
		                }catch (Exception e) {
		                	e.printStackTrace();                
		                }	        
	                	scale = (size*1.0F)/(max_word_count*1.0F) * 4;	        
					}                        
					return AffineTransform.getScaleInstance(scale, scale).createTransformedShape(circle);
				}
		};
		visViewer.getRenderContext().setVertexShapeTransformer(vertexShape);
        
		/*Picking and Highlighting Picked Vertexes*/
		PickedState<String> picked_state = visViewer.getPickedVertexState();	 
		vertexStrokehilite = new VertexStrokeHighlight<String,Number>(this.graph, picked_state);
		visViewer.getRenderContext().setVertexStrokeTransformer(vertexStrokehilite); 
		vertexDisplayPredicate = new VertexDisplayPredicate<Integer,Number>(true);
        
		/*Edge Color and shape*/
		Transformer<Number, Paint> edgePaint = new Transformer<Number, Paint>() {
		    public Paint transform(Number s) {
		    	//System.out.println(s);
		        return Color.GRAY;
		    }
		};

		visViewer.getRenderContext().setEdgeDrawPaintTransformer(edgePaint);
		visViewer.getRenderContext().setArrowFillPaintTransformer(new ConstantTransformer(Color.LIGHT_GRAY));
		visViewer.getRenderContext().setArrowDrawPaintTransformer(new ConstantTransformer(Color.LIGHT_GRAY));

		DefaultModalGraphMouse graphMouse = new DefaultModalGraphMouse();
		JComboBox modeComboBox = graphMouse.getModeComboBox();
        visViewer.setGraphMouse(graphMouse);
        visViewer.addKeyListener(graphMouse.getModeKeyListener());		
        modeComboBox.addItemListener(graphMouse.getModeListener());
		graphMouse.setMode(ModalGraphMouse.Mode.TRANSFORMING);	
			
		Box vertexBox = Box.createVerticalBox();
		vertexBox.setBorder(BorderFactory.createTitledBorder("Vertices"));
		chkBoxHiliteOnStroke = new JCheckBox("Highlight vertexes on selection"); 
		chkBoxHiliteOnStroke.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AbstractButton source = (AbstractButton)e.getSource(); 
				vertexStrokehilite.setHighlight(source.isSelected());		         				
			}
		}); 
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
	}
	public void prepareGraph() {
		System.out.println("\t --> In UoD_NetGraph.prepareGraph()");
		graph = new ObservableGraph<String , Number>(
        								Graphs.<String,Number>synchronizedDirectedGraph(
        										new DirectedSparseMultigraph<String,Number>()));
   
		Set<String> seedVertices = new HashSet<String>();
		
//        Iterator<UoDEntry> iterator = UoDList.iterator();
		Iterator<UoD_Word> wordsIter = UoD_WordsList.iterator();
       	
       	String word = "";
       	String strAuthorList = "";           	
        int i = 0;
       	while(wordsIter.hasNext()){
       		UoD_Word uodWordObj = (UoD_Word)wordsIter.next();
        	if (uodWordObj.getWordCount() >= wordCountLmt ){
            	word =  "[Word] "+uodWordObj.getStrWord() + "#" + uodWordObj.getWordCount();
            	strAuthorList = "[Auth "+ i++ +"]"+ uodWordObj.getWordAuthCount();
            	graph.addVertex(word);
            	seedVertices.add(word);
            	Iterator<UoD_CoWord> coWordsIter = uodWordObj.coWordsList.iterator();
            	while (coWordsIter.hasNext()){
            		UoD_CoWord uodCoWordObj = (UoD_CoWord)coWordsIter.next();
	            	String coWord = "[Word] " + uodCoWordObj.getStrWord() + "#" + uodCoWordObj.getWordCount();
	            	if (uodCoWordObj.getCoOccurenceCount() >= coOccurCountLmt || uodCoWordObj.getWordCount() >= coWordCountLmt){
		            	graph.addEdge(graph.getEdgeCount(), coWord, word);
		            	edge_weight.put(graph.getEdgeCount()-1, .4);
	            	}            	
            	}
        	}
        }        
       	
       	System.out.println("\t\t -- Seed Vertices Count" + seedVertices.size());
       	if (seedVertices.size() >= 2) {
	       	Transformer<Number,String> edge_transformer = new NumberFormattingTransformer<Number>(MapTransformer.getInstance(edge_weight)); 
	          
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
	        //VoltageScorer<String, Number> voltage_scores =  
	        //    new VoltageScorer<String, Number>(this.graph,  
	        //           MapTransformer.getInstance(edge_weight), sources, sinks); 
	        //voltage_scores.evaluate(); 
	        //voltage_transformer = new VertexScoreTransformer<String, Double>(voltage_scores); 
	        //Transformer<String, String> vertex_transformer = new NumberFormattingTransformer<String>(voltage_transformer); 
	          
	        Collection<String> verts = this.graph.getVertices(); 
	          
	        // assign a transparency value of 0.9 to all vertices 
	        for(String v : verts) { 
	        	transparencyMap.put(v, new Double(0.9)); 
	        }  
	        frLayout = new SpringLayout<String,Number>(graph, new ConstantTransformer(EDGE_LENGTH));	        
       	}
    }	
    private final static class VertexColorTransformer<V> implements Transformer<V, Paint> {
    	@Override
    	public Paint transform(V v) {
    		String str = v.toString();
			Color color = new Color(1);
			if (str.contains("[Authors]") )							
				color = Color.ORANGE;					
			else if (str.contains("[Word]")) 
				color = Color.GREEN;
			else  if(str.contains("[CoWord]"))
				color = Color.ORANGE;							
			return color;
		}	
	}    
    private final static class VertexToolTipTransformer<V> implements Transformer<V, String> 
    {		
		public String transform(V v) {	
			return v.toString();
		}
	}
	private final static class VertexLabeler<V> implements Transformer<V, String> 
    {
		@Override
		public String transform(V v) {	
			String label = v.toString();
			label = label.substring(label.lastIndexOf(']')+1, label.indexOf('#'));
			return label;
		}
	}
//	private final class SeedFillColor<V> implements Transformer<V,Paint> 
//    { 
//        protected PickedInfo<V> pi; 
//        protected final static float dark_value = 0.8f; 
//        protected final static float light_value = 0.2f; 
//        protected boolean seed_coloring; 
//          
//        public SeedFillColor(PickedInfo<V> pi) 
//        { 
//            this.pi = pi; 
//            seed_coloring = false; 
//        } 
//  
//        public void setSeedColoring(boolean b) 
//        { 
//            this.seed_coloring = b; 
//        } 
//          
////        public Paint getDrawPaint(V v) 
////        { 
////            return Color.BLACK; 
////        } 
//          
//        public Paint transform(V v) 
//        { 
//            float alpha = transparencyMap.get(v).floatValue(); 
//            if (pi.isPicked(v)) 
//            { 
//                return new Color(1f, 1f, 0, alpha);  
//            } 
//            else 
//            { 
//                if (seed_coloring && seedVertices.contains(v)) 
//                { 
//                    Color dark = new Color(0, 0, dark_value, alpha); 
//                    Color light = new Color(0, 0, light_value, alpha); 
//                    return new GradientPaint( 0, 0, dark, 10, 0, light, true); 
//                } 
//                else 
//                    return new Color(1f, 0, 0, alpha); 
//            } 
//                  
//        } 
//    } 
	private final class SeedDrawColor<V> implements Transformer<V,Paint> 
    { 
        protected PickedInfo<V> pi; 
        protected final static float dark_value = 0.8f; 
        protected final static float light_value = 0.2f; 
        protected boolean seed_coloring; 
          
        public SeedDrawColor(PickedInfo<V> pi) 
        { 
            this.pi = pi; 
            seed_coloring = false; 
        } 
  
        public void setSeedColoring(boolean b) 
        { 
            this.seed_coloring = b; 
        } 
          
        public Paint transform(V v) 
        { 
            return Color.WHITE; 
        } 
    }
	@Override
	public void actionPerformed(ActionEvent e) {
		AbstractButton source = (AbstractButton)e.getSource(); 
		if (source == v_small) 
        { 
        	vertexDisplayPredicate.filterSmall(source.isSelected()); 
        } 
	} 
	//public UoD_NetGraph{}
}

class UoD_Word{
	/**
	 * @param wordCount
	 * @param wordAuthCount
	 * @param strCoWord
	 * @param isQueryWord
	 */
	public UoD_Word(Integer wordCount, Integer wordAuthCount, String strWord,
			boolean isQueryWord) {
		this.wordCount = wordCount;
		this.wordAuthCount = wordAuthCount;
		this.strWord = strWord;
		this.isQueryWord = isQueryWord;
	}
	private Integer wordCount; 
	private Integer wordAuthCount; 
	private String strWord;
	private boolean isQueryWord;
	public ArrayList<UoD_CoWord> coWordsList = new ArrayList<UoD_CoWord>();
	
	public Integer getWordCount() {
		return wordCount;
	}
	public void setWordCount(Integer wordCount) {
		this.wordCount = wordCount;
	}
	public Integer getWordAuthCount() {
		return wordAuthCount;
	}
	public void setWordAuthCount(Integer wordAuthCount) {
		this.wordAuthCount = wordAuthCount;
	}
	public String getStrWord() {
		return strWord;
	}
	public void setStrWord(String strCoWord) {
		this.strWord = strCoWord;
	}
	public boolean isQueryWord() {
		return isQueryWord;
	}
	public void setQueryWord(boolean isQueryWord) {
		this.isQueryWord = isQueryWord;
	}
	
}
class UoD_CoWord extends UoD_Word{
	
	public Integer getCoOccurenceCount() {
		return coOccurenceCount;
	}

	public void setCoOccurenceCount(Integer coOccurenceCount) {
		this.coOccurenceCount = coOccurenceCount;
	}

	public UoD_CoWord(Integer wordCount, Integer wordAuthCount,
			String strCoWord, boolean isQueryWord) {
		super(wordCount, wordAuthCount, strCoWord, isQueryWord);
		
	}

	private Integer coOccurenceCount;	
}


class UoDEntry {
	/**
	 * @param strWord
	 * @param wordCount
	 * @param wordAuthCount
	 * @param strCoWord
	 * @param coWordCount
	 * @param coWordAuthorsCount
	 * @param coOccerenceCount
	 */
	public UoDEntry(String strWord, int wordCount, int wordAuthCount,
					String strCoWord, int coWordCount, int coWordAuthorsCount,
					int coOccerenceCunt, boolean isQueryWord) {
		this.strWord = strWord;
		this.wordCount = wordCount;
		this.wordAuthCount = wordAuthCount;
		this.strCoWord = strCoWord;
		this.coWordCount = coWordCount;
		this.coWordAuthorsCount = coWordAuthorsCount;
		this.coOccurenceCount = coOccerenceCunt;
		this.isQueryWord = isQueryWord;
	}
	public int getCoOccurenceCount() {
		return coOccurenceCount;
	}
	public void setCoOccurenceCount(int coOccurenceCount) {
		this.coOccurenceCount = coOccurenceCount;
	}
	public int getWordAuthCount() {
		return wordAuthCount;
	}
	public void setWordAuthCount(int wordAuthCount) {
		this.wordAuthCount = wordAuthCount;
	}
	public int getCoWordCount() {
		return coWordCount;
	}
	public void setCoWordCount(int coWordCount) {
		this.coWordCount = coWordCount;
	}
	public int getCoWordAuthorsCount() {
		return coWordAuthorsCount;
	}
	public void setCoWordAuthorsCount(int coWordAuthorsCount) {
		this.coWordAuthorsCount = coWordAuthorsCount;
	}	
	public String getStrWord() {
		return strWord;
	}
	public void setStrWord(String strWord) {
		this.strWord = strWord;
	}
	public int getWordCount() {
		return wordCount;
	}
	public void setWordCount(int wordCount) {
		this.wordCount = wordCount;
	}	
	public String getStrCoWord() {
		return strCoWord;
	}
	public void setStrCoWord(String strCoWord) {
		this.strCoWord = strCoWord;
	}	
	private String strWord;	
	private int wordCount; 
	private int wordAuthCount; 
	private String strCoWord;
	private int coWordCount;
	private int coWordAuthorsCount;
	private int coOccurenceCount;	
	private boolean isQueryWord;
	public boolean isQueryWord() {
		return isQueryWord;
	}
	public void setQueryWord(boolean isQueryWord) {
		this.isQueryWord = isQueryWord;
	}
	
}
