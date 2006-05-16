package jpatch.boundary.action;

import java.awt.event.*;
import javax.swing.*;
import jpatch.entity.*;
import jpatch.boundary.*;
import jpatch.control.edit.*;

public final class NewCameraAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public NewCameraAction() {
		super("New Camera");
	}
	
	public void actionPerformed(ActionEvent actionEvent) {
		AnimObject animObject = new Camera("New Camera");
		JPatchRootEdit edit = new AtomicAddRemoveAnimObject(animObject, false);
		MainFrame.getInstance().getUndoManager().addEdit(edit);
	}
}


