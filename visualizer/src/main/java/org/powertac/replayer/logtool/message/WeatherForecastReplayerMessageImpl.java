package org.powertac.replayer.logtool.message;

import java.util.ArrayList;
import java.util.List;

import org.powertac.common.WeatherForecast;
import org.powertac.common.WeatherForecastPrediction;
import org.powertac.replayer.logtool.ReplayerMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Creator for weather forecast entries.
 * 
 * @author DWietoska
 */
@Service("org.powertac.common.WeatherForecast")
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class WeatherForecastReplayerMessageImpl implements ReplayerMessage {
	
	/**
	 * Access to weather forecast prediction.
	 */
	@Autowired
	private WeatherForecastPredictionReplayerMessageImpl weatherForecastPredictionReplayerMessageImpl;

	/**
	 * Creates a new object.
	 * 
	 * @param id Id from object.
	 * @param args Arguments from log entry.
	 */
	@Override
	public Object createObject(long id, String[] args) throws Exception {

		String body = args[4].substring(1, args[4].indexOf(')'));
		String[] items = body.split(",");
		List<WeatherForecastPrediction> listArg = 
				new ArrayList<WeatherForecastPrediction>();
		
		for (String item : items) {

			listArg.add(weatherForecastPredictionReplayerMessageImpl
					.getWeatherForecastPredictionMessage(Long.parseLong(item)));
		}
		
		WeatherForecast weatherForecast = new WeatherForecast(
				Integer.parseInt(args[3]), listArg);
		
		ReflectionTestUtils.setField(weatherForecast, "id", id);
		
		return weatherForecast;
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
