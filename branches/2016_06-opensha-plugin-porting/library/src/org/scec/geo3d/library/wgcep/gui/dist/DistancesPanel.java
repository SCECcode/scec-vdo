package org.scec.geo3d.library.wgcep.gui.dist;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

//import javax.media.j3d.Appearance;
//import javax.media.j3d.BranchGroup;
//import javax.media.j3d.ColoringAttributes;
//import javax.media.j3d.PointArray;
//import javax.media.j3d.PointAttributes;
//import javax.media.j3d.Shape3D;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.tree.TreeNode;

import org.opensha.commons.geo.Location;
import org.opensha.commons.param.constraint.impl.StringConstraint;
import org.opensha.commons.param.event.ParameterChangeEvent;
import org.opensha.commons.param.event.ParameterChangeListener;
import org.opensha.commons.param.impl.DoubleParameter;
import org.opensha.commons.param.impl.StringParameter;
import org.opensha.refFaultParamDb.calc.sectionDists.FaultSectDistRecord;
import org.scec.geo3d.library.wgcep.faults.AbstractFaultSection;
import org.scec.geo3d.library.wgcep.tree.AbstractFaultNode;
import org.scec.geo3d.library.wgcep.tree.events.CustomColorSelectionListener;
import org.scec.geo3d.library.wgcep.tree.events.TreeChangeListener;

public class DistancesPanel extends JPanel implements ActionListener, ParameterChangeListener, TreeChangeListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final String FAULT_CONNECTIONS_PARAM_NAME = "Fault Connections";
	private static final String FAULT_CONNECTIONS_PARAM_DEFAULT = "None";
	private StringParameter faultConnectionsParam;
	private JButton loadFaultConnectionsButton;
	private JButton exportFaultConnButton;
	private DoubleParameter faultConnectionsDistParam;
	private static final String FAULT_CONNECTIONS_DIST_PARAM_NAME = "Fault Distance Cutoff";
	private static final Double FAULT_CONNECTIONS_DIST_PARAM_DEFAULT = 10.0;
	private static final Double FAULT_CONNECTIONS_DIST_PARAM_MIN = 0.0;
	private static final Double FAULT_CONNECTIONS_DIST_PARAM_MAX = 100.0;
//	private BranchGroup closestPtBranchGroup;
	
	private ArrayList<NamedDistRecord> faultDistRecords;
	private ArrayList<String> faultDistNames;

	// sorters
	private static final String SORT_PARAM_NAME = "Sort by ";
	private static final String SORT_DISTANCES = "Min Distance";
	private static final String SORT_SLIP = "Avg Slip";
	private static final String SORT_DEFAULT = SORT_DISTANCES;
	private StringParameter sortParam;

