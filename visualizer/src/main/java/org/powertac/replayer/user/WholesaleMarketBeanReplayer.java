package org.powertac.replayer.user;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import org.powertac.common.ClearedTrade;
import org.powertac.common.Competition;
import org.powertac.common.MarketTransaction;
import org.powertac.replayer.data.LogDataImpl;
import org.powertac.replayer.data.dto.ClearedTradeObject;
import org.powertac.replayer.data.dto.VisualizerBeanAttributes;
import org.powertac.replayer.visualizer.LogParametersBean;
import org.powertac.visualizer.display.BrokerSeriesTemplate;
import org.powertac.visualizer.display.WholesaleAnalysisTemplate;
import org.powertac.visualizer.domain.Appearance;
import org.powertac.visualizer.statistical.DynamicData;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.Gson;

/**
 * Called when in replaying method the views for wholesale market was 
 * requested. Loads the wholesale market data up to the current time slot.
 * 
 * Modified from WholesaleMarketBean from visualizer.
 * 
 * @author DWietoska
 */
public class WholesaleMarketBeanReplayer implements Serializable {

	/**
	 * serialVersionUID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * JSON for energy data.
	 */
	private String energyMostRecentClearingsJson;
	
	/**
	 * JSON for cash data.
	 */
	private String cashMostRecentClearingsJson;

	/**
	 * JSON for cleared Trade data.
	 */
	private String clearedTradesDataJson;
	
	/**
	 * JSON for all market transaction data.
	 */
	private String allMarketTransactionsData;

	/**
	 * JSON for cumulative data.
	 */
	private String wholesaleDynData;
	
	/**
	 * JSON for data per timeslot.
	 */
	private String wholesaleDynDataOneTimeslot;
	
	/**
	 * JSON for wholesale average data.
	 */
	private String wholesaleAverageTimeslotPriceData;

	/**
	 * Number of timeslots to display.
	 */
	private final int TIMESLOTS_TO_DISPLAY = 48;

	/**
	 * Called every time, if a wholesale view was requested.
	 * 
	 * @param logDAOImplExtended Contains all replaying data.
	 * @param helper Contains helper methods.
	 * @param logParametersBean Log view.
	 */
	@Autowired
	public WholesaleMarketBeanReplayer(LogDataImpl logDAOImplExtended,
			LogParametersBean logParametersBean,
			VisualizerHelperServiceReplayer helper) {

		Gson gson = new Gson();
		createMostRecentClearings(gson, logDAOImplExtended, logParametersBean,
				helper);
		createAllClearings(gson, logDAOImplExtended, logParametersBean, 
				helper);
		createBrokerWholesaleTransactions(gson, logDAOImplExtended,
				logParametersBean, helper);
	}

