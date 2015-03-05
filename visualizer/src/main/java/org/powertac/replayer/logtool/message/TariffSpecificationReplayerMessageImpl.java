package org.powertac.replayer.logtool.message;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.Instant;
import org.powertac.common.Broker;
import org.powertac.common.RateCore;
import org.powertac.common.TariffSpecification;
import org.powertac.replayer.GameInitialization;
import org.powertac.replayer.logtool.MethodsReplayerMessage;
import org.powertac.replayer.logtool.ReplayerDomainObjectReader;
import org.powertac.replayer.logtool.ReplayerMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Creator for tariff specification entries.
 * 
 * @author DWietoska
 */
@Service("org.powertac.common.TariffSpecification")
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class TariffSpecificationReplayerMessageImpl implements ReplayerMessage, 
			 GameInitialization {

	/**
	 * Access to ReplayerDomainObjectReader.
	 */
	@Autowired
	private ReplayerDomainObjectReader replayerDomainObjectReader;
	
	/**
	 * Access to broker map.
	 */
	@Autowired
	private BrokerMap brokerMap;
	
	/**
	 * Contains all tariff specifications.
	 */
	private Map<Long, TariffSpecification> idMap;

	@Autowired
	private RateCoreMap rateCoreMap;
	
	/**
	 * Default constructor.
	 */
	public TariffSpecificationReplayerMessageImpl() {
		super();
		idMap = new HashMap<Long, TariffSpecification>();
	}
	
	/**
	 * Reset for new replaying.
	 */
	@Override
	public void newGame() {
		idMap = new HashMap<Long, TariffSpecification>();
	}
	
	/**
	 * Creates a new object.
	 * 
	 * @param id Id from object.
	 * @param args Arguments from log entry.
	 */
	@Override
	public Object createObject(long id, String[] args) throws Exception {

		Broker broker = null;

		if (!args[3].equals("null")) {
			
			broker = brokerMap.getBrokerMessage(Long
					.parseLong(args[3]));
		}
		
		TariffSpecification spec = new TariffSpecification(broker, 
				MethodsReplayerMessage.getPowerType(args[4]));
		
		ReflectionTestUtils.setField(spec, "id", id);
		
		idMap.put(id, spec);

		return spec;
	}

	/**
	 * Read resolve.
	 * 
	 * @param id Id from object.
	 * @param args Arguments from log entry.
	 */
	@Override
	public Object restoreObject(long id, String[] args) throws Exception {

		TariffSpecification spec = (TariffSpecification) 
				createObject(id, args);
		spec.withMinDuration(Long.parseLong(args[5]));
		spec.withSignupPayment(Double.parseDouble(args[6]));
		spec.withEarlyWithdrawPayment(Double.parseDouble(args[7]));
		spec.withPeriodicPayment(Double.parseDouble(args[8]));

		if (!args[9].equals("null")) {
			
			List<Long> entrys = (List<Long>) replayerDomainObjectReader
					.resolveListString(Long.class, args[9]);

			for (Long entry : entrys) {
				spec.addSupersedes(entry);
			}
		}
		
		return spec;
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

		TariffSpecification spec = idMap.get(id);

	    if (methodname.equals("addRate")) {

	    	RateCore rateCore = rateCoreMap.getRateCoreMessage(Long
	    			.parseLong(args[3]));
	    	spec.addRate(rateCore);
		} else if (methodname.equals("withExpiration")) {
			
			spec.withExpiration(Instant.parse(args[3]));
		} else if (methodname.equals("withMinDuration")) {
			
			spec.withMinDuration(Long.parseLong(args[3]));
		} else if (methodname.equals("withSignupPayment")) {
			
			spec.withSignupPayment(Double.parseDouble(args[3]));
		} else if (methodname.equals("withEarlyWithdrawPayment")) {
			
			spec.withEarlyWithdrawPayment(Double.parseDouble(args[3]));
		} else if (methodname.equals("withPeriodicPayment")) {
			
			spec.withPeriodicPayment(Double.parseDouble(args[3]));
		} else if (methodname.equals("addSupersedes")) {
			
			if (args[3] != null) {
				spec.addSupersedes(Long.parseLong(args[3]));
			}
		}
	}
	
	/**
	 * Getter tariff specification.
	 * 
	 * @param id Id
	 * @return Tariff specification
	 */
	public TariffSpecification getTariffSpecificationMessage(long id) 
			throws Exception {
		return idMap.get(id);
	}
}
