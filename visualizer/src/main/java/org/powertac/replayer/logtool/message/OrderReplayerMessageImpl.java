package org.powertac.replayer.logtool.message;

import org.powertac.common.Broker;
import org.powertac.common.Order;
import org.powertac.replayer.logtool.ReplayerMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Creator for order entries.
 * 
 * @author DWietoska
 */
@Service("org.powertac.common.Order")
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class OrderReplayerMessageImpl implements ReplayerMessage {

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
		Double limitPrice = null;
		
		if (!args[3].equals("null")) {

			broker = brokerMap.getBrokerMessage(Long.parseLong(args[3]));
		}
		
		if (!args[6].equals("null")) {
			
			limitPrice = Double.parseDouble(args[6]);
		}

		Order order = new Order(broker, Integer.parseInt(args[4]),
				Double.parseDouble(args[5]), limitPrice);

		ReflectionTestUtils.setField(order, "id", id);
		
		return order;
	}
	
	/**
	 * Read resolve.
	 * 
	 * @param id Id from object.
	 * @param args Arguments from log entry.
	 */
	@Override
	public Object restoreObject(long id, String[] args) throws Exception {
		return createObject(id, args);
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
		// Order only has getter methods and no setter.
	}
}
