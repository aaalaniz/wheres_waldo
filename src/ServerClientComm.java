
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.*;
import java.util.*;

//Class RX
public class ServerClientComm{
	//Data members
	static ServersConfig mSC;
	static Thread mThRXClient;
	static ServerCoordinator mSCoord;
	static String mFilePath;
	
	//Constructor
	public ServerClientComm(ServerCoordinator inSCoord){
		mSC = ServersConfig.getConfig();
		mSCoord = inSCoord;
		
		mThRXClient = new Thread(new RXListenClient());
		mThRXClient.start();
		
		 threadMessage("ServerClientComm:Created");
	}
	
    static void threadMessage(String message)
    {
        String threadName =
            Thread.currentThread().getName();
        System.out.format("%s: %s%n",
                          threadName,
                          message);
    }
    
    //Thread to process client requests
    private static class RXListenClient implements Runnable
    {
        public void run()
        {
            threadMessage("ServerClientComm:Starting TCP ServerRX");

            /*try
            {
                ServerSocket ss = new ServerSocket(mSC.getCSPort());

                while (true)
                {
                    Socket s = ss.accept();
                    threadMessage("Request from client. Spawning thread");
                    Thread t = new Thread(new RXProcessClient(s));
                    t.start();
                }
            }
            catch (IOException e)
            {
                System.err.println(e);
                System.exit(-1);
            }*/
            if(mSC.mMyID == 0){
	            threadMessage("ServerClientComm: Waiting before kicking off the job");
	            try {
					Thread.sleep(15000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	            
		        	String mFilePath = "whereswaldo1.jpg";
		        	mSCoord.ProcessJob(mFilePath);
	        }           
        }
    }
    

    //Class to handle individual client request
    public static class RXProcessClient implements Runnable
    {
        private Socket mS;
        private BufferedReader mInputStream;
        private PrintStream mOutputStream;
        public RXProcessClient(Socket inS)
        {
            try
            {
                mS = inS;
                mInputStream  = new BufferedReader(new InputStreamReader(mS.getInputStream()));
                mOutputStream = new PrintStream(mS.getOutputStream());
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
                String requestIn, requestOut;

                while ((requestIn = mInputStream.readLine()) != null)
                {
                    //Receive request
                	// \todo receive image. Will need to break the file down into pieces
                	//and then write it to file system on the server
                    threadMessage(requestIn + " request received from hostname " + mS.getInetAddress().getHostName()
                                  + "(" +  mS.getInetAddress().getHostAddress() + ")");
                                        
                    //Process request. 
                    //\todo Instantiate server coordinator, and do the work
                    mSCoord.ProcessJob(mFilePath);
                    requestOut = "Image Received";
                    
                    
                    //Respond
                    mOutputStream.println(requestOut);
                    mOutputStream.flush();
                    threadMessage("hostname " + mS.getInetAddress().getHostName() + "("
                                  +  mS.getInetAddress().getHostAddress() + ") request processed");
                    threadMessage("Request In: " + requestIn);
                    threadMessage("Request Out: " + requestOut);
                }
            }
            catch (SocketException se)
            {
                System.err.println(se);
            }
            catch (IOException e)
            {
                System.err.println(e);
            }

            System.out.println("TCP Connection Closed with hostname " + mS.getInetAddress().getHostName()
                               + "(" +  mS.getInetAddress().getHostAddress() + ")");
        }

    }

}

