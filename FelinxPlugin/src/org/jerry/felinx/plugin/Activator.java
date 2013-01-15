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
package org.jerry.felinx.plugin;

import java.io.File;
import java.io.PrintStream;
import java.net.MalformedURLException;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.jerry.felinx.plugin.launch.JMXClient;
import org.jerry.felinx.plugin.launch.Launcher;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.launch.Framework;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin implements BundleListener, ServiceListener {

	// The plug-in ID
	public static final String PLUGIN_ID = "FelinxPlugin"; //$NON-NLS-1$

	public static Framework framework = null;

	// The shared instance
	private static Activator plugin;

	private static PluginConsole console;

	private static JMXClient frameworkShell = null;

	private static ILaunch activeLaunch;

	public static final int JMX_PORT = 9999;

	public static final String IMAGE_RUN = "run";
	public static final String IMAGE_DEBUG = "debug";

	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext )
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		console = new PluginConsole("OSGiFramework", null);
		System.out.println("Starting FelinxPlugin");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext )
	 */
	public void stop(BundleContext context) throws Exception {
		if (isFelixRunning()) {
			stopFelix();
		}
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in relative path
	 * 
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	public static boolean isFelixRunning() {
		return activeLaunch != null && !activeLaunch.isTerminated();
	}

	/**
	 * @param launchMode
	 *            : ILaunchManager.RUN_MODE | ILaunchManager.DEBUG_MODE | ILaunchManager.PROFILE_MODE
	 */
	public static void startFelix(String launchMode) {
		try {
			if (activeLaunch == null || activeLaunch.isTerminated()) {
				new Launcher().launch(launchMode);
				Thread.sleep(500);
				frameworkShell = new JMXClient("localhost", JMX_PORT);
				frameworkShell.connect();
			} else {
				System.err.println("There is still a framework running. Please terminate!");
			}
		} catch (Exception e) {
			System.err.println("Could not start OSGi framework: " + e.getMessage());
			System.err.println("StackTrace: ");
			e.printStackTrace();
		}
	}

	public static void stopFelix() {
		System.out.println("Stopping FelixServer...");
		try {
			frameworkShell.stopFramework();
			System.out.println("FelixServer is stopped.");
		} catch (Exception e) {
			System.err.println("Could not stop framework: " + e.getMessage());
			e.printStackTrace();
		} finally {
			frameworkShell.disconnect();
			frameworkShell = null;
		}
	}

	public static void updateBundle(String aSymbolicName, String aVersion, File aBundleToInstall) {
		frameworkShell.updateBundle(aSymbolicName, aVersion, aBundleToInstall);
	}

	@Override
	public void serviceChanged(ServiceEvent arg0) {
		System.out.println("ServiceEvent: " + arg0.toString());
	}

	@Override
	public void bundleChanged(BundleEvent arg0) {
		System.out.println("BundleEvent: " + arg0.toString());

	}

	public static PrintStream getOutputStream() {
		return console.getOutputStream();
	}

	@Override
	protected void initializeImageRegistry(ImageRegistry registry) {
		super.initializeImageRegistry(registry);
		try {
			registry.put(IMAGE_RUN, createDescriptor(IMAGE_RUN));
			registry.put(IMAGE_DEBUG, createDescriptor(IMAGE_DEBUG));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	private ImageDescriptor createDescriptor(String id) throws MalformedURLException {
		return ImageDescriptor.createFromURL(FileLocator.find(getBundle(), new Path("images/" + id + ".png"), null));
	}

	public static Image getImage(String id) {
		ImageRegistry imageRegistry = plugin.getImageRegistry();
		return imageRegistry.get(id);
	}

	public static void setActiveLaunch(ILaunch launch) {
		activeLaunch = launch;
	}
}
