package org.powertac.replayer.user;

import org.springframework.webflow.conversation.impl.SessionBindingConversationManager;
import org.springframework.webflow.execution.repository.impl.DefaultFlowExecutionRepository;
import org.springframework.webflow.executor.FlowExecutor;
import org.springframework.webflow.executor.FlowExecutorImpl;

/**
 * Sets the Lock Timeout in Spring WebFlow.
 */
public class WebFlowHelper {

	/**
	 * Flow executor.
	 */
	private FlowExecutor flowExecutor;

	/**
	 * Create new WebFlowHelper.
	 * 
	 * @param flowExecutor Flow executor
	 */
	public WebFlowHelper(FlowExecutor flowExecutor) {
		this.flowExecutor = flowExecutor;
	}
	
	/**
	 * Getter flowExecutor.
	 */
	public FlowExecutor getFlowExecutor() {
		return flowExecutor;
	}
	
	/**
	 * Getter ConversationManager.
	 * 
	 * @return SessionBindingConversationManager
	 */
	public SessionBindingConversationManager getConversationManager() {
		return ((SessionBindingConversationManager) 
				((DefaultFlowExecutionRepository) 
						((FlowExecutorImpl) flowExecutor)
						.getExecutionRepository()).getConversationManager());
	}

	/**
	 * Getter Lock timeout.
	 * 
	 * @return Lock timeout
	 */
	public int getLockTimeoutSeconds() {
		return getConversationManager().getLockTimeoutSeconds();
	}

	/**
	 * Setter Lock timeouts
	 * 
	 * @param lockTimeoutSeconds Lock timeout
	 */
	public void setLockTimeoutSeconds(int lockTimeoutSeconds) {
		getConversationManager().setLockTimeoutSeconds(lockTimeoutSeconds);
	}
}
