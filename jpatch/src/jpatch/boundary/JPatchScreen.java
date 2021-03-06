package jpatch.boundary;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import jpatch.auxilary.*;
import jpatch.entity.*;
import jpatch.boundary.action.*;
import jpatch.boundary.mouse.*;
import jpatch.boundary.settings.RealtimeRendererSettings;
import jpatch.boundary.settings.Settings;
import jpatch.boundary.settings.ViewportSettings;
import jpatch.boundary.settings.RealtimeRendererSettings.LightingMode;
import jpatch.boundary.tools.*;

public final class JPatchScreen extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final int SINGLE = 1;
	public static final int HORIZONTAL_SPLIT = 2;
	public static final int VERTICAL_SPLIT = 3;
	public static final int QUAD = 4;
	
	public static final int LIGHT_OFF = 0;
	public static final int LIGHT_SIMPLE = 1;
	public static final int LIGHT_HEAD = 2;
	public static final int LIGHT_THREE_POINT = 3;
	
	public static final int NUMBER_OF_VIEWPORTS = 4;
	
	public static final int JAVA2D = 0;
	public static final int SOFTWARE = 1;
	public static final int OPENGL = 2;
	
	private Viewport2 activeViewport;
	
	//private JPatchCanvas[] aComponent = new JPatchCanvas[NUMBER_OF_VIEWPORTS];
	private JPatchDrawable2[] aDrawable = new JPatchDrawable2[NUMBER_OF_VIEWPORTS];
	private ViewDefinition[] aViewDef;
	private Viewport2[] aViewport = new Viewport2[NUMBER_OF_VIEWPORTS];
	
//	private boolean bSnapToGrid = Settings.getInstance().viewports.snapToGrid;
//	private float fGridSpacing = Settings.getInstance().viewports.gridSpacing;
	
	private boolean bSelectPoints = true;
	private boolean bSelectBones = true;
	private boolean bLockPoints = false;
	private boolean bLockBones = false;
	
	private int iMode;
	//protected ControlPoint cpCursor = new ControlPoint(0,0,0);
	
	private boolean bBackfaceNormalFlip = false;
	private int iBackfaceCulling = 0;
