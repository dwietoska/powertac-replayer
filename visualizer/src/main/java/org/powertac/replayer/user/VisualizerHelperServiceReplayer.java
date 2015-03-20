package org.powertac.replayer.user;

import org.joda.time.Instant;
import org.powertac.common.Competition;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;

/**
 * Contains helper methods for replaying data.
 * 
 * Modified from visualizer.
 * 
 * @author DWietoska
 */
@Service
//@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class VisualizerHelperServiceReplayer {

	/**
	 * Returns the milliseconds of the given time slot.
	 * 
	 * @param index Time slot
	 * @param comp Competition
	 * @return Millis for given time slot.
	 */
	public long getMillisForIndex(int index, Competition comp) {

		if (comp != null) {
			return new Instant(comp.getSimulationBaseTime().getMillis() + index
					* comp.getTimeslotDuration()).getMillis();
		} else {
			return -1;
		}
	}

	/**
	 * Returns the time slot of the given milliseconds.
	 * 
	 * @param time Time
	 * @param comp Competition
	 * @return Time slot for given milliseconds.
	 */
	public int getTimeslotIndex(Instant time, Competition comp) {

		if (comp != null) {
			long offset = time.getMillis()
					- comp.getSimulationBaseTime().getMillis();
			long duration = comp.getTimeslotDuration();
			// truncate to timeslot boundary
			return (int) (offset / duration);
		} else {
			return -1;
		}
	}
}
