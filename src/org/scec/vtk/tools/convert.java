package org.scec.vtk.tools;

/************************************************************************/
/*Class to convert from Latitude, Longitude to UTM values and vice-versa*/
/************************************************************************/

public class convert {
	/*********************************************************************/
	/************************  Global Variables  *************************/

	double AXIS[] =
		{
			6378206.4,
			6378249.145,
			6377397.155,
			6378157.5,
			6378388,
			6378135,
			6377276.3452,
			6378145,
			6378137,
			6377563.396,
			6377304.063,
			6377341.89,
			6376896.0,
			6378155.0,
			6378160,
			6378245,
			6378270,
			6378166,
			6378150,
			6378137 };

	double BXIS[] =
		{
			6356583.8,
			6356514.86955,
			6356078.96284,
			6356772.2,
			6356911.94613,
			6356750.519915,
			6356075.4133,
			6356759.769356,
			6356752.31414,
			6356256.91,
			6356103.039,
			6356036.143,
			6355834.8467,
			6356773.3205,
			6356774.719,
			6356863.0188,
			6356794.343479,
			6356784.283666,
			6356768.337303,
			6356752.31414 };

	double AK0 = 0.9996;

	double A, B, ES, IZONE;

	double RADSEC = 206264.8062470964; //Radian Conversion

	double CVAL1, CVAL2, IIZONE = 100;
	// 100 is chosen coz # of regions are 60
	//  so it would never reach this number
	double IUTZ = 0, CM = 0;

	double spheroidRegion;

	double vt[] = { 0.0, 0.0 };

	/*********************************************************************/

	/*************  Function to convert from lat lon to UTM  *************/
	public double[] LatLonToUTM() {

		vt[0] = vt[1] = 0;
		double IIZ = 0;
		double PHI, DLAM, EPRI;
		double EN, T, C, AA, S2, S4, S6, F1, F2, F3, F4, EM, XX, YY;

		double secLAT, secLON;

		secLAT = CVAL1 * 3600;
		secLON = - (CVAL2 * 3600);

		/*** Uses another class defined UTMZone, used as book-keeping ***/

		UTMZone uz = new UTMZone(CVAL1, spheroidRegion);
		uz.setLon(CVAL2);
		/******** Constructor accepts Lon, SpheroidRegion Code **********/
		/* Spheroid region codes can be referenced from the readme file */

		A = AXIS[uz.getSpheroidRegion() - 1]; //Returns Integer value 
		B = BXIS[uz.getSpheroidRegion() - 1]; //Returns Integer value
		/********* index-1 since array starts from zero in java *********/

		ES = (Math.pow(A, 2) - Math.pow(B, 2)) / Math.pow(A, 2);

		if (IIZONE != 100)
			IZONE = IIZONE; // getting user UTM zone value
		else
			IZONE = uz.getUTMZone(); //Returns Integer value

		if (IZONE == 0) {
			IIZ = secLON / 21600;
			IZONE = 30 - IIZ;
			if (secLON < 0)
				IZONE = 31 - IIZ;
		}
		if (IZONE <= 30) {
			IUTZ = 30 - Math.abs(IZONE);
			CM = ((IUTZ * 6) + 3) * (3600);
		} else {
			IUTZ = Math.abs(IZONE) - 30;
			CM = ((IUTZ * 6) - 3) * (-3600);
		}

		if (secLAT < 0)
			IZONE = -Math.abs(IZONE);
		if (IZONE < 0)
			secLAT = -Math.abs(secLAT);

		/***** All the Mathematics involved to do the conversion ******/
		PHI = secLAT / RADSEC;
		DLAM = - (secLON - CM) / RADSEC;
		EPRI = ES / (1 - ES);
		EN = A / Math.sqrt(1 - ES * (Math.pow(Math.sin(PHI), 2)));
		T = Math.pow(Math.tan(PHI), 2);
		C = EPRI * Math.pow(Math.cos(PHI), 2);
		AA = DLAM * Math.cos(PHI);
		S2 = Math.sin(2 * PHI);
		S4 = Math.sin(4 * PHI);
		S6 = Math.sin(6 * PHI);
		F1 = (1 - (ES / 4) - (3 * ES * ES / 64) - (5 * ES * ES * ES / 256));
		F2 = ((3 * ES / 8) + (3 * ES * ES / 32) + (4.5 * ES * ES * ES / 1024));
		F3 = ((15 * ES * ES / 256) + (45 * ES * ES * ES / 1024));
		F4 = (35 * ES * ES * ES / 3072);
		EM = A * (F1 * PHI - F2 * S2 + F3 * S4 - F4 * S6);

		XX =
			AK0
				* EN
				* (AA
					+ (1 - T + C) * Math.pow(AA, 3) / 6
					+ (5 - 18 * T + T * T + 72 * C - 58 * EPRI)
						* Math.pow(AA, 5)
						/ 120);
		XX = XX + 500000;

		YY =
			AK0
				* (EM
					+ EN
						* Math.tan(PHI)
						* ((AA * AA / 2)
							+ (5 - T + 9 * C + 4 * C * C) * Math.pow(AA, 4) / 24
							+ (61 - 58 * T + T * T + 600 * C - 330 * EPRI)
								* Math.pow(AA, 6)
								/ 720));

		if (IZONE < 0 || secLAT < 0)
			YY = YY + 10000000;

		if (vt[0] == 1.0 && vt[1] == 1.0) {
			return vt; //Error has occurred
		} else {
			vt[0] = XX;
			vt[1] = YY;
		}
		return new double[]{vt[0], vt[1]};
		/* vt array contains the UTMx and UTMy value as vt[0] and vt[1] respectively 
		 * Return a new object so if you call it multiple times there aren't point problems */
	}

