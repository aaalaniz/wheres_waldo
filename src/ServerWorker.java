


public class ServerWorker {
	private final static int RED_THRESHOLD = 170;
	private final static int BLUE_THRESHOLD = 70;
	private final static int GREEN_THRESHOLD = 70;
	private final static int RED_COUNT_MIN = 20;
	private final static int RED_COUNT_MAX = 200;
	final static int SUB_IMAGE_WIDTH = 40;
	final static int SUB_IMAGE_HEIGHT = 60;
	//Data members
	String mFilePath;
	int mXCoord, mYCoord, mJobItemID;
	int mcoordID;
	static ServerCommTX mSST;


	//Constructor
	public ServerWorker(ServerCommTX inSST){
		mSST = inSST;
		threadMessage("ServerWorker instantiated");
	}

    static void threadMessage(String message)
    {
        String threadName =
            Thread.currentThread().getName();
        System.out.format("%s: %s%n",
                          threadName,
                          message);
    }

	//Initialize new job
	//Called by SSC.processMsg when "job_init" message is received
	public void InitJob(int inCoordID){
		mcoordID = inCoordID;
		mXCoord = 0;
		mYCoord = 0;
		mJobItemID = 0;
		mFilePath = "";

		threadMessage("ServerWorker JobInit");
	}

	//Image received
	//Called by ServerCommRX.processMsg when "image_transfer_done" message is received
	//This sets the local file path, and also sends an image received ack back to the coordinator
	//which will tell the coordinator that it's ready
	public void ImageReceived(String inFilePath){
		mFilePath = inFilePath;
		threadMessage("ServerWorker ImageReceived " + inFilePath);
		mSST.sendMsg(mcoordID, "image_received_ack","");
	}

	//ProcessImage
	//Called by SSC.processMsg when "job_start" message is received
	//XCoord, YCoord, and JobItemID are sent by the coordinator along with the "job_start"
	public void StartJob(int inXCoord, int inYCoord, int inJobItemID){
		mJobItemID = inJobItemID;
		mXCoord = inXCoord;
		mYCoord = inYCoord;

		threadMessage("ServerWorker StartJob. JobItemID:" + mJobItemID +"X: " + mXCoord + "Y: "  +mYCoord);

		try {
			// Establish a boundary safe width and height
			int width = (inXCoord + SUB_IMAGE_WIDTH < target.getWidth()) ? (SUB_IMAGE_WIDTH) : (target.getWidth() - inXCoord);
			int height = (inYCoord + SUB_IMAGE_HEIGHT < target.getHeight()) ? (SUB_IMAGE_HEIGHT) : (target.getHeight() - inYCoord);
			BufferedImage subImage = target.getSubimage(inXCoord, inYCoord, width, height);
			int rgbData[] = target.getRGB(inXCoord, inYCoord, width, height, null, 0, width);
			int redCount = getRedCount(rgbData);

			// Throw out this coordinate if it does not contain a good amount of red
			if (!goodAmountOfRed(redCount)) {
				this.Done(inJobItemID, 0);
				return;
			}

			// Find the number of matches
			MBFImage subTarget = new MBFImage(width, height, ColourSpace.RGB);
			ImageUtilities.assignBufferedImage(subImage, subTarget);
			LocalFeatureList<Keypoint> targetKeypoints = engine.findFeatures(subTarget);
			LocalFeatureMatcher<Keypoint> matcher = new BasicTwoWayMatcher<Keypoint>();
			matcher.setModelFeatures(queryKeypoints);
			matcher.findMatches(targetKeypoints);
			this.Done(inJobItemID, matcher.getMatches().size());
		} catch (Exception e) {
			System.out.println(e.getMessage());
			this.Done(inJobItemID, 0);
		}
	}

	//Done
	//Called when image processing is complete
	//This sends "job_done" message with JobItemID and Features Matched back to the coordinator
	public void Done(int inJobItemID, int inFeatMatched){
		//Send a message back to coordinator with number of features matched
		String msg = inJobItemID + " " + inFeatMatched;
		threadMessage("ServerWorker JobDone. JobItemID:" + mJobItemID + "FeaturesMatched: " + inFeatMatched);
		mSST.sendMsg(mcoordID, "job_done", msg);
	}

	/**
	 * Returns the amount of red pixels from an array of RBG data.
	 *
	 * @param rgbData	Array of 32 bit integer values containing RGB data
	 * @return	Number of red pixels in a sub image
	 */
	private static int getRedCount(int[] rgbData) {
		int redCount = 0;

		for (int i = 0 ; i < rgbData.length ; i++) {
			int red = (rgbData[i] >> 16) & 0xFF;
			int green = (rgbData[i] >> 8) & 0xFF;
			int blue = rgbData[i] & 0xFF;
			if (red >= RED_THRESHOLD && green < GREEN_THRESHOLD && blue < BLUE_THRESHOLD) {
			//if (red >= RED_THRESHOLD) {
				redCount++;
			}
		}
		return redCount;
	}

	/**
	 * Threshold function that determines if a sub image contains enough red pixels to perform a search.
	 *
	 * @param redCount	Number of red pixels.
	 * @return	true of the amount of red pixels is sufficient and false otherwise
	 */
	private static boolean goodAmountOfRed(int redCount) {
		if (redCount > RED_COUNT_MIN && redCount < RED_COUNT_MAX) {
			return true;
		} else {
			return false;
		}
	}

}