//	private boolean bSynchronized = Settings.getInstance().viewports.synchronizeViewports;
	private LightingMode iLightMode = Settings.getInstance().realtimeRenderer.lightingMode;
	private boolean bStickyLight = Settings.getInstance().realtimeRenderer.lightFollowsCamera;
	private JPatchTool tool;
	private final Settings settings = Settings.getInstance();
	
	private MouseListener popupMouseListener = new MouseAdapter() {
		public void mousePressed(MouseEvent mouseEvent) {
//			System.out.println("*");
			if (mouseEvent.getButton() == MouseEvent.BUTTON3) {
				if (popupMenu != null) {
					if (popupMenu.isShowing()) {
						popupMenu.setVisible(false);
					} else {
						popupMenu.show((Component) mouseEvent.getSource(), mouseEvent.getX(),mouseEvent.getY());
					}
				}
			}
		}
	};
	private MoveZoomRotateMouseAdapter moveZoomRotateMouseAdapter = new MoveZoomRotateMouseAdapter();
	
	private boolean bPopupEnabled;
	private ActiveViewportMouseAdapter activeViewportMouseAdapter = new ActiveViewportMouseAdapter();
	private boolean bShowTangents = false;
	private TangentTool tangentTool = Tools.tangentTool;
	private Grid grid = new Grid();
	private JPopupMenu popupMenu;
	
	public JPatchScreen(Model model,int mode,ViewDefinition[] viewDefinitions) {
		aViewDef = viewDefinitions;
		setLightingMode(iLightMode);
		initScreen();
//		setFocusable(false);
		setMode(mode);
		
		enablePopupMenu(true);
		setBackground(settings.colors.background.get());
//		activeViewport = aDrawable[0];
//		snapToGrid(bSnapToGrid);
	}
	
	public void initScreen() {
		int mode = iMode;
		setMode(0);
		if (Settings.getInstance().realtimeRenderer.realtimeRenderer == RealtimeRendererSettings.RealtimeRenderer.OPEN_GL && !JoglInstall.isInstalled()) {
			if (SplashScreen.instance != null)
				SplashScreen.instance.clearSplash();
			JOptionPane.showMessageDialog(MainFrame.getInstance(), new JLabel("Can't use OpenGL display: native JOGL libraries not found."), "Warning", JOptionPane.WARNING_MESSAGE);		
		}
		for (int i = 0; i < NUMBER_OF_VIEWPORTS; i++) {
			final int I = i;
			JPatchDrawableEventListener listener = new JPatchDrawableEventListener() {
				public void display(JPatchDrawable2 drawable) {
					aViewport[I].prepare();
					if (MainFrame.getInstance().getModel() != null)
						aViewport[I].drawRotoscope();
					aViewport[I].drawGrid(grid);
					aViewport[I].drawOrigin();
					if (MainFrame.getInstance().getModel() != null)
						aViewport[I].drawModel(MainFrame.getInstance().getModel(), MainFrame.getInstance().getSelection());
					if (MainFrame.getInstance().getAnimation() != null)
						aViewport[I].drawAnimFrame(MainFrame.getInstance().getAnimation());
					if (tool != null && aViewport[I].getViewDefinition().getCamera() == null)
						aViewport[I].drawTool(tool);
					if (bShowTangents)
						aViewport[I].drawTool(tangentTool);
					aViewport[I].drawInfo();
					aViewport[I].drawBorder(aViewport[I] == activeViewport);
					
					
//					aViewport[I].drawTest();
				}
			};
			switch (Settings.getInstance().realtimeRenderer.realtimeRenderer) {
				case JAVA_2D: aDrawable[i] = new JPatchDrawable2D(listener, false); break;
				case SOFTWARE_ZBUFFER: aDrawable[i] = new JPatchDrawable3D(listener, false); break;
				case OPEN_GL: {
					if (JoglInstall.isInstalled())
						aDrawable[i] = new JPatchDrawableGL(listener, false);
					else
						aDrawable[i] = new JPatchDrawable3D(listener, true);
				} break;
			}
			aDrawable[i].setProjection(JPatchDrawable2.ORTHOGONAL);
			aViewport[i] = new Viewport2(aDrawable[i], aViewDef[i]);
			aViewDef[i].setDrawable(aDrawable[i]);
			//aComponent[i] = new JPatchCanvas(model,aViewDef[i]);
			add(aDrawable[i].getComponent());
//			aDrawable[i].getComponent().setFocusable(false);
			//aViewDef[i].setLighting(RealtimeLighting.createThreepointLight()); // FIXME
			setActiveViewport(aViewport[0]);
			aDrawable[i].getComponent().setFocusable(false);
		}
		setMode(mode);
	}
	
	public void setPopupMenu(JPopupMenu popupMenu){
		this.popupMenu = popupMenu;
	}
	
	public JPopupMenu getPopupMenu() {
		return popupMenu;
	}
	
	
//	public void switchRenderer(int renderer) {
//		JPatchUserSettings.getInstance().iRealtimeRenderer = renderer;
//		initScreen();
//		update_all();
//	}
	
	public ViewDefinition getViewDefinition(Component component) {
		for (int i = 0; i < NUMBER_OF_VIEWPORTS; i++) {
			if (aDrawable[i].getComponent() == component)
				return aViewDef[i];
		}
		throw new IllegalArgumentException("component not found");
	}
	
	public Viewport2 getViewport(Component component) {
		for (int i = 0; i < NUMBER_OF_VIEWPORTS; i++) {
			if (aDrawable[i].getComponent() == component)
				return aViewport[i];
		}
		throw new IllegalArgumentException("component not found");
	}
	
	public Viewport2 getActiveViewport() {
		return activeViewport;
	}
	
	public void setActiveViewport(Component component) {
		requestFocusInWindow();
		setActiveViewport(getViewport(component));
	}
	
	public void setActiveViewport(Viewport2 viewport) {
		if (viewport != activeViewport) {
			Viewport2 old = activeViewport;
			activeViewport = viewport;
			Actions.getInstance().setViewDefinition(activeViewport.getViewDefinition());
			if (old != null)
				old.getDrawable().display();
			viewport.getDrawable().display();
		}
	}
	
	public Grid getGrid() {
		return grid;
	}
	
