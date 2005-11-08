package jpatch.boundary;

import inyo.RtInterface;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.imageio.*;
import javax.sound.sampled.*;
import javax.vecmath.*;

import patterns.TextureParser;
import buoy.event.*;
import buoy.widget.*;
import jpatch.entity.*;
import jpatch.renderer.*;
import jpatch.auxilary.*;

public final class Animator extends BFrame {
	private static Animator INSTANCE;
	
	private ArrayList listObjects = new ArrayList();
	private BorderContainer content = new BorderContainer();
	private Camera camera1 = new Camera("Camera 1");
	private CameraViewport cameraViewport = new CameraViewport(camera1, listObjects);
	private PoseSliders poseSliders;
	private MotionCurveEditor motionCurveEditor;
	private MotionCurveDisplay motionCurveDisplay;
	private BSplitPane splitPane;
	
	private String strPrefix = "frame_";
	
	private BDialog propertiesDialog;
	private BTextField textStart;
	private BTextField textEnd;
	private BTextField textRate;
	private BTextField textModelDir;
	private BTextField textPrefix;
	private BTextArea textPov;
	private BTextArea textRib;
	private float fStart;
	private float fEnd;
	private float fPosition;
	private float fFramerate = 24;
	
	private Clip clip;
	
	private HashMap mapMotionCurves = new HashMap();
	
	private HashMap mapFilenames = new HashMap();
	
	private MotionKey2 activeKey;
	private MotionCurve2 activeCurve;
	
	private RenderExtension re = new RenderExtension(new String[] {
		"povray", "",
		"renderman", ""
	});
	
	private Animator() {
		//super("JPatch Animator");
		
		if (jpatch.VersionInfo.release) {
			setTitle("JPatch Animator " + jpatch.VersionInfo.version);
		} else {
			setTitle("JPatch Animator " + jpatch.VersionInfo.version + " compiled " + jpatch.VersionInfo.compileTime);
		}
		
		INSTANCE = this;
		
		try {
			UIManager.setLookAndFeel(JPatchSettings.getInstance().strPlafClassName);
			JPopupMenu.setDefaultLightWeightPopupEnabled(false);
		} catch (Exception e) {
		}
			
		fStart = fPosition = 0;
		fEnd = 10 * fFramerate - 1;
		
		camera1.setPosition(new Point3d(0f, 0f, -100f));
		mapMotionCurves.put(camera1, new MotionCurveSet.Camera(camera1));
		listObjects.add(camera1);
		
		poseSliders = new PoseSliders();
		
		motionCurveEditor = new MotionCurveEditor();
		motionCurveDisplay = motionCurveEditor.getMotionCurveDisplay();
			
		splitPane = new BSplitPane(BSplitPane.VERTICAL, cameraViewport.getWidget(), motionCurveEditor);
		splitPane.addEventLink(ValueChangedEvent.class, this, "rerenderViewports");
		content.add(splitPane, BorderContainer.CENTER);
		
		content.add(poseSliders.getContent(), BorderContainer.EAST);
		setContent(content);
		setMenuBar(new AnimatorMainMenu());
		pack();
		addEventLink(WindowClosingEvent.class, this, "quit");
		setBounds(new java.awt.Rectangle(0,0,1000,700));
		setVisible(true);
		poseSliders.init();
		rerenderViewports();
		setActiveObject(camera1);
	}
	
	public void NEW() {
		listObjects.clear();
		mapMotionCurves.clear();
		fStart = fPosition = 0;
		fEnd = 10 * fFramerate - 1;
		strPrefix = "frame_";
		camera1.setOrientation(0, 0, 0);
		camera1.setFocalLength(50);
		camera1.setPosition(new Point3d(0f, 0f, -100f));
		mapMotionCurves.put(camera1, new MotionCurveSet.Camera(camera1));
		listObjects.add(camera1);
		setActiveObject(camera1);
		rerenderViewports();
	}
	
	public void setClip(Clip clip) {
		if (this.clip != null) this.clip.flush();
		this.clip = clip;
		clip.setMicrosecondPosition((long) (fPosition /fFramerate * 1000));
	}
	
	public Clip getClip() {
		return clip;
	}
	
	public static Animator getInstance() {
		return (INSTANCE == null) ? new Animator() : INSTANCE;	// Singleton pattern
	}

	public ArrayList getObjectList() {
		return listObjects;
	}
	
	public void enumerateLights() {
		int num = 0;
		for (Iterator it = listObjects.iterator(); it.hasNext(); ) {
			Object o = it.next();
			if (o instanceof AnimLight) {
				((AnimLight) o).setNumber(num++);
			}
		}
	}
	
	public float getStart() {
		return fStart;
	}
	
	public float getEnd() {
		return fEnd;
	}
	
	public float getPosition() {
		return fPosition;
	}
	
	public float getFramerate() {
		return fFramerate;
	}
	
	public void stop() {
		motionCurveEditor.stop();
	}
	
	public MotionKey2 getActiveKey() {
		return activeKey;
	}
	
	public MotionCurve2 getActiveCurve() {
		return activeCurve;
	}
	
	public void setActiveKey(MotionKey2 key) {
		activeKey = key;
	}
	
	public void setActiveCurve(MotionCurve2 curve) {
		activeCurve = curve;
	}
	
