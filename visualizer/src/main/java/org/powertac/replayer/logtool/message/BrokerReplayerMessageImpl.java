package org.powertac.replayer.logtool.message;

import org.powertac.common.Broker;
import org.powertac.common.MarketPosition;
import org.powertac.replayer.logtool.ReplayerMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Creator for broker entries.
 * 
 * @author DWietoska
 */
@Service("org.powertac.common.Broker")
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class BrokerReplayerMessageImpl implements ReplayerMessage {

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
	 * Default constructor.
	 */
	public BrokerReplayerMessageImpl() {
		super();
	}
	
	/**
	 * Creates a new object.
	 * 
	 * @param id Id from object.
	 * @param args Arguments from log entry.
	 */
	@Override
	public Object createObject(long id, String[] args) throws Exception {
		
		Broker broker;
		
		if (args.length == 4) {
			
			broker = new Broker(args[3]);
		} else {
			
			broker = new Broker(args[3], Boolean.parseBoolean(args[4]),  
					Boolean.parseBoolean(args[5]));
		}
		
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

		Broker broker = brokerMap.getBrokerMessage((Long) id);
		
		//@StateChange logged
	    if (methodname.equals("addMarketPosition")) {

	    	MarketPosition marketPosition = marketPositionMessageImpl
	    			.getMarketPositionMessage(Long.parseLong(args[3]));
	    	
	    	broker.addMarketPosition(marketPosition, Integer.parseInt(args[4]));
		} else if (methodname.equals("setLocal")) {
			
			broker.setLocal(Boolean.parseBoolean(args[3]));
		} else if (methodname.equals("setWholesale")) {
			
			broker.setWholesale(Boolean.parseBoolean(args[3]));
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
