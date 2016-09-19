package org.scec.vtk.commons.opensha.faults;

import org.opensha.commons.data.Named;
import org.opensha.commons.param.ParameterList;
import org.opensha.sha.faultSurface.RuptureSurface;
import org.opensha.sha.faultSurface.Surface3D;
import org.scec.vtk.commons.opensha.surfaces.GeometryGenerator;

public abstract class AbstractFaultSection implements Named {
	
	private String name;
	private int id;
	
	public AbstractFaultSection(String name, int id) {
		this.name = name;
		this.id = id;
	}
	
	public abstract Surface3D createSurface(ParameterList faultRepresentationParams);
	
	protected static void checkHasParam(ParameterList faultRepresentationParams, String name) {
		if (!faultRepresentationParams.containsParameter(name))
			throw new IllegalArgumentException("Given fault representation param list doesn't contain" +
					" required parameter: " + name);
	}

	public String getName() {
		return name;
	}
	
	protected void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return getName();
	}

	public int getId() {
		return id;
	}
	
	public abstract double getSlipRate();
	
	public abstract double getAvgRake();
	
	public abstract double getAvgStrike();
	
	public abstract double getAvgDip();
	
	public String getInfo() {
		return "Name: " + getName()
		+ "\nID: " + getId()
		+ "\nSlip Rate: " + (float)getSlipRate()
		+ "\nAverage Rake: " + (float)getAvgRake()
		+ "\nAverage Strike: " + (float)getAvgStrike()
		+ "\nAverage Dip: " + (float)getAvgDip();
	}
	
	public String getInfoHTML() {
		return "<html>" + getInfo().replaceAll("\n", "<br>") + "</html>";
	}
	
	public GeometryGenerator getCustomGeometryGenerator() {
		return null;
	}

}
