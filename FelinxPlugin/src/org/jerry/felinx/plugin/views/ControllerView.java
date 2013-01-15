package org.jerry.felinx.plugin.views;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Path;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.jerry.felinx.plugin.Activator;

public class ControllerView extends ViewPart {

	public ControllerView() {
		// TODO Auto-generated constructor stub
	}

	Button runButton = null;
	Button debugButton = null;

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new GridLayout(2, true));
		parent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		runButton = new Button(parent, SWT.BUTTON1);
		if (Activator.isFelixRunning()) {
			runButton.setText("Stop");
		} else {
			runButton.setText("Run");
		}
		runButton.setImage(Activator.getImage(Activator.IMAGE_RUN));
		runButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				if (Activator.isFelixRunning()) {
					Activator.stopFelix();
					runButton.setText("Run");
				} else {
					Activator.startFelix(ILaunchManager.RUN_MODE);
					runButton.setText("Stop");
				}
			}
		});

		debugButton = new Button(parent, SWT.BUTTON1);
		debugButton.setSize(200, 24);
		debugButton.setImage(Activator.getImage(Activator.IMAGE_DEBUG));
		debugButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				if (Activator.isFelixRunning()) {
					Activator.stopFelix();
					debugButton.setText("Debug");
				} else {
					Activator.startFelix(ILaunchManager.DEBUG_MODE);
					debugButton.setText("Stop");
				}
			}
		});
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
