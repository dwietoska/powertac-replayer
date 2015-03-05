package org.powertac.replayer.logtool.message;

import java.util.HashMap;
import java.util.Map;

import org.powertac.common.Broker;
import org.powertac.common.MarketPosition;
import org.powertac.replayer.GameInitialization;
import org.powertac.replayer.logtool.ReplayerMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.test.util.ReflectionTestUtils;

@Service("org.powertac.common.MarketPosition")
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class MarketPositionReplayerMessageImpl implements ReplayerMessage, 
		GameInitialization {

	/**
	 * Contains all market positions.
	 */
	private Map<Long, MarketPosition> idMap;
	
	/**
	 * Default constructor.
	 */
	public MarketPositionReplayerMessageImpl() {
		super();
		idMap = new HashMap<Long, MarketPosition>();
	}
	
	/**
	 * Reset for new replaying.
	 */
	@Override
	public void newGame() {
		idMap = new HashMap<Long, MarketPosition>();
	}
	
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

			broker = brokerMap.getBrokerMessage(Long
					.parseLong(args[3]));
		}

		MarketPosition marketPosition = new MarketPosition(
				broker, Integer.parseInt(args[4]), 
				Double.parseDouble(args[5]));

		ReflectionTestUtils.setField(marketPosition, "id", id);
		
		idMap.put(id, marketPosition);

		return marketPosition;
	}
	
	/**
	 * Read resolve.
	 * 
	 * @param id Id from object.
	 * @param args Arguments from log entry.
	 */
	@Override
	public Object restoreObject(long id, String[] args) throws Exception {
		// MarketPosition has no readResolve
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
		// MarketPosition only has getter methods and no setter.
	}
	
	/**
	 * Get market position.
	 * 
	 * @param id Id
	 * @return Market position
	 */
	public MarketPosition getMarketPositionMessage(long id) throws Exception {
		return idMap.get(id);
	}
}
