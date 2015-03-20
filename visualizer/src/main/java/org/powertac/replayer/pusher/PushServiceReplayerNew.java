package org.powertac.replayer.pusher;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.powertac.common.MarketTransaction;
import org.powertac.common.TariffTransaction;
import org.powertac.common.WeatherReport;
import org.powertac.common.TariffTransaction.Type;
import org.powertac.replayer.data.LogDataImpl;
import org.powertac.replayer.data.dto.BalancingCategoryAttributes;
import org.powertac.replayer.data.dto.ClearedTradeObject;
import org.powertac.replayer.data.dto.FinanceCategoryAttributes;
import org.powertac.replayer.data.dto.GradingAttributes;
import org.powertac.replayer.data.dto.TariffCategoryAttributes;
import org.powertac.replayer.data.dto.VisualizerBeanAttributes;
import org.powertac.replayer.data.dto.WholesaleCategoryAttributes;
import org.powertac.replayer.user.CustomerModelBeanReplayer;
import org.powertac.replayer.user.VisualizerHelperServiceReplayer;
import org.powertac.replayer.visualizer.LogParametersBean;
import org.powertac.visualizer.VisualizerApplicationContext;
import org.powertac.visualizer.domain.broker.TariffDynamicData;
import org.powertac.visualizer.push.DynDataPusher;
import org.powertac.visualizer.push.FinancePusher;
import org.powertac.visualizer.push.GlobalPusher;
import org.powertac.visualizer.push.NominationCategoryPusher;
import org.powertac.visualizer.push.NominationPusher;
import org.powertac.visualizer.push.StatisticsPusher;
import org.powertac.visualizer.push.TariffMarketPusher;
import org.powertac.visualizer.push.WeatherPusher;
import org.powertac.visualizer.push.WholesaleMarketPusher;
import org.powertac.visualizer.statistical.DynamicData;
import org.powertac.visualizer.statistical.FinanceDynamicData;
import org.primefaces.push.PushContext;
import org.primefaces.push.PushContextFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * Creates push objects for Highchart. 
 * 
 * The methods to push must have the following syntax: 
 * "push" + VIEWNAME (View name starts with upper case).
 * 
 * The channel name must have the syntax: 
 * "/" +  PLACEHOLDER_CHANNELNAME + "ExtendedChangeTimeslot";
 * 
 * @author DWietoska
 */
