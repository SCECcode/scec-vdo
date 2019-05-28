package org.scec.vtk.plugins.EarthquakeCatalogPlugin.Components;

import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.util.ArrayList;

import org.scec.vtk.main.Info;
import org.scec.vtk.tools.picking.PickEnabledActor;
import org.scec.vtk.tools.picking.PickHandler;

import vtk.vtkActor;
import vtk.vtkCellPicker;
import vtk.vtkGlyph3D;
import vtk.vtkMapper;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkStringArray;
import vtk.vtkTextActor;
import vtk.vtkUnsignedCharArray;
import vtk.vtkVertexGlyphFilter;

public class EQInfoDisplayPickHandler  implements PickHandler<EQCatalog> {

	//control what happens on mouse click
	@Override
	public void actorPicked(PickEnabledActor<EQCatalog> actor, EQCatalog cat, vtkCellPicker picker, MouseEvent e) {
		// TODO Auto-generated method stub
		if (cat == null || e.getButton() != MouseEvent.BUTTON1)
			return;

		double[] oldColor = null;
		vtkTextActor textActor = Info.textDisplayActor;
		vtkPoints eqPoints = new vtkPoints();
		if (e.getButton() == MouseEvent.BUTTON1) {
			// Pick from this location. 
			if (actor != null && actor.GetMapper().GetInputAsDataSet() instanceof vtkPolyData) {
				
				ArrayList<Earthquake> eqList = cat.getSelectedEqList();
				vtkActor actorPointsOld = (vtkActor) cat.getActors().get(0);
				vtkActor actorSpheresOld = (vtkActor) cat.getActors().get(1);
				
				vtkGlyph3D glyphPoints = new vtkGlyph3D();
				glyphPoints = (vtkGlyph3D) actorSpheresOld.GetMapper().GetInputAlgorithm();

				vtkPolyData inputData = new vtkPolyData();
				inputData = (vtkPolyData) glyphPoints.GetInput();
				eqPoints = inputData.GetPoints();
				

				for(int i =0;i<eqPoints.GetNumberOfPoints();i++)
				{
					if(((int)picker.GetPickPosition()[0]-10<=(int)eqPoints.GetPoint(i)[0] && (int)picker.GetPickPosition()[0]+1>=(int)eqPoints.GetPoint(i)[0])
							&& ((int) picker.GetPickPosition()[1]-10<=(int)eqPoints.GetPoint(i)[1]  && (int) picker.GetPickPosition()[1]+1>=(int)eqPoints.GetPoint(i)[1])
									&&( (int)picker.GetPickPosition()[2]-10<=(int)eqPoints.GetPoint(i)[2] && (int)picker.GetPickPosition()[2]+1>=(int)eqPoints.GetPoint(i)[2])){
						DecimalFormat df = new DecimalFormat("##.00");
						System.out.println("Earthquake:- Latitude:"+df.format(eqList.get(i).getEq_latitude())+"; Longitude:"+df.format(eqList.get(i).getEq_longitude())+"; Magnitude:"+df.format(eqList.get(i).getEq_magnitude())+"; Depth:"+df.format(eqList.get(i).getEq_depth()));
						textActor.SetInput ("Earthquake:- Latitude:"+df.format(eqList.get(i).getEq_latitude())+"; Longitude:"+df.format(eqList.get(i).getEq_longitude())+"; Magnitude:"+df.format(eqList.get(i).getEq_magnitude())+"; Depth:"+df.format(eqList.get(i).getEq_depth()));
						textActor.SetPosition2 ( 10, 40 );
						textActor.GetTextProperty().SetFontSize (16);
						textActor.GetTextProperty().SetColor ( 1.0, 0.0, 0.0 );
						textActor.Modified();
						Info.getMainGUI().getRenderWindow().getRenderer().AddActor2D(textActor);
						Info.getMainGUI().updateRenderWindow();
						break;
					}
				}
				
			
			}	
		}
		vtkActor pickedActor = actor;
		if (pickedActor != null){
		if(textActor!=null){
			textActor.SetInput ("");
			textActor.SetPosition2 ( 10, 40 );
			textActor.GetTextProperty().SetFontSize ( 24 );
			textActor.GetTextProperty().SetColor ( 1.0, 0.0, 0.0 );
			textActor.Modified();
		}
		}
		
	}
}
