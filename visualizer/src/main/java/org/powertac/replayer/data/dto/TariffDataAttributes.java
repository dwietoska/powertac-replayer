package org.powertac.replayer.data.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.concurrent.ConcurrentHashMap;



//import org.apache.log4j.Logger;
import org.powertac.common.CustomerInfo;
import org.powertac.common.Rate;
import org.powertac.common.TariffSpecification;
import org.powertac.common.TariffTransaction;
//import org.powertac.visualizer.domain.broker.BrokerModel;
import org.powertac.visualizer.domain.broker.TariffCustomerStats;
//import org.powertac.visualizer.domain.broker.TariffData;



import com.google.gson.Gson;

/**
 * Contains data that are required for the tariff specification view in 
 * the visualizer. Used by DAO to store this data.
 * 
 * @author DWietoska
 */
public class TariffDataAttributes {
	
	/**
	 * Tariff specification.
	 */
	private TariffSpecification spec;
	
	/**
	 * Broker name.
	 */
	private String brokerName;
	
	/**
	 * Profit.
	 */
	private double profit;
	
	/**
	 * NetKWh.
	 */
	private double netKWh;
	
	/**
	 * Customers.
	 */
	private long customers;
	
	/**
	 * Power Type.
	 */
	private String powerType;
	
	/**
	 * Json for rates.
	 */
	private String ratesGraph;
	
	/**
	 * Gson to create Json objects.
	 */
	Gson gson = new Gson();

	/**
	 * Creates new TariffDataAttributes.
	 * 
	 * @param tariffDataAttribute
	 *            TariffDataAttribute
	 */
	public TariffDataAttributes(TariffDataAttributes tariffDataAttribute) {
		
		this.spec = tariffDataAttribute.getSpec();
		this.brokerName = tariffDataAttribute.getBrokerName();
		this.profit = tariffDataAttribute.getProfitNormal();
		this.netKWh = tariffDataAttribute.getNetKWhNormal();
		this.customers = tariffDataAttribute.getCustomers();
		this.powerType = tariffDataAttribute.getPowerType();
		this.ratesGraph = tariffDataAttribute.getRatesGraph();
	}

	/**
	 * Creates new TariffDataAttributes.
	 * 
	 * @param spec TariffSpecification
	 */
	public TariffDataAttributes(TariffSpecification spec) {
		
		this.brokerName = spec.getBroker().getUsername();
		this.spec = spec;
		powerType = spec.getPowerType().toString();
		createRatesGraph();
	}

	/**
	 * Creates new TariffDataAttributes.
	 */
	public TariffDataAttributes(
			String brokerName,
			TariffSpecification spec,
			double profit,
			double netKWh,
			long customers,
			String powerType,
			String ratesGraph,
			ConcurrentHashMap<CustomerInfo, TariffCustomerStats> 
				tariffCustomerStats) {
		
		super();
	    this.brokerName = brokerName;
		this.spec = spec;
		this.profit = profit;
		this.netKWh = netKWh;
		this.customers = customers;
		this.powerType = powerType;
		this.ratesGraph = ratesGraph;
	}

	/**
	 * Add TariffDataAttributes.
	 */
	public void add(double profit, double netKWh, long customers) {
		this.profit += profit;
		this.netKWh += netKWh;
		this.customers += customers;
	}
	
	/**
	 * Process tariff transaction.
	 */
	public void processTariffTx(TariffTransaction tx) {
		profit += tx.getCharge();
		netKWh += tx.getKWh();
	}
	
	/** Getter and Setter and toString. */
	public double getNetKWhNormal() {
		return netKWh;
	}
	
	public double getNetKWh() {
		return Math.round(netKWh);
	}

	public long getCustomers() {
		return customers;
	}

	public double getProfitNormal() {
		return profit;
	}
	
	public double getProfit() {
		return Math.round(profit);
	}

	public BigDecimal getProfitInThousandsOfEuro() {
		return new BigDecimal(profit / 1000).setScale(2, 
				RoundingMode.HALF_UP);
	}

	public TariffSpecification getSpec() {
		return spec;
	}

	public void setCustomers(long customers) {
		this.customers += customers;
	}

