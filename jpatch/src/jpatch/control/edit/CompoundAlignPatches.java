package jpatch.control.edit;

import jpatch.boundary.*;
import jpatch.entity.*;
import java.util.*;

public class CompoundAlignPatches extends JPatchCompoundEdit implements JPatchRootEdit {
	private HashSet patches;
	
	public CompoundAlignPatches() {
		this(MainFrame.getInstance().getModel().getPatchSet());
	}
	
	public CompoundAlignPatches(Collection patches) {
		this.patches = new HashSet(patches);
		for (Iterator it = (new HashSet(patches)).iterator(); it.hasNext(); ) {
			Patch patch = (Patch) it.next();
			if (patches.contains(patch)) processPatch(patch);
		}
	}
	
	public String getName() {
		return "align patches";
	}
	
	private void processPatch(Patch patch) {
		patches.remove(patch);
		for (Iterator it = (new HashSet(patches)).iterator(); it.hasNext(); ) {
			Patch p = (Patch) it.next();
			if (patches.contains(p)) {
				int r = comparePatches(patch, p);
				if (r < 0) {
					addEdit(new AtomicFlipPatch(p));
					processPatch(p);
				} else if (r > 0) processPatch(p);
			}
		}
	}
	
	/**
	* Compares two patches
	* @return 1 if the two patches share a common boundary, -1 if the two patches share a common boundary but in
	* opposit directions, 0 if the two patches don't share a common boundary
	**/
	private int comparePatches(Patch a, Patch b) {
		ControlPoint[] acpA = a.getControlPoints();
		int na = acpA.length;
		ControlPoint[] acpB = b.getControlPoints();
		int nb = acpB.length;
		for (int i = 0; i < acpA.length; i += 2) {
			for (int j = 0; j < acpB.length; j+= 2) {
				if (acpA[i] == acpB[j] && acpA[i + 1] == acpB[j + 1] && acpA[(i - 1 + na) % na] == acpB[(j - 1 + nb) % nb] && acpA[(i + 2) % na] == acpB[(j + 2) % nb]) return -1;
				else if (acpA[i] == acpB[j + 1] && acpA[i + 1] == acpB[j] && acpA[(i - 1 + na) % na] == acpB[(j + 2) % nb] && acpA[(i + 2) % na] == acpB[(j - 1 + nb) % nb]) return 1;
				/* treat hooks specially */
				else if (acpA[i].isStartHook()) {
					ControlPoint cpA = acpA[i].getParentHook();
					ControlPoint cpB = acpA[i].getEnd().getParentHook();
					if (cpA == acpB[j] && cpB == acpB[j + 1]) return -1;
					else if (cpA == acpB[j + 1] && cpB == acpB[j]) return 1;
				} else if (acpA[i + 1].isStartHook()) {
					ControlPoint cpA = acpA[i + 1].getParentHook();
					ControlPoint cpB = acpA[i + 1].getEnd().getParentHook();
					if (cpA == acpB[j] && cpB == acpB[j + 1]) return 1;
					else if (cpA == acpB[j + 1] && cpB == acpB[j]) return -1;
				} else if (acpB[j].isStartHook()) {
					ControlPoint cpA = acpB[j].getParentHook();
					ControlPoint cpB = acpB[j].getEnd().getParentHook();
					if (cpA == acpA[i] && cpB == acpA[i + 1]) return -1;
					else if (cpA == acpA[i + 1] && cpB == acpA[i]) return 1;
				} else if (acpB[j + 1].isStartHook()) {
					ControlPoint cpA = acpB[j + 1].getParentHook();
					ControlPoint cpB = acpB[j + 1].getEnd().getParentHook();
					if (cpA == acpA[i] && cpB == acpA[i + 1]) return 1;
					else if (cpA == acpA[i + 1] && cpB == acpA[i]) return -1;
				}
			}
		}
		return 0;
	}
}