	/**
	 * Creates the wholesale data.
	 * 
	 * @param gson GSON
	 * @param logDAOImplExtended Contains all replaying data.
	 * @param helper Contains helper methods.
	 * @param logParametersBean Log view.
	 */
	private void createBrokerWholesaleTransactions(Gson gson,
			LogDataImpl logDAOImplExtended,
			LogParametersBean logParametersBean,
			VisualizerHelperServiceReplayer helper) {

		int safetyWholesaleTs;
		int startNumberTimeslot;
		int numberOfBrokers;
		int posIndex;
		int position;
		int ts;
		boolean isDummy;
		int safetyWholesaleTsArrayPos;
		int currentTsIndex;
	    double totalProfit;
	    double totalEnergy;
	    List<String> brokerNames;
	    
		Map<String, Integer> mapBrokerArrayAssign = logDAOImplExtended
				.getMapBrokerArrayAssign();
		DynamicData[] dynamicData = logDAOImplExtended
				.getMarketTransactionsData();
		List<MarketTransaction>[] listMarketTransaction = 
				logDAOImplExtended.getAllMarketTransactionsData();
		VisualizerBeanAttributes[] visualizerBeanAttributes = 
				logDAOImplExtended.getVisualizerBeanAttributesData();
		Competition competition = logDAOImplExtended.getCompetition();
		Appearance[] appearances = logDAOImplExtended.getBrokerAppearances();
	      
		ArrayList<Object> allWholesaleData = new ArrayList<Object>();
		ArrayList<Object> wholesaleTxData = new ArrayList<Object>();
		ArrayList<Object> wholesaleTxDataOneTimeslot = new ArrayList<Object>();
		
		if (dynamicData != null && listMarketTransaction != null
				&& visualizerBeanAttributes != null && competition != null
				&& appearances != null) {
			
			brokerNames = competition.getBrokers();
			safetyWholesaleTs = logParametersBean.getTimeslot() - 1;
			startNumberTimeslot = logDAOImplExtended.getStartNumberTimeslot();
			numberOfBrokers = mapBrokerArrayAssign.size();
			safetyWholesaleTsArrayPos = safetyWholesaleTs - startNumberTimeslot;
			
			for (String brokerName : brokerNames) {

				ArrayList<Object> profitData = new ArrayList<Object>();
				ArrayList<Object> netMwhData = new ArrayList<Object>();
				// one timeslot
				ArrayList<Object> profitDataOneTimeslot = new ArrayList<Object>();
				ArrayList<Object> mwhDataOneTimeslot = new ArrayList<Object>();
				// market tx data
				ArrayList<Object> wholesaleTxBrokerData = new ArrayList<Object>();

				posIndex = mapBrokerArrayAssign.get(brokerName);
				ts = 0;
				isDummy = true;
				totalProfit = 0;
				totalEnergy = 0;

				// dynamic balancing data:
				for (ts = 0; ts <= safetyWholesaleTsArrayPos; ts++) {

					position = posIndex + numberOfBrokers * ts;
					currentTsIndex = ts + startNumberTimeslot;

					DynamicData dynData = dynamicData[position];

					if (dynData != null) {

						totalEnergy += dynData.getEnergyDelta();
						totalProfit += dynData.getProfitDelta();

						isDummy = false;
						Object[] profit = {
								helper.getMillisForIndex(currentTsIndex,
										competition),
								totalProfit };
						Object[] netMwh = {
								helper.getMillisForIndex(currentTsIndex,
										competition),
								totalEnergy };

						profitData.add(profit);
						netMwhData.add(netMwh);

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
						mwhDataOneTimeslot.add(kWhOneTimeslot);
					}
				}

				if (isDummy) {
					// dummy
					Object[] dummy = { helper.getMillisForIndex(0,
							competition), 0 };
					profitData.add(dummy);
					netMwhData.add(dummy);
					profitDataOneTimeslot.add(dummy);
					mwhDataOneTimeslot.add(dummy);
				}

				// All Wholesale Data
				int startTs = safetyWholesaleTsArrayPos - TIMESLOTS_TO_DISPLAY < 0 ? 0
						: safetyWholesaleTsArrayPos - TIMESLOTS_TO_DISPLAY;

				for (ts = startTs; ts <= safetyWholesaleTsArrayPos - 1; ts++) {

					position = posIndex + numberOfBrokers * ts;
					List<MarketTransaction> mtxList = listMarketTransaction[position];

					if (mtxList != null) {

						for (Iterator iterator3 = mtxList.iterator(); iterator3
								.hasNext();) {
							MarketTransaction marketTransaction = (MarketTransaction) iterator3
									.next();
							Object[] mtxEntry = { marketTransaction.getPrice(),
									marketTransaction.getMWh() };
							wholesaleTxBrokerData.add(mtxEntry);
						}
					}
				}

				Appearance appearance = appearances[posIndex];
				wholesaleTxData.add(new BrokerSeriesTemplate(brokerName,
						appearance.getColorCode(), 0,
						profitData, true));
				wholesaleTxData.add(new BrokerSeriesTemplate(brokerName,
						appearance.getColorCode(), 1,
						netMwhData, false));

				// one timeslot:
				wholesaleTxDataOneTimeslot
						.add(new BrokerSeriesTemplate(brokerName, appearance
								.getColorCode(), 0, profitDataOneTimeslot, 
								true));
				wholesaleTxDataOneTimeslot.add(new BrokerSeriesTemplate(
						brokerName, appearance.getColorCode(), 1, 
						mwhDataOneTimeslot, false));
				allWholesaleData.add(new BrokerSeriesTemplate(brokerName,
						appearance.getColorCodeRGBShading(),
						wholesaleTxBrokerData, true));
			}
			
			this.wholesaleDynData = gson.toJson(wholesaleTxData);
			this.wholesaleDynDataOneTimeslot = gson
					.toJson(wholesaleTxDataOneTimeslot);
		} else {
			
			this.wholesaleDynData = "[[0,0]]";
			this.wholesaleDynDataOneTimeslot = "[[0,0]]";
		}

		this.allMarketTransactionsData = gson.toJson(allWholesaleData);
	}

