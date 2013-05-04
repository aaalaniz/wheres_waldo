import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;


public class ServerCommTX {
	
	//Data Members
	OutputStream[] mSocketOut;	
    PrintWriter[] mDataOut;
    int mID;
	    
    //Constructor
    public ServerCommTX(PrintWriter[] inDataOut,OutputStream[] inSocketOut,
    						 int inID){
    	mSocketOut = inSocketOut;
    	inDataOut = mDataOut;
    	mID = inID;
    }
    
	
	//Send Msg
    public void sendMsg(int destId, String tag, String msg) {       
    	mDataOut[destId].println(mID + " " + destId + " " + tag + " " + msg + "#");
    	mDataOut[destId].flush();
    }
    
    //Send File
    public void sendFile(int inDestID, String inFilePath){
    	byte[] filebuffer = new byte[65536];
    	
    	//Send file__transfer_start msg to the destination server
    	mDataOut[inDestID].println(mID + " " + inDestID + " " + "file_transfer_start" + "#" + inFilePath);
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
    	
    	//Send file_transfer_done message
    	mDataOut[inDestID].println(mID + " " + inDestID + " " + "file_transfer_done" + "#" + " ");
    	mDataOut[inDestID].flush();
    			    	
    }

}
