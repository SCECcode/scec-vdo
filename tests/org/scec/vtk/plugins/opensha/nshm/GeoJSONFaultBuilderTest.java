package org.scec.vtk.plugins.opensha.nshm;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;

import org.opensha.commons.geo.Location;
import org.opensha.commons.param.ParameterList;
import org.opensha.commons.param.editor.AbstractParameterEditor;
import org.opensha.commons.param.editor.impl.ParameterListEditor;
import org.opensha.commons.param.impl.ButtonParameter;
import org.opensha.commons.param.impl.DoubleParameter;
import org.opensha.commons.param.impl.IntegerParameter;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.earthquake.faultSysSolution.ruptures.util.GeoJSONFaultReader;
import org.opensha.sha.earthquake.faultSysSolution.util.SubSectionBuilder;
import org.opensha.sha.faultSurface.FaultSection;
import org.opensha.sha.faultSurface.FaultTrace;
import org.opensha.sha.faultSurface.GeoJSONFaultSection;
import org.scec.vtk.commons.opensha.faults.faultSectionImpl.PrefDataSection;
import org.scec.vtk.commons.opensha.tree.FaultSectionNode;
import org.scec.vtk.plugins.Plugin;
import org.scec.vtk.plugins.PluginActors;
import org.scec.vtk.tools.RememberedFileParameter;

/** Headless builder checks. Set java.util.prefs.userRoot to a temporary directory to isolate file preferences. */
public class GeoJSONFaultBuilderTest {
	private static int checks;

	private static void check(boolean condition, String message) {
		checks++;
		if (!condition)
			throw new AssertionError(message);
	}

	private static Object field(GeoJSONFaultBuilder builder, String name) throws Exception {
		Field field = GeoJSONFaultBuilder.class.getDeclaredField(name);
		field.setAccessible(true);
		return field.get(builder);
	}

	private static void buildSubsections(GeoJSONFaultBuilder builder) throws Exception {
		Method method = GeoJSONFaultBuilder.class.getDeclaredMethod("buildSubsections");
		method.setAccessible(true);
		try {
			method.invoke(builder);
		} catch (InvocationTargetException exception) {
			throw (RuntimeException) exception.getCause();
		}
	}

	private static GeoJSONFaultSection section(int id, String name, String state, double latitude) {
		FaultSectionPrefData section = new FaultSectionPrefData();
		section.setSectionId(id);
		section.setSectionName(name);
		section.setAveDip(45);
		section.setAveRake(0);
		section.setAveUpperDepth(0);
		section.setAveLowerDepth(10);
		section.setDipDirection(180);
		section.setAveSlipRate(1);
		FaultTrace trace = new FaultTrace(name);
		trace.add(new Location(latitude, -118));
		trace.add(new Location(latitude, -117.7));
		section.setFaultTrace(trace);
		GeoJSONFaultSection json = GeoJSONFaultSection.fromFaultSection(section);
		json.setProperty("State", state);
		return json;
	}

	private static List<FaultSection> sections(DefaultMutableTreeNode node) {
		List<FaultSection> sections = new ArrayList<>();
		if (node instanceof FaultSectionNode faultNode)
			sections.add(((PrefDataSection) faultNode.getFault()).getFaultSection());
		for (int i = 0; i < node.getChildCount(); i++)
			sections.addAll(sections((DefaultMutableTreeNode) node.getChildAt(i)));
		return sections;
	}

