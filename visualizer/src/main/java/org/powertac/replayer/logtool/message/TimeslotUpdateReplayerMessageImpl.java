package org.powertac.replayer.logtool.message;

import org.joda.time.Instant;
import org.powertac.common.msg.TimeslotUpdate;
import org.powertac.replayer.logtool.ReplayerMessage;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Creator for time slot update entries.
 * 
 * @author DWietoska
 */
@Service("org.powertac.common.msg.TimeslotUpdate")
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class TimeslotUpdateReplayerMessageImpl implements ReplayerMessage {

	/**
	 * Creates a new object.
	 * 
	 * @param id Id from object.
	 * @param args Arguments from log entry.
	 */
	@Override
	public Object createObject(long id, String[] args) throws Exception {

		TimeslotUpdate timeslotUpdate = new TimeslotUpdate(
				Instant.parse(args[3]), Integer.parseInt(args[4]),
				Integer.parseInt(args[5]));

		ReflectionTestUtils.setField(timeslotUpdate, "id", id);
		
		return timeslotUpdate;
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