	public void setPosition(float position) {
		fPosition = position;
		for (Iterator it = listObjects.iterator(); it.hasNext(); ((MotionCurveSet) mapMotionCurves.get(it.next())).setPosition(position));
		motionCurveEditor.setPosition(position);
		poseSliders.moveSliders();
		poseSliders.repaint();
		rerenderViewports();
		if (clip != null && !clip.isRunning()) clip.setMicrosecondPosition((long) (fPosition /fFramerate * 1000));
	}
	
	public void redrawMotionCurveDisplay() {
		motionCurveDisplay.repaint();
	}
	
	public void reinitMotionCurveDisplay() {
		motionCurveDisplay.init();
		motionCurveEditor.repaint();
	}
	
	public AnimObject getActiveObject() {
		return poseSliders.getActiveObject();
	}

	public void setActiveObject(AnimObject object) {
		//cameraViewport.setActiveObject(object);
		poseSliders.setActiveObject(object);
		rerenderViewports();
		reinitMotionCurveDisplay();
	}
	
	public void rerenderViewports() {
		cameraViewport.repaint();
	}

	public void repaintViewports() {
		cameraViewport.repaint();
	}
	
	public MotionCurveSet getMotionCurveSetFor(AnimObject animObject) {
		return (MotionCurveSet) mapMotionCurves.get(animObject);
	}
	
	public void setMotionCurveSetFor(AnimObject animObject, MotionCurveSet motionCurveSet) {
		mapMotionCurves.put(animObject, motionCurveSet);
	}
	
	public void setMorphValue(AnimModel animModel, Morph morph) {
		MotionCurve2.Float mc =  ((MotionCurveSet.Model) mapMotionCurves.get(animModel)).morph(morph);
		activeCurve = mc;
		activeKey = mc.setFloatAt(fPosition, morph.getValue());
		motionCurveEditor.repaint();
		rerenderViewports();
	}
	
	public void updateCurvesFor(AnimObject animObject) {
		((MotionCurveSet) mapMotionCurves.get(animObject)).updateCurves(fPosition);
		motionCurveEditor.repaint();
	}

	public void addObject(AnimObject object) {
		addObject(object, null);
	}
	
	public void addObject(AnimObject object, String filename) {
		addObject(object, filename, MotionCurveSet.createMotionCurveSetFor(object));
	}
	
	public void addObject(AnimObject object, String filename, MotionCurveSet motionCurveSet) {
		listObjects.add(object);
		mapFilenames.put(object, filename);
		mapMotionCurves.put(object, motionCurveSet);
		if (object instanceof AnimLight) enumerateLights();
	}
	
	public void removeObject(AnimObject object) {
		if (object != camera1) {
			listObjects.remove(object);
			mapMotionCurves.remove(object);
			if (object == getActiveObject()) setActiveObject(camera1);
			else {
				poseSliders.init();
				rerenderViewports();
			}
			if (object instanceof AnimLight) enumerateLights();
		}
	}
	
	public Camera getActiveCamera() {
		return camera1;
	}
	
	public void preferences() {
		propertiesDialog = new BDialog(this, "Preferences", true);
		propertiesDialog.setResizable(false);
		textModelDir = new BTextField(JPatchSettings.getInstance().strModelDir, 20);
		FormContainer form = new FormContainer(3, 1);
		BButton buttonBrowse = new BButton("browse");
		form.add(new BLabel("Model directory:"), 0, 0);
		form.add(textModelDir, 1, 0);
		form.add(buttonBrowse, 2, 0);
		RowContainer buttons = new RowContainer();
		BButton buttonOK = new BButton("OK");
		BButton buttonCancel = new BButton("Cancel");
		buttons.add(buttonOK);
		buttons.add(buttonCancel);
		propertiesDialog.addEventLink(WindowClosingEvent.class, propertiesDialog, "dispose");
		buttonCancel.addEventLink(CommandEvent.class, propertiesDialog, "dispose");
		buttonOK.addEventLink(CommandEvent.class, this, "setPrefs");
		buttonBrowse.addEventLink(CommandEvent.class, this, "openFileChooser");
		
		ColumnContainer content = new ColumnContainer();
		content.add(form);
		content.add(buttons);
		
		propertiesDialog.setContent(content);
		propertiesDialog.pack();
		((Window) propertiesDialog.getComponent()).setLocationRelativeTo(getComponent());
		propertiesDialog.setVisible(true);
	}
	
