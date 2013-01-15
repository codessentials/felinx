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
package org.jerry.felinx.plugin.views;

import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
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
