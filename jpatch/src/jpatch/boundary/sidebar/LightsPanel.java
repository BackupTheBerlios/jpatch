package jpatch.boundary.sidebar;

import jpatch.boundary.*;
import jpatch.boundary.action.*;
import jpatch.boundary.ui.*;

public class LightsPanel extends SidePanel {

	private static final long serialVersionUID = 1L;

	public LightsPanel() {
		add(new JPatchButton(new NewLightAction()));
		MainFrame.getInstance().getSideBar().clearDetailPanel();
	}
}