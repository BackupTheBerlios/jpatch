package jpatch.entity;


import java.io.File;
import java.util.*;

import javax.swing.ListModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.vecmath.*;
import jpatch.auxilary.*;
import jpatch.control.edit.*;
import jpatch.boundary.*;

/**
 *  Description of the Class
 *
 * @author     aledinsk
 * @created    02. Mai 2003
 */
public class Model implements MutableTreeNode {

	/**
	 *  Description of the Field
	 */
	private Set<ControlPoint> setCurves = new HashSet<ControlPoint>();
	private Set<Bone> setBones = new HashSet<Bone>();
	private Map<Patch, Patch> mapPatches = new HashMap<Patch, Patch>();

	private MutableTreeNode treenodeSelections;
	private MutableTreeNode treenodeMaterials;
	private MutableTreeNode treenodeMorphs; 
	private MutableTreeNode treenodeBones;
	
	private ArrayList lstCandidateFivePointPatch = new ArrayList();
	
	//private JPatchMaterial[] aJPMaterial = new JPatchMaterial[32];
	
	private AnimModel animModel;
	private List<JPatchMaterial> listMaterials = new ArrayList<JPatchMaterial>();
	private List listSelections = new ArrayList();
	private List<Morph> listMorphs = new ArrayList<Morph>();
//	private List lstBoneShapes = new ArrayList();
	private Map<String, Morph> mapPhonemes = new HashMap<String, Morph>();
	private String strName;
	private Rotoscope[] aRotoscope = new Rotoscope[6];
	private boolean bInserted = false;
	private File file;
//	private Map<ControlPoint, Integer> cpIdMap = new HashMap<ControlPoint, Integer>();
//	private Map<Bone, Integer> boneIdMap = new HashMap<Bone, Integer>();
//	private Map<Object, Integer> objectIdMap = new HashMap<Object, Integer>();
	
	private Set<String> boneNameSet = new HashSet<String>();
	private Set<String> materialNameSet = new HashSet<String>();
	private Set<String> selectionNameSet = new HashSet<String>();
	private Set<String> morphNameSet = new HashSet<String>();
	//private ArrayList listeners = new ArrayList();
	
	public Model() {
		strName = "New Model";
		treenodeSelections = new ModelTreeNode("Selections", listSelections);
		treenodeMaterials = new ModelTreeNode("Materials", listMaterials);
		treenodeMorphs = new ModelTreeNode("Morphs", listMorphs);
		treenodeBones = new ModelTreeNode("Bones", new ArrayList());
		JPatchMaterial material = new JPatchMaterial(new Color3f(1,1,1));
		material.setName("Default Material");
		listMaterials.add(material);
		materialNameSet.add(material.getName());
		JPatchMaterial.setNextNumber(1);
//		treenodeMaterials.insert(material, 0);
		
//		addBone(new Bone(this, new Point3f(0, 0, 0), new Vector3f(1, 0, 0)));
//		addBone(new Bone(this, new Point3f(1, 0, 0), new Vector3f(1, 0, 0)));
//		addBone(new Bone(this, new Point3f(2, 0, 0), new Vector3f(1, 0, 0)));
//		addBone(new Bone(this, new Point3f(3, 0, 0), new Vector3f(1, 0, 0)));
//		addBone(new Bone(this, new Point3f(4, 0, 0), new Vector3f(1, 0, 0)));
//		addBone(new Bone(this, new Point3f(5, 0, 0), new Vector3f(1, 0, 0)));
	}
	
	public void setRotoscope(int view, Rotoscope rotoscope) {
		if (view >= 1 && view <= 6) {
			aRotoscope[view - 1] = rotoscope;
		}
	}
	
	public Rotoscope getRotoscope(int view) {
		if (view >= 1 && view <= 6) {
			return aRotoscope[view - 1];
		} else {
			return null;
		}
	}
	
//	public Map<Integer, ControlPoint> getControlPointIds() {
//		Map<Integer, ControlPoint> map = new HashMap<Integer, ControlPoint>();
//		for (ControlPoint cp : setCurves) {
//			for(; cp != null; cp = cp.getNextCheckNextLoop())
//				map.put(cp.getId(), cp);
//		}
//		return map;
//	}
//	
//	public Map<String, Bone> getBoneNames() {
//		Map<String, Bone> map = new HashMap<String, Bone>();
//		for (Bone bone : setBones) {
//			map.put(bone.getName(), bone);
//		}
//		return map;
//	}
	
//	public void setObjectId(Object object, int id) {
//		objectIdMap.put(object, id);
//	}
//	
//	public int getObjectId(Object object) {
//		return objectIdMap.get(object);
//	}
	
//	public void setObjectId(Object object, int id) {
//		if (object instanceof ControlPoint)
//			cpIdMap.put((ControlPoint) object, id);
//		else if (object instanceof Bone)
//			boneIdMap.put((Bone) object, id);
//		else
//			throw new IllegalArgumentException();
//	}
//	
//	public int getObjectId(Object object) {
//		if (object instanceof ControlPoint)
//			cpIdMap.put((ControlPoint) object, id);
//		else if (object instanceof Bone)
//			boneIdMap.put((Bone) object, id);
//		else
//			throw new IllegalArgumentException();
//		
//		return objectIdMap.get(object);
//	}
	
	//public StringBuffer xmlRotoscopes(int tabs) {
	//	StringBuffer sb = new StringBuffer();
	//	String[] viewName = new String[] { "front","rear","top","bottom","left","right" };
	//	for (int i = 0; i < 6; i++) {
	//		if (aRotoscope[i] != null) {
	//			sb.append(aRotoscope[i].xml(tabs,viewName[i]));
	//		}
	//	}
	//	return sb;
	//}
	
	public String toString() {
		return strName;
	}
	
