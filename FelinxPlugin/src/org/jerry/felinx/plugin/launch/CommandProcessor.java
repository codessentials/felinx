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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * 
 * @author mvrm
 *
 */
public class CommandProcessor implements Runnable {

	private InputStream inputStream;
	private OutputStream outputStream;
	private OutputStream errorStream;
	private BundleContext bundleContext;
	private Method execMethod=null;
	private Object session=null;

	public CommandProcessor(InputStream aInputStream, OutputStream aOutputStream, OutputStream aErrorStream, BundleContext aBundleContext) {
		this.inputStream = aInputStream;
		this.outputStream = aOutputStream;
		this.errorStream = aErrorStream;
		this.bundleContext = aBundleContext;
	}

	public CommandProcessor(BundleContext aBundleContext) {
		this(System.in, System.out, System.err, aBundleContext);
	}

	public boolean start() {
		boolean started = false;
		ServiceReference<?> commandProcessorServiceRef = this.bundleContext
				.getServiceReference("org.apache.felix.service.command.CommandProcessor");
		if (commandProcessorServiceRef == null) {
			System.out.println("no CommandProcessor found");
		} else {
			try {
				Object commandProcessor = this.bundleContext.getService(commandProcessorServiceRef);
//				Class[] parameterTypes = new Class[] { inputStream.getClass(), outputStream.getClass(), errorStream.getClass() };
//				Method[] methods = commandProcessor.getClass().getDeclaredMethods();
//				Method createSessionMethod = null;
//				for (Method method : methods) {
//					if (method.getName().equals("createSession")) {
//						createSessionMethod = method;
//					}
//				}
				Method createSessionMethod = LaunchUtils.getFirstMethod(commandProcessor.getClass(), "createSession");
				Object[] params = new Object[] { inputStream, outputStream, errorStream };
				session = createSessionMethod.invoke(commandProcessor, params);
				execMethod = LaunchUtils.getFirstMethod(session.getClass(), "execute");
				new Thread(this).start();
				started = true;
			} catch (Exception ex) {
				started = false;
				System.out.println("CommandProcessor not started: " + ex.getMessage());
				ex.printStackTrace();
			}

		}
		return started;
	}

	public void stop() {

	}

	@Override
	public void run() {
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
			String line = null;
			while (true) {
				try {
					while((line = in.readLine()) != null) {
						execMethod.invoke(session, new Object[] { line });	
					}
				} catch (InvocationTargetException ex) {
					if (ex.getCause() != null){
						new PrintStream(errorStream).println(ex.getCause().getMessage());
					}
				} catch (Exception ex) {
					System.out.println("InputStreamListener failed, retrying in 2s. " + ex.getMessage()+ " ("+ex.getClass().getName()+")");
					ex.printStackTrace(new PrintStream(outputStream));
					Thread.sleep(2000);
				}
			}
		} catch (Exception ex) {
			System.out.println("InputStreamListener failed, stopping. " + ex.getMessage() + " ("+ex.getClass().getName()+")");
		}

	}

}
