package org.lemsml.jlems.core.run;

import org.lemsml.jlems.core.logging.E;

public class BuildGammaDistribution extends BuildDistribution {

	double mean;
	double shape;
	
	
	public BuildGammaDistribution(double m, double s) {
		mean = m;
		shape = s;
	}

	public double getSample() {
		return RunRandom.getInstance().nextGamma(mean, shape);
	}

}
