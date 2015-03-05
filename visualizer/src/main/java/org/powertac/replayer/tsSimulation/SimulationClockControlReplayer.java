package org.powertac.replayer.tsSimulation;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Determines and executes the next tick.
 * 
 * Modified from server.
 * 
 * @author DWietoska
 */
public class SimulationClockControlReplayer {

	/**
	 * Execution time.
	 */
    private long executionTime;
    
    /**
     * Executes the next tick.
     */
    private Timer theTimer = new Timer();
    
    /**
     * Next tick.
     */
    private int nextTick;
    
    /**
     * Clock rate.
     */
    private long tickInterval;
    
    /**
     * Create new SimulationClockControl
     * 
     * @param nextTick The next time slot.
     */
    private SimulationClockControlReplayer(int nextTick) {
    	
		super();
		this.nextTick = nextTick - 1;
	}
    
    /**
     * Returns a new instance.
     *
     * @return The new instance
     */
    public static SimulationClockControlReplayer getInstance(int nextTick) {
    	
    	return new SimulationClockControlReplayer(nextTick);
    }

    /**
     * Schedules the next tick. The interval between now and the next tick is
     * determined by comparing the current system time with what the time 
     * should be on the next tick.
     */
    public void scheduleTick() {

        long nextTick = computeNextTickTime();
        boolean success = false;
        
        while (!success) {
            
            try {

                theTimer.schedule(new TickAction(this), nextTick);
                success = true;
            } catch (IllegalStateException ise) {

                theTimer = new Timer();
            }
        }
    }

    /**
     * Calculates the next tick.
     * 
     * @return Next tick
     */
    private long computeNextTickTime() {

        long nextTick;
        
        if (executionTime < tickInterval) {

            nextTick = tickInterval - executionTime;
        } else {

            nextTick = 0;
        }

        return nextTick;
    }

    /**
     * Indicates that the simulator has completed its work on the current
     * timeslot. If the sim was delayed, then resume it. On the last tick,
     * client must call stop() rather than complete().
     */
    public synchronized void complete() {
    	
        scheduleTick();
    }
    
    /**
     * Stops the timer.
     */
    public synchronized void cancelTimer() {
    	
		 theTimer.cancel();
	}

	/**
     * Blocks the caller until the next tick.
     */
    public synchronized void waitForTick(int n) {

        while (nextTick < n) {
        	
            try {
            	
                this.wait();
            } catch (InterruptedException ie) {
            }
        }
    }

    /**
     * notifies the waiting thread (if any).
     */
    public synchronized void notifyTick() {
    	
        nextTick += 1;
        this.notifyAll();
    }

	/**
	 * Runs a tick.
	 * 
	 * Modified from server.
	 * 
	 * @author DWietoska
	 */
	private class TickAction extends TimerTask {

		/**
		 * SimulationClockControlReplayer.
		 */
        SimulationClockControlReplayer scc;

        /**
         * Create Tick-Action.
         * @param scc SimulationClockControlReplayer
         */
        TickAction(SimulationClockControlReplayer scc) {
            super();
            this.scc = scc;
        }

        /**
         * Runs a tick - updates the timeService, clears the state, schedules
         * the watchdog, and notifies the simulator. The monitor object is the
         * singleton.
         */
        @Override
        public void run() {
        	
            scc.notifyTick();
        }
    }
	
    public void setExecutionTime(long executionTime) {
        this.executionTime = executionTime;
    }

    public long getTickInterval() {
        return tickInterval;
    }

    public void setTickInterval(long tickInterval) {
        this.tickInterval = tickInterval;
    }
    
    public int getNextTick() {
		return nextTick;
	}

	public void setNextTick(int nextTick) {
		this.nextTick = nextTick;
	}
}