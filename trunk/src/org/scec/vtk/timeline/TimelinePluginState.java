package org.scec.vtk.timeline;

import org.scec.vtk.plugins.AnimatablePlugin;
import org.scec.vtk.plugins.Plugin;
import org.scec.vtk.plugins.PluginState;
import org.scec.vtk.plugins.StatefulPlugin;
import org.scec.vtk.timeline.camera.CameraAnimator.SplineType;
import org.scec.vtk.timeline.camera.CameraKeyFrame;

import vtk.vtkCamera;

import java.util.ArrayList;
import java.util.Iterator;

import org.dom4j.Element;
public class TimelinePluginState implements PluginState{

	private Timeline parent;
	ArrayList<String> pluginLayerId;
	ArrayList<ArrayList<PluginState>> pluginStateArray;
	ArrayList<ArrayList<Double>> startTimeArray;
	ArrayList<ArrayList<Double>> endTimeArray;
	ArrayList<ArrayList<Double>> durationArray;
	int numPlugins;
	private KeyFrameList cameraKeyArray;
	private ArrayList<CameraKeyFrame> cameraAtKeyframe;
	private ArrayList<Double> cameraTimeAtKeyframe;
	private ArrayList<Boolean> pluginIsFrozen;
	private ArrayList<Boolean> pluginIsDisplayed;
	private double frameRate;
	private double totalAnimationDuration;
	private String splineIndex;
	private ArrayList<ArrayList<Boolean>> visibilityArray;
	private ArrayList<ArrayList<String>> keyFrameTypeArray;

	public TimelinePluginState(Timeline timeline) {
		// TODO Auto-generated constructor stub
		this.parent = timeline;
		pluginLayerId = new ArrayList<>();
		pluginStateArray = new ArrayList<>();
		this.startTimeArray = new ArrayList<>();
		cameraAtKeyframe = new ArrayList<>();
		cameraTimeAtKeyframe = new ArrayList<>();
		endTimeArray = new ArrayList<>();
		durationArray = new ArrayList<>();
		visibilityArray = new ArrayList<>();
		keyFrameTypeArray = new ArrayList<>();
		pluginIsDisplayed= new ArrayList<>();
		pluginIsFrozen= new ArrayList<>();
		frameRate =0;
		numPlugins =0;
		totalAnimationDuration=0;
		splineIndex="";
	}

	@Override
	public void load() {
		parent.setMaxTime(totalAnimationDuration);
		parent.setCameraSplineType(SplineType.valueOf(splineIndex));
		/*
		for(int i=0;i<numPlugins;i++)
		{
			Plugin plugin = parent.getPluginAt(i);
			parent.setDisplayed(plugin, pluginIsDisplayed.get(i));
			parent.setFrozen(plugin, pluginIsFrozen.get(i));
		}
		*/
		for(int i=0;i<pluginLayerId.size();i++)
		{
			System.out.println("pluginLayerid: " + pluginLayerId.get(i) + " should be equivalent to: " + parent.getPluginWith(pluginLayerId.get(i)).getId());
			Plugin plugin = parent.getPluginWith(pluginLayerId.get(i));
			parent.setDisplayed(plugin, pluginIsDisplayed.get(i));
			parent.setFrozen(plugin, pluginIsFrozen.get(i));
		}
		

	}

	@Override
	public void toXML(Element stateEl) {
		copyLatestDetials();
		createElement(stateEl);
	}

