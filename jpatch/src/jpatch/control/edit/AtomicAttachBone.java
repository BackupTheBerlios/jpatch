/*
 * $Id: AtomicAttachBone.java,v 1.4 2006/05/22 10:46:18 sascha_l Exp $
 *
 * Copyright (c) 2005 Sascha Ledinsky
 *
 * This file is part of JPatch.
 *
 * JPatch is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * JPatch is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JPatch; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package jpatch.control.edit;

import javax.swing.tree.MutableTreeNode;
import javax.vecmath.*;
import jpatch.boundary.*;
import jpatch.entity.*;

/**
 * @author sascha
 *
 */
public class AtomicAttachBone extends JPatchAtomicEdit implements JPatchRootEdit {
	private Bone boneChild;
	private Bone boneParent;
	
	public AtomicAttachBone(Bone child, Bone parent) {
		boneChild = child;
		boneParent = parent;
		redo();
	}
	
	public void undo() {
		MutableTreeNode bones = MainFrame.getInstance().getModel().getTreenodeBones();
		MainFrame.getInstance().getTreeModel().removeNodeFromParent(boneChild);
		MainFrame.getInstance().getTreeModel().insertNodeInto(boneChild, bones, bones.getChildCount());
	}
	
	public void redo() {
		MainFrame.getInstance().getTreeModel().removeNodeFromParent(boneChild);
		MainFrame.getInstance().getTreeModel().insertNodeInto(boneChild, boneParent, boneParent.getChildCount());
	}

	public int sizeOf() {
		return 8 + 4 + 4;
	}

	public String getName() {
		return "attach bone";
	}
}
