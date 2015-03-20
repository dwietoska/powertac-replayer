package org.powertac.replayer.logtool.message;

import org.powertac.common.Rate;
import org.powertac.replayer.logtool.ReplayerMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Creator for rate entries.
 * 
 * @author DWietoska
 */
@Service("org.powertac.common.Rate")
//@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class RateReplayerMessageImpl implements ReplayerMessage {
	
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

		Rate rate = new Rate();
		
		ReflectionTestUtils.setField(rate, "id", id);
		
		rateCoreMap.setRateCoreMessage(id, rate);
		
		return rate;
	}
	
	/**
	 * Read resolve.
	 * 
	 * @param id Id from object.
	 * @param args Arguments from log entry.
	 */
	@Override
	public Object restoreObject(long id, String[] args) throws Exception {
		
		Rate rate = (Rate) createObject(id, args);
		
		rate.setTariffId(Long.parseLong(args[3]));
		rate.withWeeklyBegin(Integer.parseInt(args[4]));
		rate.withWeeklyEnd(Integer.parseInt(args[5]));
		rate.withDailyBegin(Integer.parseInt(args[6]));
		rate.withDailyEnd(Integer.parseInt(args[7]));
		rate.withTierThreshold(Double.parseDouble(args[8]));
		rate.withFixed(Boolean.parseBoolean(args[9]));
		rate.withMinValue(Double.parseDouble(args[10]));
		rate.withMaxValue(Double.parseDouble(args[11]));
		rate.withNoticeInterval(Long.parseLong(args[12]));
		rate.withExpectedMean(Double.parseDouble(args[13]));
		rate.withMaxCurtailment(Double.parseDouble(args[14]));
		
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

		Rate rate = (Rate) rateCoreMap.getRateCoreMessage((Long) id);
		
	    if (methodname.equals("withValue")) {
	    	
	    	rate.withValue(Double.parseDouble(args[3]));
		} else if (methodname.equals("setTariffId")) {
			
			rate.setTariffId(Long.parseLong(args[3]));
		} else if (methodname.equals("withWeeklyBegin")) {	
			
			rate.withWeeklyBegin(Integer.parseInt(args[3]));
		} else if (methodname.equals("withWeeklyEnd")) {	
			
			rate.withWeeklyEnd(Integer.parseInt(args[3]));
		} else if (methodname.equals("withDailyBegin")) {	
			
			rate.withDailyBegin(Integer.parseInt(args[3]));
		} else if (methodname.equals("withDailyEnd")) {		
			
			rate.withDailyEnd(Integer.parseInt(args[3]));
		} else if (methodname.equals("withTierThreshold")) {	
			
			rate.withTierThreshold(Double.parseDouble(args[3]));
		} else if (methodname.equals("withFixed")) {	
			
			rate.withFixed(Boolean.parseBoolean(args[3]));
		} else if (methodname.equals("withMinValue")) {		
			
			rate.withMinValue(Double.parseDouble(args[3]));
		} else if (methodname.equals("withMaxValue")) {
			
			rate.withMaxValue(Double.parseDouble(args[3]));
		} else if (methodname.equals("withNoticeInterval")) {
			
			rate.withNoticeInterval(Long.parseLong(args[3]));
		} else if (methodname.equals("withExpectedMean")) {	
			
			rate.withExpectedMean(Double.parseDouble(args[3]));
		} else if (methodname.equals("withMaxCurtailment")) {	
			
			rate.withMaxCurtailment(Double.parseDouble(args[3]));
		}
	    
	    // boolean addHourlyCharge (HourlyCharge newCharge, boolean publish)
	}
}
