import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
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
                	int port = 0;
                	for(int i = 0; i<4; i++){
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
	                		port = Integer.parseInt(st1.nextToken());
	                	}
	                	if (tag1.equals("CSTCPPort")){	                    	
	                		port = Integer.parseInt(st1.nextToken());
	                	}
                	}
                	cfg.addServer(port, name, address);
                }

                if (tag.equals("NumServers"))
                {
                	cfg.mNumServers = Integer.parseInt(st.nextToken());
                }

                                              
            }
            cfg.mMyPort = cfg.getServerPort(cfg.mMyID);
            threadMessage("MyID: " + cfg.mMyID);
            threadMessage("MyPort: " + cfg.mMyPort);
            threadMessage("NumSeats: " + cfg.mNumSeats);
            threadMessage("NumServers: " + cfg.mNumServers);
    		for(int i=0; i<cfg.mNumServers;i++){   
    			//Set all servers alive
    			cfg.mServers.get(i).setAlive(true);
    			threadMessage("Port: " + cfg.mServers.get(i).getPort());        			 
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
	public static void main(String[] args){
		
        String configFile = "";
        String ID = "";
        Config cfg = Config.getConfig();

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
        Server s = new Server(configFile);
				

        //Create objects                
        //Server to Server communication object 
        ServerServerComm ssc = new ServerServerComm();
        
        //Server Worker
        //This object is always instantiated, but only becomes active on receiving messages
        //Also, at a given time either the server will act as a worker or coordinator
        ServerWorker sw = new ServerWorker(ssc);
        
        //Server Coordinator
        //This object is always instantiated, but only becomes active on receiving messages
        //Also, at a given time either the server will act as a worker or coordinator
        ServerCoordinator sc = new ServerCoordinator(ssc);
        
        //Client to/from communication object 
        ServerClientComm scc = new ServerClientComm();
        
	}
	
}



