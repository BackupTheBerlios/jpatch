/*
 * $Id: Settings.java,v 1.12 2006/05/22 10:48:57 sascha_l Exp $
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
package jpatch.boundary.settings;

import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.vecmath.*;

import jpatch.boundary.*;
import jpatch.boundary.action.Actions;
import jpatch.boundary.laf.SmoothLookAndFeel;


/**
 * @author sascha
 *
 */
public class Settings extends AbstractSettings {
	private static Settings INSTANCE;
	
	public static enum Startup { MODELER, ANIMATOR };
	public static enum Plaf { CROSS_PLATFORM, SYSTEM, JPATCH };
	public Startup startup = Startup.MODELER;
	public transient boolean newInstallation = true;
	public transient boolean cleanExit = false;
	public int screenPositionX = 0;
	public int screenPositionY = 0;
	public int screenWidth = 1024;
	public int screenHeight = 768;
	public boolean saveScreenDimensionsOnExit = true;
	public Plaf lookAndFeel = Plaf.CROSS_PLATFORM;
	public long undoMaxMem = 128;
	public long undoMinMem = 120;
	public final DirectorySettings directories = new DirectorySettings();
	public final ViewportSettings viewports = new ViewportSettings();
	public final ColorSettings colors = new ColorSettings();
	public final RealtimeRendererSettings realtimeRenderer = new RealtimeRendererSettings();
	public final RendererSettings export = new RendererSettings();
	
	public void showDialog(Component parent) {
		load();
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
//		initTree();
		final JTable table = getTable();
		table.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				table.repaint();
				
			}
		});
		JTree tree = new JTree(this);
		tree.setCellRenderer(getTreeCellRenderer());
		tree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent e) {
				AbstractSettings settings = (AbstractSettings) e.getPath().getLastPathComponent();
				table.setModel((TableModel) settings.getTableModel());
				table.getColumnModel().getColumn(0).setHeaderValue("Preference Name");
				table.getColumnModel().getColumn(1).setHeaderValue("Value");
				table.setDefaultEditor(Object.class, settings.getTableCellEditor());
			}
		});
		splitPane.add(new JScrollPane(tree));
		splitPane.add(new JScrollPane(getTable()));
		
		MainFrame mf = MainFrame.getInstance();
		if (mf != null) {
			screenPositionX = mf.getX();
			screenPositionY = mf.getY();
			screenWidth = mf.getWidth();
			screenHeight = mf.getHeight();
		}
		if (JOptionPane.showConfirmDialog(parent, splitPane, "Preferences", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null) == JOptionPane.OK_OPTION) {
			if (mf != null && (screenWidth != mf.getWidth() || screenHeight != mf.getHeight() || screenPositionX != mf.getX() || screenPositionY != mf.getY()))
				mf.setBounds(screenPositionX, screenPositionY, screenWidth, screenHeight);
			save();
			
			try {
				switch (lookAndFeel) {
				case CROSS_PLATFORM:
					UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
					UIManager.put("swing.boldMetal", false);
					break;
				case SYSTEM:
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					break;
				case JPATCH:
					UIManager.setLookAndFeel(new SmoothLookAndFeel());
					UIManager.put("swing.boldMetal", false);
					break;
				}
				if (mf != null) {
					SwingUtilities.updateComponentTreeUI(mf);
					SwingUtilities.updateComponentTreeUI(mf.getJPatchScreen().getPopupMenu());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		} else {
			System.out.println("Cancel");
			load();
		}
	}
	
	private Settings() {
		if (SplashScreen.instance != null)
			SplashScreen.instance.setText("Loading preferences");
		System.out.println("Loading preferences...");
		storeDefaults();
		load();
		initTree();
		INSTANCE = this;
	}
	
	public static Settings getInstance() {
		if (INSTANCE == null)
			new Settings();
		return INSTANCE;
	}
}
