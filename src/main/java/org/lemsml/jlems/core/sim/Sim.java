package org.lemsml.jlems.core.sim;
 
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.lemsml.jlems.core.display.DataViewPort;
import org.lemsml.jlems.core.display.DataViewer;
import org.lemsml.jlems.core.display.DataViewerFactory;
import org.lemsml.jlems.core.display.StateTypeWriter;
import org.lemsml.jlems.core.expression.ParseError;
import org.lemsml.jlems.core.logging.E;
import org.lemsml.jlems.core.out.EventResultWriter;
import org.lemsml.jlems.core.out.ResultWriter;
import org.lemsml.jlems.core.out.ResultWriterFactory;
import org.lemsml.jlems.core.run.ConnectionError;
import org.lemsml.jlems.core.run.EventManager;
import org.lemsml.jlems.core.run.RunConfig;
import org.lemsml.jlems.core.run.RuntimeDisplay;
import org.lemsml.jlems.core.run.RuntimeError;
import org.lemsml.jlems.core.run.RuntimeEventOutput;
import org.lemsml.jlems.core.run.RuntimeEventRecorder;
import org.lemsml.jlems.core.run.RuntimeOutput;
import org.lemsml.jlems.core.run.RuntimeRecorder;
import org.lemsml.jlems.core.run.StateInstance;
import org.lemsml.jlems.core.run.StateType;
import org.lemsml.jlems.core.type.Component;
import org.lemsml.jlems.core.type.Meta;
import org.lemsml.jlems.core.type.Target;
 


public class Sim extends LemsProcess {

    StateType rootBehavior;
    StateType targetBehavior;
    
    StateInstance currentRootState;
     
    HashMap<String, DataViewer> dvHM;
    HashMap<String, ResultWriter> rwHM;
    HashMap<String, EventResultWriter> erwHM;
    
    ArrayList<ResultWriter> resultWriters = new ArrayList<ResultWriter>();
    ArrayList<EventResultWriter> eventResultWriters = new ArrayList<EventResultWriter>();
    
    ArrayList<RunConfig> runConfigs;
    
    int maxExecutionTime = 0;
    
    EventManager eventManager;

    public long simulationStartTime = -1; 
    public long simulationEndTime = -1;  
    public long simulationSaveTime = -1; 
    public double[] times;
    
    
    
    public Sim(String srcStr) {
    	super(srcStr);
    }
    
    public Map<String, DataViewer> getDvHM() {
    	return dvHM;
    }
 
 	public List<RunConfig> getRunConfigs() {
 		return runConfigs;
 	}
    	
    public void build() throws ContentError, ConnectionError, ParseError {
    	
    	applySubstitutions();
    	
    	eventManager = EventManager.getInstance();
    	
  	    Target dr = lems.getTarget();
 	    Component simCpt = dr.getComponent();
	
	    if (simCpt == null) {
	        E.error("No such component: " + dr.component + " as referred to by default simulation.");
	        E.error(lems.textSummary());
	        throw new ContentError("No such component " + dr.component);
	    }
 	
	    rootBehavior = simCpt.getStateType();
	    
	    // collect everything in the StateType tree that makes a display
	    ArrayList<RuntimeDisplay> runtimeDisplays = new ArrayList<RuntimeDisplay>();
	    DisplayCollector oc = new DisplayCollector(runtimeDisplays);
	    rootBehavior.visitAll(oc);
	   
	    // build the displays and keep them in dvHM
	    dvHM = new HashMap<String, DataViewer>();
	    for (RuntimeDisplay ro : runtimeDisplays) {
	    	DataViewer dv = DataViewerFactory.getFactory().newDataViewer(ro.getTitle());
	    	dvHM.put(ro.getID(), dv);
	    	if (dv instanceof DataViewPort) {
	    		((DataViewPort)dv).setRegion(ro.getBox());
 	    	}
	    }
	     
	 
	    // collect everything in the StateType tree that records something
	    ArrayList<RuntimeOutput> runtimeOutputs = new ArrayList<RuntimeOutput>();
	    OutputCollector oco = new OutputCollector(runtimeOutputs);
	    rootBehavior.visitAll(oco);
	   
	    // build the outputs and keep them in rwHM
	    rwHM = new HashMap<String, ResultWriter>();
 	    for (RuntimeOutput ro : runtimeOutputs) {
 	    	ResultWriter rw = ResultWriterFactory.getFactory().newResultWriter(ro);
            //System.out.println("Putting "+ro.getID()+" in rwHM");
	    	rwHM.put(ro.getID(), rw);
	    	resultWriters.add(rw);
	    }
        
	 
	    // collect everything in the StateType tree that records events
	    ArrayList<RuntimeEventOutput> runtimeEventOutputs = new ArrayList<RuntimeEventOutput>();
	    EventOutputCollector eoco = new EventOutputCollector(runtimeEventOutputs);
	    rootBehavior.visitAll(eoco);
	   
	    // build the event outputs and keep them in erwHM
	    erwHM = new HashMap<String, EventResultWriter>();
 	    for (RuntimeEventOutput reo : runtimeEventOutputs) {
 	    	EventResultWriter erw = ResultWriterFactory.getFactory().newEventResultWriter(reo);
            //System.out.println("Putting "+reo.getID()+" in erwHM");
	    	erwHM.put(reo.getID(), erw);
	    	eventResultWriters.add(erw);
	    }
        
        
	    runConfigs = new ArrayList<RunConfig>();
        
	    RunConfigCollector rcc = new RunConfigCollector(runConfigs);
	    rootBehavior.visitAll(rcc);
	}

    
    public void run() throws ConnectionError, ContentError, RuntimeError, ParseError {
    	run(true);
    }
    