//	public boolean isSynchronized() {
//		return bSynchronized;
//	}
	
	public boolean flipBackfacingNormals() {
		return bBackfaceNormalFlip;
	}
	
	public int cullBackfacingPolys() {
		return iBackfaceCulling;
	}
	
	public void flipBackfacingNormals(boolean flip) {
//		bBackfaceNormalFlip = flip;
//		for (int i = 0; i < NUMBER_OF_VIEWPORTS; i++) {
//			JPatchDrawable drawable = aComponent[i].getDrawable();
//			if (drawable != null) drawable.getLighting().setBackfaceNormalFlip(flip);
//			//if (drawable instanceof ZBufferRenderer) ((ZBufferRenderer) drawable).setBackfaceNormalFlip(flip);
//		}
	}
	
	public void cullBackfacingPolys(int mode) {
//		iBackfaceCulling = mode;
//		for (int i = 0; i < NUMBER_OF_VIEWPORTS; i++) {
//			JPatchDrawable drawable = aComponent[i].getDrawable();
//			if (drawable instanceof ZBufferRenderer) ((ZBufferRenderer) drawable).setCulling(mode);
//		}
	}
	
//	public void synchronize(boolean sync) {
//		bSynchronized = sync;
//	}
	
	public boolean showTangents() {
		return bShowTangents;
	}
	
	public void showTangents(boolean enable) {
		bShowTangents = enable;
	}
	
	public TangentTool getTangentTool() {
		return tangentTool;
	}
	
	public void setLightingMode(LightingMode mode) {
		iLightMode = mode;
		for (int i = 0; i < NUMBER_OF_VIEWPORTS; i++) {
			switch(iLightMode) {
				case OFF:
					aViewDef[i].setLighting(null);
					break;
				case SIMPLE:
					aViewDef[i].setLighting(RealtimeLighting.createSimpleLight());
					break;
				case HEADLIGHT:
					aViewDef[i].setLighting(RealtimeLighting.createHeadLight());
					break;
				case THREE_POINT:
					aViewDef[i].setLighting(RealtimeLighting.createThreepointLight());
					break;
			}
		}
		update_all();
	}
	
	public void checkCameraViewports(Camera camera) {
		for (int i = 0; i < NUMBER_OF_VIEWPORTS; i++) {
			if (aViewDef[i].getCamera() == camera)
				aViewDef[i].setView(ViewDefinition.BIRDS_EYE);
		}
		setTool(tool);
	}
	
	public LightingMode getLightingMode() {
		return iLightMode;
	}
	
//	public void setActiveViewport(Viewport viewport) {
//		if (viewport != activeViewport) {
//			activeViewport = viewport;
//			//for (int i = 0; i < NUMBER_OF_VIEWPORTS; i++) {
//			//	aComponent[i].drawActiveViewportMarker(aComponent[i] == viewport);
//			//}
//			update_all();
//		}
//	}
	
//	public Viewport getActiveViewport() {
//		return activeViewport;
//	}
	
	public void setStickyLight(boolean sticky) {
		bStickyLight = sticky;
//		for (int i = 0; i < NUMBER_OF_VIEWPORTS; i++) {
//			aComponent[i].getLighting().setStickyLight(sticky);
//		}
//		update_all();
	}
	
//	public boolean snapToGrid() {
//		return bSnapToGrid;
//	}
//	
//	public void snapToGrid(boolean enable) {
//		bSnapToGrid = enable;
//		for (int i = 0; i < NUMBER_OF_VIEWPORTS; i++) {
////			aComponent[i].getGrid().snap(enable);
//		}
//		update_all();
//		Settings.getInstance().viewports.snapToGrid = enable;
//	}
	
//	public float getGridSpacing() {
//		return fGridSpacing;
//	}
	
