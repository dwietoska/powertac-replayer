package org.powertac.replayer.logtool.message;

import org.powertac.common.WeatherReport;
import org.powertac.replayer.logtool.ReplayerMessage;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Creator for weather report entries.
 * 
 * @author DWietoska
 */
@Service("org.powertac.common.WeatherReport")
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class WeatherReportReplayerMessageImpl implements ReplayerMessage {

	/**
	 * Creates a new object.
	 * 
	 * @param id Id from object.
	 * @param args Arguments from log entry.
	 */
	@Override
	public Object createObject(long id, String[] args) throws Exception {

		WeatherReport weatherReport = new WeatherReport(
				Integer.parseInt(args[3]), Double.parseDouble(args[4]),
				Double.parseDouble(args[5]), Double.parseDouble(args[6]),
				Double.parseDouble(args[7]));

		ReflectionTestUtils.setField(weatherReport, "id", id);

		return weatherReport;
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
