package org.powertac.replayer.logtool;

import org.powertac.common.TariffTransaction.Type;
import org.powertac.common.enumerations.PowerType;

/**
 * Replaced through spring factory.
 * 
 * @author DWietoska
 */
public class MethodsReplayerMessage {
	
	/**
	 * Get Power Type.
	 * 
	 * @param powerTypename The Name of power type
	 * @return The Power type
	 */
	public static PowerType getPowerType(String powerTypename) {

		PowerType powerType = null;

		if (powerTypename.equals("CONSUMPTION")) {
			
			powerType = PowerType.CONSUMPTION;
		} else if (powerTypename.equals("PRODUCTION")) {
			
			powerType = PowerType.PRODUCTION;
		} else if (powerTypename.equals("INTERRUPTIBLE_CONSUMPTION")) {
			
			powerType = PowerType.INTERRUPTIBLE_CONSUMPTION;
		} else if (powerTypename.equals("THERMAL_STORAGE_CONSUMPTION")) {
			
			powerType = PowerType.THERMAL_STORAGE_CONSUMPTION;
		} else if (powerTypename.equals("SOLAR_PRODUCTION")) {
			
			powerType = PowerType.SOLAR_PRODUCTION;
		} else if (powerTypename.equals("WIND_PRODUCTION")) {
			
			powerType = PowerType.WIND_PRODUCTION;
		} else if (powerTypename.equals("RUN_OF_RIVER_PRODUCTION")) {
			
			powerType = PowerType.RUN_OF_RIVER_PRODUCTION;
		} else if (powerTypename.equals("PUMPED_STORAGE_PRODUCTION")) {
			
			powerType = PowerType.PUMPED_STORAGE_PRODUCTION;
		} else if (powerTypename.equals("CHP_PRODUCTION")) {
			
			powerType = PowerType.CHP_PRODUCTION;
		} else if (powerTypename.equals("FOSSIL_PRODUCTION")) {
			
			powerType = PowerType.FOSSIL_PRODUCTION;
		} else if (powerTypename.equals("BATTERY_STORAGE")) {
			
			powerType = PowerType.BATTERY_STORAGE;
		} else if (powerTypename.equals("ELECTRIC_VEHICLE")) {
			
			powerType = PowerType.ELECTRIC_VEHICLE;
		}

		return powerType;
	}
	
	/**
	 * Get Type.
	 * 
	 * @param typename The Name of type
	 * @return The type
	 */
	public static Type getType(String typename) {

		Type type = null;

		if (typename.equals("PUBLISH")) {
			
			type = Type.PUBLISH;
		} else if (typename.equals("PRODUCE")) {
			
			type = Type.PRODUCE;
		} else if (typename.equals("CONSUME")) {
			
			type = Type.CONSUME;
		} else if (typename.equals("PERIODIC")) {
			
			type = Type.PERIODIC;
		} else if (typename.equals("SIGNUP")) {
			
			type = Type.SIGNUP;
		} else if (typename.equals("WITHDRAW")) {
			
			type = Type.WITHDRAW;
		} else if (typename.equals("REVOKE")) {
			
			type = Type.REVOKE;
		} 

		return type;
	}
}
