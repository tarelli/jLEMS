package org.lemsml.eval;

public class GTComp extends BComp {

	
	public GTComp(DVal dvl, DVal dvr) {
		super(dvl, dvr);
	}

	 
	public boolean eval() {
		return (left.eval() > right.eval());
	}
	
	
	
}