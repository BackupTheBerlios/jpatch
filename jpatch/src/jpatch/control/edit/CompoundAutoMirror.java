package jpatch.control.edit;

import java.awt.geom.Arc2D.Float;
import java.util.*;
import javax.swing.*;
import javax.vecmath.*;

import jpatch.entity.*;
import jpatch.boundary.*;


public class CompoundAutoMirror extends AbstractClone {
	
	protected List mirrorList = new ArrayList();
	
	public CompoundAutoMirror(Selection selection) {
		super(selection.getControlPointArray());
		//System.out.println("automirror");
		int side = 0;
		boolean valid = true;
		loop:
		for (int i = 0; i < acp.length; i++) {
			if (!acp[i].isHook()) {
				Point3f pos = acp[i].getPosition();
				if (side == 0) {
					if (pos.x < 0) side = -1;
					if (pos.x > 0) side = 1;
				} else if (side == -1 && pos.x > 0) {
					valid = false;
					break loop;
				} else if (side == 1 && pos.x < 0) {
					valid = false;
					break loop;
				}
			}
		}
		if (!valid) {
			JOptionPane.showMessageDialog(MainFrame.getInstance(), "Found points on both sides, automirror operation canceled!", "Can't automirror", JOptionPane.ERROR_MESSAGE);
		} else {
			
			/* convert all hooks on boundary curves to regular controlpoints... */
			for (Iterator it = MainFrame.getInstance().getModel().getCurveSet().iterator(); it.hasNext(); ) {
//			for (Curve curve = MainFrame.getInstance().getModel().getFirstCurve(); curve != null; curve = curve.getNext()) {
				ControlPoint start = (ControlPoint) it.next();
				/* check if this is a boundary curve */
				boolean boundary = true;
				loop:
				for (ControlPoint cp = start; cp != null; cp = cp.getNextCheckNextLoop()) {
					if (cp.getPosition().x != 0) {
						boundary = false;
						break loop;
					}
				}
				
				if (boundary) {
					for (ControlPoint cp = start; cp != null; cp = cp.getNextCheckNextLoop()) {
					
						/* check if we are start of a hook curve, and if both, start and end are on the boundary */
						if (selection.contains(cp.getHead()) && cp.getChildHook() != null && cp.getNext() != null && selection.contains(cp.getNext().getHead())) { //&& cp.getPosition().x == 0 && cp.getNext().getPosition().x == 0) {
							
							/* convert hooks... */
							ControlPoint startHook = cp.getChildHook();
							ControlPoint endHook = startHook.getEnd();
							ControlPoint cpToAppend = cp;
							ControlPoint cpEnd = cp.getNext();
//							Curve hookCurve = cp.getChildHook().getCurve();
							for (ControlPoint hook = cp.getChildHook().getNext(); hook.getHookPos() < 1;) {
								Point3f position = hook.getPosition();
								ControlPoint next = hook.getNext();
								//System.out.println(hook);
								/* remove hook from hook curve */
								addEdit(new AtomicRemoveControlPointFromCurve(hook));
								
								/* and insert it on the parent curve */
								addEdit(new AtomicInsertControlPoint(hook, cpToAppend));
								
								/* and convert it to a regular controlpoint by setting hookpos to -1 */
								addEdit(new AtomicChangeControlPoint.HookPos(hook,-1));
								
								hook.setPosition(position);
								//hook.fixPosition();
								
								/* add "new" cp to all relevant selections */
//								ArrayList listAdd = new ArrayList();
//								ArrayList listRemove = new ArrayList();
//								listAdd.add(hook);
//								listRemove.add(startHook);
//								listRemove.add(endHook);
//								for (Iterator it = MainFrame.getInstance().getModel().getSelections().iterator(); it.hasNext(); ) {
//									PointSelection psLoop = (PointSelection) it.next();
//									if (psLoop.contains(cpEnd) && ps.contains(cpEnd.getPrev())) {
//										addEdit(new AddControlPointsToSelectionEdit(psLoop, listAdd));
//									}
//									if (psLoop.contains(startHook) && psLoop.contains(endHook)) {
//										addEdit(new RemoveControlPointsFromSelectionEdit(psLoop, listRemove));
//									}
//								}
//								addEdit(new AddControlPointsToSelectionEdit(ps, listAdd));
//								addEdit(new RemoveControlPointsFromSelectionEdit(ps, listRemove));
								HashMap mapAdd = new HashMap();
								mapAdd.put(hook, new Float(1));
								HashMap mapRemove = new HashMap();
								mapRemove.put(startHook, new Float(1));
								mapRemove.put(endHook, new Float(1));
								for (Iterator it2 = MainFrame.getInstance().getModel().getSelections().iterator(); it.hasNext(); ) {
									Selection loopSelection = (Selection) it.next();
									if (loopSelection.contains(cpEnd) && loopSelection.contains(cpEnd.getPrev())) {
										addEdit(new AtomicModifySelection.AddObjects(loopSelection, mapAdd));
									}
									if (loopSelection.contains(startHook) && loopSelection.contains(endHook)) {
										addEdit(new AtomicModifySelection.RemoveObjects(loopSelection, mapRemove));
									}
								}
								addEdit(new AtomicModifySelection.AddObjects(selection, mapAdd));
								addEdit(new AtomicModifySelection.RemoveObjects(selection, mapRemove));
								cpToAppend = hook;
								hook = next;
							}
							
							/* remove the hook-curve */
							addEdit(new AtomicRemoveCurve(cp.getChildHook()));
							addEdit(new AtomicChangeControlPoint.ChildHook(cp, null));
							
							/* correct patches */
							ArrayList patches;
//							patches = MainFrame.getInstance().getModel().getPatchesContaining(startHook);
							for (Iterator it2 = MainFrame.getInstance().getModel().getPatchSet().iterator(); it.hasNext(); ) {
								Patch patch = (Patch) it.next();
								if (patch.contains(startHook)) {
									ControlPoint[] patchCPs = patch.getControlPoints();
									for (int i = 0; i < patchCPs.length; i++) {
										if (patchCPs[i] == startHook) {
											patchCPs[i] = cp;
										}
									}
								}
							}
//							patches = MainFrame.getInstance().getModel().getPatchesContaining(endHook);
							for (Iterator it2 = MainFrame.getInstance().getModel().getPatchSet().iterator(); it.hasNext(); ) {
								Patch patch = (Patch) it.next();
								if (patch.contains(endHook)) {
									ControlPoint[] patchCPs = patch.getControlPoints();
									for (int i = 0; i < patchCPs.length; i++) {
										if (patchCPs[i] == endHook) {
											patchCPs[i] = cpEnd;
										}
									}
								}
							}
						}
					}
				}
			}
			
			
			
			/* clone points */
			buildCloneMap(true);
			cloneControlPoints();
			cloneCurves();
			
			for (Iterator it = mapOriginals.keySet().iterator(); it.hasNext(); ) {
				ControlPoint cpClone = (ControlPoint) it.next();
				ControlPoint cpOriginal = getOriginal(cpClone);
				if (cpClone == cpOriginal) {
					ControlPoint cp = cpClone;
					if (cp.getNext() == null) {
						addEdit(new AtomicChangeControlPoint.Next(cp,getClone(cp.getPrev())));
					} else {
						addEdit(new AtomicChangeControlPoint.Prev(cp,getClone(cp.getNext())));
						//addEdit(new ChangeControlPointLoopEdit(cp.getCurve().getStart(),true));
					}
					
					
				}
			}
			
			
			
			for (Iterator it = mapOriginals.keySet().iterator(); it.hasNext(); ) {
				ControlPoint cpClone = (ControlPoint) it.next();
				if (cpClone.getPosition().x == 0 && cpClone.getNext() != null && cpClone.getNext().getPosition().x == 0) {
					//System.out.println("something went wrong in automirror!");
				} else if (cpClone.isHead()) {
					Point3f p3 = cpClone.getPosition();
					p3.x = -p3.x;
					cpClone.setPosition(p3);
					//cpClone.fixPosition();
				}
			}
			
			
			// FIXME huh???
			/* remove dead curves */
//			for (Curve curve = MainFrame.getInstance().getModel().getFirstCurve(); curve != null; curve = curve.getNext()) {
//				if (curve.getStart().getCurve() != curve) {
//					addEdit(new AtomicRemoveCurve(curve));
//				}
//			}
			
			/* add loop flags */
//			for (Curve curve = MainFrame.getInstance().getModel().getFirstCurve(); curve != null; curve = curve.getNext()) {
			for (Iterator it = MainFrame.getInstance().getModel().getCurveSet().iterator(); it.hasNext(); ) {
				ControlPoint cp = (ControlPoint) it.next();
				if (!cp.getLoop()) {
					ControlPoint cpStart = cp;
					cp = cp.getNext();
					while (cp != null && cp != cpStart) cp = cp.getNext();
					if (cp == cpStart && !cp.getLoop()) {
						addEdit(new AtomicChangeControlPoint.Loop(cp));
					}
				}
			}
			
//			/* validate all curves */
//			for (Curve curve = MainFrame.getInstance().getModel().getFirstCurve(); curve != null; curve = curve.getNext()) {
//				curve.validate();
//			}
			
			cloneHooks();
			
			
			
			clonePatches(mirrorList);
			
			/*
			 * mirror morphs
			 */
			ArrayList newMorphList = new ArrayList();
			for (Iterator it = MainFrame.getInstance().getModel().getMorphList().iterator(); it.hasNext(); ) {
				Morph morph = (Morph) it.next();
				
				boolean expand = false;
				boolean mirror = true;
				for (Iterator jt = morph.getMorphMap().keySet().iterator(); jt.hasNext(); ) {
					ControlPoint cp = (ControlPoint) jt.next();
					if (!mapClones.containsKey(cp) && !mirrorList.contains(cp))
						mirror = false;
					else if (mapClones.get(cp) == cp || mirrorList.contains(cp))
						expand = true;
				}
				if (mirror) {
//					List pointList = morph.getPointList();
//					List vectorList = morph.getVectorList();
					Map morphMap = morph.getMorphMap();
					if (expand) {
//						ArrayList newPointList = new ArrayList();
//						ArrayList newVectorList = new ArrayList();
						Map newMap = new HashMap();
						for (Iterator jt = morphMap.keySet().iterator(); jt.hasNext(); ) {
							ControlPoint cp = (ControlPoint) jt.next();
							ControlPoint clone = (ControlPoint) mapClones.get(cp);
							if (cp != clone && !mirrorList.contains(cp)) {
								Vector3f vector = new Vector3f((Vector3f) morphMap.get(cp));
								vector.x = -vector.x;
//								newPointList.add(clone);
//								newVectorList.add(vector);
								newMap.put(clone, vector);
							}
						}
						if (newMap.size() > 0)
							addEdit(new AddPointsToMorphEdit(morph, newMap));
					} else {
						Morph newMorph = new Morph(morph.getNodeType(), "mirrored " + morph.getName());
						for (Iterator jt = morphMap.keySet().iterator(); jt.hasNext(); ) {
							ControlPoint cp = (ControlPoint) jt.next();
							ControlPoint clone = (ControlPoint) mapClones.get(cp);
							Vector3f vector = new Vector3f((Vector3f) morphMap.get(cp));
							vector.x = -vector.x;
							newMorph.addPoint(clone, vector);
							newMorph.setMax(morph.getMax());
							newMorph.setMin(morph.getMin());
							newMorph.setSliderValue(morph.getSliderValue());
						}
						newMorphList.add(newMorph);
					}
				}
			}
			for (Iterator it = newMorphList.iterator(); it.hasNext(); addEdit(new AtomicAddMorph((Morph) it.next())));
			
			/*
			* mirror selections
			*/
			//System.out.println(mirrorList);
			ArrayList newSelections = new ArrayList();
			for (Iterator it = MainFrame.getInstance().getModel().getSelections().iterator(); it.hasNext(); ) {
				Selection sel = (Selection) it.next();
				ControlPoint[] acp = selection.getControlPointArray();
				boolean mirror = true;
				boolean expand = false;
				for (int c = 0; c < acp.length; c++) {
					if (!mapClones.containsKey(acp[c]) && !mirrorList.contains(acp[c])) {
						mirror = false;
					} //else if (mirrorList.contains(acp[c])) {
					//else if (acp[c].getPosition().x == 0) {
					else if (mapClones.get(acp[c]) == acp[c] || mirrorList.contains(acp[c])) {
						expand = true;
					}
				}
				//System.out.println(ps.getName() + "\t" + mirror + "\t" + expand);
				if (mirror) {
					if (expand) {
						//System.out.println("expand " + ps.getName());
						Map points = new HashMap();
						for (int c = 0; c < acp.length; c++) {
							//if (!mirrorList.contains(acp[c])) {
							//if (acp[c].getPosition().x != 0) {
							if (mapClones.get(acp[c]) != acp[c] && !mirrorList.contains(acp[c])) {
								//System.out.println(acp[c] + " " + mapClones.get(acp[c]));
								points.put(mapClones.get(acp[c]), sel.getMap().get(acp[c]));
							}
						}
						if (points.size() > 0) {
							addEdit(new AtomicModifySelection.AddObjects(sel, points));
						}
					} else {
						Map points = new HashMap();
						for (int c = 0; c < acp.length; c++) {
							points.put(mapClones.get(acp[c]), sel.getMap().get(acp[c]));
						}
						Selection newSelection = new Selection(points);
						if (sel.getName().startsWith("*"))
							newSelection.setName("*mirrored " + sel.getName().substring(1));
						else
							newSelection.setName("mirrored " + sel.getName());
						newSelections.add(newSelection);
					}
				}
			}
			for (Iterator it = newSelections.iterator(); it.hasNext(); ) {
				addEdit(new AtomicAddSelection((Selection) it.next()));
			}
			
			//for (Iterator it = mapOriginals.keySet().iterator(); it.hasNext(); ) {
			//	ControlPoint cpClone = (ControlPoint) it.next();
			//	ControlPoint cpOriginal = getOriginal(cpClone);
			//	if (cpClone != cpOriginal) {
			//		//if (!cpClone.getCurve().getStart().getLoop()) {
			//			ControlPoint cp = cpClone.getNext();
			//			cpClone.setNext(cpClone.getPrev());
			//			cpClone.setPrev(cp);
			//		//}
			//	}
			//}
			
			
			//MainFrame.getInstance().getModel().dump();
			
			////clonePatches();
			//for (Iterator it = mapOriginals.keySet().iterator(); it.hasNext(); ) {
			//	ControlPoint cpClone = (ControlPoint) it.next();
			//	if (cpClone.getPosition().x == 0 && cpClone.getNext() != null && cpClone.getNext().getPosition().x == 0) {
			//		addEdit(new RemoveCurveSegmentEdit(cpClone));
			//	} else if (cpClone.isHead()) {
			//		Point3f p3 = cpClone.getPosition();
			//		p3.x = -p3.x;
			//		cpClone.setPosition(p3);
			//		cpClone.fixPosition();
			//	}
			//}
			//for (Iterator it = mapOriginals.keySet().iterator(); it.hasNext(); ) {
			//	ControlPoint cpClone = (ControlPoint) it.next();
			//	if (cpClone.getPosition().x == 0 && !cpClone.isHook() && cpClone.isSingle()) {
			//		System.out.println(cpClone.getNext() + "," + cpClone.getPrev());
			//		ControlPoint cpOriginal = getOriginal(cpClone);
			//		if ((cpOriginal.getNext() == null || cpOriginal.getNext().getPosition().x != 0) && (cpOriginal.getPrev() == null || cpOriginal.getPrev().getPosition().x != 0)) {
			//			
			//			//System.out.println(cpClone + " " + cpOriginal);
			//			addEdit(new ComplexAppendControlPointsEdit(cpClone,cpOriginal));
			//		} //else System.out.println("*");
			//	}
			//}
		}
	}
	/**
	* checks all selected ControlPoints (those in acp), and if they are really to be
	* cloned, adds them to mapCPs
	**/
	protected void buildCloneMap(boolean hooks) {
		for (int i = 0; i < acp.length; i++) {
			ControlPoint[] acpStack = acp[i].getStack();
			for (int s = 0; s < acpStack.length; s++) {
				ControlPoint cp = acpStack[s];
        
				/*
				* check if a hook-curve should be added
				*/
				if (cp.isStartHook()) {
					if (hooks && checkStartHook(cp)) {
						addToCloneMap(cp);
						addToCloneMap(cp.getEnd());
					}
				} else if (cp.isHook()) {
					if (hooks && cp.getPrevAttached() != null && checkForNeighbor(cp.getPrevAttached())) {
						if (checkStartHook(cp.getStart())) {
							addToCloneMap(cp);
						}
					}
				} else if (checkForNeighbor(cp)) {
					if (cp.getPosition().x != 0) {
						addToCloneMap(cp);
					} else {
						//System.out.print(cp + " ");
						if ((cp.getNext() != null && cp.getPrev() == null && cp.getNext().getPosition().x != 0) || (cp.getPrev() != null && cp.getNext() == null && cp.getPrev().getPosition().x != 0)) {
							/* if the cp is not single, check if the attached curve is entrirely on the mirror plane */
							if (cp.isSingle()) {
								//System.put.println(cp + " single");
								mapClones.put(cp,cp);
								mapOriginals.put(cp,cp);
							} else if (cp.getNextAttached() == null && cp.getPrevAttached() != null && cp.getPrevAttached().getPrevAttached() == null) {
								//System.put.println(cp + " + prev attched curve");
								boolean onMirror = true;
								loop:
								for (ControlPoint c = cp.getPrevAttached().getStart(); c != null; c = c.getNextCheckNextLoop()) {
									if (c.getPosition().x != 0) {
										onMirror = false;
										break loop;
									}
								}
								if (onMirror) {
									mapClones.put(cp,cp);
									mapOriginals.put(cp,cp);
								} else {
									addToCloneMap(cp);
								}
							} else if (cp.getPrevAttached() == null && cp.getNextAttached() != null && cp.getNextAttached().getNextAttached() == null) {
								//System.put.println(cp + " next attached curve");
								boolean onMirror = true;
								loop:
								for (ControlPoint c = cp.getNextAttached().getStart(); c != null; c = c.getNextCheckNextLoop()) {
									if (c.getPosition().x != 0) {
										onMirror = false;
										break loop;
									}
								}
								if (onMirror) {
									mapClones.put(cp,cp);
									mapOriginals.put(cp,cp);
								} else {
									addToCloneMap(cp);
								}
							} else {
								addToCloneMap(cp);
							}
							
							//System.out.println("self");
						} else {
							boolean onMirror = true;
							loop:
							for (ControlPoint c = cp.getStart(); c != null; c = c.getNextCheckNextLoop()) {
								if (c.getPosition().x != 0) {
									onMirror = false;
									break loop;
								}
							}
							if (onMirror) {
								mirrorList.add(cp);
								//System.out.println("mirror");
							} else {
								addToCloneMap(cp);
								//System.out.println("clone");
							}
						}
					}
				}
			}
		}
	}
}