	/**
	 * Creates the wholesale average data.
	 * 
	 * @param gson GSON
	 * @param logDAOImplExtended Contains all replaying data.
	 * @param helper Contains helper methods.
	 * @param logParametersBean Log view.
	 */
	private void createAllClearings(Gson gson,
			LogDataImpl logDAOImplExtended,
			LogParametersBean logParametersBean, 
			VisualizerHelperServiceReplayer helper) {

		int safetyWholesaleTs;
		int startNumberTimeslot;
		int ts;
		int safetyWholesaleTsArrayPos;
		
		List<ClearedTradeObject>[] listClearedTrades = logDAOImplExtended
				.getClearedTradesData();
		Competition competition = logDAOImplExtended.getCompetition();
		
		ArrayList<Object> allClearedTrades = new ArrayList<Object>();
		ArrayList<Object> totalDataOneTimeslot = new ArrayList<Object>();
		ArrayList<Object> averageProfitPerTimeslot = new ArrayList<Object>();

		// Contains total amount of money payed in each timeslot.
		ConcurrentHashMap<Long, Double> totalPriceInTimeslot = 
				new ConcurrentHashMap<Long, Double>();
		// Number of transactions in timeslot
		ConcurrentHashMap<Long, Integer> numberOfTransactions = 
				new ConcurrentHashMap<Long, Integer>();
		// Contains total amount of energy traded in each timeslot.
		ConcurrentHashMap<Long, Double> totalEnergyInTimeslot = 
				new ConcurrentHashMap<Long, Double>();

		safetyWholesaleTs = logParametersBean.getTimeslot() - 1;
		
		if (listClearedTrades != null && competition != null) {

			startNumberTimeslot = logDAOImplExtended.getStartNumberTimeslot();
			safetyWholesaleTsArrayPos = safetyWholesaleTs - 
					startNumberTimeslot;
			
			for (ts = 0; ts <= safetyWholesaleTsArrayPos; ts++) {

				List<ClearedTradeObject> listClearedTradeObject = 
						listClearedTrades[ts];

				if (listClearedTradeObject != null) {

					for (Iterator<ClearedTradeObject> iterator2 = 
							listClearedTradeObject.iterator(); 
							iterator2.hasNext();) {

						ClearedTradeObject cto = (ClearedTradeObject) iterator2
								.next();
						ClearedTrade ct = cto.getClearedTrade();

						Object[] entry = { ct.getExecutionMWh(),
								ct.getExecutionPrice() };
						allClearedTrades.add(entry);

						// If there is no entry for timeslot, create one;
						// otherwise
						// update given timeslot with amount of money and
						// increase number of transactions in timeslot.
						if (!totalPriceInTimeslot.containsKey(helper
								.getMillisForIndex(ct.getTimeslotIndex(),
										competition))) {
							
							totalPriceInTimeslot.put(helper
									.getMillisForIndex(ct.getTimeslotIndex(),
											competition),
									ct.getExecutionPrice());
							totalEnergyInTimeslot.put(helper
									.getMillisForIndex(ct.getTimeslotIndex(),
											competition),
									ct.getExecutionMWh());
							numberOfTransactions.put(helper
									.getMillisForIndex(ct.getTimeslotIndex(),
											competition), 1);
						} else {
							
							totalPriceInTimeslot.put(
									helper.getMillisForIndex(ct
											.getTimeslotIndex(),
											competition),
									totalPriceInTimeslot.get(helper
											.getMillisForIndex(ct
													.getTimeslotIndex(),
													competition))
											+ ct.getExecutionPrice());
							
							totalEnergyInTimeslot.put(
									helper.getMillisForIndex(ct
											.getTimeslotIndex(),
											competition),
									totalEnergyInTimeslot.get(helper
											.getMillisForIndex(ct
													.getTimeslotIndex(),
													competition))
											+ ct.getExecutionMWh());
							
							numberOfTransactions.put(helper
									.getMillisForIndex(ct.getTimeslotIndex(),
											competition),
									numberOfTransactions.get(helper
											.getMillisForIndex(ct
													.getTimeslotIndex(),
													competition)) + 1);
						}
					}
				}
			}
		}
		
		// Sort keys (timeslots)
		SortedSet<Long> sortedSet = new TreeSet<Long>(
				totalPriceInTimeslot.keySet()).headSet(
				helper.getMillisForIndex(safetyWholesaleTs, 
						competition), true);

		for (Iterator<Long> totalIterator = sortedSet.iterator(); totalIterator
				.hasNext();) {
			
			Long timeslot = (Long) totalIterator.next();
			Double totalProfitTimeslot = totalPriceInTimeslot.get(timeslot);
			Object[] totalProfitOneTimeslot = { timeslot,
					totalProfitTimeslot / numberOfTransactions.get(timeslot),
					totalEnergyInTimeslot.get(timeslot) };
			totalDataOneTimeslot.add(totalProfitOneTimeslot);
		}

		averageProfitPerTimeslot.add(new WholesaleAnalysisTemplate(
				totalDataOneTimeslot));

		this.clearedTradesDataJson = gson.toJson(allClearedTrades);		
		this.wholesaleAverageTimeslotPriceData = gson
				.toJson(averageProfitPerTimeslot);
	}

