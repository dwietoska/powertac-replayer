package org.powertac.replayer.user;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.powertac.common.Competition;
import org.powertac.replayer.data.LogDataImpl;
import org.powertac.replayer.data.dto.FinanceCategoryAttributes;
import org.powertac.replayer.data.dto.VisualizerBeanAttributes;
import org.powertac.replayer.visualizer.LogParametersBean;
import org.powertac.visualizer.domain.broker.TariffDynamicData;
import org.powertac.visualizer.statistical.DynamicData;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.Gson;

/**
 * Called when in replaying method the view ranking was requested. 
 * Loads the ranking data for current time slot.
 * 
 * Modified from visualizer.
 * 
 * @author DWietoska
 */
public class RankingBeanReplayer {
	
	/**
	 * Access to spring beans.
	 */
	@Autowired
	private LogDataImpl logDAOImplExtended;
	
	/**
	 * Access to replayer-view-session.
	 */
	@Autowired
	private LogParametersBean logParametersBean;
	
	/**
	 * Contains all broker names.
	 */
	private ArrayList<String> names = new ArrayList<String>();

	/**
	 * Called every time, if the ranking view was requested.
	 * 
	 * @param logDAOImplExtended Contains all replaying data.
	 * @param helper Contains helper methods.
	 * @param logParametersBean Log view.
	 */
	@Autowired
	public RankingBeanReplayer(LogDataImpl logDAOImplExtended,
			LogParametersBean logParametersBean) {

		Competition competition = logDAOImplExtended.getCompetition();

		if (competition != null) {

			List<String> listBrokerNames = competition.getBrokers();

			for (String brokerName : listBrokerNames) {

				this.names.add("'" + brokerName + "'");
			}
		}
	}

	/**
	 * Called from ranking view to obtain ranking data.
	 * 
	 * @return ranking data.
	 */
	public String getRanking() {
		
		return createRankingJson(logParametersBean.getTimeslot());
	}

	/**
	 * Creates ranking data.
	 * 
	 * @param timeslot Time slot
	 * @return Ranking data.
	 */
	public String createRankingJson(int timeslot) {

		Gson gson = new Gson();

		int startNumberTimeslot;
		int numberOfBrokers;
		int timeslotIndex;
		int posIndex;
		int position;
		List<String> brokerNames;
		
		Map<String, Integer> mapBrokerArrayAssign = 
				logDAOImplExtended.getMapBrokerArrayAssign();
		FinanceCategoryAttributes[] financeCategoryAttributes = 
				logDAOImplExtended.getFinanceCategoryAttributesData();
		TariffDynamicData[] tariffDynamicDatas = 
				logDAOImplExtended.getTariffDynamicsData();
		VisualizerBeanAttributes[] visualizerBeanAttributes = 
				logDAOImplExtended.getVisualizerBeanAttributesData();
		DynamicData[] dynamicDatas = logDAOImplExtended
				.getMarketTransactionsData();
		Competition competition = logDAOImplExtended.getCompetition();
		Map<String, Integer> mapBrokerNamesIndex = 
				new HashMap<String, Integer>();

		ArrayList<Object> result = new ArrayList<Object>();

		if (mapBrokerArrayAssign != null && financeCategoryAttributes != null
				&& tariffDynamicDatas != null
				&& visualizerBeanAttributes != null && dynamicDatas != null
				&& competition != null) {

			brokerNames = competition.getBrokers();
			startNumberTimeslot = logDAOImplExtended.getStartNumberTimeslot();
			numberOfBrokers = mapBrokerArrayAssign.size();
			timeslotIndex = timeslot - startNumberTimeslot;

			HashMap<String, Double> map = new HashMap<String, Double>();
			int i = 0;
			
			for (String brokerName : brokerNames) {
				
				posIndex = mapBrokerArrayAssign.get(brokerName);
				position = posIndex + numberOfBrokers * timeslotIndex;
				map.put(brokerName,
						financeCategoryAttributes[position].getProfit());
				mapBrokerNamesIndex.put(brokerName, i);
				i++;
			}

			ValueComparator bvc = new ValueComparator(map);
			TreeMap<String, Double> sorted_map = new TreeMap<String, Double>(
					bvc);
			sorted_map.putAll(map);

			for (int j = 0; j < sorted_map.size(); j++) {

				String brokerName = (String) sorted_map.keySet().toArray()[j];
				posIndex = mapBrokerArrayAssign.get(brokerName);
				position = posIndex + numberOfBrokers * timeslotIndex;

				TariffDynamicData tdd = tariffDynamicDatas[position];

				// getLastTariffDynamicData ersetzen
				if (tdd == null) {

					int safeTimeslot = timeslotIndex - 1;
					position = posIndex + numberOfBrokers * safeTimeslot;

					tdd = tariffDynamicDatas[position];

					while (safeTimeslot >= 0 && tdd == null) {
						
						safeTimeslot--;
						
						if (safeTimeslot >= 0) {
							position = posIndex + numberOfBrokers
									* safeTimeslot;
							tdd = tariffDynamicDatas[position];
						}
					}

					if (tdd == null) {
						
						tdd = new TariffDynamicData(new DynamicData(
								startNumberTimeslot, 0, 0), 0, 0);
					}
				}

				int lastCompletedTimeslot = -1;
				
				if (visualizerBeanAttributes[timeslotIndex] != null) {
					
					lastCompletedTimeslot = visualizerBeanAttributes[timeslotIndex]
							.getTimeslotComplete().getTimeslotIndex();
				}

				int lastCompletedTimeslotIndexArray = lastCompletedTimeslot
						- startNumberTimeslot;
				position = posIndex + numberOfBrokers
						* lastCompletedTimeslotIndexArray;

				double profitDelta = 0;
				double energyDelta = 0;

				DynamicData dd = dynamicDatas[position];

				if (position >= 0 && dd == null) {

					int safeTimeslot = lastCompletedTimeslotIndexArray - 1;
					position = posIndex + numberOfBrokers * safeTimeslot;
					dd = dynamicDatas[position];

					while (safeTimeslot >= 0 && dd == null) {
						
						safeTimeslot--;
						
						if (safeTimeslot >= 0) {
							
							position = posIndex + numberOfBrokers
									* safeTimeslot;
							dd = dynamicDatas[position];
						}
					}

					if (dd != null) {
						
						profitDelta = dd.getProfitDelta();
						energyDelta = dd.getEnergyDelta();
					}
				} else if (position >= 0 && dd != null) {

					DynamicData lastWholesaledd = dynamicDatas[position];
					profitDelta = lastWholesaledd.getProfitDelta();
					energyDelta = lastWholesaledd.getEnergyDelta();
				}

				int customerDelta = tdd.getCustomerCount();
				Object[] pair = {
						mapBrokerNamesIndex
								.get(sorted_map.keySet().toArray()[j]),
						sorted_map.values().toArray()[j],
						customerDelta,
						tdd.getDynamicData().getTsIndex(),
						tdd.getDynamicData().getEnergyDelta(),
						tdd.getDynamicData().getProfitDelta(),
						tdd.getDynamicData().getTsIndex() 
								!= lastCompletedTimeslot ? 0
								: tdd.getCustomerCountDelta(), profitDelta,
						energyDelta

				};

				result.add(pair);
			}

		}
		return gson.toJson(result);
	}

