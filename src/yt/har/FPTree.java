package yt.har;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import yt.HarVis;

public class FPTree {
	int freqThreshold = 0;
	public FPTree(int freqThresholdVal, ArrayList<ContentItem> contentList){
		rootNode = new Node();
		this.freqThreshold = freqThresholdVal;
		this.contentList = contentList;
	}
	// protected Map<String, Node> itemsList = new HashMap<String, Node>();
	//public Map<String, Node> getItemsList(){return itemsList;}
	public java.util.List<Node> getUniqueNodesList(){return uniqueNodesList;}
	private Node rootNode;
	// Make a wordList and sort the words w.r.t occurrence_count		
	java.util.List<Node> uniqueNodesList = new java.util.ArrayList<Node>();	
	private ArrayList<ContentItem> contentList = new ArrayList<ContentItem>();	
	ArrayList<String> frequencyOrderedContentList = new ArrayList<String>();
	Map<String, Node> nodeFreqMap = new HashMap<String, Node>();
	public Node getRootNode(){		
		return rootNode;
	}
	
	
	class Node{
		private Node parentNode;
		public static final int IS_ROOT_NODE 	= 1;
		public static final int IS_CHILD_NODE 	= 0;
		private int nodeType;
		public int getNodeType(){
			return nodeType;
		}
		public Node(){			
			nodeType = IS_ROOT_NODE;
			nodeName = "$";
		}
		private Node(Node parentNode){ // private: should only be called by addChildNode()
			nodeType = IS_CHILD_NODE;
			this.parentNode = parentNode;			
		}
		@Override
	    public boolean equals(Object object) {

	        if (object != null && object instanceof Node) {
	            Node node = (Node) object;
	            if(this.nodeName.equalsIgnoreCase(node.getNodeName()))
	            	return true;
	            	
//	            if (this.nodeName == null) {
//	                return (node.getNodeName() == null);
//	            }
//	            else {
//	                return this.nodeName.equalsIgnoreCase(node.getNodeName());
//	            }
	        }
	        return false;
	    }

		public Node getParentNode(){return parentNode;}
		
		public String getNodeName() {
			return nodeName;
		}
		private void setNodeName(String nodeName) {
			this.nodeName = nodeName;
		}
		public int getNodeValue() {
			return nodeFreq;
		}
		private void setNodeValue(int nodeValue) {
			this.nodeFreq = nodeValue;
		}
		public void incrementNodeValue() {
			this.nodeFreq++;
		}
		private ArrayList<FPTree.Node>	childNodes = new ArrayList<FPTree.Node>();
		public Node addChildNode(String name) {
			Node childNode = new Node(this);
			childNode.setNodeName(name);
			childNode.setNodeValue(childNode.getNodeValue()+1);
			childNodes.add(childNode);			
			childNode.getNodePath();
			// Update the list of unique items in the fpTree
			if(uniqueNodesList.contains(childNode) == false){
				uniqueNodesList.add(childNode);
			}			
			return childNodes.get(childNodes.size()-1);
		}		
		public Node getDirectChildNode(String name){			
			Node childNodeNull = null;
			Iterator<FPTree.Node> childNodesIter = childNodes.iterator();
			while(childNodesIter.hasNext()){
				Node node = childNodesIter.next();
				if(node.getNodeName().equalsIgnoreCase(name)){					
					return node;
				}
			}
			return childNodeNull;			
		}
		
		
		public ArrayList<Node> getGrandChildNode(String name){			
			ArrayList<Node> nodesList = new ArrayList<Node>();			
			Iterator<FPTree.Node> childNodesIter = childNodes.iterator();
			while(childNodesIter.hasNext() ){
				Node node = childNodesIter.next();
				if(node.getNodeName().equalsIgnoreCase(name)){					
					nodesList.add(node);
				}else{
					// depth first
					ArrayList<Node> nodesList2 = node.getGrandChildNode(name);
					if (nodesList2.size() > 0 )
						nodesList.addAll(nodesList2);
				}
			}
			return nodesList;			
		}
		public boolean hasChildNodes(){
			boolean hasChildNodes = false;
			if(childNodes.size()>0)
				hasChildNodes = true;
			return hasChildNodes;
		}
		public String getNodePath(){
			Node parent = parentNode;			
			nodePath = "{"+getNodeValue()+"}"+getNodeName();
			
			if(parent !=null){
				do{
					nodePath += "-->"+parent.getNodeName();
					parent = parent.getParentNode();
				}while(parent != null);
			}
			
			return nodePath;
		}
		public void addNodeLink(FPTree.Node linkNode){			
			nodeLink = linkNode;						
		}
		public FPTree.Node getNodeLink(){return nodeLink;}
		