	public StringBuffer xml(String prefix) {
		String prefix2 = prefix + "\t";
		String prefix3 = prefix + "\t\t";
		String prefix4 = prefix + "\t\t\t";
		StringBuffer sb = new StringBuffer();
		sb.append(prefix).append("<model>").append("\n");
		sb.append(prefix2).append("<name>").append(strName).append("</name>").append("\n");
		/*
		for (int m = 0; m < 32; m++) {
			if (aJPMaterial[m] != null) {
				sb.append(aJPMaterial[m].xml(tab + 1,m));
			}
		}
		*/
		
		//sb.append(MainFrame.getInstance().getJPatchScreen().xmlRotoscopes(1));
		
		String[] viewName = new String[] { "front","rear","top","bottom","left","right" };
		for (int i = 0; i < 6; i++) {
			if (aRotoscope[i] != null) {
				sb.append(aRotoscope[i].xml(prefix2, viewName[i]));
			}
		}
		
		int n = 0;
		for (Iterator it = listMaterials.iterator(); it.hasNext();) {
			JPatchMaterial mat = (JPatchMaterial) it.next();
//			mat.setXmlNumber(n++);
			sb.append(mat.xml(prefix2));
		}
		
//		objectIdMap.clear();
//		ArrayList curveList = new ArrayList(setCurves);
//		setCpMap(curveList);
//		ArrayList boneList = new ArrayList(setBones);
//		setBoneMap(boneList);
		sb.append(prefix2).append("<mesh>\n");
		for (ControlPoint start : setCurves) {
			sb.append(prefix3);
			sb.append(start.getLoop() ? "<curve closed=\"true\">" : "<curve>");
			sb.append("\n");
			for (ControlPoint cp = start; cp != null; cp = cp.getNextCheckNextLoop())
				sb.append(cp.xml(prefix4));
			
			sb.append(prefix3).append("</curve>").append("\n");
		}
		for (Iterator it = mapPatches.keySet().iterator(); it.hasNext(); ) {
			sb.append(((Patch) it.next()).xml(prefix3));
		}
		for (Iterator it = listMorphs.iterator(); it.hasNext(); ) {
			Morph morph = (Morph) it.next();
			sb.append(morph.xml(prefix3));
		}
//		for (Iterator itBones = setBones.iterator(); itBones.hasNext(); ) {
//			for (Iterator itDofs = ((Bone) itBones.next()).getDofs().iterator(); itDofs.hasNext(); ) {
//				RotationDof dof = (RotationDof) itDofs.next();
//				//FIXME
////				sb.append(dof.getMinMorph().xml(prefix3, dof, "min"));
////				sb.append(dof.getMaxMorph().xml(prefix3, dof, "max"));
//			}
//		}
		StringBuffer lipSyncMap = new StringBuffer();
		for (Iterator it = mapPhonemes.keySet().iterator(); it.hasNext(); ) {
			String phoneme = (String) it.next();
			Morph morph = mapPhonemes.get(phoneme);
			if (morph != null) lipSyncMap.append(prefix).append("\t\t<map phoneme=\"" + phoneme + "\" morph=\"" + listMorphs.indexOf(morph) + "\"/>").append("\n");
		}
		if (lipSyncMap.length() > 0) {
			sb.append(prefix).append("\t<lipsync>").append("\n");
			sb.append(lipSyncMap);
			sb.append(prefix).append("\t</lipsync>").append("\n");
		}
		sb.append(prefix2).append("</mesh>").append("\n");
		sb.append(prefix2).append("<selections>\n");
		for (Iterator it = listSelections.iterator(); it.hasNext();) {
			Selection selection = (Selection) it.next();
			sb.append(selection.xml(prefix3, this));
		}
		sb.append(prefix2).append("</selections>\n");
		
		sb.append(prefix2).append("<skeleton>\n");
		for (Bone bone : setBones) {
			sb.append(bone.xml(prefix3));
		}
		sb.append(prefix2).append("</skeleton>\n");
		sb.append(prefix).append("</model>").append("\n");
		return sb;
	}
	
	/*
	public void addMaterial(JPatchMaterial material, int n) {
		if (aJPMaterial[n] == null) {
			treenodeMaterials.add(material);
		}
		aJPMaterial[n] = material;
		material.setNumber(n);
	}
	*/
	
	public AnimModel getAnimModel() {
		return animModel;
	}

	public void setAnimModel(AnimModel animModel) {
		this.animModel = animModel;
	}
	
	public String getName() {
		return strName;
	}
	
	public void setName(String name) {
		strName = name;
	}
	
	public void setMorphFor(String phoneme, Morph morph) {
		mapPhonemes.put(phoneme, morph);
	}
	
	public Morph getMorphFor(String phoneme) {
		return (Morph) mapPhonemes.get(phoneme);
	}
	
	public Set getPhonemeMorphSet() {
		HashSet set = new HashSet();
		for (Iterator it = mapPhonemes.keySet().iterator(); it.hasNext(); ) {
			set.add(mapPhonemes.get(it.next()));
		}
		return set;
	}
	
	public void addMaterial(JPatchMaterial material) {
		/*
		for (int m = 0; m < 32; m++) {
			if (aJPMaterial[m] == null) {
				aJPMaterial[m] = material;
				material.setNumber(m);
				treenodeMaterials.add(material);
				return true;
			}
		}
		return false;
		*/
		System.out.println(material.getName() + " " + materialNameSet);
		int n = 1;
		String name = material.getName();
		String name1 = name;
		while (materialNameSet.contains(name1))
			name1 = name + "(" + n++ + ")";
		material.setName(name1);
		materialNameSet.add(name1);
		
		if (MainFrame.getInstance() != null)
			MainFrame.getInstance().getTreeModel().insertNodeInto(material, treenodeMaterials, treenodeMaterials.getChildCount());
//		MainFrame.getInstance().getTreeModel().nodeStructureChanged(treenodeMaterials);
//		treenodeMaterials.insert(material, 0);
//		lstMaterials.add(material);
//		MainFrame.getInstance().getTreeModel().nodesWereInserted(treenodeMaterials, new int[] { treenodeMaterials.getChildCount() } );
//		return true;
	}
	
	public boolean checkSelection(Selection selection) {
		for (int i = 0, n = listSelections.size(); i < n; i++) {
			Selection sel = (Selection) listSelections.get(i);
//			System.out.println(sel + " " + selection + " " + selection.getMap().equals(sel.getMap()));
			if (selection.getMap().equals(sel.getMap()))
				return false;
		}
		return true;
	}
	
