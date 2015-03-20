package org.powertac.replayer.logtool.message;

import java.util.List;

import org.powertac.common.Broker;
import org.powertac.common.MarketPosition;
import org.powertac.genco.CpGenco;
import org.powertac.replayer.logtool.ReplayerDomainObjectReader;
import org.powertac.replayer.logtool.ReplayerMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Creator for genco entries.
 * 
 * @author DWietoska
 */
@Service("org.powertac.genco.CpGenco")
//@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class GencoBrokerReplayerMessageImpl implements ReplayerMessage {

	/**
	 * Access to replayer domain object reader.
	 */
	@Autowired
	private ReplayerDomainObjectReader replayerDomainObjectReader;
	
	/**
	 * Access to market positions.
	 */
	@Autowired
	private MarketPositionReplayerMessageImpl marketPositionMessageImpl;
	
	/**
	 * Access to broker map.
	 */
	@Autowired
	private BrokerMap brokerMap;
	
	/**
	 * Creates a new object.
	 * 
	 * @param id Id from object.
	 * @param args Arguments from log entry.
	 */
	@Override
	public Object createObject(long id, String[] args) throws Exception {

		Broker broker = new CpGenco(args[3]);
		
		ReflectionTestUtils.setField(broker, "id", id);
		
		brokerMap.setBrokerMessage(id, broker);
		
		return broker;
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

		CpGenco broker = (CpGenco) brokerMap.getBrokerMessage((Long) 
				id);
		
		//@StateChange logged
	    if (methodname.equals("addMarketPosition")) {

	    	MarketPosition marketPosition = marketPositionMessageImpl
	    			.getMarketPositionMessage(Long.parseLong(args[3]));
	    	broker.addMarketPosition(marketPosition, Integer.parseInt(args[4]));
		} else if (methodname.equals("setLocal")) {
			
			broker.setLocal(Boolean.parseBoolean(args[3]));
		} else if (methodname.equals("setWholesale")) {
			
			broker.setWholesale(Boolean.parseBoolean(args[3]));
		} else if (methodname.equals("withCoefficients")) {
			
	        List<String> listArg = (List<String>) 
	        		replayerDomainObjectReader.resolveListString(
	        				String.class, args[3]);
			broker.withCoefficients(listArg);
		} else if (methodname.equals("withPSigma")) {
			
			broker.withPSigma(Double.parseDouble(args[3]));
		} else if (methodname.equals("withQSigma")) {
			
			broker.withQSigma(Double.parseDouble(args[3]));
		} else if (methodname.equals("withRwaSigma")) {
			
			broker.withRwaSigma(Double.parseDouble(args[3]));
		} else if (methodname.equals("withRwaOffset")) {
			
			broker.withRwaOffset(Double.parseDouble(args[3]));
		} else if (methodname.equals("withRwcSigma")) {
			
			broker.withRwcSigma(Double.parseDouble(args[3]));
		} else if (methodname.equals("withRwcOffset")) {
			
			broker.withRwcOffset(Double.parseDouble(args[3]));
		} else if (methodname.equals("withPriceInterval")) {
			
			broker.withPriceInterval(Double.parseDouble(args[3]));
		} else if (methodname.equals("withMinQuantity")) {
			
			broker.withMinQuantity(Double.parseDouble(args[3]));
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
		// Broker has no readResolve
		return null;
	}
}
