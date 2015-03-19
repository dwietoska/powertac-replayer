package org.powertac.replayer.user;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.powertac.common.Competition;
import org.powertac.common.enumerations.PowerType;
import org.powertac.replayer.data.LogDataImpl;
import org.powertac.replayer.data.dto.TariffCategoryAttributes;
import org.powertac.replayer.data.dto.TariffDataAttributes;
import org.powertac.replayer.data.dto.VisualizerBeanAttributes;
import org.powertac.replayer.visualizer.LogParametersBean;
import org.powertac.visualizer.display.BrokerSeriesTemplate;
import org.powertac.visualizer.display.CustomerStatisticsTemplate;
import org.powertac.visualizer.display.DrillDownTemplate;
import org.powertac.visualizer.display.DrillDownTemplate2;
import org.powertac.visualizer.domain.Appearance;
import org.powertac.visualizer.domain.broker.TariffDynamicData;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.Gson;

/**
 * Called when in replaying method the view for tariff market was requested. Loads 
 * the tariff market data up to the current time slot.
 * Modified from TariffMarketBean from visualizer.
 * 
 * @author DWietoska
 */
public class TariffMarketBeanReplayer implements Serializable {

	/**
	 * serialVersionUID.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * JSON for cumulative data.
	 */
	private String tariffDynData;
	
	/**
	 * JSON for data per time slot.
	 */
	private String tariffDynDataOneTimeslot;
	
	/**
	 * Contains all tariff data.
	 */
//	private ArrayList<TariffData> allTarifs = new ArrayList<TariffData>();
	private ArrayList<TariffDataAttributes> allTarifsReplayer = new ArrayList<TariffDataAttributes>();
	
	/**
	 * JSON for customer statistics.
	 */
	private String customerStatictics;
//	private List<TariffData> filteredValue;
	
	/**
	 * Selected tariff data.
	 */
//	private TariffData selectedTariff;
	private TariffDataAttributes selectedTariffDataAttribute;
  