	private void createElement(Element stateEl) {
		Element timeLinePropertyElement = stateEl.addElement("TimelineProperty");
		timeLinePropertyElement.addElement("frameRate").addText(Double.toString(frameRate));
		timeLinePropertyElement.addElement("cameraSplineType").addText((splineIndex));
		timeLinePropertyElement.addElement("maxTime").addText( Double.toString(totalAnimationDuration));
		
		for(int j=0;j<cameraKeyArray.size();j++)
		{
			//Write out key frame information to XML
			Element pluginKeyFrameElement = stateEl.addElement("CameraKeyFrame");
			pluginKeyFrameElement.addElement("index").addText( Integer.toString(j));
			pluginKeyFrameElement.addElement("camPositon").addText( cameraAtKeyframe.get(j).getCam().GetPosition()[0]+","+cameraAtKeyframe.get(j).getCam().GetPosition()[1]+","+cameraAtKeyframe.get(j).getCam().GetPosition()[2]);
			pluginKeyFrameElement.addElement("camViewUp").addText( cameraAtKeyframe.get(j).getCam().GetViewUp()[0]+","+cameraAtKeyframe.get(j).getCam().GetViewUp()[1]+","+cameraAtKeyframe.get(j).getCam().GetViewUp()[2]);
			pluginKeyFrameElement.addElement("camLookAt").addText( cameraAtKeyframe.get(j).getCam().GetFocalPoint()[0]+","+cameraAtKeyframe.get(j).getCam().GetFocalPoint()[1]+","+cameraAtKeyframe.get(j).getCam().GetFocalPoint()[2]);
			pluginKeyFrameElement.addElement("camTime").addText(Double.toString(cameraTimeAtKeyframe.get(j)));
			pluginKeyFrameElement.addElement("paused").addText(Boolean.toString(cameraAtKeyframe.get(j).isPause()));
		}
		
		for(int i =0;i<this.numPlugins;i++)
		{
			
			Element timelineElement = stateEl.addElement( "TimelineLayer" );
			timelineElement.addElement( "pluginIndex").addText( Integer.toString(i));
			System.out.println(pluginLayerId.get(i));
			System.out.println(parent.getPluginWith(pluginLayerId.get(i)).getId());
			//timelineElement.addElement( "pluginId").addText( pluginLayerId.get(i));
			timelineElement.addElement( "pluginId").addText( parent.getPluginWith(pluginLayerId.get(i)).getId());
			timelineElement.addElement( "isFrozen").addText( Boolean.toString(pluginIsFrozen.get(i)));
			timelineElement.addElement( "isDisplayed").addText( Boolean.toString(pluginIsDisplayed.get(i)));
			timelineElement.addElement( "pluginNumKeyFrames").addText(Integer.toString(keyFrameTypeArray.get(i).size()));
		
			for(int j=0;j<keyFrameTypeArray.get(i).size();j++)
			{
				Element pluginKeyFrameElement = timelineElement.addElement("Keyframe");
				pluginKeyFrameElement.addElement("index").addText( Integer.toString(j));
				pluginKeyFrameElement.addElement("startTime").addText( Double.toString(startTimeArray.get(i).get(j)));
				pluginKeyFrameElement.addElement("type").addText( keyFrameTypeArray.get(i).get(j));
				if(keyFrameTypeArray.get(i).get(j).equals("Visibility"))
				{
					pluginKeyFrameElement.addElement("visiblity").addText( Boolean.toString(visibilityArray.get(i).get(j)));
				}
				else if (keyFrameTypeArray.get(i).get(j).equals("Range")) {
					if(durationArray.get(i).get(j)>0)
						{
						pluginKeyFrameElement.addElement("endTime").addText( Double.toString(endTimeArray.get(i).get(j)));
						pluginKeyFrameElement.addElement("duration").addText(Double.toString(durationArray.get(i).get(j)));
						}
					pluginStateArray.get(i).get(j).load();
					pluginStateArray.get(i).get(j).toXML(pluginKeyFrameElement);
				}
				else  {
					pluginStateArray.get(i).get(j).load();
					pluginStateArray.get(i).get(j).toXML(pluginKeyFrameElement);
				}
				
			}
		}

	}

