import java.io.IOException;
import java.util.Random;



public class ServerWorker {
	//Data members
	String mFilePath;
	int mXCoord, mYCoord, mJobItemID;
	int mcoordID;
	static ServerCommTX mSST;
	
	
	//Constructor
	public ServerWorker(ServerCommTX inSST){
		mSST = inSST;
		threadMessage("ServerWorker instantiated");
	}
	
    static void threadMessage(String message)
    {
        String threadName =
            Thread.currentThread().getName();
        System.out.format("%s: %s%n",
                          threadName,
                          message);
    }
	
	//Initialize new job
	//Called by SSC.processMsg when "job_init" message is received
	public void InitJob(int inCoordID){
		mcoordID = inCoordID;
		mXCoord = 0;
		mYCoord = 0;
		mJobItemID = 0;
		mFilePath = "";
		
		threadMessage("ServerWorker JobInit");
	}
	
	//Image received
	//Called by ServerCommRX.processMsg when "image_transfer_done" message is received
	//This sets the local file path, and also sends an image received ack back to the coordinator
	//which will tell the coordinator that it's ready
	public void ImageReceived(String inFilePath){
		mFilePath = inFilePath;
		threadMessage("ServerWorker ImageReceived " + inFilePath);
		try {
			String msg = "image_received_ack#" + " ";
			mSST.sendMsg(mcoordID, msg);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//ProcessImage
	//Called by SSC.processMsg when "job_start" message is received
	//XCoord, YCoord, and JobItemID are sent by the coordinator along with the "job_start"
	public void StartJob(int inXCoord, int inYCoord, int inJobItemID) throws InterruptedException{
		mJobItemID = inJobItemID;
		mXCoord = inXCoord;
		mYCoord = inYCoord; 
		
		threadMessage("ServerWorker StartJob. JobItemID:" + mJobItemID +"X: " + mXCoord + "Y: "  +mYCoord);
		
	}
	
	//Done
	//Called when image processing is complete
	//This sends "job_done" message with JobItemID and Features Matched back to the coordinator
	public void Done(int inJobItemID, int inFeatMatched){
		//Send a message back to coordinator with number of features matched
		String msg = "job_done#" +inJobItemID + " " + inFeatMatched;
		threadMessage("ServerWorker JobDone. JobItemID:" + mJobItemID + "FeaturesMatched: " + inFeatMatched);
		try {
			mSST.sendMsg(mcoordID,msg);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
		
}
