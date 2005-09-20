package jpatch.boundary.action;

import java.awt.event.*;
import javax.swing.*;
import jpatch.boundary.*;

import jpatch.control.edit.*;


public final class FlipPatchesAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public FlipPatchesAction() {
		super("flip patches");
		}
	public void actionPerformed(ActionEvent actionEvent) {
		PointSelection ps = MainFrame.getInstance().getPointSelection();
		if (ps != null) {
			MainFrame.getInstance().getUndoManager().addEdit(new FlipPatchesEdit(ps));
			MainFrame.getInstance().getJPatchScreen().update_all();
		}
	}
}

