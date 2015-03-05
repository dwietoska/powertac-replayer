package org.powertac.replayer.logtool;

/**
 * Object Listener for logfile entries.
 * 
 * @author DWietoska
 */
public interface NewObjectListenerReplayer {
	public void handleNewObject (Object thing) throws Exception;
}