	public void properties() {
		propertiesDialog = new BDialog(this, "Sequence properties", true);
		propertiesDialog.setResizable(false);
		FormContainer form = new FormContainer(2, 4);
		form.add(new BLabel("Sequence start:"), 0, 0);
		form.add(new BLabel("Sequence end:"), 0, 1);
		form.add(new BLabel("Frame rate:"), 0, 2);
		form.add(new BLabel("File prefix:"), 0, 3);
		//form.add(new BLabel("Model directory:"), 0, 3);
		//BButton buttonBrowse = new BButton("browse");
		//form.add(buttonBrowse, 2, 3);
		textStart = new BTextField("" + fStart, 20);
		textEnd = new BTextField("" + fEnd, 20);
		textRate = new BTextField("" + fFramerate, 20);
		textPrefix = new BTextField(strPrefix, 20);
		//textModelDir = new BTextField(JPatchSettings.getInstance().strModelDir, 20);
		textPov = new BTextArea(re.getRenderString("povray", ""), 10, 50);
		textRib = new BTextArea(re.getRenderString("renderman", ""), 10, 50);
		form.add(textStart, 1, 0);
		form.add(textEnd, 1, 1);
		form.add(textRate, 1, 2);
		form.add(textPrefix, 1, 3);
		//form.add(textModelDir, 1, 3);
		RowContainer buttons = new RowContainer();
		BButton buttonOK = new BButton("OK");
		BButton buttonCancel = new BButton("Cancel");
		buttons.add(buttonOK);
		buttons.add(buttonCancel);
		
		BTabbedPane tabbedPane = new BTabbedPane();
		tabbedPane.add(new BScrollPane(textPov), "POV-Ray");
		tabbedPane.add(new BScrollPane(textRib), "RenderMan");
		
		ColumnContainer content = new ColumnContainer();
		content.add(form);
		content.add(tabbedPane);
		content.add(buttons);
		
		propertiesDialog.addEventLink(WindowClosingEvent.class, propertiesDialog, "dispose");
		buttonCancel.addEventLink(CommandEvent.class, propertiesDialog, "dispose");
		buttonOK.addEventLink(CommandEvent.class, this, "setProperties");
		//buttonBrowse.addEventLink(CommandEvent.class, this, "openFileChooser");
		propertiesDialog.setContent(content);
		propertiesDialog.pack();
		((Window) propertiesDialog.getComponent()).setLocationRelativeTo(getComponent());
		propertiesDialog.setVisible(true);
	}
	
	public void setRenderString(String format, String version, String renderString) {
		re.setRenderString(format, version, renderString);
	}
	
	public String getRenderString(String format, String version) {
		return re.getRenderString(format, version);
	}
	
	public StringBuffer renderStrings(String prefix) {
		return re.xml(prefix);
	}
	