	/**
	 * Called everytime, if a tariff market view was requested.
	 * 
	 * @param logDAOImplExtended Contains all replaying data.
	 * @param helper Contains helper methods.
	 * @param logParametersBean Log view.
	 */
	@Autowired
	public TariffMarketBeanReplayer(LogDataImpl logDAOImplExtended,
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
		TariffCategoryAttributes[] tariffCategoryAttributes = logDAOImplExtended
				.getTariffCategoryAttributesData();
		TariffDynamicData[] tariffDynamicData = logDAOImplExtended
				.getTariffDynamicsData();
		List<TariffDataAttributes>[] listTariffDataAttributes = logDAOImplExtended
				.getTariffDataTransactionsData();
		Competition competition = logDAOImplExtended.getCompetition();
		VisualizerBeanAttributes[] visualizerBeanAttributes = logDAOImplExtended
				.getVisualizerBeanAttributesData();
		Appearance[] appearances = logDAOImplExtended.getBrokerAppearances();

		// Data Array
		ArrayList<Object> tariffData = new ArrayList<Object>();
		ArrayList<Object> tariffDataOneTimeslot = new ArrayList<Object>();
		ArrayList<Object> customerStaticticsArray = new ArrayList<Object>();
		
		Gson gson = new Gson();

		if (mapBrokerArrayAssign != null && tariffCategoryAttributes != null
				&& tariffDynamicData != null
				&& listTariffDataAttributes != null && competition != null
				&& visualizerBeanAttributes != null && appearances != null) {
		
			brokerNames = competition.getBrokers();
			currentTimeslot = logParametersBean.getTimeslot();
			startNumberTimeslot = logDAOImplExtended.getStartNumberTimeslot();
			numberOfBrokers = mapBrokerArrayAssign.size();
			endNumberTimeslot = currentTimeslot - startNumberTimeslot;
			
			if (endNumberTimeslot >= 0) {
	
			// TariffDataAttribues nach Broker aufteilen
			List<TariffDataAttributes> listTariffDataAttribute = listTariffDataAttributes[endNumberTimeslot];

			List<TariffDataAttributes>[] listTariffDataAttributesSortedBroker = new LinkedList[mapBrokerArrayAssign
					.size()];

			for (int i = 0; i < mapBrokerArrayAssign.size(); i++) {
				listTariffDataAttributesSortedBroker[i] = new LinkedList<TariffDataAttributes>();
			}

			if (listTariffDataAttribute != null) {
			for (TariffDataAttributes tariffDataAttributes : listTariffDataAttribute) {
				listTariffDataAttributesSortedBroker[mapBrokerArrayAssign
						.get(tariffDataAttributes.getBrokerName())]
						.add(tariffDataAttributes);
			}
			}

			for (String brokerName : brokerNames) {

				posIndex = mapBrokerArrayAssign.get(brokerName);
				ts = 0;

				Object customerStatisticsBroker;
				long[] customerType = new long[3];
				customerType[0] = 0; // consumption
				customerType[1] = 0; // production
				customerType[2] = 0; // storage
				HashMap<PowerType, Long> customerTypeSpecific = new HashMap<PowerType, Long>() {
					{
						put(PowerType.BATTERY_STORAGE, 0l);
						put(PowerType.CHP_PRODUCTION, 0l);
						put(PowerType.CONSUMPTION, 0l);
						put(PowerType.ELECTRIC_VEHICLE, 0l);
						put(PowerType.FOSSIL_PRODUCTION, 0l);
						put(PowerType.INTERRUPTIBLE_CONSUMPTION, 0l);
						put(PowerType.PRODUCTION, 0l);
						put(PowerType.PUMPED_STORAGE_PRODUCTION, 0l);
						put(PowerType.RUN_OF_RIVER_PRODUCTION, 0l);
						put(PowerType.SOLAR_PRODUCTION, 0l);
						// put(PowerType.STORAGE, 0l);
						put(PowerType.THERMAL_STORAGE_CONSUMPTION, 0l);
						put(PowerType.WIND_PRODUCTION, 0l);
					}
				};

				ArrayList<Object> customerNumberData = new ArrayList<Object>();
				ArrayList<Object> profitData = new ArrayList<Object>();
				ArrayList<Object> netKWhData = new ArrayList<Object>();
				ArrayList<Object> drillDataBroker = new ArrayList<Object>();

				List<TariffDataAttributes> listTariffDataAttributeSortedBroker = listTariffDataAttributesSortedBroker[posIndex];

				// calculating number of customers for each broker's tariff
				for (TariffDataAttributes tda : listTariffDataAttributeSortedBroker) {
					if (tda.getCustomers() > 0) {
						if (tda.getSpec().getPowerType().isConsumption())
							customerType[0] += tda.getCustomers();
						if (tda.getSpec().getPowerType().isProduction())
							customerType[1] += tda.getCustomers();
						if (tda.getSpec().getPowerType().isStorage())
							customerType[2] += tda.getCustomers();

						customerTypeSpecific.put(
								tda.getSpec().getPowerType(),
								customerTypeSpecific.get(tda.getSpec()
										.getPowerType()) + tda.getCustomers());
					}
				}

				ArrayList<Object> data_c = new ArrayList<Object>();
				data_c.add(new PowerTypeTemplate("Consumption",
						customerTypeSpecific.get(PowerType.CONSUMPTION)));
				data_c.add(new PowerTypeTemplate("Interruptible consumption",
						customerTypeSpecific
								.get(PowerType.INTERRUPTIBLE_CONSUMPTION)));
				data_c.add(new PowerTypeTemplate("Thermal storage consumption",
						customerTypeSpecific
								.get(PowerType.THERMAL_STORAGE_CONSUMPTION)));

				ArrayList<Object> data_p = new ArrayList<Object>();
				data_p.add(new PowerTypeTemplate("Production",
						customerTypeSpecific.get(PowerType.PRODUCTION)));
				data_p.add(new PowerTypeTemplate("Chp production",
						customerTypeSpecific.get(PowerType.CHP_PRODUCTION)));
				data_p.add(new PowerTypeTemplate("Fossil production",
						customerTypeSpecific.get(PowerType.FOSSIL_PRODUCTION)));
				data_p.add(new PowerTypeTemplate("Run of river production",
						customerTypeSpecific
								.get(PowerType.RUN_OF_RIVER_PRODUCTION)));
				data_p.add(new PowerTypeTemplate("Solar production",
						customerTypeSpecific.get(PowerType.SOLAR_PRODUCTION)));
				data_p.add(new PowerTypeTemplate("Wind production",
						customerTypeSpecific.get(PowerType.WIND_PRODUCTION)));

				ArrayList<Object> data_s = new ArrayList<Object>();
				// data_s.add(new PowerTypeTemplate("Storage",
				// customerTypeSpecific
				// .get(PowerType.STORAGE)));
				data_s.add(new PowerTypeTemplate("Battery storage",
						customerTypeSpecific.get(PowerType.BATTERY_STORAGE)));
				data_s.add(new PowerTypeTemplate("Electric vehicle",
						customerTypeSpecific.get(PowerType.ELECTRIC_VEHICLE)));
				data_s.add(new PowerTypeTemplate("Pumped storage production",
						customerTypeSpecific
								.get(PowerType.PUMPED_STORAGE_PRODUCTION)));

				DrillDownTemplate2 dt2_c = new DrillDownTemplate2("Consumers",
						customerType[0], new DrillDown("Specific", data_c));
				DrillDownTemplate2 dt2_p = new DrillDownTemplate2("Producers",
						customerType[1], new DrillDown("Specific", data_p));
				DrillDownTemplate2 dt2_s = new DrillDownTemplate2("Storage",
						customerType[2], new DrillDown("Specific", data_s));

				if (customerType[0] > 0) {
					drillDataBroker.add(dt2_c);
				}
				if (customerType[1] > 0) {
					drillDataBroker.add(dt2_p);
				}
				if (customerType[2] > 0) {
					drillDataBroker.add(dt2_s);
				}

				Appearance appearance = appearances[posIndex];

				customerStatisticsBroker = new DrillDownTemplate(brokerName,
						appearance.getColorCode(), drillDataBroker);

				isDummy = true;

				// one timeslot
				ArrayList<Object> customerNumberDataOneTimeslot = new ArrayList<Object>();
				ArrayList<Object> profitDataOneTimeslot = new ArrayList<Object>();
				ArrayList<Object> kwhDataOneTimeslot = new ArrayList<Object>();

				// dynamic balancing data:
				// Benutzt in Normal helper.getSafetyWholesaleTimeslotIndex(),
				// deswegen - 1
				for (ts = 0; ts <= endNumberTimeslot - 1; ts++) {

					position = posIndex + numberOfBrokers * ts;
					currentTsIndex = ts + startNumberTimeslot;

					TariffDynamicData dynData = tariffDynamicData[position];

					if (dynData != null) {

						isDummy = false;
						Object[] timeCustomerCount = {
								helper.getMillisForIndex(currentTsIndex,
										competition),
								dynData.getCustomerCount() };
						Object[] profit = {
								helper.getMillisForIndex(currentTsIndex,
										competition),
								dynData.getDynamicData().getProfit() };
						Object[] netKWh = {
								helper.getMillisForIndex(currentTsIndex,
										competition),
								dynData.getDynamicData().getEnergy() };

						customerNumberData.add(timeCustomerCount);
						profitData.add(profit);
						netKWhData.add(netKWh);

						// one timeslot:
						Object[] customerCountOneTimeslot = {
								helper.getMillisForIndex(currentTsIndex,
										competition),
								dynData.getCustomerCountDelta() };
						Object[] profitOneTimeslot = {
								helper.getMillisForIndex(currentTsIndex,
										competition),
								dynData.getDynamicData().getProfitDelta() };
						Object[] kWhOneTimeslot = {
								helper.getMillisForIndex(currentTsIndex,
										competition),
								dynData.getDynamicData().getEnergyDelta() };

						customerNumberDataOneTimeslot
								.add(customerCountOneTimeslot);
						profitDataOneTimeslot.add(profitOneTimeslot);
						kwhDataOneTimeslot.add(kWhOneTimeslot);
					}
				}
	
				if (isDummy) {
					// dummy:
					double[] dummy = { helper.getMillisForIndex(0,
							competition), 0 };
					customerNumberData.add(dummy);
					profitData.add(dummy);
					netKWhData.add(dummy);
					customerNumberDataOneTimeslot.add(dummy);
					profitDataOneTimeslot.add(dummy);
					kwhDataOneTimeslot.add(dummy);
				}

				tariffData.add(new BrokerSeriesTemplate(brokerName, appearance
						.getColorCode(), 0, // +
											// " PRICE"
						profitData, true));
				tariffData.add(new BrokerSeriesTemplate(brokerName, appearance
						.getColorCode(), 1, // +
											// " KWH"
						netKWhData, false));
				tariffData.add(new BrokerSeriesTemplate(brokerName, appearance
						.getColorCode(), 2, // +
											// " CUST"
						true, customerNumberData, false));

				// one timeslot:
				tariffDataOneTimeslot
						.add(new BrokerSeriesTemplate(brokerName, appearance // +
																				// " PRICE"
								.getColorCode(), 0, profitDataOneTimeslot, true));
				tariffDataOneTimeslot.add(new BrokerSeriesTemplate(brokerName,
						appearance // + " KWH"
								.getColorCode(), 1, kwhDataOneTimeslot, false));
				tariffDataOneTimeslot.add(new BrokerSeriesTemplate(brokerName,
						appearance // + " CUST"
								.getColorCode(), 2, true,
						customerNumberDataOneTimeslot, false));

				if (tariffCategoryAttributes[posIndex + numberOfBrokers * endNumberTimeslot] != null) {
				customerStaticticsArray.add(new CustomerStatisticsTemplate(
						brokerName, appearance.getColorCode(),
						tariffCategoryAttributes[posIndex + numberOfBrokers
								* endNumberTimeslot].getCustomerCount(),
						customerStatisticsBroker));
				}

				listTariffDataAttributeSortedBroker = listTariffDataAttributesSortedBroker[posIndex];

				for (TariffDataAttributes tariffDataAttribute : listTariffDataAttributeSortedBroker) {
					allTarifsReplayer.add(tariffDataAttribute);
				}

			}// end BROKER for loop
			
			this.tariffDynData = gson.toJson(tariffData);
			this.tariffDynDataOneTimeslot = gson.toJson(tariffDataOneTimeslot);
		} else {
			this.tariffDynData = "[[0,0]]";
			this.tariffDynDataOneTimeslot = "[[0,0]]";
		}
		} else {
			
			this.tariffDynData = "[[0,0]]";
			this.tariffDynDataOneTimeslot = "[[0,0]]";
		}
		
		this.customerStatictics = gson.toJson(customerStaticticsArray);
	}

