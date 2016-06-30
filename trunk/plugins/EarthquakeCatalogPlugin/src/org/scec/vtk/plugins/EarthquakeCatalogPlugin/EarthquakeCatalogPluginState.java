package org.scec.vtk.plugins.EarthquakeCatalogPlugin;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import org.dom4j.Element;
import org.scec.vtk.main.Info;
import org.scec.vtk.plugins.PluginState;
import org.scec.vtk.plugins.EarthquakeCatalogPlugin.Components.ComcatResourcesDialog;
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
		
		for (EQCatalog eqc : parent.getCatalogs())
		{
			EQCatalog cat;
			cat = eqc;
			dispName.add(eqc.getSourceFile());
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
			geometry.add(eqc.getGeometry());
			catalogs.add(cat);
		}
	}
	@Override
	public void load() {
		// call methods to update based on the properties captured //might also want to put swing invoke and wait
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
					.addAttribute( "comcat",Boolean.toString(catalogTypeIsComcat.get(i)))
					.addAttribute( "visibility",(visibility.get(i).toString()));
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
	            dispName.add(e.attributeValue("dispName"));
	            filePath.add(e.attributeValue("filePath"));
	            color1.add(Color.decode(e.attributeValue("color1")));
	            color2.add(Color.decode(e.attributeValue("color2")));
	            scaling.add(Integer.parseInt(e.attributeValue("scaling")));
	            transparency.add(Integer.parseInt(e.attributeValue("transparency")));
	            geometry.add(Integer.parseInt(e.attributeValue("geometry")));
	            visibility.add(Boolean.parseBoolean(e.attributeValue("visibility")));
	            catalogTypeIsComcat.add(Boolean.parseBoolean(e.attributeValue("comcat")));
	            
	            System.out.println(e.attributeValue("filePath"));
	            // read the catalog file
	            File file = new File(filePath.get(filePath.size()-1));
	            EQCatalog eq;
	            if(!catalogTypeIsComcat.get(catalogTypeIsComcat.size()-1))
	            	eq = new EQCatalog(parent, file, parent.getPluginActors());
	            else
	            	{
	            		eq = new EQCatalog(parent);
	            		System.out.println(file.getPath());
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

