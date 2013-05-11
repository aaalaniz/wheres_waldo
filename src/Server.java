import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.StringTokenizer;

//Class MovieReservationServer
class Server{
		
	//Constructor
	public Server(String inConfigFile){
		processConfigFile(inConfigFile);

	}
	
    static void threadMessage(String message)
    {
        String threadName =
            Thread.currentThread().getName();
        System.out.format("%s: %s%n",
                          threadName,
                          message);
    }
	
	//Parse Config File	
	//Init Config object
	private void processConfigFile(String inFname){
		ServersConfig cfg = ServersConfig.getConfig();
        try
        {
            BufferedReader fin = new BufferedReader(new FileReader(inFname));
            String line;

            while ((line = fin.readLine()) != null)
            {
                StringTokenizer st = new StringTokenizer(line, " :");
                String tag = st.nextToken();
                
                if(tag.equals("Server{"))
                {
                	
                	// \todo error checking
                	String address = null, name = null;
                	int ssport =0, csport = 0, ssudpport=0;
                	for(int i = 0; i<5; i++){
	                	line = fin.readLine();
	                	StringTokenizer st1 = new StringTokenizer(line, " :");
	                	String tag1 = st1.nextToken();
	                	if (tag1.equals("ServerAddress")){	                    	
				    	  address = st1.nextToken();
	                	}
	                	if (tag1.equals("ServerName")){	                    	
	                		name = st1.nextToken();
	                	}
	                	if (tag1.equals("SSTCPPort")){	                    	
	                		ssport = Integer.parseInt(st1.nextToken());
	                	}
	                	if (tag1.equals("CSTCPPort")){	                    		                		
	                		csport =  Integer.parseInt(st1.nextToken());
	                	}
	                	if (tag1.equals("SSUDPPort")){	                    		                		
	                		ssudpport =  Integer.parseInt(st1.nextToken());
	                	}
                	}
                	cfg.addServer(ssport, name, address,ssudpport, csport); //MArefin added , csport
                }

                if (tag.equals("NumServers"))
                {
                	cfg.mNumServers = Integer.parseInt(st.nextToken());
                }
                if(tag.equals("TemplateImagePath")){
                	cfg.mTmpImgPath = st.nextToken();
                	
                	//MArefin debugging in windows machines
                	//resolve the file path
                	if (cfg.mTmpImgPath.equals("C")){
                		String filePath = st.nextToken();;
                		cfg.mTmpImgPath = cfg.mTmpImgPath + ":" + filePath;
                		
                	}
                }

                                              
            }
            cfg.mMySSTCPPort = cfg.getServerTCPPort(cfg.mMyID);  
            cfg.mMySSUDPPort = cfg.getServerUDPPort(cfg.mMyID);
            //MArefin 5/7/2013
            cfg.mMyCSTCPPort = cfg.getClientServerTCPPort(cfg.mMyID);
            threadMessage("MyID: " + cfg.mMyID);
            threadMessage("mMySSTCPPort: " + cfg.mMySSTCPPort);  
            threadMessage("mMySSUDPPort: " + cfg.mMySSUDPPort);  
            threadMessage("mMyCSTCPPort: " + cfg.mMyCSTCPPort);  
            threadMessage("NumServers: " + cfg.mNumServers);
            threadMessage("TemplateImagePath: " + cfg.mTmpImgPath);
    		for(int i=0; i<cfg.mNumServers;i++){       			   			
    			threadMessage("SSTCPPort: " + cfg.mServers.get(i).getTCPPort());   
    			threadMessage("SSUDPPort: " + cfg.mServers.get(i).getUDPPort());
    			threadMessage("IPAddress: " + cfg.mServers.get(i).getIPAddress());  
    			threadMessage("Name: " + cfg.mServers.get(i).getName());        				
    		}
    			  
        }
        catch (IOException e)
        {
            System.err.println(e);
        }
	}
		
	
	//Main entry point in the client program
	public static void main(String[] args) throws IOException{
		
        String configFile = "";
        String ID = "";
        ServersConfig cfg = ServersConfig.getConfig();

        if (args.length == 2)
        {
            configFile = args[0];
            cfg.mMyID = Integer.parseInt(args[1]);
        }
        else
        {
            System.err.println("Invalid args. Usage Server [configfile] [ID]");
            System.exit(-1);
        }

        
		System.out.println("Starting Server");
		System.out.println("Config File: " + args[0]);
		System.out.println();
		
		//Create server object and process config file
        Server s = new Server(configFile);
			
        
	        //Create objects                
        	//Create a Datagram socket
        	DatagramSocket UDPSocket=null;
			try {
				UDPSocket = new DatagramSocket(cfg.mMySSUDPPort);
			} catch (SocketException e) {
				//TODO Auto-generated catch block
				e.printStackTrace();
			}
	        
	        //ServerCommTX
	        ServerCommTX sct = new ServerCommTX(UDPSocket);
	   
	        //Server Worker
	        //This object is always instantiated, but only becomes active on receiving messages
	        //Also, at a given time either the server will act as a worker or coordinator
	        ServerWorker sw = new ServerWorker(sct);
	        
	        //ServerImageTemplate
	        //ServerImageTemplate sit = new ServerImageTemplate(cfg.getTmpImgPath());
	        
	        //Server Coordinator
	        //This object is always instantiated, but only becomes active on receiving messages
	        //Also, at a given time either the server will act as a worker or coordinator
	        ServerCoordinator sc = new ServerCoordinator(sct,null);
	        
	        //Client to/from communication object 
	        ServerClientComm scc = new ServerClientComm(sc);
	        
	        //ServerCommRX
	        ServerCommRX scr = new ServerCommRX(sw, sc,UDPSocket,sct);	                           
        
	}
	
}



