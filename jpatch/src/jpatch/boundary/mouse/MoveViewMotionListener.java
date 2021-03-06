package jpatch.boundary.mouse;

import java.awt.*;
import java.awt.event.*;
import jpatch.boundary.*;

public class MoveViewMotionListener extends MouseMotionAdapter {
	protected int iMouseX;
	protected int iMouseY;
	protected int iWidth;
	protected int iHeight;
	protected float fMin;
	
	public MoveViewMotionListener(int mouseX, int mouseY) {
		iMouseX = mouseX;
		iMouseY = mouseY;
	}
	
	public void mouseDragged(MouseEvent mouseEvent) {
		ViewDefinition viewDef = MainFrame.getInstance().getJPatchScreen().getViewDefinition((Component) mouseEvent.getSource());
		int deltaX = mouseEvent.getX() - iMouseX;
		int deltaY = mouseEvent.getY() - iMouseY;
		iMouseX = mouseEvent.getX();
		iMouseY = mouseEvent.getY();
		iWidth = ((Component)(mouseEvent.getSource())).getWidth();
		iHeight = ((Component)(mouseEvent.getSource())).getHeight();
		//fMin = (iWidth < iHeight) ? iWidth : iHeight;
		fMin = iWidth/2;
		viewDef.setLock(null);
		viewDef.moveView((float)deltaX/fMin,(float)deltaY/fMin);
		//((Viewport)(mouseEvent.getSource())).getViewDefinition().moveView(deltaX,deltaY);
	}
}