	private void openFileChooser() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fileChooser.setSelectedFile(new File(textModelDir.getText()));
		if (fileChooser.showDialog(this.getComponent(), "Select") == JFileChooser.APPROVE_OPTION) {
			textModelDir.setText(fileChooser.getSelectedFile().getPath());
		}
	}
	
	private void setProperties() {
		try {
			fStart = Float.parseFloat(textStart.getText());
			fEnd = Float.parseFloat(textEnd.getText());
			fFramerate = Math.round(Float.parseFloat(textRate.getText()));
			re.setRenderString("povray", "", textPov.getText());
			re.setRenderString("renderman", "", textRib.getText());
			strPrefix = textPrefix.getText();
			propertiesDialog.dispose();
			reinitMotionCurveDisplay();
			motionCurveEditor.setPosition(fPosition);
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}
	
	private void setPrefs() {
		JPatchSettings.getInstance().strModelDir = textModelDir.getText();
		JPatchSettings.getInstance().saveSettings();
		propertiesDialog.dispose();
	}
	
	public void setPrefix(String prefix) {
		strPrefix = prefix;
	}
	
	public String getPrefix() {
		return strPrefix;
	}
	
	void parseTimesheet(String filename) {
		try {
			AnimObject animObject = getActiveObject();
			if (animObject instanceof AnimModel) {
				MotionCurveSet.Model mcs = ((MotionCurveSet.Model) mapMotionCurves.get(animObject));
				Model model = ((AnimModel) animObject).getModel();
				BufferedReader brFile = new BufferedReader(new FileReader(filename));
				String strLine;
				while ((strLine = brFile.readLine()) != null) {
					String mpFrame = strLine.substring(0, 5);
//					String mpTime = strLine.substring(6, 18);
//					String mpKey = strLine.substring(21, 22);
					String mpMouth = strLine.substring(25, (strLine.length() >= 38) ? 38 : strLine.length());
					
					if (mpFrame.equals("Frame")) continue;
					int frame = Integer.parseInt(mpFrame.replaceAll("^\\s*","").replaceAll("\\s*$",""));
					Morph morph = model.getMorphFor(mpMouth.replaceAll("^\\s*","").replaceAll("\\s*$",""));
					if (morph != null) {
						for (Iterator it = model.getPhonemeMorphSet().iterator(); it.hasNext(); ) {
							Morph m = (Morph) it.next();
							MotionCurve2.Float mc =  mcs.morph(m);
							mc.setFloatAt((float) (frame - 1), (m == morph) ? 1 : 0);
						}
					}
					//System.out.println(strLine);
					//strLine = strLine.replaceAll("^\\s*","");	//remove leading spaces
					//strLine = strLine.replaceAll("\\s*$","");	//remove trailing spaces
					//String[] parts = strLine.split("\\s+");		//split on whitespace
					//if (parts[0].matches("^\\d+$")) {		//check if firt string is a number
					//	frame = (new Integer(parts[0]).intValue());
					//	if (parts.length == 3) {
					//		Morph morph = model.getMorphFor(parts[2]);
					//		for (Iterator it = model.getPhonemeMorphSet().iterator(); it.hasNext(); ) {
					//			//System.out.println(it.next());
					//			Morph m = (Morph) it.next();
					//			MotionCurve2.Float mc =  mcs.morph(m);
					//			mc.setFloatAt((float) (frame - 1), (m == morph) ? 1 : 0);
					//		}
					//	}
					//}
				}
				motionCurveEditor.repaint();
				rerenderViewports();
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}
	
	private void quit() {
		if (JOptionPane.showConfirmDialog(Animator.getInstance().getComponent(), "Are you sure?", "Quit JPatch", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
			dispose();
			System.exit(0);
		}
	}
	
	public void renderCurrentFrame() {
		new ProgressDisplay((int) Math.round(fPosition + 1), (int) Math.round(fPosition + 1));
		
		//progressDisplay.set(0,1);
		//JPatchSettings settings = JPatchSettings.getInstance();
		//imagePanel.setImage(new BufferedImage(settings.iRenderWidth, settings.iRenderHeight, BufferedImage.TYPE_INT_RGB));
		//renderFrame(frameName);
	}
		
	/**
	 * @param frameName
	 * @param progressDisplay
	 */
	public void renderFrame(String frameName, ProgressDisplay progressDisplay) {
		
		JPatchSettings settings = JPatchSettings.getInstance();
		//progressDisplay.clearText();
		//progressDisplay.show();
		/* output geometry to temporary file */
		ArrayList models = new ArrayList();
		ArrayList lights = new ArrayList();
		for (Iterator it = Animator.getInstance().getObjectList().iterator(); it.hasNext(); ) {
			Object o = it.next();
			if (o instanceof AnimModel) {
				models.add(o);
			}
			if (o instanceof AnimLight) {
				lights.add(o);
			}
		}
		
		
		switch (settings.iRenderer) {
			case JPatchSettings.RENDERER_INYO: {
				progressDisplay.clearText();
				progressDisplay.addText("Working directory is: " + settings.strWorkingDir + "\n");
				progressDisplay.addText("Invoking Inyo...");
				TextureParser.setTexturePath(settings.inyoSettings.strTexturePath);
				InyoRenderer3 renderer = new InyoRenderer3(models, Animator.getInstance().getActiveCamera(), lights);
				Image image = renderer.render(new RtInterface());
				if (image != null) {
					File imageFile = new File(settings.strWorkingDir, frameName + ".png");
					try {
						ImageIO.write((BufferedImage) image, "png", imageFile);
					} catch (IOException e) {
						e.printStackTrace();
					}
					progressDisplay.loadImage(imageFile);
				}
				//RendererThread rendererThread = new InyoRendererThread(renderer);
				//progressDisplay.setRendererThread(rendererThread);
				////imagePanel.setImage(image);
				////progressDisplay.repaint();
			}
			break;	
			case JPatchSettings.RENDERER_RIB: {
				RibRenderer4 renderer = new RibRenderer4();
				
				//models, Animator.getInstance().getActiveCamera(), lights);
				File ribFile = new File(settings.strWorkingDir, frameName + ".rib");
				progressDisplay.clearText();
				progressDisplay.addText("Working directory is: " + settings.strWorkingDir + "\n");
				progressDisplay.addText("Generating geometry...");
				
				try {
					BufferedWriter writer = new BufferedWriter(new FileWriter(ribFile));
					renderer.writeToFile(models, Animator.getInstance().getActiveCamera(), lights, writer, frameName + ".tif");
					writer.close();
				} catch (Exception exception) {
					exception.printStackTrace();
				}
				String[] ribCmd = { settings.ribSettings.strExecutable, frameName + ".rib" };
				String[] ribEnv = JPatchSettings.getEnv(settings.ribSettings.strEnv);
				
				
					File imageFile = new File(settings.strWorkingDir, frameName + ".tif");
					if (imageFile.exists()) imageFile.delete();
					
					StringBuffer sb = new StringBuffer();
					sb.append("Starting RenderMAN renderer using:\n");
					for (int i = 0; i < ribCmd.length; sb.append(ribCmd[i++]).append(" "));
					sb.append("\n");
					
					if (ribEnv != null) {
						sb.append("Environment variables:\n");
						for (int i = 0; i < ribEnv.length; sb.append(ribEnv[i++]).append("\n"));
					}
					
					sb.append("\n");
					sb.append("--------------------------------------------------------------------------------\n");
					progressDisplay.addText(sb.toString());
				try {
					Process rib = Runtime.getRuntime().exec(ribCmd, ribEnv, new File(settings.strWorkingDir));
					new ProcessMonitor(rib, progressDisplay);
					rib.waitFor();
					if (settings.bDeleteSources) ribFile.delete();
					progressDisplay.loadImage(imageFile);
				} catch (Exception exception) {
					exception.printStackTrace();
				}
			}
			break;
			case JPatchSettings.RENDERER_POVRAY: {
				//PovrayRenderer3 renderer = new PovrayRenderer3(models, Animator.getInstance().getActiveCamera(), lights);
				progressDisplay.clearText();
				progressDisplay.addText("Working directory is: " + settings.strWorkingDir + "\n");
				progressDisplay.addText("Generating geometry...");
				
				File povrayFile = new File(settings.strWorkingDir, frameName + ".pov");
				
				try {
					PovrayRenderer3 renderer = new PovrayRenderer3();
					BufferedWriter writer = new BufferedWriter(new FileWriter(povrayFile));
					renderer.writeFrame(models, camera1, lights, re.getRenderString("povray", ""), writer);
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				ArrayList listCmd = new ArrayList();
				listCmd.add(settings.povraySettings.strExecutable);
				if (settings.povraySettings.iVersion == JPatchSettings.POVRAY_UNIX) {
					listCmd.add("+I" + frameName + ".pov");
				} else {
					listCmd.add("/RENDER");
					listCmd.add(frameName + ".pov");
					listCmd.add("/EXIT");
				}
				listCmd.add("+O" + frameName + ".png");
				listCmd.add("+W" + settings.iRenderWidth);
				listCmd.add("+H" + settings.iRenderHeight);
				listCmd.add("-D");
				listCmd.add("-P");
				listCmd.add("+FN8");
				if (settings.povraySettings.iAaMethod != 0) {
					listCmd.add("+A" + settings.povraySettings.fAaThreshold);
					listCmd.add("+AM" + settings.povraySettings.iAaMethod);
					listCmd.add("+R" + settings.povraySettings.iAaLevel);
				} else {
					listCmd.add("-A");
				}
				if (settings.povraySettings.fAaJitter != 0) {
					listCmd.add("+J" + settings.povraySettings.fAaJitter);
				} else {
					listCmd.add("-J");
				}
				
				String[] povCmd = (String[]) listCmd.toArray(new String[0]);
				String[] povEnv = JPatchSettings.getEnv(settings.povraySettings.strEnv);
				
				StringBuffer sb = new StringBuffer();
				sb.append("Starting POV-Ray renderer using:\n");
				
				for (int i = 0; i < povCmd.length; sb.append(povCmd[i++]).append(" "));
				sb.append("\n");
				
				if (povEnv != null) {
					sb.append("Environment variables:\n");
					for (int i = 0; i < povEnv.length; sb.append(povEnv[i++]).append("\n"));
				}
				
				sb.append("\n");
				sb.append("--------------------------------------------------------------------------------\n");
				progressDisplay.addText(sb.toString());
				
				//try {
					File imageFile = new File(settings.strWorkingDir, frameName + ".png");
					if (imageFile.exists()) imageFile.delete();
					try {
						Process pov = Runtime.getRuntime().exec(povCmd, povEnv, new File(settings.strWorkingDir));
						progressDisplay.setRendererProcess(pov);
						new ProcessMonitor(pov, progressDisplay);
					
						pov.waitFor();
						if (settings.bDeleteSources) povrayFile.delete();
						progressDisplay.loadImage(imageFile);
					} catch (Exception e) {
						e.printStackTrace();
					}
					//RendererThread rendererThread = new RendererThread(pov);
					//progressDisplay.setRendererThread(rendererThread);
					
					//try {
					//	while (rendererThread.running) Thread.sleep(100);
					//} catch (InterruptedException e) {
					//	e.printStackTrace();
					//}
					//
					//////Process pov = Runtime.getRuntime().exec(povCmd);
					////System.out.println("pov started");
					////
					////BufferedReader br = new BufferedReader(new InputStreamReader(pov.getErrorStream()));
					////String line;
					////while ((line = br.readLine()) != null) {
					////	//System.out.println(line);
					////}
					////
					////pov.waitFor();
					////System.out.println("pov finished");
					//
					//if (settings.bDeleteSources) povrayFile.delete();
					//
					////imageFrame.setTitle(frameName);
					//if (imageFile.exists()) {
					//	//imageFrame.setTitle(frameName);
					//	Image image = ImageIO.read(imageFile);
					//	if (warn && image == null) {
					//		JOptionPane.showMessageDialog(getComponent(), "The image seems to be corrupted", "Can't display image", JOptionPane.WARNING_MESSAGE);
					//	} else {
					//		imagePanel.setImage(image);
					//		progressDisplay.repaint();
					//	}
					//} else JOptionPane.showMessageDialog(getComponent(), "The renderer did not output an image. Check the renderer settings.", "Can't find image", JOptionPane.WARNING_MESSAGE);
					//
				//} catch (Exception exception) {
				//	exception.printStackTrace();
				//}
			}
		}
	}
	
	//public void showImage(Image image) {
	//	imagePanel.setImage(image);
	//	//imageFrame.pack();
	//	//imageFrame.show();
	//	ProgressDisplay progressDisplay = new ProgressDisplay(imagePanel,0);
	//}
	
	public void renderingDone(Image image) {
		//progressDisplay.done();
		//imagePanel.setImage(image);
		//if (animationRenderer != null) animationRenderer.renderNextFrame();
	}
	
	//public void renderingDone() {
	//	progressDisplay.done();
	//	if (imageFile.exists()) {
	//		try {
	//			Image image;
	//			if (imageFile.getPath().endsWith(".tif")) image = (new ReadTiff()).loadImage(imageFile);
	//			else image = ImageIO.read(imageFile);
	//			imagePanel.setImage(image);
	//			if (animationRenderer != null) animationRenderer.renderNextFrame();
	//		} catch (Exception e) {
	//			JPatchSettings settings = JPatchSettings.getInstance();
	//			Image image = new BufferedImage(settings.iRenderWidth, settings.iRenderHeight, BufferedImage.TYPE_INT_RGB);
	//			Graphics g = image.getGraphics();
	//			g.setColor(Color.RED);
	//			g.drawString("Can't display image - error reading file", 16, 16);
	//			imagePanel.setImage(image);
	//		}
	//	}
	//	else {
	//		JPatchSettings settings = JPatchSettings.getInstance();
	//		Image image = new BufferedImage(settings.iRenderWidth, settings.iRenderHeight, BufferedImage.TYPE_INT_RGB);
	//		Graphics g = image.getGraphics();
	//		g.setColor(Color.RED);
	//		g.drawString("Can't display image - file not found", 16, 16);
	//		imagePanel.setImage(image);
	//	}
	//}
	
	public void renderAnimation() {
		final BDialog dialog = new BDialog(this, "Render animation", true);
		dialog.setResizable(false);
		final BTextField textFirst = new BTextField("1", 20);
		final BTextField textLast = new BTextField("" + ((int) (Math.round(fEnd - fStart)) + 1), 20);
		FormContainer form = new FormContainer(2, 2);
		form.add(new BLabel("First frame:"), 0, 0);
		form.add(new BLabel("Last frame:"), 0, 1);
		form.add(textFirst, 1, 0);
		form.add(textLast, 1, 1);
		RowContainer buttons = new RowContainer();
		BButton buttonOK = new BButton("OK");
		BButton buttonCancel = new BButton("Cancel");
		buttons.add(buttonOK);
		buttons.add(buttonCancel);
		dialog.addEventLink(WindowClosingEvent.class, dialog, "dispose");
		buttonCancel.addEventLink(CommandEvent.class, dialog, "dispose");
		buttonOK.addEventLink(CommandEvent.class, new Object() {
			private void processEvent() {
				try {
					int iFirst = Integer.parseInt(textFirst.getText());
					int iLast = Integer.parseInt(textLast.getText());
					dialog.dispose();
					new ProgressDisplay((int) Math.round(fStart + iFirst), (int) Math.round(fStart+ iLast));
				} catch (NumberFormatException e) {
				}
			}
		});
		
		ColumnContainer content = new ColumnContainer();
		content.add(form);
		content.add(buttons);
		
		dialog.setContent(content);
		dialog.pack();
		((Window) dialog.getComponent()).setLocationRelativeTo(getComponent());
		dialog.setVisible(true);
		
		//ProgressDisplay progressDisplay = new ProgressDisplay((int) Math.round(fStart + 1), (int) Math.round(fEnd + 1));
		//JPatchSettings settings = JPatchSettings.getInstance();
		//imagePanel.setImage(new BufferedImage(settings.iRenderWidth, settings.iRenderHeight, BufferedImage.TYPE_INT_RGB));
		//animationRenderer = new AnimationRenderer((int) Math.round(fStart), (int) Math.round(fEnd));
		//
		//int frameNumber = 0;
		//int frameCount = (int) Math.round(fEnd - fStart);
		//for (float frame = fStart; frame <= fEnd; frame++) {
		//	progressDisplay.set(frameNumber, frameCount);
		//	
		//	
		//	//String fr = (frameNumber < 10) ? "000" + frameNumber : (frameNumber < 100) ? "00" + frameNumber : (frameNumber < 10) ? "0" + frameNumber : "" + frameNumber;
		//	//String imageFile = "frame" + fr + ".png";
		//	//inyoCmd[3] = imageFile;
		//	//
		//	//try {
		//	//	Process inyo = Runtime.getRuntime().exec(inyoCmd, inyoEnv, new File(workingDir));
		//	//	BufferedReader br = new BufferedReader(new InputStreamReader(inyo.getInputStream()));
		//	//	String line;
		//	//	while ((line = br.readLine()) != null) {
		//	//		System.out.println(line);
		//	//	}
		//	//	inyo.waitFor();
		//	//	imagePanel.setImage(ImageIO.read(new File(workingDir + "/" + imageFile)));
		//	//	imageFrame.setTitle(imageFile);
		//	//} catch(Exception exception) {
		//	//	exception.printStackTrace();
		//	//}
		//	
		//	//double t = ((double) frame) / 10000.0;
		//	//double s = ((double) iStart) / 10000.0;
		//	//double df = (t - s) * dFramerate;
		//	//frame = iStart + (int) ((Math.floor(df + 0.1 / dFramerate) + 1) / dFramerate * 10000.0);
		//	frameNumber++;
		//}
	}
	
	public StringBuffer xml() {
//		StringBuffer indent = XMLutils.indent(1);
//		StringBuffer indent2 = XMLutils.indent(2);
//		StringBuffer linebreak = XMLutils.lineBreak();
		StringBuffer sb = new StringBuffer();
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>").append("\n");
		sb.append("<sequence>").append("\n");
		sb.append("\t<name>New Sequence</name>").append("\n");
		sb.append("\t<start>" + fStart + "</start>").append("\n");
		sb.append("\t<end>" + fEnd + "</end>").append("\n");
		sb.append("\t<framerate>" + fFramerate + "</framerate>").append("\n");
		sb.append("\t<prefix>" + strPrefix + "</prefix>").append("\n");
		sb.append(renderStrings("\t"));
		
		/* Cameras */
		
		for (Iterator it = listObjects.iterator(); it.hasNext(); ) {
			Object o = it.next();
			if (o instanceof Camera) {
				Camera cam = (Camera) o;
				sb.append("\t<camera>").append("\n");
				sb.append("\t\t<name>" + cam.getName() + "</name>").append("\n");
				getMotionCurveSetFor(cam).xml(sb, "\t\t");
				sb.append("\t</camera>").append("\n");
			}
		}
		
		/* Lights */
		
		for (Iterator it = listObjects.iterator(); it.hasNext(); ) {
			Object o = it.next();
			if (o instanceof AnimLight) {
				AnimLight light = (AnimLight) o;
				sb.append("\t<lightsource>").append("\n");
				sb.append("\t\t<name>" + light.getName() + "</name>").append("\n");
				if (!light.isActive())
					sb.append("\t\t<inactive/>").append("\n");
				sb.append(light.renderStrings("\t\t"));
				getMotionCurveSetFor(light).xml(sb, "\t\t");
				sb.append("\t</lightsource>").append("\n");
			}
		}
		
		/* Models */
		
		for (Iterator it = listObjects.iterator(); it.hasNext(); ) {
			Object o = it.next();
			if (o instanceof AnimModel) {
				AnimModel animModel = (AnimModel) o;
				String filename = (String) mapFilenames.get(animModel);
				sb.append("\t<model>").append("\n");
				sb.append("\t\t<name>" + animModel.getName() + "</name>").append("\n");
				sb.append("\t\t<filename>" + filename + "</filename>").append("\n");
				int sdo = animModel.getSubdivisionOffset();
				if (sdo != 0) {
					sb.append("\t\t<subdivisionoffset>" + sdo + "</subdivisionoffset>").append("\n");
				}
				sb.append(animModel.renderStrings("\t\t"));
				getMotionCurveSetFor(animModel).xml(sb, "\t\t");
				sb.append("\t</model>").append("\n");
			}
		}
		
		sb.append("</sequence>").append("\n");
		return sb;
	}
	
	//public void setResolution(int x, int y) {
	//	iAnimWidth = x;
	//	iAnimHeight = y;
	//}
	//
	//public void setAspectratio(double x, double y) {
	//	;
	//}
	
	public  void setFramerate(float fps) {
		fFramerate = fps;
	}
	
	public void setStart(float start) {
		fStart = start;
	}
	
	public void setEnd(float end) {
		fEnd = end;
	}
	
	static class ImagePanel extends JPanel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private Image image;
		
		ImagePanel() {
			setImage(new BufferedImage(JPatchSettings.getInstance().iRenderWidth, JPatchSettings.getInstance().iRenderHeight, BufferedImage.TYPE_INT_RGB));
		}
		
		void setImage(Image image) {
			this.image = image;
			setPreferredSize(new Dimension(image.getWidth(null), image.getHeight(null)));
			repaint();
		}
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			if (image != null) g.drawImage(image, 0, 0, this);
		}
	}
	
	class ProgressDisplay extends BDialog {
		private ColumnContainer columnContainer = new ColumnContainer();
		private ImagePanel imagePanel;
		private BProgressBar progressBar = new BProgressBar();
		private BButton buttonAbort = new BButton("Abort");
		private BButton buttonClose = new BButton("Close");
		private BTextArea textArea = new BTextArea(10, 80);
		private BScrollBar scrollBar;
		private OverlayContainer overlayContainer = new OverlayContainer();
		private final int frames;
		private volatile StopableThread rendererThread;
		private volatile Process rendererProcess;
		
		ProgressDisplay(final int start, int stop) {
			super(Animator.getInstance(), true);	// make dialog modal
			frames = stop - start + 1;
			imagePanel = new ImagePanel();
			progressBar.setMinimum(0);
			progressBar.setMaximum(frames);
			textArea.setWrapStyle(BTextArea.WRAP_WORD);
			textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
			textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
			textArea.setEditable(false);
			textArea.setBackground(Color.BLACK);
			textArea.getComponent().setForeground(Color.GREEN);
			BScrollPane scrollPane = new BScrollPane(textArea, BScrollPane.SCROLLBAR_NEVER, BScrollPane.SCROLLBAR_ALWAYS);
			scrollBar = scrollPane.getVerticalScrollBar();
			LayoutInfo layoutInfo = new LayoutInfo(LayoutInfo.CENTER, LayoutInfo.HORIZONTAL);
			overlayContainer.add(buttonAbort);
			overlayContainer.add(buttonClose);
			buttonClose.setVisible(false);
			columnContainer.add(progressBar, layoutInfo);
			columnContainer.add(new AWTWidget(imagePanel));
			columnContainer.add(scrollPane, layoutInfo);
			columnContainer.add(overlayContainer);
			
			buttonAbort.addEventLink(CommandEvent.class, this, "abort");
			buttonClose.addEventLink(CommandEvent.class, this, "dispose");
			addEventLink(WindowClosingEvent.class, this, "close");
			setContent(columnContainer);
			pack();
			setResizable(false);
			
			rendererThread = new StopableThread() {
				public void run() {
					running = true;
					for (int f = 0; f < frames && running; f++) {
						int fn = start + f - (int) (Math.round(fStart));
						//clearText();
						setTitle("Rendering frame " + fn + " (" + (f + 1) + " of " + frames + ")");
						progressBar.setValue(f);
						setPosition(start + f - 1);
						String frameName = strPrefix + (
						(fn < 10) ? "00000" + fn :
						(fn < 100) ? "0000" + fn :
						(fn < 1000) ? "000" + fn :
						(fn < 10000) ? "00" + fn :
						(fn < 100000) ? "0" + fn :
						"" + fn);
						renderFrame(frameName, ProgressDisplay.this);
					}
					running = false;
					rendererThread = null;
					rendererProcess = null;
					done();
					setTitle("Done. " + frames + " frames rendered.");
				}
			};
			rendererThread.start();
			setVisible(true);
		}
		
		class StopableThread extends Thread {
			volatile boolean running;
		}
		
		void setImage(Image image) {
			imagePanel.setImage(image);
		}
		
		void loadImage(File imageFile) {
			if (rendererThread != null && rendererThread.running) {
				if (imageFile.exists()) {
					try {
						Image image;
						if (imageFile.getPath().endsWith(".tif") && !ImageIO.getImageReadersByFormatName("tiff").hasNext()) {
							addText("");
							addText("********************************************************************************");
							addText("The renderer generated a tiff image, but no ImageIO tiff-reader is available.");
							addText("Plase install the Java Advanced Imagige (JAI) IO Tools package available from Sun Microsystems at http://java.sun.com/products/java-media/jai/current.html");
							addText("********************************************************************************");
							addText("");
							addText("Attempting to load the tiff image with JPatch's builtin tiff-reader (which can't handle compressed files)...");
							addText("");
							image = (new ReadTiff()).loadImage(imageFile);
						}
						else image = ImageIO.read(imageFile);
						Graphics g = image.getGraphics();
						g.setColor(Color.BLACK);
						g.drawString(imageFile.getParent() + File.separator, 8, 16);
						g.drawString(imageFile.getName(), 8, 32);
						g.setColor(Color.WHITE);
						g.drawString(imageFile.getParent() + File.separator, 7, 15);
						g.drawString(imageFile.getName(), 7, 31);
						imagePanel.setImage(image);
					} catch (Exception e) {
						JPatchSettings settings = JPatchSettings.getInstance();
						Image image = new BufferedImage(settings.iRenderWidth, settings.iRenderHeight, BufferedImage.TYPE_INT_RGB);
						Graphics g = image.getGraphics();
						g.setColor(Color.RED);
						g.drawString("Can't display image - error reading file", 8, 16);
						imagePanel.setImage(image);
					}
				}
				else {
					JPatchSettings settings = JPatchSettings.getInstance();
					Image image = new BufferedImage(settings.iRenderWidth, settings.iRenderHeight, BufferedImage.TYPE_INT_RGB);
					Graphics g = image.getGraphics();
					g.setColor(Color.RED);
					g.drawString("Can't display image - file not found", 8, 16);
					imagePanel.setImage(image);
				}
			}
		}
	
		void setRendererProcess(Process process) {
			rendererProcess = process;
		}
		
		private void abort() {
			if (rendererThread != null) {
				rendererThread.running = false;
				rendererThread.stop();
			}
			if (rendererProcess != null) rendererProcess.destroy();
			setTitle("Rendering aborted");
			done();
		}
		
		private void close() {
			if (rendererThread != null && rendererThread.running) {
				if (JOptionPane.showConfirmDialog(getComponent(), "Do you want to stop the rendering process?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
					rendererThread.running = false;
					if (rendererProcess != null) rendererProcess.destroy();
					dispose();
				}
			} else dispose();
		}
			
		
		
		void done() {
			buttonAbort.setVisible(false);
			buttonClose.setVisible(true);
			progressBar.setValue(progressBar.getMaximum());
		}
		
		synchronized void clearText() {
			textArea.setText("");
		}
		
		synchronized void addText(String line) {
			textArea.append(line);
			textArea.append("\n");
			scrollBar.setValue(scrollBar.getMaximum());
			textArea.repaint();
		}
		
		//void done() {
		//	set(frame + 1, frames);
		//}
		//
		//void set(int frame, int frames) {
		//	this.frames = frames;
		//	this.frame = frame;
		//	progressBar.setIndeterminate(frame == 0 && frames == 1);
		//	progressBar.setMaximum(frames);
		//	progressBar.setValue(frame);
		//	if (frame < frames) {
		//		setTitle("Rendering frame " + (frame + 1) + " of " + frames);
		//	} else if (frame == frames) {
		//		setTitle("Rendering of " + frames + " frames finished");
		//	}
		//}
	}
	
	class ProcessMonitor {
		ProcessMonitor(final Process process, final ProgressDisplay progressDisplay) {
			(new Thread() {
				public void run() {
					String line;
					BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
					try {
						while ((line = br.readLine()) != null) progressDisplay.addText(line);
					} catch (IOException e) {
						progressDisplay.addText("*** ABORTED ***");
					}
				}
			}).start();
			(new Thread() {
				public void run() {
					String line;
					BufferedReader br = new BufferedReader(new InputStreamReader(process.getErrorStream()));
					try {
						while ((line = br.readLine()) != null) progressDisplay.addText(line);
					} catch (IOException e) {
						progressDisplay.addText("*** ABORTED ***");
					}
				}
			}).start();
		}
	}
}

