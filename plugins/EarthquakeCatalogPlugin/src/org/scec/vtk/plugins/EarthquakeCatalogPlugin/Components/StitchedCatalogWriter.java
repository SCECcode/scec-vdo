package org.scec.vtk.plugins.EarthquakeCatalogPlugin.Components;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.opensha.commons.util.ExceptionUtils;
import org.scec.vtk.plugins.EarthquakeCatalogPlugin.EarthquakeCatalogPlugin;
import org.scec.vtk.tools.Prefs;

import gov.usgs.earthquake.event.EventQuery;
import gov.usgs.earthquake.event.EventWebService;
import gov.usgs.earthquake.event.Format;
import gov.usgs.earthquake.event.JsonEvent;
import gov.usgs.earthquake.event.OrderBy;

public class StitchedCatalogWriter {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		JSONObject obj = new JSONObject();
		JSONArray catalogList = new JSONArray();

		double minLon = -130;
		double maxLon = -110;
		double minLat = 30;
		double maxLat = 40;
		
		double minMag = 2d;
		double maxMag = 10d;
		
		String name = "stitched_catalog";
		
		File outputDir = new File("/home/kevin/.scec_vdo/EQCatalogStore/display/data/");
		
		File outputFile = new File(outputDir, name+"-"+System.currentTimeMillis()+".json");
		
		int startYear = 1990; // inclusive
		int endYear = 2023; // exclusive
		
		// do it year by year
		for (int year=startYear; year<endYear; year++) {
			EventWebService service = null;
			
			//masterEarthquakeCatalogBranchGroup = new ArrayList<vtkActor>();
			try {
				//call usgs service to obtain earthquake catalog
				service = new EventWebService(new URL("https://earthquake.usgs.gov/fdsnws/event/1/"));
			} catch (MalformedURLException e) {
				ExceptionUtils.throwAsRuntimeException(e);
			}

			//create query url
			EventQuery query = new EventQuery();

//			if(minDepth>0)
//				query.setMinDepth(new BigDecimal(minDepth));
//			if(maxDepth>0)
//				query.setMaxDepth(new BigDecimal(maxDepth));
			query.setMinMagnitude(new BigDecimal(minMag));
			query.setMaxMagnitude(new BigDecimal(maxMag));

			query.setMinLatitude(new BigDecimal(minLat));
			query.setMaxLatitude(new BigDecimal(maxLat));

			query.setMinLongitude(new BigDecimal(minLon));
			query.setMaxLongitude(new BigDecimal(maxLon));

			query.setLimit(20000);

			query.setOrderBy(OrderBy.TIME_ASC);
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
			sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

			try {
				Date startDate = sdf.parse(year+"");
				query.setStartTime(startDate);
				Date endDate = sdf.parse((year+1)+"");
				query.setEndTime(endDate);
			} catch (ParseException e1) {
				throw ExceptionUtils.asRuntimeException(e1);
			}
			try {
				//print the URL
				System.out.println(service.getUrl(query, Format.GEOJSON));
			} catch (MalformedURLException e) {
				throw ExceptionUtils.asRuntimeException(e);
			}
			List<JsonEvent> events;
			try {
				//get the events from the query
				events = service.getEvents(query);
			} catch (Exception e) {
				throw ExceptionUtils.asRuntimeException(e);
			}
			System.out.println("Loaded "+events.size()+" events");
			for (JsonEvent event : events)
				catalogList.add(event);
		}
		System.out.println("TOTAL: "+catalogList.size()+" events");
		//write json object to file
		obj.put(name, catalogList);
//		writeToJSONFile(cat,obj);
		
		FileWriter fw = new FileWriter(outputFile);
		fw.write(obj.toJSONString());
		System.out.println("Successfully Copied JSON Object to File...");
		fw.close();
	}

}
