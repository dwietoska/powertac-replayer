package org.powertac.replayer.logtool.message;

import org.powertac.common.Broker;
import org.powertac.common.CustomerInfo;
import org.powertac.common.TariffSpecification;
import org.powertac.common.TariffTransaction;
import org.powertac.replayer.logtool.MethodsReplayerMessage;
import org.powertac.replayer.logtool.ReplayerMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Creator for tariff transaction entries.
 * 
 * @author DWietoska
 */
@Service("org.powertac.common.TariffTransaction")
//@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class TariffTransactionReplayerMessageImpl implements ReplayerMessage {
	
	/**
	 * Access to broker map.
	 */
	@Autowired
	private BrokerMap brokerMap;
	
	/**
	 * Access to customer info.
	 */
	@Autowired
	private CustomerInfoReplayerMessageImpl customerInfoReplayerMessageImpl;
	
	/**
	 * Access to tariff specification.
	 */
	@Autowired
	private TariffSpecificationReplayerMessageImpl tariffSpecificationReplayerMessageImpl;

	/**
	 * Creates a new object.
	 * 
	 * @param id Id from object.
	 * @param args Arguments from log entry.
	 */
	@Override
	public Object createObject(long id, String[] args) throws Exception {

		CustomerInfo customerInfo = null;
		Broker broker = null;
		TariffSpecification spec = null;
		
		if (!args[3].equals("null")) {

			broker = brokerMap.getBrokerMessage(Long.parseLong(args[3]));
		}

		if (!args[6].equals("null")) {

			spec = tariffSpecificationReplayerMessageImpl
					.getTariffSpecificationMessage(Long.parseLong(args[6]));
		}

		if (!args[7].equals("null")) {

	    	customerInfo = customerInfoReplayerMessageImpl
	    			.getCustomerInfoMessage(Long.parseLong(args[7]));
		}

		TariffTransaction tariffTransaction = new TariffTransaction(broker,
				Integer.parseInt(args[4]),
				MethodsReplayerMessage.getType(args[5]), spec, customerInfo,
				Integer.parseInt(args[8]), Double.parseDouble(args[9]),
				Double.parseDouble(args[10]));

		ReflectionTestUtils.setField(tariffTransaction, "id", id);
		
		return tariffTransaction;
	}
	
	/**
	 * Read resolve.
	 * 
	 * @param id Id from object.
	 * @param args Arguments from log entry.
	 */
	@Override
	public Object restoreObject(long id, String[] args) throws Exception {
		return null;
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
	}
}