	private static void run(File directory) throws Exception {
		PluginActors actors = new PluginActors();
		Plugin plugin = (Plugin) Proxy.newProxyInstance(Plugin.class.getClassLoader(), new Class<?>[] { Plugin.class },
				(proxy, method, args) -> {
					if (method.getName().equals("getPluginActors"))
						return actors;
					throw new UnsupportedOperationException(method.getName());
				});
		GeoJSONFaultBuilder builder = new GeoJSONFaultBuilder(plugin);
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
		int[] updates = { 0 };
		builder.setTreeChangeListener(ignored -> {
			updates[0]++;
			root.removeAllChildren();
			builder.buildTree(root);
		});
		ParameterList options = (ParameterList) field(builder, "subsectionParams");
		IntegerParameter minimum = (IntegerParameter) options.getParameter("Minimum Subsections Per Fault");
		DoubleParameter fraction = (DoubleParameter) options.getParameter("DDW Fraction");
		DoubleParameter length = (DoubleParameter) options.getParameter("Fixed Length");
		check(minimum.getValue() == SubSectionBuilder.MIN_SUB_SECTS_PER_FAULT_DEFAULT, "Wrong minimum default");
		check(fraction.getValue() == SubSectionBuilder.DOWN_DIP_FRACT_DEFAULT, "Wrong DDW-fraction default");
		check(Double.isNaN(length.getValue()) && Double.isNaN(SubSectionBuilder.MAX_LEN_DEFAULT),
				"Wrong fixed-length default");
		new ParameterListEditor(options); // In particular, NaN must work in the numeric editor.
		ButtonParameter button = (ButtonParameter) builder.getBuilderParams().getParameter("Subsection Faults");
		check(!((AbstractParameterEditor<?>) button.getEditor()).getWidget().isEnabled(),
				"Button enabled before loading");

		List<GeoJSONFaultSection> originals = List.of(section(305, "Z Fault", "NV", 36),
				section(101, "A Fault", "CA", 34));
		File input = new File(directory, "faults.geojson");
		GeoJSONFaultReader.writeFaultSections(input, originals);
		RememberedFileParameter load = (RememberedFileParameter) builder.getBuilderParams()
				.getParameter("Load Fault Sections GeoJSON");
		load.setValue(input);
		check(sections(root).size() == 2, "Loading did not rebuild the full-section tree");
		check(((AbstractParameterEditor<?>) button.getEditor()).getWidget().isEnabled(),
				"Button disabled after loading");
		int fullUpdates = updates[0];
		buildSubsections(builder);
		List<FaultSection> subsections = sections(root);
		check(updates[0] == fullUpdates + 1, "Subsectioning did not notify the viewer exactly once");
		check(subsections.size() == SubSectionBuilder.buildSubSects(originals).size(),
				"Defaults differ from OpenSHA builder");
		SubSectionBuilder.validateSubSects(subsections);
		for (FaultSection subsection : subsections) {
			check(subsection.getParentSectionId() == 101 || subsection.getParentSectionId() == 305, "Lost parent ID");
			check(((GeoJSONFaultSection) subsection).getProperty("State") != null, "Lost state property");
		}

		minimum.setValue(1);
		fraction.setValue(0d);
		length.setValue(100d);
		buildSubsections(builder);
		check(sections(root).size() == 2, "Repeated subdivision did not start from the original loaded faults");
		minimum.setValue(3);
		fraction.setValue(Double.NaN);
		length.setValue(5d);
		buildSubsections(builder);
		check(sections(root).size() == SubSectionBuilder.buildSubSects(originals, 3, Double.NaN, 5).size(),
				"Fixed-length settings differ from OpenSHA builder");
		for (FaultSection subsection : sections(root))
			check(subsection.getTraceLength() <= 5.001, "Fixed-length limit was not applied");

		for (double invalid : new double[] { 0, -1, Double.POSITIVE_INFINITY }) {
			fraction.setValue(0d);
			length.setValue(invalid);
			int before = updates[0];
			boolean rejected = false;
			try {
				buildSubsections(builder);
			} catch (IllegalArgumentException expected) {
				rejected = true;
			}
			check(rejected && updates[0] == before, "Invalid settings altered the displayed sections");
		}

		ButtonParameter reload = (ButtonParameter) builder.getBuilderParams().getParameter("Reload GeoJSON File?");
		reload.setValue(reload.getValue() + 1);
		check(sections(root).size() == 2 && sections(root).get(0).getParentSectionId() < 0,
				"Reload did not restore the original full sections");
		File empty = new File(directory, "empty.geojson");
		GeoJSONFaultReader.writeFaultSections(empty, List.of());
		load.setValue(empty);
		check(sections(root).isEmpty(), "Empty file left old sections in the tree");
		check(!((AbstractParameterEditor<?>) button.getEditor()).getWidget().isEnabled(),
				"Button enabled for an empty file");
	}

	public static void main(String[] args) throws Exception {
		File directory = Files.createTempDirectory("geojson-subsection-check-").toFile();
		SwingUtilities.invokeAndWait(() -> {
			try {
				run(directory);
			} catch (Exception exception) {
				throw new RuntimeException(exception);
			}
		});
		System.out.println("PASS: " + checks + " GeoJSON subsectioning checks");
	}
}
