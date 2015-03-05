package org.powertac.replayer.logtool.message;

import java.util.HashMap;
import java.util.Map;

import org.powertac.common.CustomerInfo;
import org.powertac.replayer.GameInitialization;
import org.powertac.replayer.logtool.MethodsReplayerMessage;
import org.powertac.replayer.logtool.ReplayerMessage;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Creator for customer info entries.
 * 
 * @author DWietoska
 */
@Service("org.powertac.common.CustomerInfo")
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class CustomerInfoReplayerMessageImpl implements ReplayerMessage, 
		GameInitialization {
	
	/**
	 * Access to customer infos.
	 */
	private Map<Long, CustomerInfo> idMap;
	
	/**
	 * Default constructor.
	 */
	public CustomerInfoReplayerMessageImpl() {
		super();
		idMap = new HashMap<Long, CustomerInfo>();
	}
	
	/**
	 * Reset for new replaying.
	 */
	@Override
	public void newGame() {
		idMap = new HashMap<Long, CustomerInfo>();
	}

	/**
	 * Creates a new object.
	 * 
	 * @param id Id from object.
	 * @param args Arguments from log entry.
	 */
	@Override
	public Object createObject(long id, String[] args) throws Exception {

		CustomerInfo customerInfo = new CustomerInfo(args[3],
				Integer.parseInt(args[4]));

		ReflectionTestUtils.setField(customerInfo, "id", id);

		idMap.put(id, customerInfo);
		
		return customerInfo;
	}

	/**
	 * Calls a method on the given object.
	 * 
	 * @param id Id.
	 * @param methodname Method name to invoke.
	 * @param args Arguments from log entry.
	 */
	@Override
	public void callMethod(Object id, String methodname,
			String[] args) throws Exception {
		
		CustomerInfo customerInfo = idMap.get(id);
		
	    if (methodname.equals("withPowerType")) {
			customerInfo.withPowerType(MethodsReplayerMessage
					.getPowerType(args[3]));
		} else if (methodname.equals("setPopulation")) {
			
			customerInfo.setPopulation(Integer.parseInt(args[3]));
		} else if (methodname.equals("withMultiContracting")) {
			
			customerInfo.withMultiContracting(Boolean.parseBoolean(args[3]));
		} else if (methodname.equals("withCanNegotiate")) {
			
			customerInfo.withCanNegotiate(Boolean.parseBoolean(args[3]));
		} else if (methodname.equals("withControllableKW")) {
			
			customerInfo.withControllableKW(Double.parseDouble(args[3]));
		} else if (methodname.equals("withUpRegulationKW")) {
			
			customerInfo.withUpRegulationKW(Double.parseDouble(args[3]));
		} else if (methodname.equals("withDownRegulationKW")) {
			
			customerInfo.withDownRegulationKW(Double.parseDouble(args[3]));
		} else if (methodname.equals("withStorageCapacity")) {
			
			customerInfo.withStorageCapacity(Double.parseDouble(args[3]));
		}
	}
	
	/**
	 * Read resolve.
	 * 
	 * @param id Id from object.
	 * @param args Arguments from log entry.
	 */
	@Override
	public Object restoreObject(long id, String[] args) throws Exception {
		// CustomerInfo has no readResolve
		return null;
	}
	
	/**
	 * Get customer info.
	 * 
	 * @param id Id 
	 * @return Customer info
	 */
	public CustomerInfo getCustomerInfoMessage(long id) throws Exception {
		return idMap.get(id);
	}
}
