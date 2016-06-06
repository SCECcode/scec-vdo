package org.scec.geo3d.library.wgcep.gui.anim;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.EnumSet;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.opensha.commons.param.Parameter;
import org.opensha.commons.param.ParameterList;
import org.opensha.commons.param.impl.BooleanParameter;
import org.opensha.commons.param.impl.DoubleParameter;
import org.opensha.commons.param.impl.EnumParameter;
import org.opensha.commons.param.impl.StringParameter;
import org.scec.geo3d.library.wgcep.faults.anim.FaultAnimation;
import org.scec.geo3d.library.wgcep.faults.anim.IDBasedFaultAnimation;
import org.scec.geo3d.library.wgcep.faults.anim.TimeBasedFaultAnimation;
import org.scec.geo3d.library.wgcep.faults.faultSectionImpl.PrefDataSection;
import org.scec.geo3d.library.wgcep.tree.FaultSectionNode;
import org.opensha.commons.param.editor.impl.GriddedParameterListEditor;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;

public class AnimationPanel extends JPanel implements ChangeListener, ActionListener, FocusListener
//, RenderStepListener // TODO RenderStepListener
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private FaultAnimation faultAnim;

	private JSlider slider = new JSlider();

	private JLabel label = new JLabel();

	private JButton playButton = new JButton("Play");
	private JButton pauseButton = new JButton("Pause");
	private JButton nextButton = new JButton("Next");
	private JButton prevButton = new JButton("Prev");
	private AnimThread animThread;

	private static final String DURATION_PARAM_NAME = "Duration (seconds)";
	private static final Double DURATION_MIN = 1d;
	private static final Double DURATION_MAX = 720000d;
	private static final Double DURATION_DEFAULT = 30d;
	private DoubleParameter durationParam;

	private static final String ANIM_TYPE_PARAM_NAME = "Animation Type";
	private static final String ANIM_TYPE_TIME_BASED = "Time Based";
	private static final String ANIM_TYPE_EVEN = "Evely Spaced";
	private StringParameter animTypeParam;

	private static final String LOOP_PARAM_NAME = "Loop Animation";
	private static boolean LOOP_PARAM_DEFAULT = false;
	private BooleanParameter loopParam;

