import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;

import javax.imageio.*;
import javax.swing.*;

public class DisplayImage extends Component {

	BufferedImage img;

    public void paint(Graphics g) {
        g.drawImage(img, 0, 0, null);
    }

    public DisplayImage(String imgFile) {
       try {
           img = ImageIO.read(new File(imgFile));
       } catch (IOException e) {
       }

    }

    public Dimension getPreferredSize() {
        if (img == null) {
             return new Dimension(1000,1000);
        } else {
           return new Dimension(img.getWidth(null), img.getHeight(null));
       }
    }
    
    
    public static void displayImgFrame(String imgFile){
    	
    	 JFrame f = new JFrame("Load Highlighted Image");
         
         f.addWindowListener(new WindowAdapter(){
                 public void windowClosing(WindowEvent e) {
                     System.exit(0);
                 }
             });

         f.add(new DisplayImage(imgFile));
         f.pack();
         f.setVisible(true);
    	
    }
    
}