//	public void setGridSpacing(float gridSpacing) {
//		grid.setSpacing(gridSpacing);
////		fGridSpacing = gridSpacing;
////		for (int i = 0; i < NUMBER_OF_VIEWPORTS; i++) {
//////			aComponent[i].getGrid().setSpacing(gridSpacing);
////		}
//		update_all();
//	}
	
	public boolean isStickyLight() {
		return bStickyLight;
	}
	
	public void single_update(Component component) {
		if (settings.viewports.synchronizeViewports) {
			update_all();
		} else {
			getViewDefinition(component).getDrawable().display();
		}
	}
	
	public void full_update() {
		//System.out.println("full_update()");
		if (!settings.viewports.synchronizeViewports) {
			update_all();
		}
	}
	
	public void update_all() {
		switch(iMode) {
			case SINGLE:
				aDrawable[0].display();
				break;
			case HORIZONTAL_SPLIT:
				aDrawable[0].display();
				aDrawable[1].display();
				break;
			case VERTICAL_SPLIT:
				aDrawable[0].display();
				aDrawable[2].display();
				break;
			case QUAD:
				aDrawable[0].display();
				aDrawable[1].display();
				aDrawable[2].display();
				aDrawable[3].display();
				break;
		}
	}
	
	public void zoomToFit_all() {
		switch(iMode) {
			case SINGLE:
				ZoomToFitAction.zoomToFit(aViewport[0]);
				break;
			case HORIZONTAL_SPLIT:
				ZoomToFitAction.zoomToFit(aViewport[0]);
				ZoomToFitAction.zoomToFit(aViewport[1]);
				break;
			case VERTICAL_SPLIT:
				ZoomToFitAction.zoomToFit(aViewport[0]);
				ZoomToFitAction.zoomToFit(aViewport[2]);
				break;
			case QUAD:
				ZoomToFitAction.zoomToFit(aViewport[0]);
				ZoomToFitAction.zoomToFit(aViewport[1]);
				ZoomToFitAction.zoomToFit(aViewport[2]);
				ZoomToFitAction.zoomToFit(aViewport[3]);
				break;
		}
	}
	