//	private static final String RENDER_PARAM_NAME = "Rendering Behaviour";
//	private static enum RenderOptions {
//		DO_NOTHING("Do Nothing"),
//		PLAY_START("Play From Start"),
//		PLAY_END("Play Align End"),
//		PLAY_MATCH_DURATION("Play Match Duration");
//		
//		private String name;
//		
//		private RenderOptions(String name) {
//			this.name = name;
//		}
//		
//		@Override
//		public String toString() {
//			return name;
//		}
//	}
//	private static RenderOptions RENDER_PARAM_DEFAULT = RenderOptions.DO_NOTHING;
//	private EnumParameter<RenderOptions> renderParam;
	
	private StepTimeCalculator renderTimeCalc;

	private ParameterList generalAnimParams;
	private ParameterList faultAnimParams;

	private JTextField idField = new JTextField(6);

	private AnimationListener l;
	
	

	public AnimationPanel(FaultAnimation faultAnim, AnimationListener l) {
		super.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
//		Geo3dInfo.getRenderEnabledCanvas().addRenderStepListener(this); // TODO

		this.faultAnim = faultAnim;
		this.l = l;
		faultAnim.addRangeChangeListener(this);

		// anim params
		durationParam = new DoubleParameter(DURATION_PARAM_NAME, DURATION_MIN, DURATION_MAX, DURATION_DEFAULT);

		ArrayList<String> strings = new ArrayList<String>();
		strings.add(ANIM_TYPE_EVEN);
		String defaultAnimType;
		if (faultAnim instanceof TimeBasedFaultAnimation) {
			strings.add(ANIM_TYPE_TIME_BASED);
			defaultAnimType = ANIM_TYPE_TIME_BASED;
		} else {
			defaultAnimType = ANIM_TYPE_EVEN;
		}
		animTypeParam = new StringParameter(ANIM_TYPE_PARAM_NAME, strings, defaultAnimType);

		loopParam = new BooleanParameter(LOOP_PARAM_NAME, LOOP_PARAM_DEFAULT);
		
//		renderParam = new EnumParameter<AnimationPanel.RenderOptions>(
//				RENDER_PARAM_NAME, EnumSet.allOf(RenderOptions.class), RENDER_PARAM_DEFAULT, null);

		generalAnimParams = new ParameterList();
		generalAnimParams.addParameter(durationParam);
		if (strings.size() > 1)
			generalAnimParams.addParameter(animTypeParam);
		generalAnimParams.addParameter(loopParam);
//		generalAnimParams.addParameter(renderParam);
		faultAnimParams = faultAnim.getAnimationParameters();

		slider.addChangeListener(this);
		updateSliderRange();

		JPanel labelWrap = new JPanel();
		labelWrap.add(label);
		label.setAlignmentX(Component.CENTER_ALIGNMENT);

		prevButton.setFont(prevButton.getFont().deriveFont(10f));
		playButton.setFont(playButton.getFont().deriveFont(10f));
		pauseButton.setFont(pauseButton.getFont().deriveFont(10f));
		nextButton.setFont(nextButton.getFont().deriveFont(10f));

		JPanel controlPanel = new JPanel();
		controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.X_AXIS));
		controlPanel.add(prevButton);
		controlPanel.add(playButton);
		controlPanel.add(pauseButton);
		controlPanel.add(nextButton);
		controlPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		playButton.addActionListener(this);
		pauseButton.addActionListener(this);
		nextButton.addActionListener(this);
		prevButton.addActionListener(this);

		if (faultAnim instanceof IDBasedFaultAnimation) {
			idField.setMaximumSize(idField.getPreferredSize());
			controlPanel.add(new JLabel("  ID: "));
			controlPanel.add(idField);
			idField.addActionListener(this);
			idField.addFocusListener(this);
		}

