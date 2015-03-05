package org.powertac.replayer.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.powertac.common.BalancingTransaction;
import org.powertac.common.CashPosition;
import org.powertac.common.ClearedTrade;
import org.powertac.common.Competition;
import org.powertac.common.CustomerInfo;
import org.powertac.common.DistributionTransaction;
import org.powertac.common.MarketTransaction;
import org.powertac.common.Order;
import org.powertac.common.Rate;
import org.powertac.common.RateCore;
import org.powertac.common.RegulationRate;
import org.powertac.common.TariffSpecification;
import org.powertac.common.TariffTransaction;
import org.powertac.common.TimeService;
import org.powertac.common.WeatherForecast;
import org.powertac.common.WeatherReport;
import org.powertac.common.TariffTransaction.Type;
import org.powertac.common.msg.SimEnd;
import org.powertac.common.msg.SimStart;
import org.powertac.common.msg.TariffStatus;
import org.powertac.common.msg.TimeslotComplete;
import org.powertac.common.msg.TimeslotUpdate;
import org.powertac.replayer.GameInitialization;
import org.powertac.replayer.data.dto.BalancingCategoryAttributes;
import org.powertac.replayer.data.dto.ClearedTradeObject;
import org.powertac.replayer.data.dto.FinanceCategoryAttributes;
import org.powertac.replayer.data.dto.GradingAttributes;
import org.powertac.replayer.data.dto.TariffCategoryAttributes;
import org.powertac.replayer.data.dto.TariffDataAttributes;
import org.powertac.replayer.data.dto.VisualizerBeanAttributes;
import org.powertac.replayer.data.dto.WholesaleCategoryAttributes;
import org.powertac.replayer.logtool.ErrorReadDomainObject;
import org.powertac.replayer.logtool.NewObjectListenerReplayer;
import org.powertac.replayer.pusher.PushServiceReplayerNew;
import org.powertac.replayer.visualizer.LogParametersBean;
import org.powertac.visualizer.domain.Appearance;
import org.powertac.visualizer.domain.broker.TariffDynamicData;
import org.powertac.visualizer.statistical.DynamicData;
import org.powertac.visualizer.statistical.FinanceDynamicData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;

/**
 * Holds replaying data for current extended and normal mode 
 * (RunnerExtended and Runner). Uses array data structure 
 * because of speed reasons.
 * 
 * @author DWietoska
 */
