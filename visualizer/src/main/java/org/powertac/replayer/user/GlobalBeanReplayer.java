package org.powertac.replayer.user;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.powertac.common.Competition;
import org.powertac.common.WeatherReport;
import org.powertac.replayer.data.LogDataImpl;
import org.powertac.replayer.data.dto.BalancingCategoryAttributes;
import org.powertac.replayer.data.dto.FinanceCategoryAttributes;
import org.powertac.replayer.data.dto.TariffCategoryAttributes;
import org.powertac.replayer.visualizer.LogParametersBean;
import org.powertac.visualizer.push.GlobalPusher;
import org.powertac.visualizer.push.NominationCategoryPusher;
import org.powertac.visualizer.push.NominationPusher;
import org.powertac.visualizer.push.WeatherPusher;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.Gson;

/**
 * Called when in replaying method global data was requested. 
 * Loads the global data for current time slot.
 * 
 * Modified from GlobalBean from visualizer.
 * 
 * @author DWietoska
 */
public class GlobalBeanReplayer implements Serializable {

	/**
	 * serialVersionUID.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Global push object.
	 */
	private GlobalPusher globalPusher;

	/**
	 * Called everytime, if global data were requested.
	 * 
	 * @param logDAOImplExtended Contains all replaying data.
	 * @param helper Contains helper methods.
	 * @param logParametersBean Log view.
	 */
	@Autowired
	public GlobalBeanReplayer(LogDataImpl logDAOImplExtended,
			LogParametersBean logParametersBean, 
			VisualizerHelperServiceReplayer helper) {

		int currentTimeslot;
		int startNumberTimeslot;
		int numberOfBrokers;
		int posIndex;
		int position;
		int endNumberTimeslot;
		List<String> brokerNames;
		
		Map<String, Integer> mapBrokerArrayAssign = logDAOImplExtended
				.getMapBrokerArrayAssign();
		WeatherReport[] weatherReportData = logDAOImplExtended
				.getWeatherReportsData();
		TariffCategoryAttributes[] tariffCategoryAttributes = 
				logDAOImplExtended.getTariffCategoryAttributesData();
		FinanceCategoryAttributes[] financeCategoryAttributes = 
				logDAOImplExtended.getFinanceCategoryAttributesData();
		BalancingCategoryAttributes[] balancingCategoryAttributes = 
				logDAOImplExtended.getBalancingCategoryAttributesData();
		Competition competition = logDAOImplExtended.getCompetition();
		
		if (mapBrokerArrayAssign != null && weatherReportData != null
				&& tariffCategoryAttributes != null
				&& financeCategoryAttributes != null
				&& balancingCategoryAttributes != null
				&& competition != null) {

			brokerNames = competition.getBrokers();
			currentTimeslot = logParametersBean.getTimeslot();
			startNumberTimeslot = logDAOImplExtended.getStartNumberTimeslot();
			numberOfBrokers = mapBrokerArrayAssign.size();
			endNumberTimeslot = currentTimeslot - startNumberTimeslot;

			if (endNumberTimeslot >= 0) {

				WeatherReport weatherReport = weatherReportData[endNumberTimeslot];
				WeatherPusher weatherPusher;

				if (weatherReport != null) {
					
					weatherPusher = new WeatherPusher(helper.getMillisForIndex(
							weatherReport.getCurrentTimeslot()
									.getSerialNumber(), competition),
							weatherReport.getTemperature(),
							weatherReport.getWindSpeed(),
							weatherReport.getWindDirection(),
							weatherReport.getCloudCover(),
							weatherReport.getTimeslotIndex());
				} else {
					
					// Dummy
					weatherPusher = new WeatherPusher(0, 0, 0, 0, 0, 0);
				}

				NominationPusher np = null;

				for (String brokerName : brokerNames) {

					posIndex = mapBrokerArrayAssign.get(brokerName);
					position = posIndex + numberOfBrokers * endNumberTimeslot;

					FinanceCategoryAttributes fca = 
							financeCategoryAttributes[position];
					TariffCategoryAttributes tca = 
							tariffCategoryAttributes[position];
					BalancingCategoryAttributes bca = 
							balancingCategoryAttributes[position];

					if (fca != null && tca != null && bca != null) {

						if (np == null) {

							np = new NominationPusher(
									new NominationCategoryPusher(brokerName,
											(long) fca.getProfit()),
									new NominationCategoryPusher(brokerName,
											(long) Math.abs(bca.getProfit())),
									new NominationCategoryPusher(brokerName,
											tca.getCustomerCount()));
						} else {

							long profitAmount = (long) fca.getProfit();
							long balanceAmount = (long) Math.abs(bca
									.getProfit());
							long customerAmount = (long) tca.getCustomerCount();
							
							if (profitAmount > np.getProfit().getAmount()) {
								np.setProfit(new NominationCategoryPusher(
										brokerName, profitAmount));
							}
							
							if (balanceAmount < Math.abs(np.getBalance()
									.getAmount())) {
								np.setBalance(new NominationCategoryPusher(
										brokerName, balanceAmount));
							}
							
							if (customerAmount > np.getCustomerNumber()
									.getAmount()) {
								np.setCustomerNumber(new NominationCategoryPusher(
										brokerName, customerAmount));
							}
						}
					} else {
						
						// Dummy
						NominationCategoryPusher dummyNc = new NominationCategoryPusher(
								"", 0);
						np = new NominationPusher(dummyNc, dummyNc, dummyNc);
					}
				}

				globalPusher = new GlobalPusher(weatherPusher, np);
			}
		} else {

			// Dummy
			NominationCategoryPusher dummyNc = new NominationCategoryPusher("",
					0);
			NominationPusher nominationPusher = new NominationPusher(dummyNc,
					dummyNc, dummyNc);
			WeatherPusher weatherPusher = new WeatherPusher(0, 0, 0, 0, 0, 0);

			globalPusher = new GlobalPusher(weatherPusher, nominationPusher);
		}
	}

	public String getGlobalPusher() {
		return new Gson().toJson(globalPusher);
	}
}
