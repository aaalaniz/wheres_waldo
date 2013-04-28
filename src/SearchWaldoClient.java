/**
 * @file SearchWaldoClient.java
 */

/**
 * @author Arefin
 *
 */

import java.io.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import com.sun.xml.internal.ws.api.server.Container;

public class SearchWaldoClient extends JPanel
							   implements ActionListener  {

	static private final String newline = "\n";
    JButton openButton, submitButton;
    JTextArea log;
    JFileChooser fc;
    JFrame dailogFrame;
	
	public SearchWaldoClient(){
		//super(new GridLayout(3,1));  //3 rows, 1 column
		super(new BorderLayout());
		
        JLabel label1, label2;
        
        //Create labels
        label1 = new JLabel("The application will search waldo using distributed algorithm.", JLabel.CENTER);
        label2 = new JLabel("Please select an image and press submit to search waldo.", JLabel.CENTER);
        
        //Create tool tips, for the heck of it.
        label1.setToolTipText("The application will search waldo using distributed algorithm.");
        label2.setToolTipText("Please select an image and press submit to search waldo.");        
 
        //Add the labels.
        JPanel labelPanel = new JPanel(); //use FlowLayout        
        labelPanel.add(label1);
        labelPanel.add(label2);
 
       // add(label1);
       // add(label2);
        
        
        
        
        //Create the log first, because the action listeners
        //need to refer to it.
        log = new JTextArea(30,20);
        log.setMargin(new Insets(5,5,5,5));
        log.setEditable(false);
        JScrollPane logScrollPane = new JScrollPane(log);
        
        //Create a file chooser
        fc = new JFileChooser();
		
        //Create the open button.  We use the image from the JLF
        //Graphics Repository (but we extracted it from the jar).
        openButton = new JButton("Open Image",
                                 createImageIcon("images/Open16.gif", "Open16.gif"));
        openButton.addActionListener(this);
 
        //Create the save button.  We use the image from the JLF
        //Graphics Repository (but we extracted it from the jar).
        submitButton = new JButton("Submit Image");
        submitButton.addActionListener(this);
 
        //For layout purposes, put the buttons in a separate panel
        JPanel buttonPanel = new JPanel(); //use FlowLayout        
        buttonPanel.add(openButton);
        buttonPanel.add(submitButton);
 
        //Add the buttons and the log to this panel.
        add(labelPanel, BorderLayout.PAGE_START);
        add(buttonPanel, BorderLayout.CENTER);
        add(logScrollPane, BorderLayout.PAGE_END);
	}
	
	public void actionPerformed(ActionEvent e) {
		 
        //Handle open button action.
        if (e.getSource() == openButton) {
            int returnVal = fc.showOpenDialog(SearchWaldoClient.this);
 
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                //This is where a real application would open the file.
                log.append("Opening: " + file.getName() + "." + newline);
            } else {
                log.append("Open command cancelled by user." + newline);
            }
            log.setCaretPosition(log.getDocument().getLength());
 
        //Handle submit button action.
        } else if (e.getSource() == submitButton) {
        	File file = fc.getSelectedFile();
            int returnVal = JOptionPane.showConfirmDialog(
            		dailogFrame, "Would you to search waldo in " + file.getName() + "?",
		                    "Confirm your selection",
		                    JOptionPane.YES_NO_OPTION);
        	
            
            if (returnVal == JOptionPane.YES_OPTION) {
                
            	
            	//File file = fc.getSelectedFile();
                
                //
                
                //This is where a real application would save the file.
                
                log.append("Submit: " + file.getName() + "." + newline);
                
               
            } else {
                log.append("Submit command cancelled by user." + newline);
            }
            
            
            
            log.setCaretPosition(log.getDocument().getLength());
        }
    }
	
		
	/** Returns an ImageIcon, or null if the path was invalid. */
    protected static ImageIcon createImageIcon(String path,
                                               String description) {
        java.net.URL imgURL = SearchWaldoClient.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL, description);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event dispatch thread.
     */
    private static void createAndShowGUI() {
    	
    	//Ask for window decorations provided by the look and feel.
    	JFrame.setDefaultLookAndFeelDecorated(true);
    			
        //Create and set up the window.
        JFrame frame = new JFrame("Wheres Waldo Search Application");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
        //Add content to the window.
        frame.add(new SearchWaldoClient());
 
        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }
    

    public static void main(String[] args) {
        //Schedule a job for the event dispatch thread:
        //creating and showing this application's GUI.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
        //Turn off metal's use of bold fonts
            UIManager.put("swing.boldMetal", Boolean.FALSE);
                 
        createAndShowGUI();
            }
        });
    }
    
    
    
    
    
    
       
    
    
    
	
	//select an image
	//submit the image
		//select one of the server randomly
		//try to open tcp/ip connection
		//if not successful go to the next server on the list until successful	
		//send the image to the server	
	//wait for the server to finish processing
	//monitor progress while waiting 
	//show progress in the progress bar
	//when done
	//receive image from the server
	//receive message from the server
	
	
	
	
	
	
}
