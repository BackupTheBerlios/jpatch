//package jpatch.control.edit;
//
//import jpatch.entity.*;
//
///**
// *  This edit detaches a control point.
// *
// * @author     Sascha Ledinsky
// * @created    26. Dezember 2003
// */
//public class FullyDetachControlPointEdit extends JPatchAbstractUndoableEdit {
//	private ControlPoint cp;
//	private ControlPoint cpNextAttached;
//	private ControlPoint cpPrevAttached;
//
//	/**
//	 * Constructor
//	 * The controlPoint will be detached immediately
//	 *
//	 * @param  cp  ControlPoint to remove
//	 */
//	public FullyDetachControlPointEdit(ControlPoint cp) {
//		/*
//		 *  store ControlPoint, nextAttached and previousAttached ControlPoints for undo
//		 */
//		this.cp = cp;
//		cpNextAttached = cp.getNextAttached();
//		cpPrevAttached = cp.getPrevAttached();
//		detach();
//	}
//
//
//	/**
//	 *  redoes the edit
//	 */
//	public void redo() {
//		detach();
//	}
//
//
//	/**
//	 *  undoes the edit
//	 */
//	public void undo() {
//		attach();
//	}
//
//
//	/**
//	 *  This method detaches the ControlPoint
//	 */
//	private void detach() {
//		if (cpNextAttached != null) {
//			cpNextAttached.setPrevAttached(cpPrevAttached);
//		}
//		if (cpPrevAttached != null) {
//			cpPrevAttached.setNextAttached(cpNextAttached);
//			if (cpNextAttached == null) {
//				cpPrevAttached.setPosition(cp.getPosition());
//			}
//		}
//		cp.setNextAttached(null);
//		cp.setPrevAttached(null);
//	}
//
//
//	/**
//	 *  This Method re-attaches the ControlPoint
//	 */
//	private void attach() {
//		if (cpNextAttached != null) {
//			cpNextAttached.setPrevAttached(cp);
//		}
//		if (cpPrevAttached != null) {
//			cpPrevAttached.setNextAttached(cp);
//		}
//		cp.setNextAttached(cpNextAttached);
//		cp.setPrevAttached(cpPrevAttached);
//	}
//}
