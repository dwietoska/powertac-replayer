package org.powertac.replayer.logtool.message;

import java.util.HashMap;
import java.util.Map;

import org.powertac.common.Broker;
import org.powertac.common.msg.TariffStatus;
import org.powertac.common.msg.TariffStatus.Status;
import org.powertac.replayer.GameInitialization;
import org.powertac.replayer.logtool.ReplayerMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Creator for tariff status entries.
 * 
 * @author DWietoska
 */
@Service("org.powertac.common.msg.TariffStatus")
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class TariffStatusReplayerMessageImpl implements ReplayerMessage,
		GameInitialization {
	
	/**
	 * Access to broker map.
	 */
	@Autowired
	private BrokerMap brokerMap;
	
	/**
	 * Contains all tariff status.
	 */
	private Map<Long, TariffStatus> idMap;

	/**
	 * Default constructor.
	 */
	public TariffStatusReplayerMessageImpl() {
		super();
		idMap = new HashMap<Long, TariffStatus>();
	}
	
	/**
	 * Reset for new replaying.
	 */
	@Override
	public void newGame() {
		idMap = new HashMap<Long, TariffStatus>();
	}
	
	/**
	 * Creates a new object.
	 * 
	 * @param id Id from object.
	 * @param args Arguments from log entry.
	 */
	@Override
	public Object createObject(long id, String[] args) throws Exception {
		
		Broker broker = null;
		TariffStatus tariffStatus = null;
		
		if (!args[3].equals("null")) {
			
			broker = brokerMap.getBrokerMessage(Long.parseLong(args[3]));
		}
				
		tariffStatus = new TariffStatus(broker, Long.parseLong(args[4]), 
				Long.parseLong(args[5]), Status.valueOf(args[6]));
		
		ReflectionTestUtils.setField(tariffStatus, "id", id);
		
		idMap.put(id, tariffStatus);
		
		return tariffStatus;
	}

	/**
	 * Read resolve.
	 * 
	 * @param id Id from object.
	 * @param args Arguments from log entry.
	 */
	@Override
	public Object restoreObject(long id, String[] args) throws Exception {	
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
		
		TariffStatus tariffStatus = idMap.get(id);
		
	    if (methodname.equals("withMessage")) {
	    	
	    	tariffStatus.withMessage(args[3]);
		}
	}
	
	/**
	 * Getter for tariff status.
	 * 
	 * @param id Id
	 * @return Tariff status
	 */
	public TariffStatus getTariffStatusMessage(long id) throws Exception {
		return idMap.get(id);
	}
}
