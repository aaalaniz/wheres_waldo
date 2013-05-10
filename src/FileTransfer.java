import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;


public class FileTransfer {

	static int bufferSize = 6022386;
	
	public static void sendFile(Socket sock, File myFile){
		
		
		try{
			DataOutputStream outToServer = new DataOutputStream(sock.getOutputStream());
			BufferedReader inFromServer = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			
			//send file name
			String sendName = "File Name: " + myFile.getName();
			System.out.println("sending file name");
			outToServer.writeBytes(sendName + '\n');
			//receive file name
			System.out.println("reading file name from client");
			String receiveName = inFromServer.readLine();
			
			//send file size			
			String sendSize = "File Size: " + myFile.length()+ '\n';
			System.out.println("sending file size");
			outToServer.writeBytes(sendSize + '\n');
	    	//receive file size
			System.out.println("reading file size from client");
			String receiveSize = inFromServer.readLine();
			
			System.out.println("sending file to client");
			//now send the file			
			byte [] mybytearray  = new byte [(int)myFile.length()];
			FileInputStream fis = new FileInputStream(myFile);
			BufferedInputStream bis = new BufferedInputStream(fis);
			bis.read(mybytearray,0,mybytearray.length);
			OutputStream os = sock.getOutputStream();
					  
			os.write(mybytearray,0,mybytearray.length);
			os.flush();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static String receiveFile(Socket sock, String filePath){
		
		int bytesRead;
	    int current = 0;	   
	   
	    try{
	    	
	    	DataOutputStream outToClient = new DataOutputStream(sock.getOutputStream());	    	
			BufferedReader inFromClient = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			
			//receive file name
			String receiveName = inFromClient.readLine();
			//Send file name 
			outToClient.writeBytes(receiveName + '\n');
			
			
	    	//receive file size
			String receiveSize = inFromClient.readLine();			
			//send file size		
			outToClient.writeBytes(receiveSize + '\n');
			
			//parse to get the file name
			//File Name: xxxxlkjsf.jpg
			String fileName = receiveName.substring(11);
			//parse the file size
			//File Size: 1234
			int fileSize = Integer.parseInt(receiveSize.substring(11));
			
			// receive file
		    byte [] mybytearray  = new byte [fileSize];
		    InputStream is = sock.getInputStream();
		    FileOutputStream fos = new FileOutputStream(filePath + "/" + fileName);
		    BufferedOutputStream bos = new BufferedOutputStream(fos);
		    
		    bytesRead = is.read(mybytearray,0,mybytearray.length);
		    current = bytesRead;
			    
		    do {
		       bytesRead =
		          is.read(mybytearray, current, (mybytearray.length-current));
		       if(bytesRead >= 0) current += bytesRead;
		    } while(bytesRead > 0);
	
		    bos.write(mybytearray, 0 , current);
		    //bos.write(mybytearray, 0 , mybytearray.length);
		    bos.flush();
		    
		    bos.close();
		    
		    return fileName;
	    }
	    catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	    return null;
	}
	
}
