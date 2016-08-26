package yt;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import yt.har.UoD_Param;
import yt.har.uTubeAuthor;
import yt.har.uTubeAuthorsCrawler;
import yt.har.uTubeDataManager;

public class D2I_GUI extends JPanel{
	Border blackline = BorderFactory.createLineBorder(Color.black);		
	JButton btnDiscoverUoD = new JButton("Discover UoD");
	JButton btnFilterIrrelevantVideos = new JButton("Filter Irrelevant");
	private final JTextField txtFieldCoOccurCountLimit = new JTextField(10);	
	private final JTextField txtFieldWordCountLimit = new JTextField(10);	
	private final JTextField txtFieldCoWordCountLimit = new JTextField(10);
	private final JTextField txtFieldUoDKeyWordFilter = new JTextField(10);	
	public JTextArea uodOutputTextArea;
	boolean srcTableNamesLoaded = false;
	boolean destTableNamesLoaded = false;	
	private uTubeDataManager uTubeDM;	
	public String srcTableName, destTableName, uodKeywordFilter;
	
	// Author Profiles Collecter
	private JButton btnCrawluTubeAuthors = new JButton("Crawl Authors !");
	
	public D2I_GUI(){		
		JPanel uodOutputPanel = new JPanel();
		uodOutputTextArea = new JTextArea(20, 50);
		JScrollPane uodOutputScrollPane = new JScrollPane(uodOutputTextArea);
		uodOutputPanel.add(uodOutputScrollPane);		
		btnDiscoverUoD.addActionListener(new ActionListener() {			
			public void actionPerformed(ActionEvent arg0) {
				System.out.println(srcTableName + " " + destTableName);
				if (srcTableName == null | srcTableName.equals("") | destTableName == null | destTableName.equals("")){
					JOptionPane.showMessageDialog(null, "Please select source/destination tables ! ");
					return;
				}
				if(uodKeywordFilter != null )
					uodKeywordFilter = txtFieldUoDKeyWordFilter.getText();
				int wordCountLmt = Integer.parseInt(txtFieldWordCountLimit.getText());
				int coWordCountLmt = Integer.parseInt(txtFieldCoWordCountLimit.getText());
				int coOccurCountLmt = Integer.parseInt(txtFieldCoOccurCountLimit.getText());
				UoD_Param uod_param = new UoD_Param(
						uodOutputTextArea,
						wordCountLmt, 
						coWordCountLmt, 
						coOccurCountLmt, 
						srcTableName, 
						destTableName, 
						uodKeywordFilter);
				uTubeDM = new uTubeDataManager(uod_param);
				new Thread(uTubeDM, "uTubeDataManager_DiscoveringUoD").start();								
			}
		});		
		btnFilterIrrelevantVideos.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				uTubeDataManager dataManager = new uTubeDataManager();
				dataManager.filterVideos();
			}
		});
		JPanel uodPanel = new JPanel();
		TitledBorder titleUodPanel = BorderFactory.createTitledBorder(
	            blackline, "Discover the UoD (Universe of Discourse)");
		titleUodPanel.setTitleJustification(TitledBorder.CENTER);		
		uodPanel.setBorder(titleUodPanel);		
		uodPanel.setLayout(new BorderLayout());
		uodPanel.add(createUoDControlPanel(), BorderLayout.NORTH);
		uodPanel.add(createSrcDestPanel(), BorderLayout.CENTER);
		uodPanel.add(uodOutputPanel, BorderLayout.SOUTH);
		
		
		//d2iPanel.setLayout(new GridLayout(1,1));
		
		JPanel authorsProfilePanel = new JPanel();
		authorsProfilePanel.setLayout(new GridLayout(2, 1));
		TitledBorder titleAuthorsProfileCollectionPanel = BorderFactory.createTitledBorder(
	            blackline, "Collect Authors Information");
		titleAuthorsProfileCollectionPanel.setTitleJustification(TitledBorder.CENTER);		
		authorsProfilePanel.setBorder(titleAuthorsProfileCollectionPanel);
		JPanel btnAuthorProfilePanel = new JPanel();
		btnAuthorProfilePanel.add(btnCrawluTubeAuthors);		
		authorsProfilePanel.add(btnAuthorProfilePanel);
		
		final JTextArea authorsOutputTextArea = new JTextArea(20, 50);
		JScrollPane authorsOutputScrollPane = new JScrollPane(authorsOutputTextArea);
				
		btnCrawluTubeAuthors.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				uTubeAuthorsCrawler authorsCrawler = new uTubeAuthorsCrawler(authorsOutputTextArea);
				authorsCrawler.start();
			}
		});
		JPanel authorsProfileOutputPanel = new JPanel();
		authorsProfileOutputPanel.add(authorsOutputScrollPane);
		authorsProfilePanel.add(authorsProfileOutputPanel);
		
		add(uodPanel);
		add(authorsProfilePanel);
		add(createFilterPanel());
	}	
	private JPanel createSrcDestPanel(){
		JPanel uodDataSrcDestPanel = new JPanel();		
		TitledBorder titleDataSrcDestpanel = BorderFactory.createTitledBorder(
	            blackline, "Content Source/Destination");
		titleDataSrcDestpanel.setTitleJustification(TitledBorder.LEFT);
				
//		Vector<String> tableNamesVector = new Vector<String>();
		String []strSrcTableNames = new String[]{"utubevideos","utubevideocomments"};
		final JComboBox<String> srcTableNamesCombo = new JComboBox<String>(strSrcTableNames);		
		srcTableName = "utubevideos";
		srcTableNamesCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {				
				@SuppressWarnings("unchecked")
				JComboBox<String> source = (JComboBox<String>)event.getSource();
				srcTableName = source.getSelectedItem().toString();
			}
		});		
