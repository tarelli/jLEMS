package org.lemsml.eval;

import java.util.ArrayList;

public abstract class DVal {

	public abstract double eval();

	public abstract void recAdd(ArrayList<DVar> val);

    @Override
	public abstract String toString();

	public abstract String toString(String prefix, ArrayList<String> ignore);

	public abstract DVal makeCopy();

	public abstract DVal makePrefixedCopy(String pfx);
	
}