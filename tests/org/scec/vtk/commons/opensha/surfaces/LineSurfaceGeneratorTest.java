package org.scec.vtk.commons.opensha.surfaces;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;

import org.jdesktop.jxlayer.plaf.ext.LockableUI;
import org.opensha.commons.geo.Location;
import org.opensha.commons.param.ParameterList;
import org.opensha.commons.param.editor.AbstractParameterEditor;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.faultSurface.ApproxEvenlyGriddedSurface;
import org.opensha.sha.faultSurface.CompoundSurface;
import org.opensha.sha.faultSurface.Surface3D;
import org.scec.vtk.commons.opensha.faults.AbstractFaultSection;
import org.scec.vtk.commons.opensha.faults.faultSectionImpl.PrefDataSection;
import org.scec.vtk.commons.opensha.gui.EventManager;
import org.scec.vtk.commons.opensha.gui.GeometryTypeSelectorPanel;
import org.scec.vtk.commons.opensha.surfaces.LineSurfaceGenerator.LineColoring;
import org.scec.vtk.commons.opensha.surfaces.LineSurfaceGenerator.SurfaceType;
import org.scec.vtk.commons.opensha.surfaces.pickBehavior.FaultSectionPickBehavior;
import org.scec.vtk.commons.opensha.tree.FaultSectionNode;
import org.scec.vtk.commons.opensha.tree.builders.FaultTreeBuilder;
import org.scec.vtk.commons.opensha.tree.events.TreeChangeListener;
import org.scec.vtk.commons.opensha.tree.gui.FaultTreeTable;
import org.scec.vtk.plugins.PluginActors;
import org.scec.vtk.plugins.utils.components.ColorWellButton;
import org.scec.vtk.plugins.opensha.faultSystems.ParentFaultSectionBundler;
import org.scec.vtk.tools.picking.PickEnabledActor;

import vtk.vtkActor;
import vtk.vtkCellPicker;
import vtk.vtkNativeLibrary;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;

/** Standalone headless Swing/native-VTK checks; run with classes and lib/*.jar on the classpath. */
public class LineSurfaceGeneratorTest {
	private static int checks;

	private static void check(boolean condition, String message) {
		checks++;
		if (!condition)
			throw new AssertionError(message);
	}

	private static ApproxEvenlyGriddedSurface surface(int rows, int cols, double offset) {
		ApproxEvenlyGriddedSurface surface = new ApproxEvenlyGriddedSurface(rows, cols, 1);
		for (int row = 0; row < rows; row++)
			for (int col = 0; col < cols; col++)
				surface.setLocation(row, col, new Location(34 + offset + col * .01, -118 + row * .005, 1 + row));
		return surface;
	}

	private static class Fault extends AbstractFaultSection {
		Fault(int id) {
			super("Fault " + id, id);
		}

		@Override
		public Surface3D createSurface(ParameterList params) {
			return surface(3, 4, getId() * .1);
		}
		@Override
		public double getSlipRate() {
			return 1;
		}
		@Override
		public double getAvgRake() {
			return 0;
		}
		@Override
		public double getAvgStrike() {
			return 0;
		}
		@Override
		public double getAvgDip() {
			return 60;
		}
	}

	private static List<FaultSectionActorList> leaves(FaultSectionActorList list) {
		List<FaultSectionActorList> result = new ArrayList<>();
		if (list instanceof CompositeFaultSectionActorList) {
			for (FaultSectionActorList part : ((CompositeFaultSectionActorList) list).getParts())
				result.addAll(leaves(part));
		} else {
			result.add(list);
		}
		return result;
	}

	private static vtkActor actor(FaultSectionActorList list) {
		return list instanceof FaultSectionBundledActorList
				? ((FaultSectionBundledActorList) list).getBundle().getActor()
				: list.get(0);
	}

