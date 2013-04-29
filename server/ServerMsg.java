
import java.util.*;

//Server Message Format
//SrcID DestID tag#buf
public class ServerMsg{
	int srcID, destID;
	int msgNum;
	String tag;
	String msgBuf;
	public ServerMsg(int s, int t, String msgType, String buf){
		this.srcID = s;
		destID = t;	
		tag = msgType;
		msgBuf = buf;
	}
	public int getSrcId(){
		return srcID;
	}
	public int getDestID(){
		return destID;
	}

	public String getTag(){
		return tag;
	}
	public String getMessage(){
		return msgBuf;
	}
	public int getMessageInt(){		
		return Integer.parseInt(msgBuf);
	}
	public static ServerMsg parseMsg(StringTokenizer st){
		int srcID = Integer.parseInt(st.nextToken());
		int destID = Integer.parseInt(st.nextToken());
		String tag = st.nextToken("#");
		tag = tag.trim();//For some reason there is a space in front of this
		String buf = st.nextToken();
		return new ServerMsg(srcID, destID,tag, buf);
	}
	
}