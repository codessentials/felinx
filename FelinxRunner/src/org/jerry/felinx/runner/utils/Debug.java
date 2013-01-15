package org.jerry.felinx.runner.utils;

public class Debug {

public static boolean enabled=false;

	public static void message(String aMessage){
		if (enabled){
			System.out.println(aMessage);
		}
	}

}
