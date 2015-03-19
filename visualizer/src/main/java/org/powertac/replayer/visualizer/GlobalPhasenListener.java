package org.powertac.replayer.visualizer;

import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;

import org.powertac.replayer.TimeslotRunnerGeneral;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

/**
 * Represent the JSF lifecycle. 
 * 
 * @author DWietoska
 */
@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class GlobalPhasenListener implements PhaseListener {
	 
	/**
	 * SerialVersionUID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Access to session scope bean LogParametersBean.
	 */
	@Autowired
	private LogParametersBean logParametersBean;
	
	/**
	 * Current clock rate.
	 */
	private long tickInterval;
	
	/**
	 * Current timeslot
	 */
	private int timeslot;
	
	/**
	 * Says whether Timeslotrunner should start again.
	 */
	private boolean restart;
	
	/**
	 * Starts the timeslotrunner again, if it has been stopped at before phase.
	 * 
	 * @param PhaseEvent Jsf phase
	 */
	@Override
	public void afterPhase(PhaseEvent phase) {
	
		if (phase.getPhaseId() == PhaseId.RENDER_RESPONSE) {

			FacesContext ctx = phase.getFacesContext();
			Map<String, String> headers = ctx.getExternalContext()
					.getRequestHeaderMap();
			
			// No Ajax-Request. This means, a website was loaded new.
			if (!(headers.get("Faces-Request") != null
					&& headers.get("Faces-Request").equals("partial/ajax"))) {
		
				// webflow url not restart.
				if (this.logParametersBean.isReplayerUrl()) {
					restart = false;
				}
	
	    		if (restart) {

	    			restart = false;
	    			
	    			if (logParametersBean.getCurrentRunner() != null) {
	    				logParametersBean.getCurrentRunner().createRunner(tickInterval,
	    						timeslot, true);
	    			}
	    			
	    		}
			}
		}
	}

	/**
	 * Stops the Timeslotrunner when he runs. After reloading a page Primefaces 
	 * p:socket component requires time to initialize her state. Without this, 
	 * the timeslot will become lost in Highchart.
	 * 
	 * @param PhaseEvent Jsf phase
	 */
	@Override
	public void beforePhase(PhaseEvent phase) {

		if (phase.getPhaseId() == PhaseId.RESTORE_VIEW) {

			FacesContext ctx = phase.getFacesContext();
			Map<String, String> headers = ctx.getExternalContext().getRequestHeaderMap();
			
			// No Ajax-Request. This means, a website was loaded new.
			if (!(headers.get("Faces-Request") != null && 
					headers.get("Faces-Request").equals("partial/ajax"))) {
	
				TimeslotRunnerGeneral timeslotRunner = null;
				if (logParametersBean.getCurrentRunner() != null) {
	    	    	timeslotRunner = logParametersBean.getCurrentRunner()
	    	    			.getLogTimeslotRunner();
				}

        		if (timeslotRunner != null && 
        				timeslotRunner.isRunning()) {
 			
        			restart = true;
        			
        			timeslotRunner.cancelTimer();
        			tickInterval = timeslotRunner.getTickInterval();
        			timeslot = timeslotRunner.getSafeTimeslot();
        			
        		    // Wait, until Thread is finished.
        			while (timeslotRunner.isRunning());
        		}
			}
		}
	}
	
	@Override
	public PhaseId getPhaseId() {
		return PhaseId.ANY_PHASE;
	}
}
