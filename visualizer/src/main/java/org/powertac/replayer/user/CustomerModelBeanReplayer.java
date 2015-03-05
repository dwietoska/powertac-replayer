package org.powertac.replayer.user;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.powertac.common.CustomerInfo;
import org.powertac.common.TariffTransaction;
import org.powertac.common.TariffTransaction.Type;
import org.powertac.replayer.data.LogDataImpl;
import org.powertac.replayer.visualizer.LogParametersBean;
import org.powertac.visualizer.display.BrokerSeriesTemplate;
import org.primefaces.event.TabChangeEvent;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.Gson;

/**
 * Represent the view customer. Called when in replaying method the view 
 * customer was requested. Loads the customer data up to the current 
 * time slot. 
 * 
 * Modified from CustomersBean from visualizer.
 * 
 * @author DWietoska
 */
public class CustomerModelBeanReplayer {
	
	/**
	 * Access to beans.
	 */
	@Autowired 
	VisualizerHelperServiceReplayer helper;
	
	/**
	 * Access to all data.
	 */
	@Autowired
	private LogDataImpl logDaoImplExtended4;
	
	/**
	 * Access to replayer-view.
	 */
	@Autowired
	private LogParametersBean logParametersBean;
	
	/**
	 * Contains all customer.
	 */
	private CustomerInfo[] customerInfo;
	
	/**
	 * JSON for customer data per time slot.
	 */
	private String wholesaleDynDataOneTimeslot = "[]";
	
	/**
	 * Contains the id from chosen customer.
	 */
	private long choosenCustomerInfoId;
	
	/**
	 * Contains for tab view the chosen id.
	 */
	private int activeIndex;
	
	/**
	 * Creates a new customer view.
	 */
	public CustomerModelBeanReplayer() {
	}

	/**
	 * Creates a new customer view.
	 * 
	 * @param logDAOImplExtended Contains all replaying data.
	 * @param helper Contains helper methods.
	 * @param logParametersBean Log view.
	 */
	public CustomerModelBeanReplayer(LogDataImpl logDaoImplExtended4,
			LogParametersBean logParametersBean, 
			VisualizerHelperServiceReplayer helper) {
		
		this.helper = helper;
		this.logParametersBean = logParametersBean;
		this.logDaoImplExtended4 = logDaoImplExtended4;
	}
	
	/**
	 * The tab view displays all data from the returned customers.
	 * @return Customers.
	 */
	public CustomerInfo[] getCustomerInfo() {

		customerInfo = logDaoImplExtended4.getCustomersInfo();	

		return customerInfo;
	}

	/**
	 * Creates json data for current tab view.
	 * 
	 * @return Json
	 */
	public String getWholesaleDynDataOneTimeslot() {

		Gson gson = new Gson();
		ArrayList<Object> wholesaleTxDataOneTimeslot = new ArrayList<Object>();
		ArrayList<Object> profitDataOneTimeslot = new ArrayList<Object>();
		ArrayList<Object> mwhDataOneTimeslot = new ArrayList<Object>();

		List<TariffTransaction>[] listTariffTransaction = logDaoImplExtended4
				.getListCustomerTariffTransaction();
		Map<Long, Integer> mapCustomerArrayAssign = logDaoImplExtended4
				.getMapCustomerArrayAssign();
		
		int customerIndex = -1;

		if (customerInfo == null) {

			if (logDaoImplExtended4.getCustomersInfo() != null && 
					logDaoImplExtended4.getCustomersInfo().length > 0) {
				
				choosenCustomerInfoId = logDaoImplExtended4.getCustomersInfo()[0]
						.getId();
				customerIndex = mapCustomerArrayAssign
						.get(choosenCustomerInfoId);
			}
		} else {

			if (mapCustomerArrayAssign.get(choosenCustomerInfoId) == null
					&& customerInfo.length > 0) {

				choosenCustomerInfoId = customerInfo[0].getId();
				customerIndex = mapCustomerArrayAssign.get(customerInfo[0].getId());
			} else {
				customerIndex = mapCustomerArrayAssign
						.get(choosenCustomerInfoId);
			}
		}
		
		if (customerIndex >= 0 && customerIndex < 
				listTariffTransaction.length) {

			List<TariffTransaction> listTx = 
					listTariffTransaction[customerIndex];
			int currentTimeslot = logParametersBean.getTimeslot();
			
			if (listTx != null) {
				
				for (TariffTransaction tx : listTx) {
					
					if (tx.getPostedTimeslotIndex() <= currentTimeslot) {
						
						if (tx.getTxType() == Type.CONSUME
								|| tx.getTxType() == Type.PRODUCE) {

							Object[] profitOneTimeslot = {
									helper.getMillisForIndex(tx
											.getPostedTimeslotIndex(),
											logDaoImplExtended4
													.getCompetition()),
									tx.getCharge() };
							
							Object[] kWhOneTimeslot = {
									helper.getMillisForIndex(tx
											.getPostedTimeslotIndex(),
											logDaoImplExtended4
													.getCompetition()),
									tx.getKWh() };

							profitDataOneTimeslot.add(profitOneTimeslot);
							mwhDataOneTimeslot.add(kWhOneTimeslot);
						}
					} else {
						
						break;
					}
				}
			}
		}
		
		wholesaleTxDataOneTimeslot.add(new BrokerSeriesTemplate("Price(€)",
				"#808080", 0, profitDataOneTimeslot, true));
		wholesaleTxDataOneTimeslot.add(new BrokerSeriesTemplate("Energy(MWh)",
				"#8BBC21", 1, mwhDataOneTimeslot, true));	
		this.wholesaleDynDataOneTimeslot = gson
				.toJson(wholesaleTxDataOneTimeslot);
		
		return wholesaleDynDataOneTimeslot;
	}

	/**
	 * Called when a tab was selected. Sets the chosen customer id.
	 * 
	 * @param event TabChangeEvent
	 */
	public void onChange(TabChangeEvent event) {

		int i;

		for (i = 0; i < this.customerInfo.length; i++) {

			if (this.customerInfo[i].getName().equals(
					((CustomerInfo) event.getData()).getName())) {
				
				choosenCustomerInfoId = this.customerInfo[i].getId();
				break;
			}
		}
	}

	public long getChoosenCustomerInfoId() {
		return choosenCustomerInfoId;
	}

	public void setChoosenCustomerInfoId(long choosenCustomerInfoId) {
		this.choosenCustomerInfoId = choosenCustomerInfoId;
	}

	public int getActiveIndex() {
		return activeIndex;
	}

	public void setActiveIndex(int activeIndex) {
		this.activeIndex = activeIndex;
	}
}
