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
	private ArrayList<Boolean> catalogTypeIsComcat;
	private ArrayList<String> valuesBy;


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
		valuesBy =new ArrayList<>();
		catalogTypeIsComcat = new ArrayList<>();
	}
	void copyLatestCatalogDetials()
	{
		catalogs.clear();
		dispName.clear();
		filePath.clear();
		color1.clear();
		color2.clear();
		scaling.clear();
		transparency.clear();
		visibility.clear();
		geometry.clear();
		catalogTypeIsComcat.clear();
		for (EQCatalog eqc : parent.getCatalogs())
		{
			EQCatalog cat;
			cat = eqc;
			dispName.add(eqc.getDisplayName());
			if(!eqc.getCatalogTypeIsComcat())
				{
				filePath.add( eqc.getAttributeFile().getPath());
				catalogTypeIsComcat.add(false);
				}
			else
				{
				filePath.add( eqc.getComcatFilePathString());
				catalogTypeIsComcat.add(true);
				}
			//create local copies of display attributes
			color1.add( cat.getColor1());
			color2.add(cat.getColor2());
			scaling.add(cat.getScaling());
			System.out.println("scaling:"+cat.getScaling());
			transparency.add(cat.getTransparency());
			visibility.add(eqc.isDisplayed());
			//sibility.add(cat.isDisplayed());
			System.out.println("cat.isDisplayed: " + cat.isDisplayed());

			geometry.add(eqc.getGeometry());
			System.out.println("cat.getGeometry() " + cat.getGeometry());

		    valuesBy.add(eqc.getValuesBy());
			System.out.println("cat.getValuesBy() " + cat.getValuesBy());


			catalogs.add(cat);
		}
	}
	@Override
	public void load() {
		// call methods to update based on the properties captured //might also want to put swing invoke and wait
		int i=0;
		for (EQCatalog eqc : catalogs)
		{
			eqc.setValuesBy(valuesBy.get(i));
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
			Element propertyEl = stateEl.addElement( "EarthquakeCatalog" );
			propertyEl.addElement("dispName").addText(dispName.get(i));
			propertyEl.addElement("filePath").addText(filePath.get(i));
			propertyEl.addElement("color1").addText(Integer.toString(color1.get(i).getRGB()));
			propertyEl.addElement("color2").addText(Integer.toString(color2.get(i).getRGB()));
			propertyEl.addElement("scaling").addText(Integer.toString(scaling.get(i)));
			propertyEl.addElement("transparency").addText(Integer.toString(transparency.get(i)));
			propertyEl.addElement("geometry").addText(Integer.toString(geometry.get(i)));
			propertyEl.addElement("comcat").addText(catalogTypeIsComcat.get(i).toString());
			propertyEl.addElement("valuesBy").addText(valuesBy.get(i));
			propertyEl.addElement("visibility").addText(visibility.get(i).toString());
			System.out.println(eqc.getColor1());
    		System.out.println(eqc.getColor2());
			i++;
		}
	}

	@Override
	public void toXML(Element stateEl) {
		copyLatestCatalogDetials();
		createElement(stateEl);
	}

	@Override
	public void fromXML(Element stateEl) {
		 for ( Iterator i = stateEl.elementIterator( "EarthquakeCatalog" ); i.hasNext(); ) {
	            Element e = (Element) i.next();
	       
	            dispName.add(e.elementText("dispName"));
	            filePath.add(e.elementText("filePath"));
	            color1.add(Color.decode(e.elementText("color1")));
	            color2.add(Color.decode(e.elementText("color2")));
	            scaling.add(Integer.parseInt(e.elementText("scaling")));
	            transparency.add(Integer.parseInt(e.elementText("transparency")));
	            geometry.add(Integer.parseInt(e.elementText("geometry")));
	            visibility.add(Boolean.parseBoolean(e.elementText("visibility")));
	            valuesBy.add((e.elementText("valuesBy")));
	            catalogTypeIsComcat.add(Boolean.parseBoolean(e.elementText("comcat")));
	            
	            System.out.println(e.elementText("filePath"));
	            // read the catalog file
	            File file = new File(filePath.get(filePath.size()-1));
	            EQCatalog eq;
	            if(!catalogTypeIsComcat.get(catalogTypeIsComcat.size()-1))
	            	eq = new EQCatalog(parent, file, parent.getPluginActors());
	            else
	            	{
	            		eq = new EQCatalog(parent);
	            		eq.setDisplayName(dispName.get(dispName.size()-1));
	            		System.out.println(file.getPath());
	            		System.out.println(eq.getDisplayName());
	            		eq.getCrd().readFromComcatDataFile(eq, file.getPath());
	            		
	            	}
	            //add to table
	            parent.getCatalogTable().addCatalog(eq);
	            
	            parent.getCatalogTable().setSelected(eq);
	            int row = parent.getCatalogTable().tableModel.indexOf(eq);
	            //parent.getCatalogTable().tableModel.setVisibilityForRow(true, row);
	            if(!parent.getCatalogTable().tableModel.getLoadedStateForRow(row)){
	            	parent.getCatalogTable().tableModel.setLoadedStateForRow(true, row);
            		parent.processTableSelectionChange();
            	}
	            eq=(EQCatalog) parent.getCatalogTable().tableModel.getObjectAtRow(row);
	            catalogs.add(eq);
	            eq.setDisplayed(false);
	            parent.getCatalogTable().setVisibility(parent.getCatalogTable().tableModel, eq, row);
	        }
	}
	@Override
	public PluginState deepCopy() {
		EarthquakeCatalogPluginState state = new EarthquakeCatalogPluginState(parent);
		state.copyLatestCatalogDetials();
		return state;
	}

}

