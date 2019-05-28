package org.scec.vtk.commons.opensha.gui.anim;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.opensha.commons.param.Parameter;
import org.opensha.commons.param.ParameterList;
import org.opensha.commons.param.editor.impl.GriddedParameterListEditor;
import org.opensha.commons.param.event.ParameterChangeEvent;
import org.opensha.commons.param.event.ParameterChangeListener;
import org.opensha.commons.param.impl.BooleanParameter;
import org.opensha.commons.param.impl.DoubleParameter;
import org.opensha.commons.param.impl.EnumParameter;
import org.opensha.commons.param.impl.StringParameter;
import org.opensha.commons.util.ExceptionUtils;
import org.scec.vtk.commons.legend.LegendItem;
import org.scec.vtk.commons.legend.LegendUtils;
import org.scec.vtk.commons.opensha.faults.anim.FaultAnimation;
import org.scec.vtk.commons.opensha.faults.anim.IDBasedFaultAnimation;
import org.scec.vtk.commons.opensha.faults.anim.TimeBasedFaultAnimation;
import org.scec.vtk.commons.opensha.gui.EventManager;
import org.scec.vtk.main.Info;
import org.scec.vtk.main.MainGUI;
import org.scec.vtk.plugins.Plugin;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;

import vtk.vtkTextActor;