	/**
	 * Value Comparator.
	 */
	class ValueComparator implements Comparator<String> {

		Map<String, Double> base;

		public ValueComparator(Map<String, Double> base) {
			this.base = base;
		}

		// Note: this comparator imposes orderings that are inconsistent with
		// equals.
		public int compare(String a, String b) {
			if (base.get(a) >= base.get(b)) {
				return -1;
			} else {
				return 1;
			} // returning 0 would merge keys
		}
	}

	/**
	 * Ascertain the last tariff dynamic data.
	 * 
	 * @param brokerName Broker name
	 * @return Last tariff dynamic data.
	 */
	public TariffDynamicData getLastTariffDynamicData(String brokerName) {

		int currentTimeslot;
		int startNumberTimeslot;
		int numberOfBrokers;
		int posIndex;
		int position;
		int endNumberTimeslot;
		TariffDynamicData tdd = null;
		
		Map<String, Integer> mapBrokerArrayAssign = logDAOImplExtended
				.getMapBrokerArrayAssign();
		TariffDynamicData[] tariffDynamicData = logDAOImplExtended
				.getTariffDynamicsData();

		if (mapBrokerArrayAssign != null && tariffDynamicData != null) {

			currentTimeslot = logParametersBean.getTimeslot();
			startNumberTimeslot = logDAOImplExtended.getStartNumberTimeslot();
			numberOfBrokers = mapBrokerArrayAssign.size();
			endNumberTimeslot = currentTimeslot - startNumberTimeslot;
			posIndex = mapBrokerArrayAssign.get(brokerName);
			position = posIndex + numberOfBrokers * endNumberTimeslot;

			tdd = tariffDynamicData[position];

			if (tdd == null) {

				int safeTimeslot = endNumberTimeslot - 1;
				position = posIndex + numberOfBrokers * safeTimeslot;

				tdd = tariffDynamicData[position];

				while (safeTimeslot >= 0 && tdd == null) {
					
					safeTimeslot--;
					
					if (safeTimeslot >= 0) {
						
						position = posIndex + numberOfBrokers * safeTimeslot;
						tdd = tariffDynamicData[position];
					}
				}

				if (tdd == null) {
					
					tdd = new TariffDynamicData(new DynamicData(
							startNumberTimeslot, 0, 0), 0, 0);
				}
			}
		}

		return tdd;
	}
		
	/**
	 * Ascertain energy delta.
	 * 
	 * @return energy delta.
	 */
	public ArrayList<Double> getEnergyDelta() {

		List<String> brokerNames;
		Competition competition = logDAOImplExtended.getCompetition();
		ArrayList<Double> energy = new ArrayList<Double>();

		if (competition != null) {
			
			brokerNames = competition.getBrokers();

			for (String brokerName : brokerNames) {
				
				energy.add(getLastTariffDynamicData(brokerName)
						.getDynamicData().getEnergyDelta());
			}
		}

		return energy;
	}

	/**
	 * Ascertain revenue delta.
	 * 
	 * @return revenue delta.
	 */
	public ArrayList<Double> getRevenueDelta() {

		List<String> brokerNames;
		Competition competition = logDAOImplExtended.getCompetition();
		ArrayList<Double> revenue = new ArrayList<Double>();

		if (competition != null) {

			brokerNames = competition.getBrokers();

			for (String brokerName : brokerNames) {

				revenue.add(getLastTariffDynamicData(brokerName)
						.getDynamicData().getProfitDelta());
			}
		}

		return revenue;
	}

	/**
	 * Ascertain customer delta.
	 * 
	 * @return customer delta.
	 */
	public ArrayList<Integer> getCustomersDelta() {

		List<String> brokerNames;
		Competition competition = logDAOImplExtended.getCompetition();
		ArrayList<Integer> customers = new ArrayList<Integer>();

		if (competition != null) {

			brokerNames = competition.getBrokers();

			for (String brokerName : brokerNames) {

				customers.add(getLastTariffDynamicData(brokerName)
						.getCustomerCountDelta());
			}
		}

		return customers;
	}
	
	public ArrayList<String> getBrokerNames() {
		return this.names;
	}

	public int getNumberOfBrokers() {
		return this.names.size();
	}
}