	@Override
	public void fromXML(Element stateEl) {
		
		pluginLayerId.clear();
		pluginStateArray.clear();
		this.startTimeArray.clear();
		cameraAtKeyframe.clear();
		cameraTimeAtKeyframe.clear();
		endTimeArray.clear();
		durationArray.clear();
		visibilityArray.clear();
		keyFrameTypeArray.clear();
		pluginIsDisplayed.clear();
		pluginIsFrozen.clear();
		frameRate =0;
		numPlugins =0;
		totalAnimationDuration=0;
		splineIndex ="";
		
		for ( Iterator i = stateEl.elementIterator("TimelineProperty"); i.hasNext(); ) {
			Element eSub = (Element) i.next();
			frameRate = Double.parseDouble( eSub.elementText("frameRate"));
			splineIndex = ( eSub.elementText("cameraSplineType"));
			totalAnimationDuration = Double.parseDouble( eSub.elementText("maxTime"));
		}
		//camera
		for ( Iterator j = stateEl.elementIterator("CameraKeyFrame"); j.hasNext(); ) {
			Element eSub = (Element) j.next();
			
			vtkCamera cam = new vtkCamera();
			String[] pos = eSub.elementText("camPositon").split(",");
			cam.SetPosition(Double.parseDouble(pos[0]),Double.parseDouble(pos[1]),Double.parseDouble(pos[2]));
			String[] viewUp = eSub.elementText("camViewUp").split(",");
			cam.SetViewUp(Double.parseDouble(viewUp[0]),Double.parseDouble(viewUp[1]),Double.parseDouble(viewUp[2]));
			String[] focalPoint = eSub.elementText("camLookAt").split(",");
			cam.SetFocalPoint(Double.parseDouble(focalPoint[0]),Double.parseDouble(focalPoint[1]),Double.parseDouble(focalPoint[2]));
			
			CameraKeyFrame key = new CameraKeyFrame(Double.parseDouble(eSub.elementText("camTime")), cam, Boolean.parseBoolean(eSub.elementText("paused")));
			
			parent.addCameraKeyFrame(key);
		}
		
		numPlugins = 0;
		for ( Iterator i = stateEl.elementIterator( "TimelineLayer" ); i.hasNext(); ) {
			Element e = (Element) i.next();
			
			pluginLayerId.add(e.elementText("pluginId"));
			System.out.println("LayerID: " + pluginLayerId.get(numPlugins));
			//Plugin plugin = parent.getPluginAt(numPlugins);
			Plugin plugin = parent.getPluginWith(pluginLayerId.get(numPlugins));
			System.out.println("PluginId: " + plugin.getId());
			pluginIsDisplayed.add(Boolean.parseBoolean(e.elementText("isDisplayed")));
			pluginIsFrozen.add(Boolean.parseBoolean(e.elementText("isFrozen")));
		
			int keyFrameListSize = Integer.parseInt(e.elementText("pluginNumKeyFrames"));
			ArrayList<PluginState> pluginStateAtKeyframe  = new ArrayList<>();
			ArrayList<Double> startTimeAtKeyframe  = new ArrayList<>();
			ArrayList<Double> endTimeAtKeyframe  = new ArrayList<>();
			ArrayList<Double> durationAtKeyframe  = new ArrayList<>();
			ArrayList<Boolean> visibilityAtKeyframe  = new ArrayList<>();
			ArrayList<String> typeOfKeyFrame = new ArrayList<>();
			for ( Iterator j = e.elementIterator("Keyframe"); j.hasNext(); ) {
				Element eSub = (Element) j.next();
				
				startTimeAtKeyframe.add(Double.parseDouble(eSub.elementText("startTime")));
				typeOfKeyFrame.add(eSub.elementText("type"));
				if (eSub.elementText("type").equals("Visibility")) {
					VisibilityKeyFrame key = new VisibilityKeyFrame(Double.parseDouble(eSub.elementText("startTime")),parent.getActorsForPlugin(plugin),Boolean.parseBoolean(eSub.elementText("visiblity")));
					parent.addKeyFrame(parent.getPluginAt(numPlugins),key);
					System.out.println("visibility of " + Double.parseDouble(eSub.elementText("startTime")) + " being added to " + plugin.getId());
				}
				if (eSub.elementText("type").equals("Range")) {
					double duration = Double.parseDouble(eSub.elementText("duration"));
					if (duration < 0)
						return;
					if (duration == 0)
						{
						pluginStateAtKeyframe.add(loadAndCreateStateKeyFrame(eSub,plugin));
						}
					else
					{
						endTimeAtKeyframe.add(Double.parseDouble(eSub.elementText("endTime")));
						durationAtKeyframe.add(Double.parseDouble(eSub.elementText("duration")));
						PluginState state = ((StatefulPlugin)plugin).getState().deepCopy();
						state.fromXML(eSub);
						
						KeyFrame key = new RangeKeyFrame(Double.parseDouble(eSub.elementText("startTime")),Double.parseDouble(eSub.elementText("endTime")), state,(AnimatablePlugin)plugin);
						parent.addKeyFrame(parent.getPluginAt(numPlugins),key);
						pluginStateAtKeyframe.add(state);
					}
				}
				if (eSub.elementText("type").equals("State")) {
					
					pluginStateAtKeyframe.add(loadAndCreateStateKeyFrame(eSub,plugin));
				}
			}
		
				keyFrameTypeArray.add(typeOfKeyFrame);
	
				pluginStateArray.add(pluginStateAtKeyframe);
		
				startTimeArray.add(startTimeAtKeyframe);
		
				endTimeArray.add(endTimeAtKeyframe);
	
				durationArray.add(durationAtKeyframe);
		
				visibilityArray.add(visibilityAtKeyframe);	
			numPlugins++;
		}

	}