	public String getPowerType() {
		return powerType;
	}

	public BigDecimal getNetMWh() {
		return new BigDecimal(netKWh / 1000).setScale(2, 
				RoundingMode.HALF_UP);
	}
	
	/**
	 * Create Json for tariff specification rates.
	 */
	private void createRatesGraph() {
		
		ArrayList<Object> series = new ArrayList<Object>();
		Calendar calendar = GregorianCalendar.getInstance();

		for (Rate rate : spec.getRates()) {

			// check if day of the week is specified
			if (rate.getWeeklyBegin() != -1) {
				
				ArrayList<Object> data = new ArrayList<Object>();
				// For start we only need the day of the week; January 2007 is
				// used
				// since the days for January 2007 correspond to days in Rate
				// class
				// (1 == monday, ...)
				calendar.clear();
				calendar.set(2007, 0, rate.getWeeklyBegin(), rate
						.getDailyBegin() >= 0 ? rate.getDailyBegin() + 1 : 0,
						0, 0);
				Object[] start = { calendar.getTimeInMillis(),
						rate.getMinValue() * 100 };

				data.add(start);
				calendar.clear();
				calendar.set(
						2007,
						0,
						rate.getWeeklyEnd() >= 0 ? rate.getWeeklyEnd() : rate
								.getWeeklyBegin(),
						rate.getDailyEnd() >= 0 ? rate.getDailyEnd() + 1 : 23,
						0, 0);
				Object[] end = { calendar.getTimeInMillis(),
						rate.getMinValue() * 100 };
				data.add(end);
				series.add(new RatesGraphTemplate(data));
			} else if (rate.getDailyBegin() != -1 && rate.getDailyEnd() != -1) {
				
				ArrayList<Object> data = new ArrayList<Object>();
				calendar.clear();
				calendar.set(2006, 0, 1, rate.getDailyBegin() + 1, 0, 0);
				Object[] start = { calendar.getTimeInMillis(),
						rate.getMinValue() * 100 };
				data.add(start);
				calendar.clear();
				calendar.set(2006, 0, 1, rate.getDailyEnd() + 1, 0, 0);
				Object[] end = { calendar.getTimeInMillis(),
						rate.getMinValue() * 100 };
				data.add(end);
				series.add(new RatesGraphTemplate(data));
			} else if (rate.getDailyBegin() == -1 && rate.getDailyEnd() == -1
					&& rate.getWeeklyBegin() == -1 && rate.getWeeklyEnd() == -1) {
				
				ArrayList<Object> data = new ArrayList<Object>();
				calendar.clear();
				calendar.set(2007, 0, 1, 1, 0, 0);
				Object[] start = { calendar.getTimeInMillis(),
						rate.getMinValue() * 100 };
				data.add(start);
				calendar.clear();
				calendar.set(2007, 0, 7, 1, 0, 0);
				Object[] end = { calendar.getTimeInMillis(),
						rate.getMinValue() * 100 };
				data.add(end);
				series.add(new RatesGraphTemplate(data));
			}
		}

		ratesGraph = gson.toJson(series);
	}

	/**
	 * RatesGraphTemplate.
	 */
	private class RatesGraphTemplate {
		ArrayList<Object> data;

		public RatesGraphTemplate(ArrayList<Object> data) {
			this.data = data;
		}
	}

	public String getRatesGraph() {
		return ratesGraph;
	}

	public String getBrokerName() {
		return brokerName;
	}

	public void setBrokerName(String brokerName) {
		this.brokerName = brokerName;
	}

	@Override
	public boolean equals(Object obj) {
		 
		TariffDataAttributes b = (TariffDataAttributes) obj;
	    if (this.spec.getId() == b.spec.getId()) {
	      return true;
	    }
	    return false;
	  }
	
	@Override
	public String toString() {
		return "TariffDataAttributes [spec=" + spec + ", brokerName="
				+ brokerName + ", profit=" + profit + ", netKWh=" + netKWh
				+ ", customers=" + customers + ", powerType=" + powerType
				+ ", ratesGraph=" + ratesGraph + "]";
	}
}