//		JPanel cpWrap = new JPanel();
//		cpWrap.add(controlPanel);

		this.add(slider);
		this.add(controlPanel);
		this.add(labelWrap);

		if (generalAnimParams != null && generalAnimParams.size() > 0) {
			GriddedParameterListEditor edit = new GriddedParameterListEditor(generalAnimParams);
			edit.setAlignmentX(Component.CENTER_ALIGNMENT);
			this.add(edit);
		}
		if (faultAnimParams != null && faultAnimParams.size() > 0) {
			GriddedParameterListEditor edit = new GriddedParameterListEditor(faultAnimParams);
			edit.setAlignmentX(Component.CENTER_ALIGNMENT);
			this.add(edit);
		}
	}

	private void updateSliderRange() {
		//		System.out.println("Updating slider range!");
		int max = faultAnim.getNumSteps();
		int min;
		if (max > 0) {
			min = 1;
		} else {
			min = 0;
		}
		l.animationRangeChanged(faultAnim);
		slider.setMinimum(min);
		int val = slider.getValue();
		slider.setMaximum(max);
		
		int preferred = faultAnim.getPreferredInitialStep();
		slider.setValue(preferred);
		if (val == preferred) {
			// setValue isn't goint to fire an event, so we do it manually
			updateStep();
		}
	}

	private void updateStep(){
		int step = slider.getValue()-1;
		faultAnim.setCurrentStep(step);
		String labelStr = "";
		if (faultAnim.includeStepInLabel())
			labelStr += "Frame "+slider.getValue()+"/"+slider.getMaximum();
		if (faultAnim instanceof TimeBasedFaultAnimation) {
			Double stepTime = ((TimeBasedFaultAnimation)faultAnim).getTimeForStep(step);
			if (!stepTime.isNaN() && stepTime >= 0)
				labelStr += ": " + getLabel(stepTime);
		}
		String custom = faultAnim.getCurrentLabel();
		if (custom != null && custom.length() > 0) {
			if (labelStr.length() > 0 && !custom.startsWith(" "))
				labelStr += " ";
			labelStr += custom;
		}
		if (faultAnim instanceof IDBasedFaultAnimation) {
			int id = ((IDBasedFaultAnimation)faultAnim).getIDForStep(step);
			idField.setText(""+id);
			labelStr += " (ID: " + id + ")";
		}
		
//		Geo3dInfo.getMainWindow().setMessage(labelStr); // TODO label support
		labelStr = wrapText(labelStr, 60);
		String info = labelStr;
		info.replace("<br>MoRate:", "MoRate:");
		labelStr = "<html>"+labelStr+"</html>";
		label.setToolTipText(labelStr);
			 
		
		
//		if (labelStr.length() > 70)
//			labelStr = labelStr.substring(0, 67)+"...";
		label.setText(labelStr);
		if (faultAnim.getNumSteps()>0)
			l.animationStepChanged(faultAnim);
		enableAnimControls();
	}

	private static final DecimalFormat df = new DecimalFormat("0.00");
	private static final double secsPerMin = 60d;
	private static final double secsPerHour = secsPerMin * 60d;
	private static final double secsPerDay = secsPerHour * 24d;
	private static final double secsPerYear = secsPerDay * 365d;

	private static String getLabel(double secs) {
		if (secs < secsPerMin)
			return df.format(secs)+" secs";
		if (secs < secsPerHour)
			return df.format(secs / secsPerMin)+" mins";
		if (secs < secsPerDay)
			return df.format(secs / secsPerHour)+" hours";
		if (secs < secsPerYear)
			return df.format(secs / secsPerDay)+" days";
		return df.format(secs / secsPerYear)+" years";
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() == faultAnim) {
			updateSliderRange();
		} else if (e.getSource() == slider) {
			updateStep();
		}
	}

	private synchronized void enableAnimControls() {
		boolean isFirst = slider.getValue() == slider.getMinimum();
		boolean isLast = slider.getValue() == slider.getMaximum();
		boolean playing = animThread != null && animThread.isAlive() && !isLast;
		slider.setEnabled(!playing);
		playButton.setEnabled(!playing);
		pauseButton.setEnabled(playing);
		if (generalAnimParams != null) {
			for (Parameter<?> param : generalAnimParams) {
				param.getEditor().setEnabled(!playing);
			}
		}
		if (faultAnimParams != null) {
			for (Parameter<?> param : faultAnimParams) {
				param.getEditor().setEnabled(!playing);
			}
		}
		idField.setEnabled(!playing);
		nextButton.setEnabled(!playing && !isLast);
		prevButton.setEnabled(!playing && !isFirst);
	}

	private StepTimeCalculator getTimeCalc() {
		return getTimeCalc(durationParam.getValue());
	}
	
	private StepTimeCalculator getTimeCalc(double duration) {
		int maxStep = slider.getMaximum();
		String selectedType = animTypeParam.getValue();
		if (selectedType.equals(ANIM_TYPE_TIME_BASED)) {
			return new TimeBasedCalc((TimeBasedFaultAnimation)faultAnim, maxStep, duration);
		} else if (selectedType.equals(ANIM_TYPE_EVEN)) {
			return new EvenlySpacedCalc(maxStep, duration);
		}
		throw new RuntimeException("Unknown anim type: " + selectedType);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(playButton)) {
			animThread = new AnimThread(slider, getTimeCalc());
			animThread.setLoop(loopParam.getValue());
			animThread.start();
			enableAnimControls();
		} else if (e.getSource().equals(pauseButton)) {
			if (animThread != null) {
				animThread.pause();
			}
			while (animThread.isAlive());
			enableAnimControls();
		} else if (e.getSource().equals(nextButton)) {
			slider.setValue(slider.getValue()+1);
		} else if (e.getSource().equals(prevButton)) {
			slider.setValue(slider.getValue()-1);
		} else if (e.getSource().equals(idField)) {
			//			System.out.println("idField: action performed");
			idFieldUpdated();
		}
	}

	@Override
	public void focusGained(FocusEvent focusevent) {}

	@Override
	public void focusLost(FocusEvent focusevent) {
		//		System.out.println("idField: focus lost");
		idFieldUpdated();
	}

	private void idFieldUpdated() {
		String idFieldVal = idField.getText();
		try {
			int id = Integer.parseInt(idFieldVal);
			int step = ((IDBasedFaultAnimation)faultAnim).getStepForID(id)+1; // +1 because colorer steps are 0-based
			slider.setValue(step);
		} catch (Exception e) {
			// couldn't parse, reset it to the current ID
			idField.setText(((IDBasedFaultAnimation)faultAnim).getIDForStep(slider.getValue()-1)+"");
		}
	}

	private static String wrapText (String text, int len)
	{
		// return empty string for null text
		if (text == null)
			return "";

		// return text if len is zero or less
		if (len <= 0)
			return text;

		// return text if less than length
		if (text.length() <= len)
			return text;

		char [] chars = text.toCharArray();
		ArrayList<String> lines = new ArrayList<String>();
		StringBuffer line = new StringBuffer();
		StringBuffer word = new StringBuffer();

		for (int i = 0; i < chars.length; i++) {
			word.append(chars[i]);

			if (chars[i] == ' ') {
				if ((line.length() + word.length()) > len) {
					lines.add(line.toString());
					line.delete(0, line.length());
				}

				line.append(word);
				word.delete(0, word.length());
			}
		}

		// handle any extra chars in current word
		if (word.length() > 0) {
			if ((line.length() + word.length()) > len) {
				lines.add(line.toString());
				line.delete(0, line.length());
			}
			line.append(word);
		}

		// handle extra line
		if (line.length() > 0) {
			lines.add(line.toString());
		}

		String ret = null;
		for (String str : lines) {
			if (ret == null)
				ret = "";
			else
				ret += "<br>";
			ret += str;
		}
		return ret;
	}

