package org.scec.vtk.plugins.CommunityfaultModelPlugin.components;

import java.awt.event.MouseEvent;

import org.scec.vtk.main.Info;
import org.scec.vtk.tools.picking.PickEnabledActor;
import org.scec.vtk.tools.picking.PickHandler;

import vtk.vtkActor;
import vtk.vtkCellPicker;
import vtk.vtkPolyData;
import vtk.vtkStringArray;
import vtk.vtkTextActor;

public class FaultInfoDisplayPickHandler  implements PickHandler<Fault3D> {

	//control what happens on mouse click
	@Override
	public void actorPicked(PickEnabledActor<Fault3D> actor, Fault3D fault, vtkCellPicker picker, MouseEvent e) {
		// TODO Auto-generated method stub
		if (fault == null || e.getButton() != MouseEvent.BUTTON1)
			return;
		String s = fault.getDisplayName();//.getInfo();

		double[] oldColor = null;
		vtkTextActor textActor = Info.textDisplayActor;
		if (e.getButton() == MouseEvent.BUTTON1) {
			// Pick from this location. 
			if (actor != null && actor.GetMapper().GetInputAsDataSet() instanceof vtkPolyData) {
				oldColor = actor.GetProperty().GetColor();
				vtkPolyData pd = (vtkPolyData) actor.GetMapper().GetInputAsDataSet();
				vtkStringArray info = (vtkStringArray) (pd).GetPointData().GetAbstractArray("Info");
				// Highlight the picked actor by changing its properties
				if(info!=null){

					textActor.SetInput ((info.GetValue(0)));
					textActor.SetPosition2 ( 10, 40 );
					textActor.GetTextProperty().SetFontSize (16);
					textActor.GetTextProperty().SetColor ( 1.0, 0.0, 0.0 );
					textActor.Modified();
					Info.getMainGUI().getRenderWindow().getRenderer().AddActor2D(textActor);
				}
				actor.GetProperty().SetColor(1.0, 1.0, 0.0);
				actor.Modified();
				Info.getMainGUI().updateRenderWindow();
			}
		}
		s = s.replaceAll("\n", ", ");
		System.out.println(s);
		vtkActor pickedActor = actor;
		if (pickedActor != null){
			if(textActor!=null){
				textActor.SetInput ("");
				textActor.SetPosition2 ( 10, 40 );
				textActor.GetTextProperty().SetFontSize ( 24 );
				textActor.GetTextProperty().SetColor ( 1.0, 0.0, 0.0 );
				textActor.Modified();
			}
			pickedActor.GetProperty().SetColor(oldColor);
			pickedActor.Modified();
		}
	}
}
