package org.scec.vtk.plugins.opensha.regions;

import java.util.ListIterator;

import javax.swing.tree.DefaultMutableTreeNode;

import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationList;
import org.opensha.commons.geo.Region;
import org.opensha.commons.param.ParameterList;
import org.opensha.sha.faultSurface.Surface3D;
import org.scec.vtk.commons.opensha.faults.AbstractFaultSection;
import org.scec.vtk.commons.opensha.tree.FaultSectionNode;
import org.scec.vtk.commons.opensha.tree.builders.FaultTreeBuilder;
import org.scec.vtk.commons.opensha.tree.events.TreeChangeListener;

public class RegionsBuilder implements FaultTreeBuilder {

	@Override
	public ParameterList getBuilderParams() {
		return null;
	}

	@Override
	public ParameterList getFaultParams() {
		return null;
	}

	@Override
	public void setTreeChangeListener(TreeChangeListener l) {
		// only needed if we make tree variable (e.g. through params)
	}

	@Override
	public void buildTree(DefaultMutableTreeNode root) {
		addNode(root, new CaliforniaRegions.RELM_TESTING());
		addNode(root, new CaliforniaRegions.RELM_COLLECTION());
		addNode(root, new CaliforniaRegions.RELM_SOCAL());
		addNode(root, new CaliforniaRegions.RELM_NOCAL());
		addNode(root, new CaliforniaRegions.LA_BOX());
		addNode(root, new CaliforniaRegions.SF_BOX());
		addNode(root, new CaliforniaRegions.NORTHRIDGE_BOX());
		addNode(root, new CaliforniaRegions.SAN_DIEGO_BOX());
		addNode(root, new CaliforniaRegions.CYBERSHAKE_MAP_REGION());
		addNode(root, new CaliforniaRegions.CYBERSHAKE_CCA_MAP_REGION());
	}
	
	private void addNode(DefaultMutableTreeNode root, Region region) {
		RegionSection sect = new RegionSection(region, root.getChildCount());
		
		FaultSectionNode faultNode = new FaultSectionNode(sect);
		
		root.add(faultNode);
	}
	
	private class RegionSection extends AbstractFaultSection {
		
		private Region region;

		public RegionSection(Region region, int id) {
			super(region.getName(), id);
			this.region = region;
		}

		@Override
		public Surface3D createSurface(ParameterList faultRepresentationParams) {
			return new RegionSurface(region.getBorder());
		}

		@Override
		public double getSlipRate() {
			return Double.NaN;
		}

		@Override
		public double getAvgRake() {
			return Double.NaN;
		}

		@Override
		public double getAvgStrike() {
			return Double.NaN;
		}

		@Override
		public double getAvgDip() {
			return Double.NaN;
		}
		
	}
	
	private class RegionSurface implements Surface3D {
		
		private LocationList perimeter;
		
		public RegionSurface(LocationList perimeter) {
			this.perimeter = perimeter;
		}

		@Override
		public ListIterator<Location> getLocationsIterator() {
			return perimeter.listIterator();
		}

		@Override
		public LocationList getEvenlyDiscritizedPerimeter() {
			throw new UnsupportedOperationException("Not implemented");
		}

		@Override
		public LocationList getPerimeter() {
			return perimeter;
		}

		@Override
		public boolean isPointSurface() {
			return perimeter.size() == 1;
		}
		
	}

}
