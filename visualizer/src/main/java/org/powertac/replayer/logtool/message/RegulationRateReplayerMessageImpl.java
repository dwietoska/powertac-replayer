package org.powertac.replayer.logtool.message;

import org.powertac.common.RegulationRate;
import org.powertac.common.RegulationRate.ResponseTime;
import org.powertac.replayer.logtool.ReplayerMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Creator for regulation rate entries.
 * 
 * @author DWietoska
 */
@Service("org.powertac.common.RegulationRate")
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class RegulationRateReplayerMessageImpl implements ReplayerMessage {

	/**
	 * Access to rates map.
	 */
	@Autowired
	private RateCoreMap rateCoreMap;
	
	/**
	 * Creates a new object.
	 * 
	 * @param id Id from object.
	 * @param args Arguments from log entry.
	 */
	@Override
	public Object createObject(long id, String[] args) throws Exception {

		RegulationRate regulationRate = new RegulationRate();
		
		ReflectionTestUtils.setField(regulationRate, "id", id);
		
		rateCoreMap.setRateCoreMessage(id, regulationRate);
		
		return regulationRate;
	}
	
	/**
	 * Read resolve.
	 * 
	 * @param id Id from object.
	 * @param args Arguments from log entry.
	 */
	@Override
	public Object restoreObject(long id, String[] args) throws Exception {
		
		RegulationRate rate = (RegulationRate) createObject(id, args);
		
		rate.setTariffId(Long.parseLong(args[3]));
		
		if (args[4].equals("SECONDS")) {
			
			rate.withResponse(ResponseTime.SECONDS);
		} else {
			
			rate.withResponse(ResponseTime.MINUTES);
		}
		
		rate.withUpRegulationPayment(Double.parseDouble(args[5]));
		rate.withDownRegulationPayment(Double.parseDouble(args[6]));
		
		return rate;
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

		RegulationRate rate = (RegulationRate) rateCoreMap
				.getRateCoreMessage((Long) id);
		 
	    if (methodname.equals("setTariffId")) {	
	    	
			rate.setTariffId(Long.parseLong(args[3]));
		} else if (methodname.equals("withResponse")) {	
			
			if (args[3].equals("SECONDS")) {
				
				rate.withResponse(ResponseTime.SECONDS);
			} else {
				
				rate.withResponse(ResponseTime.MINUTES);
			}
		} else if (methodname.equals("withUpRegulationPayment")) {	
			
			rate.withUpRegulationPayment(Double.parseDouble(args[3]));
		} else if (methodname.equals("withDownRegulationPayment")) {
			
			rate.withDownRegulationPayment(Double.parseDouble(args[3]));
		}
	}
}
