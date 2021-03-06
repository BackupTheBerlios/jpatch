/*
 * $Id$
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

import java.awt.event.ActionEvent;
import java.net.*;
import java.util.*;
import java.util.prefs.Preferences;

import javax.swing.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;
import jpatch.auxilary.KeyStrokeUtils;
import jpatch.boundary.*;
import jpatch.boundary.settings.*;
import jpatch.boundary.ui.*;
import jpatch.entity.Camera;


/**
 * @author sascha
 *
 */
public class Actions extends DefaultHandler {
	private static final URL URL = ClassLoader.getSystemResource("jpatch/boundary/action/actions.xml");
	private static Actions INSTANCE;
	private Map<String, ActionDescriptor> actionMap = new HashMap<String, ActionDescriptor>();
	private Map<String, ButtonGroup> buttonGroupMap = new HashMap<String, ButtonGroup>();
	private ActionDescriptor actionDescriptor;

	public static Actions getInstance() {
		if (INSTANCE == null)
			INSTANCE = new Actions(URL);
		return INSTANCE;
	}
	
	private Actions(URL url) {
		addActions(actionMap);
		XMLReader xmlReader = null;
		try {
			xmlReader = XMLReaderFactory.createXMLReader();
		} catch (SAXException e) {
			try {
				xmlReader = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
			} catch (SAXException ee) {
				ee.printStackTrace();
			}
		}
		try {
			xmlReader.setContentHandler(this);
			System.out.println("Loading actions...");
			if (SplashScreen.instance != null)
				SplashScreen.instance.setText("Loading actions");
			xmlReader.parse(new InputSource(url.toString()));
			
		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
		
		System.out.println("Loading key bindings...");
		if (SplashScreen.instance != null)
			SplashScreen.instance.setText("Loading key bindings");
		loadKeySettings();
		
		
		/*
		 * Disable commands
		 */
		enableAction("clear rotoscope image", false);
		enableAction("stop edit morph", false);
		
		/*
		 * Set toggle button states
		 */
		actionMap.get("select points").buttonModel.setSelected(true);
		actionMap.get("select bones").buttonModel.setSelected(true);
		actionMap.get("snap to grid").buttonModel.setSelected(Settings.getInstance().viewports.snapToGrid);	
		actionMap.get("round tangents").buttonModel.setSelected(true);
		actionMap.get("synchronize viewports").buttonModel.setSelected(Settings.getInstance().viewports.synchronizeViewports);
	}
	
	public void enableAction(String key, boolean enable) {
		getAction(key).setEnabled(enable);
	}
	
	public void enableActions(String[] keys, boolean enable) {
		for (int i = 0; i < keys.length; i++)
			enableAction(keys[i], enable);
	}
	
	public void setViewDefinition(ViewDefinition viewDef) {
		Camera camera = viewDef.getCamera();
		if (camera != null)
			actionMap.get("camera" + camera.hashCode()).buttonModel.setSelected(true);
		else
			actionMap.get(viewDef.getViewName()).buttonModel.setSelected(true);
		actionMap.get("show points").buttonModel.setSelected(viewDef.renderPoints());
		actionMap.get("show curves").buttonModel.setSelected(viewDef.renderCurves());
		actionMap.get("show patches").buttonModel.setSelected(viewDef.renderPatches());
		actionMap.get("show bones").buttonModel.setSelected(viewDef.renderBones());
		actionMap.get("show rotoscope").buttonModel.setSelected(viewDef.showRotoscope());
		actionMap.get("lock view").buttonModel.setSelected(viewDef.isLocked());
		enableAction("unlock view", viewDef.isLocked());
		enableAction("show patches", viewDef.getDrawable().isShadingSupported());
		enableAction("clear rotoscope image", MainFrame.getInstance().getModel() != null && MainFrame.getInstance().getModel().getRotoscope(viewDef.getView()) != null);
	}
	
	public ButtonGroup getButtonGroup(String key) {
		ButtonGroup buttonGroup = buttonGroupMap.get(key);
		if (buttonGroup == null)
			throw new IllegalArgumentException("no buttongroup for key " + key);
		return buttonGroup;
	}
	
	public Action getAction(String key) {
		ActionDescriptor actionDescriptor = actionMap.get(key);
		if (actionDescriptor == null)
			throw new IllegalArgumentException("Action for key " + key + " not found!");
		if (actionDescriptor.action == null)
			throw new RuntimeException("no Action for key " + key + "!");
		return actionDescriptor.action;
	}
	
	public void addAction(String key, Action action, DefaultButtonModel buttonModel) {
		ActionDescriptor actionDescriptor = new ActionDescriptor(action);
		actionDescriptor.buttonModel = buttonModel;
		actionMap.put(key, actionDescriptor);
	}
	
	public void removeAction(String key) {
		actionMap.remove(key);
	}
	
	public ButtonModel getButtonModel(String key) {
		ActionDescriptor actionDescriptor = actionMap.get(key);
		if (actionDescriptor == null)
			throw new IllegalArgumentException("Action for key " + key + " not found!");
		if (actionDescriptor.buttonModel == null)
			throw new RuntimeException("no ButtonModel for key " + key + "!");
		return actionDescriptor.buttonModel;
	}
	
	public Map<String, KeyStroke> getKeyMapping() {
		Map<String, KeyStroke> map = new HashMap<String, KeyStroke>();
		for (String key : actionMap.keySet()) {
			map.put(key, KeyStroke.getKeyStroke((String) actionMap.get(key).action.getValue(JPatchAction.ACCELERATOR)));
		}
		return map;
	}
	
	public KeyStroke getDefaultKeyStroke(String key) {
		return actionMap.get(key).defaultAccelerator;
	}
	
//	public Set<String> getKeys() {
//		return actionMap.keySet();
//	}
	
	public AbstractButton getButton(String key) {
		ActionDescriptor actionDescriptor = actionMap.get(key);
		if (actionDescriptor == null)
			throw new IllegalArgumentException("Action for key " + key + " not found!");
		AbstractButton button = null;
		if (actionDescriptor.buttonModel instanceof JPatchLockingToggleButtonModel.UnderlyingModel)
			button = new JPatchLockingToggleButton((JPatchLockingToggleButtonModel.UnderlyingModel) actionDescriptor.buttonModel);
		else if (actionDescriptor.buttonModel instanceof JPatchMenuButton.MenuButtonModel)
			button = new JPatchMenuButton((JPatchMenuButton.MenuButtonModel) actionDescriptor.buttonModel);
		else if (actionDescriptor.buttonModel instanceof JToggleButton.ToggleButtonModel)
			button = new JPatchToggleButton((JPatchToggleButton.ToggleButtonModel) actionDescriptor.buttonModel);
		else if (actionDescriptor.buttonModel instanceof DefaultButtonModel)
			button = new JPatchButton(actionDescriptor.buttonModel);
		button.setAction(actionDescriptor.action);
		actionDescriptor.bound = true;
		return button;
	}
	
	public JMenuItem getMenuItem(String key) {
		ActionDescriptor actionDescriptor = actionMap.get(key);
		if (actionDescriptor == null)
			throw new IllegalArgumentException("Action for key " + key + " not found!");
		JMenuItem menuItem = null;
		if (actionDescriptor.buttonModel instanceof JPatchLockingToggleButtonModel.UnderlyingModel) {
			if (actionDescriptor.buttonModel.getGroup() == null)
				throw new IllegalArgumentException("LockingToggleButton is not part of a buttongroup!");
			else
				menuItem = new JPatchLockingRadioButtonMenuItem((JPatchLockingToggleButtonModel.UnderlyingModel) actionDescriptor.buttonModel);
		} else if (actionDescriptor.buttonModel instanceof JToggleButton.ToggleButtonModel) {
			if (actionDescriptor.buttonModel.getGroup() == null)
				menuItem = new JPatchCheckBoxMenuItem((JToggleButton.ToggleButtonModel) actionDescriptor.buttonModel);
			else
				menuItem = new JPatchRadioButtonMenuItem((JToggleButton.ToggleButtonModel) actionDescriptor.buttonModel);
		} else if (actionDescriptor.buttonModel instanceof DefaultButtonModel) {
			menuItem = new JPatchMenuItem((DefaultButtonModel) actionDescriptor.buttonModel);
		}
//		menuItem.setModel(actionDescriptor.buttonModel);
		menuItem.setAction(actionDescriptor.action);
		actionDescriptor.bound = true;
		return menuItem;
	}

	public Set<Action> getUnboundActions() {
		Set<Action> unboundActions = new HashSet<Action>();
		for (String key : actionMap.keySet()) {
			ActionDescriptor actionDescriptor = actionMap.get(key);
			if (!actionDescriptor.bound)
				unboundActions.add(actionDescriptor.action);
		}
		return unboundActions;
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
//		System.out.print("<" + localName);
//		for (int i = 0, n = attributes.getLength(); i < n; i++)
//			System.out.print(" " + attributes.getLocalName(i) + "=\"" + attributes.getValue(i) + "\"");
//		System.out.println(">");
		if (localName.equals("buttongroup")) {
			if (attributes.getValue("type").equals("standard"))
				buttonGroupMap.put(attributes.getValue("name"), new ButtonGroup());
			else if (attributes.getValue("type").equals("locking"))
				buttonGroupMap.put(attributes.getValue("name"), new LockingButtonGroup());
		} else if (localName.equals("action")) {
			actionDescriptor = actionMap.get(attributes.getValue("name"));
			if (actionDescriptor == null)
				throw new IllegalArgumentException("No actionDescriptor for key " + attributes.getValue("name") + "!");
			String model = attributes.getValue("model");
			if (model.equals("default"))
				actionDescriptor.buttonModel = new DefaultButtonModel();
			else if (model.equals("radio")) {
				actionDescriptor.buttonModel = new JToggleButton.ToggleButtonModel();
				getButtonGroup(attributes.getValue("group")).add(new JPatchDummyButton(actionDescriptor.buttonModel));
			} else if (model.equals("locking radio")) {
				actionDescriptor.buttonModel = new JPatchLockingToggleButtonModel.UnderlyingModel(); // FIXME
				getButtonGroup(attributes.getValue("group")).add(new JPatchDummyButton(actionDescriptor.buttonModel));
			} else if (model.equals("check")) {
				actionDescriptor.buttonModel = new JToggleButton.ToggleButtonModel();
			} else if (model.equals("menu")) {
				actionDescriptor.buttonModel = new JPatchMenuButton.MenuButtonModel();
			}
			String isDefault = attributes.getValue("default");
			if (isDefault != null && isDefault.equals("true"))
				((LockingButtonGroup) getButtonGroup(attributes.getValue("group"))).setDefaultButtonModel(actionDescriptor.buttonModel);
		} else if (localName.equals("property")) {
			actionDescriptor.action.putValue(attributes.getValue("key"), attributes.getValue("value"));
			if (attributes.getValue("key").equals(JPatchAction.ACCELERATOR))
				actionDescriptor.defaultAccelerator = KeyStroke.getKeyStroke(attributes.getValue("value"));
		}
	}
	
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (localName.equals("action")) {
//			if (actionDescriptor.button != null) {
//				System.out.println("*** button != null, setting action ***");
//				actionDescriptor.button.setAction(null);
//				actionDescriptor.button.setAction(actionDescriptor.action);
//			}
//			if (actionDescriptor.menuItem != null)
//				actionDescriptor.menuItem.setAction(actionDescriptor.action);
			actionDescriptor = null;
		}
	}
	