	public String getTariffDynData() {
		return tariffDynData;
	}

	public String getTariffDynDataOneTimeslot() {
		return tariffDynDataOneTimeslot;
	}

//	public ArrayList<TariffData> getAllTarifs() {
//		return allTarifs;
//	}

	public ArrayList<TariffDataAttributes> getAllTarifsReplayer() {
		return allTarifsReplayer;
	}

//	public List<TariffData> getFilteredValue() {
//		return filteredValue;
//	}
//
//	public void setFilteredValue(List<TariffData> filteredValue) {
//		this.filteredValue = filteredValue;
//	}

	public String getCustomerStatictics() {
		return customerStatictics;
	}

//	public TariffData getSelectedTariff() {
//		return selectedTariff;
//	}
//
//	public void setSelectedTariff(TariffData selectedTariff) {
//		this.selectedTariff = selectedTariff;
//
//	}
	
	public TariffDataAttributes getSelectedTariffDataAttribute() {
		return selectedTariffDataAttribute;
	}

	public void setSelectedTariffDataAttribute(
			TariffDataAttributes selectedTariffDataAttribute) {
		this.selectedTariffDataAttribute = selectedTariffDataAttribute;
	}

	private class DrillDown {
		String name;
		ArrayList<Object> data;

		public DrillDown(String name, ArrayList<Object> data) {
			this.name = name;
			this.data = data;
		}

	}

	private class PowerTypeTemplate {
		String name;
		long y;

		public PowerTypeTemplate(String name, long y) {
			this.name = name;
			this.y = y;
		}
	}
}
