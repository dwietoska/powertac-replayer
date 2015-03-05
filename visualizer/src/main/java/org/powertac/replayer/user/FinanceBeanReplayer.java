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
import org.powertac.visualizer.statistical.FinanceDynamicData;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.Gson;

/**
 * Called when in replaying method the view finance was requested. 
 * Loads the finances data up to the current time slot.
 * 
 * Modified from FinanceBean from visualizer.
 * 
 * @author DWietoska
 */
public class FinanceBeanReplayer implements Serializable {

	/**
	 * serialVersionUID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * JSON for cumulative data.
	 */
	private String financeDynData;
	
	/**
	 * JSON for data per timeslot.
	 */
	private String financeDynDataOneTimeslot;

	/**
	 * Creates new finance bean.
	 */
	public FinanceBeanReplayer() {
		
	}
	
	/**
	 * Called everytime, if the finance view was requested.
	 * 
	 * @param logDAOImplExtended Contains all replaying data.
	 * @param helper Contains helper methods.
	 * @param logParametersBean Log view.
	 */
	@Autowired
	public FinanceBeanReplayer(LogDataImpl logDAOImplExtended,
			LogParametersBean logParametersBean, 
			VisualizerHelperServiceReplayer helper) {

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
		FinanceDynamicData[] financeDynamicData = logDAOImplExtended
				.getFinanceDynamicsData();
		Competition competition = logDAOImplExtended.getCompetition();
		Appearance[] appearances = logDAOImplExtended.getBrokerAppearances();

		Gson gson = new Gson();
		ArrayList<Object> financeTxData = new ArrayList<Object>();
		ArrayList<Object> financeTxDataOneTimeslot = new ArrayList<Object>();

		if (mapBrokerArrayAssign != null && financeDynamicData != null
				&& competition != null && appearances != null) {

			brokerNames = competition.getBrokers();
			currentTimeslot = logParametersBean.getTimeslot();
			startNumberTimeslot = logDAOImplExtended.getStartNumberTimeslot();
			numberOfBrokers = mapBrokerArrayAssign.size();
			endNumberTimeslot = currentTimeslot - startNumberTimeslot;

			for (String brokerName : brokerNames) {

				ArrayList<Object> profitData = new ArrayList<Object>();
				// one timeslot
				ArrayList<Object> profitDataOneTimeslot = new ArrayList<Object>();

				posIndex = mapBrokerArrayAssign.get(brokerName);
				ts = 0;
				isDummy = true;

				for (ts = 0; ts <= endNumberTimeslot; ts++) {

					position = posIndex + numberOfBrokers * ts;
					currentTsIndex = ts + startNumberTimeslot;

					FinanceDynamicData dynData = financeDynamicData[position];

					if (dynData != null) {

						isDummy = false;
						Object[] profit = {
								helper.getMillisForIndex(currentTsIndex,
										competition),
								dynData.getProfit() };
						profitData.add(profit);

						// one timeslot:
						Object[] profitOneTimeslot = {
								helper.getMillisForIndex(currentTsIndex,
										competition),
								dynData.getProfitDelta() };
						profitDataOneTimeslot.add(profitOneTimeslot);
					}
				}

				if (isDummy) {
					
					// dummy:
					double[] dummy = { helper.getMillisForIndex(0,
							competition), 0 };
					profitData.add(dummy);
					profitDataOneTimeslot.add(dummy);
				}

				Appearance appearance = appearances[posIndex];

				financeTxData.add(new BrokerSeriesTemplate(brokerName,
						appearance.getColorCode(), 0, profitData, true));

				// one timeslot:
				financeTxDataOneTimeslot
						.add(new BrokerSeriesTemplate(brokerName, appearance
								.getColorCode(), 0, profitDataOneTimeslot, true));
			}
			
			this.financeDynData = gson.toJson(financeTxData);
			this.financeDynDataOneTimeslot = gson.toJson(financeTxDataOneTimeslot);
		
		} else {
			
			// Leer Dummy-Object
			this.financeDynData = "[[0,0]]";
			this.financeDynDataOneTimeslot = "[[0,0]]";
		}
	}
	
	public String getFinanceDynData() {
		return financeDynData;
	}
	
	public String getFinanceDynDataOneTimeslot() {
		return financeDynDataOneTimeslot;
	}
}
