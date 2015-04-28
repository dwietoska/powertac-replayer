package org.powertac.replayer.logtool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.powertac.common.TimeService;
import org.powertac.du.DefaultBroker;
import org.powertac.logtool.common.NewObjectListener;
import org.powertac.replayer.GameInitialization;
import org.powertac.replayer.StrategyFactory;
import org.primefaces.push.PushContext;
import org.primefaces.push.PushContextFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;

/**
 * Processor for state log entries. Creates domain object instances.
 * 
 * The solution (org.powertac.logtool.common.DomainObjectReader) uses reflection,
 * which is too slow for replayer use case.
 * 
 * @author DWietoska
 */
@Service
//@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class ReplayerDomainObjectReader implements GameInitialization {

	/**
	 * Time Service.
	 */
	@Autowired
	private TimeService timeService;

	/**
	 * If-Implementors.
	 */
	HashMap<Class<?>, Class<?>> ifImplementors;
	
	/**
	 * Substitutes.
	 */
	HashMap<String, Class<?>> substitutes;
	
	/**
	 * Ignore classes.
	 */
	HashSet<String> ignores;

	/**
	 * Listener-Pattern. Old Solution.
	 */
	HashMap<Class<?>, ArrayList<NewObjectListener>> newObjectListeners;

	/**
	 * Strategie-Pattern.
	 */
	@Autowired
	private StrategyFactory runnerStrategyFactory;

	/**
	 * Listener-Pattern.
	 */
	HashMap<Class<?>, ArrayList<NewObjectListenerReplayer>> newObjectListenersReplayer;
	
	/**
	 * Default constructor
	 */
	public ReplayerDomainObjectReader() {
		super();

		sendInfinitiveNanMessage = false;
		
		// Set up the interface defaults
		ifImplementors = new HashMap<Class<?>, Class<?>>();
		ifImplementors.put(List.class, ArrayList.class);

		// set up substitute list to handle inner classes in a reasonable way
		substitutes = new HashMap<String, Class<?>>();
		substitutes.put("org.powertac.du.DefaultBrokerService$LocalBroker",
				DefaultBroker.class);

		// set up the ignore list
		ignores = new HashSet<String>();
		ignores.add("org.powertac.common.Tariff");
		ignores.add("org.powertac.common.Rate$ProbeCharge");
		ignores.add("org.powertac.common.msg.SimPause");
		ignores.add("org.powertac.common.msg.SimResume");
		ignores.add("org.powertac.common.msg.PauseRequest");
		ignores.add("org.powertac.common.msg.PauseRelease");
		ignores.add("org.powertac.common.MarketPosition");
		ignores.add("org.powertac.common.Orderbook");
		ignores.add("org.powertac.common.OrderbookOrder");
		ignores.add("org.powertac.common.WeatherForecastPrediction");

		// set up listener list
		newObjectListeners = new HashMap<Class<?>, ArrayList<NewObjectListener>>();
		newObjectListenersReplayer = new HashMap<Class<?>, 
				ArrayList<NewObjectListenerReplayer>>();
	}
	  
	/**
	 * Remove all object listeners.
	 */
	public void clearObjectListener() {
		this.newObjectListenersReplayer.clear();
		this.newObjectListeners.clear();
	}
	  
	private boolean sendInfinitiveNanMessage;
	
	/**
	 * A new replaying has been started.  
	 */
	@Override
	public void newGame() {
		sendInfinitiveNanMessage = false;
		clearObjectListener();
	}

	/**
	 * Converts a line from the log to an object. Each line is of the form<br>
	 * &nbsp;&nbsp;<code>ms:class::id::method{::arg}*</code>
	 * 
	 * @param line Line
	 */
	public Object readObject(String line) throws Exception {

	    String body = line.substring(line.indexOf(':') + 1);
	    String[] tokens = body.split("::");

	    if (ignores.contains(tokens[0])) {
	    	
	      return null;
	    }
	    
	    // Replace infinitive or nan entries.
	    for (int i = 0; i < tokens.length; i++) {
	    	if (tokens[i] != null && (
	    			tokens[i].toLowerCase().equals("infinity") || 
	    			tokens[i].toLowerCase().equals("-infinity") 
	    			)) { // ||  tokens[i].toLowerCase().equals("nan")

	    		tokens[i] = "0";
	    		
				if (!sendInfinitiveNanMessage) {
					
					PushContext pushContext = PushContextFactory.getDefault()
							.getPushContext();
					pushContext.push("infinitiveNanMessage", "");

					sendInfinitiveNanMessage = true;
				}
	    	}
	    }
	    
	    // Ascertain right message-class.
	    ReplayerMessage replayerMessage = null;
	    
	    try {
	    	
	     replayerMessage = runnerStrategyFactory
	    		.getReplayerMessage(tokens[0]);
	    } catch (Exception e) {	
	    	return null;
	    }

	    // Ascertain Id.
	    String methodName = tokens[2];
	    Long id = null;
	    
	    try {
	    	
	      id = Long.parseLong(tokens[1]);
	    } catch (NumberFormatException nfe) {
	    	
	    	Object newInst = null;
	    	
	    	// Id is null
			if (methodName.equals("new")) {

				newInst = replayerMessage.createObject(-1, tokens);
					
				fireNewObjectEventReplayer(newInst);

			} else if (methodName.equals("-rr")) {

				newInst = replayerMessage.restoreObject(-1, tokens);

				fireNewObjectEventReplayer(newInst);
			} else {

				newInst = replayerMessage.createObject(-1, tokens);

				fireNewObjectEventReplayer(newInst);
			}
			
			return newInst;
	    }

	    // Action (New, RR oder method call).
		if (methodName.equals("new")) {

			Object newInst = replayerMessage.createObject(id, tokens);

			fireNewObjectEventReplayer(newInst);
			return newInst;

		} else if (methodName.equals("-rr")) {

			// readResolve
			Object newInst = replayerMessage.restoreObject(id, tokens);

			fireNewObjectEventReplayer(newInst);

			return newInst;
		} else {
			// other method calls -- object should already exist
			replayerMessage.callMethod(id, methodName, tokens);
		}
	    
		return null;
	}
	  
	/**
	 * Registers a NewObjectListener. The listener will be called with each
	 * newly-created object of the given type. If type is null, then the
	 * listener will be called for each new object.
	 */
	public void registerNewObjectListenerReplayer(NewObjectListenerReplayer 
			listener, Class<?> type) {
	
		ArrayList<NewObjectListenerReplayer> list = newObjectListenersReplayer
				.get(type);
		
		if (null == list) {
			
			list = new ArrayList<NewObjectListenerReplayer>();
			newObjectListenersReplayer.put(type, list);
		}
		
		list.add(listener);
	}
	
	/**
	 * Fires a new created object to the listener.
	 * 
	 * @param thing New Object
	 * @throws Exception 
	 */
	public void fireNewObjectEventReplayer(Object thing) throws Exception {

		ArrayList<NewObjectListenerReplayer> listeners = 
				newObjectListenersReplayer.get(thing.getClass());
		
		if (null == listeners) {
			// try one up the tree to catch local subclasses like the default
			// broker
			listeners = newObjectListenersReplayer
					.get(thing.getClass().getSuperclass());
		}
		
		if (null != listeners) {
			
			for (NewObjectListenerReplayer li : listeners) {
				
				li.handleNewObject(thing);
			}
		}
		
		// check for promiscuous listener
		listeners = newObjectListenersReplayer.get(null);
		
		if (null != listeners) {
			
			for (NewObjectListenerReplayer li : listeners) {
				
				li.handleNewObject(thing);
			}
		}
	}
	
	/**
	 * Removes a NewObjectListener.
	 */
	public void removeObjectListenerReplayer(Class<?> type) {

		ArrayList<NewObjectListenerReplayer> list = newObjectListenersReplayer
				.get(type);
		
		if (null == list) {
			
			return;
		}
		
		list.clear();
	}
	
	/**
	 * Resolves arguments to his type in a list.
	 * 
	 * @param type Type
	 * @param args Argument
	 * @return List with new type
	 */
	public List<? extends Object> resolveListString(Class<?> type, 
			String args) {

		String body = args.substring(1, args.indexOf(')'));
		String[] items = body.split(",");

		if (type == String.class) {

			List<String> listArg = new ArrayList<String>();
			
			for (String item : items) {
				listArg.add(item);
			}

			return listArg;
		} else if (type == Long.class) {

			List<Long> listArg = new ArrayList<Long>();
			
			for (String item : items) {
				listArg.add(Long.parseLong(item));
			}

			return listArg;
		}

		return null;
	}
	
	/**
	 * Registers a NewObjectListener. The listener will be called with each
	 * newly-created object of the given type. If type is null, then the
	 * listener will be called for each new object.
	 */
	@Deprecated
	public void registerNewObjectListener(NewObjectListener listener,
			Class<?> type) {
	
		ArrayList<NewObjectListener> list = newObjectListeners.get(type);
		
		if (null == list) {
			
			list = new ArrayList<NewObjectListener>();
			newObjectListeners.put(type, list);
		}
		
		list.add(listener);
	}
	
	/**
	 * Removes a NewObjectListener.
	 */
	@Deprecated
	public void removeObjectListener(Class<?> type) {

		ArrayList<NewObjectListener> list = newObjectListeners.get(type);
		
		if (null == list) {
			
			return;
		}
		
		list.clear();
	}
	
	/**
	 * Fires a new created object to the listener.
	 * 
	 * @param thing New Object
	 */
	@Deprecated
	public void fireNewObjectEvent(Object thing) {

		ArrayList<NewObjectListener> listeners = newObjectListeners.get(thing
				.getClass());
		
		if (null == listeners) {
			// try one up the tree to catch local subclasses like the default
			// broker
			listeners = newObjectListeners
					.get(thing.getClass().getSuperclass());
		}
		
		if (null != listeners) {
			
			for (NewObjectListener li : listeners) {
				
				li.handleNewObject(thing);
			}
		}
		
		// check for promiscuous listener
		listeners = newObjectListeners.get(null);
		
		if (null != listeners) {
			
			for (NewObjectListener li : listeners) {
				
				li.handleNewObject(thing);
			}
		}
	}

	  /** Getter and Setter. */
	public HashMap<Class<?>, ArrayList<NewObjectListener>> getNewObjectListeners() {
		return newObjectListeners;
	}

	public void setNewObjectListeners(
			HashMap<Class<?>, ArrayList<NewObjectListener>> newObjectListeners) {
		this.newObjectListeners = newObjectListeners;
	}	  
}
