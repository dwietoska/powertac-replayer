package org.powertac.replayer.utils;

/**
 * Enums for replayer-mode.
 * 
 * @author DWietoska
 */
public enum Mode {

	NORMAL {
		public String toString() {
			return "RunnerNormal";
		}
	},
	EXTENTED {
		public String toString() {
			return "RunnerExtended";
		}
	}
}
