import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.StringTokenizer;


public class ServerCommRX {
	
	//Data Members
    int mId;    
    ServersConfig mSC;   
	static ServerSocket mTCPSocket;
	String mLocalPath;
	DatagramSocket mUDPSocket;
	static Thread mThreadRXTCP, mThreadRXUDP;
    static ServerWorker mSW;
    ServerCoordinator mSCoord;
    static public Boolean mIsWorker;
    static ServerCommTX mSCT;
    
	//Constructor	
	public ServerCommRX( ServerWorker inSW,ServerCoordinator inCoord, DatagramSocket inUDPSocket, 
			ServerCommTX inSCT) throws IOException{
		mSC = ServersConfig.getConfig();
		mId = mSC.mMyID;		
		mSW = inSW;
		mSCoord = inCoord;
		mUDPSocket = inUDPSocket;
		mIsWorker = false;
		mSCT = inSCT;
	    
		threadMessage("ServerCommRX created mID: "+ mId);
		File homeDir = new File(System.getProperty("user.home"));
		File dir = new File(homeDir,"//WaldoFiles//Worker//" + "_" + mSC.getMyID());
		if (!dir.exists() && !dir.mkdirs()) {
			throw new IOException("Unable to create " + dir.getAbsolutePath());
		}	
		else{
			mLocalPath = dir.getPath();
		}		
		
		//Create and start TCP thread. This is for file transmission
		mThreadRXTCP = new Thread(new TCPListener(mLocalPath,mSC.mMySSTCPPort,mSW));
		mThreadRXTCP.start();
		
		
		//Start a UDP to thread listen. This is for messages
		mThreadRXUDP = new Thread(new UDPListener(mUDPSocket, this));
		mThreadRXUDP.start();
		
		threadMessage("ServerCommConnect: Inited");
	}
	
    static void threadMessage(String message)
    {
    	/*
        String threadName =
            Thread.currentThread().getName();
        System.out.format("%s: %s%n",
                          threadName,
                          message);
                          */
    }
	
    //Receive Msg
    public ServerMsg receiveMsg(String inStr) throws IOException  {
        StringTokenizer st = new StringTokenizer(inStr);
        int srcId = Integer.parseInt(st.nextToken());
        int destId = Integer.parseInt(st.nextToken());
        String tag = st.nextToken("#");
        String msg = st.nextToken();
        
        threadMessage("ServerCommRX received message " + srcId + " " + destId + " " + tag + " " + msg);
        return new ServerMsg(srcId, destId, tag, msg);
    }
    
