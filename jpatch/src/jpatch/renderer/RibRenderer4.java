package jpatch.renderer;

import java.io.*;
import java.util.*;

import javax.vecmath.*;
import jpatch.entity.*;
import jpatch.boundary.*;
import jpatch.boundary.settings.RendermanSettings;
import jpatch.boundary.settings.Settings;

public class RibRenderer4 implements Renderer {
	private volatile boolean abort = false;
	
	//private BufferedWriter file;
	//
	//private List animModels;
	//private List lights;
	//private Camera camera;
	private PatchTesselator3 patchTesselator = new PatchTesselator3();
	
	//public RibRenderer4(List animModels, Camera camera, List lights) {
	//	this.animModels = animModels;
	//	this.camera = camera;
	//	this.lights = lights;
	//}
	
	
	public void writeToFile(List animModels, Camera camera, List lights, BufferedWriter file, String frameName) {
		Settings settings = Settings.getInstance();
		
		try {
			String[] filter = { "box", "triangle", "catmullrom", "gaussian", "sinc" };
			float screenHeight = settings.export.aspectHeight / settings.export.aspectWidth;
			float pixelAspect = (float) settings.export.imageWidth / (float) settings.export.imageHeight * screenHeight;
			float[] rgb = settings.export.backgroundColor.get().getRGBColorComponents(new float[3]);
			
			//file = new BufferedWriter(new FileWriter(f));
			file.write("##RenderMan RIB-Structure 1.1\n");
			file.write("##Creator JPatch pre0.4 development version - http://www.jpatch.com\n");
			file.write("\n\n");
			file.write("FrameBegin 1\n");
			file.write("\n");
			file.write("Display \"" + frameName + "\" \"tiff\" \"rgb\" \"string compression\" \"none\" \n");
			file.write("Format " + settings.export.imageWidth + " " + settings.export.imageHeight + " " + pixelAspect + "\n");
			file.write("PixelSamples " + settings.export.renderman.pixelSamplesX + " " + settings.export.renderman.pixelSamplesY + "\n");
			file.write("PixelFilter \"" + settings.export.renderman.pixelFilter + "\" " + settings.export.renderman.pixelFilterX + " " + settings.export.renderman.pixelFilterY + "\n");
			file.write("ShadingRate " + settings.export.renderman.shadingRate + "\n");
			switch (settings.export.renderman.shadingInterpolation) {
			case CONSTANT:
				file.write("ShadingInterpolation \"constant\"\n");
				break;
			case SMOOTH:
				file.write("ShadingInterpolation \"smooth\"\n");
				break;
			}
			file.write("\n");
			//file.write("Declare \"background\" \"color\"\n");
			file.write("Imager \"background\" \"color background\" [" + rgb[0] + " " + rgb[1] + " " + rgb[2] + "]\n");
			file.write("\n");
			Matrix4d cam = new Matrix4d(camera.getTransform());
			cam.invert();
			file.write("Projection \"perspective\" \"fov\" [" + camera.getFieldOfView() + "]\n");
			file.write("ScreenWindow -1 1 " + (-screenHeight) + " " + screenHeight + "\n");
			file.write("Transform [" + cam.m00 + " " + cam.m10 + " " + cam.m20 + " " + cam.m30 + " " +
						   cam.m01 + " " + cam.m11 + " " + cam.m21 + " " + cam.m31 + " " +
						   cam.m02 + " " + cam.m12 + " " + cam.m22 + " " + cam.m32 + " " +
						   cam.m03 + " " + cam.m13 + " " + cam.m23 + " " + cam.m33 + "]\n");
			file.write("Orientation \"rh\"\n");
			file.write("\n");
			file.write("WorldBegin\n");
			file.write("\n");
			for (int i = 0, n = lights.size(); i < n; i++) {
				if (abort)
					return;
				AnimLight light = (AnimLight) lights.get(i);
				if (light.isActive()) file.write(light(light));
			}
			file.write("\n");
			
			for (Iterator it = animModels.iterator(); it.hasNext(); ) {
				if (abort)
					return;
				AnimModel animModel = (AnimModel) it.next();
				Model model = animModel.getModel();
				
				Matrix4d m = animModel.getTransform();
				file.write("TransformBegin\n");
				file.write("Transform [" + m.m00 + " " + m.m10 + " " + m.m20 + " " + m.m30 + " " +
					  		   m.m01 + " " + m.m11 + " " + m.m21 + " " + m.m31 + " " +
							   m.m02 + " " + m.m12 + " " + m.m22 + " " + m.m32 + " " +
							   m.m03 + " " + m.m13 + " " + m.m23 + " " + m.m33 + "]\n");
				file.write("\n");
				
				writeModel(model, m, animModel.getRenderString("renderman", ""), animModel.getSubdivisionOffset(), file);
				
				file.write("TransformEnd\n\n");
			}
			file.write("WorldEnd\n");
			file.write("FrameEnd\n");
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
	}
	
	public void writeModel(Model model, Matrix4d m, String renderString, int subdivOffset, BufferedWriter file) throws IOException {
		if (Settings.getInstance().export.renderman.outputMode != RendermanSettings.Mode.BICUBIC_PATCHES) {
			int subdiv = Settings.getInstance().export.renderman.subdivisionLevel + subdivOffset;
			if (subdiv < 1) subdiv = 1;
			if (subdiv > 5) subdiv = 5;
			patchTesselator.tesselate(model, subdiv, null, Settings.getInstance().export.renderman.outputMode != RendermanSettings.Mode.CATMULL_CLARK_SUBDIVISION_SURFACE);
			
			for (Iterator itMat = model.getMaterialList().iterator(); itMat.hasNext(); ) {
				if (abort)
					return;
				JPatchMaterial material = (JPatchMaterial) itMat.next();
				
				PatchTesselator3.Vertex[] vtx = patchTesselator.getPerMaterialVertexArray(material);
				if (vtx.length > 0) {
					file.write(renderString);
					file.write("AttributeBegin\n");
					file.write(AbstractRenderer.shader(material.getMaterialProperties(), material.getRenderString("renderman","")));
					
					switch (Settings.getInstance().export.renderman.outputMode) {
						case TRIANGLES: {
							file.write("PointsPolygons [");
							int[][] triangles = patchTesselator.getPerMaterialTriangleArray();
							for (int i = 0; i < triangles.length; i++) file.write("3 ");
							file.write("]\n[");
							for (int t = 0; t < triangles.length; t++) file.write(triangles[t][0] + " " + triangles[t][1] + " " + triangles[t][2] + " ");
							file.write("]\n\"P\" [");
							for (int v = 0; v < vtx.length; v++) file.write(vtx[v].p.x + " " + vtx[v].p.y + " " + vtx[v].p.z + " ");
							file.write("]\n\"vertex point Pref\" [");
							for (int v = 0; v < vtx.length; v++) file.write(vtx[v].r.x + " " + vtx[v].r.y + " " + vtx[v].r.z + " ");
							file.write("]\n\"N\" [");
							for (int v = 0; v < vtx.length; v++) file.write(vtx[v].n.x + " " + vtx[v].n.y + " " + vtx[v].n.z + " ");
							file.write("]\n");
						}
						break;
						case QUADRILATERALS: {
							file.write("PointsPolygons [");
							int[][] quads = patchTesselator.getPerMaterialQuadArray();
							for (int i = 0; i < quads.length; i++) file.write(quads[i].length + " ");
							file.write("]\n[");
							for (int t = 0; t < quads.length; t++) {
								for (int v = 0; v < quads[t].length; v++) {
									file.write(quads[t][v] + " ");
								}
							}
							file.write("]\n\"P\" [");
							for (int v = 0; v < vtx.length; v++) file.write(vtx[v].p.x + " " + vtx[v].p.y + " " + vtx[v].p.z + " ");
							file.write("]\n\"vertex point Pref\" [");
							for (int v = 0; v < vtx.length; v++) file.write(vtx[v].r.x + " " + vtx[v].r.y + " " + vtx[v].r.z + " ");
							file.write("]\n\"N\" [");
							for (int v = 0; v < vtx.length; v++) file.write(vtx[v].n.x + " " + vtx[v].n.y + " " + vtx[v].n.z + " ");
							file.write("]\n");
						}
						break;
						case CATMULL_CLARK_SUBDIVISION_SURFACE: {
							file.write("SubdivisionMesh \"catmull-clark\" [ ");
							int[][] quads = patchTesselator.getPerMaterialQuadArray();
							for (int i = 0; i < quads.length; i++) file.write(quads[i].length + " ");
							file.write("]\n[");
							for (int t = 0; t < quads.length; t++) {
								for (int v = 0; v < quads[t].length; v++) {
									file.write(quads[t][v] + " ");
								}
							}
							file.write("]\n[\"interpolateboundary\"] [0 0] [] [] \"P\" [");
							for (int v = 0; v < vtx.length; v++) file.write(vtx[v].p.x + " " + vtx[v].p.y + " " + vtx[v].p.z + " ");
							file.write("]\n\"vertex point Pref\" [");
							for (int v = 0; v < vtx.length; v++) file.write(vtx[v].r.x + " " + vtx[v].r.y + " " + vtx[v].r.z + " ");
							file.write("]\n");
						}
						break;
					}
					file.write("AttributeEnd\n\n");
				}
			}
		}
		else {
			file.write("Basis \"bezier\" 3 \"bezier\" 3\n");
			for (Iterator itMat = model.getMaterialList().iterator(); itMat.hasNext(); ) {
				if (abort)
					return;
				JPatchMaterial material = (JPatchMaterial) itMat.next();
				boolean active = false;
				for (Iterator it = model.getPatchSet().iterator(); it.hasNext(); ) {
					Patch patch = (Patch) it.next();
					if (patch.getMaterial() == material) {
						if (!active) {
							file.write(renderString);
							file.write("AttributeBegin\n");
							file.write("" + AbstractRenderer.shader(material.getMaterialProperties(), material.getRenderString("renderman","")));
							active = true;
						}
						Point3f[][] bicubicPatches = patch.bicubicPatches();
						Point3f[][] refBicubicPatches = patch.bicubicReferencePatches();
							
						for (int p = 0; p < bicubicPatches.length; p++) {
							{
								Point3f[] bezierControlPoints = bicubicPatches[p];
								
								file.write("Patch \"bicubic\" \"P\" [");
								String strCPs = "";
								for (int c = 0; c < 16; c++) {
									strCPs += (bezierControlPoints[c].x + " " + bezierControlPoints[c].y + " " + bezierControlPoints[c].z);
									if (c != 15) {
										strCPs += (" ");
									}
								}
								file.write(strCPs + "]\n");
							}
							if (refBicubicPatches != null) {
								Point3f[] bezierControlPoints = refBicubicPatches[p];
								file.write("\"vertex point Pref\" [");
								String strCPs = "";
								for (int c = 0; c < 16; c++) {
									strCPs += (bezierControlPoints[c].x + " " + bezierControlPoints[c].y + " " + bezierControlPoints[c].z);
									if (c != 15) {
										strCPs += (" ");
									}
								}
								file.write(strCPs + "]\n");
							}
						}
					}
				}
				if (active) {
					file.write("AttributeEnd\n");
				}
			}
		}
	}
	
	private static String toRibVector(Tuple3d t) {
		return "[" + t.x + " " + t.y + " " + t.z + "]";
	}
	
	private static String toRibVector(Tuple3f t) {
		return "[" + t.x + " " + t.y + " " + t.z + "]";
	}
	
	public static String light(AnimLight light) {
		String s = light.getRenderString("renderman", "");
		s = AbstractRenderer.light(light, s);
		s = s.replaceAll("\\$position",toRibVector(light.getPositionDouble()));
		s = s.replaceAll("\\$color",toRibVector(light.getColor()));
		return s;
	}

	public synchronized void abort() {
		abort = true;
		patchTesselator.abort();
	}
}
