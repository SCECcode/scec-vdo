package org.scec.vtk.plugins.SurfacePlugin;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.scec.vtk.plugins.SurfacePlugin.Component.LoadedFilesProperties;
import org.scec.vtk.plugins.utils.components.AddButton;
import org.scec.vtk.plugins.utils.components.EditButton;
import org.scec.vtk.plugins.utils.components.RemoveButton;
import org.scec.vtk.plugins.utils.components.ShowButton;
import org.scec.vtk.tools.Prefs;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class ImagePluginGUI  extends JPanel implements ActionListener, ChangeListener, ListSelectionListener {
		
		private static final long serialVersionUID = 1L;
		private JPanel mainPanel = new JPanel();
		private JPanel topPanel = new JPanel();
		private JPanel bottomPanel = new JPanel();
		private JPanel sliderPanel = new JPanel();
		private JPanel panesPanel = new JPanel();
		private JScrollPane displayedImagePane = new JScrollPane();
		private JScrollPane displayedSurfacePane = new JScrollPane();
		private Vector displayedSetVector = new Vector();
		private Vector displayedSetInfoVector = new Vector();
		protected JList displayedImageList = new JList();
		protected JList displayedSurfaceList = new JList();
		private JPanel labelPanel = new JPanel();
		private JPanel defaultPanel = new JPanel();
		private JPanel checkboxPanel = new JPanel();
		private JScrollPane defaultScrollPane = new JScrollPane(checkboxPanel);
		private JLabel defaultSurfaces = new JLabel("Default Surfaces:");
		private JLabel imageLabel = new JLabel("Image(s)");
		private JLabel surfaceLabel = new JLabel("Surface(s)");
		private JCheckBox wm=new JCheckBox("World Map");
		private JCheckBox sc=new JCheckBox("Southern California Map");
		private JCheckBox cm=new JCheckBox("California Map");
		private JCheckBox jm=new JCheckBox("Japan Map");
		private JCheckBox nz=new JCheckBox("New Zealand Map");
		private JCheckBox im=new JCheckBox("Indonesia Map");
		private JCheckBox hm=new JCheckBox("Haiti Map");
		private JCheckBox mm=new JCheckBox("Mexico Map");
		private JCheckBox sa=new JCheckBox("South America Map");
		private JCheckBox cd=new JCheckBox("California DEM");
		private JCheckBox cdc=new JCheckBox("California DEM Colored");

		//private ButtonGroup bg=new ButtonGroup();

		private JLabel addRemLabel = new JLabel("Add / Remove ");
		protected RemoveButton   remISButton;
		//private SaveButton     saveISButton;
		private AddButton   newISButton;
		private EditButton editButton;
		protected ShowButton showImageButton;
		//private JSlider transparencySlider = new JSlider(); //Note: transparency slider works for .png files but not .jpg
		protected SurfacePluginGUI parent;
		//private MapSetLoadPluginGUI mslg;
		private MapSetCreatePluginGUI mscpg;
		//private MapSetEditPluginGUI lol;
		//private LoadedFilesProperties lfp;
		
		private JButton displayButton = new JButton("Display");
		
		private boolean surfaceFile = false;
		private boolean imageFile = false;
		
		//private Appearance ap2 = new Appearance();
		protected int imageIndex = -1;
		protected int surfaceIndex = -1;
		protected int scIndex= -1;
		protected int wmIndex= -1;
		protected int cmIndex= -1;
		protected int jmIndex= -1;
		protected int nzIndex= -1;
		protected int imIndex= -1;
		protected int hmIndex= -1;
		protected int mmIndex= -1;
		protected int saIndex= -1;
		protected int cdIndex= -1;
		protected int cdcIndex= -1;
		private double[] imageData;

		//private final int MAX_FILENAME_LENGTH = 30;
		//private final int MAX_COORDINATE_LENGTH = 20;
		//private final int MAX_DEPTH_LENGTH = 10;

		public ImagePluginGUI(SurfacePluginGUI sp) {

			//JScrollPane scrollPane = new JScrollPane(displayedImageList);
			//JScrollPane surfaceScrollPane = new JScrollPane(displayedSurfaceList);
			parent = sp;
			//parent.getImageSurfaceVector().get(0).set
			editButton = new EditButton(this, "Edit surface/image");
			newISButton = new AddButton(this,"Add a new surface/image");
			remISButton = new RemoveButton(this,"Remove selected surface/image");
			showImageButton = new ShowButton(this,"Toggle visibility of selected image(s)");

			displayedImageList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			displayedImageList.addListSelectionListener(this);
			displayedImageList.setEnabled(true);
			displayedImagePane.getViewport().setView(displayedImageList);

			displayedSurfaceList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			displayedSurfaceList.addListSelectionListener(this);
			displayedSurfacePane.getViewport().setView(displayedSurfaceList);


			labelPanel.setLayout(new GridLayout(1,2,10,10));
			labelPanel.add(imageLabel);
			labelPanel.add(surfaceLabel);
			panesPanel.setLayout(new GridLayout(1,2,10,10));
			panesPanel.add(displayedImagePane);
			panesPanel.add(displayedSurfacePane);
			topPanel.setLayout(new BorderLayout());
			topPanel.add(labelPanel, BorderLayout.NORTH);

			topPanel.add(panesPanel, BorderLayout.CENTER);

			JPanel bar = new JPanel();
			bar.setBorder(BorderFactory.createEmptyBorder(3,0,0,0));
			bar.setLayout(new BoxLayout(bar,BoxLayout.LINE_AXIS));
			bar.setOpaque(true);
			int buttonSpace = 3;

			bar.add(editButton);
			bar.add(this.showImageButton);
			bar.add(Box.createHorizontalGlue());
			bar.add(addRemLabel);
			bar.add(this.newISButton);
			bar.add(Box.createHorizontalStrut(buttonSpace));
			bar.add(this.remISButton);
			bar.add(Box.createHorizontalStrut(buttonSpace));
			editButton.setEnabled(false);
			newISButton.setEnabled(true);
			bottomPanel.setLayout(new BorderLayout());
			bottomPanel.add(sliderPanel, BorderLayout.NORTH);
			bottomPanel.add(bar, BorderLayout.SOUTH);

			/*transparencySlider.setMajorTickSpacing(10);
			transparencySlider.setMinorTickSpacing(5);
			transparencySlider.setPaintLabels(true); 
			transparencySlider.setPaintTicks(true);
			transparencySlider.addChangeListener(this);
			//transparencySlider.setToolTipText("Set transparency level (0 = Opaque; 100 = Transparent)");  to get rid of tool tip text so it doesnt interfere with rending
			transparencySlider.setEnabled(false);

			sliderPanel.setLayout(new BorderLayout());
			sliderPanel.add(new JLabel("Transparency"),BorderLayout.NORTH);
			sliderPanel.add(transparencySlider,BorderLayout.CENTER);*/


			wm.addActionListener(this);
			sc.addActionListener(this);
			cm.addActionListener(this);
			jm.addActionListener(this);
			nz.addActionListener(this);
			im.addActionListener(this);
			hm.addActionListener(this);
			mm.addActionListener(this);
			sa.addActionListener(this);
			cd.addActionListener(this);
			cdc.addActionListener(this);
			defaultPanel.add(defaultSurfaces);
			defaultPanel.setLayout(new BoxLayout(defaultPanel,BoxLayout.Y_AXIS));
			defaultPanel.setPreferredSize(new Dimension(300,120));
			defaultScrollPane.setPreferredSize(new Dimension(300,120));
			checkboxPanel.add(cm);
			checkboxPanel.add(sc);
			checkboxPanel.add(cd);
			checkboxPanel.add(cdc);
			checkboxPanel.add(wm);
			checkboxPanel.add(mm);
			checkboxPanel.add(sa);
			checkboxPanel.add(jm);
			checkboxPanel.add(im);
			checkboxPanel.add(nz);
			checkboxPanel.add(hm);
			checkboxPanel.setLayout(new GridLayout(0,2));
			defaultPanel.add(defaultScrollPane);


			mainPanel.setLayout(new BorderLayout(5, 5));
			mainPanel.add(topPanel, BorderLayout.CENTER);
			mainPanel.add(bottomPanel, BorderLayout.SOUTH);
			mainPanel.add(defaultPanel,BorderLayout.NORTH);
			mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
			mainPanel.setPreferredSize(new Dimension(Prefs.getPluginWidth(), Prefs.getPluginHeight()/2));

		}
		
		public JPanel getPanel() {
			return mainPanel;
		}

		@Override
		public void valueChanged(ListSelectionEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void changed(ObservableValue arg0, Object arg1, Object arg2) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			Object source = e.getSource();
			if (source == newISButton) {
				mscpg = new MapSetCreatePluginGUI(this);
			}
		}

		public SurfacePluginGUI getImagePluginGUIParent() {
			// TODO Auto-generated method stub
			return parent;
		}
}