	/*************  Function to convert from UTM to lat lon  *************/
	public double[] UTMToLatLon(boolean northernHemisphere) {
		if (!northernHemisphere) {
			CVAL2 = 10000000 - CVAL2;
		}
		
		vt[0] = vt[1] = 0;
		double PHI1,
			UM,
			EPRI,
			E1,
			EM,
			EN,
			T,
			C,
			R,
			D,
			PHI,
			ALAM,
			SLAT,
			SLON,
			DLAM,
			XX,
			YY,
			secLAT,
			secLON;

		UTMZone uz = new UTMZone();
		uz.setSpheroidRegion(spheroidRegion);

		A = AXIS[uz.getSpheroidRegion() - 1]; //Returns Integer value
		B = BXIS[uz.getSpheroidRegion() - 1]; //Returns Integer value
		/********* index-1 since array starts from zero in java *********/

		ES = (Math.pow(A, 2) - Math.pow(B, 2)) / Math.pow(A, 2);

		IZONE = IIZONE; // getting user UTM zone value

		if (IZONE <= 30) {
			IUTZ = 30 - Math.abs(IZONE);
			CM = ((IUTZ * 6) + 3) * (3600);
		} else {
			IUTZ = Math.abs(IZONE) - 30;
			CM = ((IUTZ * 6) - 3) * (-3600);
		}

		YY = CVAL2;
		if (IZONE < 0)
			YY = YY - 10000000;
		XX = CVAL1 - 500000;

		EM = YY / AK0;

		UM =
			EM
				/ (A
					* (1
						- (ES / 4)
						- (3 * ES * ES / 64)
						- (5 * ES * ES * ES / 256)));
		E1 = (1 - Math.sqrt(1 - ES)) / (1 + Math.sqrt(1 - ES));
		PHI1 =
			UM
				+ ((3 * E1 / 2) - (27 * Math.pow(E1, 3) / 32)) * Math.sin(2 * UM)
				+ ((21 * E1 * E1 / 16) - (55 * Math.pow(E1, 4) / 32))
					* Math.sin(4 * UM)
				+ (151 * Math.pow(E1, 3) / 96) * Math.sin(6 * UM);

		EN = A / Math.sqrt(1 - ES * Math.pow(Math.sin(PHI1), 2));
		T = Math.pow(Math.tan(PHI1), 2);
		EPRI = ES / (1 - ES);
		C = EPRI * Math.pow(Math.cos(PHI1), 2);
		R =
			(A * (1 - ES))
				/ (Math.pow(1 - ES * Math.pow(Math.sin(PHI1), 2), 1.5));
		D = XX / (EN * AK0);

		PHI =
			PHI1
				- (EN * Math.tan(PHI1) / R)
					* ((D * D / 2)
						- (5 + 3 * T + 10 * C - 4 * C * C - 9 * EPRI)
							* Math.pow(D, 4)
							/ 24
						+ (61
							+ 90 * T
							+ 298 * C
							+ 45 * T * T
							- 252 * EPRI
							- 3 * C * C)
							* Math.pow(D, 6)
							/ 720);

		ALAM =
			(CM / RADSEC)
				- (D
					- (1 + 2 * T + C) * Math.pow(D, 3) / 6
					+ (5 - 2 * C + 28 * T - 3 * C * C + 8 * EPRI + 24 * T * T)
						* Math.pow(D, 5)
						/ 120)
					/ Math.cos(PHI1);

		secLAT = PHI * RADSEC;
		secLON = ALAM * RADSEC;
		DLAM = - (secLON - CM) / RADSEC;

//		CONV =
//			DLAM
//				* (Math.sin(PHI)
//					+ 0.0000000000019587
//						* Math.pow(DLAM, 2)
//						* Math.sin(PHI)
//						* Math.cos(PHI * PHI));

		SLAT = secLAT / 3600;
		SLON = - (secLON / 3600);

		if (vt[0] == 1.0 && vt[1] == 1.0) {
			return vt; //Error has occurred
		} else {
			vt[0] = SLAT;
			vt[1] = SLON;
		}

		if (Math.abs(secLAT) > 302400 || Math.abs(DLAM) > 0.16) {
			vt[0] = vt[1] = 1.0; //Error occurred
			System.out.println("Error converting to UTM");
			/**** If the value returned in the array is errFlag[0]=1.0 and
			      errFlag[1]=1.0 then error has occurred.                 ****/
		}
		if (northernHemisphere==false) {
			return new double[]{-1*vt[0], vt[1]};
		}
		return new double[]{vt[0], vt[1]};
	}

