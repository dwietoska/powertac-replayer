package org.powertac.replayer.logtool.message;

import java.util.HashMap;
import java.util.Map;

import org.powertac.common.Broker;
import org.powertac.replayer.GameInitialization;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;

/**
 * Contains all brokers from a competition.
 * 
 * @author DWietoska
 */
@Service
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class BrokerMap implements GameInitialization {

	/**
	 * All brokers.
	 */
	private Map<Long, Broker> idMap;

	/**
	 * Default constructor.
	 */
	public BrokerMap() {
		super();
		idMap = new HashMap<Long, Broker>();
	}
	
	/** Getter and Setter */
	public void setBrokerMessage(long id, Broker broker) throws Exception {
		idMap.put(id, broker);
	}
	
	public Broker getBrokerMessage(long id) throws Exception {
		return idMap.get(id);
	}

	/**
	 * Reset for new replaying.
	 */
	@Override
	public void newGame() {
		idMap = new HashMap<Long, Broker>();
	}
}
