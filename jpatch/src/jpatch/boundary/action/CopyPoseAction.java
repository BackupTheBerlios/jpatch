/*
 * $Id: CopyPoseAction.java,v 1.2 2006/02/01 21:11:28 sascha_l Exp $
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
package jpatch.boundary.action;

import javax.swing.*;
import java.awt.event.*;
import jpatch.boundary.*;
import jpatch.entity.*;

/**
 * @author sascha
 *
 */
public class CopyPoseAction extends AbstractAction {
	private Model model;
	
	public CopyPoseAction(Model model) {
		super("copy pose");
		this.model = model;
	}
	
	public void actionPerformed(ActionEvent e) {
		MainFrame.getInstance().getAnimation().getClipboardPose(model).setPose();
		MainFrame.getInstance().getJPatchScreen().update_all();
	}
}