    public void runTree() throws ConnectionError, ContentError, RuntimeError, ParseError {
    	run(false);
    }
    
    private void run(boolean flatten) throws ConnectionError, ContentError, RuntimeError, ParseError {
     	for (RunConfig rc : runConfigs) {
    		run(rc, flatten);
    	}
      }

    
    public void runWithMeta() throws ConnectionError, ContentError, RuntimeError, ParseError {
    	for (RunConfig rc : runConfigs) {
    		run(rc, false);
    	}
    }
    
    public StateType getRootBehavior() {
        return rootBehavior;
    }
    
    public StateType getTargetBehavior() {
        return targetBehavior;
    }
    
    public StateInstance getCurrentRootState() {
        return currentRootState;
    }
  
    /*
    Temporary method for testing 
    
    public StateInstance getRootState(boolean flatten) throws ContentError, ParseError, ConnectionError, RuntimeError {
        
  		StateType raw = runConfigs.get(0).getTarget();
  		if (flatten) {
  			targetBehavior = raw.getConsolidatedStateType("root");
  		} else {
  			targetBehavior = raw;
  		}
  	    StateInstance rootState = lems.build(targetBehavior, eventManager);
        
        return rootState;
    }*/

    
    public void run(RunConfig rc, boolean flatten) throws ConnectionError, ContentError, RuntimeError, ParseError {
   	    	
  		StateType raw = rc.getTarget();
  	
  		Component cpt = rc.getControlComponent();
  		
  		boolean mflat = flatten;

  		if (cpt != null) {
	  		E.info("checking metas " + cpt.getID() + " " + cpt.metas.size());
	  		
	  		for (Meta m : cpt.metas.getContents()) {
	  			HashMap<String, String> hm = m.getAttributes();
	  			if (hm.containsKey("method")) {
	  				String val = hm.get("method").toLowerCase();
	  				if (val.equals("rk4")) {
	  					mflat = true;
	  					E.info("Got meta for jlems: " + val);
	  					
	  				} else if (val.equals("eulertree")) {
	  					mflat = false; 
	  					E.info("Got meta for jlems: " + val);
	  					
	  				} else {
	  					E.warning("unrecognized method " + val);
	  				}
	  			}
	  			
	  		}
  		}
  		
  		
  		if (mflat) {
  			targetBehavior = raw.getConsolidatedStateType("root");
  		} else {
  			targetBehavior = raw;
  		}
  		
  	    currentRootState = lems.build(targetBehavior, eventManager);
  	
  	    RunnableAccessor ra = new RunnableAccessor(currentRootState);
  	       
  	    ArrayList<RuntimeRecorder> recorders = rc.getRecorders();
  	    
  	    for (RuntimeRecorder rr : recorders) {
  	    	String disp = rr.getDisplay();
  	    	if (dvHM.containsKey(disp)) {
  	    		rr.connectRunnable(ra, dvHM.get(disp));
  	    	
  	    	} else if (rwHM.containsKey(disp)) {
  	    		ResultWriter rw = rwHM.get(disp);
  	    		rw.addedRecorder();
  	    		rr.connectRunnable(ra, rw);
                //System.out.println("Connected ["+rw.getID()+"] to ["+rr.toString()+"]");
  	    		
  	    	} else {
  	    		throw new ConnectionError("No such data viewer " + disp + " needed for " + rr);
  	    	}
  	    }
  	       
  	    ArrayList<RuntimeEventRecorder> eventRecorders = rc.getEventRecorders();
  	    
  	    for (RuntimeEventRecorder rer : eventRecorders) {
  	    	String id = rer.getParent();
  	    	if (erwHM.containsKey(id)) {
  	    		EventResultWriter erw = erwHM.get(id);
  	    		erw.addedRecorder();
  	    		rer.connectRunnable(ra, erw);
  	    		
  	    	} else {
  	    		throw new ConnectionError("No such writer " + id + " needed for [[" + rer.toString() +"]], <<"+erwHM.keySet()+">>, <<"+erwHM.values()+">>");
  	    	}
  	    }
  	    
        double dt = rc.getTimestep();
        int nstep = (int) Math.round(rc.getRuntime() / dt);

 
        simulationStartTime = System.currentTimeMillis();
  
        double t = 0;
        times = new double[nstep+1];
       
       
        currentRootState.initialize(null);  
          
        long realTimeStart = System.currentTimeMillis();
        int nsDone = 0;
        
        try{
     
            for (int istep = 0; istep <= nstep; istep++) {
                if (istep > 0) {
                    eventManager.advance(t);
                    currentRootState.advance(null, t, dt);
                }


                for (ResultWriter rw : resultWriters) {
                    rw.advance(t);
                }
                
                for (EventResultWriter erw : eventResultWriters) {
                    erw.advance(t);
                }

                for (RuntimeRecorder rr : recorders) {
                    rr.appendState(t);
                }

                times[istep] = t;

                t += dt;

                if (maxExecutionTime > 0 && istep % 100 == 0) {
                    long realTimeNow = System.currentTimeMillis();
                    long dtReal = realTimeNow - realTimeStart;
                    if (dtReal > maxExecutionTime) {
                        E.info("Stopped execution at t=" + t + " (exceeded maxExecutionTime) " + (dtReal));
                        break;
                    }
                }
                nsDone = istep;
            }
        } catch (RuntimeError rt) {
            E.error(rt.toString());
            rt.printStackTrace();
            
            String info = "Error occurred when running jLEMS!";
            if (t>0) {
                info+="\n\nNote: LEMS model description was successfully parsed, and simulation started (t = "+(float)t+" sec)\n"
                        + "This error *MAY* be caused by too large a time step (dt = "+dt+" sec currently)\n"
                        + "Try reducing the 'step' attribute in the <Simulation> element";
            }
            E.informativeError(info);
            System.exit(0);
        }
        
        simulationEndTime = System.currentTimeMillis();
        
        E.info("Finished " + nsDone + " steps "
                + "in "+((simulationEndTime-simulationStartTime)/1000f)+" seconds (sim duration: "+rc.getRuntime()*1000+"ms; dt: "+dt*1000+"ms)");
    	        
        for (ResultWriter rw : resultWriters) {
    		rw.advance(t);
    		rw.close();
    	}  
        for (EventResultWriter erw : eventResultWriters) {
    		erw.advance(t);
    		erw.close();
    	}
        
        simulationSaveTime = System.currentTimeMillis();
    }

    
	public void printCB() throws ContentError, ParseError {
		StateTypeWriter cbw = new StateTypeWriter();
		for (RunConfig rc : runConfigs) {
			
			cbw.print(rc.getTarget());
			
		}
		
	}
	
	
	public void printFirstConsolidated() throws ContentError, ParseError, RuntimeError {
		StateTypeWriter cbw = new StateTypeWriter();
		if (!runConfigs.isEmpty()) {
			RunConfig rc = runConfigs.get(0);
			
			StateType cb = rc.getTarget().getConsolidatedStateType("root");
			cbw.print(cb);
		}
		 
		
	}

	public void setMaxExecutionTime(int nms) {
		maxExecutionTime = nms;
	}
	
	
}
