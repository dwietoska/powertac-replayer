package org.powertac.replayer.data;

import org.powertac.common.TariffTransaction;

/**
 * Helper methods.
 * 
 * Copied from visualizer.
 * 
 * @author Jurica Babic
 */
public class HelperReplayer {

	/**
	 * Returns the number of customer.
	 * 
	 * @param tariffTransaction Tariff-Transaction message
	 * @return positive number for new customers, negative number for ex
	 *         customers. Otherwise, zero is returned.
	 */
	public static int getCustomerCount(TariffTransaction tariffTransaction) {

		switch (tariffTransaction.getTxType()) {
		case SIGNUP:
			return tariffTransaction.getCustomerCount();
		case REVOKE:
		case WITHDRAW:
			return (-1) * tariffTransaction.getCustomerCount();
		case CONSUME:
		case PERIODIC:
		case PRODUCE:
		case PUBLISH:
		default:
			return 0;
		}
	}
}