@Service
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class LogDataImpl extends LogData implements 
		GameInitialization {

	/**
	 * Access to appearances.
	 */
	@Autowired
	private AppearanceListBeanReplayer appearanceListBean;

	/**
	 * Access to LogParametersBean.
	 */
	@Autowired
	private LogParametersBean logParametersBean;
	
	/**
	 * Access to PushServiceReplayerNew.
	 */
	@Autowired
	private PushServiceReplayerNew pushServiceReplayerNew;
	
	/**
	 * Collects all "Tariff Transaction" messages which occur before 
	 * SimStart message.
	 */
	private List<TariffTransaction> tariffTransactionRepo;
	
	/**
	 * Collects all "Tariff Specification" messages which occur before 
	 * SimStart message.
	 */
	private List<TariffSpecification> tariffSpecificationRepo;
	
	/**
	 * Collects all "Timeslot Update" messages which occur before 
	 * SimStart message.
	 */
	private List<TimeslotUpdate> tsUpdateRepo;
	
	/**
	 * Collects all "Weather Report" messages which occur before 
	 * SimStart message.
	 */
	private List<WeatherReport> weatherReportRepo;
	
	/**
	 * Collects all "Weather Forecast" messages which occur before 
	 * SimStart message.
	 */
	private List<WeatherForecast> weatherForecastRepo;
	
	/**
	 * Collects "Rate" messages for a time slot. 
	 */
	private List<RateCore> listRatesForTariffSpecification;
	
	/**
	 * Number of time services. 
	 */
	private int counterTimeServicesMsg;
	
	/**
	 * Appearances data.
	 */
	private volatile Appearance[] brokerAppearances;
			
	/**
	 * Contains all weather report data.
	 */
	private volatile WeatherReport[] weatherReportsData;
	
	/**
	 * Index position for weather report data.
	 */
	private int positionWeatherReportData;
	
	/**
	 * Contains all weather forecast data.
	 */
	private volatile WeatherForecast[] weatherForecastsData;
	
	/**
	 * Index position for weather forecast data.
	 */
	private int positionWeatherForecastData;
	
	/**
	 * Contains all data which is needed for grading view attributes.
	 */
	private volatile GradingAttributes[] gradingAttributesData;
	
	/**
	 * Last grading view attribute.
	 */
	private GradingAttributes lastGradingAttribute;
	
	/**
	 * Contains all balancing data for every time slot.
	 */
	private volatile DynamicData[] balancingsData;
	
	/**
	 * Last balancing data.
	 */
	private DynamicData[] lastBalancingsData;
	
	/**
	 * Contains all data which is needed for balancing view attributes.
	 */
	private volatile BalancingCategoryAttributes[] balancingCategoryAttributesData;
	
	/**
	 * Contains all distribution data for every time slot.
	 */
	private volatile DynamicData[] distributionsData;
	
	/**
	 * Last distribution data.
	 */
	private DynamicData[] lastDistributionsData;
	
	/**
	 * Contains all market transaction dynamic data for every time slot.
	 */
	private volatile DynamicData[] marketTransactionsData;
	
	/**
	 * Last market transaction dynamic data.
	 */
	private DynamicData[] lastMarketTransactionsData;
	
	/**
	 * Contains all "Market Transaction" messages for every time slot.
	 */
	private volatile List<MarketTransaction>[] allMarketTransactionsData;
	
	/**
	 * Contains all "Order" messages for every time slot.
	 */
	private volatile List<Order>[] orderTransactionsData;
	
	/**
	 * Contains all tariff transaction dynamic data for every time slot.
	 */
	private volatile DynamicData[] tariffTransactionsData;
	
	/**
	 * Last tariff transaction dynamic data.
	 */
	private DynamicData[] lastTariffTransactionsData;
	
	/**
	 * Contains all finance dynamic data for every time slot.
	 */
	private volatile FinanceDynamicData[] financeDynamicsData;
	
	/**
	 * Contains profit which is needed for finance view attributes.
	 */
	private volatile Double[] financeDynamicsDataProfit;

	/**
	 * Contains all tariff dynamic data for every time slot.
	 */
	private volatile TariffDynamicData[] tariffDynamicsData;
	
	/**
	 * Contains all data which is needed for wholesale view attributes.
	 */
	private volatile WholesaleCategoryAttributes[] wholesaleCategoryAttributesData;
	
	/**
	 * Last wholesale view attribute.
	 */
	private WholesaleCategoryAttributes[] lastWholesaleCategoryAttribute;
	
	/**
	 * Contains all data which is needed for tariff category view attributes.
	 */
	private volatile TariffCategoryAttributes[] tariffCategoryAttributesData;
	
	/**
	 * Last tariff category view attribute.
	 */
	private TariffCategoryAttributes[] lastTariffCategoryAttribute;
	  
	/**
	 * Contains all data which is needed for finance view attributes.
	 */
	private volatile FinanceCategoryAttributes[] financeCategoryAttributesData;
	
	/**
	 * Last finance view attribute.
	 */
	private FinanceCategoryAttributes[] lastFinanceCategoryAttribute;
	
	/**
	 * Contains all data which is needed for visualizer bean attributes.
	 */
	private volatile VisualizerBeanAttributes[] visualizerBeanAttributesData;
	
	/**
	 * Last visualizer bean attribute.
	 */
	private VisualizerBeanAttributes lastVisualizerBeanAttribute;
	
	/**
	 * Contains the position in the array data structure for a broker.
	 */
	private volatile Map<String, Integer> mapBrokerArrayAssign;
	
	/**
	 * Contains all "Cleared Trade" messages for every time slot.
	 */
	private volatile List<ClearedTradeObject>[] clearedTradesData; 

	/**
	 * Contains all "Tariff Data" attributes.
	 * Key: Id Tariff Specification
	 */
	private Map<Long, TariffDataAttributes> allTariffDataTransactionsData;

	/**
	 * Contains all "Tariff Data" attributes (which contains "Tariff 
	 * Specification") for every time slot.
	 */
	private volatile List<TariffDataAttributes>[] tariffDataTransactionsData;
	
	/**
	 * Contains the position in the array data structure for a customer.
	 */
	private volatile Map<Long, Integer> mapCustomerArrayAssign;
	
	/**
	 * Contains all "Tariff Transaction" messages for every time slot.
	 */
	private volatile List<TariffTransaction>[] listCustomerTariffTransaction;
	
	/**
	 * Contains all "Customer Info" messages for every time slot.
	 */
	private volatile CustomerInfo[] customersInfo;

	/**
	 * Open time slot messages.
	 */
	private int diffLastFirstEnabled;
	
	/**
	 * Number of time slots.
	 */
	private int numberOfIndexedTimeSlots;
	
	/**
	 * Reset for new replaying.
	 */
	@Override
	public void newGame() {
		
		this.mapBrokerArrayAssign = new HashMap<String, Integer>();
		this.mapCustomerArrayAssign = new HashMap<Long, Integer>();
		this.tariffTransactionRepo = new LinkedList<TariffTransaction>();
		this.tariffSpecificationRepo = new LinkedList<TariffSpecification>();
		this.tsUpdateRepo = new LinkedList<TimeslotUpdate>();
		this.listRatesForTariffSpecification = new ArrayList<RateCore>();
		this.allTariffDataTransactionsData = new HashMap<Long, TariffDataAttributes>();
		this.weatherReportRepo = new LinkedList<WeatherReport>();
		this.weatherForecastRepo = new LinkedList<WeatherForecast>();
		this.isSimStart = false;
		this.isSimEnd = false;
		this.timeslotComplete = false;
		this.appearanceListBean.resetAvailableList();;
		this.numberOfIndexedTimeSlots = 0;
		this.counterTimeServicesMsg = 0;
	}
	
	/**
	 * Processes the read lines. Register all classes which should 
	 * receive the log entries.
	 */
	@Override
	public void registerListenerObjects() {
		
		domainObjectReader.registerNewObjectListenerReplayer(new 
				NewObjectListenerReplayer() {

            @Override
            public void handleNewObject (Object thing) throws Exception {

            	isSimStart = true;
            	
            	/*
            	 * Save message. 
            	 */
            	simStart = (SimStart) thing;
            	
            	/*
            	 * Current number of time slots.
            	 */
            	numberOfTimeslots = startNumberTimeslot - 1;
            	
            	/*
            	 * Ascertain the length for arrays.
            	 */
            	int index = competition.getExpectedTimeslotCount() + 
            			diffLastFirstEnabled;
            	
            	numberOfIndexedTimeSlots = competition
            			.getExpectedTimeslotCount();
            	            	
            	/*
            	 * Create all datastructure.
            	 */
        		weatherReportsData = new WeatherReport[index];
        		weatherForecastsData = new WeatherForecast[index];
        		positionWeatherReportData = 0;
        		positionWeatherForecastData = 0;
        		
            	List<String> brokerNames = competition.getBrokers();
            	brokerAppearances = new Appearance[brokerNames.size()];
            	
            	int indexArray = 0;
            	for (String brokerName : brokerNames) {
            		
            		brokerAppearances[indexArray] = appearanceListBean
            				.getAppereance();
            		mapBrokerArrayAssign.put(brokerName, indexArray);
            		indexArray++;           		
            	}
 
            	List<CustomerInfo> customers = competition.getCustomers();
            	indexArray = 0;
            	
            	customersInfo = new CustomerInfo[customers.size()];
            	
            	for (CustomerInfo customer : customers) {
            		
            		customersInfo[indexArray] = customer;
            		mapCustomerArrayAssign.put(customer.getId(), indexArray);
            		indexArray++;  
            	}
            	
            	listCustomerTariffTransaction = new LinkedList[customers.size()];
            	
            	for (int i = 0; i < customers.size(); i++) {
            		
            		listCustomerTariffTransaction[i] = 
            				new LinkedList<TariffTransaction>();
            	}
            	
        		balancingsData = new DynamicData[index * brokerNames.size()];
        		lastBalancingsData = new DynamicData[brokerNames.size()];
        		
        		distributionsData = new DynamicData[index * brokerNames.size()];
        		lastDistributionsData = new DynamicData[brokerNames.size()];
        		
        		marketTransactionsData = new DynamicData[index * brokerNames.size()];
        		lastMarketTransactionsData = new DynamicData[brokerNames.size()];
        		
        		allMarketTransactionsData = new LinkedList[index * brokerNames.size()];
        		
        		orderTransactionsData = new LinkedList[index * brokerNames.size()];
        		
        		financeDynamicsData = new FinanceDynamicData[index * brokerNames.size()]; 
        		financeDynamicsDataProfit = new Double[brokerNames.size()];
        		
        		tariffTransactionsData = new DynamicData[index * brokerNames.size()];
        		lastTariffTransactionsData = new DynamicData[brokerNames.size()];
        		
        		tariffDynamicsData = new TariffDynamicData[index * brokerNames.size()];
        		
        		tariffCategoryAttributesData = new 
        				TariffCategoryAttributes[index * brokerNames.size()];
                wholesaleCategoryAttributesData = new 
                		WholesaleCategoryAttributes[index * brokerNames.size()];
               	financeCategoryAttributesData = new 
               			FinanceCategoryAttributes[index * brokerNames.size()];
               	balancingCategoryAttributesData = new 
               			BalancingCategoryAttributes[index * brokerNames.size()];
             
        		lastTariffCategoryAttribute = new 
        				TariffCategoryAttributes[brokerNames.size()];
        		lastWholesaleCategoryAttribute = new 
        				WholesaleCategoryAttributes[brokerNames.size()];
        		lastFinanceCategoryAttribute = new 
        				FinanceCategoryAttributes[brokerNames.size()];
        		
        		int numberOfBroker = brokerNames.size();
        		
        		// Init-data for first time slot.
        		for (int i = 0; i < numberOfBroker; i++) {
        			
        			lastBalancingsData[i] = new DynamicData(0.0, 0.0, 0.0, 0.0, 0);
        			lastDistributionsData[i] = new DynamicData(0.0, 0.0, 0.0, 0.0, 0);
        			lastMarketTransactionsData[i] = new DynamicData(0.0, 0.0, 0.0, 0.0, 0);
        			lastTariffTransactionsData[i] = new DynamicData(0.0, 0.0, 0.0, 0.0, 0);
        			financeDynamicsDataProfit[i] = 0.0;
        			lastTariffCategoryAttribute[i] = new TariffCategoryAttributes();
        			lastWholesaleCategoryAttribute[i] = new WholesaleCategoryAttributes();
        			lastFinanceCategoryAttribute[i] = new FinanceCategoryAttributes();
        		}
        		
        		clearedTradesData = new LinkedList[index];
        		
        		tariffDataTransactionsData = new LinkedList[index];
        		
        		gradingAttributesData = new GradingAttributes[index];
        		lastGradingAttribute = new GradingAttributes();
        		
               	visualizerBeanAttributesData = new VisualizerBeanAttributes[index];
        		lastVisualizerBeanAttribute = new VisualizerBeanAttributes(competition);
        		
        		/*
        		 * Send all data before Sim Start.
        		 */
        		Iterator<WeatherReport> iteratorWeatherReport = weatherReportRepo.iterator();
        		while (iteratorWeatherReport.hasNext()) {
        			
        			domainObjectReader.fireNewObjectEventReplayer(iteratorWeatherReport.next());
        		}
        		
        		Iterator<WeatherForecast> iteratorWeatherForecast = weatherForecastRepo.iterator();
        		while (iteratorWeatherForecast.hasNext()) {
        			
        			domainObjectReader.fireNewObjectEventReplayer(iteratorWeatherForecast.next());
        		}
        		
        		Iterator<TariffSpecification> iteratorSpec = tariffSpecificationRepo.iterator();
        		while (iteratorSpec.hasNext()) {
        			
        			domainObjectReader.fireNewObjectEventReplayer(iteratorSpec.next());
        		}
        		
        		Iterator<TariffTransaction> iterator = tariffTransactionRepo.iterator();
        		while (iterator.hasNext()) {
        			
        			domainObjectReader.fireNewObjectEventReplayer(iterator.next());
        		}
 		
        		Iterator<TimeslotUpdate> iteratorTsUpdate = tsUpdateRepo.iterator();
        		while (iteratorTsUpdate.hasNext()) {
        			
        			domainObjectReader.fireNewObjectEventReplayer(iteratorTsUpdate.next());
        		}
            }
        }, SimStart.class);
		
		domainObjectReader.registerNewObjectListenerReplayer(new NewObjectListenerReplayer() {
			
            @Override
            public void handleNewObject (Object thing) throws Exception {

            	isSimEnd = true;
            	
            	// Save message
            	simEnd = (SimEnd) thing;  	
            	
            	// Update progress bar.
    			logParametersBean.setProgress(100);
    			logParametersBean.setLastLoadedTimeslotNumber(numberOfTimeslots);
				pushServiceReplayerNew.push("/updateProgressBar", 
						String.valueOf(100));
            }
        }, SimEnd.class);
		
		domainObjectReader.registerNewObjectListenerReplayer(new NewObjectListenerReplayer() {
			
            @Override
            public void handleNewObject (Object thing) throws Exception {
            	            	
            	WeatherReport report = (WeatherReport) thing;

				if (!isSimStart) {

					// Message receive before Sim Start. Collect it.
					weatherReportRepo.add(report);
				} else {
										
					// Check whether the array is too small
					if (positionWeatherReportData >= weatherReportsData.length) {

						// Resize Array
						WeatherReport[] tmpWeatherReportsData = new WeatherReport[
						        weatherReportsData.length
								+ competition.getExpectedTimeslotCount()];
						System.arraycopy(weatherReportsData, 0,
								tmpWeatherReportsData, 0,
								weatherReportsData.length);
						weatherReportsData = tmpWeatherReportsData;
					}
					
					// Save message
					weatherReportsData[positionWeatherReportData] = report;
					positionWeatherReportData++;
				}
			}
        }, WeatherReport.class);
		
		domainObjectReader.registerNewObjectListenerReplayer(new NewObjectListenerReplayer() {
			
            @Override
            public void handleNewObject (Object thing) throws Exception {
            	
            	WeatherForecast forecast = (WeatherForecast) thing;
    
				if (!isSimStart) {

					// Message receive before Sim Start. Collect it.
					weatherForecastRepo.add(forecast);
				} else {
					
					// Check whether the array is too small
					if (positionWeatherForecastData >= weatherForecastsData.length) {

						// Resize Array
						WeatherForecast[] tmpWeatherForecastsData = new WeatherForecast[
						        weatherForecastsData.length 
						        + competition.getExpectedTimeslotCount()];
						System.arraycopy(weatherForecastsData, 0,
								tmpWeatherForecastsData, 0,
								weatherForecastsData.length);
						weatherForecastsData = tmpWeatherForecastsData;
					}
					
					// Save message
					weatherForecastsData[positionWeatherForecastData] = forecast;
					positionWeatherForecastData++;
				}
            }
        }, WeatherForecast.class);
		
		domainObjectReader.registerNewObjectListenerReplayer(new NewObjectListenerReplayer() {
			
            @Override
            public void handleNewObject (Object thing) throws Exception {

            	TariffSpecification tariffSpecification = 
            			(TariffSpecification) thing;

            	if (!isSimStart) {
            		
            		// Message receive before Sim Start. Collect it.
            		tariffSpecificationRepo.add(tariffSpecification);
            	} else {
            		
            		// Forward dependency. Before tariffSpecification occur a 
            		// addRate method call.
            		for (RateCore rateCore : listRatesForTariffSpecification) {
            			
            			tariffSpecification.addRate(rateCore);
            		}
       		
            		// Save message
            		// prevent invalid or double
					if (!allTariffDataTransactionsData
							.containsKey(tariffSpecification.getId())) {
  				
						allTariffDataTransactionsData.put(
								tariffSpecification.getId(),
								new TariffDataAttributes(tariffSpecification));
					}					
            	}
            }
        }, TariffSpecification.class);
		
		domainObjectReader.registerNewObjectListenerReplayer(new NewObjectListenerReplayer() {
			
            @Override
            public void handleNewObject (Object thing) throws Exception {

            	if (isSimStart) {
            		
            		// Save message
            		listRatesForTariffSpecification.add((Rate) thing);
            	}
            }
		}, Rate.class);
		
		domainObjectReader.registerNewObjectListenerReplayer(new NewObjectListenerReplayer() {
			
            @Override
            public void handleNewObject (Object thing) throws Exception {

				if (isSimStart) {
					
					// Save message
					listRatesForTariffSpecification.add((RegulationRate) thing);
				}
            }
		}, RegulationRate.class);
		
		domainObjectReader.registerNewObjectListenerReplayer(new NewObjectListenerReplayer() {
			
            @Override
            public void handleNewObject (Object thing) throws Exception {
            	
            	// Remove all Rates.
            	listRatesForTariffSpecification.removeAll(listRatesForTariffSpecification);
            }
        }, TariffStatus.class);
		
		domainObjectReader.registerNewObjectListenerReplayer(new NewObjectListenerReplayer() {
			
            @Override
            public void handleNewObject (Object thing) throws Exception {

				TariffTransaction tariffTransaction = (TariffTransaction) thing;

				if (!isSimStart) {
					
					// Message receive before Sim Start. Collect it.
					tariffTransactionRepo.add(tariffTransaction);
					return;
				}
						
				// Save messages only from Broker which participate at the game.
				if (mapBrokerArrayAssign.containsKey(tariffTransaction
						.getBroker().getUsername())) {
					
					// Calculate index position for array
					int posIndex = mapBrokerArrayAssign.get(tariffTransaction
							.getBroker().getUsername());
					int tsMessage = tariffTransaction.getPostedTimeslotIndex()
							- startNumberTimeslot;
					int numberOfBrokerNames = mapBrokerArrayAssign.size();
					int position = posIndex + numberOfBrokerNames * tsMessage;
					
					// Save values for grading and tariffCategory attributes
				    lastGradingAttribute.addCharge(Math.abs(tariffTransaction.getCharge()));

				    if (tariffTransaction.getTxType() == Type.SIGNUP) {
				    	
				    	lastTariffCategoryAttribute[posIndex].addCustomers(tariffTransaction.getCustomerCount());
				    }
	
				    if (tariffTransaction.getTxType() == Type.CONSUME) {
				    	
				    	lastGradingAttribute.addSoldEnergyTariffMarket(Math.abs(tariffTransaction.getKWh()));
				    	lastTariffCategoryAttribute[posIndex].addConsumptionConsumers(tariffTransaction.getCustomerCount());
				    	lastTariffCategoryAttribute[posIndex].addSoldEnergy(Math.abs(tariffTransaction.getKWh()));
				    	lastTariffCategoryAttribute[posIndex].addMoneyFromSold(tariffTransaction.getCharge());
				    }
	
				    if (tariffTransaction.getTxType() == Type.PRODUCE) {
				    	
				    	lastGradingAttribute.addBoughtEnergyTariffMarket(tariffTransaction.getKWh());
				    	lastTariffCategoryAttribute[posIndex].addBoughtEnergy(tariffTransaction.getKWh());
				    }
				      
				    lastTariffCategoryAttribute[posIndex].addCharge(Math.abs(tariffTransaction.getCharge()));

				    // Save Tariff Transaction and Tariff Dynamic Data
					if (tariffTransactionsData[position] == null) {
						
						// Copy last Dynamic Data.
						DynamicData dd = lastTariffTransactionsData[posIndex];		
						tariffTransactionsData[position] = new DynamicData(
								tariffTransaction.getPostedTimeslotIndex(), dd
										.getEnergy(), dd.getProfit());		
					}

					tariffTransactionsData[position].update(
							tariffTransaction.getKWh(),
							tariffTransaction.getCharge());

					if (tariffDynamicsData[position] == null) {

						DynamicData dd = lastTariffTransactionsData[posIndex];
						TariffDynamicData tdd = new TariffDynamicData(
								tariffTransaction.getPostedTimeslotIndex(), dd
										.getProfit(), dd.getEnergy(), 
										lastTariffCategoryAttribute[posIndex].getCustomerCount());
						tariffDynamicsData[position] = tdd;
					}

				    lastTariffCategoryAttribute[posIndex].addCustomerCount(HelperReplayer
								.getCustomerCount(tariffTransaction));
				    
					tariffDynamicsData[position].update(tariffTransaction
							.getCharge(), tariffTransaction.getKWh(),
							HelperReplayer.getCustomerCount(tariffTransaction));
					
					lastTariffTransactionsData[posIndex]
							.setEnergy(lastTariffTransactionsData[posIndex]
									.getEnergy() + tariffTransaction.getKWh());
					lastTariffTransactionsData[posIndex]
							.setProfit(lastTariffTransactionsData[posIndex].getProfit() 
									+ tariffTransaction.getCharge());  
					
					// TariffSpec Update					
					if (tariffTransaction.getTariffSpec() != null) {
						
						long specId = tariffTransaction.getTariffSpec().getId();
						TariffDataAttributes tda = allTariffDataTransactionsData.get(specId);
						tda.setCustomers(HelperReplayer.getCustomerCount(tariffTransaction));
						tda.processTariffTx(tariffTransaction);
					}
				}
				
				// Save Tariff Transaction for Customer
				if (tariffTransaction.getCustomerInfo() != null) {
					
					Integer index = mapCustomerArrayAssign.get(tariffTransaction
							.getCustomerInfo().getId());
					
					if (index != null) {
						
						listCustomerTariffTransaction[index]
								.add(tariffTransaction);
					}
				}
            }
        }, TariffTransaction.class);
		
		domainObjectReader.registerNewObjectListenerReplayer(new NewObjectListenerReplayer() {
			
            @Override
            public void handleNewObject (Object thing) throws Exception {   
            	
            	// Save message.
            	competition = (Competition) thing;
            }
        }, Competition.class);
		
		domainObjectReader.registerNewObjectListenerReplayer(new NewObjectListenerReplayer() {
			
            @Override
            public void handleNewObject (Object thing) throws Exception {
            	
            	timeslotComplete = false;
            	
            	TimeslotUpdate tsUpdate = (TimeslotUpdate) thing;
            	
            	if (isSimStart) {
            		
            		// Update visualizerBeanAttributes.
            		if (lastVisualizerBeanAttribute.getTimeslotUpdate() != null) {
            			
            			lastVisualizerBeanAttribute.setOldTimeslotUpdate(lastVisualizerBeanAttribute
            					.getTimeslotUpdate());
            		}

            		lastVisualizerBeanAttribute.setTimeslotUpdate(tsUpdate);

            		lastVisualizerBeanAttribute.setCurrentMillis(tsUpdate.getPostedTime()
            				.getMillis());

            		int timeslotSerialNumber = tsUpdate.getFirstEnabled()
            				- competition.getDeactivateTimeslotsAhead();
            		lastVisualizerBeanAttribute.setCurrentTimeslotSerialNumber(timeslotSerialNumber);
            	} else {
            		
            		// Before Sim Start is a TimeslotUpdate message.
            		startNumberTimeslot = tsUpdate.getFirstEnabled() - 1;      		
        
            		logParametersBean.setTimeslot(startNumberTimeslot);
            		
            		// timeslots open for sending.
            		diffLastFirstEnabled = tsUpdate.getLastEnabled() 
            				- tsUpdate.getFirstEnabled() + 1;
            		
            		// Collect message.
            		tsUpdateRepo.add((TimeslotUpdate) thing);
            	}
            }
        }, TimeslotUpdate.class);
		
		domainObjectReader.registerNewObjectListenerReplayer(new NewObjectListenerReplayer() {
			
            @Override
            public void handleNewObject (Object thing) throws Exception {
            	
            	timeslotComplete = true;

            	if (isSimStart) {
        		
            		// Ignore first TimeService message after SimStart.
            		if (counterTimeServicesMsg > 0) {

            			numberOfTimeslots++;

            			// Check if the array length is too small
            			// In competition.ExpectedTimeSlotCount could be a wrong value.
            			if (numberOfTimeslots - startNumberTimeslot >= numberOfIndexedTimeSlots - 1) {

            				// Resize array structure
            				numberOfIndexedTimeSlots += competition.getExpectedTimeslotCount();
            				resizeAllDatastructure(numberOfIndexedTimeSlots + diffLastFirstEnabled);
            			}
            			
            			// Value for ProgressBar.
						double calculate = ((numberOfTimeslots - startNumberTimeslot) * 100)
								/ competition.getExpectedTimeslotCount();
						int newProgessValue = (int) Math.round(calculate);

						if (logParametersBean.getProgress() != newProgessValue) {	
							
							logParametersBean.setProgress(newProgessValue);
							logParametersBean.setLastLoadedTimeslotNumber(numberOfTimeslots);
							pushServiceReplayerNew.push("/updateProgressBar", 
									String.valueOf(newProgessValue));
						}
						
						// Update attributes data
                		gradingAttributesData[numberOfTimeslots - startNumberTimeslot] = 
                				new GradingAttributes(lastGradingAttribute);
                				
                		lastVisualizerBeanAttribute.setTimeslotComplete(new 
                				TimeslotComplete(numberOfTimeslots));
                		
                   		visualizerBeanAttributesData[numberOfTimeslots - startNumberTimeslot] =
                   				new VisualizerBeanAttributes(lastVisualizerBeanAttribute);
                        
                   		// Update tariffData.
                   		tariffDataTransactionsData[numberOfTimeslots - startNumberTimeslot] 
                   				= new LinkedList<TariffDataAttributes>();
                   		Collection<TariffDataAttributes> listTariffDataAttribute =
                   				allTariffDataTransactionsData.values();
                   		Iterator<TariffDataAttributes> iteratorTariffDataAttributes = 
                   				listTariffDataAttribute.iterator();
                   		
                   		while (iteratorTariffDataAttributes.hasNext()) {
                   			
                   			tariffDataTransactionsData[numberOfTimeslots - startNumberTimeslot].add(
                   					new TariffDataAttributes(iteratorTariffDataAttributes
                   							.next()));
                   		}
                		
            			int safetyTxIndex = numberOfTimeslots - startNumberTimeslot;    			
            			int numberOfBrokerNames = mapBrokerArrayAssign.size();
            			int position;
            			
            			// Update all Broker arrays.
            			for (int posIndex = 0; posIndex < mapBrokerArrayAssign.size(); posIndex++) {
            				
            				// calculate index position for array
            				position = posIndex + numberOfBrokerNames * safetyTxIndex;

            				// Save marketTransactionsData.
                		    if (position - numberOfBrokerNames >= 0 && 
                		    		marketTransactionsData[position - numberOfBrokerNames] != null) {
                		    	
                		    	DynamicData dd = lastMarketTransactionsData[posIndex];   		    	
                		    	marketTransactionsData[position- numberOfBrokerNames].setProfit(dd.getProfit());
                		    	marketTransactionsData[position- numberOfBrokerNames].setEnergy(dd.getEnergy());
    
                		        lastMarketTransactionsData[posIndex].setEnergy(
                		        		marketTransactionsData[position- numberOfBrokerNames].getEnergy() + 
                		        		marketTransactionsData[position- numberOfBrokerNames].getEnergyDelta());
                		        lastMarketTransactionsData[posIndex].setProfit(
                		        		marketTransactionsData[position- numberOfBrokerNames].getProfit() + 
                		        		marketTransactionsData[position- numberOfBrokerNames].getProfitDelta());
                		    }
         
                		    // Update attributes data for all broker.
                		    wholesaleCategoryAttributesData[position] = 
                		    		new WholesaleCategoryAttributes(lastWholesaleCategoryAttribute[posIndex]);
                		    tariffCategoryAttributesData[position] = 
                    				new TariffCategoryAttributes(lastTariffCategoryAttribute[posIndex]);
                		    financeCategoryAttributesData[position] = 
                    				new FinanceCategoryAttributes(lastFinanceCategoryAttribute[posIndex]
                    						.getProfit());
                		    balancingCategoryAttributesData[position] = 
                    				new BalancingCategoryAttributes(lastBalancingsData[posIndex].getEnergy(),
                    						lastBalancingsData[posIndex].getProfit());
            			}            			
            		}
            		
            		counterTimeServicesMsg++;
            	}
            }
        }, TimeService.class);
		
		domainObjectReader.registerNewObjectListenerReplayer(new NewObjectListenerReplayer() {
			
            @Override
            public void handleNewObject (Object thing) throws Exception {
            	
            	DistributionTransaction distribution = 
            			(DistributionTransaction) thing;
            	
            	// calculate index position for array.
            	int posIndex = mapBrokerArrayAssign.get(distribution.getBroker().getUsername());
            	int tsMessage = distribution.getPostedTimeslotIndex() - startNumberTimeslot;
            	int numberOfBrokerNames = mapBrokerArrayAssign.size();
            	int position = posIndex + numberOfBrokerNames * tsMessage;
            	
            	// Save distribution data
            	if (distributionsData[position] == null) {
            		
            		// Copy last Dynamic Data
            		DynamicData dd = lastDistributionsData[posIndex];
            		distributionsData[position] = new DynamicData(distribution.getPostedTimeslotIndex(),
            				dd.getEnergy(), dd.getProfit());
            	}
            	
            	distributionsData[position].update(distribution.getKWh(), distribution.getCharge());
            	lastDistributionsData[posIndex].setEnergy(lastDistributionsData[posIndex].getEnergy() 
            			+ distribution.getKWh());
            	lastDistributionsData[posIndex].setProfit(lastDistributionsData[posIndex].getProfit() 
            			+ distribution.getCharge());
            
            	// Update grading attribute
            	lastGradingAttribute.addEnergyDistribution(Math.abs(distribution.getKWh()));
            }
        }, DistributionTransaction.class);
		
		domainObjectReader.registerNewObjectListenerReplayer(new NewObjectListenerReplayer() {
						
            @Override
            public void handleNewObject (Object thing) throws Exception {
            	
            	BalancingTransaction balancing = 
            			(BalancingTransaction) thing;
            	
            	// calculate index position for array.
            	int posIndex = mapBrokerArrayAssign.get(balancing.getBroker().getUsername());
            	int tsMessage = balancing.getPostedTimeslotIndex() - startNumberTimeslot;
            	int numberOfBrokerNames = mapBrokerArrayAssign.size();
            	int position = posIndex + numberOfBrokerNames * tsMessage;
            	
            	// Save balancing data
            	if (balancingsData[position] == null) {
            		
            		// Copy last Dynamic Data
            		DynamicData dd = lastBalancingsData[posIndex];
            		balancingsData[position] = new DynamicData(balancing.getPostedTimeslotIndex(),
            				dd.getEnergy(), dd.getProfit());
            	}
            	
            	balancingsData[position].update(balancing.getKWh(), balancing.getCharge());
            	lastBalancingsData[posIndex].setEnergy(lastBalancingsData[posIndex].getEnergy() 
            			+ balancing.getKWh());
            	lastBalancingsData[posIndex].setProfit(lastBalancingsData[posIndex].getProfit() 
            			+ balancing.getCharge());
            }
        }, BalancingTransaction.class);
		
		domainObjectReader.registerNewObjectListenerReplayer(new NewObjectListenerReplayer() {
			
            @Override
            public void handleNewObject (Object thing) throws Exception {

            	CashPosition cashPosition = 
            			(CashPosition) thing;
            	
            	// Save messages only from Broker which participate at the game.
				if (mapBrokerArrayAssign.containsKey(cashPosition
						.getBroker().getUsername())) {

					int posIndex = mapBrokerArrayAssign.get(cashPosition
							.getBroker().getUsername());
					
					// calculate index position for array.
					int tsMessage = cashPosition.getPostedTimeslotIndex()
							- startNumberTimeslot;
					int numberOfBrokerNames = mapBrokerArrayAssign.size();
					int position = posIndex + numberOfBrokerNames * tsMessage;

					// Save finance data
					if (financeDynamicsData[position] == null) {
						
						FinanceDynamicData fdd = new FinanceDynamicData(
								financeDynamicsDataProfit[posIndex],
								cashPosition.getPostedTimeslotIndex());
						financeDynamicsData[position] = fdd;
					}

					financeDynamicsData[position].updateProfit(cashPosition
							.getBalance());
					financeDynamicsDataProfit[posIndex] = cashPosition
							.getBalance();
					lastFinanceCategoryAttribute[posIndex].setProfit(cashPosition
							.getBalance());
            	}
            }
        }, CashPosition.class);

		domainObjectReader.registerNewObjectListenerReplayer(new NewObjectListenerReplayer() {

            @Override
            public void handleNewObject (Object thing) throws Exception {
            	
            	MarketTransaction marketTransaction = 
            			(MarketTransaction) thing;

            	// Save messages only from Broker which participate at the game.
				if (mapBrokerArrayAssign.containsKey(marketTransaction
						.getBroker().getUsername())) {

					// calculate index position for array.
					int posIndex = mapBrokerArrayAssign.get(marketTransaction
							.getBroker().getUsername());
					int tsMessage = marketTransaction.getTimeslotIndex()
							- startNumberTimeslot;
					int numberOfBrokerNames = mapBrokerArrayAssign.size();
					int position = posIndex + numberOfBrokerNames * tsMessage;

					// Save marketTransaction data
					if (marketTransactionsData[position] == null) {
						
						marketTransactionsData[position] = new DynamicData(
								marketTransaction.getTimeslotIndex(), 0,
								0);
					}

					marketTransactionsData[position].update(
							marketTransaction.getMWh(),
							marketTransaction.getPrice()
									* Math.abs(marketTransaction.getMWh()));
					
					if (allMarketTransactionsData[position] == null) {
						
						allMarketTransactionsData[position] = new LinkedList<MarketTransaction>();
					}

					allMarketTransactionsData[position].add(marketTransaction);

					// Update attributes for Broker
				    if (marketTransaction.getMWh() > 0) {
				    	
				        // positive value:
				       	lastWholesaleCategoryAttribute[posIndex].addEnergyBought(marketTransaction.getMWh());
				       	  
				       	// negative value:
				        lastWholesaleCategoryAttribute[posIndex].addCostFromBuying(-1.0f*Math.abs(marketTransaction
				        		.getPrice()* marketTransaction.getMWh()));
				           
				        // positive value:
				    	lastGradingAttribute.addBoughtEnergyWholesaleMarket(marketTransaction.getMWh());
				           
				    	// negative value
				    	lastGradingAttribute.addMoneyFromBuyingWholesaleMarket(-1.0f*Math.abs(marketTransaction
				    			.getPrice()* marketTransaction.getMWh()));
				    } else {
				    	
				        //negative value:
				       	lastWholesaleCategoryAttribute[posIndex].addEnergySold(marketTransaction.getMWh());
				           
				       	// positive value:
				        lastWholesaleCategoryAttribute[posIndex].addRevenueFromSelling(Math.abs(marketTransaction
				        		.getPrice() * marketTransaction.getMWh()));
				          
				        //negative value
				        lastGradingAttribute.addSoldEnergyWholesaleMarket(marketTransaction.getMWh());
				           
				        //positive value:
				        lastGradingAttribute.addMoneyFromSellingWholesaleMarket(Math.abs(marketTransaction
				        		.getPrice() * marketTransaction.getMWh()));
				     }
				}
            }
        }, MarketTransaction.class);
		 
		domainObjectReader.registerNewObjectListenerReplayer(new NewObjectListenerReplayer() {
			
			@Override
			public void handleNewObject(Object thing) {
				
				Order order = (Order) thing;

				// Save messages only from Broker which participate at the game.
				if (mapBrokerArrayAssign.containsKey(order.getBroker()
						.getUsername())) {

					// calculate index position for array.
					int posIndex = mapBrokerArrayAssign.get(order.getBroker()
							.getUsername());
					int tsMessage = order.getTimeslotIndex()
							- startNumberTimeslot;
					int numberOfBrokerNames = mapBrokerArrayAssign.size();
					int position = posIndex + numberOfBrokerNames * tsMessage;

					// Save order data
					if (orderTransactionsData[position] == null) {
						
						orderTransactionsData[position] = new LinkedList<Order>();
					}

					orderTransactionsData[position].add(order);
				}
			}
		}, Order.class);
		
		domainObjectReader.registerNewObjectListenerReplayer(new NewObjectListenerReplayer() {
			
			@Override
			public void handleNewObject(Object thing) {

				ClearedTrade clearedTrade = (ClearedTrade) thing;
				
				// calculate index position for array.
				int tsMessage = clearedTrade.getTimeslotIndex()
						- startNumberTimeslot;
				
				// Save clearedTrade data
				if (clearedTradesData[tsMessage] == null) {

					clearedTradesData[tsMessage] 
							= new LinkedList<ClearedTradeObject>();
				}

				TimeslotUpdate old = lastVisualizerBeanAttribute
						.getOldTimeslotUpdate();

				if (old != null) {
					
					clearedTradesData[tsMessage].add(new ClearedTradeObject(old
							.getPostedTime().getMillis(), clearedTrade));
				}
			}
        }, ClearedTrade.class);
	}
	
	/**
	 * Resizes all data structure arrays.
	 * 
	 * @param newLength The new length
	 */
	public void resizeAllDatastructure(int newLength) {

		int numberOfBroker = competition.getBrokers().size();

		// CustomerInfo array not resize, because all CustomerInfo message are
		// received before Sim Start

		DynamicData[] tmpBalancingsData = new DynamicData[newLength
				* numberOfBroker];
		System.arraycopy(balancingsData, 0, tmpBalancingsData, 0,
				balancingsData.length);
		balancingsData = tmpBalancingsData;

		DynamicData[] tmpDistributionsData = new DynamicData[newLength
				* numberOfBroker];
		System.arraycopy(distributionsData, 0, tmpDistributionsData, 0,
				distributionsData.length);
		distributionsData = tmpDistributionsData;

		DynamicData[] tmpMarketTransactionsData = new DynamicData[newLength
				* numberOfBroker];
		System.arraycopy(marketTransactionsData, 0, tmpMarketTransactionsData,
				0, marketTransactionsData.length);
		marketTransactionsData = tmpMarketTransactionsData;

		List<MarketTransaction>[] tmpAllMarketTransactionsData = 
				new LinkedList[newLength * numberOfBroker];
		System.arraycopy(allMarketTransactionsData, 0,
				tmpAllMarketTransactionsData, 0,
				allMarketTransactionsData.length);
		allMarketTransactionsData = tmpAllMarketTransactionsData;

		List<Order>[] tmpOrderTransactionsData = 
				new LinkedList[newLength * numberOfBroker];
		System.arraycopy(orderTransactionsData, 0, tmpOrderTransactionsData, 0,
				orderTransactionsData.length);
		orderTransactionsData = tmpOrderTransactionsData;

		FinanceDynamicData[] tmpFinanceDynamicsData = 
				new FinanceDynamicData[newLength * numberOfBroker];
		System.arraycopy(financeDynamicsData, 0, tmpFinanceDynamicsData, 0,
				financeDynamicsData.length);
		financeDynamicsData = tmpFinanceDynamicsData;

		DynamicData[] tmpTariffTransactionsData = new DynamicData[newLength
				* numberOfBroker];
		System.arraycopy(tariffTransactionsData, 0, tmpTariffTransactionsData,
				0, tariffTransactionsData.length);
		tariffTransactionsData = tmpTariffTransactionsData;

		TariffDynamicData[] tmpTariffDynamicsData = 
				new TariffDynamicData[newLength * numberOfBroker];
		System.arraycopy(tariffDynamicsData, 0, tmpTariffDynamicsData, 0,
				tariffDynamicsData.length);
		tariffDynamicsData = tmpTariffDynamicsData;

		TariffCategoryAttributes[] tmpTariffCategoryAttributesData = 
				new TariffCategoryAttributes[newLength * numberOfBroker];
		System.arraycopy(tariffCategoryAttributesData, 0,
				tmpTariffCategoryAttributesData, 0,
				tariffCategoryAttributesData.length);
		tariffCategoryAttributesData = tmpTariffCategoryAttributesData;

		WholesaleCategoryAttributes[] tmpWholesaleCategoryAttributesData = 
				new WholesaleCategoryAttributes[newLength * numberOfBroker];
		System.arraycopy(wholesaleCategoryAttributesData, 0,
				tmpWholesaleCategoryAttributesData, 0,
				wholesaleCategoryAttributesData.length);
		wholesaleCategoryAttributesData = tmpWholesaleCategoryAttributesData;

		FinanceCategoryAttributes[] tmpFinanceCategoryAttributesData = 
				new FinanceCategoryAttributes[newLength * numberOfBroker];
		System.arraycopy(financeCategoryAttributesData, 0,
				tmpFinanceCategoryAttributesData, 0,
				financeCategoryAttributesData.length);
		financeCategoryAttributesData = tmpFinanceCategoryAttributesData;

		BalancingCategoryAttributes[] tmpBalancingCategoryAttributesData = 
				new BalancingCategoryAttributes[newLength * numberOfBroker];
		System.arraycopy(balancingCategoryAttributesData, 0,
				tmpBalancingCategoryAttributesData, 0,
				balancingCategoryAttributesData.length);
		balancingCategoryAttributesData = tmpBalancingCategoryAttributesData;

		List<ClearedTradeObject>[] tmpClearedTradesData = 
				new LinkedList[newLength];
		System.arraycopy(clearedTradesData, 0, tmpClearedTradesData, 0,
				clearedTradesData.length);
		clearedTradesData = tmpClearedTradesData;

		List<TariffDataAttributes>[] tmpTariffDataTransactionsData = 
				new LinkedList[newLength];
		System.arraycopy(tariffDataTransactionsData, 0,
				tmpTariffDataTransactionsData, 0,
				tariffDataTransactionsData.length);
		tariffDataTransactionsData = tmpTariffDataTransactionsData;

		GradingAttributes[] tmpGradingAttributesData = 
				new GradingAttributes[newLength];
		System.arraycopy(gradingAttributesData, 0, tmpGradingAttributesData, 0,
				gradingAttributesData.length);
		gradingAttributesData = tmpGradingAttributesData;

		VisualizerBeanAttributes[] tmpVisualizerBeanAttributesData = 
				new VisualizerBeanAttributes[newLength];
		System.arraycopy(visualizerBeanAttributesData, 0,
				tmpVisualizerBeanAttributesData, 0,
				visualizerBeanAttributesData.length);
		visualizerBeanAttributesData = tmpVisualizerBeanAttributesData;
	}
	
	/**
	 * Read all messages from log file and send it.
	 */
	public void readAllObjects() throws ErrorReadDomainObject {
	
		String line = null;
		
		try {

			while ((line = logDataSource.readNextLineFromLog()) != null) {
				domainObjectReader.readObject(line);
			}
		} catch (IOException e) {

			throw new ErrorReadDomainObject("IOException: " + e.getMessage());
		} catch (Exception e) {
			
			throw new ErrorReadDomainObject(line, e.getMessage());
		}
	}
	
	/** getter and setter methods **/
	public int getNumberOfTimeslots() {
		return numberOfTimeslots;
	}

	public int getEndNumberTimeslot() {
		return numberOfTimeslots;
	}
	
	public DynamicData[] getBalancingsData() {
		return balancingsData;
	}

	public void setBalancingsData(DynamicData[] balancingsData) {
		this.balancingsData = balancingsData;
	}
	
	public DynamicData[] getDistributionsData() {
		return distributionsData;
	}

	public void setDistributionsData(DynamicData[] distributionsData) {
		this.distributionsData = distributionsData;
	}

	public DynamicData[] getMarketTransactionsData() {
		return marketTransactionsData;
	}

	public void setMarketTransactionsData(DynamicData[] marketTransactionsData) {
		this.marketTransactionsData = marketTransactionsData;
	}

	public List<MarketTransaction>[] getAllMarketTransactionsData() {
		return allMarketTransactionsData;
	}

	public void setAllMarketTransactionsData(
			List<MarketTransaction>[] allMarketTransactionsData) {
		this.allMarketTransactionsData = allMarketTransactionsData;
	}

	public List<Order>[] getOrderTransactionsData() {
		return orderTransactionsData;
	}

	public void setOrderTransactionsData(List<Order>[] orderTransactionsData) {
		this.orderTransactionsData = orderTransactionsData;
	}

	public FinanceDynamicData[] getFinanceDynamicsData() {
		return financeDynamicsData;
	}

	public void setFinanceDynamicsData(FinanceDynamicData[] financeDynamicsData) {
		this.financeDynamicsData = financeDynamicsData;
	}

	public DynamicData[] getTariffTransactionsData() {
		return tariffTransactionsData;
	}

	public void setTariffTransactionsData(DynamicData[] tariffTransactionsData) {
		this.tariffTransactionsData = tariffTransactionsData;
	}

	public TariffDynamicData[] getTariffDynamicsData() {
		return tariffDynamicsData;
	}

	public void setTariffDynamicsData(TariffDynamicData[] tariffDynamicsData) {
		this.tariffDynamicsData = tariffDynamicsData;
	}

	public TariffCategoryAttributes[] getTariffCategoryAttributesData() {
		return tariffCategoryAttributesData;
	}

	public void setTariffCategoryAttributesData(
			TariffCategoryAttributes[] tariffCategoryAttributesData) {
		this.tariffCategoryAttributesData = tariffCategoryAttributesData;
	}

	public GradingAttributes[] getGradingAttributesData() {
		return gradingAttributesData;
	}

	public void setGradingAttributesData(GradingAttributes[] gradingAttributesData) {
		this.gradingAttributesData = gradingAttributesData;
	}

	public WholesaleCategoryAttributes[] getWholesaleCategoryAttributesData() {
		return wholesaleCategoryAttributesData;
	}

	public void setWholesaleCategoryAttributesData(
			WholesaleCategoryAttributes[] wholesaleCategoryAttributesData) {
		this.wholesaleCategoryAttributesData = wholesaleCategoryAttributesData;
	}

	public FinanceCategoryAttributes[] getFinanceCategoryAttributesData() {
		return financeCategoryAttributesData;
	}

	public void setFinanceCategoryAttributesData(
			FinanceCategoryAttributes[] financeCategoryAttributesData) {
		this.financeCategoryAttributesData = financeCategoryAttributesData;
	}

	public VisualizerBeanAttributes[] getVisualizerBeanAttributesData() {
		return visualizerBeanAttributesData;
	}

	public void setVisualizerBeanAttributesData(
			VisualizerBeanAttributes[] visualizerBeanAttributesData) {
		this.visualizerBeanAttributesData = visualizerBeanAttributesData;
	}

	public List<ClearedTradeObject>[] getClearedTradesData() {
		return clearedTradesData;
	}

	public void setClearedTradesData(List<ClearedTradeObject>[] clearedTradesData) {
		this.clearedTradesData = clearedTradesData;
	}

	public Map<Long, TariffDataAttributes> getAllTariffDataTransactionsData() {
		return allTariffDataTransactionsData;
	}

	public void setAllTariffDataTransactionsData(
			Map<Long, TariffDataAttributes> allTariffDataTransactionsData) {
		this.allTariffDataTransactionsData = allTariffDataTransactionsData;
	}

	public List<TariffDataAttributes>[] getTariffDataTransactionsData() {
		return tariffDataTransactionsData;
	}

	public void setTariffDataTransactionsData(
			List<TariffDataAttributes>[] tariffDataTransactionsData) {
		this.tariffDataTransactionsData = tariffDataTransactionsData;
	}

	public Map<String, Integer> getMapBrokerArrayAssign() {
		return mapBrokerArrayAssign;
	}

	public void setMapBrokerArrayAssign(Map<String, Integer> mapBrokerArrayAssign) {
		this.mapBrokerArrayAssign = mapBrokerArrayAssign;
	}

	public WeatherReport[] getWeatherReportsData() {
		return weatherReportsData;
	}

	public void setWeatherReportsData(WeatherReport[] weatherReportsData) {
		this.weatherReportsData = weatherReportsData;
	}

	public Appearance[] getBrokerAppearances() {
		return brokerAppearances;
	}

	public void setBrokerAppearances(Appearance[] brokerAppearances) {
		this.brokerAppearances = brokerAppearances;
	}

	public Map<Long, Integer> getMapCustomerArrayAssign() {
		return mapCustomerArrayAssign;
	}

	public void setMapCustomerArrayAssign(Map<Long, Integer> mapCustomerArrayAssign) {
		this.mapCustomerArrayAssign = mapCustomerArrayAssign;
	}

	public List<TariffTransaction>[] getListCustomerTariffTransaction() {
		return listCustomerTariffTransaction;
	}

	public void setListCustomerTariffTransaction(
			List<TariffTransaction>[] listCustomerTariffTransaction) {
		this.listCustomerTariffTransaction = listCustomerTariffTransaction;
	}

	public CustomerInfo[] getCustomersInfo() {
		return customersInfo;
	}

	public void setCustomersInfo(CustomerInfo[] customersInfo) {
		this.customersInfo = customersInfo;
	}

	public BalancingCategoryAttributes[] getBalancingCategoryAttributesData() {
		return balancingCategoryAttributesData;
	}

	public void setBalancingCategoryAttributesData(
			BalancingCategoryAttributes[] balancingCategoryAttributesData) {
		this.balancingCategoryAttributesData = balancingCategoryAttributesData;
	}
}
