package org.powertac.replayer.user;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.powertac.common.Competition;
import org.powertac.replayer.data.LogDataImpl;
import org.powertac.replayer.data.dto.BalancingCategoryAttributes;
import org.powertac.replayer.data.dto.GradingAttributes;
import org.powertac.replayer.data.dto.TariffCategoryAttributes;
import org.powertac.replayer.data.dto.WholesaleCategoryAttributes;
import org.powertac.replayer.visualizer.LogParametersBean;
import org.powertac.visualizer.display.GameOverviewTemplate;
import org.powertac.visualizer.statistical.DynamicData;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.Gson;

/**
 * Called when in replaying method the view "gameoverview" was requested. 
 * Loads the "gameoverview" data up to the current time slot.
 * 
 * Modified from GameOverviewBean from visualizer.
 * 
 * @author DWietoska
 */
public class GameOverviewBeanReplayer implements Serializable {

	/**
	 * serialVersionUID.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * JSON for "gameoverview" data.
	 */
	private String gameOverview;

	/**
	 * Called everytime, if the "gameoverview" view was requested.
	 * 
	 * @param logDAOImplExtended Contains all replaying data.
	 * @param helper Contains helper methods.
	 * @param logParametersBean Log view.
	 */
	@Autowired
	public GameOverviewBeanReplayer(LogDataImpl logDAOImplExtended,
			VisualizerHelperServiceReplayer helper, 
			LogParametersBean logParametersBean) {

		Gson gson = new Gson();
		createMostRecentOverview(gson, logDAOImplExtended, helper,
				logParametersBean);
	}

	/**
	 * Creates the "gameoverview" data.
	 * 
	 * @param gson GSON
	 * @param logDAOImplExtended Contains all replaying data.
	 * @param helper Contains helper methods.
	 * @param logParametersBean Log view.
	 */
	private void createMostRecentOverview(Gson gson,
			LogDataImpl logDAOImplExtended,
			VisualizerHelperServiceReplayer helper, 
			LogParametersBean logParametersBean) {

		int currentTimeslot;
		int startNumberTimeslot;
		int numberOfBrokers;
		int posIndex;
		int position;
		int endNumberTimeslot;
		double highestEnergyAmount;
		double energy ;
		List<String> brokerNames;
		
		ArrayList<Object> allBrokersData = new ArrayList<Object>();
		Map<String, Integer> mapBrokerArrayAssign = logDAOImplExtended
				.getMapBrokerArrayAssign();
		DynamicData[] dynamicDataDistribution = logDAOImplExtended
				.getDistributionsData();
		TariffCategoryAttributes[] tariffCategoryAttributes = 
				logDAOImplExtended.getTariffCategoryAttributesData();
		WholesaleCategoryAttributes[] wholesaleCategoryAttributes = 
				logDAOImplExtended.getWholesaleCategoryAttributesData();
		BalancingCategoryAttributes[] balancingCategoryAttributes = 
				logDAOImplExtended.getBalancingCategoryAttributesData();
		GradingAttributes[] gradingAttributes = logDAOImplExtended
				.getGradingAttributesData();
		Competition competition = logDAOImplExtended.getCompetition();
		
		highestEnergyAmount = 0.0;
		energy = 0.0;

		if (mapBrokerArrayAssign != null && dynamicDataDistribution != null
				&& tariffCategoryAttributes != null
				&& wholesaleCategoryAttributes != null
				&& balancingCategoryAttributes != null
				&& gradingAttributes != null && competition != null) {

			brokerNames = competition.getBrokers();
			currentTimeslot = logParametersBean.getTimeslot();
			startNumberTimeslot = logDAOImplExtended.getStartNumberTimeslot();
			numberOfBrokers = mapBrokerArrayAssign.size();
			endNumberTimeslot = currentTimeslot - startNumberTimeslot;

			for (String brokerName : brokerNames) {

				posIndex = mapBrokerArrayAssign.get(brokerName);
				position = posIndex + numberOfBrokers * endNumberTimeslot;

				energy = 0.0;
				DynamicData dd = dynamicDataDistribution[position];
				if (dd == null) {

					int safeTimeslot = endNumberTimeslot - 1;
					position = posIndex + numberOfBrokers * safeTimeslot;

					dd = dynamicDataDistribution[position];

					while (safeTimeslot >= 0 && dd == null) {
						safeTimeslot--;
						if (safeTimeslot >= 0) {
							position = posIndex + numberOfBrokers
									* safeTimeslot;
							dd = dynamicDataDistribution[position];
						}
					}

					if (dd != null) {
						energy = dd.getEnergy();
					}
				}

				if (energy > highestEnergyAmount) {
					highestEnergyAmount = energy;
				}
			}

			for (String brokerName : brokerNames) {

				ArrayList<Double> data = new ArrayList<Double>();

				posIndex = mapBrokerArrayAssign.get(brokerName);
				position = posIndex + numberOfBrokers * endNumberTimeslot;

				GradingAttributes gradingAttribute = gradingAttributes[endNumberTimeslot];

				TariffCategoryAttributes tca = tariffCategoryAttributes[position];

				data.add(gradingAttribute.getTariffGrade(
						tca.getTotalMoneyFlow(), tca.getConsumptionConsumers(),
						tca.getTotalMoneyFromSold(),
						tca.getTotalBoughtEnergy(), tca.getTotalSoldEnergy(),
						tca.getCustomerCount(), tca.getLostCustomers()));

				// wholesale grade
				WholesaleCategoryAttributes wca = wholesaleCategoryAttributes[position];
				data.add(gradingAttribute.getWholesaleGrade(
						wca.getTotalRevenueFromSelling(),
						wca.getTotalCostFromBuying(),
						wca.getTotalEnergyBought(), wca.getTotalEnergySold()));

				BalancingCategoryAttributes bca = balancingCategoryAttributes[position];

				// LastDynamicData ersetzen
				double distributionEnergy = 0.0;
				DynamicData dd = dynamicDataDistribution[position];

				if (dd == null) {

					int safeTimeslot = endNumberTimeslot - 1;
					position = posIndex + numberOfBrokers * safeTimeslot;

					dd = dynamicDataDistribution[position];

					while (safeTimeslot >= 0 && dd == null) {
						safeTimeslot--;
						if (safeTimeslot >= 0) {
							position = posIndex + numberOfBrokers
									* safeTimeslot;
							dd = dynamicDataDistribution[position];
						}
					}

					if (dd != null) {
						distributionEnergy = dd.getEnergy();
					}
				} else {
					distributionEnergy = dd.getEnergy();
				}

				data.add(gradingAttribute.getBalancingGrade(bca.getEnergy(),
						distributionEnergy,
						bca.getProfit()));

				// distribution grade
				data.add(gradingAttribute
						.getDistributionGrade(distributionEnergy));

				allBrokersData.add(new GameOverviewTemplate(brokerName, data));
			}
		}

		this.gameOverview = gson.toJson(allBrokersData);
	}

	public String getGameOverview() {
		return gameOverview;
	}
}
