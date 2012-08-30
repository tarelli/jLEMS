package org.lemsml.sim;

import java.util.ArrayList;

import org.lemsml.run.ComponentBehavior;
import org.lemsml.run.RuntimeOutput;
import org.lemsml.run.RuntimeRecorder;
import org.lemsml.util.E;

public class RecorderCollector implements ComponentBehaviorVisitor {

	ArrayList<RuntimeRecorder> recorders;
	
	public RecorderCollector(ArrayList<RuntimeRecorder> al) {
		recorders = al;
	}

	@Override
	public void visit(ComponentBehavior cb) {
		ArrayList<RuntimeRecorder> a = cb.getRuntimeRecorders();
		if (a != null && a.size() > 0) {
			recorders.addAll(a);
			E.info("Added recorder " + a + " from " + cb);
		}
		
	}

	
	
}