	public Selection getSelection(Selection selection) {
		for (int i = 0, n = listSelections.size(); i < n; i++) {
			Selection sel = (Selection) listSelections.get(i);
			if (selection.getMap().equals(sel.getMap()))
				return sel;
		}
		return null;
	}
	
	public void addSelection(Selection selection) {
		/*
		for (int m = 0; m < 32; m++) {
			if (aJPMaterial[m] == null) {
				aJPMaterial[m] = material;
				material.setNumber(m);
				treenodeMaterials.add(material);
				return true;
			}
		}
		return false;
		*/
		//if (!lstSelections.contains(selection)) {
//			treenodeSelections.insert(selection, 0);
//			lstSelections.add(selection);
		//	return true;
		//} else {
		//	return false;
		//}
		if (MainFrame.getInstance() != null && MainFrame.getInstance().getModel() != null)
			addSelection(treenodeSelections.getChildCount(), selection);
	}

	public void addSelection(int index, Selection selection) {
//			treenodeSelections.insert(selection, index);
//			lstSelections.add(index, selection);
		int n = 1;
		String name = selection.getName();
		String name1 = name;
		while (selectionNameSet.contains(name1))
			name1 = name + "(" + n++ + ")";
		selection.setName(name1);
		selectionNameSet.add(name1);
		
		if (MainFrame.getInstance() != null)
			MainFrame.getInstance().getTreeModel().insertNodeInto(selection, treenodeSelections, index);
	}
	
