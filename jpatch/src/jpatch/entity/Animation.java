package jpatch.entity;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.PrintStream;
import java.util.*;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonModel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JToggleButton;
import javax.swing.tree.*;

import jpatch.boundary.*;
import jpatch.boundary.action.*;
import jpatch.boundary.sidebar.SidePanel;
import jpatch.boundary.ui.JPatchDummyButton;
import jpatch.boundary.ui.JPatchRadioButtonMenuItem;

public class Animation implements MutableTreeNode {

	private float fStart = 0;
	private float fEnd = 200;
	private float fPosition = 0;
	private float fFramerate = 24;
	
	private List<AnimModel> listModels = new ArrayList<AnimModel>();
	private List<AnimLight> listLights = new ArrayList<AnimLight>();
	private List<Camera> listCameras = new ArrayList<Camera>();
	private List<JPatchDummyButton> dummyButtonList = new ArrayList<JPatchDummyButton>();
	private Map<AnimObject, MotionCurveSet> mapMotionCurves = new HashMap<AnimObject, MotionCurveSet>();
	private AnimTreeNode treenodeModels = new AnimTreeNode("Models", listModels);
	private AnimTreeNode treenodeLights = new AnimTreeNode("Lights", listLights);
	private AnimTreeNode treenodeCameras = new AnimTreeNode("Cameras", listCameras);
	private Map<Model, Pose> mapClipboardPose = new HashMap<Model, Pose>();
	private File file;
	
	private RenderExtension re = new RenderExtension(new String[] {
			"povray", "",
			"renderman", ""
	});
	
	private boolean bInserted;
	private String strName = "New Animation";
	
//	public Animation() {
//		addCamera(new Camera("Camera 1"), null);
//	}
	
	public float getEnd() {
		return fEnd;
	}

	public void setEnd(float end) {
		fEnd = end;
	}

	public float getFramerate() {
		return fFramerate;
	}

	public void setFramerate(float framerate) {
		fFramerate = framerate;
	}

	public float getPosition() {
		return fPosition;
	}

	public Camera getActiveCamera() {
		return listCameras.get(0);
	}
	
	public Pose getClipboardPose(Model model) {
		Pose pose = mapClipboardPose.get(model);
		if (pose == null) {
			pose = new Pose(model);
			mapClipboardPose.put(model, pose);
		}
		return pose;
	}
	
	public void setPosition(float position) {
		if (position < fStart)
			position = fStart;
		else if (position > fEnd)
			position = fEnd;
		fPosition = position;
//		System.out.println("position = " + position);
		for (AnimModel animModel:listModels) {
			mapMotionCurves.get(animModel).setPosition(position);
//			animModel.getModel().applyMorphs();
//			animModel.getModel().setPose();
		}
		for (AnimLight animLight:listLights) {
			mapMotionCurves.get(animLight).setPosition(position);
		}
		for (Camera camera:listCameras) {
			mapMotionCurves.get(camera).setPosition(position);
		}
		MainFrame.getInstance().getTimelineEditor().setCurrentFrame((int) position);
		MainFrame.getInstance().getVcrControls().setPosition((int) position);
		MainFrame.getInstance().getSideBar().updatePanel();
	}

	public void rethink() {
		setPosition(fPosition);
	}
	
	public float getStart() {
		return fStart;
	}

	public void setStart(float start) {
		fStart = start;
	}	
	
	public void addModel(AnimModel animModel, MotionCurveSet mcs) {
		System.out.println(this.getName() + " add model " + animModel.getModel().xml(""));
		MainFrame.getInstance().getTreeModel().insertNodeInto(animModel, treenodeModels, listModels.size());
		setCurvesetFor(animModel, mcs);
	}
	
	public void addLight(AnimLight animLight, MotionCurveSet mcs) {
		MainFrame.getInstance().getTreeModel().insertNodeInto(animLight, treenodeLights, listLights.size());
		setCurvesetFor(animLight, mcs);
	}
	
