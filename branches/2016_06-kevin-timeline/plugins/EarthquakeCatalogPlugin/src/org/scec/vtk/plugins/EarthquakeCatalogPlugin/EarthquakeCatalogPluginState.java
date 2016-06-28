package org.scec.vtk.plugins.EarthquakeCatalogPlugin;

import java.awt.Color;
import java.util.ArrayList;

import org.dom4j.Element;
import org.scec.vtk.main.Info;
import org.scec.vtk.plugins.PluginState;
import org.scec.vtk.plugins.EarthquakeCatalogPlugin.Components.EQCatalog;

public class EarthquakeCatalogPluginState implements PluginState {
	private EarthquakeCatalogPluginGUI parent;
	private ArrayList<EQCatalog> catalogs;
	private Color color1;
	private Color color2;
	private int scaling;
	private int transparency;
	boolean visibility;
	int geometry;
	EarthquakeCatalogPluginState(EarthquakeCatalogPluginGUI parent)
	{
		this.parent = parent;
		catalogs = new ArrayList<EQCatalog>();
		for (EQCatalog eqc : parent.getCatalogs())
		{
				EQCatalog cat;
				cat = eqc;
				//create local copies of display attributes
				color1  = cat.getColor1();
				color2  = cat.getColor2();
				scaling = cat.getScaling();
				transparency = cat.getTransparency();
				System.out.println(scaling);
				System.out.println(transparency);
				visibility = eqc.isDisplayed();
				geometry = eqc.getGeometry();
				catalogs.add(cat);
		}
	}

	@Override
	public void load() {
		// TODO Auto-generated method stub
		for (EQCatalog eqc : catalogs)
		{
			Color[] newColor =  {color1,color2};
			parent.setColGradient(eqc, newColor);
			parent.setMagnitudeScale(scaling,eqc);
			parent.setTransparency(eqc, transparency);
			parent.setCatalogVisible(eqc,geometry);
			Info.getMainGUI().updateRenderWindow();
		}
	}

	@Override
	public void toXML(Element stateEl) {
		// TODO Auto-generated method stub

	}

	@Override
	public void fromXML(Element stateEl) {
		// TODO Auto-generated method stub

	}

	@Override
	public PluginState deepCopy() {
		return this;
	}

}

