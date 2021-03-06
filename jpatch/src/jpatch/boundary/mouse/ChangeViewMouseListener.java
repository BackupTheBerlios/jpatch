package jpatch.boundary.mouse;

import java.awt.*;
import java.awt.event.*;

import javax.vecmath.Point3d;
import javax.vecmath.Quat4f;

import jpatch.boundary.*;
import jpatch.boundary.action.*;
import jpatch.boundary.tools.*;
import jpatch.boundary.ui.LockingButtonGroup;
import jpatch.control.edit.*;
import jpatch.entity.*;

public class ChangeViewMouseListener extends MouseAdapter {
	public static final int MOVE = 1;
	public static final int ZOOM = 2;
	public static final int ROTATE = 3;
	
	private float fFocalLength;
	private Quat4f q4CameraOrient = new Quat4f();
	
	protected int iButton;
	protected int iMode;
	
	protected MouseMotionAdapter mouseMotionListener;
	public ChangeViewMouseListener(int button, int mode) {
		iButton = button;
		iMode = mode;
		switch(iMode) {
			case MOVE:
				MainFrame.getInstance().setHelpText("drag to move view");
			break;
			case ZOOM:
				MainFrame.getInstance().setHelpText("drag to zoom view");
			break;
			case ROTATE:
				MainFrame.getInstance().setHelpText("drag to rotate view");
			break;
		}
	}
	
	public void mousePressed(MouseEvent mouseEvent) {
		if (mouseEvent.getButton() == iButton) {
//			MainFrame.getInstance().getDefaultToolTimer().stop();
			switch(iMode) {
				
				case MOVE:
					mouseMotionListener = new MoveViewMotionListener(mouseEvent.getX(),mouseEvent.getY());
				break;
				case ZOOM:
					mouseMotionListener = new ZoomViewMotionListener(mouseEvent.getX(),mouseEvent.getY());
				break;
				case ROTATE:
					mouseMotionListener = new RotateViewMotionListener(mouseEvent.getX(),mouseEvent.getY());
				break;
				
			}
			((Component)mouseEvent.getSource()).addMouseMotionListener(mouseMotionListener);
			ViewDefinition viewDef = MainFrame.getInstance().getJPatchScreen().getViewDefinition((Component) mouseEvent.getSource());
			if (viewDef.getCamera() != null) {
				fFocalLength = viewDef.getCamera().getFocalLength();
				q4CameraOrient.set(viewDef.getCamera().getOrientation());
			}
		} else if (mouseEvent.getButton() == MouseEvent.BUTTON3) {
//			MainFrame.getInstance().getMeshToolBar().reset();
		}
	}
	
	public void mouseReleased(MouseEvent mouseEvent) {
		if (mouseEvent.getButton() == iButton) {
			((Component)mouseEvent.getSource()).removeMouseMotionListener(mouseMotionListener);
//			MainFrame.getInstance().getDefaultToolTimer().restart();
//			MainFrame.getInstance().getJPatchScreen().setTool(new DefaultTool());
			((LockingButtonGroup) Actions.getInstance().getButtonGroup("mode")).actionDone(false);
			ViewDefinition viewDef = MainFrame.getInstance().getJPatchScreen().getViewDefinition((Component) mouseEvent.getSource());
			if (viewDef.getCamera() != null) {
				System.out.println("*");
				Camera camera = viewDef.getCamera();
				ModifyAnimObject edit = new ModifyAnimObject(camera);
				Quat4f newOrientation = camera.getOrientation();
				float newFocalLength = camera.getFocalLength();
				camera.setOrientation(q4CameraOrient);
				camera.setFocalLength(fFocalLength);
				MotionCurveSet.Camera mcs = (MotionCurveSet.Camera) MainFrame.getInstance().getAnimation().getCurvesetFor(camera);
				float position = MainFrame.getInstance().getAnimation().getPosition();
				if (!newOrientation.equals(q4CameraOrient))
					edit.addEdit(new AtomicModifyMotionCurve.Quat4f(mcs.orientation, position, newOrientation));
				if (newFocalLength != fFocalLength) {
					edit.addEdit(new AtomicModifyMotionCurve.Float(mcs.focalLength, position, newFocalLength));
//					edit.addEdit(new AtomicChangeAnimObjectScale(camera, newScale));
				}
				mcs.setPosition(position);
				MainFrame.getInstance().getUndoManager().addEdit(edit);
				MainFrame.getInstance().getTimelineEditor().repaint();
			}
		}
	}
}

