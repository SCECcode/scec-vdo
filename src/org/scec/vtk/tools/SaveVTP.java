package org.scec.vtk.tools;
import java.io.File;
import java.util.ArrayList;
import org.scec.vtk.main.Info;
import vtk.vtkActor;
import vtk.vtkActorCollection;
import vtk.vtkAppendPolyData;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkXMLPolyDataWriter;
import vtk.rendering.jogl.vtkJoglPanelComponent;

public class SaveVTP {
 
 public static void saveVTPObj(ArrayList<vtkActor> actor,String title)
 {  
   vtkXMLPolyDataWriter objExporter = new vtkXMLPolyDataWriter();
   objExporter.SetFileName(System.getProperty("user.home") + File.separator + ".scec_vdo/tmp/" + title + ".vtp"); 
   vtkAppendPolyData  mainData = new vtkAppendPolyData();
   if(actor.size()>0) {
      for(vtkActor pbActor : actor) {
       vtkPolyDataMapper gmapper = (vtkPolyDataMapper) pbActor.GetMapper();
       if(gmapper!=null){
       vtkPolyData pd  = new vtkPolyData();
       pd.SetPoints(gmapper.GetInput().GetPoints());
       pd.SetLines(gmapper.GetInput().GetLines());
       pd.SetPolys(gmapper.GetInput().GetPolys());
       mainData.AddInputData(pd);
       mainData.Update();
     }
     objExporter.SetInputConnection(mainData.GetOutputPort());
     objExporter.Write();
     
   }
  }
   else {
    File file = new File(System.getProperty("user.home") + File.separator + ".scec_vdo/tmp/" + title + ".vtp");
    file.delete();
   }
  
 }
}
		
		

