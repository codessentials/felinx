package org.jerry.felinx.plugin.builder;

public class ActionResult {

	private Throwable error = null;
	private String message = null;
	private String actionDescription = null;

	/**
	 * 
	 * @param aActionDescription
	 *            description of what is currently being done.
	 */
	public ActionResult(String aActionDescription) {
		actionDescription = aActionDescription;
	}

	public Throwable getError() {
		return error;
	}

	public void setError(Throwable error) {
		this.error = error;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void setError(String message) {
		this.error = new RuntimeException(message);
	}

	public void setError(String message, Exception e) {
		this.error = new RuntimeException(message, e);
	}

	public boolean succeeded() {
		return error == null;
	}

	public String getActionDescription() {
		return actionDescription;
	}

}

