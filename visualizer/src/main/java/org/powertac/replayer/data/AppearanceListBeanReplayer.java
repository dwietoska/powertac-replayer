package org.powertac.replayer.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.powertac.visualizer.domain.Appearance;

/**
 * Holds the list of appearances. Such list should be declared in xml
 * declaration file.
 * 
 * Copied for replayer because appearances needs every session object.
 * 
 * @author Jurica Babic
 * 
 */
public class AppearanceListBeanReplayer implements Serializable {

	private static final long serialVersionUID = 1L;
	private List<Appearance> appereanceList;
	private List<Boolean> availableList;
	private Appearance defaultAppearance;

	public AppearanceListBeanReplayer(List<Appearance> appereanceList,
			Appearance defaultAppearance) {

		this.appereanceList = appereanceList;
		this.defaultAppearance = defaultAppearance;
		resetAvailableList();

	}

	public void resetAvailableList() {
		availableList = new ArrayList<Boolean>(appereanceList.size());
		for (int i = 0; i < appereanceList.size(); i++) {
			availableList.add(true);
		}

	}

	public Appearance getAppereance() {

		Appearance appearance;
		int appereanceIndex = availableList.indexOf(true);

		if (appereanceIndex == -1) {
			appearance = defaultAppearance;
		} else {
			appearance = appereanceList.get(appereanceIndex);
			availableList.set(appereanceIndex, false);
		}
		return appearance;
	}
}
