package org.scec.vtk.plugins.opensha.geoJSON;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.EnumSet;

import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;

import org.opensha.commons.geo.json.Feature;
import org.opensha.commons.geo.json.FeatureCollection;
import org.opensha.commons.geo.json.FeatureProperties;
import org.opensha.commons.geo.json.GeoJSON_Type;
import org.opensha.commons.geo.json.Geometry;
import org.opensha.commons.geo.json.Geometry.DepthSerializationType;
import org.opensha.commons.param.ParameterList;
import org.opensha.commons.param.event.ParameterChangeEvent;
import org.opensha.commons.param.event.ParameterChangeListener;
import org.opensha.commons.param.impl.ButtonParameter;
import org.opensha.commons.param.impl.EnumParameter;
import org.opensha.commons.param.impl.FileParameter;
import org.scec.vtk.commons.opensha.tree.FaultCategoryNode;
import org.scec.vtk.commons.opensha.tree.FaultSectionNode;
import org.scec.vtk.commons.opensha.tree.builders.FaultTreeBuilder;
import org.scec.vtk.commons.opensha.tree.events.TreeChangeListener;

import com.google.gson.Gson;

public class GeoJSONPluginBuilder implements FaultTreeBuilder, ParameterChangeListener {
	
	private FileParameter loadParam;
	private ButtonParameter reloadButton;
	private EnumParameter<DepthSerializationType> depthTypeParam;
	
	private ParameterList params;
	private TreeChangeListener l;
	
	public GeoJSONPluginBuilder() {
		params = new ParameterList();
		
		loadParam = new FileParameter("GeoJSON File");
		loadParam.addParameterChangeListener(this);
		
		reloadButton = new ButtonParameter("Reload GeoJSON File?", "Reload");
		reloadButton.addParameterChangeListener(this);
		reloadButton.getEditor().setEnabled(false);
		
		depthTypeParam = new EnumParameter<>("Depth Type",
				EnumSet.allOf(DepthSerializationType.class), Geometry.DEPTH_SERIALIZATION_DEFAULT, null);
		depthTypeParam.addParameterChangeListener(this);

		params.addParameter(loadParam);
		params.addParameter(reloadButton);
		params.addParameter(depthTypeParam);
	}

	@Override
	public ParameterList getBuilderParams() {
		return params;
	}

	@Override
	public ParameterList getFaultParams() {
		return null;
	}

	@Override
	public void setTreeChangeListener(TreeChangeListener l) {
		this.l = l;
	}

	@Override
	public void buildTree(DefaultMutableTreeNode root) {
		File file = loadParam.getValue();
		
		if (file == null)
			return;
		
		Gson gson = FeatureCollection.buildGson(depthTypeParam.getValue());
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			
			FeatureCollection features;
			try {
				features = gson.fromJson(reader, FeatureCollection.class);
				reader.close();
			} catch (IllegalStateException e) {
				// see if it works as just a feature
				reader = new BufferedReader(new FileReader(file));
				try {
					Feature feature = gson.fromJson(reader, Feature.class);
					features = new FeatureCollection(feature);
				} catch (Exception e1) {
					throw e;
				}
			}
			
			FaultCategoryNode node = new FaultCategoryNode(features.features.size()+" Features");
			int id = 0;
			for (Feature feature : features.features) {
				FaultSectionNode sectNode = new FaultSectionNode(new GeoJSONFakeFaultSection(feature, id++));
				node.add(sectNode);
			}
			
			root.add(node);
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e.getMessage(), "Failed to load GeoJSON", JOptionPane.ERROR_MESSAGE);
			return;
		}
	}

	@Override
	public void parameterChange(ParameterChangeEvent event) {
		l.treeChanged(null);
		reloadButton.getEditor().setEnabled(loadParam.getValue() != null);
		reloadButton.getEditor().refreshParamEditor();
		
	}

}