//	@Override
//	public void renderStarted() {
//		if (renderParam.getValue() != RenderOptions.DO_NOTHING) {
//			// slider is 1-based
//			slider.setValue(1);
//			renderTimeCalc = null;
//		}
//	}
//
//	@Override
//	public void renderStopped() {
//		renderTimeCalc = null;
//	}
//
//	@Override
//	public void renderFrameToBeProcessed(long frameIndex, long totalNumFrames,
//			double fps) {
//		double animationDuration = (double)totalNumFrames/fps;
//		
//		int startFrame;
//		double playbackDuration;
//		
//		switch (renderParam.getValue()) {
//		case DO_NOTHING:
//			// do nothing
//			return;
//		case PLAY_START:
//			startFrame = 0;
//			playbackDuration = durationParam.getValue();
//			break;
//		case PLAY_END:
//			playbackDuration = durationParam.getValue();
//			double durationDelta = animationDuration - playbackDuration;
//			startFrame = (int)(durationDelta*fps);
//			break;
//		case PLAY_MATCH_DURATION:
//			startFrame = 0;
//			playbackDuration = animationDuration;
//			break;
//
//		default:
//			throw new IllegalStateException("Unknown render type: "+renderParam.getValue());
//		}
//		
//		if (renderTimeCalc == null)
//			renderTimeCalc = getTimeCalc(playbackDuration);
//		
//		int stepToSet;
//		if (frameIndex < startFrame) {
//			stepToSet = 1;
//		} else {
//			long numFramesIn = frameIndex - startFrame;
//			double curTimeSec = (double)numFramesIn/fps;
//			long curMillis = (long)(curTimeSec * 1000d);
//			stepToSet = renderTimeCalc.getStepForTime(slider.getValue(), curMillis);
//		}
//		if (stepToSet < slider.getMinimum())
//			stepToSet = slider.getMinimum();
//		if (stepToSet > slider.getMaximum())
//			stepToSet = slider.getMaximum();
//		slider.setValue(stepToSet);
//	}

}