	private static void color(FaultSectionActorList list, Color expected, int alpha) {
		if (list instanceof FaultSectionBundledActorList) {
			FaultSectionBundledActorList part = (FaultSectionBundledActorList) list;
			for (int i = part.getMyFirstPointIndex(); i < part.getMyFirstPointIndex() + part.getMyNumPoints(); i++) {
				double[] rgba = part.getBundle().getColorArray().GetTuple4(i);
				check(rgba[0] == expected.getRed() && rgba[1] == expected.getGreen()
						&& rgba[2] == expected.getBlue() && rgba[3] == alpha, "Incorrect bundled color/opacity");
			}
		} else {
			double[] rgb = actor(list).GetProperty().GetColor();
			check(Math.abs(rgb[0] * 255 - expected.getRed()) < 1e-6
					&& Math.abs(rgb[1] * 255 - expected.getGreen()) < 1e-6
					&& Math.abs(rgb[2] * 255 - expected.getBlue()) < 1e-6, "Incorrect standalone color");
		}
	}

	private static JComponent widget(LineSurfaceGenerator generator, String parameterName) {
		return ((AbstractParameterEditor<?>)generator.getDisplayParams().getParameter(parameterName).getEditor())
				.getWidget();
	}

	private static void editors() {
		LineSurfaceGenerator generator = new LineSurfaceGenerator();
		// Match the plugin startup sequence, including editors that exist before their values are configured.
		for (var parameter : generator.getDisplayParams())
			parameter.getEditor();
		generator.setLineColoring(LineColoring.FIXED_OUTLINE);
		generator.setSize(1d);
		generator.setInteriorSize(1d);
		generator.setFixedLineColor(Color.WHITE);
		JComboBox<?> coloring = (JComboBox<?>)widget(generator, LineSurfaceGenerator.COLORING_PARAM_NAME);
		ColorWellButton fixedColor = (ColorWellButton)widget(generator, LineSurfaceGenerator.FIXED_COLOR_PARAM_NAME);
		JComboBox<?> outlineSize = (JComboBox<?>)widget(generator, LineSurfaceGenerator.LINE_SIZE_PARAM_NAME);
		JComboBox<?> interiorSize = (JComboBox<?>)widget(generator, LineSurfaceGenerator.INTERIOR_LINE_SIZE_PARAM_NAME);
		check(coloring.getSelectedItem() == LineColoring.FIXED_OUTLINE, "Coloring editor displays a stale value");
		check(Color.WHITE.equals(fixedColor.getColor1()) && Color.WHITE.equals(fixedColor.getColor2()),
				"Fixed-color editor displays a stale color");
		check(Double.valueOf(1d).equals(outlineSize.getSelectedItem()), "Outline-width editor displays a stale value");
		check(Double.valueOf(1d).equals(interiorSize.getSelectedItem()), "Interior-width editor displays a stale value");
		check(fixedColor.isEnabled() && interiorSize.isEnabled(), "All Lines controls should be enabled");

		generator.setSurfaceType(SurfaceType.OUTLINE_ONLY);
		check(((JComboBox<?>)widget(generator, "Surface Type")).getSelectedItem() == SurfaceType.OUTLINE_ONLY,
				"Surface-type editor displays a stale value");
		check(!coloring.isEnabled() && !fixedColor.isEnabled() && !interiorSize.isEnabled(),
				"Interior/coloring controls should be disabled for outlines");
		generator.setSurfaceType(SurfaceType.SOLID);
		generator.setInteriorSize(3d);
		check(Double.valueOf(3d).equals(interiorSize.getSelectedItem()), "Interior-width editor did not refresh");
		generator.setLineColoring(LineColoring.SUPPLIED);
		check(coloring.getSelectedItem() == LineColoring.SUPPLIED && !fixedColor.isEnabled(),
				"Supplied-color mode did not refresh its controls");

		// Refreshing a combo box must not feed back into extra geometry rebuilds or undo the user's selection.
		int[] changes = { 0 };
		generator.addPlotSettingsChangeListener(event -> changes[0]++);
		coloring.setSelectedItem(LineColoring.FIXED_INTERIOR);
		check(generator.getDisplayParams().getParameter(LineSurfaceGenerator.COLORING_PARAM_NAME).getValue()
				== LineColoring.FIXED_INTERIOR, "Editor change did not reach the parameter");
		check(changes[0] == 1, "Refreshing the editor caused duplicate geometry changes");
	}

