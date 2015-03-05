package org.powertac.replayer.logtool.message;

import java.util.HashMap;
import java.util.Map;

import org.powertac.common.RateCore;
import org.powertac.replayer.GameInitialization;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;

/**
 * Contains all rates from a competition.
 * 
 * @author DWietoska
 */
@Service
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class RateCoreMap implements GameInitialization {

	/**
	 * All rates.
	 */
	private Map<Long, RateCore> idMap;

	/**
	 * Default constructor.
	 */
	public RateCoreMap() {
		super();
		idMap = new HashMap<Long, RateCore>();
	}
	
	/** Getter and Setter */
	public void setRateCoreMessage(long id, RateCore rate) throws Exception {
		idMap.put(id, rate);
	}
	
	public RateCore getRateCoreMessage(long id) throws Exception {
		return idMap.get(id);
	}

	/**
	 * Reset for new replaying.
	 */
	@Override
	public void newGame() {
		idMap = new HashMap<Long, RateCore>();
	}
}
