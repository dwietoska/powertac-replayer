package org.powertac.replayer.logtool.message;

import java.util.HashMap;
import java.util.Map;

import org.powertac.common.Competition;
import org.powertac.common.CustomerInfo;
import org.powertac.replayer.GameInitialization;
import org.powertac.replayer.logtool.ReplayerMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Creator for competition entries.
 * 
 * @author DWietoska
 */
@Service("org.powertac.common.Competition")
//@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class CompetitionReplayerMessageImpl implements ReplayerMessage, 
		GameInitialization {
	
	/**
	 * All competitions.
	 */
	private Map<Long, Competition> idMap;
	
	/**
	 * Access to customer infos.
	 */
	@Autowired
	private CustomerInfoReplayerMessageImpl customerInfoReplayerMessageImpl;
	
	/**
	 * Default constructor.
	 */
	public CompetitionReplayerMessageImpl() {
		super();
		idMap = new HashMap<Long, Competition>();
	}

	/**
	 * Reset for new replaying.
	 */
	@Override
	public void newGame() {
		idMap = new HashMap<Long, Competition>();
	}

	/**
	 * Creates a new object.
	 * 
	 * @param id Id from object.
	 * @param args Arguments from log entry.
	 */
	@Override
	public Object createObject(long id, String[] args) throws Exception {

		Competition competition = Competition.newInstance(args[3]);	

		// Id have no setters.
		ReflectionTestUtils.setField(competition, "id", id);
		
		idMap.put(id, competition);
		
		return competition;
	}

	/**
	 * Calls a method on the given object.
	 * 
	 * @param id Id.
	 * @param methodname Method name to invoke.
	 * @param args Arguments from log entry.
	 */
	@Override
	public void callMethod(Object id, String methodname, 
			String[] args) throws Exception {
		
		Competition competition = idMap.get(id);
		
	    if (methodname.equals("addCustomer")) {

	    	CustomerInfo customerInfo = (CustomerInfo) 
	    			customerInfoReplayerMessageImpl
	    			.getCustomerInfoMessage(Long.parseLong(args[3]));
	    	competition.addCustomer(customerInfo);
		} else if (methodname.equals("addBroker")) {
			
			competition.addBroker(args[3]);
		} else if (methodname.equals("withSimulationBaseTime")) {
			
			competition.withSimulationBaseTime(Long.parseLong(args[3]));
		} else if (methodname.equals("withMinimumTimeslotCount")) {
			
			competition.withMinimumTimeslotCount(Integer.parseInt(args[3]));
		} else if (methodname.equals("withExpectedTimeslotCount")) {
			
			competition.withExpectedTimeslotCount(Integer.parseInt(args[3]));
		} else if (methodname.equals("withTimezoneOffset")) {
			
			competition.withTimezoneOffset(Integer.parseInt(args[3]));
		} else if (methodname.equals("withLatitude")) {
			
			competition.withLatitude(Integer.parseInt(args[3]));
		} else if (methodname.equals("withBootstrapTimeslotCount")) {
			
			competition.withBootstrapTimeslotCount(Integer.parseInt(args[3]));
		} else if (methodname.equals("withBootstrapDiscardedTimeslots")) {
			
			competition.withBootstrapDiscardedTimeslots(Integer.parseInt(args[3]));
		} else if (methodname.equals("withTimeslotLength")) {
			
			competition.withTimeslotLength(Integer.parseInt(args[3]));
		} else if (methodname.equals("withSimulationRate")) {
			
			competition.withSimulationRate(Long.parseLong(args[3]));
		} else if (methodname.equals("withTimeslotsOpen")) {
			
			competition.withTimeslotsOpen(Integer.parseInt(args[3]));
		} else if (methodname.equals("withDeactivateTimeslotsAhead")) {
			
			competition.withDeactivateTimeslotsAhead(Integer.parseInt(args[3]));
		} else if (methodname.equals("withMinimumOrderQuantity")) {
			
			competition.withMinimumOrderQuantity(Double.parseDouble(args[3]));
		} else if (methodname.equals("withSimulationModulo")) {
			
			competition.withSimulationModulo(Long.parseLong(args[3]));
		} else if (methodname.equals("withSimulationTimeslotSeconds")) {
			
			competition.withSimulationTimeslotSeconds(Integer.parseInt(args[3]));
		} else if (methodname.equals("withDescription")) {
			
			competition.withDescription(args[3]);
		} else if (methodname.equals("update")) {

			Competition newCompetition = idMap.get(Long.parseLong(args[3]));
			competition.update(newCompetition);
		}
	}
	
	/**
	 * Read resolve.
	 * 
	 * @param id Id from object.
	 * @param args Arguments from log entry.
	 */
	@Override
	public Object restoreObject(long id, String[] args) throws Exception {
		// Competition has no readResolve
		return null;
	}
}