//	public void prepareBackground(Component component) {
//		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
//		if (bSynchronized) {
//			for (int i = 0; i < NUMBER_OF_VIEWPORTS; i++) {
//				if (aComponent[i].isVisible()) {
//					((JPatchCanvas)aComponent[i]).prepareBackground();
//				}
//			}
//		} else {
//			((JPatchCanvas)component).prepareBackground();
//		}
//		setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
//	}
//	
//	public void clearBackground() {
//		for (int i = 0; i < NUMBER_OF_VIEWPORTS; i++) {
//			//if (aComponent[i].isVisible()) {
//				((JPatchCanvas)aComponent[i]).clearBackground();
//			//}
//		}
//	}
	
	//public void set3DCursor(Point3f cursor) {
	//	cpCursor.setPosition(cursor);
	//}
	//public void set3DCursor(float x, float y, float z) {
	//	cpCursor.setPosition(x,y,z);
	//}
	//public ControlPoint get3DCursor() {
	//	return cpCursor;
	//}
	
	public void setMouseListener(MouseListener mouseAdapter) {
		removeAllMouseListeners();
		for (int i = 0; i < NUMBER_OF_VIEWPORTS; i++) {
			aDrawable[i].getComponent().addMouseListener(mouseAdapter);
		}
		addMMBListener();
//		MainFrame.getInstance().getDefaultToolTimer().stop();
	}
	
	public void setTool(JPatchTool tool) {
		this.tool = tool;
		if (tool == Tools.defaultTool) {
			Actions.getInstance().getButtonModel("default tool").setSelected(true);
		} else if (tool == Tools.rotateTool) {
			Tools.rotateTool.reInit(MainFrame.getInstance().getSelection());
			Actions.getInstance().getButtonModel("rotate tool").setSelected(true);
		} else if (tool == Tools.rotoscopeTool) {
			Actions.getInstance().getButtonModel("rotoscope tool").setSelected(true);
		}
		removeAllMouseListeners();
		for (int i = 0; i < NUMBER_OF_VIEWPORTS; i++) {
//			((JPatchCanvas)aComponent[i]).setTool(tool);
			if (aViewport[i].getViewDefinition().getCamera() == null && tool != null)
				aViewport[i].getDrawable().getComponent().addMouseListener(tool);
		}
		addMMBListener();
		update_all();
//		if (tool instanceof DefaultTool)
//			Command.getMenuItemFor("default tool").setSelected(true);
//		if (tool instanceof RotateTool)
//			Command.getMenuItemFor("rotate tool").setSelected(true);
	}
	
	public JPatchTool getTool() {
		return tool;
	}
	
	private void removeAllMouseListeners() {
		for (int i = 0; i < NUMBER_OF_VIEWPORTS; i++) {
			MouseListener[] aMouseListener = aDrawable[i].getComponent().getMouseListeners();
			MouseMotionListener[] aMouseMotionListener = aDrawable[i].getComponent().getMouseMotionListeners();
			MouseWheelListener[] aMouseWheelListener = aDrawable[i].getComponent().getMouseWheelListeners();
			for (int m = 0; m < aMouseListener.length; m++) {
				aDrawable[i].getComponent().removeMouseListener(aMouseListener[m]);
			}
			for (int m = 0; m < aMouseMotionListener.length; m++) {
				aDrawable[i].getComponent().removeMouseMotionListener(aMouseMotionListener[m]);
			}
			for (int m = 0; m < aMouseWheelListener.length; m++) {
				aDrawable[i].getComponent().removeMouseWheelListener(aMouseWheelListener[m]);
			}
		}
		enablePopupMenu(false);
		enablePopupMenu(true);
	}
	/*
	public void rerender() {
		switch(mode) {
			case SINGLE:
				aComponent[0].render();
			break;
			case HORIZONTAL_SPLIT:
				aComponent[0].render();
				aComponent[1].render();
			break;
			case VERTICAL_SPLIT:
				aComponent[0].render();
				aComponent[2].render();
			break;
			case QUAD:
				aComponent[0].render();
				aComponent[1].render();
				aComponent[2].render();
				aComponent[3].render();
			break;
		}
	}
	*/
	public void enablePopupMenu(boolean enable) {
		//System.out.println("popup: " + bPopupEnabled + " " + enable);
		if (enable != bPopupEnabled) {
			for (int i = 0; i < NUMBER_OF_VIEWPORTS; i++) {
				if (enable) {
					aDrawable[i].getComponent().addMouseListener(popupMouseListener);
				} else {
					aDrawable[i].getComponent().removeMouseListener(popupMouseListener);
				}
			}
			bPopupEnabled = enable;
		}
	}
	
	public void addMMBListener() {
		for (int i = 0; i < NUMBER_OF_VIEWPORTS; i++) {
			aDrawable[i].getComponent().addMouseListener(moveZoomRotateMouseAdapter);
			aDrawable[i].getComponent().addMouseWheelListener(moveZoomRotateMouseAdapter);
			aDrawable[i].getComponent().addMouseListener(activeViewportMouseAdapter);
			aDrawable[i].getComponent().addMouseWheelListener(activeViewportMouseAdapter);
			if (bShowTangents) {
				aDrawable[i].getComponent().addMouseListener(tangentTool);
			}
		}
	}
	
	public int getMode() {
		return iMode;
	}
	
	public Dimension getMinimumSize() {
		return new Dimension(100, 100);
	}
	
	public void setMode(int mode) {
		
		iMode = mode;
		int h = getHeight();
		int w = getWidth();
		int h2 = h/2;
		int w2 = w/2;
//		aComponent[0].setVisible(false);
//		aComponent[1].setVisible(false);
//		aComponent[2].setVisible(false);
//		aComponent[3].setVisible(false);
//		aComponent[0].clearImage();
//		aComponent[1].clearImage();
//		aComponent[2].clearImage();
//		aComponent[3].clearImage();
		//update_all();
		
		if (h > 0 && w > 0) {
			switch(mode) {
				case 0:
					aDrawable[0].getComponent().setVisible(false);
					aDrawable[1].getComponent().setVisible(false);
					aDrawable[2].getComponent().setVisible(false);
					aDrawable[3].getComponent().setVisible(false);
				break;
				case SINGLE:
//					activeViewport = aComponent[0];
					aDrawable[0].getComponent().setBounds(0,0,w,h);
					aDrawable[0].getComponent().setVisible(true);
					aDrawable[1].getComponent().setBounds(0,0,0,0);
					aDrawable[1].getComponent().setVisible(false);
					aDrawable[2].getComponent().setBounds(0,0,0,0);
					aDrawable[2].getComponent().setVisible(false);
					aDrawable[3].getComponent().setBounds(0,0,0,0);
					aDrawable[3].getComponent().setVisible(false);
				break;
				case HORIZONTAL_SPLIT:
//					if (activeViewport != aComponent[0] && activeViewport != aComponent[1]) {
//						activeViewport = aComponent[0];
//					}
					aDrawable[0].getComponent().setBounds(0,0,w2,h);
					aDrawable[0].getComponent().setVisible(true);
					aDrawable[1].getComponent().setBounds(w2,0,w2,h);
					aDrawable[1].getComponent().setVisible(true);
					aDrawable[2].getComponent().setBounds(0,0,0,0);
					aDrawable[2].getComponent().setVisible(false);
					aDrawable[3].getComponent().setBounds(0,0,0,0);
					aDrawable[3].getComponent().setVisible(false);
				break;
				case VERTICAL_SPLIT:
//					if (activeViewport != aComponent[0] && activeViewport != aComponent[2]) {
//						activeViewport = aComponent[0];
//					}
					aDrawable[0].getComponent().setBounds(0,0,w,h2);
					aDrawable[0].getComponent().setVisible(true);
					aDrawable[1].getComponent().setBounds(0,0,0,0);
					aDrawable[1].getComponent().setVisible(false);
					aDrawable[2].getComponent().setBounds(0,h2,w,h2);
					aDrawable[2].getComponent().setVisible(true);
					aDrawable[3].getComponent().setBounds(0,0,0,0);
					aDrawable[3].getComponent().setVisible(false);
				break;
				case QUAD:
					aDrawable[0].getComponent().setBounds(0,0,w2,h2);
					aDrawable[0].getComponent().setVisible(true);
					aDrawable[1].getComponent().setBounds(w2,0,w2,h2);
					aDrawable[1].getComponent().setVisible(true);
					aDrawable[2].getComponent().setBounds(0,h2,w2,h2);
					aDrawable[2].getComponent().setVisible(true);
					aDrawable[3].getComponent().setBounds(w2,h2,w2,h2);
					aDrawable[3].getComponent().setVisible(true);

				break;
			}
		}
		
		switch (iMode) {
		case 1:
			Settings.getInstance().viewports.viewportMode = ViewportSettings.ScreenMode.SINGLE;
			break;
		case 2:
			Settings.getInstance().viewports.viewportMode = ViewportSettings.ScreenMode.HORIZONTAL_SPLIT;
			break;
		case 3:
			Settings.getInstance().viewports.viewportMode = ViewportSettings.ScreenMode.VERTICAL_SPLIT;
			break;
		case 4:
			Settings.getInstance().viewports.viewportMode = ViewportSettings.ScreenMode.QUAD;
			break;
		}
		//JPatchUserSettings.getInstance().saveSettings();
		
		//update_all();
		/*
		iMode = mode;
		removeAll();
		switch(mode) {
			case SINGLE:
				setLayout(new GridLayout(1,1));
				add(aComponent[0], BorderLayout.CENTER);
				break;
			case HORIZONTAL_SPLIT:
				setLayout(new GridLayout(1,2));
				add(aComponent[0]);
				add(aComponent[1]);
				break;
			case VERTICAL_SPLIT:
				setLayout(new GridLayout(1,2));
				add(aComponent[0]);
				add(aComponent[2]);
				break;
			case QUAD:
				setLayout(new GridLayout(2,2));
				add(aComponent[0]);
				add(aComponent[1]);
				add(aComponent[2]);
				add(aComponent[3]);
				break;
		}
		validate();
		*/
	}
	
	public void doLayout() {
//		super.doLayout();
		setMode(iMode);
	}
	
	public void resetMode(int mode) {
		setMode(mode);
	}

	public boolean isLockBones() {
		return bLockBones;
	}

	public void setLockBones(boolean lockBones) {
		bLockBones = lockBones;
	}

	public boolean isLockPoints() {
		return bLockPoints;
	}

	public void setLockPoints(boolean lockPoints) {
		bLockPoints = lockPoints;
	}

	public boolean isSelectBones() {
		return bSelectBones;
	}

	public void setSelectBones(boolean selectBones) {
		bSelectBones = selectBones;
	}

	public boolean isSelectPoints() {
		return bSelectPoints;
	}

	public void setSelectPoints(boolean selectPoints) {
		bSelectPoints = selectPoints;
	}
}
