package org.scec.vtk.plugins.GISHazusEventsPlugin;

public class EventAttributes {
	private String ID;
	private String eventName;
	private String dbfFile;
	private String shpFile;
	private String column;
	private String likeEarthquake;
	private String legendTitle;
	
	EventAttributes(){
		ID = null;
		eventName = null;
		dbfFile = null;
		shpFile = null;
		column = null;
		likeEarthquake = null;
		legendTitle = null;
	}
	
	public String getID(){
		return ID;
	}
	
	public void setID(String id){
		ID = id;
	}
	
	public String getEventName(){
		return eventName;
	}
	
	public void setEventName(String earthquake){
		eventName = earthquake;
	}
	
	public String getDBFFile(){
		return dbfFile;
	}
	
	public void setDBFFile(String fileName){
		dbfFile = fileName;
	}
	
	public String getSHPFile(){
		return shpFile;
	}
	
	public void setSHPFile(String fileName){
		shpFile = fileName;
	}
	
	public String getColumn(){
		return column;
	}
	
	public void setColumn(String columnName){
		column = columnName;
	}
	
	public String getLikeEarthquake(){
		return likeEarthquake;
	}
	
	public void setLikeEarthquake(String like_earthquake){
		likeEarthquake = like_earthquake;
	}
	
	public String getLegendTitle(){
		return legendTitle;
	}
	
	public void setLegendTitle(String timeOrType){
		legendTitle = timeOrType;
	}
}