	private PluginState loadAndCreateStateKeyFrame(Element eSub,Plugin plugin)
	{
		Element pluginStateElement = eSub;
		PluginState state = ((StatefulPlugin)plugin).getState().deepCopy();
		state.fromXML(pluginStateElement);
		
		KeyFrame key = new KeyFrame(Double.parseDouble(eSub.elementText("startTime")), state);
		parent.addKeyFrame(parent.getPluginAt(numPlugins),key);
		return state;
	}
	
	@Override
	public PluginState deepCopy() {
		TimelinePluginState state = new TimelinePluginState(parent);
		state.copyLatestDetials();
		return state;
	}

	private void copyLatestDetials() {
		pluginLayerId.clear();
		pluginStateArray.clear();
		this.startTimeArray.clear();
		cameraAtKeyframe.clear();
		cameraTimeAtKeyframe.clear();
		endTimeArray.clear();
		durationArray.clear();
		visibilityArray.clear();
		keyFrameTypeArray.clear();
		pluginIsDisplayed.clear();
		pluginIsFrozen.clear();
		frameRate =0;
		numPlugins =0;
		totalAnimationDuration=0;
		splineIndex ="";
		
		cameraKeyArray = parent.getCameraKeys(); 
		for(int i =0;i<cameraKeyArray.size();i++)
		{
			cameraAtKeyframe.add((CameraKeyFrame)cameraKeyArray.getKeyAt(i));
			cameraTimeAtKeyframe.add(cameraKeyArray.getKeyAt(i).getStartTime());
		}
		// TODO Auto-generated method stub //type of keyframe
		this.numPlugins = parent.getNumPlugins();
		this.frameRate = parent.getFamerate();
		this.totalAnimationDuration = parent.getMaxTime();
		splineIndex = parent.getCameraSplineType().name();
		for(int i =0;i<this.numPlugins;i++)
		{
			KeyFrameList keyFrameList = parent.getKeysForPlugin(i);
			Plugin plugin = parent.getPluginAt(i);
			pluginLayerId.add(parent.getPluginAt(i).getId());
			pluginIsDisplayed.add(parent.isDisplayed(i));
			pluginIsFrozen.add(parent.isFrozen(i));
			ArrayList<PluginState> pluginStateAtKeyframe  = new ArrayList<>();
			ArrayList<Double> startTimeAtKeyframe  = new ArrayList<>();
			ArrayList<Double> endTimeAtKeyframe  = new ArrayList<>();
			ArrayList<Double> durationAtKeyframe  = new ArrayList<>();
			ArrayList<Boolean> visibilityAtKeyframe  = new ArrayList<>();
			ArrayList<String> typeOfKeyFrame = new ArrayList<>();
			for(int j = 0;j<keyFrameList.size();j++)
			{
				KeyFrame key = keyFrameList.getKeyAt(j) ;
				startTimeAtKeyframe.add(keyFrameList.getKeyAt(j).getStartTime());
				if(key instanceof VisibilityKeyFrame)
				{
					typeOfKeyFrame.add("Visibility");	
					visibilityAtKeyframe.add(((VisibilityKeyFrame)keyFrameList.getKeyAt(j)).isVisible());
				}
				else if (key instanceof RangeKeyFrame) {
					// can add a range or regular key
					typeOfKeyFrame.add("Range");
					double duration = ((RangeKeyFrame)key).getDuration();
					if (duration < 0)
						return;
					if (duration == 0)
						{
						pluginStateAtKeyframe.add(keyFrameList.getKeyAt(j).getState());
						}
					else
						{
						pluginStateAtKeyframe.add(((RangeKeyFrame)key).getState());
						endTimeAtKeyframe.add(((RangeKeyFrame)key).getEndTime());
						durationAtKeyframe.add(((RangeKeyFrame)key).getDuration());
						}
				}
				else {
					typeOfKeyFrame.add("State");
					pluginStateAtKeyframe.add(key.getState());
				}
			}
			keyFrameTypeArray.add(typeOfKeyFrame);
			
			pluginStateArray.add(pluginStateAtKeyframe);
	
			startTimeArray.add(startTimeAtKeyframe);
	
			endTimeArray.add(endTimeAtKeyframe);

			durationArray.add(durationAtKeyframe);
	
			visibilityArray.add(visibilityAtKeyframe);		
		}
	}

}