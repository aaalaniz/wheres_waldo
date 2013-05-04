import javax.imageio.ImageIO;  
import java.awt.image.BufferedImage;  
import java.io.*; 
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;


public class ServerCoordinator {
	//Data members
	String mLocalImgPath;
	int mImgWd, mImgHt, mTmpWd, mTmpHt, mVStride, mHStride, mVSections, mHSections,mNumJobItemsRem, 
		mWorkersAvailable,mCurrentJobIdx;
	ServerImageTemplate mSit;
	ImageSenderThread mImgS;
	static Thread mThImageSender, mThScheduler;
	static ServerCommTX mSCT;
	ArrayList<JobItem> mJIList; // List to track status of job-items
	
			
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
	public ServerCoordinator(ServerCommTX inSCT, ServerImageTemplate inSit){
		mSCT = inSCT;
		mSit = inSit;
		mSC = ServersConfig.getConfig();
		mWorkersAvailable = 0;
		mNumJobItemsRem = 0;
		mLocalImgPath = "";
				
		mImgS = new ImageSenderThread();
		mThImageSender = new Thread(mImgS);
		mThScheduler = new Thread(new JobScheduler(this));
						
		threadMessage("Server Coordinator instantiated");
	}
	
    static void threadMessage(String message)
    {
        String threadName =
            Thread.currentThread().getName();
        System.out.format("%s: %s%n",
                          threadName,
                          message);
    }
	
	//ProcessJob
	//Called by ServerClientComm on receiving a request from the client
	public synchronized void ProcessJob(String inFilePath){
		
		mLocalImgPath = inFilePath;
		
		threadMessage("ServerCoordinator Process Job: " +mLocalImgPath);
		
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
		//\todo should be set at run time from image dimensions
		mHStride = 10;
		mVStride = 10;
		mHSections = (mImgWd/mHStride) - (mSit.getTmpWd()/mHStride + 1);
		mVSections = (mImgHt/mVStride) - (mSit.getTmpHt()/mVStride + 1);
				
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
		mCurrentJobIdx = 0;
			
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
		mImgS.setFilePath(mLocalImgPath);
		mThImageSender.start();
						
		//Run scheduler thread
		mThScheduler.start();
	}
	
			
	//Starts a job on a worker.
	//Called by the scheduler thread
	public synchronized void JobStart(){
		Boolean found=false;
		
		//Find free worker
		int n = 0, wid;
		while(n<mSC.mNumServers && !found){
			if(mWList.get(n).equals("DONE") || mWList.get(n).equals("IMAGE_SENT")){
				found = true;
			}
			n++;
		}
		
		wid = n-1;
		
		//setWorkerStatus to busy		
		mWList.set(wid, "BUSY");
		
		//Get next job
		JobItem ji = mJIList.get(mCurrentJobIdx);
		
		//send message "job_start" with coordinates to worker server
		String msg = ji.x  + " " + ji.y;
		mSCT.sendMsg(wid, "job_start", msg);
		
		//Reduce number of workers available
		mWorkersAvailable--;
		
		//Increment job id
		mCurrentJobIdx++;
		
		notify();
	}
		
	//Job Done Receive
	//Called by ServerServerComm processMSG
	public synchronized void JobDone(int inJobID, int inWID, int inFeatMatched){		
		//setWorkerStatus to done
		mWList.set(inWID, "DONE");
		
		//update job item
		mJIList.get(inJobID).mFeatMatched = inFeatMatched;
		mJIList.get(inJobID).mStatus = "P"; 
		
		//Decrement number of mNumJobItemsRem
		mNumJobItemsRem--;	
		
		//Increment number of workers available
		mWorkersAvailable++;
		
		notify();
	}

	//Image receive ack
	//Called by ServerServerComm processMSG
	public synchronized void ImageRcvAck(int inWID){
		//Set worker status to init
		mWList.set(inWID, "IMAGE_SENT");
			
		mWorkersAvailable++;
		
		notify();
	}
	
	public synchronized int getNumWorkersAvailable(){
		return mWorkersAvailable;		
	}
	
	public synchronized int getNumJobItemsRem(){
		return mNumJobItemsRem;		
	}
	
	private static class ImageSender{
		String mFilePath;
		
		public ImageSender(){}
		
        public void setFilePath(String inFilePath){
        	mFilePath = inFilePath;
        }
		
	}
	
	//Image sender thread
	//Separate thread so that jobs on some worker threads can get started before
	//images are transferred to all the workers
    private static class ImageSenderThread extends ImageSender implements Runnable
    {
    	
    	public ImageSenderThread()
        {    		
        }
    	
        public void run()
        {            
        	for(int i=0;i<mWList.size() ;i++){
        		if(mWList.get(i) != null){
        			if(mWList.get(i).equals("UNINIT")){
        				mSCT.sendFile(i,mFilePath);
        			}
        		}
        	}
        }
        

    }
	
						
	//Scheduler thread
    private static class JobScheduler implements Runnable
    {    	
    	ServerCoordinator mSCoord;
    	public JobScheduler(ServerCoordinator inSCoord)    	
        {    		
    		mSCoord = inSCoord;
        }
    	
    	//This keeps on running until there are job items to process.
        public void run()
        {            
    		while(mSCoord.mNumJobItemsRem > 0){
    			if(mSCoord.getNumWorkersAvailable() > 1){
    				mSCoord.JobStart();
    			}    				
    		}
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