public class AnimationPanel extends JPanel implements ChangeListener, ActionListener, FocusListener, ParameterChangeListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static final boolean D = false;

	private FaultAnimation faultAnim;

	/**
	 * The number of slider ticks for a time based animation
	 */
	private static final int SLIDER_NUM_TIME_BASED = 10000;
	private JSlider slider = new JSlider();

	private JLabel label = new JLabel();

	private JButton playButton = new JButton("Play");
	private JButton pauseButton = new JButton("Pause");
	private JButton nextButton = new JButton("Next");
	private JButton prevButton = new JButton("Prev");
	private AnimThread animThread;
	
	private JCheckBox legendCheck = new JCheckBox("Legend", false);
	private JCheckBox legendStepCheck = new JCheckBox("Step", true);
	private JCheckBox legendTimeCheck = new JCheckBox("Time", true);
	private JCheckBox legendRelativeTimeCheck = new JCheckBox("Relative", false);
	private JCheckBox legendIDCheck = new JCheckBox("ID", false);
	private JCheckBox legendLabelCheck = new JCheckBox("Label", false);
	private LegendItem legend;

	private static final String DURATION_PARAM_NAME = "Duration (seconds)";
	private static final Double DURATION_MIN = 1d;
	private static final Double DURATION_MAX = 720000d;
	private static final Double DURATION_DEFAULT = 30d;
	private DoubleParameter durationParam;

	private enum AnimType {
		TIME_BASED("Time Based"),
		EVENLY_SPACED("Evenly Spaced");
		
		private String name;
		private AnimType(String name) {
			this.name = name;
		}
		@Override
		public String toString() {
			return name;
		}
	}
	private static final String ANIM_TYPE_PARAM_NAME = "Animation Type";
	private EnumParameter<AnimType> animTypeParam;

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
	
	private StepTimeCalculator timeCalc;
	/**
	 * this is the current step, 0-based and less then faultAnim.getNumSteps()
	 */
	private int curStep;
	/**
	 * this is the current slider value, 1-based
	 */
	private int curSliderVal;
	/**
	 * this is the current animation time in seconds, up to the playback duration
	 */
	private double curAnimTime;
	/**
	 * this is the current absolute time in seconds if we have a time based animation, which is somewhere between the time of
	 * the first step and that time + the duration. This time windows doesn't need to start at zero and can be negative. 
	 */
	private double curAbsTime;

	private ParameterList generalAnimParams;
	private ParameterList faultAnimParams;

	private JTextField idField = new JTextField(6);

	private List<AnimationListener> listeners = new ArrayList<>();
	
	private Plugin plugin;
	private EventManager em;

	public AnimationPanel(Plugin plugin, EventManager em, FaultAnimation faultAnim) {
		super.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		this.plugin = plugin;
		this.em = em;
		addAnimationListener(em);
		this.faultAnim = faultAnim;
		faultAnim.addRangeChangeListener(this);

		// anim params
		durationParam = new DoubleParameter(DURATION_PARAM_NAME, DURATION_MIN, DURATION_MAX, DURATION_DEFAULT);
		durationParam.addParameterChangeListener(this);

		EnumSet<AnimType> animTypes;
		AnimType defaultAnimType;
		if (faultAnim instanceof TimeBasedFaultAnimation) {
			animTypes = EnumSet.allOf(AnimType.class);
			defaultAnimType = AnimType.TIME_BASED;
		} else {
			animTypes = EnumSet.of(AnimType.EVENLY_SPACED);
			defaultAnimType = AnimType.EVENLY_SPACED;
		}
		animTypeParam = new EnumParameter<AnimationPanel.AnimType>(ANIM_TYPE_PARAM_NAME, animTypes, defaultAnimType, null);
		animTypeParam.addParameterChangeListener(this);

		loopParam = new BooleanParameter(LOOP_PARAM_NAME, LOOP_PARAM_DEFAULT);
		
//		renderParam = new EnumParameter<AnimationPanel.RenderOptions>(
//				RENDER_PARAM_NAME, EnumSet.allOf(RenderOptions.class), RENDER_PARAM_DEFAULT, null);

		generalAnimParams = new ParameterList();
		generalAnimParams.addParameter(durationParam);
		if (animTypes.size() > 1)
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
		
		// legend
		JPanel legendPanel = new JPanel();
		legendPanel.setLayout(new BoxLayout(legendPanel, BoxLayout.X_AXIS));
		legendPanel.add(legendCheck);
		legendCheck.addActionListener(this);
		legendStepCheck.setSelected(!(faultAnim instanceof TimeBasedFaultAnimation));
		legendPanel.add(legendStepCheck);
		legendStepCheck.addActionListener(this);
		if (faultAnim instanceof TimeBasedFaultAnimation) {
			legendPanel.add(legendTimeCheck);
			legendTimeCheck.addActionListener(this);
			legendPanel.add(legendRelativeTimeCheck);
			legendRelativeTimeCheck.addActionListener(this);
		}
		if (faultAnim instanceof IDBasedFaultAnimation) {
			legendPanel.add(legendIDCheck);
			legendIDCheck.addActionListener(this);
		}
		legendPanel.add(legendLabelCheck);
		legendLabelCheck.addActionListener(this);
		setLegendChecksEnabled(legendCheck.isSelected());

//		JPanel cpWrap = new JPanel();
//		cpWrap.add(controlPanel);

		this.add(slider);
		this.add(controlPanel);
		this.add(labelWrap);
		this.add(legendPanel);

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
	
	public void addAnimationListener(AnimationListener l) {
		listeners.add(l);
	}
	
	public void removeAnimationListener(AnimationListener l) {
		listeners.remove(l);
	}
	
	private void fireAnimationRangeChanged(FaultAnimation anim) {
		for (AnimationListener l : listeners)
			l.animationRangeChanged(anim);
	}
	
	private void fireAnimationStepChanged(FaultAnimation anim) {
		for (AnimationListener l : listeners)
			l.animationStepChanged(anim);
	}
	
	boolean isTimeBasedEnabled() {
		return faultAnim instanceof TimeBasedFaultAnimation && animTypeParam.getValue() == AnimType.TIME_BASED
				&& ((TimeBasedFaultAnimation)faultAnim).getCurrentDuration() > 0d;
	}

	private void updateSliderRange() {
		if (D) System.out.println("Updating slider range!");
		timeCalc = null;
		int max = faultAnim.getNumSteps();
		Preconditions.checkState(max >= 0, "Num steps must be zero or positive: %s", max);
		if (isTimeBasedEnabled()) {
			max = SLIDER_NUM_TIME_BASED;
		}
		int min;
		if (max > 0) {
			min = 1;
		} else {
			min = 0;
		}
		fireAnimationRangeChanged(faultAnim);
		slider.setMinimum(min);
		slider.setMaximum(max);
		
		setCurrentStep(faultAnim.getPreferredInitialStep());
	}

	private void updateStep(){
		if (D) System.out.println("Updating step!");
		faultAnim.setCurrentStep(curStep);
		if (D) System.out.println("Step: "+curStep+"\tSlider: "+curSliderVal
				+"\tAnimTime: "+curAnimTime+"\tAbsTime: "+getLabel(curAbsTime));
		String labelStr = "";
		if (faultAnim.includeStepInLabel())
			labelStr += "Frame "+(curStep+1)+"/"+faultAnim.getNumSteps();
		if (faultAnim instanceof TimeBasedFaultAnimation) {
			if (!Double.isNaN(curAbsTime) && curAbsTime >= 0)
				labelStr += ": " + getLabel(curAbsTime);
		}
		String custom = faultAnim.getCurrentLabel();
		if (custom != null && custom.length() > 0) {
			if (labelStr.length() > 0 && !custom.startsWith(" "))
				labelStr += " ";
			labelStr += custom;
		}
		if (faultAnim instanceof IDBasedFaultAnimation) {
			int id = ((IDBasedFaultAnimation)faultAnim).getIDForStep(curStep);
			idField.setText(""+id);
			labelStr += " (ID: " + id + ")";
		}
		if (D) System.out.println("Label: "+labelStr);
		
//		Geo3dInfo.getMainWindow().setMessage(labelStr); // TODO label support
		labelStr = wrapText(labelStr, 60);
		String info = labelStr;
		info.replace("<br>MoRate:", "MoRate:");
		labelStr = "<html>"+labelStr+"</html>";
		label.setToolTipText(labelStr);
			 
		
		
//		if (labelStr.length() > 70)
//			labelStr = labelStr.substring(0, 67)+"...";
		label.setText(labelStr);
		if (D) System.out.println("Firing step changed");
		if (faultAnim.getNumSteps()>0)
			fireAnimationStepChanged(faultAnim);
		if (D) System.out.println("DONE Firing step changed");
		enableAnimControls();
		
		updateLegend();
	}
	
	private void updateTime() {
		if (isTimeBasedEnabled()) {
			boolean fire = ((TimeBasedFaultAnimation)faultAnim).timeChanged(curAbsTime);
			if (fire)
				fireAnimationStepChanged(faultAnim);
			updateLegend();
		}
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
			if (D) System.out.println("Slider changed!");
			if (slider.getValue() != curSliderVal)
				updateStepFromSlider();
		}
	}
	
	private synchronized void updateStepFromSlider() {
		curSliderVal = slider.getValue();
		if (D) System.out.println("Updating state from sliderVal="+curSliderVal);
		
		if (slider.getMinimum() == 0) {
			// no current animation
			Preconditions.checkState(slider.getMaximum() == 0);
			curStep = -1;
			curAnimTime = 0d;
			curAbsTime = Double.NaN;
			return;
		}
		
		if (D) Preconditions.checkState(slider.getMinimum() == 1);
		// calculate animation time
		curAnimTime = durationParam.getValue()*(curSliderVal-1d)/(slider.getMaximum()-1d);
		if (D) System.out.println("Calculated animTime="+curAnimTime);
		if (Double.isNaN(curAnimTime))
			curAnimTime = 0d;
		
		// calculate step
		if (isTimeBasedEnabled()) {
			curStep = getTimeCalc().getStepForAnimTimeSecs(curAnimTime);
			curAbsTime = getTimeCalc().getAbsoluteTime(curAnimTime);
		} else {
			curStep = curSliderVal - 1;
			curAbsTime = Double.NaN;
		}
		
		updateStep();
		updateTime();
	}
	
	private Runnable sliderUpdate = new Runnable() {
		@Override
		public void run() {
			if (D) System.out.println("Slider update runable. setting value: "+curSliderVal);
			slider.setValue(curSliderVal);
			if (D) System.out.println("Slider update runable. DONE setting value");
		}
	};
	
	synchronized void setCurrentAnimTime(double animTime) {
		if (D) System.out.println("setCurrentAnimTime called with time="+animTime);
		
		int calcStep;
		if (faultAnim.getNumSteps() == 0) {
			// no current animation
			Preconditions.checkState(slider.getMaximum() == 0);
			curStep = -1;
			curAnimTime = 0d;
			curAbsTime = Double.NaN;
			curSliderVal = 0;
			calcStep = -1;
		} else {
			// calculate step
			calcStep = getTimeCalc().getStepForAnimTime(curStep, animTime);
			this.curAnimTime = animTime;
			
			// calculate animation time
			StepTimeCalculator timeCalc = getTimeCalc();
			
			// calculate slider value for step
			if (isTimeBasedEnabled()) {
				double fractionalAnimTime = curAnimTime/durationParam.getValue();
				Preconditions.checkState(slider.getMaximum() == SLIDER_NUM_TIME_BASED);
				curSliderVal = 1 + (int)(fractionalAnimTime*(SLIDER_NUM_TIME_BASED-1d));
				curAbsTime = timeCalc.getAbsoluteTime(curAnimTime);
			} else {
				// evenly spaced
				curSliderVal = calcStep+1;
				curAbsTime = Double.NaN;
			}
		}
		
		if (D) System.out.println("Updating slider for animTime="+animTime+", sliderVal="+curSliderVal
				+". EDT? "+SwingUtilities.isEventDispatchThread());
		// make sure to run this in the EDT
		if (SwingUtilities.isEventDispatchThread()) {
			sliderUpdate.run();
		} else {
			try {
				SwingUtilities.invokeAndWait(sliderUpdate);
			} catch (Exception e) {
				throw ExceptionUtils.asRuntimeException(e);
			}
		}
		
		if (curStep >= 0) {
			if (calcStep != curStep) {
				if (D) System.out.println("Updating step itself");
				curStep = calcStep;
				updateStep();
			}
			updateTime();
		}
		
		if (D) System.out.println("End setCurrentSetp");
	}
	
	double getAnimDuration() {
		return durationParam.getValue();
	}
	
	synchronized void setCurrentStep(int step) {
		if (D) System.out.println("setCurrentStep called with step="+step);
		curStep = step;
		
		if (faultAnim.getNumSteps() <= 0) {
			// no current animation
			Preconditions.checkState(slider.getMaximum() == 0);
			curStep = -1;
			curAnimTime = 0d;
			curAbsTime = Double.NaN;
			curSliderVal = 0;
		} else {
			// calculate animation time
			StepTimeCalculator timeCalc = getTimeCalc();
			// anim time in secs for this step
			curAnimTime = timeCalc.getAnimTimeUntil(0l, step);
			
			// calculate slider value for step
			if (isTimeBasedEnabled()) {
				double fractionalAnimTime = curAnimTime/durationParam.getValue();
				Preconditions.checkState(slider.getMaximum() == SLIDER_NUM_TIME_BASED);
				curSliderVal = 1 + (int)(fractionalAnimTime*(SLIDER_NUM_TIME_BASED-1d));
				curAbsTime = timeCalc.getAbsoluteTime(curAnimTime);
			} else {
				// evenly spaced
				curSliderVal = step+1;
				curAbsTime = Double.NaN;
			}
		}
		
		if (D) System.out.println("Updating slider for step="+step+", sliderVal="+curSliderVal
				+". EDT? "+SwingUtilities.isEventDispatchThread());
		// make sure to run this in the EDT
		if (SwingUtilities.isEventDispatchThread()) {
			sliderUpdate.run();
		} else {
			try {
				SwingUtilities.invokeAndWait(sliderUpdate);
			} catch (Exception e) {
				throw ExceptionUtils.asRuntimeException(e);
			}
		}
		
		if (curStep >= 0) {
			if (D) System.out.println("Updating step itself");
			updateStep();
			updateTime();
		}
		
		if (D) System.out.println("End setCurrentSetp");
	}
	
	int getCurrentStep() {
		return curStep;
	}
	
	private Runnable enableAnimControlsRunnable = new Runnable() {

		@Override
		public void run() {
			boolean isFirst = curStep == 0;
			boolean isLast = curStep == faultAnim.getNumSteps()-1;
//			boolean isFirst = slider.getValue() == slider.getMinimum();
//			boolean isLast = slider.getValue() == slider.getMaximum();
			boolean playing = animThread != null && animThread.isAlive();
			if (isTimeBasedEnabled())
				// re-enable if slider is at a maximum
				playing = playing && slider.getValue() < slider.getMaximum();
			else
				// re-enable if we're at the last step
				playing = playing && !isLast;
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
		
	};

	private void enableAnimControls() {
		if (SwingUtilities.isEventDispatchThread())
			enableAnimControlsRunnable.run();
		else
			SwingUtilities.invokeLater(enableAnimControlsRunnable);
	}

	StepTimeCalculator getTimeCalc() {
		if (timeCalc == null) {
			timeCalc = getTimeCalc(durationParam.getValue());
		}
		return timeCalc;
	}
	
	private StepTimeCalculator getTimeCalc(double duration) {
		int maxStep = faultAnim.getNumSteps()-1;
		AnimType type = animTypeParam.getValue();
		switch (type) {
		case TIME_BASED:
			return new TimeBasedCalc((TimeBasedFaultAnimation)faultAnim, maxStep, duration);
		case EVENLY_SPACED:
			return new EvenlySpacedCalc(faultAnim, maxStep, duration);
		default:
			throw new IllegalStateException("Unknown anim type: "+type);
		}
	}
	
	void enableAnimControlsAfterAnimThread() {
		// wait until anim thread is done to enable animation controls
		// must do so in a separate thread as anim thread can wait on EDT events to flush renders
		// and this method is part of an EDT thread. So waiting on it in this thread causes deadlock
		new Thread() {
			@Override
			public void run() {
				while (animThread != null && animThread.isAlive());
				enableAnimControls(); // now checks if it's in the EDT
			}
		}.start();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == playButton) {
			animThread = new AnimThread(this, faultAnim, getTimeCalc(), durationParam.getValue());
			animThread.setLoop(loopParam.getValue());
			animThread.start();
			enableAnimControls();
		} else if (e.getSource() == pauseButton) {
			if (animThread != null) {
				animThread.pause();
			}
			enableAnimControlsAfterAnimThread();
		} else if (e.getSource() == nextButton) {
			setCurrentStep(curStep+1);
		} else if (e.getSource() == prevButton) {
			setCurrentStep(curStep-1);
		} else if (e.getSource() == idField) {
			if (D) System.out.println("idField: action performed");
			idFieldUpdated();
		} else if (e.getSource() == legendCheck) {
			boolean displayed = legendCheck.isSelected();
			setLegendChecksEnabled(displayed);
			
			updateLegend();
			setLegendDisplayed(displayed);
		} else if (e.getSource() == legendStepCheck || e.getSource() == legendTimeCheck
				|| e.getSource() == legendRelativeTimeCheck || e.getSource() == legendIDCheck
				|| e.getSource() == legendLabelCheck) {
			if (e.getSource() == legendTimeCheck)
				setLegendChecksEnabled(legendCheck.isSelected());
			updateLegend();
		}
	}
	
	private void setLegendChecksEnabled(boolean enabled) {
		legendStepCheck.setEnabled(enabled);
		legendTimeCheck.setEnabled(enabled);
		legendRelativeTimeCheck.setEnabled(enabled && legendTimeCheck.isSelected());
		legendIDCheck.setEnabled(enabled);
		legendLabelCheck.setEnabled(enabled);
	}

	@Override
	public void focusGained(FocusEvent focusevent) {}

	@Override
	public void focusLost(FocusEvent focusevent) {
		if (D) System.out.println("idField: focus lost");
		idFieldUpdated();
	}

	private void idFieldUpdated() {
		String idFieldVal = idField.getText();
		try {
			int id = Integer.parseInt(idFieldVal);
			int step = ((IDBasedFaultAnimation)faultAnim).getStepForID(id);
			setCurrentStep(step);
		} catch (Exception e) {
			// couldn't parse, reset it to the current ID
			idField.setText(((IDBasedFaultAnimation)faultAnim).getIDForStep(curStep)+"");
		}
	}

	private static String wrapText(String text, int len) {
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

	@Override
	public void parameterChange(ParameterChangeEvent event) {
		if (event.getSource() == durationParam || event.getSource() == animTypeParam) {
			timeCalc = null;
		}
	}
	
	private Color selectLegendColor() {
		Color bkg = Info.getBackgroundColor();
		double meanVal = (double)(bkg.getRed() + bkg.getGreen() + bkg.getBlue())/3d;
		Color color;
		if (meanVal >= 127.5)
			// it's a light color
			color = Color.BLACK;
		else
			// it's a dark color
			color = Color.WHITE;
//		System.out.println("Building legend. BKG="+bkg+" (mean="+meanVal+")");
		return color;
	}
	
	private synchronized void setLegendDisplayed(boolean displayed) {
		Color color = selectLegendColor();
		if (legend == null) {
			// determine color as compliment of background color
			// first pass in the name so that it shows up as such in the legend gui
			legend = LegendUtils.buildTextLegend(plugin, faultAnim.getName(), Font.SANS_SERIF, 28, color, 5, 5);
			((vtkTextActor)legend.getActor()).SetInput(buildLegendText());
		} else {
			((vtkTextActor)legend.getActor()).GetTextProperty().SetColor(Info.convertColor(color));
		}
		
		if (displayed)
			plugin.getPluginActors().addLegend(legend);
		else
			plugin.getPluginActors().removeLegend(legend);
		
		MainGUI.updateRenderWindow();
	}
	
//	private static final Joiner legendJoin = Joiner.on(", ");
	private static final Joiner legendJoin = Joiner.on("\n");
	
	private synchronized void updateLegend() {
		if (legend == null)
			return;
		String text = buildLegendText();
		Preconditions.checkState(legend.getActor() instanceof vtkTextActor);
		vtkTextActor actor = (vtkTextActor) legend.getActor();
		actor.SetInput(text);
		actor.Modified();
		em.updateViewer();
	}
	
	private String buildLegendText() {
		ArrayList<String> elems = new ArrayList<>();
		if (legendStepCheck.isSelected())
			elems.add((curStep+1)+"/"+faultAnim.getNumSteps());
		if (faultAnim instanceof TimeBasedFaultAnimation && legendTimeCheck.isSelected() && Double.isFinite(curAbsTime)) {
			double time = curAbsTime;
			if (legendRelativeTimeCheck.isSelected())
				time -= ((TimeBasedFaultAnimation)faultAnim).getTimeForStep(0);
			elems.add(getLabel(time));
		}
		if (faultAnim instanceof IDBasedFaultAnimation && legendIDCheck.isSelected()) {
			int id = ((IDBasedFaultAnimation)faultAnim).getIDForStep(curStep);
			elems.add("ID: "+id);
		}
		if (legendLabelCheck.isSelected()) {
			String label = faultAnim.getCurrentLabel();
			if (label != null && !label.isEmpty())
				elems.add(label);
		}
		return legendJoin.join(elems);
	}

}
