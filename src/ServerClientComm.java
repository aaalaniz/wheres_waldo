
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
            threadMessage("ServerRX:Starting TCP ServerRX");

            try
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
                /*
             if(mSC.mMyID == 0){   
	               try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	               mFilePath = "whereswaldo1.jpg";
	               mSCoord.ProcessJob(mFilePath);
	            }
            }*/
            catch (IOException e)
            {
                System.err.println(e);
                System.exit(-1);
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
            mFilePath = FileTransfer.receiveFile(mS, mSCoord.mLocalBasePath);
			mSCoord.ProcessJob(mFilePath);
			
			//Spin until job is done
			while(!mSCoord.getClientJobDone()){
				
			}

			String resFname = mSCoord.getResultImgPath();
			File f=new File(resFname);
			FileTransfer.sendFile(mS,f);
			
            System.out.println("TCP Connection Closed with hostname " + mS.getInetAddress().getHostName()
                               + "(" +  mS.getInetAddress().getHostAddress() + ")");
        }

    }

}

