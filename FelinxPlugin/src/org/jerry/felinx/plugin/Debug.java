package org.jerry.felinx.plugin;

public class Debug {

public static boolean enabled=false;

	public static void message(String aMessage){
		if (enabled){
			System.out.println(aMessage);
		}
	}

}
