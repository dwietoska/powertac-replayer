package org.powertac.replayer.logtool.message;

import java.util.HashMap;
import java.util.Map;

import org.powertac.common.WeatherForecastPrediction;
import org.powertac.replayer.GameInitialization;
import org.powertac.replayer.logtool.ReplayerMessage;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Creator for weather forecast prediction entries.
 * 
 * @author DWietoska
 */
@Service("org.powertac.common.WeatherForecastPrediction")
//@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class WeatherForecastPredictionReplayerMessageImpl implements
		ReplayerMessage, GameInitialization {

	/**
	 * Contains all weather forecast prediction.
	 */
	private Map<Long, WeatherForecastPrediction> idMap;

	/**
	 * Default constructor.
	 */
	public WeatherForecastPredictionReplayerMessageImpl() {
		super();
		idMap = new HashMap<Long, WeatherForecastPrediction>();
	}

	/**
	 * Reset for new replaying.
	 */
	@Override
	public void newGame() {
		idMap = new HashMap<Long, WeatherForecastPrediction>();
	}
	
	/**
	 * Creates a new object.
	 * 
	 * @param id Id from object.
	 * @param args Arguments from log entry.
	 */
	@Override
	public Object createObject(long id, String[] args) throws Exception {

		WeatherForecastPrediction weatherForecastPrediction = 
				new WeatherForecastPrediction(
						Integer.parseInt(args[3]), 
						Double.parseDouble(args[4]),
						Double.parseDouble(args[5]), 
						Double.parseDouble(args[6]),
						Double.parseDouble(args[7]));
				
		ReflectionTestUtils.setField(weatherForecastPrediction, "id", id);
		
		idMap.put(id, weatherForecastPrediction);
		
		return weatherForecastPrediction;
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
	
	/**
	 * Getter weather forecast prediction
	 * @param id Id
	 * @return weather forecast prediction
	 */
	public WeatherForecastPrediction getWeatherForecastPredictionMessage(long 
			id) throws Exception {
		return idMap.get(id);
	}
}
