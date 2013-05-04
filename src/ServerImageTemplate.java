import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.imageio.ImageIO;


public class ServerImageTemplate {
	
	//Data members
	int mTmpWd, mTmpHt;
	String mImageTemplatePath;
	
	
	//Constructor
	public ServerImageTemplate(String inFilePath){
		//Determine dimension of the image	
		mImageTemplatePath = inFilePath;
		File file = new File(mImageTemplatePath); 
        FileInputStream fis;
        BufferedImage image;
		try {
			fis = new FileInputStream(file);
			image = ImageIO.read(fis);
			mTmpWd = image.getWidth();
			mTmpHt = image.getHeight();
		} catch (FileNotFoundException e) {			
			e.printStackTrace();
		} catch (IOException e) {			
			e.printStackTrace();
		}
	}
	
	public int getTmpWd(){
		return mTmpWd;
	}
	
	public int getTmpHt(){
		return mTmpHt;
	}

}
