package org.powertac.replayer.logtool.message;

import org.powertac.common.Broker;
import org.powertac.common.CashPosition;
import org.powertac.replayer.logtool.ReplayerMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Creator for cash position entries.
 * 
 * @author DWietoska
 */
@Service("org.powertac.common.CashPosition")
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class CashPositionReplayerMessageImpl implements ReplayerMessage {

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

		CashPosition cashPosition = new CashPosition(broker,
				Double.parseDouble(args[4]), Integer.parseInt(args[5]));

		ReflectionTestUtils.setField(cashPosition, "id", id);

		return cashPosition;
	}
	
	/**
	 * Read resolve.
	 * 
	 * @param id Id from object.
	 * @param args Arguments from log entry.
	 */
	@Override
	public Object restoreObject(long id, String[] args) throws Exception {
		// CashPosition has no readResolve
		return null;
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
		// No method calls (no @StateChange)
	}
}
