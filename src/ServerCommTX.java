import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;


public class ServerCommTX {
	
	//Data Members
	ServersConfig mSC; 
	DatagramSocket mUDPSocket;
    int mID;
	    
    //Constructor
    public ServerCommTX(DatagramSocket inUDPSocket){
    	
    	mSC = ServersConfig.getConfig();
    	mUDPSocket = inUDPSocket;
    	mID = mSC.mMyID;
    	
    	threadMessage("ServerCommTX created mID: "+ mID);
    }
    
    static void threadMessage(String message)
    {
       /*String threadName =
            Thread.currentThread().getName();
        System.out.format("%s: %s%n",
                          threadName,
                          message);
                          */
    }
	
	//Send Msg
    public synchronized void sendMsg(int destId, String inMsg) throws IOException {    
    	threadMessage("ServerCommTX sending message to destID: " + destId + " msg: " + inMsg);

        DatagramPacket sp;
        String msg = inMsg;
        
        //Server Message Format
        //srcID destID tag#buf
        msg = mID + " " + destId + " " + inMsg;
        byte[] buffer = new byte[msg.length()];
        buffer = msg.getBytes();
        try {
        	InetAddress myIA=null;
        	try {

                myIA = InetAddress.getByName(mSC.mServers.get(destId).getIPAddress());
        		//mmIA = InetAddress.get
        	} catch (UnknownHostException e) {
                System.err.println(e);
        	}

        	//Send Packet
        	sp = new DatagramPacket(buffer,
        			buffer.length, myIA, mSC.mServers.get(destId).getUDPPort());
        	mUDPSocket.send(sp);

        } catch (IOException e)   {
        	System.err.println(e);
        }        
    }
    
    //Send File
    public synchronized void sendFile(int inDestID, String inFilePath){
    	byte[] filebuffer = new byte[65536];
    	
    	threadMessage("ServerCommTX sending file to destID: " + inDestID + " LocalFilePath: " + inFilePath);
    	    	    	    	
    	//Open socket
    	Socket s;
    	OutputStream os=null;
		try {
			s = new Socket(mSC.getServerAddress(inDestID),mSC.getServerTCPPort(inDestID));
			os = s.getOutputStream();
		} catch (UnknownHostException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
    	
    	
    	//Send message with srcID filename filesize
    	//Msg format: srcID filename filesize
    	File f=new File(inFilePath);
    	byte[] buf = new byte[1024];
    	java.util.Arrays.fill(buf, (byte) 0);
    	String m = Integer.toString(mID) + " " + f.getName() + " " + f.length() + " ";
    	byte[] tbuf = m.getBytes();
    	for(int i=0;i<tbuf.length;i++){
    		buf[i] = tbuf[i];
    	}    	
    	try {
			os.write(buf);
			os.flush();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	    	
    	////Send file    	
    	FileInputStream fis=null;        	
    	long size = f.length();
    	try {
			fis = new FileInputStream(f);
	         int count = (int) (size/50000);
	         while (count >0) {
	        	 fis.read(filebuffer);
	        	 os.write(filebuffer);
	        	 os.flush();	        	
	        	 count--;
	         }
	         byte[] lastPart = new byte[(int) (size%50000)];
        	 fis.read(lastPart);
        	 os.write(lastPart);
	         os.flush();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.err.println(e);
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.err.println(e);
			e.printStackTrace();
		}finally{
			try {
				fis.close();
			} catch (IOException e) {
				System.err.println(e);
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
    	
    	
    	threadMessage("ServerCommTX file send complete to destID: " + inDestID + " LocalFilePath: " + inFilePath);
    			    	
    }

}
