package jpatch.boundary.action;

import java.awt.event.*;
import java.util.Iterator;

import javax.swing.*;
import jpatch.entity.*;
import jpatch.boundary.*;
import jpatch.control.edit.*;

public final class DeleteMaterialAction extends AbstractAction {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPatchMaterial material;
	
	public DeleteMaterialAction(JPatchMaterial material) {
		super("Delete");
		this.material = material;
		//putValue(Action.SHORT_DESCRIPTION,"Add Controlpoint [A]");
		//MainFrame.getInstance().getKeyEventDispatcher().setKeyActionListener(this,KeyEvent.VK_A);
	}
	public void actionPerformed(ActionEvent actionEvent) {
		//MainFrame.getInstance().getJPatchScreen().removeAllMouseListeners();
		//MainFrame.getInstance().getJPatchScreen().addMouseListeners(new AddControlPointMouseAdapter());
		//MainFrame.getInstance().clearDialog();
		JPatchMaterial defaultMaterial = (JPatchMaterial) MainFrame.getInstance().getModel().getMaterialList().get(0);
		if (material != defaultMaterial) {
			JPatchActionEdit edit = new JPatchActionEdit("delete material");
			for (Iterator it = MainFrame.getInstance().getModel().getPatchSet().iterator(); it.hasNext(); ) {
				Patch patch = (Patch) it.next();
				if (patch.getMaterial() == material) {
					edit.addEdit(new AtomicChangePatchMaterial(patch,defaultMaterial));
				}
			}
			edit.addEdit(new AtomicRemoveMaterial(material));
			MainFrame.getInstance().getUndoManager().addEdit(edit);
			MainFrame.getInstance().getJPatchScreen().update_all();
		}
	}
}

