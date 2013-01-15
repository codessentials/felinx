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
package org.jerry.felinx.plugin.launch;

import java.io.File;
import java.net.SocketException;
import java.rmi.UnmarshalException;
import java.util.Hashtable;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.apache.felix.main.Main;

public class JMXClient {

	private MBeanServerConnection mbsc = null;
	private JMXConnector jmxc = null;
	private String address;
	private int port;
	private boolean connected = false;

	public JMXClient(final String aAddress, final int aPort) {
		address = aAddress;
		port = aPort;
	}

	public void connect() {
		try {
			JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + address + ":" + port + "/jmxrmi");
			Hashtable<String, String[]> env = new Hashtable<String, String[]>();
			jmxc = JMXConnectorFactory.connect(url, env);
			mbsc = jmxc.getMBeanServerConnection();
			connected = true;
			System.out.println("Connected to JMX service of OSGi Framework runner.");
		} catch (Exception e) {
			System.err.println("Could not connect to JMX service at " + address + ":" + port + " : " + e.getMessage());
			e.printStackTrace();
		}
	}

	public void disconnect() {
		try {
			if (connected) {
				jmxc.close();
				connected = false;
			}
		} catch (Exception e) {
			if ("Connection reset".equals(e.getMessage()) || (e.getCause() != null && "Connection reset".equals(e.getCause().getMessage()))) {
				// this is as expected, we have just stopped the framework and thus also the JMX service.
				connected = false;
				System.out.println("Framework is stopped.");
			} else {
				System.err.println(">Could not disconnect from JMX service " + e.getMessage());
				e.printStackTrace();
			}
		}
	}

	public void stopFramework() {
		try {
			ObjectName shellObjectName = new ObjectName("org.jerry.runner.shell:type=RunnerShell,name=shell");
			Object response = mbsc.invoke(shellObjectName, "stopFramework", null, null);
		} catch (Exception e) {
			if ("Connection reset".equals(e.getMessage()) || (e.getCause() != null && "Connection reset".equals(e.getCause().getMessage()))) {
				// this is as expected, we have just stopped the framework and thus also the JMX service.
				connected = false;
				System.out.println("Framework is stopped.");
			} else {
				System.err.println(">Could not disconnect from JMX service " + e.getMessage());
				e.printStackTrace();
			}
		}
	}

	public void updateBundle(String aSymbolicName, String aVersion, File aBundleToInstall) {
		try {
			ObjectName shellObjectName = new ObjectName("org.jerry.runner.shell:type=RunnerShell,name=shell");
			String[] params = new String[]{aSymbolicName, aVersion, aBundleToInstall.getAbsolutePath()};
			Object response = mbsc.invoke(shellObjectName, "updateBundle", params, null);
			System.out.println(response);
		} catch (Exception e) {
			System.err.println(">Could not update bundle " + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		JMXClient client = new JMXClient("localhost", 9999);
		client.connect();
		client.updateBundle("blabla", "0.1", new File("bestaatniet"));
		client.disconnect();
	}

}
