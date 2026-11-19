package org.scec.vtk.commons.opensha.gui;

import java.awt.Color;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;

import org.jdesktop.jxlayer.plaf.ext.LockableUI;
import org.opensha.commons.geo.Location;
import org.opensha.commons.param.ParameterList;
import org.opensha.sha.faultSurface.ApproxEvenlyGriddedSurface;
import org.opensha.sha.faultSurface.Surface3D;
import org.scec.vtk.commons.opensha.faults.AbstractFaultSection;
import org.scec.vtk.commons.opensha.faults.anim.FaultAnimation;
import org.scec.vtk.commons.opensha.faults.colorers.ColorerChangeListener;
import org.scec.vtk.commons.opensha.faults.colorers.FaultColorer;
import org.scec.vtk.commons.opensha.surfaces.*;
import org.scec.vtk.commons.opensha.surfaces.pickBehavior.FaultSectionPickBehavior;
import org.scec.vtk.commons.opensha.tree.FaultSectionNode;
import org.scec.vtk.commons.opensha.tree.builders.FaultTreeBuilder;
import org.scec.vtk.commons.opensha.tree.events.TreeChangeListener;
import org.scec.vtk.commons.opensha.tree.gui.FaultTreeTable;
import org.scec.vtk.plugins.PluginActors;

import vtk.*;

