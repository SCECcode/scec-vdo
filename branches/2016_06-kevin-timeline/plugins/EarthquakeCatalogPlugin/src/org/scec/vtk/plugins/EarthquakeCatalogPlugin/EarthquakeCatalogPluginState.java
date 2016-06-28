package org.scec.vtk.plugins.EarthquakeCatalogPlugin;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import org.dom4j.Element;
import org.scec.vtk.main.Info;
import org.scec.vtk.plugins.PluginState;
import org.scec.vtk.plugins.EarthquakeCatalogPlugin.Components.EQCatalog;

public class EarthquakeCatalogPluginState implements PluginState {
	private EarthquakeCatalogPluginGUI parent;
	private ArrayList<EQCatalog> catalogs;
	private ArrayList<Color> color1;
	private ArrayList<Color> color2;
	private ArrayList<Integer> scaling;
	private ArrayList<Integer> transparency;
	ArrayList<Boolean> visibility;
	ArrayList<Integer> geometry;
	private ArrayList<String> dispName;
	private ArrayList<String> filePath;


	EarthquakeCatalogPluginState(EarthquakeCatalogPluginGUI parent)
	{
		
		this.parent = parent;
		catalogs = new ArrayList<EQCatalog>();
		dispName =new ArrayList<>();
		filePath =new ArrayList<>();
		color1 =new ArrayList<>();
		color2 =new ArrayList<>();
		scaling =new ArrayList<>();
		transparency =new ArrayList<>();
		visibility =new ArrayList<>();
		geometry =new ArrayList<>();
		copyKeyFrameDetials();
	}
	void copyKeyFrameDetials()
	{
	
		for (EQCatalog eqc : parent.getCatalogs())
		{
			EQCatalog cat;
			cat = eqc;
			dispName.add(eqc.getSourceFile());
			filePath.add( eqc.getAttributeFile().getPath());
			//create local copies of display attributes
			color1.add( cat.getColor1());
			color2.add(cat.getColor2());
			scaling.add(cat.getScaling());
			transparency.add(cat.getTransparency());
			visibility.add(eqc.isDisplayed());
			geometry.add(eqc.getGeometry());
			catalogs.add(cat);
		}
	}
	@Override
	public void load() {
		// call methods to update based on the properties captured
		int i=0;
		for (EQCatalog eqc : catalogs)
		{
			Color[] newColor =  {color1.get(i),color2.get(i)};
			parent.setColGradient(eqc, newColor);
			parent.setMagnitudeScale(eqc,scaling.get(i));
			parent.setTransparency(eqc, transparency.get(i));
			eqc.setDisplayed(visibility.get(i));
			//sphere or points and visibility
			parent.setCatalogVisible(eqc,geometry.get(i),visibility.get(i));
			Info.getMainGUI().updateRenderWindow();
			i++;
		}
	}

	private void createElement(Element stateEl) {
		int i=0;
		for (EQCatalog eqc : catalogs)
		{
			stateEl.addElement( "EarthquakeCatalog" )
					.addAttribute( "dispName", dispName.get(i))
					.addAttribute( "filePath", filePath.get(i))
					.addAttribute( "color1", Integer.toString(color1.get(i).getRGB()))
					.addAttribute( "color2", Integer.toString(color2.get(i).getRGB()))
					.addAttribute( "scaling", Integer.toString(scaling.get(i)))
					.addAttribute( "transparency", Integer.toString(transparency.get(i)))
					.addAttribute( "geometry",Integer.toString(geometry.get(i)))
					.addAttribute( "visibility",(visibility.get(i).toString()));
			i++;
		}
	}

	@Override
	public void toXML(Element stateEl) {
		createElement(stateEl);
	}

	@Override
	public void fromXML(Element stateEl) {
		
		 for ( Iterator i = stateEl.elementIterator( "EarthquakeCatalog" ); i.hasNext(); ) {
	            Element e = (Element) i.next();
	            dispName.add(e.attributeValue("dispName"));
	            filePath.add(e.attributeValue("filePath"));
	            color1.add(Color.decode(e.attributeValue("color1")));
	            color2.add(Color.decode(e.attributeValue("color2")));
	            scaling.add(Integer.parseInt(e.attributeValue("scaling")));
	            transparency.add(Integer.parseInt(e.attributeValue("transparency")));
	            geometry.add(Integer.parseInt(e.attributeValue("geometry")));
	            visibility.add(Boolean.parseBoolean(e.attributeValue("visibility")));
	            
	            // read source directory filtering for catalogs
	            File file = new File(filePath.get(filePath.size()-1));
	            System.out.println(file.exists());
	            EQCatalog eq = new EQCatalog(parent, file, parent.getPluginActors());
	            catalogs.add(eq);
	        }
	}


	@Override
	public PluginState deepCopy() {
		return this;
	}

}

