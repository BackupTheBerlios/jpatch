package jpatch.boundary.mouse;

import java.awt.*;
import java.awt.event.*;
import jpatch.boundary.*;

public class MoveZoomRotateMouseAdapter extends JPatchMouseAdapter {
	private static final int MOVE = 1;
	private static final int ROTATE = 2;
	
	private int iMouseX;
	private int iMouseY;
	private float fMin;
	private int iState;
	
	public void mousePressed(MouseEvent mouseEvent) {
		if (mouseEvent.getButton() == MouseEvent.BUTTON2) {
			ViewDefinition viewDef = MainFrame.getInstance().getJPatchScreen().getViewDefinition((Component) mouseEvent.getSource());
			((Component)mouseEvent.getSource()).addMouseMotionListener(this);
			iMouseX = mouseEvent.getX();
			iMouseY = mouseEvent.getY();
//			int dx = iMouseX - 40;
//			int dy = iMouseY - ((Component)mouseEvent.getSource()).getHeight() + 40;
//			if ((dx * dx + dy * dy) > 2500 && !viewDef.isLocked()) {
//				iState = MOVE;
//			} else {
//				iState = ROTATE;
//			}
			iState = (mouseEvent.isControlDown() || viewDef.isLocked()) ? ROTATE : MOVE;
		}
	}
	
	public void mouseReleased(MouseEvent mouseEvent) {
		if (mouseEvent.getButton() == MouseEvent.BUTTON2) {
			((Component)mouseEvent.getSource()).removeMouseMotionListener(this);
		}
	}
	
	public void mouseDragged(MouseEvent mouseEvent) {
		ViewDefinition viewDef = MainFrame.getInstance().getJPatchScreen().getViewDefinition((Component) mouseEvent.getSource());
		int deltaX = mouseEvent.getX() - iMouseX;
		int deltaY = mouseEvent.getY() - iMouseY;
		iMouseX = mouseEvent.getX();
		iMouseY = mouseEvent.getY();
		switch (iState) {
			case MOVE:
				int iWidth = ((Component)(mouseEvent.getSource())).getWidth();
				//int iHeight = ((Component)(mouseEvent.getSource())).getHeight();
				//fMin = (iWidth < iHeight) ? iWidth : iHeight;
				fMin = iWidth/2;
				viewDef.moveView((float)deltaX/fMin,(float)deltaY/fMin);
				break;
			case ROTATE:
				viewDef.rotateView((float)deltaX/200,(float)deltaY/200);
				break;
		}
	}
	
	public void mouseWheelMoved(MouseWheelEvent mouseWheelEvent) {
		ViewDefinition viewDef = MainFrame.getInstance().getJPatchScreen().getViewDefinition((Component) mouseWheelEvent.getSource());
		int wheelClicks = mouseWheelEvent.getWheelRotation();
		float scale = (1-((float)wheelClicks)/10);
		if (scale < 0.2) scale = (float)0.2;
		if (scale > 5) scale = (float)5;
		viewDef.scaleView(scale);
	}
}
