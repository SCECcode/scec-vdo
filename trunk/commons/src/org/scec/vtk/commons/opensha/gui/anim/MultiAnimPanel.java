package org.scec.vtk.commons.opensha.gui.anim;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.scec.vtk.commons.opensha.faults.anim.FaultAnimation;
import org.scec.vtk.commons.opensha.faults.colorers.ColorerChangeListener;
import org.scec.vtk.commons.opensha.faults.colorers.FaultColorer;
import org.scec.vtk.commons.opensha.gui.ColorerPanel;
import org.scec.vtk.commons.opensha.gui.EventManager;
import org.scec.vtk.plugins.AnimatableChangeListener;
import org.scec.vtk.plugins.AnimatablePlugin;
import org.scec.vtk.plugins.Plugin;

import com.google.common.base.Preconditions;

public class MultiAnimPanel extends JPanel implements ItemListener, ColorerChangeListener, AnimationListener {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private CardLayout cl;
	
	private JPanel mainPanel;
	
	private HashMap<String, FaultAnimation> animMap;
	private HashMap<String, AnimationPanel> animPanelMap;
	
	private JComboBox<String> combo;
	
	private ColorerPanel cp;
	
	private List<AnimatableChangeListener> animChangeListeners = new ArrayList<>();
	
	private AnimationPanel animPanel;
	private AnimatablePlugin plugin;
	
	public MultiAnimPanel(Plugin plugin, EventManager em, ArrayList<FaultAnimation> anims, ColorerPanel cp) {
		super(new BorderLayout());
		
		this.cp = cp;
		
		cl = new CardLayout();
		mainPanel = new JPanel();
		mainPanel.setLayout(cl);
		
		animMap = new HashMap<>();
		animPanelMap = new HashMap<>();
		
		boolean isAnimColorerSelected = false;
		for (FaultAnimation anim : anims) {
			animMap.put(anim.getName(), anim);
			AnimationPanel animPanel = new AnimationPanel(plugin, em, anim);
			animPanelMap.put(anim.getName(), animPanel);
			animPanel.addAnimationListener(this);
			mainPanel.add(animPanel, anim.getName());
			if (anim.getFaultColorer() != null && cp.getSelectedColorer() == anim.getFaultColorer())
				isAnimColorerSelected = true;
		}
		
		combo = new JComboBox<String>((animMap.keySet().toArray(new String[0])));
		updateSelectedAnimPanel();
		
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
		updateSelectedAnimPanel();
	}
	
	public FaultAnimation getSelectedAnim() {
		String name = (String)combo.getSelectedItem();
		return animMap.get(name);
	}
	
	private void updateSelectedAnimPanel() {
		String name = (String)combo.getSelectedItem();
		animPanel = animPanelMap.get(name);
	}
	
	public void setSelectedAnim(FaultAnimation anim) {
		Preconditions.checkState(animMap.containsKey(anim.getName()));
		combo.setSelectedItem(anim.getName());
		updateSelectedAnimPanel();
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
	
	private FractionalTimeAnimator animator;
	
	public void animationStarted() {
		FaultAnimation anim = getSelectedAnim();
		if (anim.getFaultColorer() != null) {
			cp.setSelectedColorer(anim.getFaultColorer());
			anim.fireRangeChangeEvent();
		}
		animator = new FractionalTimeAnimator(animPanel, getSelectedAnim(), animPanel.getTimeCalc());
	}

	public void animationEnded() {
		animator = null;
	}

	public void animationTimeChanged(double fractionalTime) {
		Preconditions.checkNotNull(animator,
				"Animator is null! animationStarted() not called? Or animationEnded() called prematurely.");
		animator.goToTime(fractionalTime);
	}

	public boolean isAnimatable() {
		return getSelectedAnim().getNumSteps() > 0;
	}
	
	public void addAnimatableChangeListener(AnimatableChangeListener l) {
		animChangeListeners.add(l);
	}

	public void removeAnimatableChangeListener(AnimatableChangeListener l) {
		animChangeListeners.remove(l);
	}
	
	public void setPlugin(AnimatablePlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public void animationRangeChanged(FaultAnimation anim) {
		animator = null;
		
		// fire animatable change event
		boolean isAnimatable = isAnimatable();
		for (AnimatableChangeListener l : animChangeListeners)
			l.animatableChanged(plugin, isAnimatable);
	}

	@Override
	public void animationStepChanged(FaultAnimation anim) {
		// do nothing
	}

}
