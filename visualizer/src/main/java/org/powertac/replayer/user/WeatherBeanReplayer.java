package org.powertac.replayer.user;

import java.io.Serializable;
import java.util.ArrayList;

import org.powertac.common.Competition;
import org.powertac.common.WeatherReport;
import org.powertac.replayer.data.LogDataImpl;
import org.powertac.replayer.visualizer.LogParametersBean;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.Gson;

/**
 * Called when in replaying method the view weather was requested. 
 * Loads the weather  data up to the current time slot.
 * 
 * Modified from WeatherBean from visualizer.
 * 
 * @author DWietoska
 */
public class WeatherBeanReplayer implements Serializable {

	/**
	 * serialVersionUID.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Json for temperature data.
	 */
	private String temperatureData;
	
	/**
	 * Json for wind speed data.
	 */
	private String windSpeedData;
	
	/**
	 * Json for wind direction data.
	 */
	private String windDirectionData;
	
	/**
	 * Json for cloud cover data.
	 */
	private String cloudCoverData;
	
	/**
	 * Called everytime, if the weather view was requested.
	 * 
	 * @param logDAOImplExtended Contains all replaying data.
	 * @param helper Contains helper methods.
	 * @param logParametersBean Log view.
	 */
	@Autowired
	public WeatherBeanReplayer(LogDataImpl logDAOImplExtended, 
			VisualizerHelperServiceReplayer helper, 
			LogParametersBean logParametersBean) {
		
		int currentTimeslot;
		int startNumberTimeslot;
		int ts;
		boolean isDummy = true;
		Gson gson = new Gson();

		WeatherReport[] weatherReportData = logDAOImplExtended
				.getWeatherReportsData();
		Competition competition = logDAOImplExtended
				.getCompetition();
		
		ArrayList<Object> temps = new ArrayList<Object>();
		ArrayList<Object> windSpeeds = new ArrayList<Object>();
		ArrayList<Object> windDirections = new ArrayList<Object>();
		ArrayList<Object> clouds = new ArrayList<Object>();

		if (weatherReportData != null && competition != null) {
			
			currentTimeslot = logParametersBean.getTimeslot();
			startNumberTimeslot = logDAOImplExtended.getStartNumberTimeslot();
			
			for (ts = 0; ts <= currentTimeslot - startNumberTimeslot; ts++) {

				WeatherReport report = weatherReportData[ts];
				long millis = helper
						.getMillisForIndex(ts + startNumberTimeslot,
								competition);

				if (report != null) {

					isDummy = false;
				
					double[] cloud = { millis, report.getCloudCover() };
					double[] windSpeed = { millis, report.getWindSpeed() };
					double[] windDirection = { millis,
							report.getWindDirection() };
					double[] temp = { millis, report.getTemperature() };

					clouds.add(cloud);
					windSpeeds.add(windSpeed);
					windDirections.add(windDirection);
					temps.add(temp);
				}
			}
		}
		
		if (isDummy) {
			
			//dummy:
			double[] dummy = { helper.getMillisForIndex(0,
					competition), 0};
			clouds.add(dummy);
			windSpeeds.add(dummy);
			windDirections.add(dummy);
			temps.add(dummy);
		}
		
		cloudCoverData = gson.toJson(clouds);
		windSpeedData = gson.toJson(windSpeeds);
		temperatureData = gson.toJson(temps);
		windDirectionData = gson.toJson(windDirections);
	}
	
	public String getCloudCoverData() {
		return cloudCoverData;
	}
	public String getTemperatureData() {
		return temperatureData;
	}
	public String getWindSpeedData() {
		return windSpeedData;
	}
	public String getWindDirectionData() {
		return windDirectionData;
	}
}
