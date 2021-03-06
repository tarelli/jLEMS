package org.lemsml.jlems.io.out;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.lemsml.jlems.core.logging.E;
import org.lemsml.jlems.core.out.ResultWriter;
import org.lemsml.jlems.core.run.RuntimeError;
import org.lemsml.jlems.core.run.RuntimeOutput;
import org.lemsml.jlems.io.util.FileUtil;

public class StringBuilderResultWriter implements ResultWriter {

	String id;
	
	String fileName;
	
	int colCount;
	StringBuilder sb = new StringBuilder();
	ArrayList<double[]> dat;
	
	int wkCount;
	double[] wk = null;
	boolean newFile = true;
	
	boolean verbose = false;
	
	
	public StringBuilderResultWriter(RuntimeOutput ro) {
		 id = ro.getID();
		 fileName = ro.getFileName();
		 colCount = 1;
		 dat = new ArrayList<double[]>();
	}
	
	public String getID()
	{
		return id;
	}
	   
	
	@Override
	public void addPoint(String id, double x, double y) {

    	if (verbose) System.out.println("addPoint: "+id+", "+wkCount+" ("+(float)x+", "+(float)y+", ...)");
		wk[wkCount] = y;
		wkCount += 1;
	
	}
	
	
	public void advance(double t) throws RuntimeError {

		if (wk != null) {
			if (verbose) System.out.println(".. advance: "+(float)t+", "+ wk.length+" points  ("+(float)wk[0]+", "+ (wk.length>1 ? (float)wk[1]: "--")+", ...)");
			dat.add(wk);
			if (verbose) System.out.println("a Last dat of "+dat.size()+" ("+(float)dat.get(dat.size()-1)[0]+", "+(float)dat.get(dat.size()-1)[1]+", ...)");
		} else {
			if (verbose) System.out.println(".. no data advance...");
		}

    	
		if (dat.size() > 1000) {
			flush();
		}
		
		wk = new double[colCount];
		wk[0] = t;
		wkCount = 1;
	}



	@Override
	public void addedRecorder() {
		colCount += 1;
	}
	
	public void flush() throws RuntimeError {

		if (verbose) System.out.println("--------------\nf Last dat ("+(float)dat.get(dat.size()-1)[0]+", "+(float)dat.get(dat.size()-1)[1]+", ...)");
		for (double[] d : dat) {
			for (int i = 0; i < d.length; i++) {
				sb.append((float)d[i]);
				sb.append("\t");
			}
			sb.append("\n");
		}

		if (verbose) {
	    	System.out.println("Flushed "+dat.size()+" sets of points...");
	    	System.out.println("("+(float)dat.get(0)[0]+", "+(float)dat.get(0)[1]+", ...)");
	    	System.out.println("...");
	    	System.out.println("("+(float)dat.get(dat.size()-1)[0]+", "+(float)dat.get(dat.size()-1)[1]+", ...)");
		}
		
		dat = new ArrayList<double[]>();
		
	}

	
	@Override
	public void close() throws RuntimeError {
		if (verbose) System.out.println("close()...");
		flush();
	
	}
	
	public StringBuilder getSBOutput()
	{
		return sb;
	}

	
	
}
