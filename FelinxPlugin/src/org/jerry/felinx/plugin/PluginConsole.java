package org.jerry.felinx.plugin;

import java.io.InputStream;
import java.io.PrintStream;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.IOConsoleInputStream;
import org.eclipse.ui.console.IOConsoleOutputStream;
import org.eclipse.ui.console.MessageConsoleStream;
import org.jerry.felinx.plugin.launch.CommandProcessor;

public class PluginConsole extends IOConsole {

	private static final Color RED;
	private static final Color BLUE;
	static {
		Display device = Display.getCurrent();
		RED = new Color(device, 255, 0, 0);
		BLUE = new Color(device, 0, 0, 128);
	}

	/**
	 * Property constant indicating the font of this console has changed.
	 * 
	 * @deprecated use {@link IConsoleConstants#P_FONT}
	 */
	public static final String P_FONT = IConsoleConstants.P_FONT;

	/**
	 * Property constant indicating the color of a stream has changed.
	 * 
	 * @deprecated use {@link IConsoleConstants#P_STREAM_COLOR}
	 */
	public static final String P_STREAM_COLOR = IConsoleConstants.P_STREAM_COLOR;

	/**
	 * Property constant indicating tab size has changed
	 * 
	 * @deprecated use {@link IConsoleConstants#P_TAB_SIZE}
	 */
	public static final String P_TAB_SIZE = IConsoleConstants.P_TAB_SIZE;

	/**
	 * The default tab size
	 * 
	 * @deprecated use {@link IConsoleConstants#DEFAULT_TAB_SIZE}
	 */
	public static final int DEFAULT_TAB_SIZE = IConsoleConstants.DEFAULT_TAB_SIZE;

	private PrintStream outputStream;

	/**
	 * Constructs a message console with the given name and image.
	 * 
	 * @param name
	 *            console name
	 * @param imageDescriptor
	 *            console image descriptor or <code>null</code>
	 */
	public PluginConsole(String name, ImageDescriptor imageDescriptor) {
		this(name, imageDescriptor, true);
	}

	/**
	 * Constructs a message console.
	 * 
	 * @param name
	 *            console name
	 * @param imageDescriptor
	 *            console image descriptor or <code>null</code>
	 * @param autoLifecycle
	 *            whether lifecycle methods should be called automatically when added and removed from the console manager
	 * @since 3.1
	 */
	public PluginConsole(String name, ImageDescriptor imageDescriptor, boolean autoLifecycle) {
		super(name, null, imageDescriptor, autoLifecycle);
		initializeIO();
	}

	/**
	 * Appends the given message to this console, from the specified stream.
	 * 
	 * @param text
	 *            message
	 * @param stream
	 *            stream the message belongs to
	 * @deprecated since 3.1, this method should no longer be called, and has no effect. Writing to a message console stream updates the
	 *             document
	 */
	protected void appendToDocument(String text, MessageConsoleStream stream) {
	}

	private void initializeIO() {

		IOConsoleOutputStream out = newOutputStream();
		out.setColor(BLUE);
		outputStream = new PrintStream(out);
		System.setOut(outputStream);

		IOConsoleOutputStream err = newOutputStream();
		err.setColor(RED);
		System.setErr(new PrintStream(err));

		InputStream input = getInputStream();
		System.setIn(input);
		//InputStreamListener ipsl = new InputStreamListener(input);
		//ipsl.start();
		ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[] { this });
		ConsolePlugin.getDefault().getConsoleManager().showConsoleView(this);
	}

	public PrintStream getOutputStream() {
		// TODO Auto-generated method stub
		return outputStream;
	}

}