	private static void styles() {
		for (boolean bundled : new boolean[] { true, false }) {
			for (LineColoring mode : LineColoring.values()) {
				LineSurfaceGenerator generator = new LineSurfaceGenerator();
				generator.setBundlerEneabled(bundled);
				generator.setLineColoring(mode);
				generator.setOpacity(.5);
				Fault fault = new Fault(0);
				FaultSectionActorList list = generator.createFaultActors(fault.createSurface(null), Color.RED, fault);
				List<FaultSectionActorList> parts = leaves(list);
				check(parts.size() == 2, "Expected outline and interior parts");
				for (int i = 0; i < 2; i++) {
					vtkActor actor = actor(parts.get(i));
					check(actor.GetProperty().GetLineWidth() == (i == 0 ? 2 : 1), "Incorrect default width");
					vtkPolyData data = ((vtkPolyDataMapper) actor.GetMapper()).GetInput();
					check(data.GetNumberOfLines() == (i == 0 ? 10 : 7), "Missing or duplicate grid edges");
					boolean fixed = i == 0 ? mode == LineColoring.FIXED_OUTLINE : mode == LineColoring.FIXED_INTERIOR;
					color(parts.get(i), fixed ? Color.LIGHT_GRAY : Color.RED, 0);
				}
				for (var part : parts) {
					if (part instanceof FaultSectionBundledActorList)
						((FaultSectionBundledActorList) part).getBundle()
								.setVisible((FaultSectionBundledActorList) part, true);
					else
						check(actor(part).GetProperty().GetOpacity() == .5, "Incorrect standalone opacity");
				}
				check(generator.updateColor(list, Color.BLUE), "Color update failed");
				color(parts.get(0), mode == LineColoring.FIXED_OUTLINE ? Color.LIGHT_GRAY : Color.BLUE, 127);
				color(parts.get(1), mode == LineColoring.FIXED_INTERIOR ? Color.LIGHT_GRAY : Color.BLUE, 127);

				// Matching initial colors must still behave differently on subsequent recoloring.
				generator.setFixedLineColor(Color.RED);
				list = generator.createFaultActors(fault.createSurface(null), Color.RED, fault);
				generator.updateColor(list, Color.GREEN);
				parts = leaves(list);
				color(parts.get(0), mode == LineColoring.FIXED_OUTLINE ? Color.RED : Color.GREEN, 0);
				color(parts.get(1), mode == LineColoring.FIXED_INTERIOR ? Color.RED : Color.GREEN, 0);
				generator.setSize(4d);
				generator.setInteriorSize(3d);
				parts = leaves(generator.createFaultActors(fault.createSurface(null), Color.RED, fault));
				check(actor(parts.get(0)).GetProperty().GetLineWidth() == 4, "Outline width not applied");
				check(actor(parts.get(1)).GetProperty().GetLineWidth() == 3, "Interior width not applied");
			}
		}
	}

	private static void outlinesAndCompounds() {
		Fault fault = new Fault(0);
		LineSurfaceGenerator generator = new LineSurfaceGenerator();
		generator.setLineColoring(LineColoring.FIXED_OUTLINE);
		for (SurfaceType mode : new SurfaceType[] { SurfaceType.OUTLINE_ONLY, SurfaceType.TRACE_ONLY }) {
			generator.setSurfaceType(mode);
			FaultSectionActorList part = generator.createFaultActors(surface(3, 4, 0), Color.RED, fault);
			check(!(part instanceof CompositeFaultSectionActorList), "Unexpected interior geometry");
			check(((FaultSectionBundledActorList) part).getBundle().getPolyData()
					.GetNumberOfLines() == (mode == SurfaceType.OUTLINE_ONLY ? 10 : 3),
					"Incorrect outline/trace edge count");
			generator.updateColor(part, Color.BLUE);
			color(part, Color.BLUE, 0);
		}
		generator.setSurfaceType(SurfaceType.SOLID);
		generator.setLineColoring(LineColoring.FIXED_INTERIOR);
		for (int[] shape : new int[][] { { 1, 4 }, { 3, 1 }, { 2, 2 }, { 1, 1 } }) {
			FaultSectionActorList part = generator.createFaultActors(surface(shape[0], shape[1], 0), Color.RED, fault);
			check(!(part instanceof CompositeFaultSectionActorList), "Degenerate grid has interior lines");
		}
		generator.clearBundles();
		CompoundSurface compound = CompoundSurface.get(List.of(surface(3, 4, 0), surface(3, 4, .1)));
		FaultSectionActorList list = generator.createFaultActors(compound, Color.RED, fault);
		List<FaultSectionActorList> parts = leaves(list);
		check(parts.size() == 4, "Compound surface lost bundle ranges");
		generator.updateColor(list, Color.BLUE);
		for (int i = 0; i < parts.size(); i++)
			color(parts.get(i), i % 2 == 0 ? Color.BLUE : Color.LIGHT_GRAY, 0);
	}

