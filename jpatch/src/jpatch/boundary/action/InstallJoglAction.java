/*
 * $Id: InstallJoglAction.java,v 1.1 2005/10/10 15:45:07 sascha_l Exp $
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

import java.awt.event.*;
import javax.swing.*;

import jpatch.auxilary.*;

/**
 * @author sascha
 *
 */
public class InstallJoglAction extends AbstractAction {
	
	private static final long serialVersionUID = 1L;

	public InstallJoglAction() {
		super("Install native JOGL libraries");
		setEnabled(!JoglInstall.isInstalled() && JoglInstall.isOsSupported());
	}
	
	public void actionPerformed(ActionEvent event) {
		JoglInstall.install();
		setEnabled(!JoglInstall.isInstalled() && JoglInstall.isOsSupported());
	}
}
