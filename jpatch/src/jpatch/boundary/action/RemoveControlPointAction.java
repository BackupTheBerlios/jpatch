package jpatch.boundary.action;

import java.util.*;
import java.awt.event.*;

import javax.swing.*;
import jpatch.control.edit.*;
import jpatch.entity.ControlPoint;
import jpatch.boundary.*;

public final class RemoveControlPointAction extends AbstractAction {
	public void actionPerformed(ActionEvent actionEvent) {
		
	JPatchActionEdit edit = null;
		Selection selection = MainFrame.getInstance().getSelection();
		if (selection == null)
			return;
		if (selection.getDirection() != 0) {
			ControlPoint cp = (ControlPoint) selection.getHotObject();
			if (selection.getDirection() == -1)
				cp = cp.getPrev();
			edit = new JPatchActionEdit("remove curve segment");
			edit.addEdit(new CompoundRemoveCurveSegment(cp));
		} else {
			HashSet selectionSet = new HashSet(MainFrame.getInstance().getSelection().getObjects());
			edit = new JPatchActionEdit("remove");
			edit.addEdit(new CompoundRemove(selectionSet));
		}
		edit.addEdit(new AtomicChangeSelection(null));
		MainFrame.getInstance().getUndoManager().addEdit(edit);
		MainFrame.getInstance().getJPatchScreen().update_all();
		
		
//		if (MainFrame.getInstance().getSelection() == null)
//			return;
//		HashSet selectionSet = new HashSet(MainFrame.getInstance().getSelection().getObjects());
//		JPatchActionEdit edit = new JPatchActionEdit("remove");
//		edit.addEdit(new CompoundRemove(selectionSet));
//		edit.addEdit(new AtomicChangeSelection(null));
//		MainFrame.getInstance().getUndoManager().addEdit(edit);
//		MainFrame.getInstance().getJPatchScreen().update_all();
	}
	
//	public void actionPerformed(ActionEvent actionEvent) {
//		PointSelection ps = MainFrame.getInstance().getPointSelection();
//		if (ps != null) {
//			JPatchCompoundEdit compoundEdit = new JPatchCompoundEdit();
//			if (ps.isCurve()) {
//				ControlPoint cp = ps.getControlPoint();
//				if (ps.getDirection() && cp.getNext() != null) {
//					compoundEdit.addEdit(new RemoveCurveSegmentEdit(cp));
//				} else if (cp.getPrev() != null) {
//					compoundEdit.addEdit(new RemoveCurveSegmentEdit(cp.getPrev()));
//				}
//			} else {
//				ControlPoint[] acpSelection = ps.getControlPointArray();
//				for (int s = 0; s < acpSelection.length; s++) {
//					if (!acpSelection[s].isHook()) {
//						ControlPoint[] acpStack = acpSelection[s].getStack();
//						for (int t = 0; t < acpStack.length; t++) {
//							if (acpStack[t].getCurve() != null) {
//								/*
//								 * check if we have to remove a hook patch
//								 */
//								ControlPoint hook = acpStack[t].getChildHook();
//								while(hook != null) {
//									ArrayList patches = MainFrame.getInstance().getModel().getPatchesContaining(hook);
//									for (Iterator it = patches.iterator(); it.hasNext();) {
//										Patch patch = (Patch) it.next();
//										if (patch.getModel() != null) {
//											compoundEdit.addEdit(new RemovePatchFromModelEdit(patch));
//										}
//									}
//									hook = hook.getNext();
//								}
//								if (acpStack[t].getPrev() != null) {
//									hook = acpStack[t].getPrev().getChildHook();
//									while(hook != null) {
//										ArrayList patches = MainFrame.getInstance().getModel().getPatchesContaining(hook);
//										for (Iterator it = patches.iterator(); it.hasNext();) {
//											Patch patch = (Patch) it.next();
//											if (patch.getModel() != null) {
//												compoundEdit.addEdit(new RemovePatchFromModelEdit(patch));
//											}
//										}
//										hook = hook.getNext();
//									}
//								}
//								compoundEdit.addEdit(new RemoveControlPointEdit(acpStack[t]));
//								ArrayList patches = MainFrame.getInstance().getModel().getPatchesContaining(acpStack[t]);
//								for (Iterator it = patches.iterator(); it.hasNext();) {
//									Patch patch = (Patch) it.next();
//									if (patch.getModel() != null) {
//										compoundEdit.addEdit(new RemovePatchFromModelEdit(patch));
//									}
//								}
//							}
//						}
//					}
//				}
//			}
//			compoundEdit.addEdit(new AtomicChangeSelection(null));
//			MainFrame.getInstance().getUndoManager().addEdit(compoundEdit);
//			//MainFrame.getInstance().setSelection(null);
//			MainFrame.getInstance().getJPatchScreen().update_all();
//		}
//	}
}

