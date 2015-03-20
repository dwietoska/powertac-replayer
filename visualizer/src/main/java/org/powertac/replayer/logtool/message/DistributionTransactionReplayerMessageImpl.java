package org.powertac.replayer.logtool.message;

import org.powertac.common.Broker;
import org.powertac.common.DistributionTransaction;
import org.powertac.replayer.logtool.ReplayerMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Creator for distribution entries.
 * 
 * @author DWietoska
 */
@Service("org.powertac.common.DistributionTransaction")
//@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class DistributionTransactionReplayerMessageImpl implements
		ReplayerMessage {
	
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

		DistributionTransaction distributionTransaction = 
				new DistributionTransaction(
				broker, Integer.parseInt(args[4]), Double.parseDouble(args[5]),
				Double.parseDouble(args[6]));

		ReflectionTestUtils.setField(distributionTransaction, "id", id);
		
		return distributionTransaction;
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
		// DistributionTransaction only has getter methods and no setter.
	}

	/**
	 * Read resolve.
	 * 
	 * @param id Id from object.
	 * @param args Arguments from log entry.
	 */
	@Override
	public Object restoreObject(long id, String[] args) throws Exception {
		// DistributionTransaction has no readResolve
		return null;
	}
}
