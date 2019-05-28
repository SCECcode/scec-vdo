package org.scec.vtk.plugins.EarthquakeCatalogPlugin.RelativeIntensity;
/* this class calculates the 98% confidence interval that is displayed on the 
 * Molchan diagrams. It then takes the points and puts them into an array so that 
 * the Mocho class can take them and put them on the graph
 */

public class ConfidenceInterval {
	// this is some hardcore statistical calculation
	public static double[] computeInterval(int N){
		double[]tauArray = new double[N];
		for (int n=1; n<=N; n++){
			double total = 0;
			double lowerTau = 0;
			double upperTau = 1;
			double tau = .5;
			while (Math.abs(total-.02)>.000001){
				total = 0;
				for (int i=n; i<=N; i++){
					double constant = 1;
					for(int j=0; j<i; j++){
						constant *= (double)(N-j)/(double)(i-j);
					}
				    total += constant*Math.pow(tau,i)*Math.pow(1-tau,N-i);
				}
				if (total>.02){
					upperTau = tau;
					tau = (tau+lowerTau)/2;
				}
				else{
					lowerTau = tau;
					tau= (tau+upperTau)/2;
				}
			}
			tauArray[n-1] = tau;
		}	
		return tauArray;
	}
}
