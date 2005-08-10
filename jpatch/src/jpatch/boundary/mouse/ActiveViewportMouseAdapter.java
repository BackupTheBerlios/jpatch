package jpatch.boundary.mouse;

import java.awt.event.*;
import jpatch.boundary.*;

public class ActiveViewportMouseAdapter extends JPatchMouseAdapter {
	
	public void mousePressed(MouseEvent mouseEvent) {
		MainFrame.getInstance().getJPatchScreen().setActiveViewport((Viewport) mouseEvent.getSource());
	}
	
	public void mouseWheelMoved(MouseWheelEvent mouseWheelEvent) {
		MainFrame.getInstance().getJPatchScreen().setActiveViewport((Viewport) mouseWheelEvent.getSource());
	}
}
