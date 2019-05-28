package org.scec.vtk.main;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.scec.vtk.main.MainMenu;

import javax.swing.*;

import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;

@SuppressWarnings("serial")
public class Wizard extends JPanel {

	private JFrame frame;
   private static final String TITLE_TEXT = "Welcome to SCEC-VDO";
   private static final int TITLE_POINTS = 22;
   Boolean dontShow = false;
   public Wizard(final MainMenu mainMenu, final MainGUI mainGUI) {
	   
	   JPanel mainPanel = new JPanel();
	   mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
//	   mainPanel.setSize(370, 130);

	  JLabel titleLabel = new JLabel(TITLE_TEXT);
	   titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD,
	          TITLE_POINTS));

	   FlowLayout layout = new FlowLayout();
	   layout.setAlignment(FlowLayout.CENTER);
	   
	   JPanel southBtnPanel = new JPanel(layout);
	  southBtnPanel.setLayout(new GridLayout(2,1,10,10));
	  
	  
	  /*
		 * Anon inner class: AutoSaver
		 * purpose: save the state on a time increment
		 * 
		 * to change the time increment just change the Thread.sleep(yourNum) value
		 * 
		 * essetianally, this is just a thread off the GUI thread to do tasks while the GUI 
		 * runs smoothly. it could be a template to run any concurrent tasks... like loading files, assets, etc....
		 * 
		 * */
		class AutoSaver extends SwingWorker<Integer, Void>{
			
			@Override 
			public Integer doInBackground() {
				System.out.println("start of  dobackground()");
				  
				while(true){
					
					try {
						Thread.sleep(5000);
						System.out.println("AutoSave");
						mainMenu.autoSave();
						//mainMenu.autoSaveState();
					} catch (InterruptedException e) {
						
						e.printStackTrace();
					}
				}
			}

		}
	  
		final AutoSaver auto = new AutoSaver();
	   
	   //Open an existing file using function from mainMenu
	   JButton openButton = new JButton("Open Existing Project");
	   openButton.addActionListener(new ActionListener() {
		      public void actionPerformed(ActionEvent ae) {
		          mainMenu.open();
		          if(!dontShow)
		        	  auto.execute();
		        }
		    });

	   //Create (save) a file using function from mainMenu
	   JButton createButton = new JButton("Create a New Project");
	   createButton.addActionListener(new ActionListener() {
		      public void actionPerformed(ActionEvent ae) {
		    	  mainMenu.save();
		    	  //close frame after selection has been made
		          if(!dontShow)
		        	  auto.execute();
		      }
		    });
	   
	   JCheckBox checkBox = new JCheckBox("Never show this again");
	   checkBox.setLayout(new FlowLayout());
	   
	   checkBox.addActionListener(new ActionListener() {
		      public void actionPerformed(ActionEvent ae) {
//		    	  mainMenu.save();
//		    	  close frame after selection has been made
		    	  mainGUI.wizFrame.setVisible(false);
		    	  JOptionPane.showMessageDialog(
							frame,  "You have disabled the Wizard."
									+ "\nTo reactivate it, go to the 'Help' menu and click on 'Toggle Wizard'"
									+ "\n\nNote: AutoSave will also be disabled.");
		    	  
//		    	  frame.addActionListener(new ActionListener(){
//				public void actionPerformed(ActionEvent ae) {
		    	  MainMenu.Wizard = false;
		    	  mainMenu.updateWizard(false);
//		          mainGUI.wizFrame.setVisible(false);
//				} 
//		      });
		      }
		    });
	   
	   
	   southBtnPanel.add(openButton);
	   southBtnPanel.add(createButton);
	   southBtnPanel.add(checkBox, FlowLayout.RIGHT);

	   
      JPanel titlePanel = new JPanel();
      titlePanel.add(titleLabel);
      mainPanel.add(titlePanel);
      mainPanel.add(southBtnPanel);
      add(mainPanel);
   }
}