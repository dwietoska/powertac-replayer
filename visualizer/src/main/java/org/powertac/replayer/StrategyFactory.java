package org.powertac.replayer;

import org.powertac.replayer.logtool.ReplayerMessage;

/**
 * Strategy-Pattern for Spring.
 */
public interface StrategyFactory {
	
	/**
	 * Return the given strategy (normal or extended).
	 * 
	 * @param strategyName The name of the strategy which is to be returned.
	 * @return The strategy
	 */
	Runner getStrategy(String strategyName);
	
	/**
	 * Return the domain object strategy.
	 * 
	 * @param name The name of the strategy which is to be returned.
	 * @return The strategy
	 */
	ReplayerMessage getReplayerMessage(String name);
}
