package org.powertac.replayer.user;

import java.io.Serializable;
import org.springframework.stereotype.Service;
import org.springframework.webflow.context.ExternalContext;

/**
 * Called when the url /replayer was requested. This url can contains the
 * param ?logfile="".
 * 
 * @author DWietoska
 */
@Service
public class RequestUtil implements Serializable {

	/**
	 * serialVersionUID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Ascertain the param from the url. This param was saved in replayer 
	 * webflow.
	 * 
	 * @param name param name
	 * @param context ExternalContext
	 * @return param
	 */
	public String getRequestParameter(String name, ExternalContext context) {

		try {
			
			String urlString = context.getRequestParameterMap().get(name);
			return urlString;
		} catch (Exception e) {
			
			e.printStackTrace();
			return "";
		}
	}
}