		private String 	nodeName;
		private int		nodeFreq;
		private Node 	nodeLink;
		private String nodePath;
	}
	public class NodeValueComparator implements Comparator<Node>{
		@Override    public int compare(Node o1, Node o2) {		
			return (o1.getNodeValue()>o2.getNodeValue() ? -1 : (o1.getNodeValue()==o2.getNodeValue() ? 0 : 1));    
			}
	}
	public PatternBasis createNewPatternBasis(String condItem){
		return new PatternBasis(condItem);
	}
	
	private void buildItemFrequencyMap(){
		// 3. Load Data	
		int dataItemNumber = 0;		
		
		Iterator<ContentItem> contentListIterator = contentList.iterator();
		while(contentListIterator.hasNext()){
			ContentItem contentItem =  contentListIterator.next();			
						
			String author = contentItem.getAuthor().replace(" ", ""); // Remove WhiteSpaces, Required for RelevanceMeasure Algorithm
			String content  = author + " " + contentItem.getContent().replaceAll("[^\\u0000-\\uFFFF]",""); // Replace non-alphanumeric characters
			String delim = " \t\n\r.,:;?؟`~!@#$%^&*+-=_/|{}()[]<>\"\'";
			String strWord;	
			Node newNode;	
			System.out.println("\t" + ++dataItemNumber + " Parsing Words for : \n " + (content.length() > 60 ? content.substring(0, 60) : content) + "..." );			
			StringTokenizer st = new StringTokenizer(content, delim); 
			while (st.hasMoreTokens()) {
				strWord = st.nextToken().toLowerCase().trim();	
				if (strWord.length() > 2){
					newNode = (Node) nodeFreqMap.get(strWord); 
					if (newNode == null) {
						newNode = new Node();
						newNode.setNodeName(strWord);
						newNode.setNodeValue(1);
						// Check if the newWord is among the uniqueQueryWords
//						if (uniqueQueryWordsMap.get(strWord) != null)
//							newWord.isQueryKeyWord = true;
						//newWord.addAuthor(author);
						nodeFreqMap.put(strWord, newNode); 						
					} else {						
						//newNode.addAuthor(author);
						newNode.incrementNodeValue();						
					}						
				}
			}						
		}
	}
	private void removeFilterWords(){
		// 2. Load words_2b_filtered
		String words_2b_filtered = "";
		try {
			InputStream in = HarVis.class.getResourceAsStream("Resources/word_2b_filtered");
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String line = br.readLine();
			words_2b_filtered += line;
			while(line != null){
				System.out.println(line);
				line = br.readLine();
				words_2b_filtered += line;
			}
		} catch (FileNotFoundException e) {			
			e.printStackTrace();
		} catch (IOException e) {			
			e.printStackTrace();
		}
		// 4. Remove words_2b_filtered from wordsMap
		StringTokenizer st = new StringTokenizer(words_2b_filtered, ","); 
		//uod_param.uodOutputTextArea.setText("Words before removal of filterwords.. " + wordsMap.entrySet().size());		
		while (st.hasMoreTokens()) {
			String strFilterWord = st.nextToken().toLowerCase();	
			Node wordObj = nodeFreqMap.get(strFilterWord);
			if (wordObj != null){  
				nodeFreqMap.remove(strFilterWord);		
			//	uod_param.uodOutputTextArea.append(strFilterWord + " removed as filter word \n");
			}
		}
		//uod_param.uodOutputTextArea.append("Words after removal of filterwords .. " + wordsMap.entrySet().size());
	}
	private void removeInFrequentWords(){
		// 5. Remove Word with less occurrence_count than this.wordCountLimit | this.coWordCountLimit
		Set<Map.Entry<String, Node>> wordsSet = nodeFreqMap.entrySet(); 
		Iterator<Map.Entry<String, Node>> wordsSetIter = wordsSet.iterator();
		System.out.println("Removing less frequent words than wordCountLimit ... ");
			
		int i = 1;
		while(wordsSetIter.hasNext()){		
			Node wordObj = wordsSetIter.next().getValue();			
			if(wordObj.getNodeValue() < this.freqThreshold){
				try{
					System.out.println("\n\t Removing " + i++ + " " + wordObj.getNodeName() + " WordCount " + wordObj.getNodeValue());					
				}catch(Exception e){
					e.printStackTrace();
				}
				nodeFreqMap.remove(wordObj.getNodeName());
				wordsSet = nodeFreqMap.entrySet(); 
				wordsSetIter = wordsSet.iterator();
			}
		}
		System.out.println("Words after removal of less frequent than limit ... " + nodeFreqMap.entrySet().size());		
	}
	private void sortItems(){
		Set<Map.Entry<String, Node>> wordsSet = nodeFreqMap.entrySet(); 
		Iterator<Map.Entry<String, Node>> wordsSetIter = wordsSet.iterator();		
		wordsSet = nodeFreqMap.entrySet(); 
		wordsSetIter = wordsSet.iterator();
		while(wordsSetIter.hasNext()){
			System.out.println("Sorting words wrt occurenceCount ... ");
			Node wordObj = wordsSetIter.next().getValue();
			uniqueNodesList.add(wordObj);
		}		
		nodeFreqMap.clear();
		nodeFreqMap = null;		
		Collections.sort(uniqueNodesList, new NodeValueComparator());		
	}
	private void reorderContentsList(){
		// Modify contentList to contain frequency ordered contents		
		Iterator<ContentItem> contentListIterator = contentList.iterator();		
		int itemCount = 1;
		System.out.println("Ordering content items ...  ("+ itemCount + " of "+ contentList.size() +") \n");
		while(contentListIterator.hasNext()){
			ContentItem contentItem =  contentListIterator.next();	
			String author = contentItem.getAuthor().replace(" ", ""); // Remove WhiteSpaces, Required for RelevanceMeasure Algorithm
			String content  = author.toLowerCase() + " " + contentItem.getContent().replaceAll("[^\\u0000-\\uFFFF]","").toLowerCase(); // Replace non-alphanumeric characters
			String frequencyOrderedContent = "";
			Iterator<Node> orderedWordListIterator = uniqueNodesList.iterator();
			while(orderedWordListIterator.hasNext()){
				Node nextMostFrequentNode = orderedWordListIterator.next();
				if(content.contains(nextMostFrequentNode.getNodeName())){
					frequencyOrderedContent += nextMostFrequentNode.getNodeName() + " ";
				}
			}
			System.out.println(itemCount++ + ". " +content +" --> is ordered as --> " + frequencyOrderedContent + "\n");			
//			if (uod_param.uodOutputTextArea.getLineCount()> 50){
//				uod_param.uodOutputTextArea.setText("Ordering content items ...  ("+ itemCount + " of "+ contentList.size() +") \n");
//			}
			
			if (frequencyOrderedContent.length() > 1)
				frequencyOrderedContentList.add(frequencyOrderedContent);
		}		
	}
	public void constructFPTree(){	
		buildItemFrequencyMap();		
		removeFilterWords();
		removeInFrequentWords();
		sortItems();				
		reorderContentsList();
		// Construct FPTree		
		FPTree.Node rootNode = getRootNode();	
		
		Iterator<String> iterator = frequencyOrderedContentList.iterator();
		int i = 1;
		while(iterator.hasNext()){
			String frequencyOrderedContent = iterator.next();
			System.out.println(i++ + ". " +frequencyOrderedContent);			
			// First string of the content
			FPTree.Node childNode = null;
			if (frequencyOrderedContent.contains(" ")){
				String firstWord = frequencyOrderedContent.substring(0, frequencyOrderedContent.indexOf(" "));
				childNode = getRootNode().getDirectChildNode(firstWord);
				if (childNode == null){
					childNode = rootNode.addChildNode(firstWord);		
					ArrayList<FPTree.Node> preExistingNodes = getRootNode().getGrandChildNode(firstWord);
					for(int k = 0; k < preExistingNodes.size()-1 ; k++){
							preExistingNodes.get(k).addNodeLink(preExistingNodes.get(k+1));							
					}
				}else{
					childNode.incrementNodeValue();
				}
			}
			// Rest of the strings
			frequencyOrderedContent = frequencyOrderedContent.substring(frequencyOrderedContent.indexOf(" ")+1, frequencyOrderedContent.length());
			if (frequencyOrderedContent.length() == 0)
				continue;
 			do {
 				String nextWord = frequencyOrderedContent.substring(0, frequencyOrderedContent.indexOf(" "));
				FPTree.Node childNode2 = childNode.getDirectChildNode(nextWord);
				if (childNode2 == null){
					if (nextWord.equalsIgnoreCase("c")) 
						System.out.println("C");
					childNode = childNode.addChildNode(nextWord);
					ArrayList<FPTree.Node> preExistingNodes = getRootNode().getGrandChildNode(nextWord);
					for(int k = 0; k < preExistingNodes.size()-1 ; k++){
							preExistingNodes.get(k).addNodeLink(preExistingNodes.get(k+1));							
					}					
				}else{
					childNode2.incrementNodeValue();
					childNode = childNode2;
				}						
				frequencyOrderedContent = frequencyOrderedContent.substring(frequencyOrderedContent.indexOf(" ")+1, frequencyOrderedContent.length());
			}while (frequencyOrderedContent.contains(" "));			
		}
		
	}	
	