    //Process Msg
    public void processMsg(ServerMsg m) throws IOException{
    	String tag = m.getTag();
    	tag = tag.trim();
    	
    	threadMessage("Processing Msg " + m.srcID + " " + m.destID + " " + m.tag + " " + m.getMessage());
    	
    	//Messages received as worker
    	if(tag.equals("job_init")){
    		//set server worker object's coordinator ID field
    		mIsWorker = true;
    		mSW.InitJob(m.srcID);
    	}    	
    	else if(tag.equals("job_start")){    		
    		String payload = m.getMessage();
            StringTokenizer st = new StringTokenizer(payload);
            int XCoord = Integer.parseInt(st.nextToken());
            int YCoord = Integer.parseInt(st.nextToken());
            int JobItemID = Integer.parseInt(st.nextToken());
    		
    		try {
				mSW.StartJob(XCoord, YCoord, JobItemID);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	else if(tag.equals("job_stop")){
    		//call mSW.stopProcessing
    		//\todo add a message queue in ServerWorker
    	}
    	else if(tag.equals("job_uninit")){
    		mIsWorker=false;
    	}
    	
    	//Message received as coordinator
    	else if(tag.equals("job_done")){
    		String payload = m.getMessage();
            StringTokenizer st = new StringTokenizer(payload);
            int jobid = Integer.parseInt(st.nextToken());            
            int featMatched = Integer.parseInt(st.nextToken());
            
            int wid = m.getSrcId();
    		mSCoord.JobDone(jobid, wid, featMatched,false);
    		
    	}
    	else if(tag.equals("image_received_ack")){
    		//add to coordinator queue
    		int wid = m.getSrcId();
    		mSCoord.ImageRcvAck(wid);
    	}
    	else if(tag.equals("timeout")){
    		//add to coordinator queue
    		int wid = m.getSrcId();
    		mSCoord.JobDone(0, wid, 0,true);
    	}
    }
    
    
    //Thread to listen on TCP
    private static class TCPListener implements Runnable
    {
        String mLocalPath;
        int mPort;
        ServerWorker mSW;
    	public TCPListener(String inLocalPath, int inPort, ServerWorker inSW){
    		mLocalPath = inLocalPath;
    		mPort = inPort;
    		mSW = inSW;
    	}
    	
    	public void run()
        {
            threadMessage("ServerCommConnect: Starting TCP Listener");

            try
            {
            	mTCPSocket= new ServerSocket(mPort);

                while (true)
                {
                    Socket s = mTCPSocket.accept();
                    threadMessage("Request from client. Spawning thread");
                    Thread t = new Thread(new TCPProcessRequest(s, mLocalPath,mSW));
                    t.start();
                }
            }
            catch (IOException e)
            {
                System.err.println(e);
                System.exit(-1);
            }
      
        }
    }
    

    //Class to handle individual TCP requests
    //There will be multiple from different servers
    public static class TCPProcessRequest implements Runnable
    {
        private Socket mS;
        private InputStream mInputStream;
        private OutputStream mOutputStream;
        String mLocalPath;
        ServerWorker mSW;
        public TCPProcessRequest(Socket inS, String inLocalPath,ServerWorker inSW)
        {
            try
            {
                mS = inS;
                mInputStream  = mS.getInputStream();
                mOutputStream = mS.getOutputStream();
                mLocalPath = inLocalPath;
                mSW = inSW;
            }
            catch (IOException e)
            {
                System.err.println(e);
                System.exit(-1);
            }
        }
        public void run()
        {
            try
            {
            	//Get file name and size
            	//Msg format: srcID filename filesize
            	byte[] buf = new byte[1024];
            	java.util.Arrays.fill(buf, (byte) 0);
            	mInputStream.read(buf);
            	String str = new String(buf);
                StringTokenizer st = new StringTokenizer(str);
                int srcId = Integer.parseInt(st.nextToken());	               
                String fname = st.nextToken();
                int fsize = Integer.parseInt(st.nextToken());
                	               
				//Open file
                String filePath = mLocalPath + "/"+fname;
                
                //Start receiving file
                byte[] filebuffer = new byte[50000];
                try {           
                    FileOutputStream fos = new FileOutputStream(filePath);

                    int count = fsize/50000;
                    while (count>0) {
                    	mInputStream.read(filebuffer);
                        fos.write(filebuffer);
                        count--;
                    }
                    byte[] lastPart = new byte[fsize%50000];
                    mInputStream.read(lastPart);
                    fos.write(lastPart);
                    
                    threadMessage("ServerCommRX file receive complete srcID: " + srcId + " Name:  " +  fname);
                              
                    //Signal that image was received
                    mSW.ImageReceived(filePath);
                    
                    
                }catch (IOException e) {
        			// TODO Auto-generated catch block
                	System.err.println(e);
        			e.printStackTrace();
        		}
                
                mS.close();
             
            }
            catch (SocketException se)
            {
                System.err.println(se);
            }
            catch (IOException e)
            {
                System.err.println(e);
            }

            threadMessage("ServerCommRX: TCP Connection Closed with hostname " + mS.getInetAddress().getHostName()
                               + "(" +  mS.getInetAddress().getHostAddress() + ")");
        }

    }
    
    //Thread to listen on UDP
    private static class UDPListener implements Runnable
    {	       
    	DatagramSocket mDatagramSocket;
    	ServerCommRX mSCR;
    	public UDPListener(DatagramSocket inDatagramSocket, ServerCommRX inSCR){
    		mDatagramSocket = inDatagramSocket;
    		mSCR = inSCR;
    	}
    	
    	public void run()
        {
            threadMessage("ServerCommConnect: Starting TCP Listener");

            try
            {
            	byte[] buf = new byte[2048];
            	DatagramPacket dp;

                while (true)
                {	                		                	
                    //clear out buf each time
                    java.util.Arrays.fill(buf, (byte) 0);
                    if(ServerCommRX.mIsWorker){                 
                    	//Receive packet
                    	threadMessage("UDPListener: Waiting to receive message");
                    	dp = new DatagramPacket(buf, buf.length);
                    	try{
                    		mDatagramSocket.setSoTimeout(5000);                    	                    	
                    		mDatagramSocket.receive(dp);
                    	} catch(SocketTimeoutException e){
                    		String msg = "timeout#" + " ";
                    		mSCT.sendMsg(mSW.mcoordID, msg);
                    	}
                    	
                    }
                    else{
                    	//Receive packet
                    	threadMessage("UDPListener: Waiting to receive message");
                    	dp = new DatagramPacket(buf, buf.length);
                    	mDatagramSocket.receive(dp);
                    }

                    String messageIn = new String(dp.getData(), dp.getOffset(), dp.getLength());
                    ServerMsg m = mSCR.receiveMsg(messageIn);
                    mSCR.processMsg(m);
                }
            }
            catch (IOException e)
            {
                System.err.println(e);
                System.exit(-1);
            }
      
        }
    }
        
}
