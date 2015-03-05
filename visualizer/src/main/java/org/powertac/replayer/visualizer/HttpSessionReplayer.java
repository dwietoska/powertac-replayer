package org.powertac.replayer.visualizer;

import java.io.File;

import javax.servlet.http.HttpSessionEvent;

import org.powertac.replayer.utils.Helper;
import org.springframework.security.web.session.HttpSessionEventPublisher;

/**
 * Represent all events for HttpSession.
 * 
 * @author DWietoska
 */
public class HttpSessionReplayer extends HttpSessionEventPublisher {

	/**
	 * Called when a session was created.
	 */
	@Override
	public void sessionCreated(HttpSessionEvent event) {
		super.sessionCreated(event);
	}

	/**
	 * Called when a session was destroyed.
	 */
	@Override
	public void sessionDestroyed(HttpSessionEvent event) {

		String sessionId = event.getSession().getId();
		File file = new File(Helper.PATH_LOG_FILES + sessionId
				+ LogParametersBean.END_OF_FILE_STATE);

		if (file.exists()) {
			file.delete();
		}

		super.sessionDestroyed(event);
	}
}
