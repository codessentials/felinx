package org.jerry.felinx.plugin.launch;

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
