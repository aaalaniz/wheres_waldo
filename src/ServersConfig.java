
import java.io.*;
import java.util.Vector;

//Class config
public class ServersConfig{
	public Vector<ServerConfig> mServers;
	public int mNumServers;
	public int mMySSTCPPort; // Server to Server
	public int mMySSUDPPort; // Server to Server File transfer
	public int mMyCSTCPPort; // Client to/from Server	
	public int mMyID;
	public String mTmpImgPath;
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
	public void addServer(int inSSPort, String inName, String inIPAddress,int inSSUDPPort){
		ServerConfig sc = new ServerConfig(inSSPort,inName, inIPAddress,inSSUDPPort);
		mServers.add(sc);
	}
	
	public int getSSTCPPort(){
		return mMySSTCPPort;
	}	
	public int getSSUDPPort(){
		return mMySSUDPPort;
	}
	public int getCSPort(){
		return mMyCSTCPPort;
	}
	
	public int getMyID(){
		return mMyID;
	}
	
	public String getTmpImgPath(){
		return mTmpImgPath;
	}
	
	//Get ServerPort from serverID
	public int getServerTCPPort(int inServerID){
		try{
			return mServers.get(inServerID).getTCPPort();
		} 
		catch(ArrayIndexOutOfBoundsException e){
			System.err.println(e);
			return 0;
		}		
	}
	//Get ServerPort from serverID
	public int getServerUDPPort(int inServerID){
		try{
			return mServers.get(inServerID).getUDPPort();
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
				if( (mServers.get(i).getTCPPort() == inPort) || 
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
	private int mSSTCPPort, mSSUDPPort;
	private String mName;
	private String mIPAddress;
	
	ServerConfig(int inSSTCPPort, String inName, String inIPAddress, int inSSUDPPort){
		mSSTCPPort = inSSTCPPort;
		mName = inName;
		mIPAddress = inIPAddress;
		mSSUDPPort = inSSUDPPort;
	}
	
	public int getTCPPort() { return mSSTCPPort;}	
	public int getUDPPort() { return mSSUDPPort;}
	public String getName() { return mName; }
	public String getIPAddress() { return mIPAddress; }

}
