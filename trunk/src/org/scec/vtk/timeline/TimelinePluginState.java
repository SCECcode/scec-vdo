package org.scec.vtk.timeline;

import org.scec.vtk.plugins.Plugin;
import org.scec.vtk.plugins.PluginState;
import org.scec.vtk.plugins.StatefulPlugin;
import org.scec.vtk.timeline.camera.CameraKeyFrame;

import vtk.vtkCamera;

import java.util.ArrayList;
import java.util.Iterator;

import org.dom4j.Element;
public class TimelinePluginState implements PluginState{

	private Timeline parent;
	ArrayList<String> pluginLayerId;
	ArrayList<ArrayList<PluginState>> pluginStateArray;
	ArrayList<ArrayList<Double>> timeArray;
	int numPlugins;
	private KeyFrameList cameraKeyArray;
	private ArrayList<CameraKeyFrame> cameraAtKeyframe;
	private ArrayList<Double> cameraTimeAtKeyframe;

	public TimelinePluginState(Timeline timeline) {
		// TODO Auto-generated constructor stub
		this.parent = timeline;
		pluginLayerId = new ArrayList<>();
		pluginStateArray = new ArrayList<>();
		this.timeArray = new ArrayList<>();
		cameraAtKeyframe = new ArrayList<>();
		cameraTimeAtKeyframe = new ArrayList<>();
	}

	@Override
	public void load() {
		// TODO Auto-generated method stub

	}

	@Override
	public void toXML(Element stateEl) {
		copyLatestDetials();
		createElement(stateEl);
	}

	private void createElement(Element stateEl) {
		for(int j=0;j<cameraKeyArray.size();j++)
		{
			Element pluginKeyFrameElement = stateEl.addElement("CameraKeyFrame")
					.addAttribute("index", Integer.toString(j))
					.addAttribute("camPositon", cameraAtKeyframe.get(j).getCam().GetPosition()[0]+","+cameraAtKeyframe.get(j).getCam().GetPosition()[1]+","+cameraAtKeyframe.get(j).getCam().GetPosition()[2])
					.addAttribute("camViewUp", cameraAtKeyframe.get(j).getCam().GetViewUp()[0]+","+cameraAtKeyframe.get(j).getCam().GetViewUp()[1]+","+cameraAtKeyframe.get(j).getCam().GetViewUp()[2])
					.addAttribute("camLookAt", cameraAtKeyframe.get(j).getCam().GetFocalPoint()[0]+","+cameraAtKeyframe.get(j).getCam().GetFocalPoint()[1]+","+cameraAtKeyframe.get(j).getCam().GetFocalPoint()[2])
					.addAttribute("camTime", Double.toString(cameraTimeAtKeyframe.get(j)));
		}
		
		for(int i =0;i<this.numPlugins;i++)
		{
			Element timelineElement = stateEl.addElement( "Timeline" )
					.addAttribute( "pluginIndex", Integer.toString(i))
					.addAttribute( "pluginId", pluginLayerId.get(i))
					.addAttribute( "pluginNumKeyFrames", Integer.toString(pluginStateArray.get(i).size()));
		
			for(int j=0;j<pluginStateArray.get(i).size();j++)
			{
				Element pluginKeyFrameElement = timelineElement.addElement("Keyframe")
						.addAttribute("index", Integer.toString(j))
						.addAttribute("time", Double.toString(timeArray.get(i).get(j)));
				pluginStateArray.get(i).get(j).load();
				pluginStateArray.get(i).get(j).toXML(pluginKeyFrameElement);
			}
		}

	}

	@Override
	public void fromXML(Element stateEl) {
		//camera
		for ( Iterator j = stateEl.elementIterator("CameraKeyFrame"); j.hasNext(); ) {
			Element eSub = (Element) j.next();
			
			vtkCamera cam = new vtkCamera();
			String[] pos = eSub.attributeValue("camPositon").split(",");
			cam.SetPosition(Double.parseDouble(pos[0]),Double.parseDouble(pos[1]),Double.parseDouble(pos[2]));
			String[] viewUp = eSub.attributeValue("camViewUp").split(",");
			cam.SetViewUp(Double.parseDouble(viewUp[0]),Double.parseDouble(viewUp[1]),Double.parseDouble(viewUp[2]));
			String[] focalPoint = eSub.attributeValue("camLookAt").split(",");
			cam.SetFocalPoint(Double.parseDouble(focalPoint[0]),Double.parseDouble(focalPoint[1]),Double.parseDouble(focalPoint[2]));
			
			CameraKeyFrame key = new CameraKeyFrame(Double.parseDouble(eSub.attributeValue("camTime")), cam, false);
			
			parent.addCameraKeyFrame(key);
		}
		
		numPlugins = 0;
		for ( Iterator i = stateEl.elementIterator( "Timeline" ); i.hasNext(); ) {
			Element e = (Element) i.next();
			
			pluginLayerId.add(e.attributeValue("pluginId"));
			int keyFrameListSize = Integer.parseInt(e.attributeValue("pluginNumKeyFrames"));
			ArrayList<PluginState> pluginStateAtKeyframe  = new ArrayList<>();
			ArrayList<Double> timeAtKeyframe  = new ArrayList<>();
			for ( Iterator j = e.elementIterator("Keyframe"); j.hasNext(); ) {
				Element eSub = (Element) j.next();
				Plugin plugin = parent.getPluginAt(numPlugins);
				if (plugin instanceof StatefulPlugin) {
					Element pluginStateElement = eSub;
					PluginState state = ((StatefulPlugin)plugin).getState().deepCopy();
					state.fromXML(pluginStateElement);
					KeyFrame key = new KeyFrame(Double.parseDouble(eSub.attributeValue("time")), state);
					parent.addKeyFrame(parent.getPluginAt(numPlugins),key);
					pluginStateAtKeyframe.add(state);
					timeAtKeyframe.add(Double.parseDouble(eSub.attributeValue("time")));
				}
			}
			pluginStateArray.add(pluginStateAtKeyframe);
			timeArray.add(timeAtKeyframe);
			numPlugins++;
		}

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
		this.timeArray.clear();
		cameraKeyArray = parent.getCameraKeys(); 
		for(int i =0;i<cameraKeyArray.size();i++)
		{
			cameraAtKeyframe.add((CameraKeyFrame)cameraKeyArray.getKeyAt(i));
			cameraTimeAtKeyframe.add(cameraKeyArray.getKeyAt(i).getStartTime());
		}
		// TODO Auto-generated method stub //type of keyframe
		this.numPlugins = parent.getNumPlugins();
		for(int i =0;i<this.numPlugins;i++)
		{
			KeyFrameList keyFrameList = parent.getKeysForPlugin(i);
			pluginLayerId.add(parent.getPluginAt(i).getId());
			ArrayList<PluginState> pluginStateAtKeyframe  = new ArrayList<>();
			ArrayList<Double> timeAtKeyframe  = new ArrayList<>();
			for(int j = 0;j<keyFrameList.size();j++)
			{
				pluginStateAtKeyframe.add(keyFrameList.getKeyAt(j).getState());
				timeAtKeyframe.add(keyFrameList.getKeyAt(j).getStartTime());
			}
			pluginStateArray.add(pluginStateAtKeyframe);
			timeArray.add(timeAtKeyframe);
		}
	}

}