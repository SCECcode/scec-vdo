package org.scec.vtk.plugins.EarthquakeCatalogPlugin;

import java.util.ArrayList;
import java.util.Date;

import javax.swing.JPanel;

import org.scec.vtk.main.Info;
import org.scec.vtk.plugins.ActionPlugin;
import org.scec.vtk.plugins.AnimatableChangeListener;
import org.scec.vtk.plugins.AnimatablePlugin;
import org.scec.vtk.plugins.PluginState;
import org.scec.vtk.plugins.StatefulPlugin;
import org.scec.vtk.plugins.EarthquakeCatalogPlugin.Components.EQCatalog;
import org.scec.vtk.plugins.EarthquakeCatalogPlugin.Components.Earthquake;

import vtk.vtkActor;
import vtk.vtkTextActor;

public class EarthquakeCatalogPlugin extends ActionPlugin implements StatefulPlugin, AnimatablePlugin{


	EarthquakeCatalogPluginGUI eQGui;
	private PluginState state;
	private Earthquake starteq;
	private Earthquake endeq;
	private long diff;
	vtkTextActor screenTextDate; //the date of the earthquake event

	/**
	 * Static field for location of fault data in <i>ScecVideo</i> data library.
	 */
	public static String dataStoreDir = "EQCatalogStore";

	/**
	 * Constructs a new <code>Fault3DPlugin</code> with appropriate metadata.
	 */
	public EarthquakeCatalogPlugin() {
		//this.metadata = new PluginInfo("Earthquake Catalog Plugin", "Earthquake Catalog Plugin", "P. Powers", "0.1", "EQCatalog");
		screenTextDate = new vtkTextActor();
		screenTextDate.SetPosition(0.05, 0.05);
		screenTextDate.GetTextProperty().SetFontSize(21);
		Info.getMainGUI().getRenderWindow().getRenderer().AddActor(screenTextDate);
	}

	/**
	 * Overrides createGUI() in ActionPlugin
	 * @see org.scec.geo3d.plugins.ActionPlugin#createGUI()
	 */
	public JPanel createGUI() {

		eQGui = new EarthquakeCatalogPluginGUI(this);
		return eQGui;
	}
	public void unload() {
		for (EQCatalog eqc : eQGui.getCatalogs())
			for (vtkActor actor : eqc.getActors())
				getPluginActors().removeActor(actor);

		super.unload();
		Info.getMainGUI().updateRenderWindow();
		eQGui=null;
	}


	@Override
	public PluginState getState() {
		if(state==null)
			state = new EarthquakeCatalogPluginState(this.eQGui);
		return state;
	}


	private void  aniamtiondStartedHideShowEarthquake(int opacity,double t)
	{
		int index=0;
		ArrayList<EQCatalog> catalogs = eQGui.getCatalogs();
		for(int j =0;j<catalogs.size();j++)
		{
			if(catalogs.get(j).isDisplayed())
			{
				EQCatalog cat = catalogs.get(j);
//				starteq = (Earthquake)cat.getSelectedEqList().get(0);
//				endeq = (Earthquake)cat.getSelectedEqList().get(cat.getSelectedEqList().size()-1);
				//animation start and end time are set to -1
				if(t==-1)
				{
					index = cat.getSelectedEqList().size();
				}
				else
				{
					index = (int) ((1-t)*(1)+ t*(cat.getSelectedEqList().size()-1));
				}
				if(index<=cat.getSelectedEqList().size())
					for(int i=0;i<index;i++)
					{
						Earthquake eq = cat.getSelectedEqList().get(i);
						eQGui.animateEarthquakeOpacity(i,eq,cat,opacity);
					}
			}
		}
	}

	//animatable plugin
	@Override
	public void animationStarted() {
		// set opacity to 0 when starting of the selected catalog
		aniamtiondStartedHideShowEarthquake(0,-1);

	}

	@Override
	public void animationEnded() {
		aniamtiondStartedHideShowEarthquake(255,-1);
	}

	@Override
	public void animationTimeChanged(double fractionalTime) {		
		// TODO create real time checkbox in UI		
		boolean realTime = this.eQGui.isTrueTimeSelected();
		if(realTime)
		{
			
			ArrayList<EQCatalog> catalogs = eQGui.getCatalogs();
			for(int j =0;j<catalogs.size();j++)
			{
				if(catalogs.get(j).isDisplayed())
				{
					
					EQCatalog cat = catalogs.get(j);
					Earthquake starteq = cat.getSelectedEqList().get(0); //real time values
					Earthquake endeq = cat.getSelectedEqList().get(cat.getSelectedEqList().size()-1);
					double t = fractionalTime;

					long time =  (long) ((1-t)*(starteq.getEq_time().getTime())+ t*(endeq.getEq_time().getTime()));
					Date date=new Date((time));//+starteq.getEq_time().getTime()));
										
					
					//not replacing text properly
					screenTextDate.SetInput("Date: " + date);
					System.out.print("Date: " + date);
					System.out.println("	t:"+fractionalTime);
					
					
					for(int i=0;i<cat.getSelectedEqList().size();i++)
					{

						Earthquake eq = cat.getSelectedEqList().get(i);
						//System.out.println("Date:"+eq.getEq_time());
						if(date.compareTo(eq.getEq_time())<=0)
						{
							eQGui.animateEarthquakeOpacity(i,eq,cat,255);
							//System.out.println("Date:"+eq.getEq_time());
							break;
						}
					}
				}
			}
		}else{
			screenTextDate.SetInput("");
			aniamtiondStartedHideShowEarthquake(255,fractionalTime);
		}
	}

	@Override
	public boolean isAnimatable() {
		// every catalog is animatable
		return true;
	}

	@Override
	public void addAnimatableChangeListener(AnimatableChangeListener l) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeAnimatableChangeListener(AnimatableChangeListener l) {
		// TODO Auto-generated method stub

	}
}