	/* This function to send UTMx, UTMy */
	public void setUTMxy(double val1, double val2) {
		CVAL1 = val1; //x of UTM
		CVAL2 = val2; //y of UTM
	}

	/* This function to send lat and lon respectively */
	public void setLatLon(double val1, double val2) {
		CVAL1 = val1; //lat
		CVAL2 = val2; //lon
	}

	public void setUTMZone(double zone) {
		if (zone < 1 || zone > 60) {
			System.out.println("Error in zone value");
			vt[0] = vt[1] = 1.0; //Error in zone value
		}
		IIZONE = zone;
	}
	
	public void setSpheroid(double sph) {
		if (sph < 1 || sph > 20) {
			System.out.println("Error in Spheroid region value");
			vt[0] = vt[1] = 1.0; //Error in sphreoid region value
		}
		spheroidRegion = sph;
	}

	public void setAllValues(
		double val1,
		double val2,
		double sph,
		double zone) {
		CVAL1 = val1; //lat or x of UTM
		CVAL2 = val2; //lon or y of UTM
		if (zone < 1 || zone > 60) {
			System.out.println("Error in zone value");
			vt[0] = vt[1] = 1.0; //Error in zone value
		}
		IIZONE = zone; //Set UTM zone
		if (sph < 1 || sph > 20) {
			System.out.println("Error in Spheroid region value");
			vt[0] = vt[1] = 1.0; //Error in sphreoid region value
		}
		spheroidRegion = sph;
	}

	public convert() {
	}

	public convert(double val1, double val2, double sph) {
		//This is used with Default UTM Zone value as per Longitude
		CVAL1 = val1; //lat or x of UTM
		CVAL2 = val2; //lon or y of UTM
		if (sph < 1 || sph > 20) {
			System.out.println("Error in Spheroid region value");
			vt[0] = vt[1] = 1.0; //Error in sphreoid region value
		}
		spheroidRegion = sph; //Spheroid region value
	}
	public convert(double val1, double val2, double sph, double utmzone) {
		//This is used to override the Default UTM Zone value as per Longitude
		CVAL1 = val1; //lat or x of UTM
		CVAL2 = val2; //lon or y of UTM
		if (utmzone < 1 || utmzone > 60) {
			System.out.println("Error in zone value");
			vt[0] = vt[1] = 1.0; //Error in zone value
		}
		IIZONE = utmzone;
		if (sph < 1 || sph > 20) {
			System.out.println("Error in Spheroid region value");
			vt[0] = vt[1] = 1.0; //Error in sphreoid region value
		}
		spheroidRegion = sph;
	}
}