	private static PrefDataSection parentFault(int id, int parent) {
		FaultSectionPrefData section = new FaultSectionPrefData();
		section.setSectionId(id);
		section.setParentSectionId(parent);
		section.setSectionName("Section " + id);
		return new PrefDataSection(section);
	}

	private static void grouping() {
		LineSurfaceGenerator generator = new LineSurfaceGenerator();
		generator.setFaultActorBundler(new ParentFaultSectionBundler());
		List<FaultSectionActorList> first = leaves(
				generator.createFaultActors(surface(3, 4, 0), Color.RED, parentFault(0, 5)));
		// Build VTK's cell lookup before appending more geometry, as interactive picking does.
		for (var part : first)
			((FaultSectionBundledActorList) part).getBundle().getPolyData().GetCell(0);
		List<FaultSectionActorList> second = leaves(
				generator.createFaultActors(surface(3, 4, .1), Color.BLUE, parentFault(1, 5)));
		List<FaultSectionActorList> third = leaves(
				generator.createFaultActors(surface(3, 4, .2), Color.GREEN, parentFault(2, 6)));
		for (int i = 0; i < 2; i++) {
			vtkPolyData data = ((FaultSectionBundledActorList) second.get(i)).getBundle().getPolyData();
			check(data.GetCell(data.GetNumberOfCells() - 1).GetNumberOfPoints() == 2,
					"Picking cell lookup was not invalidated after appending geometry");
			check(actor(first.get(i)) == actor(second.get(i)), "Same-parent grouping lost");
			check(actor(first.get(i)) != actor(third.get(i)), "Different-parent grouping lost");
		}
		generator.clearBundles();
		List<FaultSectionActorList> rebuilt = leaves(
				generator.createFaultActors(surface(3, 4, 0), Color.RED, parentFault(0, 5)));
		for (int i = 0; i < 2; i++)
			check(actor(first.get(i)) != actor(rebuilt.get(i)), "Clearing retained an old bundle");

		generator.setFaultActorBundler(new GenericFaultActorBundler(1));
		first = leaves(generator.createFaultActors(surface(3, 4, 0), Color.RED, new Fault(0)));
		second = leaves(generator.createFaultActors(surface(3, 4, .1), Color.RED, new Fault(1)));
		for (int i = 0; i < 2; i++)
			check(actor(first.get(i)) != actor(second.get(i)), "Bounded bundler grouping lost");
	}

	private static class RecordingGenerator extends LineSurfaceGenerator {
		final Map<AbstractFaultSection, FaultSectionActorList> built = new IdentityHashMap<>();

		@Override
		public FaultSectionActorList createFaultActors(Surface3D surface, Color color, AbstractFaultSection fault) {
			FaultSectionActorList list = super.createFaultActors(surface, color, fault);
			built.put(fault, list);
			return list;
		}
	}

	private static class PointPicker extends vtkCellPicker {
		private final int point;

		PointPicker(int point) {
			this.point = point;
		}
		@Override
		public long GetPointId() {
			return point;
		}
	}

