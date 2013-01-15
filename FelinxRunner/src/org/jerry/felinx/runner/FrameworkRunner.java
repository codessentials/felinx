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
	 */
	public static void main(String[] args) {
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
		frameworkLocation = "C:/Data/incubator/CMv3/workspace/_FelixServer";
		System.out.println("Hello from FelixRunner!!");
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

	public void updateBundle(String aSymbolicName, String aVersion, File aBundleToInstall) throws FileNotFoundException,
			BundleException {
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
				System.out.println("Bundle does not exist yet. Will install " + aSymbolicName + " " + aVersion);
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
