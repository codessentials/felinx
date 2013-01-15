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
package org.jerry.felinx.runner.utils;

import java.lang.reflect.Method;

public class LaunchUtils {

	public LaunchUtils() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * Gets the first method that matches the specified name.
	 * @param aClass
	 * @param aMethodName
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static Method getFirstMethod(Class aClass, String aMethodName) {
		Method[] methods = aClass.getDeclaredMethods();
		Method createSessionMethod = null;
		for (Method method : methods) {
			//System.out.println(method.getName() + " " + method.getParameterTypes());
			if (method.getName().equals(aMethodName)) {
				createSessionMethod = method;
			}
			if (createSessionMethod!=null){
				for (Class paramClass : createSessionMethod.getParameterTypes()) {
					//System.out.println("  - " + paramClass.getName());
				}
				break;
			}
		}
		return createSessionMethod;
	}
	
	

}
