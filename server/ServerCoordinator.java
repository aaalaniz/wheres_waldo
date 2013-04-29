import javax.imageio.ImageIO;  

import java.awt.image.BufferedImage;  
import java.io.*; 
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;


public class ServerCoordinator {
	//Data members
	String mLocalImgPath;
	int mImgWd, mImgHt, mTmpWd, mTmpHt, mVStride, mHStride, mVSections, mHSections,mNumJobItemsRem;
	static Thread mThImageSender, mThScheduler;
	static ServerServerComm mSSC;
	ArrayList<JobItem> mJIList; // List to track status of job-items
	Boolean mWorkerAvailable;
		
	
	// List to track status of each worker
	//Worker status can be 
	//SELF(It's coordinator itself)
	//UNINIT(Worker hasn't received image yet)
	//IMAGE_SENT(Worker has received image)
	//BUSY(Worker is still processing the last request sent by the coordinator)
	//DONE(Worker is done processing the last request)
	static ArrayList<String> mWList; 
	
	static ServersConfig mSC;
	
	//Constructor	
	//Instantiated on the server start
	public ServerCoordinator(ServerServerComm inSSC){
		mSSC = inSSC;
		mSC = ServersConfig.getConfig();
		mWorkerAvailable = false;
		mNumJobItemsRem = 0;
		mLocalImgPath = "";
				
		mThImageSender = new Thread(new ImageSender(mLocalImgPath));
	}
	
	//ProcessJob
	//Called by ServerClientComm on receiving a request from the client
	public void ProcessJob(){
		
		//Determine dimension of the image	
		File file = new File(mLocalImgPath); 
        FileInputStream fis;
        BufferedImage image;
		try {
			fis = new FileInputStream(file);
			image = ImageIO.read(fis);
			mImgWd = image.getWidth();
			mImgHt = image.getHeight();
		} catch (FileNotFoundException e) {			
			e.printStackTrace();
		} catch (IOException e) {			
			e.printStackTrace();
		}
		
		//Determine how many sub-sections of image we need to process
		//This is equal to number of job items		
		mHSections = (mImgWd/mHStride) - (mTmpWd/mHStride + 1);
		mVSections = (mImgHt/mVStride) - (mTmpHt/mVStride + 1);
				
		//Create job queue data structure			
		mJIList = new ArrayList<JobItem>();
		int xcoord =0, ycoord = 0;
		for(int rows=0; rows<mVSections; rows++){
			for(int cols=0; cols<mHSections; cols++){
				JobItem ji = new JobItem(xcoord,ycoord);
				mJIList.add(ji);
				
				xcoord += mHStride; //Get next section to the right
			}
			xcoord = 0; //Move back to the left again
			ycoord += mVStride;//Move one down
		}
		mNumJobItemsRem = mVSections*mHSections;
			
		//Create worker status data structure		
		mWList = new ArrayList<String>();
		for(int n=0;n<mSC.mNumServers;n++){
			if(n != mSC.mMyID){
				mWList.add("UNINIT");
			}else{
				mWList.add("SELF");
			}
		}
	
		//Run Image sender thread
		mThImageSender.start();
						
		//Run scheduler thread
	}
	
	
	//Starts a job on a worker.
	//Called by the scheduler thread
	public void JobStart(int inWID, int inXCoord, int inYCoord){
		//setWorkerStatus to busy		
		mWList.set(inWID, "BUSY");
		
		//send message "job_start" with coordinates to worker server
		String msg = inXCoord + " " + inYCoord;
		mSSC.sendMsg(inWID, "job_start", msg);
	}
		
	//Job Done Receive
	//Called by ServerServerComm processMSG
	public void JobDone(int inJobID, int inWID, int inFeatMatched){		
		//setWorkerStatus to done
		mWList.set(inWID, "DONE");
		
		//set number of features matched
		mJIList.get(inJobID).mFeatMatched = inFeatMatched;
		
		//Decrement number of mNumJobItemsRem
		mNumJobItemsRem--;	
		
		//Determine worker status
		//\todo
		//One of the listener threads will call this, and need to figure out the syntax of 
		//how the scheduler thread will get notified
		//updateWorkerStatus();
	}

	//Image receive ack
	//Called by ServerServerComm processMSG
	public void ImageRcvAck(int inWID){
		//Set worker status to init
		mWList.set(inWID, "IMAGE_SENT");
		
		//Determine worker status
		
	}
	
	
	//Image sender thread
	//Separate thread so that jobs on some worker threads can get started before
	//images are transferred to all the workers
    private static class ImageSender implements Runnable
    {
    	String mFilePath;
    	public ImageSender(String inFilePath)
        {
    		mFilePath = inFilePath;
        }
    	
        public void run()
        {            
        	for(int i=0;i<mWList.size() ;i++){
        		if(mWList.get(i) != null){
        			if(mWList.get(i).equals("UNINIT")){
        				mSSC.sendFile(i,mFilePath);
        			}
        		}
        	}
        }
    }
	
						
	//Scheduler thread
    private static class JobScheduler implements Runnable
    {    	
    	public JobScheduler()
        {    		
        }
    	
    	//This keeps on running until their are job items to process.
        public void run()
        {            
    		/*while(mNumJobItemsRem){
    			//if(worker_available)
    				//start_job();
    		}*/
        }
    }

		
	//Job-item class
	 private class JobItem{
		//Data members
		public int mWID; //Worker server to which this item is assigned to.
		public int mFeatMatched; //Number of features matched
		public int x, y; //X and Y coordinates of sub-section of the image.
		
		//Status of the job-item. Values: NP(notProcessed), P(processing), or D(done)
		public String mStatus; 
		
		public JobItem(int inX, int inY){
			mFeatMatched = 0;
			mWID = -1;
			mStatus = "NP";
		}
		
	}
	
				
}


