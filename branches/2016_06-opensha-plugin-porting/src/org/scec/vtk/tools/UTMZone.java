package org.scec.vtk.tools;

public class UTMZone {

	double longitude, iZone, spheroidRegion;

	public void setSpheroidRegion(double sph) {
		if (sph < 1 || sph > 20) {
			System.out.println("Error in Spheroid region value");
			spheroidRegion = 0; //Error in sphreoid region value
			/* If spheroidRegion value rcvd = 0 then err has occurred */
		} else
			spheroidRegion = sph;
	}

	public int getSpheroidRegion() {
		return (int) spheroidRegion;
	}

	public String getSpheroidName() {
		String spheroidName = "";
		int sph;
		sph = (int) spheroidRegion;
		switch (sph) {
			case 1 :
				spheroidName = " Clarke 1866 ";
				break;
			case 2 :
				spheroidName = " Clarke 1880 ";
				break;
			case 3 :
				spheroidName = " Bessel 1967 ";
				break;
			case 4 :
				spheroidName = " New International 1967 ";
				break;
			case 5 :
				spheroidName = " International 1909 ";
				break;
			case 6 :
				spheroidName = " WGS 72 ";
				break;
			case 7 :
				spheroidName = " Everest ";
				break;
			case 8 :
				spheroidName = " WGS 66 ";
				break;
			case 9 :
				spheroidName = " GRS 1980 ";
				break;
			case 10 :
				spheroidName = " Airy ";
				break;
			case 11 :
				spheroidName = " Modified Everest ";
				break;
			case 12 :
				spheroidName = " Modified Airy ";
				break;
			case 13 :
				spheroidName = " Walbeck ";
				break;
			case 14 :
				spheroidName = " Southeast Asia ";
				break;
			case 15 :
				spheroidName = " Australia National - South American 1969 ";
				break;
			case 16 :
				spheroidName = " Krassovsky ";
				break;
			case 17 :
				spheroidName = " Hough ";
				break;
			case 18 :
				spheroidName = " Mercury 1960 (Fischer) ";
				break;
			case 19 :
				spheroidName = " Modified Mercury 1968 (Fischer) ";
				break;
			case 20 :
				spheroidName = " WGS 84 ";
				break;
		}
		return (spheroidName);
	}

	public void setLon(double lon) {
		longitude = lon;
	}

	public void setAllValues(double lon, double sph) {
		if (sph < 1 || sph > 20) {
			System.out.println("Error in Spheroid region value");
			spheroidRegion = 0; //Error in sphreoid region value
		} else
			spheroidRegion = sph;
		longitude = lon;
	}

	public double getUTMZone() {
		double lon, val;
		/* To the Given longitude value add 180 to avoid -ve sign  */
		/* Divide this value by 6 and add one, this would give the */
		/* UTMZone value, reference readme for the table           */
		lon = longitude + 180;
		val = Math.floor(lon / 6);
		iZone = val + 1;
		return iZone;
	}

	public static int calcUTMZone(double lat, double latMin, double lon, double lonMin) {
		final String zoneLetterArray = "CDEFGHJKLMNPQUSTUVWX";	
		//Convert to decimal values
		  lat = lat + latMin/60;
		  lon = lon + lonMin/60;		
		  //From research, error ~.9996/cos(x), where x=degrees away from central meridian in equatorial longitude.
		  //x = degrees away * cos(latitude), for other latitudes.  But I'm not sure of this.
		  //I think 7 horizontal zones (~7% distortion) at the equator is the limit.  That's 42 degrees, and that should be enough.
		  //One day we could try an alternate projection instead.
		  //N-S distortion is constant.
		
		  //Not on this planet;  will move to GUI checking
		  if (Math.abs(lat)>90 || Math.abs(lon)>180) {
			  System.out.println("Invalid coordinates");
			  return -1;
		  }
		
		  //too far north or south
		  if (lat<-80 || lat>84) {
			  System.out.println("Out of UTM Zones");
			  return -2;
		  }
	
		 //String utmZone;
		 int hZone = (int)Math.ceil((lon+180)/6);
		 int vZone = (int)Math.floor((lat+80)/8);	
		 char zoneLetter = zoneLetterArray.charAt(vZone);
		
		//Checks for really bizarre cases in Finland
		if (zoneLetter=='X') {
			 if (hZone == 32 || hZone == 34 || hZone == 36) {
				  if (lon%6<3) {
					  hZone--;
				  } else {
					  hZone++;
				  }
			  }
		 } else if (zoneLetter=='V' && hZone == 31) {
			  if (lon%6>=3) {
				  hZone++;
			  }
		  }
		  Integer zoneNum = new Integer(hZone);
		  //utmZone = zoneNum.toString() + zoneLetterArray.charAt(vZone);
		 // return zoneNum.toString();
		 return zoneNum.intValue(); 
	}

	public UTMZone() {
	}

	public UTMZone(double lon, double sph) {
		longitude = lon;
		spheroidRegion = sph;
	}
}

