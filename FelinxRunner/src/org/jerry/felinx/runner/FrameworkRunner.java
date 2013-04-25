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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.jerry.felinx.runner.utils.Debug;
import org.jerry.felinx.runner.utils.LaunchConfigurator;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;

import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;

public class FrameworkRunner {

	private Framework framework = null;
	private static String frameworkLocation = null;

	public FrameworkRunner() {
	}

	/**
	 * @param args
	 *            This main is called from org.jerry.felinx.plugin.launch.Launcher
	 */
	public static void main(String[] args) {
		System.out.println("FrameworkRunner v1.0.0.SNAPSHOT");
		MBeanServer server = ManagementFactory.getPlatformMBeanServer();
		RunnerShell shell = new RunnerShell();
		ObjectName shellObjectName;
		try {
			shellObjectName = new ObjectName("org.jerry.runner.shell:type=RunnerShell,name=shell");
			server.registerMBean(shell, shellObjectName);
			System.out.println("JMX Shell created.");
			// System.out.println(loadJMXAgent(9999));
		} catch (Exception e) {
			e.printStackTrace();
		}

		Debug.enabled = true;
		frameworkLocation = System.getenv("FELIX_HOME");
		for(String arg : args){
			System.out.println(arg);
			if (arg.startsWith("FELIX_HOME=")){
				// %20 => spaces get replaced by %20 when passing program arguments
				frameworkLocation = arg.substring(11).replace("%20", " ");
			}
		}
		if (frameworkLocation == null) {
			System.out.println("Environment variable FELIX_HOME not set!");
			frameworkLocation = "C:\\Data\\tools\\felix-framework-4.0.3 - FlowBeans";
		}
		System.out.println("Felinx FrameworkRunner: FELIX HOME = " + frameworkLocation);
		boolean isDebug = java.lang.management.ManagementFactory.getRuntimeMXBean().getInputArguments().toString()
				.indexOf("-agentlib:jdwp") > 0;
		System.out.println("Debug: " + isDebug);
		FrameworkRunner runner = new FrameworkRunner();
		runner.startFramework();
		shell.setFrameworkRunner(runner);
	}

	/**
	 * @param aFrameworkLocation
	 *            : root of OSGI framework.
	 */
	public void startFramework() {
		try {
			Map<String, String> config = new HashMap<String, String>();
			// LOAD CONFIGURATION FROM FILE:
			// http://grepcode.com/file/repo1.maven.org/maven2/org.apache.felix/org.apache.felix.main/2.0.3/org/apache/felix/main/Main.java#Main.loadConfigProperties%28%29
			config = LaunchConfigurator.loadProperties(frameworkLocation);
			config.put("felix.cache.rootdir", frameworkLocation);
			config.put("felix.auto.deploy.dir", "${felix.cache.rootdir}/bundle");
			config.put("felix.fileinstall.dir", "${felix.cache.rootdir}/plugins");
			framework = getFrameworkFactory().newFramework(config);
			if (framework != null) {
				System.out.println("Starting FelixServer from " + frameworkLocation);
				System.out.println("\nFelixServer...");
				System.out.println("======================\n");
				framework.init();
				framework.start();
				// CommandProcessor cmdprc = new CommandProcessor(framework.getBundleContext());
				// cmdprc.start();
				//check if gogo is geïnstalleerd
				boolean gogoShellInstalled=false;
				for (Bundle bundle: framework.getBundleContext().getBundles()){
					if ("org.apache.felix.gogo.shell".equals(bundle.getSymbolicName())){
						gogoShellInstalled=true;
					}
				}
				if (!gogoShellInstalled){
					System.out.println("WARNING: gogo shell is not installed, cannot handle commands.");
				}
			} else {
				System.err.println("Could not create framework");
			}
		} catch (Exception ex) {
			System.err.println("Could not create framework: " + ex);
			ex.printStackTrace();
		}
	}

	public void stopFramework() {
		System.out.println("Stopping OSGi Framework...");
		try {
			framework.stop();
			framework.waitForStop(0);
			System.out.println("FelixServer is stopped.");
		} catch (Exception e) {
			System.err.println("Could not stop framework: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private static FrameworkFactory getFrameworkFactory() throws Exception {
		java.net.URL url = FrameworkRunner.class.getClassLoader().getResource(
				"META-INF/services/org.osgi.framework.launch.FrameworkFactory");
		if (url != null) {
			BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
			try {
				for (String s = br.readLine(); s != null; s = br.readLine()) {
					s = s.trim();
					// Try to load first non-empty, non-commented line.
					if ((s.length() > 0) && (s.charAt(0) != '#')) {
						Debug.message("> FrameworkFactory class name: " + s);
						return (FrameworkFactory) Class.forName(s).newInstance();
					}
				}
			} finally {
				if (br != null)
					br.close();
			}
		}

		throw new Exception("Could not find framework factory.");
	}

	public void updateBundle(String aSymbolicName, String aVersion, File aBundleToInstall) throws FileNotFoundException, BundleException {
		System.out.println("Updating " + aSymbolicName + "... ");
		/** find bundle */
		Bundle bundle = null;
		if (framework == null || framework.getState() != Bundle.ACTIVE) {
			System.out.println("Framework not running...");
		} else {
			Bundle[] bundles = framework.getBundleContext().getBundles();
			for (Bundle check : bundles) {
				if (check.getSymbolicName().equals(aSymbolicName) && check.getVersion().toString().equals(aVersion)) {
					bundle = check;
					break;
				}
			}
			InputStream stream = new FileInputStream(aBundleToInstall);
			if (bundle == null) {
				System.out.println("Bundle does not exist yet. Will install " + aSymbolicName + " " + aVersion + " ["+aBundleToInstall.getAbsolutePath()+"]");
				bundle = framework.getBundleContext().installBundle(aBundleToInstall.getAbsolutePath(), stream);
			} else {
				bundle.stop();
				bundle.update(stream);
			}
			System.out.println("Starting " + aSymbolicName + "... ");
			bundle.start();
		}
	}

	public static void setConsole(PrintStream ps) {
		// http://stackoverflow.com/questions/7573188/how-to-capture-console-output-of-eclipse-plugin-with-custom-launch-configuration
		System.setOut(ps);
		System.setErr(ps);
	}

	public static String loadJMXAgent(int port) throws IOException, AttachNotSupportedException, AgentLoadException,
			AgentInitializationException {
		System.setProperty("com.sun.management.jmxremote.port", Integer.toString(port));
		String name = ManagementFactory.getRuntimeMXBean().getName();
		VirtualMachine vm = VirtualMachine.attach(name.substring(0, name.indexOf('@')));

		String lca = vm.getAgentProperties().getProperty("com.sun.management.jmxremote.localConnectorAddress");
		if (lca == null) {
			Path p = Paths.get(System.getProperty("java.home")).normalize();
			if (!"jre".equals(p.getName(p.getNameCount() - 1).toString().toLowerCase()))
				p = p.resolve("jre");
			File f = p.resolve("lib").resolve("management-agent.jar").toFile();
			if (!f.exists())
				throw new IOException("Management agent not found");

			vm.loadAgent(f.getCanonicalPath(), "com.sun.management.jmxremote");
			lca = vm.getAgentProperties().getProperty("com.sun.management.jmxremote.localConnectorAddress");
		}
		vm.detach();
		return lca;
	}

}