@Service
//@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class PushServiceReplayerNew {
	
	/**
	 * Start time slot in log file.
	 */
	private int startNumberTs;
	
	/**
	 * Access to helper methods.
	 */
	@Autowired
	private VisualizerHelperServiceReplayer helper;
	
	/**
	 * Access to dao.
	 */
	@Autowired
	private LogDataImpl logDAOImplExtended;
	
	/**
	 * Access to view bean.
	 */
	@Autowired
	private LogParametersBean logParametersBean;
	
	/**
	 * Gson.
	 */
	private Gson gson = new Gson();
	
	/**
	 * Webappp url.
	 */
	public final String WEBAPP_URI = "/visualizer/app/";
	
	/**
	 * Placeholder for channel name.
	 */
	public final String PLACEHOLDER_CHANNELNAME = "ACTION";
	
	/**
	 * Channel name for backward jump.
	 */
	public final String CHANNEL_BACKWARD = "Backward";
	
	/**
	 * Channel name for global message.
	 */
	public static final String CHANNEL_GLOBAL_MESSAGE = "globalpush";
	
	/**
	 * Channel name for slider.
	 */
	public static final String CHANNEL_SLIDER = "slider";
	
	/**
	 * Default constructor.
	 */
	public PushServiceReplayerNew() {
		
	}
	
	/**
	 * Default constructor.
	 * 
	 * @param logDAOImplExtended DAO
	 */
	public PushServiceReplayerNew(LogDataImpl logDAOImplExtended) {
		
		this.logDAOImplExtended = logDAOImplExtended;
	}
		
	/**
	 * For Testing.
	 */
	public void pushTimeslotsForCurrentView(int[] params, String viewName) {
		
		this.logParametersBean.setChoosenViewName(viewName);
		pushTimeslotsForCurrentView(params);
	}
	
	/**
	 * Push JSON Objects for current view. Called always and delegates to
	 * push method.
	 * 
	 * @param params Old and new time slot.
	 */
	public void pushTimeslotsForCurrentView(int[] params) {

		String currentViewUri = this.logParametersBean.getChoosenViewName();	
		String channelname = "/" + PLACEHOLDER_CHANNELNAME 
				+ "ExtendedChangeTimeslot";

		Class<PushServiceReplayerNew> classPushServiceReplayerNew = 
				PushServiceReplayerNew.class;
		
		String currentViewName = currentViewUri.replaceAll(WEBAPP_URI, "");
		StringBuilder methodname = new StringBuilder();
		methodname.append("push");
		methodname.append(currentViewName.substring(0, 1).toUpperCase());
		methodname.append(currentViewName.substring(1));
		
		Method method;

		try {

			method = classPushServiceReplayerNew.getDeclaredMethod(
						methodname.toString(), String.class, String.class, 
						int[].class);
			method.invoke(this, currentViewName, channelname, params);
		} catch (NoSuchMethodException e1) {

		} catch (SecurityException e1) {

		} catch (IllegalAccessException e) {

		} catch (InvocationTargetException e) {

		}
		
		// Messages which are always pushed.
		pushGlobalmessage(currentViewName, channelname, params);
		pushSlider(currentViewName, channelname, params);
	}
	
	/**
	 * Push a JSON object.
	 * 
	 * @param channel Channel name for Primefaces.
	 * @param json Json Object
	 */
	public void push(String channel, String json) {

//		if (this.logParametersBean.getSessionId() != null) {
			
//			StringBuilder channelId = new StringBuilder(channel);
//			channelId.append(this.logParametersBean.getSessionId());
			
			PushContext pushContext = PushContextFactory.getDefault()
					.getPushContext();		
//			pushContext.push(channelId.toString(), json);	

			pushContext.push(channel, json);
//		}
	}
	
	/**
	 * Backward jump.
	 * 
	 * @param oldTimeslot Old time slot.
	 * @param newTimeslot Current time slot.
	 * @return
	 */
	@Deprecated
	public String createBackwardJson(int oldTimeslot, int newTimeslot) {
		
		return gson.toJson(determineTs(oldTimeslot, newTimeslot));
	}
	
	/**
	 * Ascertain all time slots for backward jump.
	 * 
	 * @param oldTimeslot Old time slot
	 * @param newTimeslot New time slot
	 * @return All time slots
	 */
	@Deprecated
	public ArrayList<Long> determineTs(int oldTimeslot, int newTimeslot) {
		
		ArrayList<Long> listTsMillis = new ArrayList<Long>();
		
		while (oldTimeslot > newTimeslot) {
			
			listTsMillis.add(this.helper
					.getMillisForIndex(oldTimeslot, 
							logDAOImplExtended.getCompetition()));
			oldTimeslot--;
		}
		
		return listTsMillis;
	}
	
	/**
	 * Push and create JSON objects for weather reports.
	 * 
	 * @param currentViewName Current view name
	 * @param channelname Channel name
	 * @param params Old and new time slot
	 */
	public void pushWeatherreport(String currentViewName, String 
			channelname, int[] params) {
		
		String json = null;

		if (!(params[0] >= params[1])) {

			// Forward-Jump
			json = createWeatherReportsJson(params[0] + 1, params[1]);		
			channelname = channelname.replaceAll(PLACEHOLDER_CHANNELNAME, 
					currentViewName);
		} else {
			
			// Backward-Jump
			json = createWeatherReportsBackwardJson(this.startNumberTs, params[1]);
			channelname = channelname.replaceAll(PLACEHOLDER_CHANNELNAME, 
					currentViewName + CHANNEL_BACKWARD);
		}
		
		push(channelname.toString(), json);
	}
	
	/**
	 * Create JSON weather report objects for backward jump.
	 * 
	 * @param startTimeslot Start time slot
	 * @param endTimeslot End time slot
	 */
	public String createWeatherReportsBackwardJson(int startTimeslot, 
			int endTimeslot) {
		
		WeatherReport[] weatherReportData = logDAOImplExtended
				.getWeatherReportsData();
		int startNumberTimeslot = logDAOImplExtended
				.getStartNumberTimeslot();
		int startTimeslotIndex = startTimeslot - startNumberTimeslot;
		int endTimeslotIndex = endTimeslot - startNumberTimeslot;
		
		long millis;
		WeatherReport report;
		
        ArrayList<Object> allBackwardData = new ArrayList<Object>();
        ArrayList<Object> backwardDataTemperature = new ArrayList<Object>();
        ArrayList<Object> backwardDataWindSpeed = new ArrayList<Object>();
        ArrayList<Object> backwardDataWindDirection = new ArrayList<Object>();
        ArrayList<Object> backwardDataCloudCover = new ArrayList<Object>();
        
		for (int i = startTimeslotIndex; i <= endTimeslotIndex; i++) {

			report = weatherReportData[i];

			if (report != null) {

				millis = helper.getMillisForIndex(report.getTimeslotIndex(),
						logDAOImplExtended.getCompetition());

				Object[] temperature = { millis, report.getTemperature() };
				backwardDataTemperature.add(temperature);

				Object[] windSpeed = { millis, report.getWindSpeed() };
				backwardDataWindSpeed.add(windSpeed);

				Object[] windDirection = { millis, report.getWindDirection() };
				backwardDataWindDirection.add(windDirection);

				Object[] cloudCover = { millis, report.getCloudCover() };
				backwardDataCloudCover.add(cloudCover);
			}
		}
			
		allBackwardData.add(backwardDataTemperature);
		allBackwardData.add(backwardDataWindSpeed);
		allBackwardData.add(backwardDataWindDirection);
		allBackwardData.add(backwardDataCloudCover);
		
		return gson.toJson(allBackwardData);
	}
	
	/**
	 * Create JSON weather report objects for forward jump.
	 * 
	 * @param startTimeslot Start time slot
	 * @param endTimeslot End time slot
	 */
	public String createWeatherReportsJson(int startTimeslot, int endTimeslot) {
		
		WeatherReport[] weatherReportData = logDAOImplExtended.getWeatherReportsData();
		int startNumberTimeslot = logDAOImplExtended.getStartNumberTimeslot();
		int startTimeslotIndex = startTimeslot - startNumberTimeslot;
		int endTimeslotIndex = endTimeslot - startNumberTimeslot;

		WeatherReport report;
		WeatherPusher weather;
		String weatherReportDataPush;
		ArrayList<WeatherPusher> listWeatherPusher = 
				new ArrayList<WeatherPusher>();
		
		for (int i = startTimeslotIndex; i <= endTimeslotIndex; i++) {
			
			report = weatherReportData[i];
			
			if (report != null) {
				
				weather = new WeatherPusher(helper.getMillisForIndex(report
						.getCurrentTimeslot().getSerialNumber(),
						logDAOImplExtended.getCompetition()),
						report.getTemperature(), report.getWindSpeed(),
						report.getWindDirection(), report.getCloudCover(),
						report.getTimeslotIndex());

				listWeatherPusher.add(weather);
			}
		}
		
	    weatherReportDataPush = gson.toJson(listWeatherPusher, 
	    		new TypeToken<ArrayList<WeatherPusher>>(){}.getType());
		
		return weatherReportDataPush;
	}
	
	/**
	 * Push and create JSON objects for distributions cumulative.
	 * 
	 * @param currentViewName Current view name
	 * @param channelname Channel name
	 * @param params Old and new time slot
	 */
	public void pushDistributioncumulative(String currentViewName, 
			String channelname, int[] params) {
		
		pushDistribution(currentViewName, channelname, params);
	}
	
	/**
	 * Push and create JSON objects for distributions per timeslot.
	 * 
	 * @param currentViewName Current view name
	 * @param channelname Channel name
	 * @param params Old and new time slot
	 */
	public void pushDistributionpertimeslot(String currentViewName, 
			String channelname, int[] params) {
		
		pushDistribution(currentViewName, channelname, params);
	}
	
	/**
	 * Push and create JSON objects for distributions.
	 * 
	 * @param currentViewName Current view name
	 * @param channelname Channel name
	 * @param params Old and new time slot
	 */
	public void pushDistribution(String currentViewName, 
			String channelname, int[] params) {
		
		String json = null;

		if (!(params[0] >= params[1])) {
			
			// Forward-Jump
			json = createDistributionsJson(params[0] + 1, 
					params[1]);
			channelname = channelname.replaceAll(PLACEHOLDER_CHANNELNAME, 
					currentViewName);
		} else {
			
			// Backward-Jump			
			json = createDistributionsBackwardJson(this.startNumberTs, 
					params[1], currentViewName);
			channelname = channelname.replaceAll(PLACEHOLDER_CHANNELNAME,
					currentViewName + CHANNEL_BACKWARD);
		}

		push(channelname.toString(), json);
	}
	
	/**
	 * Create JSON distribution objects for backward jump.
	 * 
	 * @param startTimeslot Start time slot
	 * @param endTimeslot End time slot
	 * @param currentViewName Current view name
	 */
	public String createDistributionsBackwardJson(int starttimeslot, 
			int endtimeslot, String currentViewName) {
				
		Map<String, Integer> mapBrokerArrayAssign = logDAOImplExtended
				.getMapBrokerArrayAssign();
		DynamicData[] dynamicDatas = logDAOImplExtended
				.getDistributionsData();
		List<String> brokerNames = logDAOImplExtended
				.getCompetition().getBrokers();

		int startNumberTimeslot = logDAOImplExtended.getStartNumberTimeslot();
		int numberOfBrokers = mapBrokerArrayAssign.size();
		int posIndex;
		int position;
		int startTimeslot = starttimeslot - startNumberTimeslot;
		int endTimeslot = endtimeslot - startNumberTimeslot;
		
		DynamicData dynamicData;
        long millis;
        
        ArrayList<Object> allBackwardData = new ArrayList<Object>();
        ArrayList<Object> backwardDataProfit;
        ArrayList<Object> backwardDataEnergy;
        
        for (String brokerName : brokerNames) {

        	posIndex = mapBrokerArrayAssign.get(brokerName);
        	
        	backwardDataProfit = new ArrayList<Object>();
        	backwardDataEnergy = new ArrayList<Object>();
        	
        	for (int ts = startTimeslot; ts <= endTimeslot; ts++) {
        		
        		position = posIndex + numberOfBrokers * ts;
        				
				if (dynamicDatas[position] != null) { 
					
					dynamicData = dynamicDatas[position];
					
					millis = helper.getMillisForIndex(dynamicData.getTsIndex(),
							logDAOImplExtended.getCompetition());

					if (currentViewName.equals("distributioncumulative")) {
						
						// Kumuliert
						Object[] profit = { millis,
								dynamicData.getProfit() };
								backwardDataProfit.add(profit);

						Object[] energy = { millis,
								dynamicData.getEnergy() };
								backwardDataEnergy.add(energy);
					} else {
						
						// Per Timeslot
						Object[] profit = { millis,
								dynamicData.getProfitDelta() };
								backwardDataProfit.add(profit);

						Object[] energy = { millis,
								dynamicData.getEnergyDelta() };
								backwardDataEnergy.add(energy);
					}
				} else {
					
					int safeTimeslot = ts;
					position = posIndex + numberOfBrokers * safeTimeslot;
					
					DynamicData dd = dynamicDatas[position];
					
					while (safeTimeslot >= 0 && dd == null) {
						safeTimeslot--;
						if (safeTimeslot >= 0) {
							position = posIndex + numberOfBrokers * safeTimeslot;
							dd = dynamicDatas[position];
						}
					}

					DynamicData dd2 = (dd == null) 
							? new DynamicData(startNumberTs, 0, 0) 
							: dd;
							
							
				    millis = helper.getMillisForIndex(dd2.getTsIndex(),
									logDAOImplExtended.getCompetition());

					if (currentViewName.equals("distributioncumulative")) {
						
						// Kumuliert
						Object[] profit = { millis, dd2.getProfit() };
						backwardDataProfit.add(profit);

						Object[] energy = { millis, dd2.getEnergy() };
						backwardDataEnergy.add(energy);
					} else {

						// Per Timeslot
						Object[] profit = { millis,
								dd2.getProfitDelta() };
						backwardDataProfit.add(profit);

						Object[] energy = { millis,
								dd2.getEnergyDelta() };
						backwardDataEnergy.add(energy);
					}
				}
        	}
        	
        	allBackwardData.add(backwardDataProfit);
        	allBackwardData.add(backwardDataEnergy);
        }
        
        return gson.toJson(allBackwardData);
	}
	
	/**
	 * Create JSON distribution objects for forward jump.
	 * 
	 * @param startTimeslot Start time slot
	 * @param endTimeslot End time slot
	 */
	public String createDistributionsJson(int starttimeslot, int endtimeslot) {

		Map<String, Integer> mapBrokerArrayAssign = logDAOImplExtended
				.getMapBrokerArrayAssign();
		DynamicData[] dynamicDatas = logDAOImplExtended
				.getDistributionsData();
		List<String> brokerNames = logDAOImplExtended
				.getCompetition().getBrokers();

		int startNumberTimeslot = logDAOImplExtended.getStartNumberTimeslot();
		int numberOfBrokers = mapBrokerArrayAssign.size();
		int posIndex;
		int position;
		int startTimeslot = starttimeslot - startNumberTimeslot;
		int endTimeslot = endtimeslot - startNumberTimeslot;
		
		DynamicData dynamicData;
		DynDataPusher pusher;
		ArrayList<DynDataPusher> distributionPushers = 
				new ArrayList<DynDataPusher>();
		ArrayList<ArrayList<DynDataPusher>> allDistributionPushers = 
				new ArrayList<ArrayList<DynDataPusher>>();
        int listIndex;
		
		for (int ts = startTimeslot; ts <= endTimeslot; ts++) {
			
			distributionPushers = new ArrayList<DynDataPusher>();
			listIndex = 0;
			
			for (String brokerName : brokerNames) {
				
				posIndex = mapBrokerArrayAssign.get(brokerName);
				position = posIndex + numberOfBrokers * ts;
				
				if (dynamicDatas[position] != null) { 
					
					dynamicData = dynamicDatas[position];
					pusher = new DynDataPusherReplayer("Broker Name",
							helper.getMillisForIndex(dynamicData.getTsIndex(),
									logDAOImplExtended.getCompetition()),
							dynamicData.getProfit(), 
							dynamicData.getEnergy(),
							dynamicData.getProfitDelta(),
							dynamicData.getEnergyDelta(), 
							listIndex);
					distributionPushers.add(pusher);
				} else {
					
					int safeTimeslot = ts;
					position = posIndex + numberOfBrokers * safeTimeslot;
					
					DynamicData dd = dynamicDatas[position];
					
					while (safeTimeslot >= 0 && dd == null) {
						safeTimeslot--;
						if (safeTimeslot >= 0) {
							position = posIndex + numberOfBrokers * safeTimeslot;
							dd = dynamicDatas[position];
						}
					}

					DynamicData dd2 = (dd == null) 
							? new DynamicData(startNumberTs, 0, 0) 
							: dd;

					pusher = new DynDataPusherReplayer("Broker Name",
							helper.getMillisForIndex(dd2.getTsIndex(),
									logDAOImplExtended.getCompetition()),
							dd2.getProfit(), 
							dd2.getEnergy(),
							dd2.getProfitDelta(),
							dd2.getEnergyDelta(), 
							listIndex);
					distributionPushers.add(pusher);
				}
				
				listIndex +=2;
			}

			allDistributionPushers.add(distributionPushers);
		}
		
		return gson.toJson(allDistributionPushers);
	}
	
	/**
	 * Push and create JSON objects for balancing per time slot.
	 * 
	 * @param currentViewName Current view name
	 * @param channelname Channel name
	 * @param params Old and new time slot
	 */
	public void pushBalancingpertimeslot(String currentViewName,
			String channelname, int[] params) {
		
		pushBalancing(currentViewName, channelname, params);
	}
	
	/**
	 * Push and create JSON objects for balancing cumulative.
	 * 
	 * @param currentViewName Current view name
	 * @param channelname Channel name
	 * @param params Old and new time slot
	 */
	public void pushBalancingcumulative(String currentViewName,
			String channelname, int[] params) {
		
		pushBalancing(currentViewName, channelname, params);
	}
	
	/**
	 * Push and create JSON objects for balancing.
	 * 
	 * @param currentViewName Current view name
	 * @param channelname Channel name
	 * @param params Old and new time slot
	 */
	public void pushBalancing(String currentViewName,
			String channelname, int[] params) {
		
		String json = null;

		if (!(params[0] >= params[1])) {

			// Forward-Jump.
			json = createBalancingsJson(params[0] + 1, params[1]);
			channelname = channelname.replaceAll(PLACEHOLDER_CHANNELNAME, 
					currentViewName);
		} else {
			
			// Backward-Jump.
			json = createBalancingsBackwardJson(this.startNumberTs, params[1],
					currentViewName);
			channelname = channelname.replaceAll(PLACEHOLDER_CHANNELNAME,
					currentViewName + CHANNEL_BACKWARD);
		}
		
		push(channelname.toString(), json);
	}
	
	/**
	 * Create JSON balancing objects for backward jump.
	 * 
	 * @param startTimeslot Start time slot
	 * @param endTimeslot End time slot
	 * @param currentViewName Current view name
	 */
	public String createBalancingsBackwardJson(int starttimeslot, 
			int endtimeslot, String currentViewName) {
		
		Map<String, Integer> mapBrokerArrayAssign = logDAOImplExtended
				.getMapBrokerArrayAssign();
		DynamicData[] dynamicDatas = logDAOImplExtended
				.getBalancingsData();
		List<String> brokerNames = logDAOImplExtended
				.getCompetition().getBrokers();

		int startNumberTimeslot = logDAOImplExtended.getStartNumberTimeslot();
		int numberOfBrokers = mapBrokerArrayAssign.size();
		int posIndex;
		int position;
		int startTimeslot = starttimeslot - startNumberTimeslot;
		int endTimeslot = endtimeslot - startNumberTimeslot;
		
		DynamicData dynamicData;
        long millis;
        
        ArrayList<Object> allBackwardData = new ArrayList<Object>();
        ArrayList<Object> backwardDataProfit;
        ArrayList<Object> backwardDataEnergy;
        
        for (String brokerName : brokerNames) {

        	posIndex = mapBrokerArrayAssign.get(brokerName);
        	
        	backwardDataProfit = new ArrayList<Object>();
        	backwardDataEnergy = new ArrayList<Object>();
        	
        	for (int ts = startTimeslot; ts <= endTimeslot; ts++) {
        		
        		position = posIndex + numberOfBrokers * ts;
        				
				if (dynamicDatas[position] != null) { 
					
					dynamicData = dynamicDatas[position];
					
					millis = helper.getMillisForIndex(dynamicData.getTsIndex(),
							logDAOImplExtended.getCompetition());

					if (currentViewName.equals("balancingcumulative")) {
						
						// Kumuliert
						Object[] profit = { millis,
								dynamicData.getProfit() };
								backwardDataProfit.add(profit);

						Object[] energy = { millis,
								dynamicData.getEnergy() };
								backwardDataEnergy.add(energy);
					} else {
						
						// Per Timeslot
						Object[] profit = { millis,
								dynamicData.getProfitDelta() };
								backwardDataProfit.add(profit);

						Object[] energy = { millis,
								dynamicData.getEnergyDelta() };
								backwardDataEnergy.add(energy);
					}
				} else {
					
					int safeTimeslot = ts;
					position = posIndex + numberOfBrokers * safeTimeslot;
					
					DynamicData dd = dynamicDatas[position];
					
					while (safeTimeslot >= 0 && dd == null) {
						safeTimeslot--;
						if (safeTimeslot >= 0) {
							position = posIndex + numberOfBrokers * safeTimeslot;
							dd = dynamicDatas[position];
						}
					}

					DynamicData dd2 = (dd == null) 
							? new DynamicData(startNumberTs, 0, 0) 
							: dd;
							
							
				    millis = helper.getMillisForIndex(dd2.getTsIndex(),
									logDAOImplExtended.getCompetition());

					if (currentViewName.equals("balancingcumulative")) {
						
						// Kumuliert
						Object[] profit = { millis, dd2.getProfit() };
						backwardDataProfit.add(profit);

						Object[] energy = { millis, dd2.getEnergy() };
						backwardDataEnergy.add(energy);
					} else {

						// Per Timeslot
						Object[] profit = { millis,
								dd2.getProfitDelta() };
						backwardDataProfit.add(profit);

						Object[] energy = { millis,
								dd2.getEnergyDelta() };
						backwardDataEnergy.add(energy);
					}
				}
        	}
        	
        	allBackwardData.add(backwardDataProfit);
        	allBackwardData.add(backwardDataEnergy);
        }
        
        return gson.toJson(allBackwardData);
	}
	
	/**
	 * Create JSON balancing objects for forward jump.
	 * 
	 * @param startTimeslot Start time slot
	 * @param endTimeslot End time slot
	 */
	public String createBalancingsJson(int starttimeslot, int endtimeslot) {
		
		Map<String, Integer> mapBrokerArrayAssign = logDAOImplExtended
				.getMapBrokerArrayAssign();
		DynamicData[] dynamicDatas = logDAOImplExtended
				.getBalancingsData();
		List<String> brokerNames = logDAOImplExtended
				.getCompetition().getBrokers();

		int startNumberTimeslot = logDAOImplExtended.getStartNumberTimeslot();
		int numberOfBrokers = mapBrokerArrayAssign.size();
		int posIndex;
		int position;
		int startTimeslot = starttimeslot - startNumberTimeslot;
		int endTimeslot = endtimeslot - startNumberTimeslot;
		
		DynamicData dynamicData;
		DynDataPusher pusher;
		ArrayList<DynDataPusher> balancingPushers = 
				new ArrayList<DynDataPusher>();
		ArrayList<ArrayList<DynDataPusher>> allBalancingPushers = 
				new ArrayList<ArrayList<DynDataPusher>>();
		int listIndex;
		
		for (int ts = startTimeslot; ts <= endTimeslot; ts++) {
			
			balancingPushers = new ArrayList<DynDataPusher>();
			listIndex = 0;
			
			for (String brokerName : brokerNames) {
				
				posIndex = mapBrokerArrayAssign.get(brokerName);
				position = posIndex + numberOfBrokers * ts;
				
				if (dynamicDatas[position] != null) { 

					dynamicData = dynamicDatas[position];
					pusher = new DynDataPusherReplayer("Broker Name",
							helper.getMillisForIndex(dynamicData.getTsIndex(),
									logDAOImplExtended.getCompetition()),
							dynamicData.getProfit(), 
							dynamicData.getEnergy(),
							dynamicData.getProfitDelta(),
							dynamicData.getEnergyDelta(), 
							listIndex);
					balancingPushers.add(pusher);
				} else {
					
					int safeTimeslot = ts;
					position = posIndex + numberOfBrokers * safeTimeslot;
					
					DynamicData dd = dynamicDatas[position];
					
					while (safeTimeslot >= 0 && dd == null) {
						safeTimeslot--;
						if (safeTimeslot >= 0) {
							position = posIndex + numberOfBrokers * safeTimeslot;
							dd = dynamicDatas[position];
						}
					}

					DynamicData dd2 = (dd == null) 
							? new DynamicData(startNumberTs, 0, 0) 
							: dd;

					pusher = new DynDataPusherReplayer("Broker Name",
							helper.getMillisForIndex(dd2.getTsIndex(),
									logDAOImplExtended.getCompetition()),
							dd2.getProfit(), 
							dd2.getEnergy(),
							dd2.getProfitDelta(),
							dd2.getEnergyDelta(), 
							listIndex);
					balancingPushers.add(pusher);
				}
				
				listIndex +=2;
			}

			allBalancingPushers.add(balancingPushers);
		}
		
		return gson.toJson(allBalancingPushers);
	}
	
	/**
	 * Push and create JSON objects for wholesale per time slot.
	 * 
	 * @param currentViewName Current view name
	 * @param channelname Channel name
	 * @param params Old and new time slot
	 */
	public void pushWholesalepertimeslot(String currentViewName, 
			String channelname, int[] params) {
		
		pushWholesale(currentViewName, channelname, params);
	}
	
	/**
	 * Push and create JSON objects for wholesale cumulative.
	 * 
	 * @param currentViewName Current view name
	 * @param channelname Channel name
	 * @param params Old and new time slot
	 */
	public void pushWholesalecumulative(String currentViewName, 
			String channelname, int[] params) {
		
		pushWholesale(currentViewName, channelname, params);
	}
	
	/**
	 * Push and create JSON objects for wholesale cumulative and per time slot.
	 * 
	 * @param currentViewName Current view name
	 * @param channelname Channel name
	 * @param params Old and new time slot
	 */
	public void pushWholesale(String currentViewName, 
			String channelname, int[] params) {
		
		String json = null;

		if (!(params[0] >= params[1])) {

			// Forward-Jump
			json = createWholesaleMarketTransactionsJson(params[0], 
					params[1] - 1);
			channelname = channelname.replaceAll(PLACEHOLDER_CHANNELNAME, 
					currentViewName);
		} else {
			
			// Backward-Jump
			json = createWholesaleMarketTransactionsBackwardJson(this.startNumberTs, 
					params[1] - 1, currentViewName);
			channelname = channelname.replaceAll(PLACEHOLDER_CHANNELNAME,
					currentViewName + CHANNEL_BACKWARD);
		}
		
		push(channelname.toString(), json);
	}
	
	/**
	 * Create JSON wholesale dynamic data objects for backward jump.
	 * 
	 * @param startTimeslot Start time slot
	 * @param endTimeslot End time slot
	 * @param currentViewName Current view name
	 */
	public String createWholesaleMarketTransactionsBackwardJson(int starttimeslot, 
			int endtimeslot, String currentViewName) {
		
		Map<String, Integer> mapBrokerArrayAssign = logDAOImplExtended
				.getMapBrokerArrayAssign();
		DynamicData[] dynamicDatas = logDAOImplExtended
				.getMarketTransactionsData();
		List<String> brokerNames = logDAOImplExtended
				.getCompetition().getBrokers();

		int startNumberTimeslot = logDAOImplExtended.getStartNumberTimeslot();
		int numberOfBrokers = mapBrokerArrayAssign.size();
		int posIndex;
		int position;
		int startTimeslot = starttimeslot - startNumberTimeslot;
		int endTimeslot = endtimeslot - startNumberTimeslot;
		
		DynamicData dynamicData;
        long millis;
        
        ArrayList<Object> allBackwardData = new ArrayList<Object>();
        ArrayList<Object> backwardDataProfit;
        ArrayList<Object> backwardDataEnergy;
        
        for (String brokerName : brokerNames) {

        	posIndex = mapBrokerArrayAssign.get(brokerName);
        	
        	backwardDataProfit = new ArrayList<Object>();
        	backwardDataEnergy = new ArrayList<Object>();
        	
        	for (int ts = startTimeslot; ts <= endTimeslot; ts++) {
        		
        		position = posIndex + numberOfBrokers * ts;
        				
				if (dynamicDatas[position] != null) { 
					
					dynamicData = dynamicDatas[position];
					
					millis = helper.getMillisForIndex(dynamicData.getTsIndex(),
							logDAOImplExtended.getCompetition());

					if (currentViewName.equals("wholesalecumulative")) {
						
						// Kumuliert
						Object[] profit = { millis,
								dynamicData.getProfit() };
								backwardDataProfit.add(profit);

						Object[] energy = { millis,
								dynamicData.getEnergy() };
								backwardDataEnergy.add(energy);
					} else {
						
						// Per Timeslot
						Object[] profit = { millis,
								dynamicData.getProfitDelta() };
								backwardDataProfit.add(profit);

						Object[] energy = { millis,
								dynamicData.getEnergyDelta() };
								backwardDataEnergy.add(energy);
					}
				} else {
					
					int safeTimeslot = ts;
					position = posIndex + numberOfBrokers * safeTimeslot;
					
					DynamicData dd = dynamicDatas[position];
					
					while (safeTimeslot >= 0 && dd == null) {
						safeTimeslot--;
						if (safeTimeslot >= 0) {
							position = posIndex + numberOfBrokers * safeTimeslot;
							dd = dynamicDatas[position];
						}
					}

					DynamicData dd2 = (dd == null) 
							? new DynamicData(startNumberTs, 0, 0) 
							: dd;
							
							
				    millis = helper.getMillisForIndex(dd2.getTsIndex(),
									logDAOImplExtended.getCompetition());

					if (currentViewName.equals("wholesalecumulative")) {
						
						// Kumuliert
						Object[] profit = { millis, dd2.getProfit() };
						backwardDataProfit.add(profit);

						Object[] energy = { millis, dd2.getEnergy() };
						backwardDataEnergy.add(energy);
					} else {

						// Per Timeslot
						Object[] profit = { millis,
								dd2.getProfitDelta() };
						backwardDataProfit.add(profit);

						Object[] energy = { millis,
								dd2.getEnergyDelta() };
						backwardDataEnergy.add(energy);
					}
				}
        	}
        	
        	allBackwardData.add(backwardDataProfit);
        	allBackwardData.add(backwardDataEnergy);
        }
        
        return gson.toJson(allBackwardData);
	}
	
	/**
	 * Create JSON wholesale dynamic data objects for forward jump.
	 * 
	 * @param startTimeslot Start time slot
	 * @param endTimeslot End time slot
	 */
	public String createWholesaleMarketTransactionsJson(int starttimeslot, 
			int endtimeslot) {
		
		Map<String, Integer> mapBrokerArrayAssign = logDAOImplExtended
				.getMapBrokerArrayAssign();
		DynamicData[] dynamicDatas = logDAOImplExtended
				.getMarketTransactionsData();
		List<String> brokerNames = logDAOImplExtended
				.getCompetition().getBrokers();

		int startNumberTimeslot = logDAOImplExtended.getStartNumberTimeslot();
		int numberOfBrokers = mapBrokerArrayAssign.size();
		int posIndex;
		int position;
		int startTimeslot = starttimeslot - startNumberTimeslot;
		int endTimeslot = endtimeslot - startNumberTimeslot;
		
        // Wholesale market push
		DynamicData dynamicData;
		WholesaleMarketPusher pusher;
		ArrayList<WholesaleMarketPusher> mtPushers;
		ArrayList<ArrayList<WholesaleMarketPusher>> allMtPushers = 
				new ArrayList<ArrayList<WholesaleMarketPusher>>();
		int listIndex;
		
		for (int ts = startTimeslot; ts <= endTimeslot; ts++) {

			mtPushers = new ArrayList<WholesaleMarketPusher>();
			listIndex = 0;
			
			for (String brokerName : brokerNames) {
				
				posIndex = mapBrokerArrayAssign.get(brokerName);
				position = posIndex + numberOfBrokers * ts;
				
				if (dynamicDatas[position] != null) { 
					
					dynamicData = dynamicDatas[position];
					pusher = new WholesaleMarketPusherReplayer("NAME_BROKER",
							helper.getMillisForIndex(dynamicData.getTsIndex(),
									logDAOImplExtended.getCompetition()),
							dynamicData.getProfitDelta(),
							dynamicData.getEnergyDelta(),
							dynamicData.getProfit(), dynamicData.getEnergy(), listIndex);
					mtPushers.add(pusher);
				} else {
					
					int safeTimeslot = ts;
					position = posIndex + numberOfBrokers * safeTimeslot;
					
					DynamicData dd = dynamicDatas[position];
					
					while (safeTimeslot >= 0 && dd == null) {
						safeTimeslot--;
						if (safeTimeslot >= 0) {
							position = posIndex + numberOfBrokers * safeTimeslot;
							dd = dynamicDatas[position];
						}
					}

					DynamicData dd2 = (dd == null) 
							? new DynamicData(startNumberTs, 0, 0) 
							: dd;

					pusher = new WholesaleMarketPusherReplayer("Broker Name",
							helper.getMillisForIndex(dd2.getTsIndex(),
									logDAOImplExtended.getCompetition()),
							dd2.getProfitDelta(), 
							dd2.getEnergyDelta(),
							dd2.getProfit(),
							dd2.getEnergy(), 
							listIndex);
					mtPushers.add(pusher);
				}
				
				listIndex += 2;
			}
			
			allMtPushers.add(mtPushers);
		}
		
		return gson.toJson(allMtPushers);
	}
	
	/**
	 * Push and create JSON objects for all market transaction data.
	 * 
	 * @param currentViewName Current view name
	 * @param channelname Channel name
	 * @param params Old and new time slot
	 */
	public void pushWholesalemarkettxs(String currentViewName,
			String channelname, int[] params) {
		
		String json = null;

		int starttimeslot = 
				(params[1] - 48 < startNumberTs) 
					? startNumberTs
					: params[1] - 48;
		
		int endtimeslot = params[1] - 1;

		json = createWholesaleAllMarketTransactionsJson(starttimeslot,
				endtimeslot);
	
		channelname = channelname.replaceAll(PLACEHOLDER_CHANNELNAME, 
				currentViewName);
		
		push(channelname.toString(), json);
	}

	/**
	 * Create JSON all market transaction objects.
	 * 
	 * @param startTimeslot Start time slot
	 * @param endTimeslot End time slot
	 */
	public String createWholesaleAllMarketTransactionsJson(int starttimeslot, 
			int endtimeslot) {

		Map<String, Integer> mapBrokerArrayAssign = logDAOImplExtended
				.getMapBrokerArrayAssign();
		List<MarketTransaction>[] listAllMarketTransaction = logDAOImplExtended
				.getAllMarketTransactionsData();
		List<String> brokerNames = logDAOImplExtended
				.getCompetition().getBrokers();
		
		int startNumberTimeslot = logDAOImplExtended.getStartNumberTimeslot();
		int numberOfBrokers = mapBrokerArrayAssign.size();
		int posIndex;
		int position;
		int startTimeslot = starttimeslot - startNumberTimeslot;
		int endTimeslot = endtimeslot - startNumberTimeslot;
		
		ArrayList<ArrayList<Object>> allWholesaleData = new ArrayList<ArrayList<Object>>();
		ArrayList<Object> wholesaleTxBrokerData;
		List<MarketTransaction> listMarketTransaction;
		ArrayList<Double> transaction;

		for (String brokerName : brokerNames) {

			wholesaleTxBrokerData = new ArrayList<Object>();
			posIndex = mapBrokerArrayAssign.get(brokerName);
		
			for (int ts = startTimeslot; ts <= endTimeslot; ts++) {

				position = posIndex + numberOfBrokers * ts;
				
				if (listAllMarketTransaction[position] != null) {

					listMarketTransaction = listAllMarketTransaction[position];

					for (MarketTransaction marketTransaction : listMarketTransaction) {

						transaction = new ArrayList<Double>();
						transaction.add(marketTransaction.getPrice());
						transaction.add(marketTransaction.getMWh());
						wholesaleTxBrokerData.add(transaction);
					}
				}
			}

			allWholesaleData.add(wholesaleTxBrokerData);
		}

		return gson.toJson(allWholesaleData);
	}

	/**
	 * Push and create JSON objects for wholesale average price
	 * per time slot.
	 * 
	 * @param currentViewName Current view name
	 * @param channelname Channel name
	 * @param params Old and new time slot
	 */
	public void pushWholesaleavgpricepertimeslot(String currentViewName,
			String channelname, int[] params) {
		
		String json = null;

		if (!(params[0] >= params[1])) {

			// Forward-Jump.
			json = createWholesaleAvgPriceDatasJson(params[0], params[1] - 1);
			channelname = channelname.replaceAll(PLACEHOLDER_CHANNELNAME, 
					currentViewName);
		} else {
			
			// Backward-Jump.
			json = createWholesaleAvgPriceDatasBackwardJson(this.startNumberTs, 
					params[1] - 1);
			channelname = channelname.replaceAll(PLACEHOLDER_CHANNELNAME, 
					currentViewName + CHANNEL_BACKWARD);
		}
		
		push(channelname.toString(), json);
	}
	
	/**
	 * Create JSON wholesale average price objects for backward jump.
	 * 
	 * @param startTimeslot Start time slot
	 * @param endTimeslot End time slot
	 */
	public String createWholesaleAvgPriceDatasBackwardJson(int starttimeslot, 
			int endtimeslot) {
		
		List<ClearedTradeObject>[] listClearedTradeObjects = logDAOImplExtended
				.getClearedTradesData();
		
		List<ClearedTradeObject> listClearedTradeObject;
		int startNumberTimeslot = logDAOImplExtended.getStartNumberTimeslot();
		int startTimeslot = starttimeslot - startNumberTimeslot;
		int endTimeslot = endtimeslot - startNumberTimeslot;
		
        ArrayList<Object> allBackwardData = new ArrayList<Object>();
        ArrayList<Object> backwardDataProfit = new ArrayList<Object>();
        ArrayList<Object> backwardDataEnergy = new ArrayList<Object>();
        long millis;

		double totalRevenue;
		double totalEnergy;
		int numberOfTransactions;

		for (int ts = startTimeslot; ts <= endTimeslot; ts++) {

			listClearedTradeObject = listClearedTradeObjects[ts];
			
			totalRevenue = 0;
			totalEnergy = 0;
			numberOfTransactions = 0;
			
			// get all cleared trades for timeslot with timeslot index = safety
			if (listClearedTradeObject != null) {

				// calculate total revenue and amount of traded energy for
				// safety timeslot and count number of transactions in that
				// timeslot
				for (ClearedTradeObject clearedTradeObject : listClearedTradeObject) {
					
					totalRevenue += clearedTradeObject.getClearedTrade().getExecutionPrice();
					totalEnergy += clearedTradeObject.getClearedTrade().getExecutionMWh();
					numberOfTransactions++;
				}

				millis = helper.getMillisForIndex(ts + startNumberTimeslot,
						logDAOImplExtended.getCompetition());
				
				// Kumuliert
				Object[] profit = { millis, totalRevenue / numberOfTransactions };
				backwardDataProfit.add(profit);

				Object[] energy = { millis, totalEnergy };
				backwardDataEnergy.add(energy);
			}
		}

		allBackwardData.add(backwardDataProfit);
		allBackwardData.add(backwardDataEnergy);
		
		return gson.toJson(allBackwardData);
	}
	
	/**
	 * Create JSON wholesale average price objects for forward jump.
	 * 
	 * @param startTimeslot Start time slot
	 * @param endTimeslot End time slot
	 */
	public String createWholesaleAvgPriceDatasJson(int starttimeslot, 
			int endtimeslot) {

		List<ClearedTradeObject>[] listClearedTradeObjects = logDAOImplExtended
				.getClearedTradesData();
		
		List<ClearedTradeObject> listClearedTradeObject;
		int startNumberTimeslot = logDAOImplExtended.getStartNumberTimeslot();
		int startTimeslot = starttimeslot - startNumberTimeslot;
		int endTimeslot = endtimeslot - startNumberTimeslot;
		
		ArrayList<StatisticsPusher> statisticsPushers = 
				new ArrayList<StatisticsPusher>();

		double totalRevenue;
		double totalEnergy;
		int numberOfTransactions;

		for (int ts = startTimeslot; ts <= endTimeslot; ts++) {

			listClearedTradeObject = listClearedTradeObjects[ts];
			
			totalRevenue = 0;
			totalEnergy = 0;
			numberOfTransactions = 0;
			
			// get all cleared trades for timeslot with timeslot index = safety
			if (listClearedTradeObject != null) {

				// calculate total revenue and amount of traded energy for
				// safety timeslot and count number of transactions in that
				// timeslot
				for (ClearedTradeObject clearedTradeObject : listClearedTradeObject) {
					
					totalRevenue += clearedTradeObject.getClearedTrade().getExecutionPrice();
					totalEnergy += clearedTradeObject.getClearedTrade().getExecutionMWh();
					numberOfTransactions++;
				}

				statisticsPushers.add(new StatisticsPusher(
						helper.getMillisForIndex(ts + startNumberTimeslot,
								logDAOImplExtended.getCompetition()),
						totalRevenue / numberOfTransactions, totalEnergy));
			}

		}
		
		return gson.toJson(statisticsPushers);
	}
	
	/**
	 * Push and create JSON objects for tariff cumulative.
	 * 
	 * @param currentViewName Current view name
	 * @param channelname Channel name
	 * @param params Old and new time slot
	 */
	public void pushTariffcumulative(String currentViewName,
			String channelname, int[] params) {
		
		pushTariff(currentViewName, channelname, params);
	}
	
	/**
	 * Push and create JSON objects for tariff per time slot.
	 * 
	 * @param currentViewName Current view name
	 * @param channelname Channel name
	 * @param params Old and new time slot
	 */
	public void pushTariffpertimeslot(String currentViewName,
			String channelname, int[] params) {
		
		pushTariff(currentViewName, channelname, params);
	}
	
	/**
	 * Push and create JSON objects for tariffs.
	 * 
	 * @param currentViewName Current view name
	 * @param channelname Channel name
	 * @param params Old and new time slot
	 */
	public void pushTariff(String currentViewName,
			String channelname, int[] params) {
		
		String json = null;

		if (!(params[0] >= params[1])) {

			// Forward-Jump.
			json = createTariffTransactionDatasJson(params[0] + 1, params[1]);
			channelname = channelname.replaceAll(PLACEHOLDER_CHANNELNAME, 
					currentViewName);
		} else {
			
			// Backward-Jump.
			json = createTariffTransactionDatasBackwardJson(this.startNumberTs, 
					params[1], currentViewName);
			channelname = channelname.replaceAll(PLACEHOLDER_CHANNELNAME, 
					currentViewName + CHANNEL_BACKWARD);
		}
		
		push(channelname.toString(), json);
	}
	
	/**
	 * Create JSON tariff objects for backward jump.
	 * 
	 * @param startTimeslot Start time slot
	 * @param endTimeslot End time slot
	 * @param currentViewName Current view name
	 */
	public String createTariffTransactionDatasBackwardJson(int starttimeslot, 
			int endtimeslot, String currentViewName) {
				
		Map<String, Integer> mapBrokerArrayAssign = logDAOImplExtended
				.getMapBrokerArrayAssign();
		TariffDynamicData[] tariffDynamicDatas = logDAOImplExtended
				.getTariffDynamicsData();
		List<String> brokerNames = logDAOImplExtended
				.getCompetition().getBrokers();

		int startNumberTimeslot = logDAOImplExtended.getStartNumberTimeslot();
		int numberOfBrokers = mapBrokerArrayAssign.size();
		int posIndex;
		int position;
		int startTimeslot = starttimeslot - startNumberTimeslot;
		int endTimeslot = endtimeslot - startNumberTimeslot;
		
		TariffDynamicData tariffDynamicData;
        long millis;
        
        ArrayList<Object> allBackwardData = new ArrayList<Object>();
        ArrayList<Object> backwardDataProfit;
        ArrayList<Object> backwardDataEnergy;
        ArrayList<Object> backwardDataCustomerCount;
        
        for (String brokerName : brokerNames) {

        	posIndex = mapBrokerArrayAssign.get(brokerName);
        	
        	backwardDataProfit = new ArrayList<Object>();
        	backwardDataEnergy = new ArrayList<Object>();
        	backwardDataCustomerCount = new ArrayList<Object>();
        	
        	for (int ts = startTimeslot; ts <= endTimeslot; ts++) {
        		
        		position = posIndex + numberOfBrokers * ts;
        				
				if (tariffDynamicDatas[position] != null) { 
					
					tariffDynamicData = tariffDynamicDatas[position];
					
					millis = helper.getMillisForIndex(tariffDynamicData
							.getDynamicData().getTsIndex(),
							logDAOImplExtended.getCompetition());

					if (currentViewName.equals("tariffcumulative")) {
						
						// Kumuliert
						Object[] profit = { millis,
								tariffDynamicData.getDynamicData().getProfit() };
						backwardDataProfit.add(profit);

						Object[] energy = { millis,
								tariffDynamicData.getDynamicData().getEnergy() };
						backwardDataEnergy.add(energy);
						
						Object[] customerCount = { millis,
								tariffDynamicData.getCustomerCount() };
						backwardDataCustomerCount.add(customerCount);
					} else {
						
						// Per Timeslot
						Object[] profit = { millis,
								tariffDynamicData.getDynamicData().getProfitDelta() };
						backwardDataProfit.add(profit);

						Object[] energy = { millis,
								tariffDynamicData.getDynamicData().getEnergyDelta() };
						backwardDataEnergy.add(energy);
						
						Object[] customerCount = { millis,
								tariffDynamicData.getCustomerCountDelta() };
						backwardDataCustomerCount.add(customerCount);
					}
				} else {
					
					int safeTimeslot = ts;
					position = posIndex + numberOfBrokers * safeTimeslot;
					
					TariffDynamicData tdd = tariffDynamicDatas[position];
					
					while (safeTimeslot >= 0 && tdd == null) {
						safeTimeslot--;
						if (safeTimeslot >= 0) {
							position = posIndex + numberOfBrokers * safeTimeslot;
							tdd = tariffDynamicDatas[position];
						}
					}

					TariffDynamicData tdd2 = (tdd == null) 
							? new TariffDynamicData(new DynamicData(startNumberTs, 0, 0), 0, 0)  
							: tdd;
							
							
				    millis = helper.getMillisForIndex(tdd2.getDynamicData().getTsIndex(),
									logDAOImplExtended.getCompetition());

					if (currentViewName.equals("tariffcumulative")) {
						
						// Kumuliert
						Object[] profit = { millis, tdd2.getDynamicData().getProfit() };
						backwardDataProfit.add(profit);

						Object[] energy = { millis, tdd2.getDynamicData().getEnergy() };
						backwardDataEnergy.add(energy);
						
						Object[] customerCount = { millis,
								tdd2.getCustomerCount() };
						backwardDataCustomerCount.add(customerCount);
					} else {

						// Per Timeslot
						Object[] profit = { millis,
								tdd2.getDynamicData().getProfitDelta() };
						backwardDataProfit.add(profit);

						Object[] energy = { millis,
								tdd2.getDynamicData().getEnergyDelta() };
						backwardDataEnergy.add(energy);
						
						Object[] customerCount = { millis,
								tdd2.getCustomerCountDelta() };
						backwardDataCustomerCount.add(customerCount);
					}
				}
        	}
        	
        	allBackwardData.add(backwardDataProfit);
        	allBackwardData.add(backwardDataEnergy);
        	allBackwardData.add(backwardDataCustomerCount);
        }
        
        return gson.toJson(allBackwardData);
	}

	/**
	 * Create JSON tariff objects for forward jump.
	 * 
	 * @param startTimeslot Start time slot
	 * @param endTimeslot End time slot
	 */
	public String createTariffTransactionDatasJson(int starttimeslot, 
			int endtimeslot) {
	      
		Map<String, Integer> mapBrokerArrayAssign = logDAOImplExtended
				.getMapBrokerArrayAssign();
		TariffDynamicData[] tariffDynamicDatas = logDAOImplExtended
				.getTariffDynamicsData();
		List<String> brokerNames = logDAOImplExtended
				.getCompetition().getBrokers();
		
		int startNumberTimeslot = logDAOImplExtended.getStartNumberTimeslot();
		int numberOfBrokers = mapBrokerArrayAssign.size();
		int posIndex;
		int position;
		int startTimeslot = starttimeslot - startNumberTimeslot;
		int endTimeslot = endtimeslot - startNumberTimeslot;
		
        // tariff market push
		TariffDynamicData tdd;
		TariffMarketPusher pusher;
		ArrayList<TariffMarketPusher> tmPushers;
		ArrayList<ArrayList<TariffMarketPusher>> allTPushers = 
				new ArrayList<ArrayList<TariffMarketPusher>>();
		int listIndex;
		
		for (int ts = startTimeslot; ts <= endTimeslot; ts++) {

			tmPushers = new ArrayList<TariffMarketPusher>();
		    listIndex = 0;
		   
		    for (String brokerName : brokerNames) {
		    	
		    	posIndex = mapBrokerArrayAssign.get(brokerName);
		    	position = posIndex + numberOfBrokers * ts;
		    	
				if (tariffDynamicDatas[position] != null) {

					tdd = tariffDynamicDatas[position];
					pusher = new TariffMarketPusherReplayer("BrokerName",
							helper.getMillisForIndex(tdd.getDynamicData()
									.getTsIndex(), logDAOImplExtended.getCompetition()), 
									tdd.getDynamicData()
									.getProfit(), tdd.getDynamicData()
									.getEnergy(), tdd.getCustomerCount(), tdd
									.getDynamicData().getProfitDelta(), tdd
									.getDynamicData().getEnergyDelta(),
							tdd.getCustomerCountDelta(), listIndex);
					tmPushers.add(pusher);
				} else {
					
					int safeTimeslot = ts;
					position = posIndex + numberOfBrokers * safeTimeslot;
					
					TariffDynamicData dd = tariffDynamicDatas[position];
					
					while (safeTimeslot >= 0 && dd == null) {
						safeTimeslot--;
						if (safeTimeslot >= 0) {
							position = posIndex + numberOfBrokers * safeTimeslot;
							dd = tariffDynamicDatas[position];
						}
					}
					
					TariffDynamicData dd2 = (dd == null) 
							? new TariffDynamicData(new DynamicData(startNumberTs, 0, 0), 0, 0) 
							: dd;

					pusher = new TariffMarketPusherReplayer("BrokerName",
							helper.getMillisForIndex(dd2.getDynamicData()
									.getTsIndex(), logDAOImplExtended.getCompetition()), 
									 dd2.getDynamicData()
									.getProfit(), dd2.getDynamicData()
									.getEnergy(), dd2.getCustomerCount(), dd2
									.getDynamicData().getProfitDelta(), dd2
									.getDynamicData().getEnergyDelta(),
							dd2.getCustomerCountDelta(), listIndex);
					tmPushers.add(pusher);
				}
				
				listIndex += 3;
			}
			
			allTPushers.add(tmPushers);
		}
		
		return gson.toJson(allTPushers);
	}
	
	/**
	 * Push and create JSON objects for replayer-flow. It needs
	 * finance cumulative objects.
	 * 
	 * @param currentViewName Current view name
	 * @param channelname Channel name
	 * @param params Old and new time slot
	 */
	public void pushReplayer(String currentViewName,
			String channelname, int[] params) {
		
		pushFinance("financecumulative", channelname, params);
	}
	
	/**
	 * Push and create JSON objects for finance cumulative.
	 * 
	 * @param currentViewName Current view name
	 * @param channelname Channel name
	 * @param params Old and new time slot
	 */
	public void pushFinancecumulative(String currentViewName,
			String channelname, int[] params) {	
		pushFinance(currentViewName, channelname, params);
	}
	
	/**
	 * Push and create JSON objects for finance per timeslot.
	 * 
	 * @param currentViewName Current view name
	 * @param channelname Channel name
	 * @param params Old and new time slot
	 */
	public void pushFinancepertimeslot(String currentViewName,
			String channelname, int[] params) {
		
		pushFinance(currentViewName, channelname, params);
	}
	
	/**
	 * Push and create JSON objects for finance.
	 * 
	 * @param currentViewName Current view name
	 * @param channelname Channel name
	 * @param params Old and new time slot
	 */
	public void pushFinance(String currentViewName,
			String channelname, int[] params) {

		String json = null;

		if (!(params[0] >= params[1])) {

			// Forward-Jump.
			json = createFinanceDatasJson(params[0] + 1, params[1]);
			channelname = channelname.replaceAll(PLACEHOLDER_CHANNELNAME, currentViewName);
		} else {

			// Backward-Jump.
			json = createFinanceDatasBackwardJson(this.startNumberTs, 
					params[1], currentViewName);
			channelname = channelname.replaceAll(PLACEHOLDER_CHANNELNAME, 
					currentViewName + CHANNEL_BACKWARD);
		}

		push(channelname.toString(), json);
	}
	
	/**
	 * Create JSON finance objects for backward jump.
	 * 
	 * @param startTimeslot Start time slot
	 * @param endTimeslot End time slot
	 * @param currentViewName Current view name
	 */
	public String createFinanceDatasBackwardJson(int starttimeslot, 
			int endtimeslot, String currentViewName) {
				
		Map<String, Integer> mapBrokerArrayAssign = logDAOImplExtended
				.getMapBrokerArrayAssign();
		FinanceDynamicData[] financeDynamicDatas = logDAOImplExtended
				.getFinanceDynamicsData();
		List<String> brokerNames = logDAOImplExtended
				.getCompetition().getBrokers();

		int startNumberTimeslot = logDAOImplExtended.getStartNumberTimeslot();
		int numberOfBrokers = mapBrokerArrayAssign.size();
		int posIndex;
		int position;
		int startTimeslot = starttimeslot - startNumberTimeslot;
		int endTimeslot = endtimeslot - startNumberTimeslot;
		
		FinanceDynamicData financeDynamicData;
        long millis;
        
        ArrayList<Object> allBackwardData = new ArrayList<Object>();
        ArrayList<Object> backwardDataProfit;
        
        for (String brokerName : brokerNames) {

        	posIndex = mapBrokerArrayAssign.get(brokerName);
        	
        	backwardDataProfit = new ArrayList<Object>();
        	
        	for (int ts = startTimeslot; ts <= endTimeslot; ts++) {
        		
        		position = posIndex + numberOfBrokers * ts;
        				
				if (financeDynamicDatas[position] != null) { 
					
					financeDynamicData = financeDynamicDatas[position];
					
					millis = helper.getMillisForIndex(financeDynamicData
							.getTsIndex(), logDAOImplExtended
							.getCompetition());

					if (currentViewName.equals("financecumulative")) {
						
						// Kumuliert
						Object[] profit = { millis,
								financeDynamicData.getProfit() };
								backwardDataProfit.add(profit);
					} else {
						
						// Per Timeslot
						Object[] profit = { millis,
								financeDynamicData.getProfitDelta() };
								backwardDataProfit.add(profit);
					}
				} else {
					
					int safeTimeslot = ts;
					position = posIndex + numberOfBrokers * safeTimeslot;
					
					FinanceDynamicData fdd = financeDynamicDatas[position];
					
					while (safeTimeslot >= 0 && fdd == null) {
						
						safeTimeslot--;
						
						if (safeTimeslot >= 0) {
							
							position = posIndex + numberOfBrokers * safeTimeslot;
							fdd = financeDynamicDatas[position];
						}
					}

					FinanceDynamicData fdd2 = (fdd == null) 
							? new FinanceDynamicData(0, 0, 0, startNumberTs)
							: fdd;
							
							
				    millis = helper.getMillisForIndex(fdd2.getTsIndex(),
									logDAOImplExtended.getCompetition());

					if (currentViewName.equals("financecumulative")) {
						
						// Kumuliert
						Object[] profit = { millis, fdd2.getProfit() };
						backwardDataProfit.add(profit);

					} else {

						// Per Timeslot
						Object[] profit = { millis,
								fdd2.getProfitDelta() };
						backwardDataProfit.add(profit);

					}
				}
        	}
        	
        	allBackwardData.add(backwardDataProfit);
        }
        
        return gson.toJson(allBackwardData);
	}
	
	/**
	 * Create JSON finance objects for forward jump.
	 * 
	 * @param startTimeslot Start time slot
	 * @param endTimeslot End time slot
	 */
	public String createFinanceDatasJson(int starttimeslot, 
			int endtimeslot) {
		
		Map<String, Integer> mapBrokerArrayAssign = logDAOImplExtended
				.getMapBrokerArrayAssign();
		FinanceDynamicData[] financeDynamicDatas = logDAOImplExtended
				.getFinanceDynamicsData();
		List<String> brokerNames = logDAOImplExtended
				.getCompetition().getBrokers();
		
		int startNumberTimeslot = logDAOImplExtended.getStartNumberTimeslot();
		int numberOfBrokers = mapBrokerArrayAssign.size();
		int posIndex;
		int position;
		int startTimeslot = starttimeslot - startNumberTimeslot;
		int endTimeslot = endtimeslot - startNumberTimeslot;
		
		FinanceDynamicData financeDynamicData;
		FinancePusher pusher;
		ArrayList<FinancePusher> fPushers;
		ArrayList<ArrayList<FinancePusher>> allFPushers = 
				new ArrayList<ArrayList<FinancePusher>>();
		int listIndex;
		
		for (int ts = startTimeslot; ts <= endTimeslot; ts++) {

			fPushers = new ArrayList<FinancePusher>();
			listIndex = 0;
			
			for (String brokerName : brokerNames) {
				
		    	posIndex = mapBrokerArrayAssign.get(brokerName);
		    	position = posIndex + numberOfBrokers * ts;
		    	
				if (financeDynamicDatas[position] != null) {
					
					financeDynamicData = financeDynamicDatas[position];
					pusher = new FinancePusherReplayer("NAME_BROKER", 
							helper.getMillisForIndex(financeDynamicData.getTsIndex(),
									logDAOImplExtended.getCompetition()),
							financeDynamicData.getProfit(), 
							financeDynamicData.getProfitDelta(), listIndex);
					fPushers.add(pusher);
				} else {
					
					int safeTimeslot = ts;
					position = posIndex + numberOfBrokers * safeTimeslot;
					
					FinanceDynamicData dd = financeDynamicDatas[position];
					while (safeTimeslot >= 0 && dd == null) {
						safeTimeslot--;
						if (safeTimeslot >= 0) {
							position = posIndex + numberOfBrokers * safeTimeslot;
							dd = financeDynamicDatas[position];
						}
					}

					FinanceDynamicData dd2 = (dd == null) 
							? new FinanceDynamicData(0, 0, 0, startNumberTs) 
							: dd;

					pusher = new FinancePusherReplayer("NAME_BROKER", 
							helper.getMillisForIndex(dd2.getTsIndex(),
									logDAOImplExtended.getCompetition()),
							dd2.getProfit(), dd2.getProfitDelta(), listIndex);
					fPushers.add(pusher);
				}
				
				listIndex += 1;
			}
			
			allFPushers.add(fPushers);
		}
		
		return gson.toJson(allFPushers);
	}
	
	/**
	 * Push and create JSON objects for transaction summary (Game overview).
	 * 
	 * @param currentViewName Current view name
	 * @param channelname Channel name
	 * @param params Old and new time slot
	 */
	public void pushTransactionssummary(String currentViewName, 
			String channelname, int[] params) {

		// Need only current Timeslot
		String json = createGameOverviewJson(params[1]);
		
		channelname = channelname.replaceAll(PLACEHOLDER_CHANNELNAME, 
				currentViewName);
		
		push(channelname.toString(), json);
	}
	
	/**
	 * Create JSON transaction summary objects.
	 * 
	 * @param timeslot Time slot
	 */
	public String createGameOverviewJson(int timeslot) {

		// Need only current Timeslot
		Map<String, Integer> mapBrokerArrayAssign = 
				logDAOImplExtended.getMapBrokerArrayAssign();
		DynamicData[] dynamicDataDistribution = 
				logDAOImplExtended.getDistributionsData();
		TariffCategoryAttributes[] tariffCategoryAttributes = 
				logDAOImplExtended.getTariffCategoryAttributesData();
		WholesaleCategoryAttributes[] wholesaleCategoryAttributes = 
				logDAOImplExtended.getWholesaleCategoryAttributesData();
		BalancingCategoryAttributes[] balancingCategoryAttributes = 
				logDAOImplExtended.getBalancingCategoryAttributesData();
		GradingAttributes[] gradingAttributes = 
				logDAOImplExtended.getGradingAttributesData();

		List<String> brokerNames = logDAOImplExtended.getCompetition()
				.getBrokers();

		int startNumberTimeslot = logDAOImplExtended.getStartNumberTimeslot();
		int numberOfBrokers = mapBrokerArrayAssign.size();
		int posIndex;
		int position;
		int timeslotIndex = timeslot - startNumberTimeslot;
		
		ArrayList<ArrayList<Double>> allBrokersData = 
				new ArrayList<ArrayList<Double>>();
		
		for (String brokerName : brokerNames) {
			
			ArrayList<Double> data = new ArrayList<Double>();
			
			posIndex = mapBrokerArrayAssign.get(brokerName);
			position = posIndex + numberOfBrokers * timeslotIndex;
			
			if (gradingAttributes[timeslotIndex] != null
					&& tariffCategoryAttributes[position] != null
					&& wholesaleCategoryAttributes[position] != null
					&& balancingCategoryAttributes[position] != null) {

				GradingAttributes ga = gradingAttributes[timeslotIndex];
				TariffCategoryAttributes tca = tariffCategoryAttributes[position];
				data.add(ga.getTariffGrade(tca.getTotalMoneyFlow(),
						tca.getConsumptionConsumers(),
						tca.getTotalMoneyFromSold(),
						tca.getTotalBoughtEnergy(), tca.getTotalSoldEnergy(),
						tca.getCustomerCount(), tca.getLostCustomers()));

				WholesaleCategoryAttributes wca = wholesaleCategoryAttributes[position];
				data.add(ga.getWholesaleGrade(wca.getTotalRevenueFromSelling(),
						wca.getTotalCostFromBuying(),
						wca.getTotalEnergyBought(), wca.getTotalEnergySold()));

				BalancingCategoryAttributes bca = balancingCategoryAttributes[position];

				// LastDynamicData ersetzen
				double distributionEnergy = 0.0;
				DynamicData dd = dynamicDataDistribution[position];

				if (dd == null) {

					int safeTimeslot = timeslotIndex - 1;
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

				data.add(ga.getBalancingGrade(bca.getEnergy(),
						distributionEnergy, bca.getProfit()));

				data.add(ga.getDistributionGrade(distributionEnergy));
			}
			
		    allBrokersData.add(data);
		}
		
		return gson.toJson(allBrokersData);
	}
	
	/**
	 * Push and create JSON objects for ranking.
	 * 
	 * @param currentViewName Current view name
	 * @param channelname Channel name
	 * @param params Old and new time slot
	 */
	public void pushRanking(String currentViewName, 
			String channelname, int[] params) {

		// Need only current Timeslot
		String json = createRankingJson(params[1]);
		
		channelname = channelname.replaceAll(PLACEHOLDER_CHANNELNAME, 
				currentViewName);
		
		push(channelname.toString(), json);
	}
	
	/**
	 * Create JSON ranking objects.
	 * 
	 * @param timeslot Time slot
	 */
	public String createRankingJson(int timeslot) {

		Map<String, Integer> mapBrokerArrayAssign = 
				logDAOImplExtended.getMapBrokerArrayAssign();
		FinanceCategoryAttributes[] financeCategoryAttributes = 
				logDAOImplExtended.getFinanceCategoryAttributesData();
		TariffDynamicData[] tariffDynamicDatas = 
				logDAOImplExtended.getTariffDynamicsData();
		VisualizerBeanAttributes[] visualizerBeanAttributes = 
				logDAOImplExtended.getVisualizerBeanAttributesData();
		DynamicData[] dynamicDatas = 
				logDAOImplExtended.getMarketTransactionsData();
		List<String> brokerNames = logDAOImplExtended
				.getCompetition().getBrokers();
		Map<String, Integer> mapBrokerNamesIndex = 
				new HashMap<String, Integer>();
		
		int startNumberTimeslot = logDAOImplExtended.getStartNumberTimeslot();
		int numberOfBrokers = mapBrokerArrayAssign.size();
		int timeslotIndex = timeslot - startNumberTimeslot;
		int posIndex;
		int position;

		HashMap<String, Double> map = new HashMap<String, Double>();
		int i = 0;
		
		for (String brokerName : brokerNames) {
			
			posIndex = mapBrokerArrayAssign.get(brokerName);
			position = posIndex + numberOfBrokers * timeslotIndex;
			
			if (financeCategoryAttributes[position] != null) {
				
				map.put(brokerName, financeCategoryAttributes[position]
						.getProfit());
			} else {
				
				map.put(brokerName, 0.0);
			}

			mapBrokerNamesIndex.put(brokerName, i);
			i++;
		}

		ValueComparator bvc = new ValueComparator(map);
		TreeMap<String, Double> sorted_map = new TreeMap<String, Double>(bvc);
		sorted_map.putAll(map);
		ArrayList<Object> result = new ArrayList<Object>();

		for (int j = 0; j < sorted_map.size(); j++) {

			String brokerName = (String) sorted_map.keySet().toArray()[j];
			posIndex = mapBrokerArrayAssign.get(brokerName);
			position = posIndex + numberOfBrokers * timeslotIndex;

			TariffDynamicData tdd = tariffDynamicDatas[position];

			if (tdd == null) {
				
				int safeTimeslot = timeslotIndex - 1;
				position = posIndex + numberOfBrokers * safeTimeslot;

				tdd = tariffDynamicDatas[position];

				while (safeTimeslot >= 0 && tdd == null) {
					
					safeTimeslot--;
					
					if (safeTimeslot >= 0) {
						
						position = posIndex + numberOfBrokers * safeTimeslot;
						tdd = tariffDynamicDatas[position];
					}
				}

				if (tdd == null) {
					
					tdd = new TariffDynamicData(new DynamicData(startNumberTs,
							0, 0), 0, 0);
				}
			}
			
			int lastCompletedTimeslot = -1;
			
			if (visualizerBeanAttributes[timeslotIndex] != null) {
				
				lastCompletedTimeslot = visualizerBeanAttributes[timeslotIndex]
						.getTimeslotComplete().getTimeslotIndex();
			}

			int lastCompletedTimeslotIndexArray = lastCompletedTimeslot - 
					startNumberTimeslot;
			position = posIndex + numberOfBrokers * 
					lastCompletedTimeslotIndexArray;

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
						
						position = posIndex + numberOfBrokers * safeTimeslot;
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
					mapBrokerNamesIndex.get(sorted_map.keySet().toArray()[j]),
					sorted_map.values().toArray()[j],
					customerDelta,
					tdd.getDynamicData().getTsIndex(),
					tdd.getDynamicData().getEnergyDelta(),
					tdd.getDynamicData().getProfitDelta(),
					tdd.getDynamicData().getTsIndex() != lastCompletedTimeslot 
							? 0
							: tdd.getCustomerCountDelta(), profitDelta,
					energyDelta

			};

			result.add(pair);
		}
		
		return gson.toJson(result);
	}

	/**
	 * Value Comparator for createRankingJson.
	 * 
	 * @author DWietoska
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
	 * Push and create JSON objects for global message.
	 * 
	 * @param currentViewName Current view name
	 * @param channelname Channel name
	 * @param params Old and new time slot
	 */
	public void pushGlobalmessage(String currentViewName,
			String channelname, int[] params) {

		String json = createGlobalMessageJson(params[1]);
		
		channelname = channelname.replaceAll(PLACEHOLDER_CHANNELNAME, 
				CHANNEL_GLOBAL_MESSAGE);
		
		push(channelname.toString(), json);
	}
	
	/**
	 * Create JSON global objects.
	 * 
	 * @param timeslot Time slot
	 */
	public String createGlobalMessageJson(int timeslot) {
	
		String json = "";

		Map<String, Integer> mapBrokerArrayAssign = 
				logDAOImplExtended.getMapBrokerArrayAssign();
		WeatherReport[] weatherReportData = 
				logDAOImplExtended.getWeatherReportsData();
		TariffCategoryAttributes[] tariffCategoryAttributes = 
				logDAOImplExtended.getTariffCategoryAttributesData();
		FinanceCategoryAttributes[] financeCategoryAttributes = 
				logDAOImplExtended.getFinanceCategoryAttributesData();
		BalancingCategoryAttributes[] balancingCategoryAttributes = 
				logDAOImplExtended.getBalancingCategoryAttributesData();
		List<String> brokerNames = logDAOImplExtended
				.getCompetition().getBrokers();

		int startNumberTimeslot = logDAOImplExtended.getStartNumberTimeslot();
		int numberOfBrokers = mapBrokerArrayAssign.size();
		int posIndex;
		int position;
		int timeslotIndex = timeslot - startNumberTimeslot;
		
		WeatherReport weatherReport = weatherReportData[timeslotIndex];

		if (weatherReport != null) {

			WeatherPusher weatherPusher = new WeatherPusher(
					helper.getMillisForIndex(weatherReport.getCurrentTimeslot()
							.getSerialNumber(), logDAOImplExtended
							.getCompetition()), weatherReport.getTemperature(),
					weatherReport.getWindSpeed(),
					weatherReport.getWindDirection(),
					weatherReport.getCloudCover(),
					weatherReport.getTimeslotIndex());

			NominationPusher np = null;

			for (String brokerName : brokerNames) {

				posIndex = mapBrokerArrayAssign.get(brokerName);
				position = posIndex + numberOfBrokers * timeslotIndex;

				FinanceCategoryAttributes fca = 
						financeCategoryAttributes[position];
				TariffCategoryAttributes tca = 
						tariffCategoryAttributes[position];
				BalancingCategoryAttributes bca = 
						balancingCategoryAttributes[position];

				if (fca != null && tca != null && bca != null) {

					if (np == null) {

						np = new NominationPusher(new NominationCategoryPusher(
								brokerName, (long) fca.getProfit()),
								new NominationCategoryPusher(brokerName,
										(long) Math.abs(bca.getEnergy())),
								new NominationCategoryPusher(brokerName, tca
										.getCustomerCount()));
					} else {

						long profitAmount = (long) fca.getProfit();
						long balanceAmount = (long) Math.abs(bca.getEnergy());
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
				}
			}

			if (weatherPusher != null && np != null) {
				json = gson.toJson(new GlobalPusher(weatherPusher, np));
			}
		}
		return json;
	}
	
	/**
	 * Push and create JSON objects for slider.
	 * 
	 * @param currentViewName Current view name
	 * @param channelname Channel name
	 * @param params Old and new time slot
	 */
	public void pushSlider(String currentViewName,
			String channelname, int[] params) {

		String json = createSliderJson(params[1]);
		
		channelname = channelname.replaceAll(PLACEHOLDER_CHANNELNAME, 
				CHANNEL_SLIDER);
	
		push(channelname.toString(), json);
	}
	
	/**
	 * Create JSON slider object.
	 * 
	 * @param timeslot Time slot
	 */
	public String createSliderJson(int timeslot) {
		
		return gson.toJson(timeslot);
	}
	
	/**
	 * Push and create JSON objects for tariff analysis.
	 * 
	 * @param currentViewName Current view name
	 * @param channelname Channel name
	 * @param params Old and new time slot
	 */
	public void pushTariffanalysis(String currentViewName, 
			String channelname, int[] params) {

		// Do nothing, because if a dialog on tariffanalysis is open, a refresh
		// close this dialog immediately. 
		// Not wanted because this dialogue is only visible for a clock rate.
	}
	
	/**
	 * Push and create JSON objects for customer models.
	 * 
	 * @param currentViewName Current view name
	 * @param channelname Channel name
	 * @param params Old and new time slot
	 */
	public void pushCustomermodels(String currentViewName, 
			String channelname, int[] params) {
				
		String json = null;

		if (!(params[0] >= params[1])) {

			// Forward-Jump.
			json = createCustomerModelsDataJson(params[0] + 1, params[1]);
			channelname = channelname.replaceAll(PLACEHOLDER_CHANNELNAME, currentViewName);
		} else {
			
			// Backward-Jump.
			json = createCustomerModelsDataBackwardJson(this.startNumberTs, 
					params[1], currentViewName);
			channelname = channelname.replaceAll(PLACEHOLDER_CHANNELNAME, 
					currentViewName + CHANNEL_BACKWARD);
		}

		push(channelname.toString(), json);
	}

	/**
	 * Create JSON customer model objects for forward jump.
	 * 
	 * @param startTimeslot Start time slot
	 * @param endTimeslot End time slot
	 */
	public String createCustomerModelsDataJson(int starttimeslot,
			int endtimeslot) {

		CustomerModelBeanReplayer customerModelBeanReplayer = 
				(CustomerModelBeanReplayer) 
				VisualizerApplicationContext
				.getBean("customerModelBeanReplayer");
			
		long choosenChoosenCustomerInfoId = customerModelBeanReplayer
				.getChoosenCustomerInfoId();
		Gson gson = new Gson();

		List<TariffTransaction>[] listTariffTransaction = logDAOImplExtended
				.getListCustomerTariffTransaction();			
		Map<Long, Integer> mapCustomerArrayAssign = logDAOImplExtended
				.getMapCustomerArrayAssign();	

		int customerIndex = mapCustomerArrayAssign
				.get(choosenChoosenCustomerInfoId);
	
		List<TariffTransaction> listTariffTransactions = 
				listTariffTransaction[customerIndex];
		List<TariffTransaction> listTariffTransactionsRightTs = 
				new LinkedList<TariffTransaction>();

		if (listTariffTransactions != null) {
			
			for (TariffTransaction tx : listTariffTransactions) {
				
				if (tx.getPostedTimeslotIndex() >= starttimeslot
						&& tx.getPostedTimeslotIndex() <= endtimeslot) {
					if (tx.getTxType() == Type.CONSUME
							|| tx.getTxType() == Type.PRODUCE) {

						listTariffTransactionsRightTs.add(tx);
					}
				}
			}
		}

		ArrayList<CustomerModelPusher> cmPushers = 
				new ArrayList<CustomerModelPusher>();

		for (TariffTransaction tx : listTariffTransactionsRightTs) {

			CustomerModelPusher customerModelPusher = new CustomerModelPusher(
					helper.getMillisForIndex(tx.getPostedTimeslotIndex(),
							logDAOImplExtended.getCompetition()),
					tx.getCharge(), tx.getKWh());
			cmPushers.add(customerModelPusher);
		}

		return gson.toJson(cmPushers);
	}
	
	/**
	 * Create JSON customer model objects for backward jump.
	 * 
	 * @param startTimeslot Start time slot
	 * @param endTimeslot End time slot
	 * @param currentViewName Current view name
	 */
	public String createCustomerModelsDataBackwardJson(int starttimeslot,
			int endtimeslot, String currentViewName) {

		CustomerModelBeanReplayer customerModelBeanReplayer = 
				(CustomerModelBeanReplayer) 
				VisualizerApplicationContext
				.getBean("customerModelBeanReplayer");
		long choosenChoosenCustomerInfoId = customerModelBeanReplayer
				.getChoosenCustomerInfoId();

		Gson gson = new Gson();
		ArrayList<Object> allBackwardData = new ArrayList<Object>();
		ArrayList<Object> profitDataOneTimeslot = new ArrayList<Object>();
		ArrayList<Object> mwhDataOneTimeslot = new ArrayList<Object>();

		List<TariffTransaction>[] listTariffTransaction = logDAOImplExtended
				.getListCustomerTariffTransaction();
		Map<Long, Integer> mapCustomerArrayAssign = logDAOImplExtended
				.getMapCustomerArrayAssign();

		int customerIndex = mapCustomerArrayAssign
				.get(choosenChoosenCustomerInfoId);
		List<TariffTransaction> listTariffTransactions = 
				listTariffTransaction[customerIndex];

		if (listTariffTransactions != null) {
			
			for (TariffTransaction tx : listTariffTransactions) {

				if (tx.getPostedTimeslotIndex() <= endtimeslot) {
					
					if (tx.getTxType() == Type.CONSUME
							|| tx.getTxType() == Type.PRODUCE) {
			
						Object[] profitOneTimeslot = {
								helper.getMillisForIndex(
										tx.getPostedTimeslotIndex(),
										logDAOImplExtended.getCompetition()),
								tx.getCharge() };
						Object[] kWhOneTimeslot = {
								helper.getMillisForIndex(
										tx.getPostedTimeslotIndex(),
										logDAOImplExtended.getCompetition()),
								tx.getKWh() };

						profitDataOneTimeslot.add(profitOneTimeslot);
						mwhDataOneTimeslot.add(kWhOneTimeslot);
					}
				} else {
					break;
				}
			}
		}

		allBackwardData.add(profitDataOneTimeslot);
		allBackwardData.add(mwhDataOneTimeslot);

		return gson.toJson(allBackwardData);
	}
	
	/** Getter and Setter. */
	public int getStartNumberTs() {
		return startNumberTs;
	}

	public void setStartNumberTs(int startNumberTs) {
		this.startNumberTs = startNumberTs;
	}
}
