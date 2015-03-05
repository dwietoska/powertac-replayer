package org.powertac.replayer.logtool;

/**
 * Contains methods which every message needs for parsing log
 * files.
 * 
 * @author DWietoska
 */
public interface ReplayerMessage {

	public Object createObject(long id, String[] args) throws Exception;
	public Object restoreObject(long id, String[] args) throws Exception;
	public void callMethod(Object id, String methodname, String[] args) throws Exception;
}
