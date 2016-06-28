package org.scec.vtk.commons.opensha.gui.anim;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.scec.vtk.commons.opensha.faults.anim.FaultAnimation;
import org.scec.vtk.commons.opensha.faults.colorers.ColorerChangeListener;
import org.scec.vtk.commons.opensha.faults.colorers.FaultColorer;
import org.scec.vtk.commons.opensha.gui.ColorerPanel;

public class MultiAnimPanel extends JPanel implements ItemListener, ColorerChangeListener {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private CardLayout cl;
	
	private JPanel mainPanel;
	
	private HashMap<String, FaultAnimation> animMap;
	
	private JComboBox<String> combo;
	
	private ColorerPanel cp;
	
	public MultiAnimPanel(ArrayList<FaultAnimation> anims, AnimationListener l, ColorerPanel cp) {
		super(new BorderLayout());
		
		this.cp = cp;
		
		cl = new CardLayout();
		mainPanel = new JPanel();
		mainPanel.setLayout(cl);
		
		animMap = new HashMap<String, FaultAnimation>();
		
		boolean isAnimColorerSelected = false;
		for (FaultAnimation anim : anims) {
			animMap.put(anim.getName(), anim);
			mainPanel.add(new AnimationPanel(anim, l), anim.getName());
			if (anim.getFaultColorer() != null && cp.getSelectedColorer() == anim.getFaultColorer())
				isAnimColorerSelected = true;
		}
		
		combo = new JComboBox<String>((animMap.keySet().toArray(new String[0])));
		
		if (anims.size() > 1) {
			JPanel topPanel = new JPanel();
			topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
			topPanel.add(new JLabel("Animation: "));
			topPanel.add(combo);
			super.add(topPanel, BorderLayout.NORTH);
		}
		
		super.add(mainPanel, BorderLayout.CENTER);
		
		combo.addItemListener(this);
		
		if (isAnimColorerSelected)
			itemStateChanged(null);
		
		cp.addColorerChangeListener(this);
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		FaultAnimation anim = getSelectedAnim();
		cl.show(mainPanel, anim.getName());
		
		if (anim.getFaultColorer() != null) {
			cp.setSelectedColorer(anim.getFaultColorer());
			anim.fireRangeChangeEvent();
		}
	}
	
	public FaultAnimation getSelectedAnim() {
		String name = (String)combo.getSelectedItem();
		return animMap.get(name);
	}

	@Override
	public void colorerChanged(FaultColorer newColorer) {
		if (newColorer != null) {
			for (FaultAnimation anim : animMap.values()) {
				if (newColorer == anim.getFaultColorer()) {
					// the colorer panel just selected a fault anim colorer.
					
					// lets see if that anim is already selected
					if (!combo.getSelectedItem().equals(anim.getName())) {
						// this means the new colorer isn't the current anim, we should make it as such.
						combo.setSelectedItem(anim.getName());
					}
					break;
				}
			}
		}
	}

}
