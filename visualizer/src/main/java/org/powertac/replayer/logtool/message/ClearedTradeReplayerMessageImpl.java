package org.powertac.replayer.logtool.message;

import org.joda.time.Instant;
import org.powertac.common.ClearedTrade;
import org.powertac.replayer.logtool.ReplayerMessage;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Creator for cleared trade entries.
 * 
 * @author DWietoska
 */
@Service("org.powertac.common.ClearedTrade")
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class ClearedTradeReplayerMessageImpl implements ReplayerMessage {

	/**
	 * Creates a new object.
	 * 
	 * @param id Id from object.
	 * @param args Arguments from log entry.
	 */
	@Override
	public Object createObject(long id, String[] args) throws Exception {

		ClearedTrade clearedTrade = new ClearedTrade(Integer.parseInt(args[3]),
				Double.parseDouble(args[4]), Double.parseDouble(args[5]),
				Instant.parse(args[6]));
		
		ReflectionTestUtils.setField(clearedTrade, "id", id);
		
		return clearedTrade;
	}
	
	/**
	 * Read resolve.
	 * 
	 * @param id Id from object.
	 * @param args Arguments from log entry.
	 */
	@Override
	public Object restoreObject(long id, String[] args) throws Exception {
		// ClearedTrade has no readResolve
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