package jpatch.renderer;

import java.awt.Image;
import java.util.*;
import javax.vecmath.*;
import jpatch.entity.*;
import jpatch.boundary.*;
import jpatch.boundary.settings.InyoSettings;
import jpatch.boundary.settings.Settings;

import inyo.*;

public class InyoRenderer3 implements Renderer {
	
	private Image image;
	private List models;
	private List lights;
	private Camera camera;
	private PatchTesselator3 patchTesselator = new PatchTesselator3();
	private JPatchInyoInterface inyo;
	private volatile boolean abort = false;
	
	public InyoRenderer3(List models, Camera camera, List lights) {
		this.models = models;
		this.camera = camera;
		this.lights = lights;
	}
	
	public Image render(final JPatchInyoInterface inyo) {
		//Model model = MainFrame.getInstance().getModel();
		//model.computePatches();
		this.inyo = inyo;
		Settings settings = Settings.getInstance();
		
		inyo.setImageSize(settings.export.imageHeight, settings.export.imageWidth, 1);
		//inyo.setAspectRatio() ???
		
		Matrix4d cam = new Matrix4d(camera.getTransform());
		cam.invert();
		inyo.setCamera(cam);
		inyo.setCameraFocalLength((float) camera.getFocalLength());
		
		inyo.setMaxRecursionDepth(settings.export.inyo.recursionDepth);
		inyo.setSoftShadowSamples(settings.export.inyo.shadowSamples);
		inyo.setTransparentShadows(settings.export.inyo.transparentShadows);
		inyo.setCaustics(settings.export.inyo.caustics, settings.export.inyo.oversampleCaustics);
		inyo.setUseAmbientOcclusion(settings.export.inyo.ambientOcclusion);
		inyo.setAmbientOcclusion(settings.export.inyo.ambientOcclusionDistance);
		inyo.setAmbientOcclusionSamples(settings.export.inyo.ambientOcclusionSamples);
		inyo.ambientOcclusionColorBleed(settings.export.inyo.ambientOcclusionColorbleed);
		inyo.setOversample(settings.export.inyo.supersamplingLevel, settings.export.inyo.supersamplingMode == InyoSettings.Supersampling.ADAPTIVE);
		
		inyo.setShowStats(false);
		
		/*
		 * background
		 */
		float[] skyColor = new float[3];
		settings.export.backgroundColor.get().getRGBColorComponents(skyColor);
		inyo.setSkyColor(skyColor[0], skyColor[1], skyColor[2]);
		
		/*
		 * lightsources
		 */
		for (Iterator it = lights.iterator(); it.hasNext(); ) {
			if (abort)
				return null;
			AnimLight light = (AnimLight) it.next();
			if (light.isActive()) {
				Point3d p = light.getPositionDouble();
				inyo.addLight(p.x, p.y, p.z, light.getIntensity());
				Color3f c = light.getColor();
				inyo.setLightColor(c.x, c.y, c.z);
				inyo.setLightRadius(light.getSize());
			}
		}
		
		
		for (Iterator it = models.iterator(); it.hasNext(); ) {
			AnimModel animModel = (AnimModel) it.next();
			Model model = animModel.getModel();
			int subdiv = Settings.getInstance().export.inyo.subdivisionLevel + animModel.getSubdivisionOffset();
			if (subdiv < 2) subdiv = 2;
			if (subdiv > 5) subdiv = 5;
			patchTesselator.tesselate(model, subdiv, animModel.getTransform(), true);
	
			
			
			for (Iterator iterator = model.getMaterialList().iterator(); iterator.hasNext();) {
				if (abort)
					return null;
				JPatchMaterial material = (JPatchMaterial)iterator.next();
				PatchTesselator3.Vertex[] vtx = patchTesselator.getPerMaterialVertexArray(material);
				int[][] triangles = patchTesselator.getPerMaterialTriangleArray();
				if (triangles.length > 0) {
					inyo.objectBegin();
					
					
					MaterialProperties mp = material.getMaterialProperties();
					inyo.addMaterial(mp.red, mp.green, mp.blue);
					inyo.setMaterialFilter(mp.filter);
					inyo.setMaterialTransmit(mp.transmit);
					inyo.setMaterialAmbient(mp.ambient);
					inyo.setMaterialDiffuse(mp.diffuse);
					inyo.setMaterialBrilliance(mp.brilliance);
					inyo.setMaterialSpecular(mp.specular);
					inyo.setMaterialRoughness(mp.roughness);
					inyo.setMaterialMetallic(mp.metallic);
					inyo.setMaterialReflection(mp.reflectionMin, mp.reflectionMax, mp.reflectionFalloff);
					inyo.setMaterialRefraction(mp.refraction);
					inyo.setMaterialConserveEnergy(mp.conserveEnergy);
					inyo.setMaterialTexture(material.getRenderString("inyo",""));
					for (int i = 0; i < vtx.length; i++) {
						if (abort)
							return null;
						PatchTesselator3.Vertex v = vtx[i];
						inyo.addVertex(v.p.x, v.p.y, v.p.z, v.r.x, v.r.y, v.r.z, v.n.x, v.n.y, v.n.z);
					}
					
					for (int i = 0; i < triangles.length; i++) {
						if (abort)
							return null;
						inyo.addTriangle(triangles[i][0], triangles[i][1], triangles[i][2]);
					}
					
					inyo.objectEnd();
				}
				
			}
			
			
		}
		
		System.out.println("INYO START");
		Thread renderer = new Thread() {
			public void run() {
				inyo.startRendering(new InyoJPatchInterface() {
					/**
					 * Tell JPatch about the rendering progress
					 * @param progress 0.0 means rendering just started, 0.5 means half way done, 1.0 means rendering finished.
					 */
					public void progress(double progress) {
						System.out.println("progress: " + progress);
						// TODO: looks like Inyo doesn't talk to us :/
					}
					
					/**
					 * Pass the rendered image back to JPatch
					 * @param image the final image
					 */
					public void renderingDone(java.awt.Image image) {
						InyoRenderer3.this.image = image;
					}
				});
			}
		};
		renderer.start();
		try {
			while (renderer.isAlive()) {
				double progress = inyo.getProgress();
				// TODO: doesn't help - progress seems to be always 1.0 :-/
//				System.out.println("PROGRESS = " + progress);
				renderer.join(250);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return image;
	}
	
	public synchronized void abort() {
		abort = true;
		inyo.stopRendering();
	}
}
