package jpatch.boundary;

import java.awt.Rectangle;
import javax.swing.*;
import jpatch.boundary.ui.*;

/**
 * 
 */

/**
 * 
 * @author sascha
 *
 */
public class LockingButtonGroup extends ButtonGroup {
	/*
	 * The default button of the LockingButtonGroup
	 */
	private ButtonModel defaultButtonModel;

	/*
	 * sets the Default Button 
	 */
	public void setDefaultButtonModel(ButtonModel defaultButtonModel) {
		this.defaultButtonModel = defaultButtonModel;
	}
	
	/*
	 * gets the Default Button 
	 */
	public ButtonModel getDefaultButtonmodel() {
		return defaultButtonModel;
	}
	
	/*
	 * select the default button
	 */
	public void actionDone() {
		ButtonModel selectedButtonModel = getSelection();
		if (selectedButtonModel instanceof LockingToggleButton.LockingToggleButtonModel) {
			if (((LockingToggleButton.LockingToggleButtonModel) selectedButtonModel).isLocked())
				return;
		}
		if (defaultButtonModel != null && defaultButtonModel != selectedButtonModel) {
			defaultButtonModel.setArmed(true);
			defaultButtonModel.setPressed(true);
			defaultButtonModel.setPressed(false);
			defaultButtonModel.setArmed(false);
		}
	}
}