	public void addCamera(final Camera camera, MotionCurveSet mcs) {
//		if (MainFrame.getInstance().getAnimation() != null)
		MainFrame.getInstance().getTreeModel().insertNodeInto(camera, treenodeCameras, listCameras.size());
		Actions.getInstance().addAction("camera" + camera.hashCode(), new ViewAction(camera), new JToggleButton.ToggleButtonModel());
		setupViewCameraMenu();
		setCurvesetFor(camera, mcs);
	}
	
	private void removeAnimObject(AnimObject animObject) {
		if (animObject instanceof Camera)
			MainFrame.getInstance().getJPatchScreen().checkCameraViewports((Camera) animObject);
		if (MainFrame.getInstance().getTimelineEditor().getAnimObject() == animObject) {
			MainFrame.getInstance().getTimelineEditor().setAnimObject(null);
			MainFrame.getInstance().getTimelineEditor().repaint();
		}
		Selection selection = MainFrame.getInstance().getSelection();
		if (selection != null && selection.getHotObject() == animObject) {
			MainFrame.getInstance().setSelection(null);
		}
		MainFrame.getInstance().getSideBar().clearSidePanels();
	}
	
	
	public void removeModel(AnimModel animModel) {
		MainFrame.getInstance().getTreeModel().removeNodeFromParent(animModel);
		removeAnimObject(animModel);
	}
	
	public void removeLight(AnimLight animLight) {
		MainFrame.getInstance().getTreeModel().removeNodeFromParent(animLight);
		removeAnimObject(animLight);
	}
	
	public void removeCamera(Camera camera) {
		MainFrame.getInstance().getTreeModel().removeNodeFromParent(camera);
		Actions.getInstance().removeAction("camera" + camera.hashCode());
		setupViewCameraMenu();
		removeAnimObject(camera);
	}
	
	public List<AnimModel> getModels() {
		return listModels;
	}
	
	public List<AnimLight> getLights() {
		return listLights;
	}
	
	public List<Camera> getCameras() {
		return listCameras;
	}
	
	public AnimTreeNode getTreenodeModels() {
		return treenodeModels;
	}
	
	public Iterable<AnimObject> getObjects() {
		return new Iterable<AnimObject>() {
			public Iterator<AnimObject> iterator() {
				final Iterator<AnimModel> itModels = listModels.iterator();
				final Iterator<AnimLight> itLights = listLights.iterator();
				final Iterator<Camera> itCameras = listCameras.iterator();
				return new Iterator<AnimObject>() {
					public boolean hasNext() {
						return itCameras.hasNext();
					}
					public AnimObject next() {
						if (itModels.hasNext())
							return itModels.next();
						if (itLights.hasNext())
							return itLights.next();
						return itCameras.next();
					}
					public void remove() {
						throw new UnsupportedOperationException();
					}
				};
			}
		};
	}
	
	public AnimTreeNode getTreenodeLights() {
		return treenodeLights;
	}
	
	public AnimTreeNode getTreenodeCameras() {
		return treenodeCameras;
	}
	
	@Override
	public String toString() {
		return strName;
	}
	
	public String getName() {
		return strName;
	}
	
	public void setName(String name) {
		strName = name;
	}
	
	public MotionCurveSet getCurvesetFor(AnimObject animObject) {
		return mapMotionCurves.get(animObject);
	}
	
	private void setCurvesetFor(AnimObject animObject, MotionCurveSet mcs) {
		if (mcs == null)
			mcs = MotionCurveSet.createMotionCurveSetFor(animObject);
		mapMotionCurves.put(animObject, mcs);
	}
	
	public void setRenderString(String format, String version, String renderString) {
		re.setRenderString(format, version, renderString);
	}
	
	public String getRenderString(String format, String version) {
		return re.getRenderString(format, version);
	}
	
	public StringBuffer renderStrings(String prefix) {
		return re.xml(prefix);
	}
	
	public void dump() {
		System.out.println("Current position:" + getPosition());
		xml(System.out, "\t");
	}
	
