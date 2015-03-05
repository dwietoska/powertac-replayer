package org.powertac.replayer.logtool.message;

import org.powertac.common.Broker;
import org.powertac.common.MarketTransaction;
import org.powertac.replayer.logtool.ReplayerMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Creator for market transaction entries.
 * 
 * @author DWietoska
 */
@Service("org.powertac.common.MarketTransaction")
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class MarketTransactionReplayerMessageImpl implements ReplayerMessage {
	
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

		Broker broker = null;

		if (!args[3].equals("null")) {
			
			broker = brokerMap.getBrokerMessage(Long.parseLong(args[3]));
		}

		MarketTransaction marketTransaction = new MarketTransaction(broker,
				Integer.parseInt(args[4]), Integer.parseInt(args[5]),
				Double.parseDouble(args[6]), Double.parseDouble(args[7]));

		ReflectionTestUtils.setField(marketTransaction, "id", id);

		return marketTransaction;
	}
	
	/**
	 * Read resolve.
	 * 
	 * @param id Id from object.
	 * @param args Arguments from log entry.
	 */
	@Override
	public Object restoreObject(long id, String[] args) throws Exception {
		// MarketTransaction has no readResolve
		return null;
	}

	/**
	 * Calls a method on the given object.
	 * 
	 * @param messageobject Object in which a method is called.
	 * @param methodname Method name to invoke.
	 * @param args Arguments from log entry.
	 */
	@Override
	public void callMethod(Object messageobject, String methodname,
			String[] args) throws Exception {
		// MarketTransaction only has getter methods and no setter.
	}
}
