/*
 * $Id: CompoundRemove.java,v 1.1 2005/09/19 12:40:15 sascha_l Exp $
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

import java.util.*;

import jpatch.entity.*;

/**
 * @author sascha
 *
 */
public class CompoundRemove extends JPatchCompoundEdit {
	
	public CompoundRemove(Collection objects) {
		if (DEBUG)
			System.out.println(getClass().getName() + "(" + objects + ")");
		HashSet controlPointSet = new HashSet();
		for (Iterator it = objects.iterator(); it.hasNext(); ) {
			ControlPoint head = (ControlPoint) it.next();
			for (ControlPoint cp = head; cp != null; cp = cp.getPrevAttached()) {
//				System.out.println(cp.getHookPos() + " " + cp.getNextAttached() + " " + ((cp.getNextAttached() != null) ? "" + cp.getNextAttached().getHookPos() : ""));
//				if (cp.getHookPos() == -1 && (cp.getNextAttached() == null || cp.getNextAttached().getHookPos() == -1))
					controlPointSet.add(cp);
			}
		}
		if (DEBUG)
			System.out.println("\t" + controlPointSet);
//		for (Iterator it = (new HashSet(MainFrame.getInstance().getModel().getCurveSet())).iterator(); it.hasNext(); ) {
//			ControlPoint start = (ControlPoint) it.next();
//			if (dropCurve(start, objects)) {
//				for (ControlPoint cp = start; cp != null; cp = cp.getNextCheckNextLoop())
//					controlPointSet.remove(cp);
//				addEdit(new CompoundDropCurve(start, start.getHookPos() == 0));
//			}
//		}
		for (Iterator it = controlPointSet.iterator(); it.hasNext(); ) {
			ControlPoint cp = (ControlPoint) it.next();
			if (!cp.isDeleted())
				addEdit(new CompoundRemoveControlPoint(cp));
		}
	}
	
//	public CompoundRemove(Model model, Collection objects) {
////		for (Curve curve = model.getFirstCurve(); curve != null; curve = curve.getNext()) {
////			if (dropCurve(curve, objects)) {
////				for (ControlPoint cp = curve.getStart(); cp != null; cp = cp.getNextCheckNextLoop())
////					objects.remove(cp.getHead());
////				addEdit(new CompoundDropCurve(curve));
////			}
////		}
//		for (Iterator it = objects.iterator(); it.hasNext(); ) {
//			Object object = it.next();
//			if (object instanceof ControlPoint) {
//				ControlPoint[] acp = ((ControlPoint) object).getHead().getStack();
//				for (int i = 0; i < acp.length; i++) {
//					addEdit(new CompoundRemoveControlPoint(acp[i]));
//				}
//			}
//		}
//	}
	
//	private boolean dropCurve(Curve curve, Collection objects) {
//		boolean consecutive = false;
//		for (ControlPoint cp = curve.getStart(); cp != null; cp = cp.getNextCheckNextLoop()) {
//			if (!objects.contains(cp.getHead())) {
//				if (consecutive)
//					return false;
//				consecutive = true;
//			} else {
//				consecutive = false;
//			}
//		}
//		return true;
//	}
}