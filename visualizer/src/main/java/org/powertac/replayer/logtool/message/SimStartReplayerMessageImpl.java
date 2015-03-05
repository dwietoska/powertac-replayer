package org.powertac.replayer.logtool.message;

import org.joda.time.Instant;
import org.powertac.common.msg.SimStart;
import org.powertac.replayer.logtool.ReplayerMessage;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;

/**
 * Creator for sim start entries.
 * 
 * @author DWietoska
 */
@Service("org.powertac.common.msg.SimStart")
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class SimStartReplayerMessageImpl implements ReplayerMessage {

	/**
	 * Creates a new object.
	 * 
	 * @param id Id from object.
	 * @param args Arguments from log entry.
	 */
	@Override
	public Object createObject(long id, String[] args) throws Exception {
		return new SimStart(Instant.parse(args[3]));
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
	}
}
