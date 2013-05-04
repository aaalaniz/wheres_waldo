import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.StringTokenizer;


public class ServerCommConnect {
	
	//Data Members
    PrintWriter[] mDataOut;
    BufferedReader[] mDataIn;  
    OutputStream[] mSocketOut;
    InputStream[] mSocketIn;
    int mId, mN, mMyPort;   
    ServersConfig mSC;

    //Constructor
	public ServerCommConnect(){
		mSC = ServersConfig.getConfig();
		mId = mSC.mMyID;
		mN = mSC.mNumServers;
		mMyPort = mSC.mMySSPort;
		mDataIn = new BufferedReader[mN];
		mDataOut = new PrintWriter[mN];  	
		mSocketOut = new OutputStream[mN];
		mSocketIn = new InputStream[mN];
	}
		
	//Connect
	public void Connect() throws Exception {   
		
		ServerSocket myss;
		Socket [] s;

		s = new Socket[mN];/* Get as many sockets as # of processes */
		myss = new ServerSocket(mMyPort);
            
		//Establish connections from processes with smaller id
		for (int i = 0; i < mId; i++) {
			Socket sc = myss.accept();               
			BufferedReader dIn = new BufferedReader( new InputStreamReader(sc.getInputStream()));
			String getline = dIn.readLine();
			StringTokenizer st = new StringTokenizer(getline);
			int hisId = Integer.parseInt(st.nextToken());
			int destId = Integer.parseInt(st.nextToken());
			String tag = st.nextToken();
			if (tag.equals("connect")) {
				s[hisId] = sc;
				mDataIn[hisId] = dIn;
				mDataOut[hisId] = new PrintWriter(sc.getOutputStream());
				mSocketOut[hisId] = sc.getOutputStream();
				mSocketIn[hisId] = sc.getInputStream();
			}
		}
		
		//Connect with processes with bigger id
		for (int i = mId + 1; i < mN; i++) {    
			boolean isConnected = false;
            	
			//Keep retrying until connected
			while(isConnected){
				try{
					s[i] = new Socket(mSC.getServerAddress(i),mSC.getServerPort(i));
	            	mDataOut[i] = new PrintWriter(s[i].getOutputStream());
	            	mDataIn[i] = new BufferedReader(new InputStreamReader(s[i].getInputStream()));
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
            	                
			//send a message to process 
			mDataOut[i].println(mId + " " + i + " " + "hello" + " " + "null");
			mDataOut[i].flush();
		}
	}
	
	//Get DataOut stream
	public PrintWriter[] GetDataOutStream(){
		return mDataOut;
	}
	
	//Get DataIn stream
	public BufferedReader[] GetDataInStream(){
		return mDataIn;
	}
	
	//Get SocketOut stream
	public OutputStream[] GetSocketOutStream(){
		return mSocketOut;
	}
	
	//Get SocketIn stream
	public InputStream[] GetSocketInStream(){
		return mSocketIn;
	}
	
	
	

}
