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

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.jerry.felinx.plugin.Activator;

public class Launcher {

	public static void main(String[] args) {
		new Launcher().launch(ILaunchManager.DEBUG_MODE);
	}

	public Launcher() {

	}

	/**
	 * @param launchMode
	 *            : ILaunchManager.RUN_MODE | ILaunchManager.DEBUG_MODE | ILaunchManager.PROFILE_MODE
	 */
	public void launch2(String launchMode) {
		try {
			DebugUITools.launch(createConfiguration(null, null), launchMode);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void launch(String launchMode) {
		try {

			DebugPlugin plugin = DebugPlugin.getDefault();
			ILaunchManager lm = plugin.getLaunchManager();
			ILaunchConfigurationType t = lm.getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION);
			ILaunchConfigurationWorkingCopy wc = t.newInstance(null, "OSGI Framework");

			/** create launch class path */
			URL delegateLocation = Launcher.class.getProtectionDomain().getCodeSource().getLocation();
			IPath runnerPath = new Path(delegateLocation.getPath());

			// -------FelixRunner.jar------------
			runnerPath = new Path("C:\\Data\\incubator\\CMv3\\workspace\\FelixRunner\\target\\FelixRunner.jar");
			IRuntimeClasspathEntry runnerEntry = JavaRuntime.newArchiveRuntimeClasspathEntry(runnerPath);
			runnerEntry.setClasspathProperty(IRuntimeClasspathEntry.USER_CLASSES);
			// -------OSGI Framework.jar------------
			IPath frameworkPath = new Path("C:\\Data\\incubator\\CMv3\\workspace\\FelixRunner\\target\\felix.jar");
			IRuntimeClasspathEntry frameworkEntry = JavaRuntime.newArchiveRuntimeClasspathEntry(frameworkPath);
			frameworkEntry.setClasspathProperty(IRuntimeClasspathEntry.USER_CLASSES);
			// -------JMX Tools.jar------------
			IPath jmxToolsPath = new Path("C:\\Data\\incubator\\CMv3\\workspace\\FelixRunner\\target\\tools.jar");
			IRuntimeClasspathEntry jmxToolsEntry = JavaRuntime.newArchiveRuntimeClasspathEntry(jmxToolsPath);
			jmxToolsEntry.setClasspathProperty(IRuntimeClasspathEntry.USER_CLASSES);
			// -------JRE------------
			IPath systemLibsPath = new Path(JavaRuntime.JRE_CONTAINER);
			IRuntimeClasspathEntry systemLibsEntry = JavaRuntime.newRuntimeContainerClasspathEntry(systemLibsPath,
					IRuntimeClasspathEntry.BOOTSTRAP_CLASSES);
			// --------Build Classpath -----
			List<String> classpath = new ArrayList<String>();
			classpath.add(runnerEntry.getMemento());
			classpath.add(frameworkEntry.getMemento());
			classpath.add(systemLibsEntry.getMemento());
			classpath.add(jmxToolsEntry.getMemento());
			wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH, classpath);
			wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH, false);
			// --------Build Sourcepath -----
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			IWorkspaceRoot root = workspace.getRoot();
			List<String> sourceLookupPath = new ArrayList<String>();
			for (IProject project : root.getProjects()) {
				IRuntimeClasspathEntry projectSourcePath = JavaRuntime.newProjectRuntimeClasspathEntry(JavaCore.create(project));
				sourceLookupPath.add(projectSourcePath.getMemento());
			}
			wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_SOURCE_PATH, sourceLookupPath);
			wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_SOURCE_PATH, false);
			// for JMX remoting
			wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, "-Dcom.sun.management.jmxremote.port="
					+ Activator.JMX_PORT + " -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false");

			// wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, "org.jerry.eclipseplugin.launch.LaunchDelegate");
			wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, "org.jerry.runner.FrameworkRunner");
			ILaunchConfiguration config = wc.doSave();
			ILaunch launch = config.launch(launchMode, null);
			Activator.setActiveLaunch(launch);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected ILaunchConfigurationWorkingCopy createConfiguration(IProject project, IPath targetPath) throws CoreException {
		ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
		// ILaunchConfigurationType configType = manager.getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION);
		ILaunchConfigurationType configType = manager.getLaunchConfigurationType(LaunchConstants.LAUNCH_CONFIG_TYPE);

		ILaunchConfigurationWorkingCopy wc;
		wc = configType.newInstance(null, "temp");
		wc.setAttribute(LaunchConstants.ATTR_CLEAN, LaunchConstants.DEFAULT_CLEAN);
		wc.setAttribute(LaunchConstants.ATTR_DYNAMIC_BUNDLES, LaunchConstants.DEFAULT_DYNAMIC_BUNDLES);
		IVMInstall vm = JavaRuntime.getVMInstall((IJavaProject) project);
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_TYPE, vm.getVMInstallType().getId());
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_NAME, vm.getName());

		// wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, "org.jerry.eclipseplugin.launch.LaunchDelegate");

		if (targetPath != null) {
			wc.setAttribute(LaunchConstants.ATTR_LAUNCH_TARGET, targetPath.toString());
			IResource targetResource = ResourcesPlugin.getWorkspace().getRoot().findMember(targetPath);
			if (targetResource != null && targetResource.exists()) {
				wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, project.getName());
			}
		}
		/*
		 * Andere props: // customize the classpath wc.setAttribute(IJavaLaunchConfigurationConstants .ATTR_DEFAULT_CLASSPATH, false);
		 * wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH, classPathList);
		 * wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, projectName);
		 * wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME , targetMainClass);
		 * wc.setAttribute(IJavaLaunchConfigurationConstants. ATTR_PROGRAM_ARGUMENTS, programArgs);
		 * wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, jvmArgs);
		 */

		return wc;
	}

}
