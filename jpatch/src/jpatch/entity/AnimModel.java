package jpatch.entity;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;

import jpatch.boundary.*;

import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.vecmath.*;

public class AnimModel extends AnimObject {
	private boolean bParent = false;
	
	protected RenderExtension re = new RenderExtension(new String[] {
		"povray", "",
		"renderman", "Attribute \"visibility\" \"string transmission\" [\"shader\"]\n"
	});
	protected Model model;
	protected int iSubdivisionOffset = 0;
	
	public AnimModel() { }
	
	public AnimModel(Model model) {
		strName = model.getName();
		this.model = model;
	}
	
	public AnimModel(String name, Model model) {
		strName = name;
		this.model = model;
	}
	
	public Model getModel() {
		return model;
	}
	
	public String toString() {
		return strName;
	}
	
	public void setModel(Model model) {
		this.model = model;
	}
	
	public void setName(String name) {
		strName = name;
	}
	
	public void setSubdivisionOffset(int offset) {
		iSubdivisionOffset = offset;
	}
	
	public int getSubdivisionOffset() {
		return iSubdivisionOffset;
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
	
	public void removeFromParent() {
		MainFrame.getInstance().getAnimation().removeModel(this);
	}

	public void setParent(MutableTreeNode newParent) {
		bParent = true;
	}

	public TreeNode getParent() {
		return bParent ? MainFrame.getInstance().getAnimation().getTreenodeModels() : null;
	}
	
	public TreeNode getChildAt(int childIndex) {
		switch(childIndex) {
		case 0:
			return model.getTreenodeExpressions();
		case 1:
			return model.getTreenodeBones();
		}
		throw new ArrayIndexOutOfBoundsException();
//		return (TreeNode) model.getMorphList().get(childIndex);
	}

	public int getChildCount() {
//		return model.getMorphList().size();
//		return 0;
		return 2;
	}

	public int getIndex(TreeNode node) {
		return model.getMorphList().indexOf(node);
//		return -1;
	}

	public boolean getAllowsChildren() {
		return true;
//		return false;
	}

	public boolean isLeaf() {
		return false;
//		return true;
	}

	public Enumeration children() {
//		return Collections.enumeration(model.getMorphList());
//		return null;
		return new Enumeration() {
			private int i = 0;
			public boolean hasMoreElements() {
				return i < 2;
			}

			public Object nextElement() {
				return getChildAt(i++);
			}
		};
	}
}