	public HashMap<String, PatternBasis> constructCondPattBasis(){
		// For each unique node(item) in the itemsList... 
		for(Node node : uniqueNodesList){
			FPTree.PatternBasis pattBasis = createNewPatternBasis(node.getNodeName());
			ArrayList<Node> nodeList = getRootNode().getGrandChildNode(node.getNodeName());
			for(Node node_i : nodeList ){
				if (node_i.getParentNode().getNodeName().equalsIgnoreCase("$"))
					continue; // It takes at least two items to make a pattern
				pattBasis.addNewPattBaseBranch(node_i.getNodePath());				
			}			
			condPatternBasisMap.put(node.getNodeName(), pattBasis);
		}
		return condPatternBasisMap;
	}
	public HashMap<Node, ArrayList<Node>> constructCondFpTree(){
		ConditonalFPTree condFpTree = new ConditonalFPTree();
		return condFpTree.processCondPatternBasis();
	}
	private HashMap<String, PatternBasis> condPatternBasisMap = new HashMap<String, PatternBasis>();
	private HashMap<Node, ArrayList<Node>> condPatternsMap = new HashMap<Node, ArrayList<Node>>();
	class PatternBasis{
		public PatternBasis(String condItem){
			this.condItem = condItem;
		}
		private String condItem;
		public String getCondItem(){return condItem;}
		private ArrayList<String> pattBaseBranches = new ArrayList<String>();		
		public ArrayList<String> getPatternBaseBranches(){return pattBaseBranches;}		
		public void addNewPattBaseBranch(String pattBase){
			pattBaseBranches.add(pattBase);
		}		
	}
	class ConditonalFPTree{
		public ConditonalFPTree(){			
			
		}
		private HashMap<Node, ArrayList<Node>> processCondPatternBasis(){
			// For each unique node(item) in the itemsList... 
			for(Node node : uniqueNodesList){										
				ArrayList<Node> freqPatternItems = processPattBasis(condPatternBasisMap.get(node.getNodeName()));
				condPatternsMap.put(node, freqPatternItems);
			}
			return condPatternsMap;
		}
		private int processCondItemFreq(String pattBaseStr){			
			short endIndex = (short) pattBaseStr.indexOf("}");
			return Integer.parseInt(pattBaseStr.substring(1, endIndex));		
		}
		private ArrayList<Node> processPattBasis(PatternBasis pattBase){
			ArrayList<Node> freqPatternItems = new ArrayList<Node>();			
			if (pattBase.getPatternBaseBranches().size() == 0)
				return freqPatternItems;	
			StringTokenizer st;
			if (pattBase.getPatternBaseBranches().size() == 1){//Only one branch captures whole pattern
				String pattBaseStr = pattBase.getPatternBaseBranches().get(0);
				int condItemFreq = processCondItemFreq(pattBaseStr);
				pattBaseStr = pattBaseStr.substring(pattBaseStr.indexOf("}")+1);
				st = new StringTokenizer(pattBaseStr, "-->");
				while(st.hasMoreTokens()){
					String strWord = st.nextToken();
					if (strWord.equalsIgnoreCase("$") || strWord.equalsIgnoreCase(pattBase.getCondItem()))
						continue; // Ignore self & root
					Node newNode = new Node();
					newNode.setNodeName(strWord);
					newNode.setNodeValue(condItemFreq);
					freqPatternItems.add(newNode);
				}
			}else{//More than one branches capture the pattern
				int condItemFreq = 0;
				Map<String, Node> nodesMap = new HashMap<String, Node>();
				for(String pattBase_i: pattBase.getPatternBaseBranches()){
					condItemFreq = processCondItemFreq(pattBase_i);
					pattBase_i = pattBase_i.substring(pattBase_i.indexOf("}")+1);					
					st = new StringTokenizer(pattBase_i, "-->");
					Node newNode;
					while(st.hasMoreTokens()){
						String strWord = st.nextToken();
						if (strWord.equalsIgnoreCase("$") || strWord.equalsIgnoreCase(pattBase.getCondItem()))
							continue;  // Ignore self & root
						newNode = (Node) nodesMap.get(strWord); 
						if (newNode == null) {
							newNode = new Node();
							newNode.setNodeName(strWord);
							newNode.setNodeValue(condItemFreq);
							nodesMap.put(strWord, newNode); 						
						} else {							
							newNode.setNodeValue(newNode.getNodeValue()+condItemFreq);						
						}
					}
				}
				for(Node node: nodesMap.values()){
					if(node.getNodeValue() >= freqThreshold)
						freqPatternItems.add(node);
				}				
			}
			return freqPatternItems;
		}
	}
}
