
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.Arrays.*;


//Class for communication between servers
public class ServerServerComm{
	//Data Members
    PrintWriter[] mDataOut;
    BufferedReader[] mDataIn;  
    OutputStream[] mSocketOut;
    InputStream[] mSocketIn;
    int myId, N;
    Connector mConnector;
    ServersConfig mSC;
    static Thread[] mListenerThreads;
    
	//Constructor	
	public ServerServerComm(){
		mSC = ServersConfig.getConfig();
		myId = mSC.mMyID;
		N = mSC.mNumServers;
		mDataIn = new BufferedReader[N];
		mDataOut = new PrintWriter[N];           
		mConnector = new Connector();		
		try {
			mConnector.Connect(myId, N, mDataIn, mDataOut,mSC.mMySSPort);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//Create listener threads on each channel
		mListenerThreads = new Thread[N];
		for(int n=0;n<mSC.mNumServers;n++){
			mListenerThreads[n] = new Thread(new ListenerThread(n));
		}
		
	}

	//Send Msg
    public void sendMsg(int destId, String tag, String msg) {       
    	mDataOut[destId].println(myId + " " + destId + " " + tag + " " + msg + "#");
    	mDataOut[destId].flush();
    }
    
    //Receive Msg
    public ServerMsg receiveMsg(int fromId) throws IOException  {
        String getline = mDataIn[fromId].readLine();      
        StringTokenizer st = new StringTokenizer(getline);
        int srcId = Integer.parseInt(st.nextToken());
        int destId = Integer.parseInt(st.nextToken());
        String tag = st.nextToken();
        String msg = st.nextToken("#");
        return new ServerMsg(srcId, destId, tag, msg);
    }

    //Send File
    public void sendFile(int inDestID, String inFilePath){
    	byte[] filebuffer = new byte[65536];
    	
    	//Send file_start msg to the destination server
    	mDataOut[inDestID].println(myId + " " + inDestID + " " + "file_start" + "#" + inFilePath);
    	mDataOut[inDestID].flush();
    	
    	//Start sending file one chunk at a time
    	File f=new File(inFilePath);
    	FileInputStream fis=null;
    	try {
			fis = new FileInputStream(f);
	         int count;
	         while ((count = fis.read(filebuffer)) >= 0) {
	        	 mSocketOut[inDestID].write(filebuffer, 0, count);

	            }
	         mSocketOut[inDestID].flush();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			try {
				fis.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
    			    	
    }
    
    //Receive File
    public void receiveFile(int inSrcID, String inName){

    	byte[] filebuffer = new byte[65536];
        try {           
            FileOutputStream fos = new FileOutputStream(inName);

            int count;
            while ((count = mSocketIn[inSrcID].read(filebuffer)) >= 0) {
                fos.write(filebuffer, 0, count);
            }
        }catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    //Process Msg
    public void processMsg(ServerMsg m){
    	String tag = m.getTag();
    	
    	//Messages received as worker
    	if(tag.equals("job_init")){
    		//set server worker object's coordinator ID field
    	}    	
    	else if(tag.equals("image_transfer_start")){
    		receiveFile(m.srcID,m.getMessage());
    	}
    	else if(tag.equals("image_transfer_done")){
    		//send  message "image_received_ack" to coordinator
    		//set file path for worker object
    	}
    	else if(tag.equals("job_start")){
    		//call mSW.processImage()
    	}
    	else if(tag.equals("job_stop")){
    		//call mSW.stopProcessing
    	}
    	
    	//Message received as coordinator
    	else if(tag.equals("job_done")){
    		//call coordinator job_done method
    	}
    	else if(tag.equals("image_received_ack")){
    		//call coordinator 
    	}
    }
    
    //Close all sockets
    public void close() {mConnector.closeSockets();}
	
    
    //Connector class
    public class Connector {
        ServerSocket myss;
        Socket [] s;
        public void Connect(int inMyId, int inNumServers,BufferedReader[] inDataIn,
        		PrintWriter[] inDataOut, int inMyPort) throws Exception {          

            s = new Socket[inNumServers];/* Get as many sockets as # of processes */
            myss = new ServerSocket(inMyPort);
            
            // Establish connections from processes with smaller id
            for (int i = 0; i < inMyId; i++) {
                Socket sc = myss.accept();               
                BufferedReader dIn = new BufferedReader( new InputStreamReader(sc.getInputStream()));
                String getline = dIn.readLine();
                StringTokenizer st = new StringTokenizer(getline);
                int hisId = Integer.parseInt(st.nextToken());
                int destId = Integer.parseInt(st.nextToken());
                String tag = st.nextToken();
                if (tag.equals("connect")) {
                    s[hisId] = sc;
                    inDataIn[hisId] = dIn;
                    inDataOut[hisId] = new PrintWriter(sc.getOutputStream());
                    mSocketOut[hisId] = sc.getOutputStream();
                    mSocketIn[hisId] = sc.getInputStream();
                }
            }
            // Connect with processes with bigger id
            for (int i = inMyId + 1; i < inNumServers; i++) {    
            	boolean isConnected = false;
            	
            	//Keep retrying until connected
            	while(isConnected){
	            	try{
	            		s[i] = new Socket(mSC.getServerAddress(i),mSC.getServerPort(i));
	            		inDataOut[i] = new PrintWriter(s[i].getOutputStream());
	            		inDataIn[i] = new BufferedReader(new InputStreamReader(s[i].getInputStream()));
	                    mSocketOut[i] = s[i].getOutputStream();
	                    mSocketIn[i] = s[i].getInputStream();
	            		isConnected = true;
	            	}catch (UnknownHostException e) {
	            		isConnected = false;
	            		 Thread.sleep(100);
	                } catch (IOException e) {
	                	isConnected = false;
	                	 Thread.sleep(100);
	                }
            	}
            	                
                // send a message to process 
                inDataOut[i].println(myId + " " + i + " " + "hello" + " " + "null");
                inDataOut[i].flush();
            }
        }

        public void closeSockets(){
            try {
                myss.close();
                for (int i=0;i<s.length; i++)
                    s[i].close();
            } catch (Exception e) {System.err.println(e);}
        }
    }
    
    
    //Listener Thread
    public class ListenerThread extends Thread {
        int channel;     
        public ListenerThread(int channel) {
            this.channel = channel;           
        }
        public void run() {
            while (true) {
                try {
                    ServerMsg m = receiveMsg(channel);
                    processMsg(m);
                } catch (IOException e) {
                    System.err.println(e);
                }
            }
        }
    }

    
    
}