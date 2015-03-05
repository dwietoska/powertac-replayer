package org.powertac.replayer.logtool.message;

import org.powertac.common.BalancingTransaction;
import org.powertac.common.Broker;
import org.powertac.replayer.logtool.ReplayerMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Creator for balancing entries.
 * 
 * @author DWietoska
 */
@Service("org.powertac.common.BalancingTransaction")
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class BalancingTransactionReplayerMessageImpl implements ReplayerMessage {
	
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
		BalancingTransaction balancingTransaction = null;

		if (!args[3].equals("null")) {
			
			broker = brokerMap.getBrokerMessage(Long.parseLong(args[3]));
		}

		balancingTransaction = new BalancingTransaction(
				broker, Integer.parseInt(args[4]), Double.parseDouble(args[5]),
				Double.parseDouble(args[6]));
		
		ReflectionTestUtils.setField(balancingTransaction, "id", id);
		
		return balancingTransaction;
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
		// BalancingTransaction only has getter methods and no setter.
	}

	/**
	 * Read resolve.
	 * 
	 * @param id Id from object.
	 * @param args Arguments from log entry.
	 */
	@Override
	public Object restoreObject(long id, String[] args) throws Exception {
		// BalancingTransaction has no readResolve
		return null;
	}
}
