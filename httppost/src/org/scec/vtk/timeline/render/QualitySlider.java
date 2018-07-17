package org.scec.vtk.timeline.render;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

import com.google.common.base.Preconditions;

public class QualitySlider extends JPanel {
	
	private JSlider slider;
	
	/**
	 * Quality slider panel with a slider from 0 to 100, with the given default
	 * @param defaultVal
	 */
	public QualitySlider(int defaultVal) {
		Preconditions.checkState(defaultVal >= 0 && defaultVal <= 100);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		JLabel qualityLabel = new JLabel("Render Quality");
		add(qualityLabel);
		
		JPanel sliderPanel = new JPanel();
		sliderPanel.setLayout(new BoxLayout(sliderPanel, BoxLayout.X_AXIS));
		sliderPanel.add(new JLabel("Lowest"));
		slider = new JSlider(0, 100, defaultVal);
		sliderPanel.add(slider);
		sliderPanel.add(new JLabel("Highest"));
		add(sliderPanel);
	}
	
	/**
	 * Quality between 0 and 100 (inclusive)
	 * @return
	 */
	public int getValue() {
		return slider.getValue();
	}

}