	public void saveKeySettings() {
		Preferences node = Preferences.userRoot().node("/JPatch/settings/keyBindings");
		for (String key : actionMap.keySet()) {
			String accelerator = (String) actionMap.get(key).action.getValue(JPatchAction.ACCELERATOR);
			if (accelerator == null)
				accelerator = "null";
			node.put(key, accelerator);
		}
	}
	
	private void loadKeySettings() {
		Preferences node = Settings.getInstance().getRootNode().node("keyBindings");
		for (String key : actionMap.keySet()) {
			Action action = actionMap.get(key).action;
			KeyStroke ks = getDefaultKeyStroke(key);
			String defaultAccelerator = ks == null ? "null" : KeyStrokeUtils.keyStrokeToString(ks);
			String accelerator = node.get(key, defaultAccelerator);
			if (accelerator.equals("null"))
				accelerator = null;
			action.putValue(JPatchAction.ACCELERATOR, accelerator);
		}
	}
	
	private void addActions(Map<String, ActionDescriptor> map) {
		/*
		 * BEGIN OF AUTO-GENERATED CODE - DO NOT MODIFY
		 * The following lines have been generated with utilities.Actions
		 * from the file "jpatch/boundary/action/actions.xml"
		 */

		actionMap.put("delete", new ActionDescriptor(new DeleteControlPointAction()));
		actionMap.put("remove", new ActionDescriptor(new RemoveControlPointAction()));
		actionMap.put("insert point", new ActionDescriptor(new InsertControlPointAction()));
		actionMap.put("next curve", new ActionDescriptor(new NextCurveAction(1)));
		actionMap.put("prev curve", new ActionDescriptor(new NextCurveAction(-1)));
		actionMap.put("new", new ActionDescriptor(new NewAction()));
		actionMap.put("new model", new ActionDescriptor(new NewModelAction()));
		actionMap.put("new animation", new ActionDescriptor(new NewAnimAction()));
		actionMap.put("open", new ActionDescriptor(new OpenAction()));
		actionMap.put("open model", new ActionDescriptor(new ImportJPatchAction()));
		actionMap.put("save", new ActionDescriptor(new SaveAsAction(false)));
		actionMap.put("single view", new ActionDescriptor(new ToggleAction(ToggleAction.Type.VIEWPORTS_SINGLE)));
		actionMap.put("horizontally split view", new ActionDescriptor(new ToggleAction(ToggleAction.Type.VIEWPORTS_HORIZONTAL_SPLIT)));
		actionMap.put("vertically split view", new ActionDescriptor(new ToggleAction(ToggleAction.Type.VIEWPORTS_VERTICAL_SPLIT)));
		actionMap.put("quad view", new ActionDescriptor(new ToggleAction(ToggleAction.Type.VIEWPORTS_QUAD)));
		actionMap.put("rotate view", new ActionDescriptor(new ViewRotateAction()));
		actionMap.put("move view", new ActionDescriptor(new ViewMoveAction()));
		actionMap.put("zoom view", new ActionDescriptor(new ViewZoomAction()));
		actionMap.put("zoom to fit", new ActionDescriptor(new ZoomToFitAction()));
		actionMap.put("undo", new ActionDescriptor(new UndoAction()));
		actionMap.put("redo", new ActionDescriptor(new RedoAction()));
		actionMap.put("lock x", new ActionDescriptor(new ToggleAction(ToggleAction.Type.LOCK_X)));
		actionMap.put("lock y", new ActionDescriptor(new ToggleAction(ToggleAction.Type.LOCK_Y)));
		actionMap.put("lock z", new ActionDescriptor(new ToggleAction(ToggleAction.Type.LOCK_Z)));
		actionMap.put("snap to grid", new ActionDescriptor(new ToggleAction(ToggleAction.Type.SNAP_TO_GRID)));
		actionMap.put("hide", new ActionDescriptor(new HideAction()));
		actionMap.put("stop edit morph", new ActionDescriptor(new StopEditMorphAction()));
		actionMap.put("select points", new ActionDescriptor(new ToggleAction(ToggleAction.Type.SELECT_POINTS)));
		actionMap.put("select bones", new ActionDescriptor(new ToggleAction(ToggleAction.Type.SELECT_BONES)));
		actionMap.put("lock points", new ActionDescriptor(new ToggleAction(ToggleAction.Type.LOCK_POINTS)));
		actionMap.put("lock bones", new ActionDescriptor(new ToggleAction(ToggleAction.Type.LOCK_BONES)));
		actionMap.put("default tool", new ActionDescriptor(new SelectMoveAction()));
		actionMap.put("add curve segment", new ActionDescriptor(new AddControlPointAction()));
		actionMap.put("add bone", new ActionDescriptor(new AddBoneAction()));
		actionMap.put("rotate tool", new ActionDescriptor(new RotateAction()));
		actionMap.put("weight selection tool", new ActionDescriptor(new WeightSelectionAction()));
		actionMap.put("knife tool", new ActionDescriptor(new KnifeAction()));
		actionMap.put("anchor tool", new ActionDescriptor(new AnchorAction()));
		actionMap.put("detach", new ActionDescriptor(new DetachControlPointsAction()));
		actionMap.put("rotoscope tool", new ActionDescriptor(new RotoscopeAction()));
		actionMap.put("tangent tool", new ActionDescriptor(new TangentAction()));
		actionMap.put("peak tangents", new ActionDescriptor(new ToggleAction(ToggleAction.Type.TANGENTS_PEAK)));
		actionMap.put("round tangents", new ActionDescriptor(new ToggleAction(ToggleAction.Type.TANGENTS_ROUND)));
		actionMap.put("clone", new ActionDescriptor(new CloneAction()));
		actionMap.put("extrude", new ActionDescriptor(new ExtrudeAction()));
		actionMap.put("lathe", new ActionDescriptor(new LatheAction()));
		actionMap.put("lathe editor", new ActionDescriptor(new LatheEditorAction()));
		actionMap.put("make patch", new ActionDescriptor(new MakeFivePointPatchAction()));
		actionMap.put("compute patches", new ActionDescriptor(new ComputePatchesAction()));
		actionMap.put("open animation", new ActionDescriptor(new ImportJPatchAnimationAction()));
		actionMap.put("append", new ActionDescriptor(new ImportJPatchAction(false)));
		actionMap.put("save as", new ActionDescriptor(new SaveAsAction(true)));
		actionMap.put("import spatch", new ActionDescriptor(new ImportSPatchAction()));
		actionMap.put("import animationmaster", new ActionDescriptor(new ImportAnimationMasterAction()));
		actionMap.put("export aliaswavefront", new ActionDescriptor(new ExportWavefrontAction()));
		actionMap.put("export povray", new ActionDescriptor(new ExportPovrayAction()));
		actionMap.put("export renderman", new ActionDescriptor(new ExportRibAction()));
		actionMap.put("quit", new ActionDescriptor(new QuitAction()));
		actionMap.put("synchronize viewports", new ActionDescriptor(new ToggleAction(ToggleAction.Type.SYNCHRONIZE_VIEWPORTS)));
		actionMap.put("settings", new ActionDescriptor(new EditSettingsAction()));
		actionMap.put("grid spacing settings", new ActionDescriptor(new SetGridSpacingAction()));
		actionMap.put("install jogl", new ActionDescriptor(new InstallJoglAction()));
		actionMap.put("phoneme morph mapping", new ActionDescriptor(new EditPhonemesAction()));
		actionMap.put("dump", new ActionDescriptor(new DumpAction()));
		actionMap.put("dump xml", new ActionDescriptor(new XmlDumpAction()));
		actionMap.put("dump undo stack", new ActionDescriptor(new DumpUndoStackAction()));
		actionMap.put("check model", new ActionDescriptor(new CheckModelAction()));
		actionMap.put("controlpoint browser", new ActionDescriptor(new ControlPointBrowserAction()));
		actionMap.put("show about", new ActionDescriptor(new AboutAction()));
		actionMap.put("show splashscreen", new ActionDescriptor(new ShowSplashAction()));
		actionMap.put("show points", new ActionDescriptor(new ToggleAction(ToggleAction.Type.SHOW_POINTS)));
		actionMap.put("show curves", new ActionDescriptor(new ToggleAction(ToggleAction.Type.SHOW_CURVES)));
		actionMap.put("show patches", new ActionDescriptor(new ToggleAction(ToggleAction.Type.SHOW_PATCHES)));
		actionMap.put("show rotoscope", new ActionDescriptor(new ToggleAction(ToggleAction.Type.SHOW_ROTOSCOPE)));
		actionMap.put("show bones", new ActionDescriptor(new ToggleAction(ToggleAction.Type.SHOW_BONES)));
		actionMap.put("front view", new ActionDescriptor(new ViewAction(ViewDefinition.FRONT)));
		actionMap.put("rear view", new ActionDescriptor(new ViewAction(ViewDefinition.REAR)));
		actionMap.put("top view", new ActionDescriptor(new ViewAction(ViewDefinition.TOP)));
		actionMap.put("bottom view", new ActionDescriptor(new ViewAction(ViewDefinition.BOTTOM)));
		actionMap.put("left view", new ActionDescriptor(new ViewAction(ViewDefinition.LEFT)));
		actionMap.put("right view", new ActionDescriptor(new ViewAction(ViewDefinition.RIGHT)));
		actionMap.put("bird's eye view", new ActionDescriptor(new ViewAction(ViewDefinition.BIRDS_EYE)));
		actionMap.put("set rotoscope image", new ActionDescriptor(new SetRotoscopeAction()));
		actionMap.put("clear rotoscope image", new ActionDescriptor(new ClearRotoscopeAction()));
		actionMap.put("lock view", new ActionDescriptor(new SetViewLockAction(true)));
		actionMap.put("unlock view", new ActionDescriptor(new SetViewLockAction(false)));
		actionMap.put("select all", new ActionDescriptor(new SelectAllAction()));
		actionMap.put("select none", new ActionDescriptor(new SelectNoneAction()));
		actionMap.put("invert selection", new ActionDescriptor(new InvertSelectionAction()));
		actionMap.put("expand selection", new ActionDescriptor(new ExtendSelectionAction()));
		actionMap.put("flip x", new ActionDescriptor(new FlipAction(FlipAction.X)));
		actionMap.put("flip y", new ActionDescriptor(new FlipAction(FlipAction.Y)));
		actionMap.put("flip z", new ActionDescriptor(new FlipAction(FlipAction.Z)));
		actionMap.put("flip patches", new ActionDescriptor(new FlipPatchesAction()));
		actionMap.put("align patches", new ActionDescriptor(new AlignPatchesAction()));
		actionMap.put("align controlpoints", new ActionDescriptor(new AlignAction()));
		actionMap.put("automirror", new ActionDescriptor(new AutoMirrorAction()));
		actionMap.put("add stubs", new ActionDescriptor(new AddStubsAction()));
		actionMap.put("remove stubs", new ActionDescriptor(new RemoveStubsAction()));
		actionMap.put("change tangents: round", new ActionDescriptor(new ChangeTangentModeAction(ChangeTangentModeAction.JPATCH)));
		actionMap.put("change tangents: peak", new ActionDescriptor(new ChangeTangentModeAction(ChangeTangentModeAction.PEAK)));
		actionMap.put("change tangents: spatch", new ActionDescriptor(new ChangeTangentModeAction(ChangeTangentModeAction.SPATCH)));
		actionMap.put("assign controlpoints to bones", new ActionDescriptor(new AssignPointsToBonesAction()));
		actionMap.put("edit keyboard mapping", new ActionDescriptor(new KeyMappingAction()));
		actionMap.put("render animation", new ActionDescriptor(new RenderAnimationAction(false)));
		actionMap.put("render current frame", new ActionDescriptor(new RenderAnimationAction(true)));

		/*
		 * END OF AUTO-GENERATED CODE
		 */
	}
	
	static class ActionDescriptor {
		Action action;
		DefaultButtonModel buttonModel;
		KeyStroke defaultAccelerator;
		boolean bound;
		
		ActionDescriptor(Action action) {
			this.action = action;
//			defaultAccelerator = KeyStroke.getKeyStroke((String) action.getValue(JPatchAction.ACCELERATOR));
		}
		
		public String toString() {
			return getClass().getName() + "@" + hashCode() + "\naction=" + action + "\nbuttonModel=" + buttonModel + "\nkeyStroke=" + defaultAccelerator + "\nbound=" + bound;
		}
	}
	
	static class DummyAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) { } // do nothing
	}
}
