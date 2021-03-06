package jpatch.boundary.action;

import java.awt.event.*;
import javax.swing.*;
import jpatch.entity.*;
import jpatch.boundary.*;
import jpatch.control.edit.AtomicAddRemoveAnimObject;
import jpatch.control.edit.JPatchRootEdit;

public final class NewLightAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public NewLightAction() {
		super("Add new lightsource");
	}
	public void actionPerformed(ActionEvent actionEvent) {
		AnimObject animObject = new AnimLight();
		JPatchRootEdit edit = new AtomicAddRemoveAnimObject(animObject, false);
		MainFrame.getInstance().getUndoManager().addEdit(edit);
		MainFrame.getInstance().getJPatchScreen().update_all();
		MainFrame.getInstance().getTimelineEditor().repaint();
	}
}