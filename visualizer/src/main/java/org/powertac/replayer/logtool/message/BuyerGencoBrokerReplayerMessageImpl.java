package org.powertac.replayer.logtool.message;

import org.powertac.common.Broker;
import org.powertac.common.MarketPosition;
import org.powertac.genco.Buyer;
import org.powertac.replayer.logtool.ReplayerMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Creator for buyer entries.
 * 
 * @author DWietoska
 */
@Service("org.powertac.genco.Buyer")
//@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class BuyerGencoBrokerReplayerMessageImpl implements ReplayerMessage {
	
	/**
	 * Access to market position creator.
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

		Broker broker = new Buyer(args[3]);
		
		ReflectionTestUtils.setField(broker, "id", id);
	
		brokerMap.setBrokerMessage(id, broker);

		return broker;
	}

	/**
	 * Calls a method on the given object.
	 * 
	 * @param Id id.
	 * @param methodname Method name to invoke.
	 * @param args Arguments from log entry.
	 */
	@Override
	public void callMethod(Object id, String methodname,
			String[] args) throws Exception {

		Buyer broker = (Buyer) brokerMap.getBrokerMessage((Long) id);
		
		//@StateChange logged
	    if (methodname.equals("addMarketPosition")) {

	    	MarketPosition marketPosition = marketPositionMessageImpl
	    			.getMarketPositionMessage(Long.parseLong(args[3]));
	    	broker.addMarketPosition(marketPosition, Integer.parseInt(args[4]));
		} else if (methodname.equals("setLocal")) {
			
			broker.setLocal(Boolean.parseBoolean(args[3]));
		} else if (methodname.equals("setWholesale")) {
			
			broker.setWholesale(Boolean.parseBoolean(args[3]));
		} else if (methodname.equals("setPriceBeta")) {
			
			broker.setPriceBeta(Double.parseDouble(args[3]));
		} else if (methodname.equals("setMwh")) {
			
			broker.setMwh(Double.parseDouble(args[3]));
		} else if (methodname.equals("withNominalCapacity")) {
			
			broker.withNominalCapacity(Double.parseDouble(args[3]));
		} else if (methodname.equals("withCost")) {
			
			broker.withCost(Double.parseDouble(args[3]));
		} else if (methodname.equals("withVariability")) {
			
			broker.withVariability(Double.parseDouble(args[3]));
		} else if (methodname.equals("withReliability")) {
			
			broker.withReliability(Double.parseDouble(args[3]));
		} else if (methodname.equals("withCommitmentLeadtime")) {
			
			broker.withCommitmentLeadtime(Integer.parseInt(args[3]));
		} else if (methodname.equals("withCarbonEmissionRate")) {
			
			broker.withCarbonEmissionRate(Double.parseDouble(args[3]));
		} else if (methodname.equals("setCurrentCapacity")) {
			
			broker.setCurrentCapacity(Double.parseDouble(args[3]));
		} else if (methodname.equals("setInOperation")) {
			
			broker.setInOperation(Boolean.parseBoolean(args[3]));
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
