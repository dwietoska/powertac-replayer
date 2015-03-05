package org.powertac.replayer.user;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.powertac.common.Competition;
import org.powertac.replayer.data.LogDataImpl;
import org.powertac.replayer.visualizer.LogParametersBean;
import org.powertac.visualizer.display.BrokerSeriesTemplate;
import org.powertac.visualizer.domain.Appearance;
import org.powertac.visualizer.statistical.DynamicData;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.Gson;

/**
 * Called when in replaying method the view distribution was requested. 
 * Loads the distribution data up to the current time slot. 
 * 
 * Modified from DistributionBean from visualizer.
 * 
 * @author DWietoska
 */
public class DistributionBeanReplayer implements Serializable {

	/**
	 * serialVersionUID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * JSON for cumulative data.
	 */
	private String distributionDynData;
	
	/**
	 * JSON for data per timeslot.
	 */
	private String distributionDynDataOneTimeslot;

	/**
	 * Called everytime, if the distribution view was requested.
	 * 
	 * @param logDAOImplExtended Contains all replaying data.
	 * @param helper Contains helper methods.
	 * @param logParametersBean Log view.
	 */
	@Autowired
	public DistributionBeanReplayer(LogDataImpl logDAOImplExtended, 
			VisualizerHelperServiceReplayer helper, 
			LogParametersBean logParametersBean) {

		Gson gson = new Gson();
		createBrokerDistributionTransactions(gson, logDAOImplExtended, 
				helper, logParametersBean);
	}

	/**
	 * Creates the distribution data.
	 * 
	 * @param gson GSON
	 * @param logDAOImplExtended Contains all replaying data.
	 * @param helper Contains helper methods.
	 * @param logParametersBean Log view.
	 */
	private void createBrokerDistributionTransactions(Gson gson,
			LogDataImpl logDAOImplExtended,
			VisualizerHelperServiceReplayer helper, 
			LogParametersBean logParametersBean) {

		int currentTimeslot;
		int startNumberTimeslot;
		int numberOfBrokers;
		int posIndex;
		int position;
		int ts;
		boolean isDummy;
		int endNumberTimeslot;
		int currentTsIndex;
		List<String> brokerNames;
		
		Map<String, Integer> mapBrokerArrayAssign = logDAOImplExtended
				.getMapBrokerArrayAssign();
		DynamicData[] dynamicData = logDAOImplExtended.getDistributionsData();
		Competition competition = logDAOImplExtended.getCompetition();
		Appearance[] appearances = logDAOImplExtended.getBrokerAppearances();

		ArrayList<Object> distributionTxData = new ArrayList<Object>();
		ArrayList<Object> distributionTxDataOneTimeslot = new ArrayList<Object>();

		if (mapBrokerArrayAssign != null && dynamicData != null
				&& competition != null && appearances != null) {

			brokerNames = competition.getBrokers();
			currentTimeslot = logParametersBean.getTimeslot();
			startNumberTimeslot = logDAOImplExtended.getStartNumberTimeslot();
			numberOfBrokers = mapBrokerArrayAssign.size();
			endNumberTimeslot = currentTimeslot - startNumberTimeslot;

			for (String brokerName : brokerNames) {

				ArrayList<Object> profitData = new ArrayList<Object>();
				ArrayList<Object> netKwhData = new ArrayList<Object>();
				// one timeslot
				ArrayList<Object> profitDataOneTimeslot = new ArrayList<Object>();
				ArrayList<Object> kwhDataOneTimeslot = new ArrayList<Object>();

				posIndex = mapBrokerArrayAssign.get(brokerName);
				ts = 0;
				isDummy = true;

				for (ts = 0; ts <= endNumberTimeslot; ts++) {

					position = posIndex + numberOfBrokers * ts;
					currentTsIndex = ts + startNumberTimeslot;

					DynamicData dynData = dynamicData[position];

					if (dynData != null) {

						isDummy = false;
						Object[] profit = {
								helper.getMillisForIndex(currentTsIndex,
										competition),
								dynData.getProfit() };
						Object[] netMwh = {
								helper.getMillisForIndex(currentTsIndex,
										competition),
								dynData.getEnergy() };

						profitData.add(profit);
						netKwhData.add(netMwh);

						// one timeslot:
						Object[] profitOneTimeslot = {
								helper.getMillisForIndex(currentTsIndex,
										competition),
								dynData.getProfitDelta() };
						Object[] kWhOneTimeslot = {
								helper.getMillisForIndex(currentTsIndex,
										competition),
								dynData.getEnergyDelta() };
						profitDataOneTimeslot.add(profitOneTimeslot);
						kwhDataOneTimeslot.add(kWhOneTimeslot);
					}
				}

				if (isDummy) {
					// dummy:
					double[] dummy = { helper.getMillisForIndex(0,
							competition), 0 };
					profitData.add(dummy);
					profitDataOneTimeslot.add(dummy);
					kwhDataOneTimeslot.add(dummy);
					netKwhData.add(dummy);
				}

				Appearance appearance = appearances[posIndex];

				distributionTxData.add(new BrokerSeriesTemplate(brokerName,
						appearance.getColorCode(), 0, 
						profitData, true));
				distributionTxData.add(new BrokerSeriesTemplate(brokerName,
						appearance.getColorCode(), 1,
						netKwhData, false));

				// one timeslot:
				distributionTxDataOneTimeslot
						.add(new BrokerSeriesTemplate(brokerName, appearance 																
								.getColorCode(), 0, profitDataOneTimeslot, true));
				distributionTxDataOneTimeslot.add(new BrokerSeriesTemplate(
						brokerName, appearance // + " KWH"
								.getColorCode(), 1, kwhDataOneTimeslot, false));
			}
			
			this.distributionDynData = gson.toJson(distributionTxData);
			this.distributionDynDataOneTimeslot = gson
					.toJson(distributionTxDataOneTimeslot);
		} else {
			
			this.distributionDynData = "[[0,0], [0,0]]";
			this.distributionDynDataOneTimeslot = "[[0,0], [0,0]]";
		}
	}
	
	public String getDistributionDynData() {
		return distributionDynData;
	}
	
	public String getDistributionDynDataOneTimeslot() {
		return distributionDynDataOneTimeslot;
	}
}