	public void getBounds(Point3f min, Point3f max) {
		min.set(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
		max.set(-Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE);
		for (Iterator it = mapPatches.keySet().iterator(); it.hasNext(); ) {
			Patch patch = (Patch) it.next();
			ControlPoint[] acp = patch.getControlPoints();
			for (int i = 0; i < acp.length; i++) {
				Point3f p = acp[i].getPosition();
				if (p.x < min.x) min.x = p.x;
				if (p.y < min.y) min.y = p.y;
				if (p.z < min.z) min.z = p.z;
				if (p.x > max.x) max.x = p.x;
				if (p.y > max.y) max.y = p.y;
				if (p.z > max.z) max.z = p.z;
			}
		}
	}
	
	public void addExpression(Morph morph) {
//		treenodeExpressions.insert(morph, 0);
//		lstMorphs.add(morph);
		int n = 1;
		String name = morph.getName();
		String name1 = name;
		while (morphNameSet.contains(name1))
			name1 = name + "(" + n++ + ")";
		morph.setName(name1);
		morphNameSet.add(name1);
		MainFrame.getInstance().getTreeModel().insertNodeInto(morph, treenodeMorphs, treenodeMorphs.getChildCount());
	}
	
	public void setReferenceGeometry() {
		//FIXME
//		unapplyMorphs();
//		for (Iterator it = setCurves.iterator(); it.hasNext(); ) {
//			for (ControlPoint cp = (ControlPoint) it.next(); cp != null; cp = cp.getNextCheckNextLoop()) {
//				cp.setReference();
//			}
//		}
//		applyMorphs();
	}
	
	public void unapplyMorphs() {
//		FIXME
//		for (Iterator it = lstMorphs.iterator(); it.hasNext(); ) {
//			((MorphTarget) it.next()).unapply();
//		}
	}
	
	public void applyMorphs() {
		/* reset morph vectors */
		for (Iterator itCurves = setCurves.iterator(); itCurves.hasNext(); ) {
			for (ControlPoint cp = (ControlPoint) itCurves.next(); cp != null; cp = cp.getNextCheckNextLoop())
				cp.getMorphVector().set(0, 0, 0);
		}
		
		/* apply morphs */
		for (Iterator itMorphs = listMorphs.iterator(); itMorphs.hasNext(); ) {
			Morph morph = (Morph) itMorphs.next();
			Map morphMap = morph.getMorphMap();
			for (Iterator itCps = morphMap.keySet().iterator(); itCps.hasNext(); ) {
				ControlPoint cp = (ControlPoint) itCps.next();
				cp.getMorphVector().add((Vector3f) morphMap.get(cp));
			}
		}
		for (Iterator itBones = setBones.iterator(); itBones.hasNext(); ) {
			for (Iterator itDofs = ((Bone) itBones.next()).getDofs().iterator(); itDofs.hasNext(); ) {
				Morph morph = (Morph) itDofs.next();
				Map morphMap = morph.getMorphMap();
				for (Iterator itCps = morphMap.keySet().iterator(); itCps.hasNext(); ) {
					ControlPoint cp = (ControlPoint) itCps.next();
					cp.getMorphVector().add((Vector3f) morphMap.get(cp));
				}
			}
		}
	}
	
	public void setMorphPose() {
		/* apply pose */
		for (Iterator itCurves = setCurves.iterator(); itCurves.hasNext(); ) {
			for (ControlPoint cp = (ControlPoint) itCurves.next(); cp != null; cp = cp.getNextCheckNextLoop())
				cp.setMorphPose();
		}
	}
	
	public void setPose() {
		for (Iterator itCurves = setCurves.iterator(); itCurves.hasNext(); ) {
			for (ControlPoint cp = (ControlPoint) itCurves.next(); cp != null; cp = cp.getNextCheckNextLoop()) {
				cp.setBonePose();
				cp.setMorphPose();
			}
		}
	}
	
//	public Iterator getSelectionIterator() {
//		return lstSelections.iterator();
//	}
	
	public List getSelections() {
		return listSelections;
	}
	
	public Iterator getMorphIterator() {
		return listMorphs.iterator();
	}
	
	public List<Morph> getMorphList() {
		return listMorphs;
	}
	
//	public List getBoneShapeList() {
//		return lstBoneShapes;
//	}
	
	public String renameBone(Bone bone, String name) {
		if (boneNameSet.contains(name))
			return bone.getName();
		boneNameSet.remove(bone.getName());
		boneNameSet.add(name);
		bone.setName(name);
		return name;
	}
	
	public String renameExpression(Morph morph, String name) {
		if (morphNameSet.contains(name))
			return morph.getName();
		morphNameSet.remove(morph.getName());
		morphNameSet.add(name);
		morph.setName(name);
		return name;
	}
	
	public String renameMaterial(JPatchMaterial material, String name) {
		if (materialNameSet.contains(name))
			return material.getName();
		materialNameSet.remove(material.getName());
		materialNameSet.add(name);
		material.setName(name);
		return name;
	}
	
	public String renameSelection(Selection selection, String name) {
		if (selectionNameSet.contains(name))
			return selection.getName();
		selectionNameSet.remove(selection.getName());
		selectionNameSet.add(name);
		selection.setName(name);
		return name;
	}
	
	public void removeSelection(Selection selection) {
		selectionNameSet.remove(selection.getName());
		MainFrame.getInstance().getTreeModel().removeNodeFromParent(selection);
	}
	
	public MutableTreeNode getTreenodeSelections() {
		return treenodeSelections;
	}
	public MutableTreeNode getTreenodeMaterials() {
		return treenodeMaterials;
	}
	public MutableTreeNode getTreenodeExpressions() {
		return treenodeMorphs;
	}
	public MutableTreeNode getTreenodeBones() {
		return treenodeBones;
	}
	
	public void removeMaterial(JPatchMaterial material) {
		materialNameSet.remove(material.getName());
		MainFrame.getInstance().getTreeModel().removeNodeFromParent(material);
	}
	
	public void removeExpression(Morph morph) {
		morphNameSet.remove(morph.getName());
		MainFrame.getInstance().getTreeModel().removeNodeFromParent(morph);
	}
	
	// accessor methods
	public void addCandidateFivePointPatchList(ArrayList list) {
		lstCandidateFivePointPatch.addAll(list);
	}
	
	public JPatchMaterial getMaterial(int m) {
		//if (m > 31) m = 0;
		return (JPatchMaterial)listMaterials.get(m);
	}
	
	public List<JPatchMaterial> getMaterialList() {
		return listMaterials;
	}
	
	

//	public MutableTreeNode getRootBone() {
//		return treenodeBones;
//	}
	
	
	public void addCurve(ControlPoint start) {
		setCurves.add(start);
	}

	public void removeCurve(ControlPoint start) {
		setCurves.remove(start);
	}

	public String addBone(Bone bone) {
		int n = 1;
		String name = bone.getName();
		String name1 = name;
		while (boneNameSet.contains(name1))
			name1 = name + "(" + n++ + ")";
		bone.setName(name1);
		boneNameSet.add(name1);
		setBones.add(bone);
//		System.out.println("addBone() called");
//		System.out.println("parent = " + bone.getParent());
		MutableTreeNode node = (MutableTreeNode) bone.getParent();
//		System.out.println(bone + " " + node);
		if (node == null)
			node = treenodeBones;
		if (MainFrame.getInstance() != null)
			MainFrame.getInstance().getTreeModel().insertNodeInto(bone, node, node.getChildCount());	
//		DefaultTreeModel treeModel = (DefaultTreeModel) MainFrame.getInstance().getTree().getModel();
//		treeModel.reload(bone.getParent());
//		if (bone.getParentBone() == null)
//			treenodeBones.add(bone);
		setPose();
		return name1;
	}

	public void removeBone(Bone bone) {
//		System.out.println("remove bone from " + bone.getParent());
		MainFrame.getInstance().getTreeModel().removeNodeFromParent(bone);
		setBones.remove(bone);
		boneNameSet.remove(bone.getName());
		setPose();
	}
	
	
//	public void addDof(RotationDof dof) {
//		dof.getBone().addDof(dof);
//	}
//	
//	public void removeDof(RotationDof dof) {
//		dof.getBone().removeDof(dof);
//	}
	
	/**
	 *  Adds a feature to the Patch attribute of the Model object
	 *
	 * @param  patch  The feature to be added to the Patch attribute
	 */
	public void addPatch(ControlPoint[] acp, JPatchActionEdit edit) {
		Patch patch = new Patch(acp);
		if (patch.getMaterial() == null)
			patch.setMaterial(listMaterials.get(0));
		addPatch(patch, edit);
	}

	public void addPatch(Patch patch, JPatchActionEdit edit) {
//		System.out.print("addPatch " + patch + " ");
		if (!mapPatches.keySet().contains(patch)) {
//			System.out.println("NEW");
			if (edit == null) {
				mapPatches.put(patch, patch);
			} else {
				edit.addEdit(new AtomicAddPatch(patch));
			}
		} else {
			((Patch) mapPatches.get(patch)).setValid(true);
		}
	}
	
	public void removePatch(Patch patch, JPatchActionEdit edit) {
		if (edit == null) {
			mapPatches.remove(patch);
		} else {
			edit.addEdit(new AtomicRemovePatch(patch));
		}
	}
	
	public void removeMorph(MorphTarget morph) {
		listMorphs.remove(morph);
	}
	
//	/**
//	 *  Removes all patches which contain cp
//	 */
//	public ArrayList getPatchesContaining(ControlPoint cp) {
//		ArrayList list = new ArrayList();
//		for (Iterator it = mapPatches.keySet().iterator(); it.hasNext(); ) {
//			Patch p = (Patch) it.next();
//			if (p.contains(cp))
//				list.add(p);
//		}
//		return list;
//	}
	
	/**
	 *  Description of the Method
	 */
	public void clearPatches() {
		//firstPatch = null;
		//lastPatch = null;
		for (Iterator it = mapPatches.keySet().iterator(); it.hasNext(); )
			((Patch) it.next()).setValid(false);
	}
	
	public ArrayList allHeads() {
		ArrayList lstHead = new ArrayList();
		for (Iterator it = setCurves.iterator(); it.hasNext(); )
			for (ControlPoint cp = (ControlPoint) it.next(); cp != null; cp = cp.getNextCheckNextLoop())
				if (cp.isHead() && !cp.isChildHook())
					lstHead.add(cp);
		return lstHead;
	}
	
//	public int numberOfCurves() {
//		int iNum = 0;
//		Curve curve = getFirstCurve();
//		while (curve != null) {
//			if (!curve.getStart().isHook()) {
//				iNum++;
//			}
//			curve = curve.getNext();
//		}
//		return iNum;
//	}
	
//	public int numberOfPatches() {
//		int iNum = 0;
//		Patch patch = getFirstPatch();
//		while (patch != null) {
//			iNum++;
//			patch = patch.getNext();
//		}
//		return iNum;
//	}
	
	///**
	// * clone heads
	// */
	//public PointSelection clone(ControlPoint[] acp) {
	//	Set setCPs = new HashSet();
	//	for (int i = 0; i < acp.length; i++) {
	//		ControlPoint[] stack = acp[i].getStack();
	//		for (int s = 0; s < stack.length; s++) {
	//			if (stack[s].isHook()) {
	//				boolean add = true;
	//				loop1:
	//				for (ControlPoint cp = stack[s].getStart(); cp != null; cp = cp.getNext()) {
	//					boolean part = false;
	//					loop2:
	//					for (int p = 0; p < acp.length; p++) {
	//						if (acp[p] == cp) {
	//							part = true;
	//							break loop2;
	//						}
	//					}
	//					if (!part) {
	//						add = false;
	//						break loop1;
	//					}
	//				}
	//				if (add) {
	//					setCPs.add(stack[s]);
	//				}
	//			} else { 
	//				setCPs.add(stack[s]);
	//			}
	//		}
	//	}
	//	return clone(setCPs);
	//}
	//
	///**
	// * clone cps
	// */
	//public PointSelection clone(Set setCPs) {
	//	ArrayList listCPs = new ArrayList();
	//	ArrayList listNewCurves = new ArrayList();
	//	HashMap mapCPs = new HashMap();
	//	PointSelection ps = new PointSelection();
	//	
	//	/* create cloned controlPoints */
	//	for (Iterator it = setCPs.iterator(); it.hasNext(); ) {
	//		ControlPoint cpToClone = (ControlPoint) it.next();
	//		ControlPoint cpClone = new ControlPoint();
	//		mapCPs.put(cpToClone, cpClone);
	//	}
	//	
	//	/* connect cloned controlPoints */
	//	for (Iterator it = setCPs.iterator(); it.hasNext(); ) {
	//		ControlPoint cpToClone = (ControlPoint) it.next();
	//		ControlPoint cpClone = (ControlPoint) mapCPs.get(cpToClone);
	//		cpClone.setNext((ControlPoint) mapCPs.get(cpToClone.getNext()));
	//		cpClone.setPrev((ControlPoint) mapCPs.get(cpToClone.getPrev()));
	//		cpClone.setNextAttached((ControlPoint) mapCPs.get(cpToClone.getNextAttached()));
	//		cpClone.setPrevAttached((ControlPoint) mapCPs.get(cpToClone.getPrevAttached()));
	//		cpClone.setParentHook((ControlPoint) mapCPs.get(cpToClone.getParentHook()));
	//		cpClone.setChildHook((ControlPoint) mapCPs.get(cpToClone.getChildHook()));
	//		cpClone.setHookPos(cpToClone.getHookPos());
	//	}
	//	
	//	/* check for loops, add curves to model*/
	//	for (Iterator it = setCPs.iterator(); it.hasNext(); ) {
	//		ControlPoint cpToClone = (ControlPoint) it.next();
	//		ControlPoint cpClone = (ControlPoint) mapCPs.get(cpToClone);
	//		
	//		/* check if the cloned curve is closed */
	//		if (cpToClone.getLoop()) {
	//			boolean bLoop = false;
	//			loop:
	//			for (ControlPoint cp = cpClone.getNext(); cp != null; cp = cp.getNext()) {
	//				if (cp == cpClone) {
	//					bLoop = true;
	//					break loop;
	//				}
	//			}
	//			cpClone.setLoop(bLoop);
	//		}
	//		
	//		/* add a curve if we are a start point AND have a next point */
	//		if ((cpClone.getLoop() || cpClone.getPrev() == null) && cpClone.getNext() != null) {
	//			addCurve(cpClone);
	//		}
	//		
	//		/* detach empty points */
	//		if (cpClone.getNext() == null && cpClone.getPrev() == null) {
	//			if (cpClone.getNextAttached() != null) {
	//				cpClone.getNextAttached().setPrevAttached(cpClone.getPrevAttached());
	//			}
	//			if (cpClone.getPrevAttached() != null) {
	//				cpClone.getPrevAttached().setNextAttached(cpClone.getNextAttached());
	//			}
	//		}
	//	}
	//	
	//	for (Iterator it = setCPs.iterator(); it.hasNext(); ) {
	//		ControlPoint cpToClone = (ControlPoint) it.next();
	//		ControlPoint cpClone = (ControlPoint) mapCPs.get(cpToClone);
	//		if (cpClone.isHead()) {
	//			/* if we are a head, add us to the new selection */
	//			ps.addControlPoint(cpClone);
	//			cpClone.setPosition(cpToClone.getPosition());
	//		}
	//	}
	//	
	//	/* clone patches */
	//	ArrayList newPatches = new ArrayList();
	//	ArrayList newMaterials = new ArrayList();
	//	for (Patch p = getFirstPatch(); p != null; p = p.getNext()) {
	//		ControlPoint[] acp = p.getControlPoints();
	//		ControlPoint[] acpNew = new ControlPoint[acp.length];
	//		boolean addPatch = true;
	//		loop:
	//		for (int n = 0; n < acp.length; n++) {
	//			if (mapCPs.get(acp[n]) == null) {
	//				addPatch = false;
	//				break loop;
	//			} else {
	//				acpNew[n] = (ControlPoint) mapCPs.get(acp[n]);
	//			}
	//		}
	//		if (addPatch) {
	//			newPatches.add(acpNew);
	//			newMaterials.add(p.getMaterial());
	//		}
	//	}
	//	Iterator itMat = newMaterials.iterator();
	//	for (Iterator it = newPatches.iterator(); it.hasNext(); ) {
	//		addPatch((ControlPoint[]) it.next(), null);
	//		getLastPatch().setMaterial((JPatchMaterial) itMat.next());
	//	}
	//	return ps;
	//}
	
	public ArrayList getCandidateFivePointPatchList() {
		return lstCandidateFivePointPatch;
	}
	
	/**
	 *  Description of the Method
	 */
	public void computePatches() {
		computePatches(null);
	}
	
	public void computePatches(JPatchActionEdit edit) {
		System.out.println("computePatches()");
		//System.out.println("computePatches() started... " + lstCandidateFivePointPatch.size() + " candidate 5-point-patches");
		clearPatches();
		//for (Curve curve = getFirstCurve(); curve != null; curve = curve.getNext()) {
		//	for (ControlPoint cp = curve.getStart(); cp != null; cp = cp.getNextCheckNextLoop()) {
		//		cp.clearPatches();
		//	}
		//}
		ArrayList lstHead = allHeads();							// stores all heads
		ArrayList lstlstNeighbor = new ArrayList();					// a list with lists of neighbors (per head)
		HashMap mapHeadIndex = new HashMap();						// key = cp, value = index
		for (int h = 0; h < lstHead.size(); h++) {
			ControlPoint cp = (ControlPoint)lstHead.get(h);
			lstlstNeighbor.add(cp.allNeighbors());
			mapHeadIndex.put(cp,new Integer(h));
	//		System.out.println(h + "\t" + cp + "\t" + cp.getPosition());
		}
	
		// ------
		/*
		Curve curve;
		ControlPoint cp;
		for(curve = getFirstCurve(); curve != null; curve = curve.getNext()) {
			for (cp = curve.getStart(); cp != null; cp = cp.getNextCheckNextLoop()) {
				System.out.println(cp + " " + cp.getPosition() + " " + cp.getHead());
			}
		}
			for (int h = 0; h < lstHead.size(); h++) {
				ArrayList test = (ArrayList)lstlstNeighbor.get(h);
				System.out.print(h + ":");
				cp = (ControlPoint)lstHead.get(h);
				System.out.print(((Integer)mapHeadIndex.get(cp)).intValue() + " ");
				for (int t = 0; t < test.size(); t++) {
					cp = ((ControlPoint[])test.get(t))[0];
					int index = ((Integer)mapHeadIndex.get(cp)).intValue();
					System.out.print(index + " ");
				}
				System.out.println();
			}
			System.out.println();
		// ------
		*/
		int num3 = 0;
		int num4 = 0;
		int num5 = 0;
		
		/**
		* check candidate 5-point patches
		**/
		ArrayList candidateFivePointPatchesToRemove = new ArrayList();
		for (int f = 0; f < lstCandidateFivePointPatch.size(); f++) {
			ControlPoint[] fpp = new ControlPoint[10];
			ControlPoint[] acp = (ControlPoint[])lstCandidateFivePointPatch.get(f);
			boolean ok = false;
			Integer iIndex = (Integer) mapHeadIndex.get(trueHead(acp[0]));
			int index;
			ArrayList lstNeighbor;
			if (iIndex != null) {
				index = iIndex.intValue();
				lstNeighbor = (ArrayList)lstlstNeighbor.get(index);
				for (int n = 0; n <lstNeighbor.size(); n++) {
					ControlPoint[] acpNeighbor = (ControlPoint[])lstNeighbor.get(n);
					if (acpNeighbor[0] == trueHead(acp[1])) {
						ok = true;
						fpp[0] = acpNeighbor[1];
						fpp[1] = acpNeighbor[2];
					}
				}
			}
			if (ok) {
				ok = false;
				index = ((Integer)mapHeadIndex.get(trueHead(acp[1]))).intValue();
				lstNeighbor = (ArrayList)lstlstNeighbor.get(index);
				for (int n = 0; n <lstNeighbor.size(); n++) {
					ControlPoint[] acpNeighbor = (ControlPoint[])lstNeighbor.get(n);
					if (acpNeighbor[0] == trueHead(acp[2])) {
						ok = true;
						fpp[2] = acpNeighbor[1];
						fpp[3] = acpNeighbor[2];
					}
				}
			}
			if (ok) {
				ok = false;
				index = ((Integer)mapHeadIndex.get(trueHead(acp[2]))).intValue();
				lstNeighbor = (ArrayList)lstlstNeighbor.get(index);
				for (int n = 0; n <lstNeighbor.size(); n++) {
					ControlPoint[] acpNeighbor = (ControlPoint[])lstNeighbor.get(n);
					if (acpNeighbor[0] == trueHead(acp[3])) {
						ok = true;
						fpp[4] = acpNeighbor[1];
						fpp[5] = acpNeighbor[2];
					}
				}
			}
			if (ok) {
				ok = false;
				index = ((Integer)mapHeadIndex.get(trueHead(acp[3]))).intValue();
				lstNeighbor = (ArrayList)lstlstNeighbor.get(index);
				for (int n = 0; n <lstNeighbor.size(); n++) {
					ControlPoint[] acpNeighbor = (ControlPoint[])lstNeighbor.get(n);
					if (acpNeighbor[0] == trueHead(acp[4])) {
						ok = true;
						fpp[6] = acpNeighbor[1];
						fpp[7] = acpNeighbor[2];
					}
				}
			}
			if (ok) {
				ok = false;
				index = ((Integer)mapHeadIndex.get(trueHead(acp[4]))).intValue();
				lstNeighbor = (ArrayList)lstlstNeighbor.get(index);
				for (int n = 0; n <lstNeighbor.size(); n++) {
					ControlPoint[] acpNeighbor = (ControlPoint[])lstNeighbor.get(n);
					if (acpNeighbor[0] == trueHead(acp[0])) {
						ok = true;
						fpp[8] = acpNeighbor[1];
						fpp[9] = acpNeighbor[2];
					}
				}
			}
			if (
				ok &&
				fpp[0].trueCp() != fpp[9].trueCp() &&
				fpp[1].trueCp() != fpp[2].trueCp() &&
				fpp[3].trueCp() != fpp[4].trueCp() &&
				fpp[5].trueCp() != fpp[6].trueCp() &&
				fpp[7].trueCp() != fpp[8].trueCp()
			) {
				addPatch(fpp,edit);
				num5++;
			} else {
				//lstCandidateFivePointPatch.remove(acp);
				candidateFivePointPatchesToRemove.add(acp);
			}
		}					
		lstCandidateFivePointPatch.removeAll(candidateFivePointPatchesToRemove);
		
		/**
		* search for 3- and 4-point patches
		**/
		for (int y = 0; y < lstHead.size(); y++) {
			ArrayList lstNeighbor = (ArrayList)lstlstNeighbor.get(y);
			for (int x = 0; x < lstNeighbor.size() - 1; x++) {
				ControlPoint[] acpNeighborX = (ControlPoint[])lstNeighbor.get(x);
				ControlPoint headX = acpNeighborX[0];
				int indexX = ((Integer)mapHeadIndex.get(headX)).intValue();
				if (indexX > y) {
//					System.out.println("\tX=" + indexX);
					for (int z = x + 1; z < lstNeighbor.size(); z++) {
						ControlPoint[] acpNeighborZ = (ControlPoint[])lstNeighbor.get(z);
						ControlPoint headZ = acpNeighborZ[0];
						int indexZ = ((Integer)mapHeadIndex.get(headZ)).intValue();
						if (indexZ > y) {
//							System.out.println("\t\tZ=" + indexZ);
							ArrayList lstX = (ArrayList)lstlstNeighbor.get(indexX);
							ArrayList lstZ = (ArrayList)lstlstNeighbor.get(indexZ);
							//
							// search for 3-point patch...
							//
							for (int xx = 0; xx < lstX.size(); xx++) {
								ControlPoint[] acpNeighborXX = (ControlPoint[])lstX.get(xx);
								ControlPoint headXX = acpNeighborXX[0];
								if (headXX == headZ) {
									//Curve c = acpNeighborXX[1].getHookCurve();
									//if (c != acpNeighborXX[2].getHookCurve() || c != acpNeighborZ[1].getHookCurve() || c != acpNeighborZ[2].getHookCurve() || c != acpNeighborX[1].getHookCurve() || c != acpNeighborX[2].getHookCurve()) {
									//System.out.println("<<<");
									//System.out.println(acpNeighborX[1]);
									//System.out.println(acpNeighborX[2]);
									//System.out.println(acpNeighborXX[1]);
									//System.out.println(acpNeighborXX[2]);
									//System.out.println(acpNeighborZ[1]);
									//System.out.println(acpNeighborZ[2]);
									//System.out.println(">>>");
									if (
										acpNeighborX[1].trueCp() != acpNeighborZ[1].trueCp() &&
										acpNeighborXX[1].trueCp() != acpNeighborX[2].trueCp() &&
										acpNeighborZ[2].trueCp() != acpNeighborXX[2].trueCp()
									) {
									//	(acpNeighborX[1].getHookCurve() != acpNeighborZ[2].getHookCurve()) &&
									//	(acpNeighborZ[1].getHookCurve() != acpNeighborXX[2].getHookCurve()) &&
									//	(acpNeighborXX[1].getHookCurve() != acpNeighborX[2].getHookCurve())
									//) {
										ControlPoint[] acpPatch = new ControlPoint[] {
											acpNeighborX[1],
											acpNeighborX[2],
											acpNeighborXX[1],
											acpNeighborXX[2],
											acpNeighborZ[2],
											acpNeighborZ[1],
										};
										//System.out.println(acpPatch[0] + " " + acpPatch[1] + " " + acpPatch[2] + " " + acpPatch[3] + " " + acpPatch[4] + " " + acpPatch[5]);
										addPatch(acpPatch,edit);
										num3++;
										//System.out.println("+");
									}
								}
								/*		
								int indexXX = ((Integer)mapHeadIndex.get(headXX)).intValue();
								if (indexXX == indexZ) {
									System.out.print("3-point-patch " + num3++ + "found:");
									System.out.println(y + " " + indexX + " " + indexZ);
								}
								*/
							}
							//
							// search for 4-point patch...
							//
							boolean ok = true;
							for (int xx = 0; xx < lstX.size(); xx++) {
								ControlPoint[] acpNeighborXX = (ControlPoint[])lstX.get(xx);
								ControlPoint headXX = acpNeighborXX[0];
								if (headXX == headZ) {
									ok = false;
								}
							}
							for (int zz = 0; zz < lstZ.size(); zz++) {
								ControlPoint[] acpNeighborZZ = (ControlPoint[])lstZ.get(zz);
								ControlPoint headZZ = acpNeighborZZ[0];
								if (headZZ == headX) {
									ok = false;
								}
							}
							for (int xx = 0; xx < lstX.size(); xx++) {
								ControlPoint[] acpNeighborXX = (ControlPoint[])lstX.get(xx);
								ControlPoint headXX = acpNeighborXX[0];
								int indexXX = ((Integer)mapHeadIndex.get(headXX)).intValue();
								if (indexXX != y) {
									for (int zz = 0; zz < lstZ.size(); zz++) {
										ControlPoint[] acpNeighborZZ = (ControlPoint[])lstZ.get(zz);
										ControlPoint headZZ = acpNeighborZZ[0];
										if (headZZ == headXX) {
											
											// eliminate if 3pp
											
											for (int w = 0; w < lstNeighbor.size(); w++) {
												ControlPoint[] acpNeighborW = (ControlPoint[])lstNeighbor.get(w);
												ControlPoint headW = acpNeighborW[0];
												if (headXX == headW) {
													ok = false;
												}
											}
											
											
											//
											if (ok) {
											//Curve c = acpNeighborXX[1].getHookCurve();
											//if (c != acpNeighborXX[2].getHookCurve() || c != acpNeighborZ[1].getHookCurve() || c != acpNeighborZ[2].getHookCurve() || c != acpNeighborX[1].getHookCurve() || c != acpNeighborX[2].getHookCurve() || c != acpNeighborZZ[1].getHookCurve() || c != acpNeighborZZ[2].getHookCurve()) {
											if (
												acpNeighborX[1].trueCp() != acpNeighborZ[1].trueCp() &&
												acpNeighborXX[1].trueCp() != acpNeighborX[2].trueCp() &&
												acpNeighborZZ[2].trueCp() != acpNeighborXX[2].trueCp() &&
												acpNeighborZZ[1].trueCp() != acpNeighborZ[2].trueCp()
											) {
												ControlPoint[] acpPatch = new ControlPoint[] {
													acpNeighborX[1],
													acpNeighborX[2],
													acpNeighborXX[1],
													acpNeighborXX[2],
													acpNeighborZZ[2],
													acpNeighborZZ[1],
													acpNeighborZ[2],
													acpNeighborZ[1],
												};
												addPatch(acpPatch,edit);
												num4++;
											}
											}
										}		
										/*
										int indexZZ = ((Integer)mapHeadIndex.get(headZZ)).intValue();
										if (indexXX == indexZZ) {
											System.out.print("4-point-patch " + num++ + " found:");
											System.out.println(y + " " + indexX + " " + indexZ + " " + indexXX);
										}
										*/
									}
								}
							}
						}
					}
				}
			}
		}
		/*
		 * remove invalid patches
		 */
		ArrayList list = new ArrayList();
		for (Iterator it = mapPatches.keySet().iterator(); it.hasNext(); ) {
			Patch p = (Patch) it.next();
			if (!p.isValid())
				list.add(p);
		}
		for (Iterator it = list.iterator(); it.hasNext(); )
			removePatch((Patch) it.next(),edit);
		//System.out.print(num3 + " 3-point-, " + num4 + " 4-point- and " + num5 + " 5-point-patches found ");
		//System.out.println("(" + lstCandidateFivePointPatch.size() + " candidate 5-point-patches)");
	//	System.out.println("...stop");
	}

	public Set<ControlPoint> getCurveSet() {
		return setCurves;
	}
	
	public Set<Patch> getPatchSet() {
		return mapPatches.keySet();
	}
	
	public Set<Bone> getBoneSet() {
		return setBones;
	}
	
	
	public void dump() {
		System.out.println("------------- curves -------------");
		System.out.println("\tcp\tnext\tprev\tloop\tna\tpa\tphook\tchook\thpos\tposition\n");
		for (Iterator it = setCurves.iterator(); it.hasNext(); ) {
			for (ControlPoint cp = (ControlPoint) it.next(); cp != null; cp = cp.getNextCheckNextLoop()) {
				System.out.println("\t" + cp + "\t" + cp.getNext() + "\t" + cp.getPrev() + "\t" + cp.getLoop() + "\t" + cp.getNextAttached() + "\t" + cp.getPrevAttached() + "\t" + cp.getParentHook() + "\t" + cp.getChildHook() + "\t" + cp.getHookPos() + "\t" + cp.getPosition() + "\t" + cp.isDeleted() + "\t" + cp.getBone());
				
				//System.out.println("\t" + cp + "\t" + cp.getPosition() + "\t" + cp.getNext() + "\t" + cp.getPrev() + "\t" + cp.getNextAttached() + "\t" + cp.getPrevAttached() + "\t" + cp.getLoop());
				//System.out.println("\t" + cp);
				//System.out.println("\t" + cp.getHead());
				//System.out.println("\t" + cp.getInTangent() + "\t" + cp.getPosition() + "\t" + cp.getOutTangent());
				//System.out.println(cp + "\t" + cp.isHook() + "\t" + cp.isTargetHook() + "\t" + cp.getHead());
			}
			System.out.println();
		}
		System.out.println("\n\n------------- patches -------------");
		for (Iterator it = mapPatches.keySet().iterator(); it.hasNext(); )
			System.out.println(it.next());
		
		System.out.println("\n\n--------active selection -------");
		Selection selection = MainFrame.getInstance().getSelection();
		if (selection != null) {
			System.out.println(selection);
			System.out.println(selection.getMap());
			System.out.println();
		}
		
		System.out.println("\n\n----------- selections -------------");
		for (Iterator it = listSelections.iterator(); it.hasNext(); ) {
			selection = (Selection) it.next();
			System.out.println(selection);
			System.out.println(selection.getMap());
			System.out.println();
		}
		
		System.out.println("\n\n----------- morphs -------------");
		for (Iterator it = listMorphs.iterator(); it.hasNext(); ) {
			((Morph) it.next()).dump();
		}
		
		System.out.println("\n\n----------- bones -------------");
		for (Iterator itBones = setBones.iterator(); itBones.hasNext(); ) {
			Bone bone = (Bone) itBones.next();
			System.out.println(bone + " \t" + bone.getParentBone());
			for (Iterator itDofs = bone.getDofs().iterator(); itDofs.hasNext(); )
				((RotationDof) itDofs.next()).dump();
		}
		
		System.out.println("\n\n----------- end -------------");
	}
	
//	public void setCpMap(List list) {
//		int i = 0;
//		for (Iterator it = list.iterator(); it.hasNext(); )
//			for (ControlPoint cp = (ControlPoint) it.next(); cp != null; cp = cp.getNextCheckNextLoop())
//				objectIdMap.put(cp, i++);
//	}
//	
//	public void setBoneMap(List list) {
//		int i = 0;
//		for (Iterator it = list.iterator(); it.hasNext(); )
//			objectIdMap.put(it.next(), i++);
//			
////		System.out.println(map);
//	}
	
	private ControlPoint trueHead(ControlPoint cp) {
		return (cp.getParentHook() == null) ? cp.getHead() : cp.getParentHook().getHead();
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
			return treenodeSelections;
		case 1:
			return treenodeMaterials;
		case 2:
			return treenodeMorphs;
		case 3:
			return treenodeBones;
		}
		throw new ArrayIndexOutOfBoundsException();
	}

	public int getChildCount() {
		return 4;
	}

	public TreeNode getParent() {
		return bInserted ? MainFrame.getInstance().getRootTreenode() : null;
	}

	public int getIndex(TreeNode node) {
		return -1;
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
	
	private class ModelTreeNode implements MutableTreeNode {
		private List list;
		private String strName;
		
		public ModelTreeNode(String name, List list) {
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
			if (animModel != null)
				return animModel;
			return Model.this;
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