//	private BranchGroup pluginBranchGroup;

	private DistanceCalc distCalc;
	
	private static final Color SELECTED_COLOR_1 = Color.GREEN;
	private static final Color SELECTED_COLOR_2 = Color.CYAN;
	
	private VisibleFaultSurfacesProvider surfaceProv;
	private CustomColorSelectionListener customColorListener;

	public DistancesPanel(VisibleFaultSurfacesProvider surfaceProv,
			CustomColorSelectionListener customColorListener) {
		this.customColorListener = customColorListener;
		distCalc = new DistanceCalc(surfaceProv);
		this.surfaceProv = surfaceProv;
//		this.pluginBranchGroup = pluginBranchGroup;

		ArrayList<String> allowedStrings = new ArrayList<String>();
		allowedStrings.add(FAULT_CONNECTIONS_PARAM_DEFAULT);
		faultConnectionsParam = new StringParameter(FAULT_CONNECTIONS_PARAM_NAME, allowedStrings, FAULT_CONNECTIONS_PARAM_DEFAULT);
		faultConnectionsDistParam = new DoubleParameter(FAULT_CONNECTIONS_DIST_PARAM_NAME,
				FAULT_CONNECTIONS_DIST_PARAM_MIN, FAULT_CONNECTIONS_DIST_PARAM_MAX, FAULT_CONNECTIONS_DIST_PARAM_DEFAULT);

		ArrayList<String>recordComparatorNames = new ArrayList<String>();
		recordComparatorNames.add(SORT_DISTANCES);
		recordComparatorNames.add(SORT_SLIP);
		sortParam = new StringParameter(SORT_PARAM_NAME, recordComparatorNames, SORT_DEFAULT);
		sortParam.addParameterChangeListener(this);

		loadFaultConnectionsButton = new JButton("Update");
		loadFaultConnectionsButton.addActionListener(this);
		exportFaultConnButton = new JButton("Export");
		exportFaultConnButton.addActionListener(this);
		exportFaultConnButton.setEnabled(false);

		JPanel connectionTopPanel = new JPanel();
		connectionTopPanel.setLayout(new BoxLayout(connectionTopPanel, BoxLayout.X_AXIS));

		connectionTopPanel.add(faultConnectionsDistParam.getEditor().getComponent());
		connectionTopPanel.add(sortParam.getEditor().getComponent());
		JPanel connectionButtonsPanel = new JPanel();
		connectionButtonsPanel.setLayout(new BoxLayout(connectionButtonsPanel, BoxLayout.Y_AXIS));
		connectionButtonsPanel.add(loadFaultConnectionsButton);
		connectionButtonsPanel.add(exportFaultConnButton);
		connectionTopPanel.add(connectionButtonsPanel);

		faultConnectionsParam.addParameterChangeListener(this);

		// TODO
//		closestPtBranchGroup = new BranchGroup();
//		closestPtBranchGroup.setCapability(BranchGroup.ALLOW_DETACH);
//		closestPtBranchGroup.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
//		closestPtBranchGroup.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);

		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		this.add(connectionTopPanel);
		this.add(faultConnectionsParam.getEditor().getComponent());
	}

	private void sortFaultConnections() {
		String sortType = sortParam.getValue();
		if (sortType.equals(SORT_DISTANCES)) {
			distCalc.sortByDistance();
		} else if (sortType.equals(SORT_SLIP)) {
			distCalc.sortBySlip();
		} else {
			throw new RuntimeException("Unknown sort type: " + sortType);
		}
	}

	private void updateConnectionsParam() {
		faultDistRecords = distCalc.getRecords();
		ArrayList<String> allowedStrings = new ArrayList<String>();
		allowedStrings.add(FAULT_CONNECTIONS_PARAM_DEFAULT);

		boolean hasRecords = faultDistRecords != null && faultDistRecords.size() > 0;
		faultDistNames = new ArrayList<String>();

		if (hasRecords) {
			for (NamedDistRecord record : faultDistRecords) {
				allowedStrings.add(record.getName());
				faultDistNames.add(record.getName());
			}
		}

		faultConnectionsParam.setValue(FAULT_CONNECTIONS_PARAM_DEFAULT);
		StringConstraint sconst = (StringConstraint)faultConnectionsParam.getConstraint();
		sconst.setStrings(allowedStrings);

		faultConnectionsParam.getEditor().setParameter(faultConnectionsParam);
		exportFaultConnButton.setEnabled(hasRecords);
	}

	private void updateFaultConnections() throws InterruptedException {
		distCalc.updateFaultConnections(faultConnectionsDistParam.getValue());
		sortFaultConnections();
		updateConnectionsParam();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(loadFaultConnectionsButton)) {
			try {
				updateFaultConnections();
			} catch (InterruptedException e1) {
				throw new RuntimeException(e1);
			}
		} else if (e.getSource().equals(exportFaultConnButton)) {
			JFileChooser chooser = new JFileChooser();
			int ret = chooser.showSaveDialog(this);
			if (ret == JFileChooser.APPROVE_OPTION) {
				File file = chooser.getSelectedFile();
				try {
					FileWriter fw = new FileWriter(file);
					fw.write("\"name1\",id1,\"name2\",id2,minDist,maxDist" + "\n");
					for (NamedDistRecord nrecord : faultDistRecords) {
						String name1 = nrecord.getFault1().getName();
						String name2 = nrecord.getFault2().getName();
						FaultSectDistRecord record = nrecord.getRecord();
						fw.write("\"" + name1 + "\"," + record.getID1()
								+ ",\"" + name2 + "\"," + record.getID2()
								+ "," + record.getMinDist() + "," + record.getMaxDist() + "\n");
					}
					fw.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
	}
	
	// TODO
//	private void highlightClosestPoint(FaultSectDistRecord record) {
//		int[] indices1 = record.getMinDistLoc1();
//		int[] indices2 = record.getMinDistLoc2();
//		Location loc1 = record.getSurface1().get(indices1[0], indices1[1]);
//		Location loc2 = record.getSurface2().get(indices2[0], indices2[1]);
//
//		Color3f color3f = new Color3f(Color.white);
//
//		Point3f[] endPoints = new Point3f[2];
//		endPoints[0] = new Point3f(LatLongToPoint.plotPoint(loc1.getLatitude(), loc1.getLongitude(), -loc1.getDepth()));
//		endPoints[1] = new Point3f(LatLongToPoint.plotPoint(loc2.getLatitude(), loc2.getLongitude(), -loc2.getDepth()));
//		PointArray endPointArray =  new PointArray(endPoints.length, PointArray.COORDINATES |
//				PointArray.COLOR_3);
//		endPointArray.setCoordinates(0, endPoints);
//		endPointArray.setColor(0,color3f);
//		endPointArray.setColor(1,color3f);
//		
//		PointAttributes pa = new PointAttributes(5f, true);
//		
//		ColoringAttributes ca = new ColoringAttributes(color3f, ColoringAttributes.SHADE_FLAT);
//		ca.setCapability(ColoringAttributes.ALLOW_COLOR_READ);
//		ca.setCapability(ColoringAttributes.ALLOW_COLOR_WRITE);
//		
//		Appearance app = new Appearance();
//		app.setColoringAttributes(ca);
//		app.setPointAttributes(pa);
//		Shape3D endPointShape = new Shape3D(endPointArray, app);
//		closestPtBranchGroup.addChild(endPointShape);
//		pluginBranchGroup.addChild(closestPtBranchGroup);
//	}
//	
//	private AbstractFaultNode highlightedNode1;
//	private Color highlightedNode1PrevColor;
//	private AbstractFaultNode highlightedNode2;
//	private Color highlightedNode2PrevColor;
//	
//	private void showRecord(NamedDistRecord record) {
//		AbstractFaultSection fault1 = record.getFault1();
//		AbstractFaultSection fault2 = record.getFault2();
//		
//		highlightedNode1 = surfaceProv.getNode(fault1);
//		highlightedNode2 = surfaceProv.getNode(fault2);
//		
//		highlightedNode1PrevColor = highlightedNode1.getColor();
//		highlightedNode1.setColor(SELECTED_COLOR_1);
//		highlightedNode2PrevColor = highlightedNode2.getColor();
//		highlightedNode2.setColor(SELECTED_COLOR_2);
//		customColorListener.customColorSelected();
//		
//		highlightedNode1.setVisible(true);
//		highlightedNode2.setVisible(true);
//		
//		highlightClosestPoint(record.getRecord());
//	}
	
	private void rollBackColor(AbstractFaultNode highlightedNode, Color highlightedNodePrevColor, Color highlightColor) {
		if (highlightedNode != null
				&& highlightedNodePrevColor != null
				&& highlightedNode.getColor().equals(highlightColor)) {
			highlightedNode.setColor(highlightedNodePrevColor);
		}
	}
	
	private void unHilight() {
		// TODO
//		if (closestPtBranchGroup.isLive())
//			closestPtBranchGroup.detach();
//		closestPtBranchGroup.removeAllChildren();
//		rollBackColor(highlightedNode1, highlightedNode1PrevColor, SELECTED_COLOR_1);
//		highlightedNode1 = null;
//		highlightedNode1PrevColor = null;
//		rollBackColor(highlightedNode2, highlightedNode2PrevColor, SELECTED_COLOR_2);
//		highlightedNode2 = null;
//		highlightedNode2PrevColor = null;
	}

	@Override
	public void parameterChange(ParameterChangeEvent event) {
		if (event.getParameterName().equals(FAULT_CONNECTIONS_PARAM_NAME)) {
			String name = faultConnectionsParam.getValue();
			unHilight();
			// TODO
//			if (!name.equals(FAULT_CONNECTIONS_PARAM_DEFAULT)) {
//				int index = faultDistNames.indexOf(name);
//				NamedDistRecord record = faultDistRecords.get(index);
//				showRecord(record);
//			}
		} else if (event.getParameterName().equals(SORT_PARAM_NAME)) {
			unHilight();
			sortFaultConnections();
			updateConnectionsParam();
		}
	}

	@Override
	public void treeChanged(TreeNode newRoot) {
		unHilight();
		faultDistRecords = null;
		distCalc.clear();
		updateConnectionsParam();
		unHilight();
	}

}