/** Standalone headless Swing/native-VTK regression checks; no render window is opened. */
public class EventManagerThreadingTest {
    private static void check(boolean ok, String message) {
        if (!ok) throw new AssertionError(message);
    }
    private static void eventThread() {
        check(SwingUtilities.isEventDispatchThread(), "Live scene/node access outside Swing");
    }
    @FunctionalInterface private interface Action { void run() throws Exception; }
    private static void edt(Action action) throws Exception {
        if (SwingUtilities.isEventDispatchThread()) { action.run(); return; }
        try {
            SwingUtilities.invokeAndWait(() -> {
                try { action.run(); } catch (Exception e) { throw new RuntimeException(e); }
            });
        } catch (InvocationTargetException e) {
            throw new AssertionError("Event-thread regression failed", e.getCause());
        }
    }
    private static void await(CountDownLatch latch) {
        try { check(latch.await(5, TimeUnit.SECONDS), "Timed out waiting for color computation"); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new AssertionError(e); }
    }
    private static class Fault extends AbstractFaultSection {
        Fault(int id) { super("Fault " + id, id); }
        public Surface3D createSurface(ParameterList params) {
            ApproxEvenlyGriddedSurface surface = new ApproxEvenlyGriddedSurface(2, 2, 1);
            surface.setLocation(0,0,new Location(34,-118,1)); surface.setLocation(0,1,new Location(34.01,-118,1));
            surface.setLocation(1,0,new Location(34,-117.99,2)); surface.setLocation(1,1,new Location(34.01,-117.99,2));
            return surface;
        }
        public double getSlipRate() { return 1; }
        public double getAvgRake() { return 0; }
        public double getAvgStrike() { return 0; }
        public double getAvgDip() { return 45; }
    }
    private static class Node extends FaultSectionNode {
        Node(int id) { super(new Fault(id)); }
        @Override public AbstractFaultSection getFault() { eventThread(); return super.getFault(); }
        @Override public void setColor(Color color) { eventThread(); super.setColor(color); }
        @Override public void setVisible(boolean visible) { eventThread(); super.setVisible(visible); }
    }
    private static class Generator extends GeometryGenerator {
        int updates;
        final List<Color> appliedColors = new ArrayList<>();
        Generator() { super("Test polygons"); }
        @Override public ParameterList getDisplayParams() { return new ParameterList(); }
        @Override public FaultSectionActorList createFaultActors(Surface3D surface, Color color, AbstractFaultSection fault) {
            eventThread();
            List<PointArray> quads = new ArrayList<>();
            double[][] top = new double[33][], bottom = new double[33][];
            for (int i=0; i<33; i++) {
                top[i] = new double[] {fault.getId()*40d+i, 0, 0};
                bottom[i] = new double[] {fault.getId()*40d+i, 1, -1};
            }
            for (int i=0; i<32; i++) quads.add(new PointArray(top[i],top[i+1],bottom[i+1],bottom[i]));
            return createFaultActors(GeometryType.POLYGON, quads, color, 1, fault);
        }
        @Override public boolean updateColor(FaultSectionActorList actors, Color color) {
            eventThread(); updates++; appliedColors.add(color);
            return super.updateColor(actors, color);
        }
    }
    private static class Picker extends FaultSectionPickBehavior {
        FaultColorer selected;
        @Override public void setColorer(FaultColorer colorer) { eventThread(); selected=colorer; super.setColorer(colorer); }
    }
    private static class Manager extends EventManager {
        int renders;
        Manager(PluginActors actors, FaultTreeTable table, GeometryTypeSelectorPanel selector, LockableUI lock, Picker picker) {
            super(actors,table,null,selector,new ParameterList(),Color.GRAY,picker,lock,null);
        }
        @Override protected void renderScene() { eventThread(); renders++; }
    }
    private static class Fixture {
        final List<Node> nodes = new ArrayList<>();
        final Generator generator = new Generator();
        final PluginActors actors = new PluginActors();
        final LockableUI lock = new LockableUI();
        final Picker picker = new Picker();
        final Manager manager;
        Fixture(int count) {
            eventThread();
            for (int i=0; i<count; i++) nodes.add(new Node(i));
            FaultTreeBuilder builder = new FaultTreeBuilder() {
                public ParameterList getBuilderParams() { return new ParameterList(); }
                public ParameterList getFaultParams() { return new ParameterList(); }
                public void setTreeChangeListener(TreeChangeListener listener) {}
                public void buildTree(DefaultMutableTreeNode root) { for (Node node:nodes) root.add(node); }
            };
            FaultTreeTable table = new FaultTreeTable(builder,null);
            ArrayList<GeometryGenerator> generators = new ArrayList<>(); generators.add(generator);
            manager = new Manager(actors,table,new GeometryTypeSelectorPanel(generators),lock,picker);
            for (Node node:nodes) node.setVisible(true);
        }
        void reset() { eventThread(); generator.updates=0; generator.appliedColors.clear(); manager.renders=0; }
        void colors(Color expected) {
            eventThread(); for (Node node:nodes) check(expected.equals(node.getColor()), "Incorrect applied color");
            check(!lock.isLocked(), "UI remained locked");
            check(actors.getActors().size()==1,"Bundled actor representation changed");
        }
    }
    private static class Colorer implements FaultColorer {
        final Color color;
        final boolean requireWorker;
        final AtomicInteger calculations = new AtomicInteger();
        Colorer(Color color, boolean requireWorker) { this.color=color; this.requireWorker=requireWorker; }
        public Color getColor(AbstractFaultSection fault) {
            if (requireWorker) check(!SwingUtilities.isEventDispatchThread(), "Color computation moved to Swing");
            calculations.incrementAndGet(); return color;
        }
        public String getName() { return "Test colors"; }
        public ParameterList getColorerParameters() { return null; }
        public void setColorerChangeListener(ColorerChangeListener listener) {}
        public String getLegendLabel() { return null; }
    }
    private static void bulk(Fixture f) throws Exception {
        Colorer colorer = new Colorer(Color.ORANGE,true);
        edt(f::reset);
        long start = System.nanoTime();
        f.manager.colorerChanged(colorer); // off-thread request immediately followed by a wait
        f.manager.waitOnCalcThread();
        EventManager.flushRenders();
        edt(() -> {
            f.colors(Color.ORANGE);
            check(colorer.calculations.get()==f.nodes.size(),"Wrong number of color computations");
            check(f.generator.updates==f.nodes.size(),"Wrong number of native color updates");
            check(f.manager.renders==1,"Expected one render for the batch, got "+f.manager.renders);
            check(f.picker.selected==colorer,"Picking was not restored after applying colors");
        });
        System.out.printf("PASS: background calculation, native writes on Swing, one render; %d faults in %.1f ms%n",
                f.nodes.size(),(System.nanoTime()-start)/1e6);
    }
    private static void eventThreadWait(Fixture f) throws Exception {
        edt(() -> {
            f.reset(); f.manager.colorerChanged(new Colorer(Color.RED,true));
            f.manager.waitOnCalcThread(); f.colors(Color.RED);
        });
        EventManager.flushRenders();
        edt(() -> check(f.manager.renders==1,"Event-thread wait rendered more than once"));
        System.out.println("PASS: wait on Swing applies results without deadlock or duplicate application");
    }
    private static void superseded(Fixture f) throws Exception {
        CountDownLatch started = new CountDownLatch(1), release = new CountDownLatch(1);
        Colorer old = new Colorer(Color.MAGENTA,true) {
            @Override public Color getColor(AbstractFaultSection fault) { started.countDown(); await(release); return super.getColor(fault); }
        };
        edt(() -> {
            f.reset(); f.manager.colorerChanged(old); await(started); release.countDown();
            f.manager.colorerChanged(new Colorer(Color.CYAN,true));
            f.manager.waitOnCalcThread(); f.colors(Color.CYAN);
        });
        EventManager.flushRenders();
        edt(() -> check(!f.generator.appliedColors.contains(Color.MAGENTA),"Superseded colors reached VTK"));
        System.out.println("PASS: superseded worker cannot overwrite the latest result");
    }
    private static void customAndFailure(Fixture f) throws Exception {
        edt(() -> {
            f.manager.colorerChanged(new Colorer(Color.BLACK,true));
            f.manager.colorerChanged(null); f.manager.waitOnCalcThread(); f.colors(Color.CYAN);
            check(f.picker.selected==null,"Custom selection did not disable the old picker");
            f.manager.colorerChanged(new Colorer(Color.BLACK,true) {
                @Override public Color getColor(AbstractFaultSection fault) { throw new IllegalStateException("test failure"); }
            });
            boolean failed=false;
            try { f.manager.waitOnCalcThread(); } catch (IllegalStateException expected) { failed=true; }
            check(failed,"Worker failure was not propagated"); check(!f.lock.isLocked(),"Failure left UI locked");
            f.manager.colorerChanged(new Colorer(Color.BLACK,true) {
                @Override public Color getColor(AbstractFaultSection fault) {
                    try { f.manager.waitOnCalcThread(); }
                    catch (InterruptedException e) { throw new RuntimeException(e); }
                    return Color.BLACK;
                }
            });
            boolean selfWaitRejected=false;
            try { f.manager.waitOnCalcThread(); }
            catch (IllegalStateException expected) {
                selfWaitRejected=expected.getMessage().contains("own calculation");
            }
            check(selfWaitRejected,"A colorer waiting on itself must fail without deadlocking Swing");
            check(!f.lock.isLocked(),"Self-wait failure left UI locked");
        });
        EventManager.flushRenders();
        System.out.println("PASS: custom selection discards old work; failures unlock the UI");
    }
    private static void animation(Fixture f) throws Exception {
        Colorer colorer = new Colorer(Color.GREEN,false);
        FaultAnimation animation = (FaultAnimation)Proxy.newProxyInstance(FaultAnimation.class.getClassLoader(),
                new Class<?>[]{FaultAnimation.class},(proxy,method,args) -> {
                    eventThread();
                    switch(method.getName()) {
                    case "getFaultColorer": return colorer;
                    case "getFaultVisibility": return Boolean.TRUE;
                    case "getName": return "Test animation";
                    default: throw new UnsupportedOperationException(method.getName());
                    }
                });
        edt(f::reset);
        f.manager.animationStepChanged(animation); // called from the animation/background thread
        EventManager.flushRenders();
        edt(() -> {
            f.colors(Color.GREEN); check(f.manager.renders==1,"Animation render barrier returned before rendering");
        });
        System.out.println("PASS: off-thread animation applies on Swing; render barrier includes its frame");
    }
    private static void replacedTree(Fixture f) throws Exception {
        edt(() -> {
            f.reset(); f.manager.colorerChanged(new Colorer(Color.YELLOW,true));
            f.manager.treeChanged(new DefaultMutableTreeNode("replacement"));
            f.manager.waitOnCalcThread();
            check(!f.lock.isLocked(),"Replacing tree left UI locked");
        });
        EventManager.flushRenders();
        edt(() -> check(f.generator.updates==0,"A replaced tree received stale native color writes"));
        System.out.println("PASS: replacing the tree cancels results for its old nodes");
    }
    public static void main(String[] args) throws Exception {
        AtomicReference<Throwable> asynchronousFailure = new AtomicReference<>();
        Thread.setDefaultUncaughtExceptionHandler((thread,error) -> { error.printStackTrace(); asynchronousFailure.compareAndSet(null,error); });
        vtkNativeLibrary.LoadNativeLibraries(vtkNativeLibrary.vtkCommonCore,vtkNativeLibrary.vtkCommonMath,
                vtkNativeLibrary.vtkCommonTransforms,vtkNativeLibrary.vtkCommonDataModel,vtkNativeLibrary.vtkCommonExecutionModel,
                vtkNativeLibrary.vtkFiltersCore,vtkNativeLibrary.vtkRenderingCore,vtkNativeLibrary.vtkRenderingOpenGL2);
        Fixture[] box = new Fixture[1]; edt(() -> box[0]=new Fixture(args.length>0?Integer.parseInt(args[0]):256));
        EventManager.flushRenders();
        Fixture f=box[0]; bulk(f); eventThreadWait(f); superseded(f); customAndFailure(f); animation(f); replacedTree(f);
        check(asynchronousFailure.get()==null,"Unhandled event-thread failure: "+asynchronousFailure.get());
        System.out.println("All EventManager threading checks passed");
    }
}
