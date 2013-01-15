/**
 *  Felinx - Integration link between Felix and Eclipse
    Copyright (C) 2013  Michiel Vermandel

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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