//		srcTableNamesCombo.addMouseListener(new MouseAdapter() {
////			public void mouseEntered(MouseEvent event) {				
////				if (srcTableNamesLoaded == false){
////					Vector<String> tablesNamesVector = uTubeDM.getTableNames("%");
////					@SuppressWarnings("unchecked")
////					JComboBox<String> source = (JComboBox<String>)event.getSource();
////					for(int i = 0 ; i < tablesNamesVector.size() ; i ++){
////						source.addItem(tablesNamesVector.get(i));					
////					}
////					srcTableNamesLoaded = true;
////				}
////		    }
//		});
		uodDataSrcDestPanel.add(new JLabel("Select Source Table"));		
		uodDataSrcDestPanel.add(srcTableNamesCombo);		
		Vector<String> destTableNamesVector = new Vector<String>();
		final JComboBox<String> destTableNamesCombo = new JComboBox<String>(destTableNamesVector);
		destTableNamesCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {				
				@SuppressWarnings("unchecked")
				JComboBox<String> source = (JComboBox<String>)event.getSource();
				destTableName = source.getSelectedItem().toString();
			}
		});		
		destTableNamesCombo.addMouseListener(new MouseAdapter() {
			public void mouseEntered(MouseEvent event) {				
				if (destTableNamesLoaded == false){
					Vector<String> tablesNamesVector = uTubeDataManager.getTableNames("uod%");
					@SuppressWarnings("unchecked")
					JComboBox<String> source = (JComboBox<String>)event.getSource();
					for(int i = 0 ; i < tablesNamesVector.size() ; i ++){
						source.addItem(tablesNamesVector.get(i));					
					}
					destTableNamesLoaded = true;
				}
		    }
		});				
		uodDataSrcDestPanel.add(new JLabel("Select Destination Table"));
		uodDataSrcDestPanel.add(destTableNamesCombo);
		
		JButton btnCreateNewUoD_Table = new JButton("Create New");
		btnCreateNewUoD_Table.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String tableName = JOptionPane.showInputDialog("Input new table name.");				
				String result = uTubeDataManager.createUoD_Table(tableName);
				JOptionPane.showMessageDialog(null, result);		
				destTableNamesLoaded = false;
			}
		});
		uodDataSrcDestPanel.add(btnCreateNewUoD_Table);
		uodDataSrcDestPanel.setBorder(titleDataSrcDestpanel);
		return uodDataSrcDestPanel;
	}
	private JPanel createFilterPanel(){
		JPanel panel = new JPanel();
		panel.add(btnFilterIrrelevantVideos);
		return panel;
	}
	private JPanel createUoDControlPanel(){
		JPanel uodControlPanel = new JPanel();
		uodControlPanel.setLayout(new GridLayout(2, 2));
		
		txtFieldCoOccurCountLimit.setText("10");
		txtFieldWordCountLimit.setText("10");
		txtFieldCoWordCountLimit.setText("10");
		
		uodControlPanel.setLayout(new GridLayout(5, 2));
		uodControlPanel.add(new JLabel("Word Count Limit"));
		uodControlPanel.add(txtFieldWordCountLimit);
		uodControlPanel.add(new JLabel("CoWord Count Limit"));
		uodControlPanel.add(txtFieldCoWordCountLimit);
		uodControlPanel.add(new JLabel("CoOccurrence Count Limit"));
		uodControlPanel.add(txtFieldCoOccurCountLimit);		
		uodControlPanel.add(txtFieldCoOccurCountLimit);
		JCheckBox chkBoxKeyWordFilter = new JCheckBox("UoD Filter Required?");
		chkBoxKeyWordFilter.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				JCheckBox chkBox = (JCheckBox) e.getSource();
				if(chkBox.isSelected()){
					txtFieldUoDKeyWordFilter.setEnabled(true);	
					uodKeywordFilter = "";
				}else{
					txtFieldUoDKeyWordFilter.setEnabled(false);
					uodKeywordFilter = null;
				}				
			}
		});
		uodControlPanel.add(chkBoxKeyWordFilter);
		uodControlPanel.add(txtFieldUoDKeyWordFilter);
		txtFieldUoDKeyWordFilter.setText("Enter Keyword here.");
		txtFieldUoDKeyWordFilter.setEnabled(false);
		
		uodControlPanel.add(new JLabel("Ensure Input Parameters"));
		uodControlPanel.add(btnDiscoverUoD);		
		TitledBorder titleUodControlPanel = BorderFactory.createTitledBorder(
	            blackline, "Content Source/Destination");
		titleUodControlPanel.setTitleJustification(TitledBorder.LEFT);
		uodControlPanel.setBorder(titleUodControlPanel);
		return uodControlPanel;
	}
}