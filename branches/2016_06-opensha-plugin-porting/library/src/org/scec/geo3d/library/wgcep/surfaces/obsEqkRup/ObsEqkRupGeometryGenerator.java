package org.scec.geo3d.library.wgcep.surfaces.obsEqkRup;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.Node;
import javax.media.j3d.PointArray;
import javax.media.j3d.PointAttributes;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Color3f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3d;

import org.opensha.commons.geo.LocationList;
import org.opensha.commons.param.ParameterList;
import org.opensha.sha.earthquake.observedEarthquake.ObsEqkRupList;
import org.opensha.sha.faultSurface.CompoundSurface;
import org.opensha.sha.faultSurface.EvenlyGriddedSurface;
import org.opensha.sha.faultSurface.RuptureSurface;
import org.scec.geo3d.library.wgcep.faults.AbstractFaultSection;
import org.scec.geo3d.library.wgcep.faults.faultSectionImpl.ObsEqkRupPointSources;
import org.scec.geo3d.library.wgcep.surfaces.FaultSectionShape3D;
import org.scec.geo3d.library.wgcep.surfaces.GeometryGenerator;
import org.scec.geo3d.library.wgcep.surfaces.PointSurfaceGenerator;
import org.scec.geo3d.library.wgcep.surfaces.PolygonSurfaceGenerator;

import com.sun.j3d.utils.geometry.Sphere;

public class ObsEqkRupGeometryGenerator extends GeometryGenerator {
	
	private PolygonSurfaceGenerator polygonGen;
	
	private BranchGroup pointsBG;
	private ArrayList<Long> pointsMilis;
	private ArrayList<Boolean> pointsVisibles;
	private ArrayList<BranchGroup> pointsSubBGs;
	
	private Transform3D tr3D = new Transform3D();
	
	public ObsEqkRupGeometryGenerator() {
		super("Observed EQ Geometry Gen");
		
		polygonGen = new PolygonSurfaceGenerator();
	}

	@Override
	public BranchGroup createFaultActor(RuptureSurface surface,
			Color color, AbstractFaultSection fault) {
		if (fault instanceof ObsEqkRupPointSources) {
			// this means it's point sources
			pointsBG = createBranchGroup();
			pointsMilis = new ArrayList<Long>();
			pointsVisibles = new ArrayList<Boolean>();
			pointsSubBGs = new ArrayList<BranchGroup>();
			
			ObsEqkRupList rups = ((ObsEqkRupPointSources)fault).getRups();
			
			List<? extends RuptureSurface> compSurfs = ((CompoundSurface)surface).getSurfaceList();
			
			for (int i=0; i<compSurfs.size(); i++) {
				RuptureSurface surf = compSurfs.get(i);
				
				Appearance app = new Appearance();
				app.setColoringAttributes(PointSurfaceGenerator.buildCA(new Color3f(color)));
				app.setPointAttributes(PointSurfaceGenerator.buildPA(getSize(fault)));
				app.setPolygonAttributes(new PolygonAttributes(
		        		PolygonAttributes.POLYGON_FILL, PolygonAttributes.CULL_BACK, 0.0f));
				
				// else this is just a point source
				Point3f pt = getPointForLoc(surf.getFirstLocOnUpperEdge());
				Sphere event = new Sphere(getSize(fault),
			               Sphere.GENERATE_NORMALS,
			               5, app);
				
				Vector3d v = new Vector3d(pt);
		        tr3D.setTranslation(v);
		        TransformGroup tGroup = new TransformGroup(tr3D);
				tGroup.addChild(event);
				
//				FaultSectionShape3D shape = new FaultSectionShape3D(pts, app, fault);
				
				BranchGroup subBG = createBranchGroup();
				subBG.addChild(tGroup);
				subBG.setUserData(app);
				
				pointsMilis.add(rups.get(i).getOriginTime());
				pointsVisibles.add(new Boolean(true));
				pointsSubBGs.add(subBG);
				
				pointsBG.addChild(subBG);
			}
			
			// this will store "hidden" earthquakes during animation
			pointsBG.setUserData(pointsSubBGs);
			
			return pointsBG;
		} else {
			// this is a finite fault EQ
			return polygonGen.createFaultActor(surface, color, fault);
		}
	}
	
	private int getSize(AbstractFaultSection fault) {
		return 1; // TODO make selectable
	}

	@Override
	public boolean updateColor(BranchGroup bg, Color color) {
		Color3f color3f = new Color3f(color);
		if (bg.getUserData() instanceof List<?>) {
			// this is for the points
			List<BranchGroup> allBGs = (List<BranchGroup>)bg.getUserData();
			for (BranchGroup allBG : allBGs) {
				((Appearance)allBG.getUserData() ).getColoringAttributes().setColor(color3f);
			}
		} else {
			// this is for a finite rup
			for (int i=0; i<bg.numChildren(); i++) {
				if (!updateColorAttributesForNode(bg.getChild(i), color3f))
					return false;
			}
		}
		return true;
	}
	
	public synchronized void updatePointSourcesAnimation(long milis, long minYearMillis, long maxYearMillis) {
//		System.out.println("updatePointSourcesAnimation called for "+milis+" milis (min="+minYearMillis+", max="+maxYearMillis+")");
		
		// first go through the "hidden" list to see if we should add anything back in
		if (pointsMilis == null)
			return;
//		if (milis <= pointsMilis.get(1)) {
//			pointsBG.removeAllChildren();
//			for (int i=0; i<pointsVisibles.size(); i++)
//				pointsVisibles.set(i, false);
//			if (pointsMilis.get(0) <= milis) {
//				pointsBG.addChild(pointsSubBGs.get(0));
//				pointsVisibles.set(0, true);
//			}
//			if (milis == pointsMilis.get(1)) {
//				pointsBG.addChild(pointsSubBGs.get(1));
//				pointsVisibles.set(1, true);
//			}
//		}
		
		if (pointsBG.numChildren() > 100) {
			int cutoff = pointsMilis.size() < 100 ? pointsMilis.size()-1 : 9;
			if (milis <= pointsMilis.get(cutoff) || milis <= minYearMillis) {
				System.out.println("Doing a remove all!");
				BranchGroup parent = (BranchGroup)pointsBG.getParent();
				pointsBG.detach();
				System.out.println("Detatched parent, now removing all");
				pointsBG.removeAllChildren();
				System.out.println("DONE");
				for (int i=0; i<pointsVisibles.size(); i++)
					pointsVisibles.set(i, false);
				System.out.println("Reattaching parent");
				parent.addChild(pointsBG);
			}
		}
		
		for (int i=0; i<pointsMilis.size(); i++) {
			long pointMillis = pointsMilis.get(i);
			boolean show = pointMillis <= milis && pointMillis >= minYearMillis && pointMillis <= maxYearMillis;
			boolean visible = pointsVisibles.get(i);
			if (show && !visible) {
				// it should be shown, but isn't currently visible
				pointsBG.addChild(pointsSubBGs.get(i));
				pointsVisibles.set(i, true);
			} else if (!show && visible) {
				// it is visible but shouldn't be shown
				pointsSubBGs.get(i).detach();
				pointsVisibles.set(i, false);
			}
		}
	}

	@Override
	public ParameterList getDisplayParams() {
		// TODO Auto-generated method stub
		return null;
	}

}