	/**
	 * Creates the wholesale most recent clearings data.
	 * 
	 * @param gson GSON
	 * @param logDAOImplExtended Contains all replaying data.
	 * @param helper Contains helper methods.
	 * @param logParametersBean Log view.
	 */
	private void createMostRecentClearings(Gson gson,
			LogDataImpl logDAOImplExtended,
			LogParametersBean logParametersBean, 
			VisualizerHelperServiceReplayer helper) {

		int safetyWholesaleTs;
		int startNumberTimeslot;
		int ts;
		int safetyWholesaleTsArrayPos;
		int currentTsIndex;
		long lastKey;
		ClearedTrade mostRecentClearing;
		
		List<ClearedTradeObject>[] listClearedTrades = logDAOImplExtended
				.getClearedTradesData();
		Competition competition = logDAOImplExtended.getCompetition();
		
		// most recent clearings for each timeslot.
		ArrayList<Object> energyMostRecentClearings = new ArrayList<Object>();
		ArrayList<Object> cashMostRecentClearings = new ArrayList<Object>();

		if (listClearedTrades != null && competition != null) {
			
			safetyWholesaleTs = logParametersBean.getTimeslot() - 1;
			startNumberTimeslot = logDAOImplExtended.getStartNumberTimeslot();
			safetyWholesaleTsArrayPos = safetyWholesaleTs - 
					startNumberTimeslot;
			
			for (ts = 0; ts <= safetyWholesaleTsArrayPos; ts++) {

				lastKey = 0;
				mostRecentClearing = null;
				currentTsIndex = ts + startNumberTimeslot;
				List<ClearedTradeObject> listClearedTradeObject = 
						listClearedTrades[ts];

				if (listClearedTradeObject != null) {

					for (Iterator<ClearedTradeObject> iterator2 = 
							listClearedTradeObject.iterator(); 
							iterator2.hasNext();) {

						ClearedTradeObject cto = (ClearedTradeObject) iterator2
								.next();

						if (cto.getMillis() >= lastKey) {
							lastKey = cto.getMillis();
							mostRecentClearing = cto.getClearedTrade();
						}
					}

					Object[] energy = {
							helper.getMillisForIndex(currentTsIndex,
									competition),
							mostRecentClearing.getExecutionMWh() };
					Object[] cash = { helper.getMillisForIndex(currentTsIndex,
							competition),
							mostRecentClearing.getExecutionPrice() };
					energyMostRecentClearings.add(energy);
					cashMostRecentClearings.add(cash);
				}
			}
		}
		
		energyMostRecentClearingsJson = gson.toJson(energyMostRecentClearings);
		cashMostRecentClearingsJson = gson.toJson(cashMostRecentClearings);
	}

  public String getCashMostRecentClearingsJson ()
  {
    return cashMostRecentClearingsJson;
  }

  public String getEnergyMostRecentClearingsJson ()
  {
    return energyMostRecentClearingsJson;
  }

  public String getClearedTradesDataJson ()
  {
    return clearedTradesDataJson;
  }

  public String getAllMarketTransactionsData ()
  {
    return allMarketTransactionsData;
  }

  public String getWholesaleDynData ()
  {
    return wholesaleDynData;
  }

  public String getWholesaleDynDataOneTimeslot ()
  {
    return wholesaleDynDataOneTimeslot;
  }

  public String getWholesaleAverageTimeslotPriceData ()
  {
    return wholesaleAverageTimeslotPriceData;
  }
}
