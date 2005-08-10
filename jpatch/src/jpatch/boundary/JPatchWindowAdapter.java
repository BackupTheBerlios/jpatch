package jpatch.boundary;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import jpatch.auxilary.*;
import jpatch.boundary.action.*;

/**
 * this WindowAdapter is used by JPatch's MainFrame
 * it stores all settings (Preferences) to the
 * permanent backings store on a windowClosing event
 */
public class JPatchWindowAdapter extends WindowAdapter {	
	public void windowClosing(WindowEvent windowEvent) {
		
		if (MainFrame.getInstance().getUndoManager().hasChanged()) {
			int option = JPatchUtils.showSaveDialog();
			switch (option) {
				
				case JOptionPane.YES_OPTION:
					SaveAsAction saveAsAction = new SaveAsAction(false);
					if (saveAsAction.save()) {
						quit(windowEvent.getWindow());
					}
					break;
				
				case JOptionPane.NO_OPTION:
					quit(windowEvent.getWindow());
			}
		} else {
			quit(windowEvent.getWindow());
		}
	}
	
	public void quit(Window window) {
		JPatchSettings settings = JPatchSettings.getInstance();
		settings.iScreenX = MainFrame.getInstance().getX();
		settings.iScreenY = MainFrame.getInstance().getY();
		settings.iScreenWidth = MainFrame.getInstance().getWidth();
		settings.iScreenHeight = MainFrame.getInstance().getHeight();
		settings.saveSettings();
		window.setVisible(false);
		window.dispose();
		System.exit(0);
	}
}
