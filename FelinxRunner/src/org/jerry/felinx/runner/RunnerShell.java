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
package org.jerry.felinx.runner;

import java.io.File;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.ReflectionException;

public class RunnerShell implements DynamicMBean {

	private FrameworkRunner runner = null;

	public RunnerShell() {
	}

	public void setFrameworkRunner(FrameworkRunner aFrameworkRunner) {
		this.runner = aFrameworkRunner;
	}

	@Override
	public Object getAttribute(String arg0) throws AttributeNotFoundException, MBeanException, ReflectionException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AttributeList getAttributes(String[] arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MBeanInfo getMBeanInfo() {
		// TODO Auto-generated method stub
		// http://weblogs.java.net/blog/emcmanus/archive/2006/11/a_real_example.html
		MBeanParameterInfo[] updateParams = {
				new MBeanParameterInfo("symbolicName", "String", "The symbolic name of the bundle to update"),
				new MBeanParameterInfo("version", "String", "The version of the bundle to update"),
				new MBeanParameterInfo("filePath", "String", "The full path to the new jar file of the bundle") };
		MBeanOperationInfo[] opers = {
				new MBeanOperationInfo("updateBundle", "update a Bundle with the content of a jar file", updateParams, "void",
						MBeanOperationInfo.ACTION),
				new MBeanOperationInfo("stopFramework", "stop the framework", null, "void", MBeanOperationInfo.ACTION) };
		return new MBeanInfo(this.getClass().getName(), "Bundle Manager", null, null, opers, null);
	}

	@Override
	public Object invoke(String actionName, Object[] params, String[] signature) throws MBeanException, ReflectionException {
		System.out.println("Invoking " + actionName);
		String result = "success";
		try {
			if ("stopFramework".equals(actionName)) {
				try {
					runner.stopFramework();
				} catch (Exception e) {
					System.err.println("Could not stop framework: " + e);
					e.printStackTrace();
				}
			} else if ("updateBundle".equals(actionName)) {
				runner.updateBundle((String) params[0], (String) params[1], new File((String) params[2]));
			} else {
				System.out.println("Received unknown command: "+actionName);
				result = "Unknown command: "+actionName;
			}
		} catch (Exception e) {
			result = "Could not invoke " + actionName + ": " + e.getMessage();
			System.err.println("Could not invoke " + actionName + ": " + e.getMessage());
			System.err.println("Parameters:");
			for (Object param : params) {
				System.err.println("  " + param);
			}
			System.err.println("Stack trace:");
			e.printStackTrace();
		}
		return result;
	}

	@Override
	public void setAttribute(Attribute arg0) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException,
			ReflectionException {
		// TODO Auto-generated method stub

	}

	@Override
	public AttributeList setAttributes(AttributeList arg0) {
		// TODO Auto-generated method stub
		return null;
	}
}
