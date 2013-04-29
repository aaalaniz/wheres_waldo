
public class ServerWorker {
	//Data members
	String mFilePath;
	int mXCoord, mYCoord, mJobItemID;
	int mcoordID;
	static ServerServerComm mSSC;
	
	
	//Constructor
	public ServerWorker(ServerServerComm inSSC){
		mSSC = inSSC;
	}
	
	//Initialize new job
	//Called by SSC.processMsg when "job_init" message is received
	public void InitJob(int inCoordID){
		mcoordID = inCoordID;
		mXCoord = 0;
		mYCoord = 0;
		mJobItemID = 0;
		mFilePath = "";
	}
	
	//Image received
	//Called by SSC.processMsg when "image_transfer_done" message is received
	//This sets the local file path, and also sends an image received ack back to the coordinator
	public void ImageReceived(String inFilePath){
		mFilePath = inFilePath;
		mSSC.sendMsg(mcoordID, "image_received_ack","");
	}
	
	//ProcessImage
	//Called by SSC.processMsg when "job_start" message is received
	//XCoord, YCoord, and JobItemID are sent by the coordinator along with the "job_start"
	public void StartJob(int inXCoord, int inYCoord, int inJobItemID){
		mJobItemID = inJobItemID;
		mXCoord = inXCoord;
		mYCoord = inYCoord; 
		
		//\todo call image processing code here
	}
	
	//Done
	//Called when image processing is complete
	//This sends "job_done" message with JobItemID and Features Matched back to the coordinator
	public void Done(int inJobItemID, int inFeatMatched){
		//Send a message back to coordinator with number of features matched
		String msg = inJobItemID + " " + inFeatMatched;
		mSSC.sendMsg(mcoordID, "job_done", msg);		
	}
		
}
