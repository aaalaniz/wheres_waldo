
import java.io.*;
import java.util.Vector;

//Class config
public class ServersConfig{
	public Vector<ServerConfig> mServers;
	public int mNumServers;
	public int mMySSPort; // Server to Server
	public int mMyCSPort; // Client to/from Server	
	public int mMyID;
	//Private constructor because this is SINGLETON
	private  ServersConfig(){		
		//This is just the initial size. The add function will re-size this 
		//automatically
		mServers = new Vector<ServerConfig>(4,4);
	}
	
	//Create an instance of class config the first time getConfifg
	//is called below
	private static class ConfigHolder{
		public static final ServersConfig mConfig = new ServersConfig();
	}
	
	//Call this method to get a reference to Config object
	public static ServersConfig getConfig(){		
		return ConfigHolder.mConfig;
	}
	
	//Add servers to the list while parsing the config file
	public void addServer(int inPort, String inName, String inIPAddress){
		ServerConfig sc = new ServerConfig(inPort,inName, inIPAddress);
		mServers.add(sc);
	}
	
	public int getSSPort(){
		return mMySSPort;
	}
	
	public int getCSPort(){
		return mMyCSPort;
	}
	
	public int getMyID(){
		return mMyID;
	}
	
	//Get ServerPort from serverID
	public int getServerPort(int inServerID){
		try{
			return mServers.get(inServerID).getPort();
		} 
		catch(ArrayIndexOutOfBoundsException e){
			System.err.println(e);
			return 0;
		}		
	}
	//Get ServerAddress from serverID
	public String getServerAddress(int inServerID){
		try{
			return mServers.get(inServerID).getIPAddress();
		} 
		catch(ArrayIndexOutOfBoundsException e){
			System.err.println(e);
			return "";
		}		
	}
	//Get ServerName from serverID
	public String getServerName(int inServerID){
		try{
			return mServers.get(inServerID).getName();
		} 
		catch(ArrayIndexOutOfBoundsException e){
			System.err.println(e);
			return "";
		}		
	}
	//Get serverID
	public int getID(int inPort, String inIPAddress, String inName){
		for(int i=0; i<mNumServers;i++){
			try {
				if( (mServers.get(i).getPort() == inPort) || 
					(mServers.get(i).getIPAddress() == inIPAddress) ||
					(mServers.get(i).getName() == inName)
					){
					return i;
				}
			}
			catch(ArrayIndexOutOfBoundsException e){
				System.err.println(e);
				return -1;
			}					
		}
		return -1;
	}
}

//Class for server config objects
class ServerConfig{
	private int mPort;
	private String mName;
	private String mIPAddress;
	
	ServerConfig(int inPort, String inName, String inIPAddress){
		mPort = inPort;
		mName = inName;
		mIPAddress = inIPAddress;
	}
	
	public int getPort() { return mPort;}
	public String getName() { return mName; }
	public String getIPAddress() { return mIPAddress; }

}
