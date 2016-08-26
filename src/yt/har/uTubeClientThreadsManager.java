package yt.har;


import java.util.HashMap;
import java.util.Map;
import java.util.Set;


import yt.HarVis;


public class uTubeClientThreadsManager {
	//protected Vector<uTubeVideoData> videoDataVector = new Vector<uTubeVideoData>(10, 10);
	Map<String, uTubeVideoData> videoDataMap;
	
	int dataSize;
	protected boolean dataReceived = false;
	protected boolean dataSaved = false;
	protected HarVis harvis;
	// uTubeCampaignManager campaignManager;
	//protected uTubeDataManager uTubeDM;
	public uTubeClientThreadsManager(HarVis harvis) {
		this.harvis = harvis;		
		/// this.campaignManager = harvis.campaignManager;
		//copyOfVideoDataMap = new HashMap<String, uTubeVideoData>();
	}
	synchronized void dataSavedSuccessfull(){
		harvis.textAreaTM.append("\n\n\t ThreadManager.dataSavedSuccessfull(): uTubeDM saved data ... notifying Crawler to continue ...");
		dataSaved = true;
		notify();
	}
	// Will be called by uTubeDataManager
	synchronized HashMap<String, uTubeVideoData> getDataReceivedFromCrawler(){
		//uTubeClient.textArea.append("\n\n\t ThreadManager: uTubeDM requesting data");
		if(  dataReceived == false  ){
			try{
				harvis.textAreaTM.append("\n\n\t ThreadManager.getDataReceivedFromCrawler(): Data not received yet, putting uTubeDM into wait ");
				wait();
			}catch(InterruptedException e){
				e.printStackTrace();
			}
		}			
		HashMap<String, uTubeVideoData> copyOfVideoDataMap = new HashMap<String, uTubeVideoData>();;
		copyOfVideoDataMap.putAll(this.videoDataMap);
		harvis.campaignManager.uTubeDM.setVideoDataMap(copyOfVideoDataMap);
		harvis.textAreaTM.append("\n\n\t ThreadManager.getDataReceivedFromCrawler(): Data sent to uTubeDM, notifying crawler ...");
		dataReceived = false;		
		notify();
		if (harvis.textAreaTM.getLineCount() > 500)
			harvis.textAreaTM.setText("");
		return copyOfVideoDataMap;
	}
	
// Will be called by uTubeCrawler
	synchronized void setVideoData(Map<String, uTubeVideoData> videoDataMap ){
		//uTubeClient.textArea.append("\n\n\t ThreadManager: uTubeCrawler has sent data to ThreadManager for uTubeDM");
		if( (dataReceived == true) ){
//			if (dataSaved == false){
				try{
					harvis.textAreaTM.append("\n\n ThreadManager.setVideoData(): uTubeDM is busy receiving data, putting uTubeCrawler into wait.");
					wait();
				}catch(InterruptedException e){
					e.printStackTrace();
				}
//			}
		}
		this.videoDataMap = videoDataMap;
		Set dataSet = videoDataMap.entrySet();		
		harvis.textAreaTM.append("\n\n ThreadManager.setVideoData(): New data of "+ dataSet.size() +  " videos received From uTubeCrawler, Notifying uTubeDM.");		
		notify();		
		dataReceived = true;		
		dataSaved = false;

		if(dataSaved == false){
			try{
				harvis.textAreaTM.append("\n\n ThreadManager.setVideoData(): uTubeDM is busy saving data, putting uTubeCrawler into wait.");
				wait();
			}catch(InterruptedException e){
				e.printStackTrace();
			}
		}
		if (harvis.textAreaTM.getLineCount() > 500)
			harvis.textAreaTM.setText("");
	}	
}