	public void xml(PrintStream out, String prefix) {
		out.append(prefix).append("<choreography>\n");
		out.append(prefix).append("\t<name>" + strName + "</name>\n");
		out.append(prefix).append("\t<start>" + fStart + "</start>\n");
		out.append(prefix).append("\t<end>" + fEnd + "</end>\n");
		out.append(prefix).append("\t<framerate>" + fFramerate + "</framerate>\n");
		out.append(prefix).append(renderStrings("\t")).append("\n");
		for (AnimObject animObject:getObjects())
			animObject.xml(out, prefix + "\t");
		out.append(prefix).append("</choreography>\n");
	}
	
/* MutableTreeNode interface implementation */
	
	public void insert(MutableTreeNode child, int index) {
		throw new UnsupportedOperationException("Can't insert nodes into model");
	}

	public void remove(int index) {
		throw new UnsupportedOperationException("Can't remove nodes from model");
	}

	public void remove(MutableTreeNode node) {
		throw new UnsupportedOperationException("Can't remove nodes from model");
	}

	public void setUserObject(Object object) {
		throw new UnsupportedOperationException();
	}

	public void removeFromParent() {
		throw new UnsupportedOperationException();
	}

	public void setParent(MutableTreeNode newParent) {
		bInserted = true;
	}

	public TreeNode getChildAt(int childIndex) {
		switch(childIndex) {
		case 0:
			return treenodeModels;
		case 1:
			return treenodeLights;
		case 2:
			return treenodeCameras;
		}
		throw new ArrayIndexOutOfBoundsException();
	}

	public int getChildCount() {
		return 3;
	}

	public TreeNode getParent() {
		return bInserted ? MainFrame.getInstance().getRootTreenode() : null;
	}

	public int getIndex(TreeNode node) {
		return 0;
	}

	public boolean getAllowsChildren() {
		return true;
	}

	public boolean isLeaf() {
		return false;
	}

	public Enumeration children() {
		return new Enumeration() {
			private int i = 0;
			public boolean hasMoreElements() {
				return i < 4;
			}

			public Object nextElement() {
				return getChildAt(i++);
			}
		};
	}
	
	private void setupViewCameraMenu() {
		JMenu viewCameraMenu = MainFrame.getInstance().getViewCameraMenu();
		viewCameraMenu.removeAll();
		for (JPatchDummyButton button : dummyButtonList)
			Actions.getInstance().getButtonGroup("view").remove(button);
		dummyButtonList.clear();
		
		for (final Camera cam : listCameras) {
			JToggleButton.ToggleButtonModel buttonModel = (JToggleButton.ToggleButtonModel) Actions.getInstance().getButtonModel("camera" + cam.hashCode());
			JPatchRadioButtonMenuItem menuItem = new JPatchRadioButtonMenuItem(buttonModel) {
				@Override
				public String getText() {
					return cam.getName();
				}
			};
			JPatchDummyButton dummyButton = new JPatchDummyButton(buttonModel);
			dummyButtonList.add(dummyButton);
			Actions.getInstance().getButtonGroup("view").add(dummyButton);
			menuItem.addActionListener(Actions.getInstance().getAction("camera" + cam.hashCode()));
			viewCameraMenu.add(menuItem);
		}
	}
	
	private class AnimTreeNode implements MutableTreeNode {
		private List list;
		private String strName;
		
		public AnimTreeNode(String name, List list) {
			strName = name;
			this.list = list;
		}
		
		public String toString() {
			return strName;
		}
		
		public void setUserObject(Object object) {
			throw new UnsupportedOperationException();
		}

		public void removeFromParent() {
			throw new UnsupportedOperationException();
		}

		public void setParent(MutableTreeNode newParent) {
		}

		public TreeNode getParent() {
			return Animation.this;
		}

		public boolean getAllowsChildren() {
			return true;
		}

		public void insert(MutableTreeNode child, int index) {
			list.add(index, child);
			child.setParent(this);
		}

		public void remove(int index) {
			list.remove(index);
		}

		public void remove(MutableTreeNode node) {
			list.remove(node);
		}

		public TreeNode getChildAt(int childIndex) {
			return (TreeNode) list.get(childIndex);
		}

		public int getChildCount() {
			return list.size();
		}

		public int getIndex(TreeNode node) {
			return list.indexOf(node);
		}

		public boolean isLeaf() {
			return list.size() <= 0;
		}

		public Enumeration children() {
			return Collections.enumeration(list);
		}
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}
}
