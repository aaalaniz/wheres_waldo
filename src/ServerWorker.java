import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.PriorityQueue;

import javax.imageio.ImageIO;

import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.feature.local.matcher.BasicTwoWayMatcher;
import org.openimaj.feature.local.matcher.LocalFeatureMatcher;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.feature.local.engine.DoGColourSIFTEngine;
import org.openimaj.image.feature.local.keypoints.Keypoint;


public class ServerWorker {
	private final static int SUB_IMAGE_WIDTH = 40;
	private final static int SUB_IMAGE_HEIGHT = 60;
	//Data members
	String mFilePath;
	int mXCoord, mYCoord, mJobItemID;
	int mcoordID;
	static ServerServerComm mSSC;
	
	
	//Constructor
	public ServerWorker(ServerServerComm inSSC){
		mSSC = inSSC;
	}
	
	//Initialize new job
	//Called by SSC.processMsg when "job_init" message is received
	public void InitJob(int inCoordID){
		mcoordID = inCoordID;
		mXCoord = 0;
		mYCoord = 0;
		mJobItemID = 0;
		mFilePath = "";
	}
	
	//Image received
	//Called by SSC.processMsg when "image_transfer_done" message is received
	//This sets the local file path, and also sends an image received ack back to the coordinator
	public void ImageReceived(String inFilePath){
		mFilePath = inFilePath;
		mSSC.sendMsg(mcoordID, "image_received_ack","");
	}
	
	//ProcessImage
	//Called by SSC.processMsg when "job_start" message is received
	//XCoord, YCoord, and JobItemID are sent by the coordinator along with the "job_start"
	public void StartJob(int inXCoord, int inYCoord, int inJobItemID) throws IOException{
		mJobItemID = inJobItemID;
		mXCoord = inXCoord;
		mYCoord = inYCoord; 
		
		BufferedImage target = ImageIO.read(new File(this.mFilePath));
		LocalFeatureList<Keypoint> queryKeypoints = null;
		System.out.println(target.getWidth() + "x" + target.getHeight());
		
		// Build data base of features
		// TODO - add path to template
		MBFImage query = ImageUtilities.readMBF(new File(new String("PLEASE REPLACE ME WITH PATH TO TEMPLATE IMAGE")));
		DoGColourSIFTEngine engine = new DoGColourSIFTEngine();
		LocalFeatureList<Keypoint> keyPoints = engine.findFeatures(query);
		
		try {
			int width = (inXCoord + SUB_IMAGE_WIDTH < target.getWidth()) ? (SUB_IMAGE_WIDTH) : (target.getWidth() - inXCoord);
			int height = (inYCoord + SUB_IMAGE_HEIGHT < target.getHeight()) ? (SUB_IMAGE_HEIGHT) : (target.getHeight() - inYCoord);
			BufferedImage subImage = target.getSubimage(inXCoord, inYCoord, width, height);
			MBFImage subTarget = new MBFImage(width, height, ColourSpace.RGB);
			ImageUtilities.assignBufferedImage(subImage, subTarget);		
			LocalFeatureList<Keypoint> targetKeypoints = engine.findFeatures(subTarget);
			LocalFeatureMatcher<Keypoint> matcher = new BasicTwoWayMatcher<Keypoint>();
			matcher.setModelFeatures(queryKeypoints);
			matcher.findMatches(targetKeypoints);
			this.Done(inJobItemID, matcher.getMatches().size());
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	//Done
	//Called when image processing is complete
	//This sends "job_done" message with JobItemID and Features Matched back to the coordinator
	public void Done(int inJobItemID, int inFeatMatched){
		//Send a message back to coordinator with number of features matched
		String msg = inJobItemID + " " + inFeatMatched;
		mSSC.sendMsg(mcoordID, "job_done", msg);		
	}
		
}