	private static void lifecycle() {
		RecordingGenerator generator = new RecordingGenerator();
		generator.setLineColoring(LineColoring.FIXED_OUTLINE);
		FaultSectionNode first = new FaultSectionNode(new Fault(0));
		FaultSectionNode second = new FaultSectionNode(new Fault(1));
		FaultTreeBuilder builder = new FaultTreeBuilder() {
			@Override
			public ParameterList getBuilderParams() {
				return new ParameterList();
			}
			@Override
			public ParameterList getFaultParams() {
				return new ParameterList();
			}
			@Override
			public void setTreeChangeListener(TreeChangeListener listener) {
			}
			@Override
			public void buildTree(DefaultMutableTreeNode root) {
				root.add(first);
				root.add(second);
			}
		};
		PluginActors actors = new PluginActors();
		ArrayList<GeometryGenerator> generators = new ArrayList<>();
		generators.add(generator);
		EventManager manager = new EventManager(actors, new FaultTreeTable(builder, null), null,
				new GeometryTypeSelectorPanel(generators), new ParameterList(), Color.RED,
				new FaultSectionPickBehavior(), new LockableUI(), null) {
			@Override
			protected void renderScene() {
				check(SwingUtilities.isEventDispatchThread(), "Render outside Swing");
			}
		};
		AtomicReference<AbstractFaultSection> picked = new AtomicReference<>();
		generator.setPickHandler((PickEnabledActor<AbstractFaultSection> actor, AbstractFaultSection fault,
				vtkCellPicker picker, MouseEvent event) -> picked.set(fault));
		first.setVisible(true);
		second.setVisible(true);
		check(actors.getActors().size() == 2, "Expected two shared rendering actors");
		List<FaultSectionActorList> firstParts = leaves(generator.built.get(first.getFault()));
		List<FaultSectionActorList> secondParts = leaves(generator.built.get(second.getFault()));
		for (int i = 0; i < 2; i++) {
			FaultSectionBundledActorList part = (FaultSectionBundledActorList) firstParts.get(i);
			part.getBundle().getActor().picked(new PointPicker(part.getMyFirstPointIndex()), null);
			check(picked.get() == first.getFault(), "Wrong fault picked from component");
		}
		first.setColor(Color.BLUE);
		color(firstParts.get(0), Color.LIGHT_GRAY, 255);
		color(firstParts.get(1), Color.BLUE, 255);
		color(secondParts.get(1), Color.RED, 255);
		first.setVisible(false);
		for (int i = 0; i < 2; i++) {
			FaultSectionBundledActorList hidden = (FaultSectionBundledActorList) firstParts.get(i);
			FaultSectionBundledActorList visible = (FaultSectionBundledActorList) secondParts.get(i);
			color(hidden, i == 0 ? Color.LIGHT_GRAY : Color.BLUE, 0);
			check(hidden.getBundle().getActor().GetVisibility() == 1, "Hiding one fault hid its neighbor");
			check(hidden.getBundle().getActor().getNumRegisteredPoints() == visible.getMyNumPoints(),
					"Hidden points still registered for picking");
		}
		first.setColor(Color.GREEN);
		color(firstParts.get(1), Color.GREEN, 0);
		first.setVisible(true);
		color(firstParts.get(1), Color.GREEN, 255);
		second.setVisible(false);
		first.setVisible(false);
		for (var part : firstParts)
			check(actor(part).GetVisibility() == 0 && actor(part).GetPickable() == 0,
					"Empty bundle still visible/pickable");
		first.setVisible(true);
		second.setVisible(true);
		generator.setInteriorSize(3d);
		check(actors.getActors().size() == 2, "Settings rebuild left old rendering actors");
		for (var list : generator.built.values()) {
			List<FaultSectionActorList> parts = leaves(list);
			check(actor(parts.get(0)).GetProperty().GetLineWidth() == 2, "Rebuild changed outline width");
			check(actor(parts.get(1)).GetProperty().GetLineWidth() == 3, "Rebuild lost interior width");
		}
		manager.treeChanged(new DefaultMutableTreeNode("empty"));
		check(actors.getActors().isEmpty(), "Tree replacement leaked rendering actors");
	}

	public static void main(String[] args) throws Exception {
		vtkNativeLibrary.LoadNativeLibraries(vtkNativeLibrary.vtkCommonCore, vtkNativeLibrary.vtkCommonMath,
				vtkNativeLibrary.vtkCommonTransforms, vtkNativeLibrary.vtkCommonDataModel,
				vtkNativeLibrary.vtkCommonExecutionModel, vtkNativeLibrary.vtkFiltersCore,
				vtkNativeLibrary.vtkRenderingCore, vtkNativeLibrary.vtkRenderingOpenGL2);
		SwingUtilities.invokeAndWait(() -> {
			editors();
			styles();
			outlinesAndCompounds();
			grouping();
			lifecycle();
		});
		EventManager.flushRenders();
		System.out.println("PASS: " + checks + " line styling, grouping, recoloring, visibility, and picking checks");
	}
}
